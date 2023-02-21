package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SquareCell extends BaseCell {
    public SquareCell(FauxsweeperBoard<SquareCell> board, BoardLocation loc) {
        super(board, loc);
    }

    @Override
    protected CellButton createButton() {
        final double guiScale = this.getBoard().getGuiScale();

        CellButton button = new CellButton();

        button.setMaxSize(16 * guiScale, 16 * guiScale);
        button.setMinSize(16 * guiScale, 16 * guiScale);

        return button;
    }

    @Override
    public void updateButton() {
        final double guiScale = this.getBoard().getGuiScale();

        String returnedResource = this.getDisplayIcon();
        if (returnedResource != null) {
            this.getButton().setGraphic(new ImageView(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(returnedResource)), 16 * guiScale, 16 * guiScale, true, false)));
        } else {
            this.getButton().setGraphic(null);
        }
    }

    protected Pane createBoardPane() {
        // if this gets called a second time, whatever
        GridPane boardPane = new GridPane();

        for (ICell cell : this.getBoard().getAllCells()) {
            cell.updateButton();
            boardPane.add(cell.getButton(), cell.getLocation().getX(), cell.getLocation().getY());
        }
        return boardPane;
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
