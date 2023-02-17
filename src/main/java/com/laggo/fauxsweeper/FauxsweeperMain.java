package com.laggo.fauxsweeper;

import java.lang.reflect.InvocationTargetException;

public class FauxsweeperMain {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        FauxsweeperBoard<SquareCell> board = new FauxsweeperBoard<>(SquareCell.class, 10, 10, 10);
        board.getCellAt(new BoardLocation(0, 0)).getConnectedMatching(c -> c.getValue() == CellValue.ZERO);
        board.dumpBoard(true);
    }
}