package com.laggo.fauxsweeper.config;

import com.google.gson.annotations.SerializedName;
import com.laggo.fauxsweeper.cell.HexagonalCell;
import com.laggo.fauxsweeper.cell.ICell;
import com.laggo.fauxsweeper.cell.SquareCell;

public enum CellType {
    @SerializedName("square")
    SQUARE,
    @SerializedName("hexagonal")
    HEXAGONAL;

    public Class<? extends ICell> toClass() {
        switch (this) {
            case SQUARE:
                return SquareCell.class;
            case HEXAGONAL:
                return HexagonalCell.class;
            default:
                return null;
        }
    }
}
