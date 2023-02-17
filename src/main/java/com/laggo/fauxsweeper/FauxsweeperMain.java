package com.laggo.fauxsweeper;

import com.laggo.fauxsweeper.board.FauxsweeperBoard;
import com.laggo.fauxsweeper.cell.ICell;
import com.laggo.fauxsweeper.config.Configuration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class FauxsweeperMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Configuration configuration = Configuration.fromFile(new File("config.json"));

        FauxsweeperBoard<? extends ICell> board = FauxsweeperBoard.fromConfiguration(configuration);

        configuration.toFile(new File("config.json"));

        primaryStage.setTitle("Fauxsweeper");
        primaryStage.setScene(new Scene(board.getGamePane()));
        primaryStage.show();
    }
}
