/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jenkinsci.plugins.zanata.cli.service.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.jenkinsci.plugins.zanata.cli.HasSyncJobDetail;
import org.jenkinsci.plugins.zanata.cli.service.PullService;
import org.jenkinsci.plugins.zanata.cli.service.PushService;
import org.jenkinsci.plugins.zanata.cli.service.ZanataSyncService;
import org.jenkinsci.plugins.zanata.cli.util.PushPullOptionsUtil;
import org.jenkinsci.plugins.zanata.exception.ZanataSyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataSyncServiceImpl implements ZanataSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(ZanataSyncServiceImpl.class);
    private static final long serialVersionUID = 1L;

    private final PushService pushService;
    private final PullService pullService;
    private final Set<String> projectConfigs;
    private final String zanataUrl;
    private final String username;
    private final String apiKey;
    private final String localeId;
    private final String pushToZanataOption;

    @VisibleForTesting
    protected ZanataSyncServiceImpl(PullService pullService,
            PushService pushService, HasSyncJobDetail jobDetail) {
        this.pullService = pullService;
        this.pushService = pushService;
        zanataUrl = jobDetail.getZanataURL();
        String syncToZanataOption = jobDetail.getSyncOption();
        pushToZanataOption = Strings.emptyToNull(syncToZanataOption);
        username = jobDetail.getZanataUsername();
        apiKey = jobDetail.getZanataSecret();
        projectConfigs = getProjectConfigs(jobDetail.getZanataProjectConfigs());

        localeId = jobDetail.getZanataLocaleIds();


        // if project id is given from webhook, only handle this project
        // String projectId = jobDetail.getProject();
        // if (!Strings.isNullOrEmpty(projectId)) {
        // pullOptions.setProj(projectId);
        // pushOptions.setProj(projectId);
        // }
    }

    public ZanataSyncServiceImpl(HasSyncJobDetail jobDetail) {
        this(new PullServiceImpl(), new PushServiceImpl(), jobDetail);
    }

    private PushOptionsImpl newPushOptionsFromJobConfig() {
        PushOptionsImpl pushOptions = new PushOptionsImpl();
        pushOptions.setInteractiveMode(false);
        pushOptions.setUsername(username);
        pushOptions.setKey(apiKey);
        pushOptions.setPushType(pushToZanataOption);
        // if localeId is given, only handle this locale
        if (!Strings.isNullOrEmpty(localeId)) {
            pushOptions.setLocales(localeId);
        }
        overrideURLIfSpecified(pushOptions, zanataUrl);
        //        this.pushOptions.setLogHttp(true);
        return pushOptions;
    }

    private PullOptionsImpl newPullOptionsFromJobConfig() {
        PullOptionsImpl pullOptions = new PullOptionsImpl();
        pullOptions.setInteractiveMode(false);
        pullOptions.setUsername(username);
        pullOptions.setKey(apiKey);
        // if localeId is given, only handle this locale
        if (!Strings.isNullOrEmpty(localeId)) {
            pullOptions.setLocales(localeId);
        }
        overrideURLIfSpecified(pullOptions, zanataUrl);
        // TODO https://zanata.atlassian.net/browse/ZNTA-1427 is fixed, we should set cacheDir to workspace root
        pullOptions.setUseCache(false);
        //        this.pullOptions.setLogHttp(true);
        return pullOptions;
    }

    private static Set<String> getProjectConfigs(String projectConfigs) {
        if (Strings.isNullOrEmpty(projectConfigs)) {
            return Collections.emptySet();
        }
        return ImmutableSet
                .copyOf(Splitter.on(",").trimResults().omitEmptyStrings()
                        .split(projectConfigs));
    }

    @Override
    public void pushToZanata(Path repoBase) throws ZanataSyncException {
        if (projectConfigs.isEmpty()) {
            Set<File> projectConfigs = findProjectConfigsOrThrow(repoBase);
            for (File config : projectConfigs) {
                PushOptionsImpl opts = newPushOptionsFromJobConfig();
                String project = opts.getProj();

                PushPullOptionsUtil
                        .applyProjectConfig(opts, config);
                pushIfProjectIdMatchesConfig(opts, project, config);
            }
        } else {
            for (String projectConfig : projectConfigs) {
                Path absPath = Paths.get(repoBase.toString(), projectConfig);
                if (Files.exists(absPath)) {
                    PushOptionsImpl opts = newPushOptionsFromJobConfig();
                    String project = opts.getProj();

                    PushPullOptionsUtil.applyProjectConfig(
                            opts, absPath.toFile());
                    pushIfProjectIdMatchesConfig(opts, project, absPath.toFile());
                } else {
                    log.warn("{} does not exist! Ignored!", projectConfig);
                }
            }
        }
    }

    private void pushIfProjectIdMatchesConfig(PushOptions opts, String project,
            File config) {
        if (Strings.isNullOrEmpty(project) || Objects.equals(opts.getProj(), project)) {
            pushService.pushToZanata(opts);
        } else if (!Strings.isNullOrEmpty(project)) {
            log.warn(
                    "project id is provided as {}. Skip {} which has project set to {}",
                    config, opts.getProj());
        }
    }

    private static void overrideURLIfSpecified(PushPullOptions opts,
            String zanataUrl) {
        if (!Strings.isNullOrEmpty(zanataUrl)) {
            try {
                opts.setUrl(new URL(zanataUrl));
            } catch (MalformedURLException e) {
                log.warn("{} is malformed", zanataUrl);
                throw new IllegalArgumentException(zanataUrl + " is malformed");
            }
        }
    }

    private Set<File> findProjectConfigsOrThrow(Path repoBase) {
        Set<File> projectConfigs =
                PushPullOptionsUtil.findProjectConfigs(repoBase.toFile());

        log.info("found {} in {}", projectConfigs, repoBase);
        if (projectConfigs.isEmpty()) {
            throw new ZanataSyncException(
                    "can not find project config (zanata.xml) in the repo");
        }
        return projectConfigs;
    }

    @Override
    public void pullFromZanata(Path repoBase) throws ZanataSyncException {
        if (projectConfigs.isEmpty()) {
            Set<File> projectConfigs =
                    findProjectConfigsOrThrow(repoBase);
            for (File config : projectConfigs) {
                PullOptionsImpl opts = newPullOptionsFromJobConfig();
                String project = opts.getProj();

                PushPullOptionsUtil
                        .applyProjectConfig(opts, config);
                pullIfProjectIdMatchesConfig(opts, project, config);
            }
        } else {
            for (String projectConfig : projectConfigs) {
                Path absPath = Paths.get(repoBase.toString(), projectConfig);
                if (Files.exists(absPath)) {
                    PullOptionsImpl opts = newPullOptionsFromJobConfig();
                    String project = opts.getProj();

                    opts = PushPullOptionsUtil.applyProjectConfig(opts,
                            absPath.toFile());
                    pullIfProjectIdMatchesConfig(opts, project, absPath.toFile());
                } else {
                    log.warn("{} does not exist! Ignored!", projectConfig);
                }
            }
        }
    }

    private void pullIfProjectIdMatchesConfig(PullOptions opts,
            String project, File config) {
        if (Strings.isNullOrEmpty(project) || Objects.equals(opts.getProj(), project)) {
            pullService.pullFromZanata(opts);
        } else if (!Strings.isNullOrEmpty(project)) {
            log.warn(
                    "project id is provided as {}. Skip {} which has project set to {}",
                    config, opts.getProj());
        }
    }
}
