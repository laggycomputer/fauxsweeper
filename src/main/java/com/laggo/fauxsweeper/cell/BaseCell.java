package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The type Base cell.
 */
public abstract class BaseCell implements ICell {
    private final BoardLocation location;
    private final FauxsweeperBoard<? extends ICell> board;
    private CellValue value = CellValue.ZERO;
    private CellState state = CellState.NO_FLAG;
    private boolean revealed = false;
    private CellButton button;
    private Pane boardPane;

    /**
     * Instantiates a new Base cell.
     *
     * @param board    the board
     * @param location the location
     */
    public BaseCell(FauxsweeperBoard<? extends ICell> board, BoardLocation location) {
        this.board = board;
        this.location = location;
    }

    @Override
    public BoardLocation getLocation() {
        return this.location;
    }

    @Override
    public FauxsweeperBoard<? extends ICell> getBoard() {
        return this.board;
    }


    /**
     * Gets display icon.
     *
     * @return The icon which should be used for this cell. Can be {@code null} if this cell should have no icon.
     */
    public String getDisplayIcon() {
        if (!this.revealed) {
            switch (this.getState()) {
                case NO_FLAG:
                    return "/question.png";
                case FLAG:
                    return "/flag.png";
                case FLAG_POTENTIAL:
                    return "/flag-potential.png";
            }
        } else {
            if (this.getState() != CellState.NO_FLAG && this.getValue() != CellValue.MINE) {
                // then the game has ended
                return "/misflagged.png";
            }

            if (this.getValue() == CellValue.MINE) {
                return (this.getBoard().getClickedMine() == this) ? "/mine-death.png" : "/mine-ceil.png";
            }

            if (this.getValue() == CellValue.ZERO) {
                return null;
            }

            return "/open" + this.getValue().ordinal() + ".png";
        }
        // technically unreachable
        return null;
    }

    /**
     * Creates a new {@link CellButton} to represent this cell. {@link #addAttributes(CellButton)} is already invoked
     * so there is no need to call this from the implementation.
     *
     * @return The new {@link CellButton} which represents this cell.
     */
    protected abstract CellButton createButton();

    /**
     * Sets attributes common to all cell buttons, regardless of which cell type.
     *
     * @param button The {@link CellButton} to alter.
     */
    protected void addAttributes(CellButton button) {
        button.setCell(this);
        button.setFocusTraversable(false);
        button.setOnMousePressed(this.getBoard()::handleMouseDown);
        button.setOnMouseReleased(evt -> {
            this.getBoard().handleMouseUp(evt);
            this.getBoard().handleBoardClick(evt);
        });
    }

    @Override
    public CellButton getButton() {
        if (this.button == null) {
            this.button = this.createButton();
            this.addAttributes(this.button);
        }
        return this.button;
    }

    /**
     * Create the board pane for a board with this cell type.
     *
     * @return The {@link Pane} which should be used to represent the {@link FauxsweeperBoard} containing this cell.
     */
    protected abstract Pane createBoardPane();

    @Override
    public Pane getBoardPane() {
        if (this.boardPane == null) {
            this.boardPane = this.createBoardPane();
        }
        return this.boardPane;
    }

    /**
     * Sets the pane this cell's UI element is on.
     *
     * @param boardPane The pane to set.
     */
    protected void setBoardPane(Pane boardPane) {
        this.boardPane = boardPane;
    }

    @Override
    public CellValue getValue() {
        return this.value;
    }

    @Override
    public void setValue(CellValue value) {
        this.value = value;
    }

    @Override
    public CellState getState() {
        return this.state;
    }

    @Override
    public Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred, boolean includeNeighbors) {
        HashMap<ICell, Boolean> foundSoFar = new HashMap<>();
        this.recursiveConnectedMatching(this, foundSoFar, pred, includeNeighbors);
        // this is technically linked to the hashset, but it'll be destroyed anyway so who cares
        return foundSoFar.keySet();
    }

    /**
     * The recursive step for the flood fill required by {@code getConnectedMatching}.
     *
     * @param startingCell The {@link ICell} to base this fill on.
     * @param soFar The {@link HashMap} of cells already traversed. A missing key means the cell has not been traversed, the value {@code false} means a cell has been processed but its neighbors have not, and the value {@code true} means a cell has been traversed fully.
     * @param pred The predicate which all cells must match in order for the flood fill to recurse over them.
     * @param includeNeighbors Whether to include every neighbor of a cell returned by the flood fill.
     */
    private void recursiveConnectedMatching(ICell startingCell, HashMap<ICell, Boolean> soFar, Predicate<ICell> pred, boolean includeNeighbors) {
        if (soFar.containsKey(startingCell)) {
            return;
        }
        soFar.put(startingCell, false);
        for (ICell neighbor : startingCell.getNeighbors()) {
            if (pred.test(neighbor)) {
                this.recursiveConnectedMatching(neighbor, soFar, pred, includeNeighbors);
            } else if (includeNeighbors) {
                // add it anyway but don't recurse
                soFar.put(neighbor, true);
            }
        }
        soFar.put(startingCell, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ICell)) return false;

        if (this.location != ((ICell) o).getLocation()) return false;
        return this.board == ((ICell) o).getBoard();
    }

    @Override
    public boolean isRevealed() {
        return this.revealed;
    }

    @Override
    public void reveal() {
        this.revealed = true;
    }

    @Override
    public ClickResult onLeftClick() {
        if (this.getBoard().isGameOver()) {
            return ClickResult.INVALID;
        }

        if (this.revealed) {
            return ClickResult.INVALID;
        }

        if (this.getState() != CellState.NO_FLAG) {
            return ClickResult.INVALID;
        }

        if (this.getValue() == CellValue.MINE) {
            return ClickResult.FAIL;
        }

        if (this.value != CellValue.ZERO) {
            this.revealed = true;
            return ClickResult.OK;
        }

        this.reveal();
        // we are clicking a zero cell
        for (ICell connectedCell : this.getConnectedMatching(c -> c.getValue() == CellValue.ZERO, true)) {
            connectedCell.reveal();
        }

        return ClickResult.OK;
    }

    public void onRightClick(MouseEvent evt) {
        ICell cellTarget = Objects.requireNonNull(((CellButton) evt.getTarget()).getCell());
        if (cellTarget.isRevealed()) {
            return;
        }
        if (cellTarget.getBoard().isGameOver()) {
            return;
        }
        this.state = CellState.values()[(this.getState().ordinal() + 3 + (evt.isShiftDown() ? -1 : 1)) % 3];
    }
}
