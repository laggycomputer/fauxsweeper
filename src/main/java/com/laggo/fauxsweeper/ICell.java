package com.laggo.fauxsweeper;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.Set;
import java.util.function.Predicate;

public interface ICell {
    BoardLocation getLocation();

    FauxsweeperBoard<? extends ICell> getBoard();

    CellButton getButton();

    void drawToBoard(GridPane pane);

    CellValue getValue();

    void setValue(CellValue val);

    CellState getState();

    void setState(CellState state);

    Set<? extends ICell> getNeighbors();

    Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred);

    boolean equals(Object other);

    boolean isRevealed();

    void setRevealed(boolean revealed);

    String getDisplayIcon();
}
