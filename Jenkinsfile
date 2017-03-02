#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

/* Only keep the 10 most recent builds. */
def projectProperties = [
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
try {
  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          info.printNode()
          notify.started()

          // Shallow Clone does not work with RHEL7, which use git-1.8.3
          // https://issues.jenkins-ci.org/browse/JENKINS-37229
          checkout scm
        }

        // Build and Unit Tests
        // The result is archived
        stage('Build') {
          info.printNode()
          info.printEnv()
          def testReports = 'target/surefire-reports/TEST-*.xml'
          def jarFiles = 'target/*.jar'
          def hpiFiles = 'target/*.hpi'

          withEnv(["MVN_HOME=${ tool name: 'mvn', type: 'hudson.tasks.Maven$MavenInstallation' }"]) {
            sh '$MVN_HOME/bin/mvn clean verify'
          }

          junit allowEmptyResults: true,
            keepLongStdio: true,
            testDataPublishers: [[$class: 'StabilityTestDataPublisher']],
            testResults: "**/${testReports}"

          // notify if compile+unit test successful
          notify.testResults(null)
          archive "**/${jarFiles},**/${hpiFiles}"
        }

        stage('stash') {
          stash name: 'workspace', includes: '**'
        }
      }
    }
  }
} catch (e) {
  notify.failed()
  junit allowEmptyResults: true,
      keepLongStdio: true,
      testDataPublishers: [[$class: 'StabilityTestDataPublisher']],
      testResults: "**/${testReports}"
  throw e
}

void archiveTestFilesIfUnstable() {
  // if tests have failed currentBuild.result will be 'UNSTABLE'
  if (currentBuild.result != null) {
    archive(
        includes: '*/target/**/*.log,**/target/site/jacoco/**')
  }
}