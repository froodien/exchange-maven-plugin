package org.mule.tools.maven.exchange;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.exchange.api.ExchangeObject;
import org.mule.tools.maven.exchange.api.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Maven plugin allows to create and update Mule Applications
 * and Mule Connectors references in Anypoint Exchange.
 *
 * @author Pablo Mantellini
 */
@Mojo(name = "add")
public class AddMojo extends AbstractMojo {
    @Parameter( defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;
    @Parameter( name = "anypointUsername", required = true, readonly = true)
    private String anypointUsername;
    @Parameter( name = "anypointPassword", required = true, readonly = true)
    private String anypointPassword;
    @Parameter( name = "nameUrl", required = true, readonly = true)
    private String nameUrl;
    @Parameter( name = "objectType", required = true, readonly = true)
    private ExchangeObjectType objectType;
    @Parameter(
            name = "versioningStrategy",
            defaultValue = MojoConstants.DEFAULT_VERSIONING_STRATEGY,
            readonly = true
    )
    private VersioningStrategyType versioningStrategy;
    @Parameter(
            name = "muleRuntimeVersion",
            defaultValue = MojoConstants.DEFAULT_MULE_RUNTIME,
            readonly = true
    )
    private String muleRuntimeVersion;
    @Parameter(
            name = "anypointUri",
            defaultValue = MojoConstants.DEFAULT_ANYPOINT_URI,
            readonly = true
    )
    private String anypointUri;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExchangeApi exchangeApi = new ExchangeApi(
                anypointUri,
                getLog(),
                anypointUsername,
                anypointPassword,
                MojoConstants.DEFAULT_ANYPOINT_ENVIRONMENT);
        exchangeApi.init();

        ExchangeObject exchangeObject = new ExchangeObject();
        setupMandatoryValues(exchangeObject);
        setupCurrentVersion(exchangeObject);
        setupExtraInformation(exchangeObject);

        upsertExchangeObject(exchangeApi, exchangeObject);
    }

    private void setupMandatoryValues(ExchangeObject exchangeObject) {
        getLog().info("");
        getLog().info("Getting mandatory values from Project...");
        String artifactId = mavenProject.getArtifactId();
        getLog().info("Project Name: " + artifactId);
        exchangeObject.setName(artifactId);
        getLog().info("Exchange Name Url: " + nameUrl);
        exchangeObject.setNameUrl(nameUrl);
        getLog().info("Exchange Owner: " + anypointUsername);
        exchangeObject.setOwner(anypointUsername);
        getLog().info("Exchange Object type: " + objectType);
        exchangeObject.setTypeId(objectType.id());
    }

    private void setupCurrentVersion(ExchangeObject exchangeObject) {
        List<Version> versions = new ArrayList();
        Version version = new Version();
        getLog().info("");
        getLog().info("Getting current version values from Project...");

        String objectVersion = mavenProject.getVersion();
        getLog().info("Exchange Object version: " + objectVersion);
        version.setObjectVersion(objectVersion);

        String muleRuntimeVersion = obtainMuleRuntimeVersion();
        getLog().info("Mule Version ID: " + muleRuntimeVersion);
        version.setMuleVersionId(muleRuntimeVersion);

        String downloadUrl = obtainDownloadUrl();
        getLog().info("Version Download URL: " + downloadUrl);
        version.setDownloadUrl(downloadUrl);

        versions.add(version);
        exchangeObject.setVersions(versions);
    }

    private void setupExtraInformation(ExchangeObject exchangeObject) {
        getLog().info("");
        getLog().info("Getting extra information from Project...");
        exchangeObject.setDescription(obtainDescription());
    }

    private String obtainMuleRuntimeVersion() {
        getLog().info("Attempting to get Mule Runtime version from 'mule.version' property...");
        String runtimeVersion = mavenProject.getProperties().getProperty("mule.version");
        if (runtimeVersion == null) {
            getLog().info("'mule.version' not found, using 'muleRuntimeVersion' parameter");
            runtimeVersion = this.muleRuntimeVersion;
        } else {
            Pattern pattern = Pattern.compile(MojoConstants.MULE_RUNTIME_VERSION_PATTERN_MATCHER);
            Matcher matcher = pattern.matcher(runtimeVersion);
            if (matcher.find()) {
                runtimeVersion = matcher.group(1);
            } else {
                runtimeVersion = this.muleRuntimeVersion;
            }
        }
        return runtimeVersion;
    }

    private String obtainDescription() {
        String description;
        try {
            getLog().info("Attempting to get Description from README.md file");
            description = FileUtils.readFileToString(new File(MojoConstants.DEFAULT_DESCRIPTION_FILE_SOURCE));
            getLog().info("Found README.md file, loading Description");
        } catch (IOException e) {
            getLog().info("README.md file not found, setting Description placeholder");
            description = MojoConstants.DEFAULT_DESCRIPTION_PLACEHOLDER;
        }

        return description;
    }

    private String obtainDownloadUrl() {
        if (mavenProject.getDistributionManagement() != null) {
            return mavenProject.getDistributionManagement().getDownloadUrl();
        }
        return null;
    }

    private void upsertExchangeObject(ExchangeApi exchangeApi, ExchangeObject exchangeObject) {
        ExchangeObject currentExchangeObject = null;
        try {
            getLog().info("");
            getLog().info("Checking if Project already exists in Exchange...");
            currentExchangeObject = exchangeApi.getExchangeObject(exchangeObject);
        } catch (IOException e) {
            getLog().error("Get object from Exchange failed with error " + e.getMessage());
        }
        if (currentExchangeObject != null) {
            getLog().info("");
            getLog().info("Project found in Exchange, updating versions...");
            ExchangeObject finalExchangeObject = mergeExchangeObjectsForUpdate(currentExchangeObject, exchangeObject);
            try {
                exchangeApi.updateExchangeObject(finalExchangeObject);
            } catch (IOException e) {
                getLog().error("Update object in Exchange failed with error " + e.getMessage());
            }
            getLog().info("Project successfully updated");
        } else {
            try {
                getLog().info("");
                getLog().info("Project not found in Exchange, creating new entry...");
                exchangeApi.createExchangeObject(exchangeObject);
            } catch (IOException e) {
                getLog().error("Create object in Exchange failed with error " + e.getMessage());
            }
            getLog().info("Project successfully created");
        }
    }

    private ExchangeObject mergeExchangeObjectsForUpdate(
            ExchangeObject currentExchangeObject, ExchangeObject newExchangeObject) {

        currentExchangeObject.setVersions(
                versioningStrategy.mergeVersions(
                        currentExchangeObject.getVersions(),
                        newExchangeObject.getVersions()
                )
        );

        return currentExchangeObject;
    }
}