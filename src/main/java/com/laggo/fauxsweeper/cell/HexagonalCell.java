package com.laggo.fauxsweeper.cell;

import com.laggo.fauxsweeper.Util;
import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HexagonalCell extends BaseCell {
    public HexagonalCell(FauxsweeperBoard<? extends ICell> board, BoardLocation location) {
        super(board, location);
    }

    private Point2D getCenterCoords() {
        final double guiScale = this.getBoard().getGuiScale();
        final double sideLength = guiScale * 8;

        double xOffset =
                // offset for the entire row
                sideLength * Math.sqrt(3) / 2 * (this.getLocation().getY() % 2)
                        // offset to the first center
                        + sideLength * Math.sqrt(3) / 2
                        // offset to the nth center
                        + sideLength * Math.sqrt(3) * this.getLocation().getX();
        double yOffset = sideLength + 1.5 * sideLength * this.getLocation().getY();

        return new Point2D(xOffset, yOffset);
    }

    @Override
    protected CellButton createButton() {
        final double guiScale = this.getBoard().getGuiScale();
        final double sideLength = guiScale * 8;

        CellButton button = new CellButton();
        this.addAttributes(button);

        button.setMaxSize(sideLength * 2, sideLength * 2);
        button.setMinSize(sideLength * 2, sideLength * 2);

        Polygon hex = new Polygon();
        Point2D centerCoords = this.getCenterCoords();
        Util.setPolygonSides(hex, centerCoords, sideLength, 6, 0);
        button.setShape(hex);
        // use correct bounding box
        button.setPickOnBounds(false);
        button.relocate(centerCoords.getX() - sideLength * Math.sqrt(3) / 2, centerCoords.getY() - sideLength);

        return button;
    }

    @Override
    protected Pane createBoardPane() {
        Pane boardPane = new Pane();
        this.setBoardPane(boardPane);
        for (ICell cell : this.getBoard().getAllCells()) {
            cell.updateButton();
            boardPane.getChildren().add(cell.getButton());
        }

        return boardPane;
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

    @Override
    public Set<? extends ICell> getNeighbors() {
        /*
         * Recall how cells are laid out in a hexagonal tiling:
         * 0 1 2 3 4 5
         *  0 1 2 3 4 5
         * 0 1 2 3 4 5
         *  0 1 2 3 4 5
         */
        FauxsweeperBoard<? extends ICell> board = this.getBoard();
        BoardLocation thisLoc = this.getLocation();

        return this.getLocation().getY() % 2 == 1 ? new HashSet<>(Arrays.asList(
                board.getCellAt(thisLoc.offsetBy(0, -1)),
                board.getCellAt(thisLoc.offsetBy(1, -1)),
                board.getCellAt(thisLoc.offsetBy(1, 0)),
                board.getCellAt(thisLoc.offsetBy(1, 1)),
                board.getCellAt(thisLoc.offsetBy(0, 1)),
                board.getCellAt(thisLoc.offsetBy(-1, 0)))
        ).stream().filter(Objects::nonNull).collect(Collectors.toSet()) : new HashSet<>(Arrays.asList(
                board.getCellAt(thisLoc.offsetBy(-1, -1)),
                board.getCellAt(thisLoc.offsetBy(0, -1)),
                board.getCellAt(thisLoc.offsetBy(1, 0)),
                board.getCellAt(thisLoc.offsetBy(0, 1)),
                board.getCellAt(thisLoc.offsetBy(-1, 1)),
                board.getCellAt(thisLoc.offsetBy(-1, 0))
        )).stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
