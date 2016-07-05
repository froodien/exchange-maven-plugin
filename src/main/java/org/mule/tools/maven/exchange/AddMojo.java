package org.mule.tools.maven.exchange;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.exchange.api.*;
import org.mule.tools.maven.exchange.core.ProjectAnalyzer;
import org.mule.tools.maven.exchange.core.ProjectPublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @Parameter( name = "anypointUsername", required = true, readonly = true, property = "anypointUsername")
    private String anypointUsername;
    @Parameter( name = "anypointPassword", required = true, readonly = true, property = "anypointPassword")
    private String anypointPassword;
    @Parameter( name = "nameUrl", required = true, readonly = true, property = "nameUrl")
    private String nameUrl;
    @Parameter( name = "objectType", required = true, readonly = true, property = "objectType")
    private ExchangeObjectType objectType;
    @Parameter(
            name = "versioningStrategy",
            defaultValue = MojoConstants.DEFAULT_VERSIONING_STRATEGY,
            readonly = true,
            property = "versioningStrategy"
    )
    private VersioningStrategyType versioningStrategy;
    @Parameter(
            name = "muleRuntimeVersion",
            readonly = true,
            property = "muleRuntimeVersion"
    )
    private String muleRuntimeVersion;
    @Parameter(
            name = "anypointUri",
            defaultValue = MojoConstants.DEFAULT_ANYPOINT_URI,
            readonly = true,
            property = "anypointUri"
    )
    private String anypointUri;
    @Parameter(
            name = "exchangeApiVersion",
            defaultValue = MojoConstants.DEFAULT_EXCHANGE_API_VERSION,
            readonly = true,
            property = "exchangeApiVersion"
    )
    private ExchangeApiVersion exchangeApiVersion;
    @Parameter(
            name = "businessGroup",
            required = true,
            readonly = true,
            property = "businessGroup"
    )
    private String businessGroup;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExchangeApi exchangeApi = new ExchangeApi(
                anypointUri,
                getLog(),
                anypointUsername,
                anypointPassword,
                exchangeApiVersion,
                businessGroup);
        try {
            exchangeApi.init();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        ExchangeObject exchangeObject = new ExchangeObject();
        setupMandatoryValues(exchangeObject);
        setupCurrentVersion(exchangeObject);
        setupExtraInformation(exchangeObject);

        try {
            ProjectPublisher.upsertExchangeObject(exchangeApi, exchangeObject, versioningStrategy, getLog());
        } catch (IOException e) {
            throw new MojoExecutionException("Exchange object upsert failed with error: " + e.getMessage());
        }
    }

    private void setupMandatoryValues(ExchangeObject exchangeObject) {
        getLog().info("");
        getLog().info("Getting mandatory values from Project...");

        exchangeObject.setName(ProjectAnalyzer.obtainName(mavenProject, getLog()));
        exchangeObject.setNameUrl(nameUrl);
        exchangeObject.setOwner(anypointUsername);
        exchangeObject.setTypeId(objectType.id());

        getLog().info("Exchange Name Url: " + nameUrl);
        getLog().info("Exchange Owner: " + anypointUsername);
        getLog().info("Exchange Object type: " + objectType);
    }

    private void setupCurrentVersion(ExchangeObject exchangeObject) {
        List<Version> versions = new ArrayList();
        Version version = new Version();
        getLog().info("");
        getLog().info("Getting current version values from Project...");

        version.setObjectVersion(ProjectAnalyzer.obtainVersion(mavenProject, getLog()));
        version.setDownloadUrl(ProjectAnalyzer.obtainDownloadUrl(mavenProject, getLog()));
        version.setDocUrl(ProjectAnalyzer.obtainDocUrl(mavenProject, getLog()));
        version.setMuleVersionId(
                ProjectAnalyzer.obtainMuleRuntimeVersion(mavenProject, getLog(), this.muleRuntimeVersion));

        versions.add(version);
        exchangeObject.setVersions(versions);
    }

    private void setupExtraInformation(ExchangeObject exchangeObject) {
        getLog().info("");
        getLog().info("Getting extra information from Project...");
        exchangeObject.setDescription(ProjectAnalyzer.obtainDescription(getLog()));
    }

}