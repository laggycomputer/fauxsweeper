package com.laggo.fauxsweeper;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

public class FauxsweeperBoard<CellT extends ICell> {
    private final int width;
    private final int height;
    private final int mineCount;
    private final Random rand;
    private final GameState gameState = GameState.FIRST;
    private final Class<CellT> cellTRef;
    private final HashMap<BoardLocation, CellT> cells = new HashMap<>();

    public FauxsweeperBoard(Class<CellT> cellTRef, int width, int height, int mineCount) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.cellTRef = cellTRef;

        this.width = width;
        this.height = height;
        this.mineCount = mineCount;

        // TODO: not random
        this.rand = new Random(69420);

        this.fillWithEmptyCells();
        this.placeMines(this.mineCount);
        this.computeNumberedCells();
    }

    private void fillWithEmptyCells() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                BoardLocation loc = new BoardLocation(x, y);
                // actually, this should never fail
                this.cells.put(loc, cellTRef.getConstructor(FauxsweeperBoard.class, BoardLocation.class).newInstance(this, loc));
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

    public CellT getCellAt(BoardLocation loc) {
        return this.cells.get(loc);
    }

    public String dumpBoard(boolean xRay) {
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
}
