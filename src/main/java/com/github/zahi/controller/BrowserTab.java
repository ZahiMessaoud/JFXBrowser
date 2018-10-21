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

import com.github.zahi.enums.JFXBrowserErrorMessage;
import com.github.zahi.exception.JFXBrowserErrorException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zahi
 */
class BrowserTab extends Controller {

    public static final String FXML_BROWSER_TAB = "views/browserTab.fxml";
    private final ObjectProperty faviconPropery = new SimpleObjectProperty();
    private final Logger logger = LoggerFactory.getLogger(BrowserTab.class);

    public BrowserTab() {
        super(null, FXML_BROWSER_TAB);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webview.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug(
                    String.format(
                            "Location: %s\n WebView -> Engine -> Worker state: %s",
                            webview.getEngine().getLocation(),
                            newValue
                    )
            );
            switch (newValue) {
                case RUNNING:
                    JFXSpinner tmpSpinner = new JFXSpinner();
                    tmpSpinner.setPrefSize(15, 15);
                    faviconPropery.set(tmpSpinner);

                    spinner.setVisible(true);
                    btnCancel.setDisable(false);
                    break;
                case FAILED:
                    spinner.setVisible(false);
                    btnCancel.setDisable(true);
                    break;
                case SUCCEEDED:
                    spinner.setVisible(false);
                    btnCancel.setDisable(true);
                    Task<ImageView> tFavIcon = new Task() {
                        @Override
                        protected ImageView call() throws Exception {
                            return new ImageView(
                                    BrowserUtil.getFavicon(webview.getEngine().getLocation())
                            );
                        }
                    };
                    tFavIcon.setOnSucceeded((event) -> {
                        faviconPropery.set(tFavIcon.getValue());
                    });
                    tFavIcon.setOnFailed((event) -> {
                        logger.error("Error while loading favicon", tFavIcon.getException());
                    });
                    new Thread(tFavIcon).start();
                    break;
                case CANCELLED:
                    spinner.setVisible(false);
                    btnCancel.setDisable(true);
                default:
                    break;
            }
        });

        webview.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            Task<Integer> tload = new Task() {
                @Override
                protected Integer call() throws Exception {
                    try {
                        URL url = new URL(newValue);
                        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                        return httpCon.getResponseCode();
                    } catch (UnknownHostException e) {
                        throw new JFXBrowserErrorException(JFXBrowserErrorMessage.UNKNOWN_HOST_EXCEPTION);
                    }
                }
            };
            tload.setOnFailed((event) -> {
                if (tload.getException() instanceof JFXBrowserErrorException) {
                    webview.getEngine().load(BrowserTab.class.getResource("/html/unknown-host.html").toExternalForm());
                }
            });
            tload.setOnSucceeded((event) -> {
                logger.debug(String.format("Location: %s, Response code: %s", newValue, tload.getValue()));
            });
            new Thread(tload).start();
        });

        webview.getEngine().getHistory().currentIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<WebHistory.Entry> entryList = webview.getEngine().getHistory().getEntries();

                btnHistNext.setDisable(newValue.intValue() == entryList.size() - 1);
                btnHistPrev.setDisable(newValue.intValue() == 0);
                tfSeachArea.setText(entryList.get(newValue.intValue()).getUrl());
            }
        });

        tfSeachArea.textProperty().addListener((observable, oldValue, newValue) -> {
            btnGo.setDisable(newValue.isEmpty());
        });

    }

    /////////////////////////////////////////
    // Methods
    /////////////////////////////////////////
    public void loadUrl(String url) {
        if (!url.isEmpty()) {
            if (BrowserUtil.isValidDomainName(url)) {
                if (!BrowserUtil.isProtocolPresent(url)) {
                    webview.getEngine().load(String.format("http://%s", url));
                } else {
                    webview.getEngine().load(url);
                }
            } else {
                webview.getEngine().load(BrowserUtil.searchInGoogle(url));
            }
        }
    }

    /////////////////////////////////////////
    // Getters & Setters
    /////////////////////////////////////////
    public WebView getWebview() {
        return webview;
    }

    public JFXSpinner getSpinner() {
        return spinner;
    }

    public ObjectProperty getFaviconPropery() {
        return faviconPropery;
    }

    /////////////////////////////////////////
    // FXML Methods & Variables
    /////////////////////////////////////////
    @FXML
    void cancelAction(ActionEvent event) {
        webview.getEngine().getLoadWorker().cancel();
        webview.getEngine().executeScript("window.stop()");
    }

    @FXML
    void reloadAction(ActionEvent event) {
        webview.getEngine().reload();
    }

    @FXML
    void goAction(ActionEvent event) {
        loadUrl(tfSeachArea.getText());
    }

    @FXML
    void searchAction(ActionEvent event) {
        loadUrl(tfSeachArea.getText());
    }

    @FXML
    void histNextAction(ActionEvent event) {
        WebHistory history = webview.getEngine().getHistory();
        history.go(1);
    }

    @FXML
    void histPrevAction(ActionEvent event) {
        WebHistory history = webview.getEngine().getHistory();
        history.go(-1);
    }

    @FXML
    private JFXButton btnHistPrev;

    @FXML
    private JFXButton btnHistNext;

    @FXML
    private JFXButton btnReload;

    @FXML
    private TextField tfSeachArea;

    @FXML
    private JFXButton btnGo;

    @FXML
    private WebView webview;

    @FXML
    private JFXSpinner spinner;

    @FXML
    private JFXButton btnCancel;
}
