/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zahi.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zahi
 */
public class BrowserTab extends Controller {

    public static final String FXML_BROWSER_TAB = "views/browserTab.fxml";
    private final ObjectProperty faviconPropery = new SimpleObjectProperty();
    private final Logger logger = LoggerFactory.getLogger(BrowserTab.class);

    public BrowserTab() {
        super(null, FXML_BROWSER_TAB, false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
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
                            return getFavicon(webview.getEngine().getLocation());
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
    private ImageView getFavicon(String url) throws URISyntaxException, MalformedURLException, IOException {
        String googleService = "https://www.google.com/s2/favicons?domain=" + getBaseName(url);
        BufferedImage bufImg = ImageIO.read(new URL(googleService));
        ImageView imageview = new ImageView(SwingFXUtils.toFXImage(bufImg, null));
        return imageview;
    }

    private String getBaseName(String url) throws URISyntaxException, MalformedURLException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain;
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
        if (!tfSeachArea.getText().isEmpty()) {
            webview.getEngine().load(tfSeachArea.getText());
        }
    }

    @FXML
    void searchAction(ActionEvent event) {
        if (!tfSeachArea.getText().isEmpty()) {
            webview.getEngine().load(tfSeachArea.getText());
        }
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
