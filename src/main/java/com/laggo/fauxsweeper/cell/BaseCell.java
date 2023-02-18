package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public abstract class BaseCell implements ICell {
    private final BoardLocation loc;
    private final FauxsweeperBoard<? extends BaseCell> board;
    private CellValue value = CellValue.ZERO;
    private CellState state = CellState.NO_FLAG;
    private boolean revealed = false;

    public BaseCell(FauxsweeperBoard<? extends BaseCell> board, BoardLocation loc) {
        this.board = board;
        this.loc = loc;
    }

    @Override
    public BoardLocation getLocation() {
        return this.loc;
    }

    @Override
    public FauxsweeperBoard<? extends BaseCell> getBoard() {
        return this.board;
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
    public Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred) {
        HashMap<ICell, Boolean> foundSoFar = new HashMap<>();
        this.recursiveConnectedMatching(this, foundSoFar, pred);
        // this is technically linked to the hashset, but it'll be destroyed anyway so who cares
        return foundSoFar.keySet();
    }

    private void recursiveConnectedMatching(ICell startingCell, HashMap<ICell, Boolean> soFar, Predicate<ICell> pred) {
        if (soFar.containsKey(startingCell)) {
            return;
        }
        soFar.put(startingCell, false);
        for (ICell neighbor : startingCell.getNeighbors()) {
            if (pred.test(neighbor)) {
                this.recursiveConnectedMatching(neighbor, soFar, pred);
            } else {
                // add it anyway but don't recurse
                soFar.put(neighbor, true);
            }
        }
        soFar.put(startingCell, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseCell)) return false;

        BaseCell baseCell = (BaseCell) o;

        if (!loc.equals(baseCell.loc)) return false;
        return board.equals(baseCell.board);
    }

    @Override
    public boolean isRevealed() {
        return this.revealed;
    }

    @Override
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    @Override
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

        // we are clicking a zero cell
        this.revealed = true;
        for (ICell connectedCell : this.getConnectedMatching(c -> c.getValue() == CellValue.ZERO)) {
            connectedCell.setRevealed(true);
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
