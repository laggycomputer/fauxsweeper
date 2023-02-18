package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.Set;
import java.util.function.Predicate;

public interface ICell {
    BoardLocation getLocation();

    FauxsweeperBoard<? extends ICell> getBoard();

    void updateButton();

    CellButton getButton();

    Pane getBoardPane();

    CellValue getValue();

    void setValue(CellValue val);

    CellState getState();

    Set<? extends ICell> getNeighbors();

    Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred);

    boolean equals(Object other);

    boolean isRevealed();

    ClickResult onLeftClick(boolean propagate);

    void onRightClick(MouseEvent evt);
}
