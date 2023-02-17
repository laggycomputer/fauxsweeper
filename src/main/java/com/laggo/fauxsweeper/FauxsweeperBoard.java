package com.laggo.fauxsweeper;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class FauxsweeperBoard<CellT extends ICell> {
    // TODO:  config
    public static final double GUI_SCALE = 1.5d;
    private final int width;
    private final int height;
    private final int mineCount;
    private final Random rand;
    private final Class<CellT> cellTRef;
    private final HashMap<BoardLocation, CellT> cells = new HashMap<>();
    private final Pane gamePane = new VBox(new StackPane(), new GridPane());
    BooleanProperty isMouseDown = new SimpleBooleanProperty(this, "isMouseDown", false);
    private GameState gameState = GameState.FIRST;
    private ICell clickedMine;
    private Timer timer = new Timer(true);
    private IntegerProperty gameTime = new SimpleIntegerProperty(this, "gameTime", 0);

    public FauxsweeperBoard(Class<CellT> cellTRef, int width, int height, int mineCount) {
        this.cellTRef = cellTRef;

        this.width = width;
        this.height = height;
        this.mineCount = mineCount;

        // TODO: not random
        this.rand = new Random(69420);

        this.fillWithEmptyCells();
        this.placeMines(this.mineCount);
        this.computeNumberedCells();

        this.isMouseDown.addListener(evt -> this.updateUpperPane());
        this.gameTime.addListener(evt -> this.updateUpperPane());

        this.updateGamePane();

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onTimerTick();
            }
        }, 1000, 1000);
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

    public ICell getClickedMine() {
        return this.clickedMine;
    }

    public void newGame(ActionEvent evt) {
        this.cells.clear();
        this.clickedMine = null;
        this.gameState = GameState.FIRST;
        this.gameTime.set(0);

        this.fillWithEmptyCells();
        this.placeMines(this.mineCount);
        this.computeNumberedCells();
        this.updateGamePane();

        this.timer.cancel();
        this.timer = new Timer(false);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onTimerTick();
            }
        }, 1000, 1000);
    }

    public CellT getCellAt(BoardLocation loc) {
        return this.cells.get(loc);
    }

    public boolean isGameOver() {
        return this.gameState == GameState.WON || this.gameState == GameState.LOST;
    }

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
            cell.setRevealed(true);
        }
    }

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

    public void updateGamePane() {
        this.updateUpperPane();
        this.updateBoardPane();
    }

    private void updateUpperPane() {
        StackPane upperPane = (StackPane) this.gamePane.getChildren().get(0);

        int minesLeft = (int) (this.mineCount - this.cells.values().stream().filter(c -> c.getState() != CellState.NO_FLAG).count());

        Text textMinesLeft = new Text(Integer.toString(minesLeft));
        textMinesLeft.setFont(FauxsweeperMain.FONT);
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

        Button faceButton = new Button("", new ImageView(new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(faceButtonImage)), 16 * GUI_SCALE * 1.25, 16 * GUI_SCALE * 1.25, true, false)));
        faceButton.setOnAction(this::newGame);
        StackPane.setAlignment(faceButton, Pos.CENTER);

        Text textTimer = new Text(Integer.toString(this.gameTime.get()));
        textTimer.setFont(FauxsweeperMain.FONT);
        StackPane.setAlignment(textTimer, Pos.CENTER_RIGHT);

        upperPane.getChildren().clear();
        upperPane.getChildren().addAll(textMinesLeft, faceButton, textTimer);
    }

    private void updateBoardPane() {
        // button shape and layout is not the same across all cells so let the type param handle this
        this.getCellAt(new BoardLocation(0, 0)).drawToBoard((GridPane) this.gamePane.getChildren().get(1));
    }

    public Pane getGamePane() {
        return this.gamePane;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void handleMouseDown(MouseEvent evt) {
        if (evt.isPrimaryButtonDown()) {
            this.isMouseDown.set(true);
        }
        // TODO
    }

    public void handleMouseUp(MouseEvent evt) {
        if (!evt.isPrimaryButtonDown()) {
            this.isMouseDown.set(false);
        }
    }
}
