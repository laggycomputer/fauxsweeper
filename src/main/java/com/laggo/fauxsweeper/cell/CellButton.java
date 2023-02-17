package com.laggo.fauxsweeper.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

public class CellButton extends Button {
    private ObjectProperty<ICell> cell;

    public final ICell getCell() {
        return cell == null ? null : cell.get();
    }

    public final void setCell(ICell cell) {
        cellProperty().set(cell);
    }

    public final ObjectProperty<ICell> cellProperty() {
        if (cell == null) {
            cell = new SimpleObjectProperty<>(null);
        }
        return cell;
    }
}
