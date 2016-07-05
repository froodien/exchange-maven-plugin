package org.mule.tools.maven.exchange.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.maven.exchange.api.cs.AbstractMuleApi;
import org.mule.tools.maven.exchange.api.cs.ApiException;

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
            ExchangeApiVersion exchangeApiVersion,
            String businessGroup) {
        super(uri, log, username, password, businessGroup);
        this.exchangeApiVersion = exchangeApiVersion;
    }

    @Override
    public void init() throws IOException {
        super.init();
        try {
            exchangeToken = obtainExchangeToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an object entry in Exchange from its model object.
     * @param exchangeObject The object to update in Exchange.
     * @return The object when updated.
     * @throws org.mule.tools.maven.exchange.api.cs.ApiException if not successful.
     * @throws java.io.IOException if model error.
     */
    public ExchangeObject updateExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this, exchangeObject);
        Entity<String> json = Entity.json(mapper.writeValueAsString(exchangeObject));
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
     * @throws org.mule.tools.maven.exchange.api.cs.ApiException if not successful.
     * @throws java.io.IOException if model error.
     */
    public ExchangeObject createExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this, exchangeObject);
        Entity<String> json = Entity.json(mapper.writeValueAsString(exchangeObject));
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
     * @throws org.mule.tools.maven.exchange.api.cs.ApiException if not successful.
     */
    public ExchangeObject getExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String object_path = exchangeApiVersion.buildExchangeObjectsDomainPath(this);
        object_path += "/" + exchangeObject.getNameUrl();
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
     * @throws org.mule.tools.maven.exchange.api.cs.ApiException if not successful.
     */
    public ExchangeObject deleteExchangeObject(ExchangeObject exchangeObject) throws ApiException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this, exchangeObject);
        Response response = delete(uri, object_path);

        if (response.getStatus() == 200)
        {
            return mapper.readValue(response.readEntity(String.class), ExchangeObject.class);
        }

        throw new ApiException(response);
    }

    public void requestForPublishing(ExchangeObject exchangeObject) throws ApiException, IOException {
        String object_path = exchangeApiVersion.buildExchangeObjectsPath(this, exchangeObject);
        Entity<String> json = Entity.json("{ }");
        Response response = post(uri, object_path + "/publishingRequest", json);

        if (response.getStatus() != 200)
        {
            throw new ApiException(response);
        }
    }

    private String obtainExchangeToken() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json_string = String.format(EXCHANGE_TOKEN_REQUEST_TEMPLATE, bearerToken);
        Entity<String> json = Entity.json(json_string);

        Response response = post(uri, EXCHANGE_TOKEN_PATH, json);

        if (response.getStatus() == 200)
        {
            ExchangeAuthorizationResponse authorizationResponse =
                    mapper.readValue(
                            response.readEntity(String.class),
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
