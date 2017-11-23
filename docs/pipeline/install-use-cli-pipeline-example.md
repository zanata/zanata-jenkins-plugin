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
    
    stage('checkout') {
        git branch: gitBranch, changelog: false, credentialsId: 'user_github', poll: false, url: "https://$gitRepo"
    }

    stage('zanata sync') {
        // from Pipeline Syntax: select 'tool: Use a tool from a predefined Tool Installation' and then generate script
        tool name: 'zanata_cli_4_0_0',
            type: 'org.jenkinsci.plugins.zanata.zanatareposync.ZanataCLIInstall'

        withCredentials(
            [
                [$class: 'UsernamePasswordMultiBinding', credentialsId: 'user_zanata', usernameVariable: 'Z_USERNAME', passwordVariable: 'Z_PASSWORD']
            ]) {
            
            withEnv(["CLI_HOME=${tool 'zanata_cli_4_0_0'}"]) {
                // ideally this script should go into a file in your repo and here you just invoke it
                sh('''
Z=$CLI_HOME/bin/zanata-cli

$Z -B push --file-types "PLAIN_TEXT[adoc]" --username $Z_USERNAME --key $Z_PASSWORD

$Z -B pull --username $Z_USERNAME --key $Z_PASSWORD

rm -rf .zanata-cache/

git add .
git config user.name "Jenkins"
git config user.email "jenkins-noreply@redhat.com"
git commit -m "$BUILD_URL did this" 

''')
            }

        }
    }

    stage('git push') {
        withCredentials(
            [
                [$class: 'UsernamePasswordMultiBinding', credentialsId: 'user_github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']
            ]) {

            sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@' + gitRepo + ' ' + gitBranch)
        }
    }

}

```

__NOTE__: The shell command in the middle is too long. 
Ideally you should store that in a file and check it into your SCM.
Then just invoke it from your pipeline script.