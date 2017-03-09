package org.jenkinsci.plugins.zanata.cli.util;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jenkinsci.plugins.zanata.exception.ZanataSyncException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.CommandHook;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.config.ZanataConfig;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PushPullOptionsUtilTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Marshaller marshaller;

    @BeforeClass
    public static void setupMarshaller() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
        marshaller = jc.createMarshaller();
    }

    @Test
    public void failIfZanataProjectConfigIsInvalid() throws IOException {

        assertThatExceptionOfType(ZanataSyncException.class)
                .isThrownBy(() -> PushPullOptionsUtil.applyProjectConfig(
                        new PushOptionsImpl(),
                        temporaryFolder.newFile("zanata.xml")))
                .withMessage("Failed applying project config");
    }

    @Test
    public void failIfProjectConfigContainsCommandHook() throws Exception {
        ZanataConfig zanataConfig = new ZanataConfig();
        LocaleList locales = new LocaleList();
        locales.add(new LocaleMapping("zh"));
        zanataConfig.setLocales(locales);
        CommandHook hook = new CommandHook();
        hook.setCommand("make");
        hook.getBefores().add("make prepare");
        zanataConfig.getHooks().add(hook);

        File configFile = temporaryFolder.newFile("zanata.xml");
        marshaller.marshal(zanataConfig, configFile);

        PushOptionsImpl options = new PushOptionsImpl();
        options.setBatchMode(true);
        assertThatExceptionOfType(ZanataSyncException.class)
                .isThrownBy(() -> {
                    PushPullOptionsUtil.applyProjectConfig(
                            options,
                            configFile);
                })
                .withMessage("Commandhook in zanata.xml is not supported");
    }

}
