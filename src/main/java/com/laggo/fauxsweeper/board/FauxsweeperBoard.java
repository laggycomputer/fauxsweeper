package com.laggo.fauxsweeper.board;

import com.laggo.fauxsweeper.cell.*;
import com.laggo.fauxsweeper.config.Configuration;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A complete board of Fauxsweeper, with UI and other state.
 *
 * @param <CellT> The type of {@link ICell} this board uses.
 */
public class FauxsweeperBoard<CellT extends ICell> {
    private final double guiScale;
    private final Font FONT;
    private final int width;
    private final int height;
    private final int mineCount;
    private final Random rand;
    private final Class<CellT> cellTRef;
    private final HashMap<BoardLocation, CellT> cells = new HashMap<>();
    private final IntegerProperty gameTime = new SimpleIntegerProperty(this, "gameTime", 0);
    private final boolean timerEnabled;
    private final StackPane upperPane = new StackPane();
    private final Pane gamePane;
    BooleanProperty isMouseDown = new SimpleBooleanProperty(this, "isMouseDown", false);
    private GameState gameState = GameState.FIRST;
    private ICell clickedMine;
    private Timer timer = new Timer(true);

    /**
     * Instantiates a new Fauxsweeper board.
     *
     * @param cellTRef     Another reference to the type of cell being used. This is necessary for reflection.
     * @param width        The width of the board.
     * @param height       The height of the board.
     * @param mineCount    The number of mines on the board.
     * @param timerEnabled Whether to update the in-game timer.
     * @param seed         The seed to use for RNG, if any.
     * @param guiScale     The scale for the GUI.
     */
    public FauxsweeperBoard(Class<CellT> cellTRef, int width, int height, int mineCount, boolean timerEnabled, Long seed, double guiScale) {
        this.cellTRef = cellTRef;

        this.width = width;
        this.height = height;
        this.mineCount = mineCount;
        this.timerEnabled = timerEnabled;

        this.rand = (seed == null) ? new Random() : new Random(seed);

        this.guiScale = guiScale;

        this.fillWithEmptyCells();
        this.placeMines(this.mineCount);
        this.computeNumberedCells();

        this.isMouseDown.addListener(evt -> this.updateUpperPane());
        this.gameTime.addListener(evt -> this.updateUpperPane());

        Pane boardPane = this.getAnyCell().getBoardPane();
        this.gamePane = new VBox(this.upperPane, boardPane);

        this.updateGamePane();

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onTimerTick();
            }
        }, 1000, 1000);

        for (Node child : this.gamePane.getChildren()) {
            child.setOnMousePressed(this::handleMouseDown);
            child.setOnMouseReleased(this::handleMouseUp);
        }

        this.FONT = Font.loadFont(Objects.requireNonNull(FauxsweeperBoard.class.getResourceAsStream("/Minecraftia-Regular.ttf")), -1);
    }

    /**
     * Create a board from a {@link Configuration}.
     *
     * @param config The configuration to use.
     * @return A new board object.
     */
    public static FauxsweeperBoard<? extends ICell> fromConfiguration(Configuration config) {
        // TODO: different cell types
        return new FauxsweeperBoard<>(SquareCell.class, config.getBoardWidth(), config.getBoardHeight(), config.getMineCount(), config.isTimerEnabled(), config.usesSetSeed() ? config.getSetSeed() : null, config.getGuiScale());
    }

    private void onTimerTick() {
        Platform.runLater(() -> {
            if (!this.isGameOver()) {
                this.gameTime.set(this.gameTime.get() + 1);
            }
        });
    }

    private void fillWithEmptyCells() {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                BoardLocation loc = new BoardLocation(x, y);
                // actually, this should never fail
                try {
                    this.cells.put(loc, cellTRef.getConstructor(FauxsweeperBoard.class, BoardLocation.class).newInstance(this, loc));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("this really shouldn't happen");
                }
            }
        }
    }

    private void placeMines(int count) {
        BoardLocation potentialLoc;
        while (count > 0) {
            potentialLoc = new BoardLocation(rand.nextInt(this.width), rand.nextInt(this.height));
            if (this.getCellAt(potentialLoc).getValue() != CellValue.MINE) {
                this.getCellAt(potentialLoc).setValue(CellValue.MINE);
                count--;
            }
        }
    }

    private CellT getAnyCell() {
        return this.cells.values().stream().findAny().orElse(null);
    }

    public Set<CellT> getAllCells() {
        return new HashSet<>(this.cells.values());
    }

    private void computeNumberedCells() {
        for (CellT cell : this.cells.values().stream().filter(c -> c.getValue() != CellValue.MINE).collect(Collectors.toSet())) {
            cell.setValue(CellValue.values()[(int) cell.getNeighbors().stream().filter(n -> n.getValue() == CellValue.MINE).count()]);
        }
    }

    private void secretlyMoveMine(BoardLocation mineLoc) {
        BoardLocation oldMineLoc = mineLoc.clone();

        while (this.getCellAt(mineLoc).getValue() == CellValue.MINE) {
            mineLoc = new BoardLocation(this.rand.nextInt(this.width), this.rand.nextInt(this.height));
        }

        this.getCellAt(mineLoc).setValue(CellValue.MINE);
        this.getCellAt(oldMineLoc).setValue(CellValue.ZERO);  // will be recomputed
        this.computeNumberedCells();
    }

    /**
     * @return The mine which was responsible for ending the game.
     */
    public ICell getClickedMine() {
        return this.clickedMine;
    }

    /**
     * Wipes the state of the board to start a new game.
     */
    public void newGame() {
        this.cells.clear();
        this.clickedMine = null;
        this.gameState = GameState.FIRST;
        this.gameTime.set(0);

        this.fillWithEmptyCells();
        this.placeMines(this.mineCount);
        this.computeNumberedCells();
        this.updateGamePane();

        Pane boardPane = this.getAnyCell().getBoardPane();
        this.gamePane.getChildren().set(1, boardPane);

        // we must reschedule otherwise the time will increment at a wonky point
        this.timer.cancel();
        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onTimerTick();
            }
        }, 1000, 1000);
    }

    /**
     * Gets the cell at a certain {@link BoardLocation}.
     *
     * @param loc The location to find a cell at.
     * @return The found cell.
     */
    public CellT getCellAt(BoardLocation loc) {
        return this.cells.get(loc);
    }

    /**
     * @return Whether the game has ended for any reason.
     */
    public boolean isGameOver() {
        return this.gameState == GameState.WON || this.gameState == GameState.LOST;
    }

    /**
     * Handle a click somewhere on the board.
     *
     * @param evt The {@link MouseEvent} in question.
     */
    public void handleBoardClick(MouseEvent evt) {
        if (evt.getTarget() instanceof CellButton) {
            if (evt.getButton() == MouseButton.PRIMARY) {
                ClickResult result = Objects.requireNonNull(((CellButton) evt.getTarget()).getCell()).onLeftClick();
                if (result == ClickResult.FAIL) {
                    if (this.gameState == GameState.FIRST) {
                        this.gameState = GameState.IN_PROGRESS;
                        this.secretlyMoveMine(((CellButton) evt.getTarget()).getCell().getLocation());
                        ((CellButton) evt.getTarget()).getCell().onLeftClick();
                    } else {
                        this.gameState = GameState.LOST;
                        this.clickedMine = ((CellButton) evt.getTarget()).getCell();
                        this.revealAll();
                    }
                } else if (result == ClickResult.OK) {
                    this.gameState = GameState.IN_PROGRESS;
                    if (this.cells.values().stream().noneMatch(c -> !c.isRevealed() && c.getValue() != CellValue.MINE)) {
                        this.gameState = GameState.WON;
                        this.revealAll();
                    }
                }
            } else if (evt.getButton() == MouseButton.SECONDARY) {
                Objects.requireNonNull(((CellButton) evt.getTarget()).getCell()).onRightClick(evt);
            }
        }

        this.updateGamePane();
    }

    private void revealAll() {
        for (CellT cell : this.cells.values()) {
            cell.reveal();
        }
    }

    /**
     * @param xRay Whether to ignore the state of the game and forcibly show the state of every cell.
     * @return The board state as an ASCII table.
     */
    public String dump(boolean xRay) {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; y++) {
                CellT thisCell = this.getCellAt(new BoardLocation(x, y));
                char actualState = CellValue.asChar(thisCell.getValue());
                builder.append((thisCell.isRevealed() || xRay) ? actualState : '?');
                builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private void updateGamePane() {
        this.updateUpperPane();
        this.updateBoardPane();
    }

    private void updateUpperPane() {
        int minesLeft = (int) (this.mineCount - this.cells.values().stream().filter(c -> c.getState() != CellState.NO_FLAG).count());

        Text textMinesLeft = new Text(String.format("%03d", minesLeft));
        textMinesLeft.setFont(FONT);
        StackPane.setAlignment(textMinesLeft, Pos.CENTER_LEFT);

        String faceButtonImage;
        switch (this.gameState) {
            case WON:
                faceButtonImage = "win.png";
                break;
            case LOST:
                faceButtonImage = "dead.png";
                break;
            default:
                faceButtonImage = (isMouseDown.get()) ? "ohh.png" : "smile.png";
                break;
        }

        Button faceButton = new Button("", new ImageView(new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(faceButtonImage)), 16 * this.guiScale * 1.25, 16 * this.guiScale * 1.25, true, false)));
        faceButton.setOnAction((event) -> this.newGame());
        StackPane.setAlignment(faceButton, Pos.CENTER);

        Text textTimer = new Text(String.format("%03d", Math.min(999, gameTime.get())));
        textTimer.setFont(FONT);
        StackPane.setAlignment(textTimer, Pos.CENTER_RIGHT);

        this.upperPane.getChildren().clear();
        this.upperPane.getChildren().addAll(textMinesLeft, faceButton);
        if (this.timerEnabled) {
            this.upperPane.getChildren().add(textTimer);
        }
    }

    private void updateBoardPane() {
        // button shape and layout is not the same across all cells so let the type param handle this
        for (CellT cell : this.cells.values()) {
            cell.updateButton();
        }
    }

    /**
     * @return The UI {@link Pane} for this board.
     */
    public Pane getGamePane() {
        return this.gamePane;
    }

    /**
     * @return The width of this board.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return The height of this board.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * @return The GUI scaling factor of this board.
     */
    public double getGuiScale() {
        return this.guiScale;
    }

    /**
     * Handle the mouse being pressed anywhere on the UI.
     *
     * @param evt The {@link MouseEvent} in question.
     */
    public void handleMouseDown(MouseEvent evt) {
        if (evt.isPrimaryButtonDown()) {
            this.isMouseDown.set(true);
        }
    }

    /**
     * Handle the mouse being released anywhere on the UI.
     *
     * @param evt The {@link MouseEvent} in question.
     */
    public void handleMouseUp(MouseEvent evt) {
        if (!evt.isPrimaryButtonDown()) {
            this.isMouseDown.set(false);
        }
    }
}
