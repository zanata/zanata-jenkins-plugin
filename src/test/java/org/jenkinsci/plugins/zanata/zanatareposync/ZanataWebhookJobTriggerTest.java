package org.jenkinsci.plugins.zanata.zanatareposync;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;

public class ZanataWebhookJobTriggerTest {
    private static final Header JSON_CONTENT_TYPE =
            new Header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    private static final String PAYLOAD =
            "{\"type\":\"milestone\", \"project\":\"zanata-server\"}";

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();
    private RequestSpecification spec;

    @Before
    public void setUp() {
        spec = new RequestSpecBuilder()
                .setBaseUri(jenkins.getInstance().getRootUrl())
                .setBasePath(
                        ZanataWebhookProjectProperty.DescriptorImpl.URL_PATH
                                .concat("/"))
                .setConfig(newConfig().encoderConfig(
                        encoderConfig().defaultContentCharset(Charsets.UTF_8)
                                .appendDefaultContentCharsetToContentTypeIfUndefined(
                                        false)))
                .build();
    }

    @Test
    public void willGetNotFoundIfNoJobName() {
        given().spec(spec)
                .header(JSON_CONTENT_TYPE).body(PAYLOAD).log().all()
                .when().get()
                .then()
                .statusCode(404)
                .body("message", equalTo("Parameter 'job' is missing or no value assigned."));
    }

    @Test
    public void willGetNotFoundIfNoPayload() {
        given().spec(spec)
                .header(JSON_CONTENT_TYPE)
                .param("job", "some")
                .log().all()
                .when().get()
                .then()
                .statusCode(404)
                .body("message", equalTo("No payload or URI contains invalid entries."));
    }

    @Test
    public void willGetMediaUnsupportedIfContentTypeIsNotJSON() {
        given().spec(spec)
                .param("job", "some")
                .body(PAYLOAD).log().all()
                .when().get()
                .then()
                .statusCode(415)
                .body("message", equalTo("Only Accept JSON payload."));
    }

    @Test
    public void willGetNotFoundIfJobNameCanNotBeFound() {
        given().spec(spec)
                .param("job", "NonExistJob")
                .header(JSON_CONTENT_TYPE).body(PAYLOAD)
                .when().get()
                .then()
                .statusCode(404)
                .body("message", equalTo("Job 'NonExistJob' is not defined in Jenkins or is not buildable"));
    }

}
