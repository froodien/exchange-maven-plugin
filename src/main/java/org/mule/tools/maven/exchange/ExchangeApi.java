package org.mule.tools.maven.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.maven.exchange.api.ExchangeAuthorizationResponse;
import org.mule.tools.maven.exchange.api.ExchangeObject;
import org.mule.tools.maven.exchange.api.User;
import org.mule.tools.maven.plugin.mule.AbstractMuleApi;
import org.mule.tools.maven.plugin.mule.ApiException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class ExchangeApi extends AbstractMuleApi {
    private static final String EXCHANGE_TOKEN_HEADER = "x-token";
    private static final String EXCHANGE_TOKEN_PATH = "/exchange/api/exchangeToken";
    private static final String EXCHANGE_TOKEN_REQUEST_TEMPLATE = "{" +
            "  \"access_token\": \"%s\"" +
            "  }";
    private String exchangeToken;
    private User user;
    private ExchangeApiVersion exchangeApiVersion;

    public ExchangeApi(
            String uri,
            Log log,
            String username,
            String password,
            String environment,
            ExchangeApiVersion exchangeApiVersion) {
        // Business groups not supported yet
        super(uri, log, username, password, environment, null);
        this.exchangeApiVersion = exchangeApiVersion;
    }

    @Override
    public void init()
    {
        super.init();
        exchangeToken = obtainExchangeToken();
    }

    /**
     * Updates an object entry in Exchange from its model object.
     * @param exchangeObject The object to update in Exchange.
     * @return The object when updated.
     * @throws org.mule.tools.maven.plugin.mule.ApiException if not successful.
     * @throws java.io.IOException if model error.
     */
    public ExchangeObject updateExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Entity<String> json = Entity.json(mapper.writeValueAsString(exchangeObject));
        Integer objectId = exchangeObject.getId();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this) + '/' + objectId;
        Response response = put(uri, object_path, json);

        if (response.getStatus() == 200)
        {
            return mapper.readValue(response.readEntity(String.class), ExchangeObject.class);
        }
        else
        {
            throw new ApiException(response);
        }
    }

    /**
     * Creates an object entry in Exchange from its model object.
     * @param exchangeObject The object to create in Exchange.
     * @return The object when created.
     * @throws org.mule.tools.maven.plugin.mule.ApiException if not successful.
     * @throws java.io.IOException if model error.
     */
    public ExchangeObject createExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Entity<String> json = Entity.json(mapper.writeValueAsString(exchangeObject));
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this) + '/';
        Response response = post(uri, object_path, json);

        if (response.getStatus() == 201)
        {
            return mapper.readValue(response.readEntity(String.class), ExchangeObject.class);
        }
        else
        {
            throw new ApiException(response);
        }
    }

    /**
     * Looks up an object by its model object.
     * @param exchangeObject The object to look up.
     * @return The object found, or null if it does not exist.
     * @throws org.mule.tools.maven.plugin.mule.ApiException if not successful.
     */
    public ExchangeObject getExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this) + '/';
        object_path += exchangeObject.getNameUrl();
        Response response = get(uri, object_path);

        if (response.getStatus() == 200)
        {
            return mapper.readValue(response.readEntity(String.class), ExchangeObject.class);
        }

        if (response.getStatus() == 404) {
            return null;
        }

        throw new ApiException(response);
    }

    /**
     * Deletes an object by its model object.
     * @param exchangeObject The object to delete.
     * @return The object when deleted.
     * @throws org.mule.tools.maven.plugin.mule.ApiException if not successful.
     */
    public ExchangeObject deleteExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Integer objectId = exchangeObject.getId();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this) + '/' + objectId;
        Response response = delete(uri, object_path);

        if (response.getStatus() == 200)
        {
            return mapper.readValue(response.readEntity(String.class), ExchangeObject.class);
        }

        throw new ApiException(response);
    }

    private String obtainExchangeToken() {
        String bearerToken = null;
        try {
            bearerToken = (String) FieldUtils.readField(this, "bearerToken", true);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        String json_string = String.format(EXCHANGE_TOKEN_REQUEST_TEMPLATE, bearerToken);
        Entity<String> json = Entity.json(json_string);

        Response response = post(uri, EXCHANGE_TOKEN_PATH, json);

        if (response.getStatus() == 200)
        {
            ExchangeAuthorizationResponse authorizationResponse = response.readEntity(
                    ExchangeAuthorizationResponse.class
            );
            user = authorizationResponse.getUser();
            return authorizationResponse.getToken();
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

    public User getUser() {
        return user;
    }
}
