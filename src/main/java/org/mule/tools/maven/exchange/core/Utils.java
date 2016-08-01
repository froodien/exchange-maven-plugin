package org.mule.tools.maven.exchange.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mule.tools.maven.exchange.MojoConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Utils {
    public static boolean unzipToFolder(File zipFile, File destination) throws ZipException, IOException {
        boolean unziped = false;
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File file = new File(destination, entry.getName());
                if (entry.isDirectory()) {
                    file.getParentFile().mkdirs();
                } else {
                    InputStream is = zip.getInputStream(entry);
                    File parent = file.getParentFile();
                    if (parent != null && parent.exists() == false) {
                        parent.mkdirs();
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        os.close();
                        is.close();
                    }
                    file.setLastModified(entry.getTime());

                }
            }
            unziped = true;
        } finally {
            IOUtils.closeQuietly(zip);
        }
        return unziped;
    }

    public static void downloadFile(String downloadUrl,
                                     String filePath,
                                     String username,
                                     String password) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        CloseableHttpClient httpclient = HttpClients.custom()
                .build();
        try {
            String userCredentials = username + ":" + password;
            String basicAuth = "Basic " + new Base64().encodeAsString(userCredentials.getBytes());
            HttpGet httpget = new HttpGet(downloadUrl);
            httpget.setHeader("Authorization", basicAuth);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
                fileOutputStream.close();
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    public static HashMap<String, String> parseConnectorArtifact(String artifactFilePath, File tmpFolder)
            throws IOException, ParserConfigurationException, SAXException {
        Utils.unzipToFolder(new File(artifactFilePath), tmpFolder);
        File fXmlFile = new File(tmpFolder + File.separator + "feature.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(fXmlFile);
        Element featureElement = (Element) doc.getElementsByTagName("feature").item(0);

        HashMap connectorAttributes = new HashMap<String, String>();
        connectorAttributes.put("featureId", featureElement.getAttribute("id") + ".feature.group");
        connectorAttributes.put("minMuleVersion", obtainMinMuleVersion(featureElement.getAttribute("id")));
        connectorAttributes.put("notes", featureElement.getAttribute("label"));
        connectorAttributes.put("connectorVersion", featureElement.getAttribute("version"));

        return connectorAttributes;
    }

    private static String obtainMinMuleVersion(String featureId) {
        Pattern pattern = Pattern.compile(MojoConstants.CONNECTOR_FEATURE_RUNTIME_PATTERN_MATCHER);
        Matcher matcher = pattern.matcher(featureId);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}
