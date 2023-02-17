package com.laggo.fauxsweeper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SquareCell extends BaseCell {
    public SquareCell(FauxsweeperBoard<SquareCell> board, BoardLocation loc) {
        super(board, loc);
    }

    @Override
    public CellButton getButton() {
        CellButton button = new CellButton();
        // TODO: get graphics, set events, etc
        button.setCell(this);
        button.setFocusTraversable(false);
        button.setOnMousePressed(evt ->
        {
            this.getBoard().handleBoardClick(evt);
            this.getBoard().handleMouseDown(evt);
        });
        button.setOnMouseReleased(this.getBoard()::handleMouseUp);

        button.setMaxSize(16 * FauxsweeperBoard.GUI_SCALE, 16 * FauxsweeperBoard.GUI_SCALE);
        button.setMinSize(16 * FauxsweeperBoard.GUI_SCALE, 16 * FauxsweeperBoard.GUI_SCALE);

        String returnedResource = this.getDisplayIcon();
        if (returnedResource != null) {
            button.setGraphic(new ImageView(new Image(
                    Objects.requireNonNull(this.getClass().getResourceAsStream(returnedResource)),
                    16 * FauxsweeperBoard.GUI_SCALE, 16 * FauxsweeperBoard.GUI_SCALE, true, false)));
        }

        return button;
    }

    // for a square cell, this is quite easy
    @Override
    public void drawToBoard(GridPane pane) {
        for (int x = 0; x < this.getBoard().getWidth(); ++x) {
            for (int y = 0; y < this.getBoard().getHeight(); ++y) {
                pane.add(this.getBoard().getCellAt(new BoardLocation(x, y)).getButton(), x, y);
            }
        }
    }

    @Override
    public Set<SquareCell> getNeighbors() {
        HashSet<SquareCell> ret = new HashSet<>();
        for (int offsetX = -1; offsetX <= 1; ++offsetX) {
            for (int offsetY = -1; offsetY <= 1; ++offsetY) {
                SquareCell neighbor = (SquareCell) this.getBoard().getCellAt(this.getLocation().offsetBy(offsetX, offsetY));
                if (neighbor != this && neighbor != null) {
                    ret.add(neighbor);
                }
            }
        }
        return ret;
    }
}
