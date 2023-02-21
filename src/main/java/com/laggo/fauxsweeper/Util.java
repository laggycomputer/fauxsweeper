package com.laggo.fauxsweeper;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class Util {

    /**
     * Configures the passed {@link Polygon} to be a regular n-gon.
     *
     * @param polygon The {@link Polygon} object to manipulate. Its points will be cleared and replaced.
     * @param center The {@link Point2D} on which the polygon is to be centered.
     * @param radius The distance from the center to any vertex of the polygon.
     * @param sides The number of sides the polygon should have.
     * @param angleOffset The angle offset at which to place the first vertex. As by convention in geometry, an offset of 0 means that a vertex will be exactly {@code radius} units to the right of the center.
     */
    public static void setPolygonSides(Polygon polygon, Point2D center, double radius, int sides, double angleOffset) {
        polygon.getPoints().clear();
        final double angleStep = Math.PI * 2 / sides;
        double angle = angleOffset;
        for (int i = 0; i < sides; ++i, angle += angleStep) {
            polygon.getPoints().addAll(Math.cos(angle) * radius + center.getX(), Math.sin(angle) * radius + center.getY());
        }
    }
}
