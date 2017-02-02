package org.jenkinsci.plugins.zanata.zanatareposync;

import java.io.IOException;

import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class WithJenkins {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    public final HtmlForm goToGlobalConfigForm() throws IOException, SAXException {
        JenkinsRule.WebClient client = configureWebClient();
        HtmlPage p = client.goTo("configure");
        return p.getFormByName("config");
    }

    public final JenkinsRule.WebClient configureWebClient() {
        JenkinsRule.WebClient client = j.createWebClient();
        client.setJavaScriptEnabled(true);
        return client;
    }
}
