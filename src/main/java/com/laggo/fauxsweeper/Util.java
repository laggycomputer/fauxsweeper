package com.laggo.fauxsweeper;

import javafx.scene.shape.Polygon;

public class Util {
    public static void setPolygonSides(Polygon polygon, double centerX, double centerY, double radius, int sides) {
        polygon.getPoints().clear();
        final double angleStep = Math.PI * 2 / sides;
        double angle = 0;
        for (int i = 0; i < sides; i++, angle += angleStep) {
            polygon.getPoints().addAll(
                    Math.sin(angle) * radius + centerX,
                    Math.cos(angle) * radius + centerY
            );
        }
    }
}
