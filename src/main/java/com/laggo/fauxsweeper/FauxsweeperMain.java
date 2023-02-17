package com.laggo.fauxsweeper;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import com.laggo.fauxsweeper.cell.SquareCell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FauxsweeperMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FauxsweeperBoard<SquareCell> board = new FauxsweeperBoard<>(SquareCell.class, 10, 10, 10);

        primaryStage.setTitle("Fauxsweeper");
        primaryStage.setScene(new Scene(board.getGamePane()));
        primaryStage.show();
    }
}
