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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
import javax.net.ssl.SSLHandshakeException;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
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
    private static final LinkedHashSet<HistEntry> WEB_HIST_ENTERIES = new LinkedHashSet<>();

    public BrowserTab() {
        super(null, FXML_BROWSER_TAB);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webview.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");

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
                    } catch (SSLHandshakeException e) {
                        throw new JFXBrowserErrorException(JFXBrowserErrorMessage.SSL_HAND_SHAKE_EXCEPTION);
                    }

                }
            };
            tload.setOnFailed((event) -> {
                if (tload.getException() instanceof JFXBrowserErrorException) {
                    JFXBrowserErrorMessage errorMsg = ((JFXBrowserErrorException) tload.getException()).getErrorMsg();
                    switch (errorMsg) {
                        case UNKNOWN_HOST_EXCEPTION:
                            webview.getEngine().load(getClass().getResource("/html/unknown-host.html").toExternalForm());
                            break;
                        case SSL_HAND_SHAKE_EXCEPTION:
                            webview.getEngine().load(getClass().getResource("/html/cert-invalid.html").toExternalForm());
                            break;
                    }
                }
            });
            tload.setOnSucceeded((event) -> {
                logger.debug(String.format("Location: %s, Response code: %s", newValue, tload.getValue()));
            });
            new Thread(tload).start();
        });

        webview.getEngine().getHistory().currentIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                WEB_HIST_ENTERIES.add(
                        new HistEntry(webview.getEngine().getHistory().getEntries().get(newValue.intValue()))
                );
                ObservableList<WebHistory.Entry> entryList = webview.getEngine().getHistory().getEntries();

                btnHistNext.setDisable(newValue.intValue() == entryList.size() - 1);
                btnHistPrev.setDisable(newValue.intValue() == 0);
                tfSeachArea.setText(entryList.get(newValue.intValue()).getUrl());
            }
        });

        tfSeachArea.textProperty().addListener((observable, oldValue, newValue) -> {
            btnGo.setDisable(newValue.isEmpty());
        });

        AutoCompletionBinding<HistEntry> atCsltBind = TextFields.bindAutoCompletion(tfSeachArea, (param) -> {
            List<HistEntry> shownSuggestions = new ArrayList<>();
            String text = tfSeachArea.getText();
            if (!text.isEmpty()) {
                for (HistEntry entry : WEB_HIST_ENTERIES) {
                    if (BrowserUtil.isProtocolPresent(entry.getUrl())) {
                        if (entry.getUrl().contains(text) || entry.getTitle().contains(text)) {
                            shownSuggestions.add(entry);
                        }
                    }
                }
                shownSuggestions.sort((o1, o2) -> {
                    return o1.getLastVisitedDate().compareTo(o2.getLastVisitedDate());
                });
            }
            shownSuggestions.add(0, new HistEntry(text));
            return shownSuggestions;
        });

        atCsltBind.setVisibleRowCount(10);

        atCsltBind.setOnAutoCompleted((event) -> {
            tfSeachArea.setText(event.getCompletion().getUrl());
            tfSeachArea.fireEvent(new ActionEvent());
        });

        atCsltBind.prefWidthProperty().bind(tfSeachArea.widthProperty());

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
    WebView getWebview() {
        return webview;
    }

    JFXSpinner getSpinner() {
        return spinner;
    }

    ObjectProperty getFaviconPropery() {
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

    private class HistEntry {

        private final WebHistory.Entry entry;
        private String text;

        public HistEntry(WebHistory.Entry entry) {
            this.entry = entry;
        }

        public HistEntry(String text) {
            this.entry = null;
            this.text = text;
        }

        public String getTitle() {
            return entry != null ? entry.getTitle() : "";
        }

        public String getUrl() {
            return entry != null ? entry.getUrl() : text;
        }

        public Date getLastVisitedDate() {
            return entry != null ? entry.getLastVisitedDate() : null;
        }

        @Override
        public String toString() {
            return entry != null
                    ? String.format("%s %s", entry.getTitle(), entry.getUrl())
                    : text;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.entry);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HistEntry other = (HistEntry) obj;
            if (this.entry == null || other.entry == null) {
                return false;
            }
            if (!Objects.equals(this.entry.getUrl(), other.entry.getUrl())) {
                return false;
            }
            return Objects.equals(this.entry.getTitle(), other.entry.getTitle());
        }
    }
}
