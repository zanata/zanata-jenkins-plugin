### Assumptions
- A username password credential with id 'user_zanata' for a Zanata server
- A username password credential with id 'user_github' for your github repo
- [Credentials Binding Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin) is installed

### Sample Jenkinsfile
```groovy
node {
    def gitRepo = 'github.com/your_org/your-repo.git'
    def gitBranch = 'branch_name'
    
    stage('checkout') {
        git branch: gitBranch, changelog: false, credentialsId: 'user_github', poll: false, url: "https://$gitRepo"
    }

    stage('zanata sync') {
        // generated from Pipeline Syntax using general step
        step([$class: 'ZanataSyncStep', zanataCredentialsId: 'user_zanata', zanataLocaleIds: '', zanataProjectConfigs: '', zanataURL: ''])
    }

    stage('git push') {
        withCredentials(
            [
                [$class: 'UsernamePasswordMultiBinding', credentialsId: 'user_github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']
            ]) {

            // copy from https://github.com/jenkinsci/pipeline-examples/blob/master/pipeline-examples/push-git-repo/pushGitRepo.Groovy
            sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@' + gitRepo + ' ' + gitBranch)
        }
    }
}

```