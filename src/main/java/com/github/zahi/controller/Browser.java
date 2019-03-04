/*
 * The MIT License
 *
 * Copyright 2018 zahi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zahi.controller;

import com.jfoenix.controls.JFXSpinner;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zahi
 */
public final class Browser extends Controller {

    public static final String FXML_BROWSER = "views/browser.fxml";
    private final Logger logger = LoggerFactory.getLogger(Browser.class);

    public Browser() {
        super(null, FXML_BROWSER);
        loadHtmlFile(getClass().getResource("/html/welcome.html").toExternalForm());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(tab)) {
                addNewTab();
            }
        });
    }

    /////////////////////////////////////////
    // Methods
    /////////////////////////////////////////
    private BrowserTab addNewTab() {
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

        return browserTab;
    }

    public void browse(String url) {
        logger.debug(url);
        BrowserTab browserTab = addNewTab();
        browserTab.loadUrl(url);
    }

    public void loadHtmlFile(String htmlFilePath) {
        logger.debug("loading: " + htmlFilePath);
        BrowserTab browserTab = addNewTab();
        browserTab.getWebview().getEngine().load(htmlFilePath);
    }

    /////////////////////////////////////////
    // FXML Variables & Methods
    /////////////////////////////////////////
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tab;

}
