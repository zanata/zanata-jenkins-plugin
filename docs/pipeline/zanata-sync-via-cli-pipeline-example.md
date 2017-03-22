### Assumptions
- Zanata CLI version 4.0.0 is [configured to be installed](/configuration/build-step/install-cli/#global-configuration) (it will generate a tool name 'zanata_cli_4_0_0').
- A username password credential with id 'user_zanata' for a Zanata server
- A username password credential with id 'user_github' for your github repo
- [Credentials Binding Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin) is installed

### Sample Jenkinsfile
```groovy
node {
    def gitRepo = 'github.com/your_org/your-repo.git'
    def gitBranch = 'branch-name'

    stage('Preparation') {
        git branch: gitBranch, changelog: false, credentialsId: 'user_github', poll: false, url: "https://$gitRepo"
    }

    stage('Pulling Translation From Zanata') {
        
        if (isUnix()) {
            // from Pipeline Syntax: select 'tool: Use a tool from a predefined Tool Installation' and then generate script
            tool name: 'zanata_cli_4_0_0',
                type: 'org.jenkinsci.plugins.zanata.zanatareposync.ZanataCLIInstall'
            withEnv(["CLI_HOME=${tool 'zanata_cli_4_0_0'}"]) {
                step(
                    [$class: 'ZanataCliBuilder', projFile: 'zanata.xml', syncG2zanata: true, syncZ2git: false, zanataCredentialsId: 'user_zanata', extraPathEntries: "$CLI_HOME/bin"])
            }

        } else {
            throw new RuntimeException("Windows node is not supportted")
        }
    }
    
    // stage to build the project is omitted

    stage('Pushing Zanata Translation to Git') {
        
        if (isUnix()) {
            // you don't need this if you have this configured globally
            sh 'git config user.name Jenkins ; git config user.email "jenkins@zanata.org"'
            step(
                [$class: 'ZanataCliBuilder', projFile: 'zanata.xml', syncG2zanata: false, syncZ2git: true, zanataCredentialsId: 'user_zanata', extraPathEntries: "$CLI_HOME/bin"]
            )
            // ==== below will run git push ====
            withCredentials(
                [[$class: 'UsernamePasswordMultiBinding', credentialsId: 'user_github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@' +
                    gitRepo + ' ' + gitBranch)
            }

        } else {
            throw new RuntimeException("Windows node is not supportted")
        }
    }

}

```
