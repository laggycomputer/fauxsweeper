package com.laggo.fauxsweeper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class FauxsweeperMain extends Application {
    // TODO: configure me
    // TODO: this doesn't work
    public static final Font FONT = Font.loadFont(Objects.requireNonNull(FauxsweeperMain.class.getResource("/Minecraftia-Regular.ttf")).toString(), 12);

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
