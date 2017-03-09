package org.jenkinsci.plugins.zanata.cli.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jenkinsci.plugins.zanata.SyncJobDetail.Builder.builder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jenkinsci.plugins.zanata.SyncJobDetail;
import org.jenkinsci.plugins.zanata.cli.service.PullService;
import org.jenkinsci.plugins.zanata.cli.service.PushService;
import org.jenkinsci.plugins.zanata.exception.ZanataSyncException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.config.ZanataConfig;

import com.google.common.collect.Lists;

public class ZanataSyncServiceImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ZanataSyncServiceImpl service;
    @Mock
    private PullService pullService;
    @Mock
    private PushService pushService;
    private File workspace;
    @Captor
    private ArgumentCaptor<PushOptions> pushOptionsCaptor;
    @Captor
    private ArgumentCaptor<PullOptions> pullOptionsCaptor;
    private static Marshaller marshaller;

    @BeforeClass
    public static void setupMarshaller() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
        marshaller = jc.createMarshaller();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        workspace = temporaryFolder.newFolder();

        ZanataConfig zanataConfig = makeZanataConfig("test");

        marshaller.marshal(zanataConfig, new File(workspace, "zanata.xml"));

    }

    private static ZanataConfig makeZanataConfig(String project) throws MalformedURLException {
        ZanataConfig zanataConfig = new ZanataConfig();
        LocaleList localeList = new LocaleList();
        Lists.newArrayList("zh", "ja", "de", "pl").stream().map(
                LocaleMapping::new).forEach(localeList::add);
        zanataConfig.setLocales(localeList);
        zanataConfig.setUrl(new URL("http://zanata.org"));
        zanataConfig.setProject(project);
        zanataConfig.setProjectVersion("master");
        zanataConfig.setProjectType("podir");
        return zanataConfig;
    }

    @Test
    @WithoutJenkins
    public void testOverrideUrlForPush() throws MalformedURLException {
        String zanataUrl = "http://localhost";
        SyncJobDetail jobDetail = builder().setZanataUrl(zanataUrl).build();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pushToZanata(workspace.toPath());

        Mockito.verify(pushService).pushToZanata(pushOptionsCaptor.capture());
        assertThat(pushOptionsCaptor.getValue().getUrl())
                .isEqualTo(new URL(zanataUrl));

    }

    @Test
    @WithoutJenkins
    public void testOverrideUrlForPull() throws MalformedURLException {
        String zanataUrl = "http://localhost";
        SyncJobDetail jobDetail = builder().setZanataUrl(zanataUrl).build();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pullFromZanata(workspace.toPath());

        Mockito.verify(pullService).pullFromZanata(pullOptionsCaptor.capture());
        assertThat(pullOptionsCaptor.getValue().getUrl())
                .isEqualTo(new URL(zanataUrl));

    }

    @Test
    @WithoutJenkins
    public void invalidURLWillTriggerError() {
        String zanataUrl = "not valid url";
        SyncJobDetail jobDetail = builder().setZanataUrl(zanataUrl).build();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    service.pushToZanata(workspace.toPath());
                });

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    service.pullFromZanata(workspace.toPath());
                });
    }

    @Test
    @WithoutJenkins
    public void canOverrideLocaleIdForPush() {
        String localeIds = "zh,ja";
        SyncJobDetail jobDetail = builder().setLocaleId(localeIds).build();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pushToZanata(workspace.toPath());

        Mockito.verify(pushService).pushToZanata(pushOptionsCaptor.capture());
        assertThat(pushOptionsCaptor.getValue().getLocaleMapList())
                .contains(new LocaleMapping("zh"), new LocaleMapping("ja"));

    }

    @Test
    @WithoutJenkins
    public void canOverrideLocaleIdForPull() {
        String localeIds = "de";
        SyncJobDetail jobDetail = builder().setLocaleId(localeIds).build();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pullFromZanata(workspace.toPath());

        Mockito.verify(pullService).pullFromZanata(pullOptionsCaptor.capture());
        assertThat(pullOptionsCaptor.getValue().getLocaleMapList())
                .contains(new LocaleMapping("de"));

    }

    @Test
    @WithoutJenkins
    public void failIfNoProjectConfigCanBeFound() throws IOException {
        SyncJobDetail jobDetail = builder().build();

        File emptyWorkspace = temporaryFolder.newFolder();
        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        assertThatExceptionOfType(ZanataSyncException.class)
                .isThrownBy(() -> {
                    service.pushToZanata(emptyWorkspace.toPath());
                })
                .withMessage("can not find project config (zanata.xml) in the repo");

        assertThatExceptionOfType(ZanataSyncException.class)
                .isThrownBy(() -> {
                    service.pullFromZanata(emptyWorkspace.toPath());
                })
                .withMessage("can not find project config (zanata.xml) in the repo");
    }

    @Test
    @WithoutJenkins
    public void canSpecifyNonExistProjectConfigsForPush() {
        SyncJobDetail jobDetail =
                builder().setProjectConfigs("non-exist.xml").build();

        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pushToZanata(workspace.toPath());

        Mockito.verifyZeroInteractions(pushService);
    }

    @Test
    @WithoutJenkins
    public void canSpecifyNonExistProjectConfigsForPull() {
        SyncJobDetail jobDetail =
                builder().setProjectConfigs("non-exist.xml").build();

        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pullFromZanata(workspace.toPath());

        Mockito.verifyZeroInteractions(pullService);
    }

    @Test
    @WithoutJenkins
    public void canSpecifyProjectConfigsForPush() throws Exception {
        // we have a workspace that contains two zanata.xml in two folders
        File workspace = temporaryFolder.newFolder();
        File folder1 = new File(workspace, "one");
        assertThat(folder1.mkdirs()).isTrue();
        File folder2 = new File(workspace, "two");
        assertThat(folder2.mkdirs()).isTrue();

        ZanataConfig project1 = makeZanataConfig("project-one");
        ZanataConfig project2 = makeZanataConfig("project-two");
        marshaller.marshal(project1, new File(folder1, "zanata.xml"));
        marshaller.marshal(project2, new File(folder2, "zanata.xml"));

        // when specify project configs in the job
        SyncJobDetail jobDetail =
                builder().setProjectConfigs("one/zanata.xml").build();

        service =
                new ZanataSyncServiceImpl(pullService, pushService, jobDetail);

        service.pushToZanata(workspace.toPath());

        // then only push the one we specified
        Mockito.verify(pushService, Mockito.times(1))
                .pushToZanata(pushOptionsCaptor.capture());
        assertThat(pushOptionsCaptor.getValue().getProj()).isEqualTo("project-one");
    }

}
