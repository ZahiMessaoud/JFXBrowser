package com.zahi.controller;

import java.net.URL;
import javafx.scene.Scene;
import java.io.IOException;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Human
 *
 */
public class Controller implements Initializable {

    private Stage stage;
    private Parent view;
    private Scene scene = null;

    public Controller(Object object, String fxml, boolean window) {
        if (window) {
            stage = new Stage();
        }
        try {
            FXMLLoader loader = new FXMLLoader(Controller.class.getClassLoader().getResource(fxml));
            loader.setController(this);
            Parent root = loader.load();
            view = new StackPane(root);
            if (window) {
                scene = new Scene(view);
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setFullScreen(boolean bool) {
        stage.setMaximized(bool);
        if (bool) {
            stage.initStyle(StageStyle.UNDECORATED);
        }
    }

    public void setMaximized(boolean bool) {
        stage.setMaximized(bool);
    }

    public void setModal(boolean bool) {
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    public void showInStage() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }

    public Parent getView() {
        return view;
    }
}
