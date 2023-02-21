package com.laggo.fauxsweeper.config;

import com.google.gson.annotations.SerializedName;
import com.laggo.fauxsweeper.cell.HexagonalCell;
import com.laggo.fauxsweeper.cell.ICell;
import com.laggo.fauxsweeper.cell.SquareCell;
import com.laggo.fauxsweeper.cell.TriangularCell;

public enum CellType {
    @SerializedName("square")
    SQUARE,
    @SerializedName("hexagon")
    HEXAGONAL,
    @SerializedName("triangle")
    TRIANGULAR;

    public Class<? extends ICell> toClass() {
        switch (this) {
            case SQUARE:
                return SquareCell.class;
            case HEXAGONAL:
                return HexagonalCell.class;
            case TRIANGULAR:
                return TriangularCell.class;
            default:
                return null;
        }
    }
}
