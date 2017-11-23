package org.jenkinsci.plugins.zanata.zanatareposync;

import static hudson.model.Item.EXTENDED_READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.jenkinsci.plugins.zanata.SlowTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * This test class is for anything requiring a running Jenkins instance but not
 * a Zanata instance.
 *
 * @see ZanataSyncStepBasicsTest
 * @see ZanataSyncStepWithZanataTest
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(SlowTest.class)
public class ZanataSyncStepTest extends WithJenkins {

    @Test
    public void testCheckCredentialId() throws IOException {
        ZanataSyncStep.DescriptorImpl descriptor =
                new ZanataSyncStep.DescriptorImpl();
        AbstractProject context = Mockito.mock(AbstractProject.class);
        when(context.hasPermission(EXTENDED_READ)).thenReturn(true);

        String url = "http://localhost";
        String credentialId = "some_id";
        assertThat(descriptor.doCheckZanataCredentialsId(
                context, url, credentialId).kind)
                .isEqualTo(FormValidation.Kind.WARNING);

        // now we insert a credential with that id
        String username = "user";
        String password = "s3cr3t";
        addUsernamePasswordCredential(username, password, credentialId,
                "");

        assertThat(descriptor.doCheckZanataCredentialsId(
                context, url, credentialId).kind)
                .isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    public void testCredentialsOptions() throws IOException {
        String username = "user";
        String password = "s3cr3t";
        String credentialId = "zanata_cred";
        String description = "zanata credentials";
        addUsernamePasswordCredential(username, password, credentialId,
                description);

        ZanataSyncStep.DescriptorImpl descriptor = j.jenkins
                .getDescriptorByType(ZanataSyncStep.DescriptorImpl.class);

        FreeStyleProject p = j.createFreeStyleProject();
        ListBoxModel listBox = descriptor.doFillZanataCredentialsIdItems(p,
                "remote", credentialId);

        assertThat(listBox).hasSize(2)
                .describedAs("one is none, one is our added credential");
        assertThat(listBox.get(1).name).contains(username)
                .contains(description);

    }

    @Test
    // https://wiki.jenkins-ci.org/display/JENKINS/Unit+Test#UnitTest-Configurationroundtriptesting
    public void configurationRoundTrip() throws Exception {
        String username = "user";
        String password = "s3cr3t";
        String credentialId = "zanata_cred";
        String description = "zanata credentials";
        addUsernamePasswordCredential(username, password, credentialId,
                description);
        FreeStyleProject p = j.createFreeStyleProject();

        // set some properties
        ZanataSyncStep before = new ZanataSyncStep(credentialId);
        before.setPullFromZanata(true);
        before.setPushToZanata(false);
        before.setSyncOption("both");
        before.setZanataLocaleIds("zh");
        before.setZanataProjectConfigs("zanata.xml");
        before.setZanataURL("http://localhost:8080/zanata");

        p.getBuildersList().add(before);

        j.submit(j.createWebClient().getPage(p, "configure")
                .getFormByName("config"));

        ZanataSyncStep after = p.getBuildersList().get(ZanataSyncStep.class);

        j.assertEqualBeans(before, after,
                "pullFromZanata,pushToZanata,syncOption,zanataLocaleIds,zanataProjectConfigs,zanataURL,zanataCredentialsId");
    }

    @Test
    public void jobWillAbortIfCredentialIdNotFound() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList()
                .add(new ZanataSyncStep("NotExistCredentialId"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        List<String> logs = build.getLog(100);
        assertThat(build.getResult()).isEqualTo(Result.FAILURE);
        assertThat(logs).contains(
                "ERROR: credential with ID [NotExistCredentialId] can not be found.");
    }

}
