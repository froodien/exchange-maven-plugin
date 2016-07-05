package org.mule.tools.maven.exchange.core;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.exchange.MojoConstants;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectAnalyzer {

    public static String obtainName( MavenProject mavenProject, Log log) {
        log.info("Project Name: " + mavenProject.getName());
        return mavenProject.getName();
    }

    public static String obtainVersion( MavenProject mavenProject, Log log) {
        log.info("Exchange Object version: " + mavenProject.getVersion());
        return mavenProject.getVersion();
    }

    public static String obtainMuleRuntimeVersion(
            MavenProject mavenProject,
            Log log,
            String muleRuntimeVersionParameter) {
        log.info("Attempting to get Mule Runtime version from 'mule.version' property...");
        String runtimeVersion = mavenProject.getProperties().getProperty("mule.version");
        if (runtimeVersion == null) {
            log.info("'mule.version' not found, using 'muleRuntimeVersion' parameter");
            runtimeVersion = muleRuntimeVersionParameter;
        } else {
            Pattern pattern = Pattern.compile(MojoConstants.MULE_RUNTIME_VERSION_PATTERN_MATCHER);
            Matcher matcher = pattern.matcher(runtimeVersion);
            if (matcher.find()) {
                runtimeVersion = matcher.group(1);
            } else {
                runtimeVersion = muleRuntimeVersionParameter;
            }
        }
        log.info("Mule Version ID: " + runtimeVersion);
        return runtimeVersion;
    }

    public static String obtainDescription( Log log) {
        String description;
        try {
            log.info("Attempting to get Description from README.md file");
            description = FileUtils.readFileToString(new File(MojoConstants.DEFAULT_DESCRIPTION_FILE_SOURCE));
            log.info("Found README.md file, loading Description");
        } catch (IOException e) {
            log.info("README.md file not found, setting Description placeholder");
            description = MojoConstants.DEFAULT_DESCRIPTION_PLACEHOLDER;
        }

        return description;
    }

    public static String obtainDownloadUrl( MavenProject mavenProject, Log log) {
        if (mavenProject.getDistributionManagement() != null) {
            String downloadUrl = mavenProject.getDistributionManagement().getRepository().getUrl() +
                    mavenProject.getDistributionManagementArtifactRepository().pathOf(mavenProject.getArtifact());

            Pattern pattern = Pattern.compile(MojoConstants.MULE_DOWNLOAD_URL_PATTERN_MATCHER);
            Matcher matcher = pattern.matcher(downloadUrl);
            if (matcher.find()) {
                downloadUrl = matcher.group(1) + ".zip";
            }
            log.info("Version Download URL: " + downloadUrl);
            return downloadUrl;
        }
        log.info("Version Download URL: not found");
        return null;
    }

    public static String obtainDocUrl( MavenProject mavenProject, Log log) {
        if (mavenProject.getScm() != null) {
            File readme = new File(MojoConstants.DEFAULT_DESCRIPTION_FILE_SOURCE);
            if (readme.exists()) {
                String connectionUrl = mavenProject.getScm().getConnection();
                Pattern pattern = Pattern.compile(MojoConstants.MULE_GITHUB_CONNECTION_URL_PATTERN_MATCHER);
                Matcher matcher = pattern.matcher(connectionUrl);
                if (matcher.find()) {

                    String docUrl = matcher.group(1)
                        + "/blob/"
                        + mavenProject.getScm().getTag()
                        + "/"
                        + MojoConstants.DEFAULT_DESCRIPTION_FILE_SOURCE;
                    log.info("Version Doc URL: " + docUrl);
                    return docUrl;
                }
            }
        }
        log.info("Version Doc URL: not found");
        return null;
    }
}
