package org.mule.tools.maven.exchange;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.exchange.api.ExchangeObject;
import org.mule.tools.maven.exchange.api.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "upsert")
public class UpsertMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;
    @Parameter( name = "anypointUsername", required = true, readonly = true)
    private String anypointUsername;
    @Parameter( name = "anypointPassword", required = true, readonly = true)
    private String anypointPassword;
    @Parameter( name = "nameUrl", required = true, readonly = true)
    private String nameUrl;
    @Parameter( name = "typeId", required = true, readonly = true)
    private Integer typeId;
    @Parameter( name = "anypointEnvironment", defaultValue = "Production", readonly = true)
    private String anypointEnvironment;
    @Parameter( name = "anypointUri", defaultValue = "https://anypoint.mulesoft.com", readonly = true)
    private String anypointUri;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExchangeApi exchangeApi = new ExchangeApi(
                anypointUri,
                getLog(),
                anypointUsername,
                anypointPassword,
                anypointEnvironment);
        exchangeApi.init();

        ExchangeObject exchangeObject = null;
        try {
            exchangeObject = createExchangeObjectFromProject();
        } catch (IOException e) {
            // TODO Handle exceptions properly
            e.printStackTrace();
        }
        try {
            upsertExchangeObject(exchangeApi, exchangeObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ExchangeObject createExchangeObjectFromProject() throws IOException {
        ExchangeObject exchangeObject = new ExchangeObject();
        List<Version> versions = new ArrayList();
        Version version = new Version();

        // Populate values from Maven project
        exchangeObject.setName(mavenProject.getArtifactId());
        exchangeObject.setNameUrl(nameUrl);
        exchangeObject.setOwner(anypointUsername);
        exchangeObject.setTypeId(typeId);
        exchangeObject.setDescription(getDescription());
        version.setObjectVersion(mavenProject.getVersion());
        version.setMuleVersionId(getMuleRuntimeVersion());
        version.setDownloadUrl(mavenProject.getDistributionManagement().getDownloadUrl());
        versions.add(version);
        exchangeObject.setVersions(versions);

        return exchangeObject;
    }

    private String getMuleRuntimeVersion() {
        // TODO implement
        return "3.7";
    }

    private String getDescription() {
        // TODO Add description from README.md if present
        return "Description placeholder.";
    }

    private void upsertExchangeObject(ExchangeApi exchangeApi, ExchangeObject exchangeObject) throws IOException {
        ExchangeObject currentExchangeObject = exchangeApi.getExchangeObject(exchangeObject);
        if (currentExchangeObject != null) {
            ExchangeObject finalExchangeObject = mergeExchangeObjectsForUpdate(currentExchangeObject, exchangeObject);
            exchangeApi.updateExchangeObject(finalExchangeObject);
        } else {
            exchangeApi.createExchangeObject(exchangeObject);
        }
    }

    private ExchangeObject mergeExchangeObjectsForUpdate(
            ExchangeObject currentExchangeObject, ExchangeObject exchangeObject) {

        // Append versions strategy
        List<Version> returnVersions = currentExchangeObject.getVersions();
        for (Version version: exchangeObject.getVersions()) {
            returnVersions.add(version);
        }
        currentExchangeObject.setVersions(returnVersions);

        // TODO Implement other versions merging strategies
        return currentExchangeObject;
    }
}
