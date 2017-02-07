package org.jenkinsci.plugins.zanata.zanatareposync;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.WithoutJenkins;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class ZanataBuilderTest extends WithJenkins {


    @Mock
    private AbstractProject mockProject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithoutJenkins
    public void descriptorBasics() {
        ZanataBuilder.DescriptorImpl descriptor =
                new ZanataBuilder.DescriptorImpl();
        assertThat(descriptor.getDisplayName()).isEqualTo("Zanata Sync");
        assertThat(descriptor.isApplicable(mockProject.getClass())).isTrue();

    }

    @Test
    public void testCredentialsOptions() throws IOException {
        String username = "user";
        String password = "s3cr3t";
        String credentialId = "zanata_cred";
        String description = "zanata credentials";
        addUsernamePasswordCredential(username, password, credentialId, description);

        ZanataBuilder.DescriptorImpl descriptor = j.jenkins
                .getDescriptorByType(ZanataBuilder.DescriptorImpl.class);

        FreeStyleProject p = j.createFreeStyleProject();
        ListBoxModel listBox = descriptor
                .doFillZanataCredentialsIdItems(p, "remote", credentialId);

        assertThat(listBox).hasSize(2).describedAs("one is none, one is our added credential");
        assertThat(listBox.get(1).name).contains(username).contains(description);

    }

    private void addUsernamePasswordCredential(String username, String password, String credentialId, String description)
            throws IOException {
        UsernamePasswordCredentialsImpl
                zanataCred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                credentialId, description, username, password);
        CredentialsProvider.lookupStores(j.jenkins).iterator().next().addCredentials(
                Domain.global(), zanataCred);
    }

    @Test
    @WithoutJenkins
    public void zanataURLCanBeNullOrEmpty() throws Exception {
        ZanataBuilder.DescriptorImpl descriptor =
                new ZanataBuilder.DescriptorImpl();
        assertThat(descriptor.doCheckZanataURL(null).kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckZanataURL("").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckZanataURL("  ").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    @WithoutJenkins
    public void invalidURLWillGetErrorForZanataURLField() throws Exception {
        ZanataBuilder.DescriptorImpl descriptor =
                new ZanataBuilder.DescriptorImpl();
        assertThat(descriptor.doCheckZanataURL("a").kind).isEqualTo(FormValidation.Kind.ERROR);
        assertThat(descriptor.doCheckZanataURL("a.b").kind).isEqualTo(FormValidation.Kind.ERROR);
        assertThat(descriptor.doCheckZanataURL("http//a").kind).isEqualTo(FormValidation.Kind.ERROR);
    }

    @Test
    @WithoutJenkins
    public void validURLWillGetOKForZanataURLField() throws Exception {
        ZanataBuilder.DescriptorImpl descriptor =
                new ZanataBuilder.DescriptorImpl();
        assertThat(descriptor.doCheckZanataURL("http://zanata.org").kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    @WithoutJenkins
    public void testPushTypeOptions() {
        ZanataBuilder.DescriptorImpl descriptor =
                new ZanataBuilder.DescriptorImpl();
        ListBoxModel options = descriptor.doFillSyncOptionItems(null);
        assertThat(options).hasSize(3);
        assertThat(options.stream().map(option -> option.value))
                .contains("source", "trans", "both");
    }

    @Test
    // https://wiki.jenkins-ci.org/display/JENKINS/Unit+Test#UnitTest-Configurationroundtriptesting
    public void configurationRoundTrip() throws Exception {
        String username = "user";
        String password = "s3cr3t";
        String credentialId = "zanata_cred";
        String description = "zanata credentials";
        addUsernamePasswordCredential(username, password, credentialId, description);
        FreeStyleProject p = j.createFreeStyleProject();

        // set some properties
        ZanataBuilder before = new ZanataBuilder(credentialId);
        before.setPullFromZanata(true);
        before.setPushToZanata(false);
        before.setSyncOption("both");
        before.setZanataLocaleIds("zh");
        before.setZanataProjectConfigs("zanata.xml");
        before.setZanataURL("http://localhost:8080/zanata");

        p.getBuildersList().add(before);

        j.submit(j.createWebClient().getPage(p,"configure")
                .getFormByName("config"));

        ZanataBuilder after = p.getBuildersList().get(ZanataBuilder.class);

        j.assertEqualBeans(before, after, "pullFromZanata,pushToZanata,syncOption,zanataLocaleIds,zanataProjectConfigs,zanataURL,zanataCredentialsId");
    }

    @Test
    public void jobWillAbortIfCredentialIdNotFound() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList()
                .add(new ZanataBuilder("NotExistCredentialId"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        List<String> logs = build.getLog(100);
        assertThat(build.getResult()).isEqualTo(Result.FAILURE);
        assertThat(logs).contains(
                "ERROR: Zanata credential with ID [NotExistCredentialId] can not be found.");
    }
}
