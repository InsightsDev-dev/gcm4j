package com.phonedeck.gcm4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import by.stub.client.StubbyClient;


/**
 *
 */
public class DefaultGcmIntegration
{
    private static final String ENDPOINT = "https://localhost:7443/android.googleapis.com/gcm/send";
    private static final String AUTH_KEY = "AIzaSyB-1uEai2WiUapxCs2Q0GZYzPu7Udno5aA";

    private static StubbyClient stubbyClient;
    private static URL endpointUrl;

    /**
     * No-arg constructor.
     */
    public DefaultGcmIntegration()
    {

    }

    @BeforeClass
    public static void setUpTestClass() throws Exception
    {
        stubbyClient = new StubbyClient();
        File stubby4jConfigFile = new File(DefaultGcmIntegration.class.getResource(
            "/stubby4j/gcm-http-connection-server-stub.json").toURI());
        stubbyClient.startJetty(stubby4jConfigFile.getPath());

        endpointUrl = new URL(ENDPOINT);
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        stubbyClient.stopJetty();
    }

    @Test
    public void testSendBlockingSuccess() throws Exception
    {
        GcmConfig config = new GcmConfig()
            .withEndpoint(endpointUrl)
            .withKey(AUTH_KEY);

        GcmRequest request = new GcmRequest()
            .withRegistrationIds(Lists.newArrayList("42"));

        Gcm gcm = new DefaultGcm(config);
        GcmResponse response = gcm.sendBlocking(request);

        assertEquals(1, response.getResults().size());
        Result result = response.getResults().get(0);
        assertEquals("1:08", result.getMessageId());
    }

    @Test
    public void testSendBlockingInternalServerError() throws Exception
    {
        GcmConfig config = new GcmConfig()
            .withEndpoint(new URL("https://localhost:7443/android.googleapis.com/gcm/send"))
            .withKey(AUTH_KEY);

        GcmRequest request = new GcmRequest()
            .withRegistrationIds(Lists.newArrayList("168"));

        Gcm gcm = new DefaultGcm(config);

        try
        {
            gcm.sendBlocking(request);
        }
        catch (GcmException ge)
        {
            Throwable t = ge.getCause();
            assertNotNull(t);
            assertTrue(t instanceof GcmNetworkException);

            GcmNetworkException gne = (GcmNetworkException) t;
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gne.getCode());
            assertEquals("Internal Server Error", gne.getResponse());
        }
    }

    @Test
    public void testSendBlockingServiceUnavailable() throws Exception
    {
        GcmConfig config = new GcmConfig()
            .withEndpoint(new URL("https://localhost:7443/android.googleapis.com/gcm/send"))
            .withKey(AUTH_KEY);

        GcmRequest request = new GcmRequest()
            .withRegistrationIds(Lists.newArrayList("172"));

        Gcm gcm = new DefaultGcm(config);

        try
        {
            gcm.sendBlocking(request);
        }
        catch (GcmException ge)
        {
            Throwable t = ge.getCause();
            assertNotNull(t);
            assertTrue(t instanceof GcmNetworkException);

            GcmNetworkException gne = (GcmNetworkException) t;
            assertEquals(HttpURLConnection.HTTP_UNAVAILABLE, gne.getCode());
            assertEquals("Service Unavailable", gne.getResponse());
        }
    }
}
