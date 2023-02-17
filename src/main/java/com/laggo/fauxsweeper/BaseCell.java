package com.laggo.fauxsweeper;

import java.util.HashMap;
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
    public CellButton getButton() {
        CellButton button = new CellButton();
        // TODO: get graphics, set events, etc
        return button;
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
    public void setState(CellState state) {
        this.state = state;
    }

    @Override
    public Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred) {
        HashMap<ICell, Boolean> foundSoFar = new HashMap<>();
        this.recursiveConnectedMatching(this, foundSoFar, pred);
        // this is technically linked to the hashset but it'll be destroyed anyway so who cares
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
}
