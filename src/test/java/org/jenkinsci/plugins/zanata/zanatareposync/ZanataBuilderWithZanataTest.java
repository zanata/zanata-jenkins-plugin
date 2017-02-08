package org.jenkinsci.plugins.zanata.zanatareposync;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jenkinsci.plugins.zanata.zanatareposync.TestUtils.readFileAsString;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * This test class will setup a stubbed Zanata server and perform a full end to
 * end Zanata push and pull.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataBuilderWithZanataTest extends WithJenkins {
    private static final int FIVE_MINUTES = 1000 * 60 * 5;
    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    private String zanataXMLContent;

    @Before
    public void setUp() {
        int port = wireMockRule.port();
        zanataXMLContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<config xmlns=\"http://zanata.org/namespace/config/\">"
                        + "  <url>http://localhost:" + port + "/</url>"
                        + "  <project>test-project</project>"
                        + "  <project-version>master</project-version>"
                        + "  <project-type>properties</project-type>"
                        + "</config>";
    }

    @Test(timeout = FIVE_MINUTES)
    public void jobCanPushToZanata() throws Exception {
        String username = "user";
        String password = "s3cr3t";
        String credentialId = "zanata_cred";
        addUsernamePasswordCredential(username, password, credentialId, "");

        setupPushToZanataStub(username, password);

        FreeStyleProject project = j.createFreeStyleProject();

        // Set workspace up as if we have done a GIT checkout
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                    BuildListener listener)
                    throws InterruptedException, IOException {
                build.getWorkspace().child("zanata.xml").write(zanataXMLContent,
                        "UTF-8");
                build.getWorkspace().child("messages.properties")
                        .write("greeting=Hello, World", "UTF-8");
                return true;
            }
        });
        ZanataBuilder builder = new ZanataBuilder(credentialId);
        builder.setPushToZanata(true);
        builder.setPullFromZanata(false);
        builder.setSyncOption("source");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertThat(build.getResult()).isEqualTo(Result.SUCCESS);

    }

    /**
     * REST calls to the stubbed Zanata server for pushing source.
     *
     * @param username
     *            zanata username
     * @param password
     *            zanata api key
     */
    private void setupPushToZanataStub(String username, String password) {
        wireMockRule.givenThat(get(urlPathMatching(
                "/rest/projects/p/test-project/iterations/i/master/locales"))
                        .withHeader("X-Auth-User", equalTo(username))
                        .withHeader("X-Auth-Token", equalTo(password))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(readFileAsString(
                                        "zanata-stub-output/locales.xml"))));
        wireMockRule.givenThat(get(urlPathEqualTo(
                "/rest/projects/p/test-project/iterations/i/master/r"))
                        .withHeader("X-Auth-User", equalTo(username))
                        .withHeader("X-Auth-Token", equalTo(password))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(readFileAsString(
                                        "zanata-stub-output/documentNames.xml"))));
        wireMockRule.givenThat(put(urlPathEqualTo(
                "/rest/async/projects/p/test-project/iterations/i/master/r/messages"))
                        .withHeader("X-Auth-User", equalTo(username))
                        .withHeader("X-Auth-Token", equalTo(password))
                        .withQueryParam("ext", equalTo("comment"))
                        .withQueryParam("copyTrans", equalTo("false"))
                        .withRequestBody(containing("Hello, World"))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(readFileAsString(
                                        "zanata-stub-output/asyncPushResponse.xml"))));
        wireMockRule.givenThat(get(urlPathEqualTo(
                "/rest/async/f41c044e-a732-42c2-938f-3c23bd50ce59"))
                        .withHeader("X-Auth-User", equalTo(username))
                        .withHeader("X-Auth-Token", equalTo(password))
                        .willReturn(aResponse().withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(readFileAsString(
                                        "zanata-stub-output/getAsyncPushStatus.xml"))));
    }
}
