package org.mule.tools.maven.exchange;

import org.apache.maven.settings.Settings;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.exchange.api.*;
import org.mule.tools.maven.exchange.core.ProjectAnalyzer;
import org.mule.tools.maven.exchange.core.ProjectPublisher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;
    @Component( hint = "mng-4384")
    private SecDispatcher secDispatcher;
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
            ExchangeObject exchangeObject = new ExchangeObject();
            setupMandatoryValues(exchangeObject);
            setupCurrentVersion(exchangeObject);
            setupExtraInformation(exchangeObject);
            ProjectPublisher.upsertExchangeObject(exchangeApi, exchangeObject, versioningStrategy, getLog());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage());
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

    private void setupCurrentVersion(ExchangeObject exchangeObject) throws IOException, ParserConfigurationException, SAXException {
        List<Version> versions = new ArrayList();
        Version version = new Version();
        getLog().info("");
        getLog().info("Getting current version values from Project...");

        String downloadUrl = ProjectAnalyzer.obtainDownloadUrl(mavenProject, getLog());

        if (!exchangeObject.getTypeId().equals(ExchangeObjectType.connector.id())) {
            // Download URL is not added to connectors versions
            version.setDownloadUrl( downloadUrl);
            version.setDocUrl(ProjectAnalyzer.obtainDocUrl(mavenProject, getLog()));
            version.setObjectVersion(ProjectAnalyzer.obtainVersion(mavenProject, getLog()));
            version.setMuleVersionId(
                    ProjectAnalyzer.obtainMuleRuntimeVersion(mavenProject, getLog(), this.muleRuntimeVersion));
        } else {
            version = ProjectAnalyzer.obtainConnectorVersionFromArtifact(
                    mavenProject,
                    getLog(),
                    settings,
                    secDispatcher,
                    downloadUrl);
        }

        versions.add(version);
        exchangeObject.setVersions(versions);
    }

    private void setupExtraInformation(ExchangeObject exchangeObject) {
        getLog().info("");
        getLog().info("Getting extra information from Project...");
        exchangeObject.setDescription(ProjectAnalyzer.obtainDescription(getLog()));
    }

}