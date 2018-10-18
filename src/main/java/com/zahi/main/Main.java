/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zahi.main;

import com.zahi.controller.Browser;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author BADEV
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Browser browser = new Browser();
        browser.showInStage();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(Main.class, args);
    }
}
