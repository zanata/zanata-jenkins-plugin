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
package org.jenkinsci.plugins.zanata.git;

import java.io.Serializable;
import java.nio.file.Path;

import org.jenkinsci.plugins.zanata.exception.RepoSyncException;

public interface RepoSyncService extends Serializable {

    /**
     *
     * @param path
     *            the workspace path
     * @return true if the translation is commited to the SCM repo
     * @throws RepoSyncException
     */
    boolean syncTranslationToRepo(Path path) throws RepoSyncException;

    default String commitAuthorName() {
        return "Zanata Sync";
    }

    default String commitAuthorEmail() {
        return "zanata-devel@redhat.com";
    }

    default String commitMessage(String zanataUsername) {
        return String.format("Zanata Sync job triggered by %s", zanataUsername);
    }

}
