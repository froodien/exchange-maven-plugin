package org.mule.tools.maven.exchange;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.mule.tools.maven.exchange.api.ExchangeObject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.tools.maven.exchange.api.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class ExchangeApiTestCase
{
    private static final String URI = "https://dev.anypoint.mulesoft.com";
    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");
    private static final String ENVIRONMENT = "Production";

    private static final String EXISTING_EXCHANGE_OBJECT_NAME_URL = "msdynamics-salesforce-contact-migration";
    private static final String EXCHANGE_OBJECT_NAME_URL = "object-sample";
    private static final String EXCHANGE_OBJECT_NAME = "Test Creation Object";
    private static final String EXCHANGE_VERSION_MULE_VERSION_ID = "3.7";
    private static final String EXCHANGE_VERSION_DOWNLOAD_URL = "http://www.google.com";
    private static final String EXCHANGE_VERSION_DOC_URL = "http://www.google.com";
    private static final String EXCHANGE_VERSION_OBJECT_VERSION = "1.2.3";

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
    public void createExchangeObject() throws IOException {
        // Create object
        ExchangeObject exchangeObject = createTestExchangeObject(EXCHANGE_OBJECT_NAME_URL + System.currentTimeMillis());
        verifyExchangeObjectDoesntExist(exchangeObject);
        ExchangeObject createdExchangeObject = exchangeApi.createExchangeObject(exchangeObject);
        verifyExchangeObjectExist(createdExchangeObject);

        // Remove object
        exchangeApi.deleteExchangeObject(createdExchangeObject);
        verifyExchangeObjectDoesntExist(createdExchangeObject);
    }

    @Test
    public void updateExchangeObjectWithNewVersion() throws IOException {
        // Create object
        ExchangeObject exchangeObject = createTestExchangeObject(EXCHANGE_OBJECT_NAME_URL + System.currentTimeMillis());
        verifyExchangeObjectDoesntExist(exchangeObject);
        ExchangeObject createdExchangeObject = exchangeApi.createExchangeObject(exchangeObject);
        verifyExchangeObjectExist(createdExchangeObject);

        // Create new version
        Version version = new Version();
        version.setMuleVersionId(EXCHANGE_VERSION_MULE_VERSION_ID);
        version.setDownloadUrl(EXCHANGE_VERSION_DOWNLOAD_URL);
        version.setDocUrl(EXCHANGE_VERSION_DOC_URL);
        version.setObjectVersion(EXCHANGE_VERSION_OBJECT_VERSION);
        List<Version> versions = new ArrayList();
        versions.add(version);

        createdExchangeObject.setVersions(versions);
        ExchangeObject updatedExchangeObject = exchangeApi.updateExchangeObject(createdExchangeObject);
        assertEquals(
                "Object returned in the update should match the expected version name",
                EXCHANGE_VERSION_OBJECT_VERSION,
                updatedExchangeObject.getVersions().get(0).getObjectVersion());

        // Remove object
        exchangeApi.deleteExchangeObject(createdExchangeObject);
        verifyExchangeObjectDoesntExist(createdExchangeObject);
    }

    private static ExchangeObject createTestExchangeObject(String nameUrl)
    {
        // Create object with minimal data
        ExchangeObject exchangeObject = new ExchangeObject();
        exchangeObject.setNameUrl(nameUrl);
        exchangeObject.setName(EXCHANGE_OBJECT_NAME);
        exchangeObject.setOwner(USERNAME);
        exchangeObject.setTypeId(1);

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