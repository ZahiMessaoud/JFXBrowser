/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.zahi.main;

import com.github.zahi.controller.Browser;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author zahi
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Browser browser = new Browser();
        Stage stage = new Stage();
        stage.setScene(new Scene(browser.getView()));
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(Main.class, args);
    }
}
