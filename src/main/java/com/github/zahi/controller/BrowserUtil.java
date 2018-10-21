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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;

/**
 *
 * @author zahi
 */
class BrowserUtil {

    private final static String DOMAIN_NAME_PATTERN
            = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";

    public String getVersion() {
        String path = "/version.txt";
        InputStream stream = BrowserUtil.class.getResourceAsStream(path);
        if (stream == null) {
            return "UNKNOWN";
        }
        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

    public static boolean isValidDomainName(String query) {
        Pattern patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
        return patternDomainName.matcher(query).find();
    }

    public static boolean isProtocolPresent(String url) {
        return url.matches("^(http|https|ftp)://.*$");
    }

    public static String searchInGoogle(String keyword) {
        return String.format("http://www.google.com/search?q=%s", keyword);
    }

    public static Image getFavicon(String url) throws URISyntaxException, MalformedURLException, IOException {
        String googleService = "https://www.google.com/s2/favicons?domain=" + getBaseName(url);
        BufferedImage bufImg = ImageIO.read(new URL(googleService));
        return SwingFXUtils.toFXImage(bufImg, null);
    }

    public static String getBaseName(String url) throws URISyntaxException, MalformedURLException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain;
    }
}
