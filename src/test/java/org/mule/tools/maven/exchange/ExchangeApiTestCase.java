package org.mule.tools.maven.exchange;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.mule.tools.maven.exchange.api.ExchangeObject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ExchangeApiTestCase
{
    private static final String URI = "https://anypoint.mulesoft.com";
    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");
    private static final String ENVIRONMENT = "Production";

    private static final String EXCHANGE_OBJECT_NAME_URL = "object-sample";
    private static final String EXISTING_EXCHANGE_OBJECT_NAME_URL = "workday-salesforce-worker-migration";

    private ExchangeApi exchangeApi;

    @Before
    public void setup()
    {
        exchangeApi = new ExchangeApi(URI, null, USERNAME, PASSWORD, ENVIRONMENT);
        exchangeApi.init();
    }

    @Test
    public void getExchangeObject()
    {
        ExchangeObject exchangeObject = createTestExchangeObject(EXISTING_EXCHANGE_OBJECT_NAME_URL);
        verifyExchangeObjectExist(exchangeObject);
    }

    @Test
    public void createExchangeObject()
    {
        ExchangeObject exchangeObject = createTestExchangeObject(EXCHANGE_OBJECT_NAME_URL);
        verifyExchangeObjectDoesntExist(exchangeObject);
        exchangeApi.createExchangeObject(exchangeObject);
        verifyExchangeObjectExist(exchangeObject);
    }

    @Test
    public void updateExchangeObject() throws Exception
    {
        ExchangeObject exchangeObject = createTestExchangeObject(EXCHANGE_OBJECT_NAME_URL);
        verifyExchangeObjectDoesntExist(exchangeObject);
        exchangeApi.createExchangeObject(exchangeObject);
        verifyExchangeObjectExist(exchangeObject);

        // TODO Update original versions and upsert
    }

    private static ExchangeObject createTestExchangeObject(String nameUrl)
    {
        ExchangeObject exchangeObject = new ExchangeObject();
        // TODO add random name
        exchangeObject.setNameUrl(nameUrl);
        // TODO add valid fields
        return exchangeObject;
    }

    private void verifyExchangeObjectDoesntExist(ExchangeObject exchangeObject)
    {
        assertThat(exchangeApi.getExchangeObject(exchangeObject), nullValue());
    }

    private void verifyExchangeObjectExist(ExchangeObject exchangeObject)
    {
        assertThat(exchangeApi.getExchangeObject(exchangeObject), notNullValue());
    }

}