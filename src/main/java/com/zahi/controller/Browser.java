/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zahi.controller;

import com.jfoenix.controls.JFXSpinner;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;

/**
 *
 * @author zahi
 */
public class Browser extends Controller {

    public static final String FXML_BROWSER = "views/browser.fxml";

    public Browser() {
        super(null, FXML_BROWSER, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        addNewTab();

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(tab)) {
                addNewTab();
            }
        });
    }

    /////////////////////////////////////////
    // Methods
    /////////////////////////////////////////
    private void addNewTab() {
        BrowserTab browserTab = new BrowserTab();

        Tab tmpTab = new Tab("new Tab");
        tmpTab.setClosable(true);

        browserTab.getWebview().getEngine().titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (!newValue.isEmpty()) {
                    tmpTab.setText(newValue);
                }
            }
        });

        browserTab.getFaviconPropery().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ImageView) {
                tmpTab.setGraphic((ImageView) newValue);
            } else if (newValue instanceof JFXSpinner) {
                tmpTab.setGraphic((JFXSpinner) newValue);
            }
        });

        tmpTab.setContent(browserTab.getView());
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tmpTab);
        tabPane.getSelectionModel().select(tmpTab);
    }

    /////////////////////////////////////////
    // FXML Variables & Methods
    /////////////////////////////////////////
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tab;

}
