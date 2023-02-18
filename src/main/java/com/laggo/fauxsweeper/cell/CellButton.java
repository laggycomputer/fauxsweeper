package com.laggo.fauxsweeper.cell;

import javafx.scene.control.Button;

public class CellButton extends Button {
    private ICell cell;

    public ICell getCell() {
        return this.cell;
    }

    public void setCell(ICell cell) {
        this.cell = cell;
    }
}
