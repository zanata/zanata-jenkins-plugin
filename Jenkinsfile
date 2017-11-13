#!/usr/bin/env groovy

boolean onJenkinsCI = env.JENKINS_URL &&
    (env.JENKINS_URL == 'https://ci.jenkins.io/' ||
        env.JENKINS_URL == 'https://ci.jenkins.io')

if (onJenkinsCI) {
  /* running on ci.jenkins.io, we will have `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
  buildPlugin()
  return
}

@Field
public static final String PROJ_URL = 'https://github.com/zanata/zanata-jenkins-plugin'

// Import pipeline library for utility methods & classes:
// ansicolor(), Notifier, PullRequests, Strings
@Field
public static final String PIPELINE_LIBRARY_BRANCH = 'ZNTA-2270-jenkins2'

@Field
public static final String testReports = 'target/surefire-reports/TEST-*.xml'


// GROOVY-3278:
//   Using referenced String constant as value of Annotation causes compile error
@Library('zanata-pipeline-library@ZNTA-2270-jenkins2')
import org.zanata.jenkins.Notifier
import org.zanata.jenkins.PullRequests
import org.zanata.jenkins.ScmGit

import groovy.transform.Field

PullRequests.ensureJobDescription(env, manager, steps)

@Field
def pipelineLibraryScmGit

@Field
def mainScmGit

@Field
def notify
// initialiser must be run separately (bindings not available during compilation phase)

/* Only keep the 10 most recent builds. */
def projectProperties = [
  [
    $class: 'GithubProjectProperty',
    projectUrlStr: PROJ_URL
  ],
  [
    $class: 'BuildDiscarderProperty',
    strategy: [$class: 'LogRotator', numToKeepStr: '10']
  ],
]

properties(projectProperties)

/* Upto stash stage should fail fast:
 * Failed and stop the build
 * Yet able to create report
 */
timestamps {
  node {
    try {
      echo "running on node ${env.NODE_NAME}"
      pipelineLibraryScmGit = new ScmGit(env, steps, 'https://github.com/zanata/zanata-pipeline-library')
      pipelineLibraryScmGit.init(PIPELINE_LIBRARY_BRANCH)
      mainScmGit = new ScmGit(env, steps, PROJ_URL)
      mainScmGit.init(env.BRANCH_NAME)
      notify = new Notifier(env, steps, currentBuild,
          pipelineLibraryScmGit, mainScmGit, 'Jenkinsfile',
      )

      ansicolor {
        stage('Checkout') {
          notify.started()

          // Shallow Clone does not work with RHEL7, which use git-1.8.3
          // https://issues.jenkins-ci.org/browse/JENKINS-37229
          checkout scm
        }

        // Build and Unit Tests
        // The result is archived
        stage('Build') {
          notify.startBuilding()
          def hpiFiles = 'target/*.hpi'

          withEnv(["MVN_HOME=${ tool name: 'mvn', type: 'hudson.tasks.Maven$MavenInstallation' }"]) {
            sh '$MVN_HOME/bin/mvn clean verify'
          }

          junit allowEmptyResults: true,
            keepLongStdio: false,
            testDataPublishers: [[$class: 'StabilityTestDataPublisher']],
            testResults: "**/${testReports}"

          // notify if compile+unit test successful
          notify.testResults(null)
          archive "**/${hpiFiles},**/target/site/jacoco/**"
        }

        stage('Report') {
          // this is not working properly yet
          // step([$class: 'JacocoPublisher'])
        }
      }
    } catch (e) {
      notify.failed()
        junit allowEmptyResults: true,
        keepLongStdio: false,
        testDataPublishers: [[$class: 'StabilityTestDataPublisher']],
        testResults: "**/${testReports}"
        throw e
    }
  }
}
