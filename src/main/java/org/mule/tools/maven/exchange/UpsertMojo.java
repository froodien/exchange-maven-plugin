package org.mule.tools.maven.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mule.tools.maven.plugin.exchange.api.ExchangeObject;
import org.mule.tools.maven.plugin.exchange.api.Version;

import java.io.IOException;
import java.util.List;

@Mojo(name = "upsert")
public class UpsertMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO Get from parameters
        String uri = "https://anypoint.mulesoft.com";
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        String environment = "Production";

        ExchangeApi exchangeApi = new ExchangeApi(uri, getLog(), username, password, environment);
        exchangeApi.init();

        ExchangeObject exchangeObject = null;
        try {
            exchangeObject = createExchangeObjectFromProject();
        } catch (IOException e) {
            // TODO Handle exceptions properly
            e.printStackTrace();
        }
        upsertExchangeObject(exchangeApi, exchangeObject);
    }

    private ExchangeObject createExchangeObjectFromProject() throws IOException {
        // TODO implement

        // JSON from String to Object
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = inspectProject();
        ExchangeObject exchangeObject = mapper.readValue(jsonInString, ExchangeObject.class);

        return exchangeObject;
    }

    private String inspectProject() {
        // TODO Review best return type for this
        return null;
    }

    private void upsertExchangeObject(ExchangeApi exchangeApi, ExchangeObject exchangeObject) {
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
