package org.mule.tools.maven.exchange;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.maven.exchange.api.ExchangeObject;
import org.mule.tools.maven.plugin.mule.AbstractMuleApi;
import org.mule.tools.maven.plugin.mule.ApiException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class ExchangeApi extends AbstractMuleApi {
    private static final String EXCHANGE_TOKEN_HEADER = "x-token";
    private static final String EXCHANGE_TOKEN_PATH = "/exchange/api/exchangeToken";
    // private static final String EXCHANGE_OBJECTS_PATH_TEMPLATE = "/exchange/api/%s/objects";
    private static final String EXCHANGE_TOKEN_REQUEST_TEMPLATE = "{" +
            "  \"access_token\": \"%s\"" +
            "  }";
    private String exchangeToken;

    public ExchangeApi(String uri, Log log, String username, String password, String environment) {
        // Business groups not supported yet
        super(uri, log, username, password, environment, null);
    }

    @Override
    public void init()
    {
        super.init();
        try {
            exchangeToken = obtainExchangeToken();
        } catch (IllegalAccessException e) {
            // TODO Handle exception properly
            e.printStackTrace();
        }
    }

    public void updateExchangeObject(ExchangeObject exchangeObject) {
        // TODO Implement with next Exchange release
    }

    public void createExchangeObject(ExchangeObject exchangeObject) {
        // TODO Implement with next Exchange release
    }

    public ExchangeObject getExchangeObject(ExchangeObject exchangeObject) {
        // Return null if it does not exist
        // TODO Implement with next Exchange release
        // String nameUrl = exchangeObject.getNameUrl();
        // String object_path = String.format(EXCHANGE_OBJECTS_PATH_TEMPLATE, getOrgId()) + '/' + nameUrl;

        return null;
    }

    private String obtainExchangeToken() throws IllegalAccessException {
        // TODO Review with next Exchange release
        String bearerToken = (String) FieldUtils.readField(this, "bearerToken", true);
        String json_string = String.format(EXCHANGE_TOKEN_REQUEST_TEMPLATE, bearerToken);
        Entity<String> json = Entity.json(json_string);

        Response response = post(uri, EXCHANGE_TOKEN_PATH, json);

        if (response.getStatus() == 200)
        {
            ExchangeAuthorizationResponse authorizationResponse = response.readEntity(ExchangeAuthorizationResponse.class);
            return authorizationResponse.token;
        }
        else
        {
            throw new ApiException(response);
        }
    }

    @Override
    protected void configureRequest(Invocation.Builder builder)
    {
        super.configureRequest(builder);
        builder.header(EXCHANGE_TOKEN_HEADER, "Bearer " + exchangeToken);
    }
}
