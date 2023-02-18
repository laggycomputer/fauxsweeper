package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.Set;
import java.util.function.Predicate;

/**
 * The bare minimum necessary to create a cell. Users are <b>strongly</b> recommended to use {@link BaseCell} instead since it implements most of the API a cell needs.
 */
public interface ICell {
    /**
     * Gets the {@link BoardLocation} of this cell on its {@link FauxsweeperBoard}.
     *
     * @return The location.
     */
    BoardLocation getLocation();

    /**
     * Gets the {@link FauxsweeperBoard} this cell is on.
     *
     * @return The board.
     */
    FauxsweeperBoard<? extends ICell> getBoard();

    /**
     * Should be called whenever the UI representation of this cell needs to be updated.
     */
    void updateButton();

    /**
     * Gets the {@link CellButton} which represents this cell on the UI.
     *
     * @return The button.
     */
    CellButton getButton();

    /**
     * Gets the {@link Pane} which this cell is displayed on.
     *
     * @return The board pane.
     */
    Pane getBoardPane();

    /**
     * Gets the {@link CellValue} on this cell, either a number or a mine.
     *
     * @return The value assigned to this cell.
     */
    CellValue getValue();

    /**
     * Sets the {@link CellValue} of this cell. Should probably not be invoked by end users.
     *
     * @param val The new {@link CellValue}.
     */
    void setValue(CellValue val);

    /**
     * Gets the {@link CellState} of this cell.
     *
     * @return The {@link CellState} of this cell: flagged, soft-flagged, or not flagged.
     */
    CellState getState();

    /**
     * Gets the cells bordering this one. This will obviously vary based on the type of cell.
     *
     * @return This cell's neighbors.
     */
    Set<? extends ICell> getNeighbors();

    /**
     * Gets all cells connected to this one where all cells in between fulfill {@code pred}.
     * This is essentially a flood fill algorithm.
     *
     * @param pred The predicate to test cells with.
     * @param includeNeighbors Whether to also include all cells bordering the cells found by the flood fill.
     * @return A set of all the connected cells matching this predicate.
     */
    Set<? extends ICell> getConnectedMatching(Predicate<ICell> pred, boolean includeNeighbors);

    boolean equals(Object other);

    /**
     * @return Whether this cell has already been revealed.
     */
    boolean isRevealed();

    /**
     * Sets {@code revealed} to true, indicating this cell's state should be shown.
     */
    void reveal();

    /**
     * Handles what happens when this cell is clicked.
     *
     * @return The result of this click.
     */
    ClickResult onLeftClick();

    /**
     * Handles what happens when this cell is right-clicked (to flag).
     *
     * @param evt The responsible {@link MouseEvent}.
     */
    void onRightClick(MouseEvent evt);
}
