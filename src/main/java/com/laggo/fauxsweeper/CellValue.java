package com.laggo.fauxsweeper;

public enum CellValue {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    MINE;

    public static char asChar(CellValue val){
        return "012345678M".charAt(val.ordinal());
    }
}
