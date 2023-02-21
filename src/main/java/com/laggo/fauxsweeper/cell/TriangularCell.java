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

public class TriangularCell extends BaseCell {
    public TriangularCell(FauxsweeperBoard<? extends ICell> board, BoardLocation location) {
        super(board, location);
    }

    private Point2D getCenterCoords() {
        final double guiScale = this.getBoard().getGuiScale();
        final double sideLength = guiScale * 16;
        final double altitude = sideLength * Math.sqrt(3) / 2;

        double xOffset =
                // offset to the first center
                sideLength / 2
                        // offset to the nth center
                        + sideLength * this.getLocation().getX();
        double yOffset =
                // offset to the first center
                altitude / 2
                        // offset to the nth center
                        + altitude * this.getLocation().getY();

        return new Point2D(xOffset, yOffset);
    }

    @Override
    protected CellButton createButton() {
        final double guiScale = this.getBoard().getGuiScale();
        final double sideLength = guiScale * 16;
        final double altitude = sideLength * Math.sqrt(3) / 2;

        CellButton button = new CellButton();

        button.setMaxSize(sideLength, sideLength);
        button.setMinSize(sideLength, sideLength);

        Polygon tri = new Polygon();
        Point2D centerCoords = this.getCenterCoords();
        /*
        If we visualize the tiling, the directions the triangles point are as follows:
        U D U D U D
        D U D U D U  ...
        U D U D U D
             .
             .
             .
        So, as it turns out, we can use (x + y) % 2 to determine which direction a triangle points given its location in the grid.
         */

        final double angleOffset = (this.getLocation().getX() + this.getLocation().getY()) % 2 == 0 ? Math.PI / 2 : -Math.PI / 2;

        Util.setPolygonSides(tri, centerCoords, sideLength, 3, angleOffset);
        button.setShape(tri);
        // use correct bounding box
        button.setPickOnBounds(false);
        button.relocate(centerCoords.getX() - sideLength / 2, centerCoords.getY() - altitude / 2);

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
        FauxsweeperBoard<? extends ICell> board = this.getBoard();
        BoardLocation thisLoc = this.getLocation();

        return new HashSet<>(Arrays.asList(
                board.getCellAt(thisLoc.offsetBy(-1, 0)),
                board.getCellAt(thisLoc.offsetBy(1, 0)),
                (this.getLocation().getX() + this.getLocation().getY()) % 2 == 1 ?
                        board.getCellAt(thisLoc.offsetBy(0, -1)) :
                        board.getCellAt(thisLoc.offsetBy(0, 1))
        )).stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
