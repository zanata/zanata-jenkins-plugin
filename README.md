### A Jenkins plugin to synchronize localization resources between SCM repository and Zanata
 
[Zanata](https://zanata.org) is an open source translation management platform. 

Once you install this jenkins plugin, you can set up a job to perform typical localization workflow:
- check out from SCM
- push source (and/or translation) to Zanata server for translators to work on
- pull translation from Zanata server
- commit translation into SCM (currently only git is supported automatically. Other SCM will need to use scripting)
- push the commit to remote SCM (using git publisher or equivalent)

### How to build and test locally
In this project, if you have docker installed:

```
mvn clean package
docker/runJenkins.sh
```

If you don't have docker:
```
hpiPluginRun.sh
```

### How to use it as normal Jenkins job
First you will need to configure Zanata credentials in Jenkins Credentials view (plus e.g. github credential if you want to push commit).
Then you will have two options to use Zanata client to push source to and/or pull translation from Zanata server.
1. install Zanata CLI on Jenkins node and use scripting to invoke it
    - go to Jenkins global tools configuration view and there will be a Zanata CLI section for you to configure
    - in your job configuration, you can choose to install the configured CLI under Build Environment section
    - choose a shell builder step and run Zanata CLI from there
2. use the plugin as a build step
    - in you job configuration, you can choose 'Zanata Sync' as a build step and fill in all the information in the UI
    
Option 1 has the advantage of being flexible. You can use all the features and options [Zanata CLI](http://zanata-client.readthedocs.io/en/release/).
The disadvantage is, you will need to know how to use Zanata CLI. You also need to manually manage commit in your shell script.

Option 2 has the advantage of being installation free and simple to use. It will work on all type of nodes.
It will commit translation after pull automatically if you use Git as SCM. 
Disadvantage being the java classes it used is from a fixed version of Zanata CLI and you can't do much customization for push and pull.

### How to use it in pipeline build

#### Use standard push and pull
```groovy
node {
    // define common variables
    def gitRepo = 'github.com/huangp/test-repo.git'
    def gitBranch = 'trans'
    def zanataCredentialsId = 'zanata'
    def gitCredentialsId = 'huangp_github'
    
    
    git([url: "https://$gitRepo", branch: gitBranch])
    
    // generated from Pipeline Syntax using general step
    step([$class: 'ZanataBuilder', pullFromZanata: true, pushToZanata: true, zanataCredentialsId: zanataCredentialsId, zanataProjectConfigs: '', zanataLocaleIds: ''])
    
    // copy from https://github.com/jenkinsci/pipeline-examples/blob/master/pipeline-examples/push-git-repo/pushGitRepo.Groovy
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: gitCredentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
        sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@' + ' ' + gitRepo)
    }
}

```

#### Install tool and run in shell 
Assuming a Zanata CLI version 4.0.0 is pre-configured (it will generate a tool name 'zanata_cli_4_0_0').
```groovy
node {
    // from Pipeline Syntax: select 'tool: Use a tool from a predefined Tool Installation' and then generate script
    tool name: 'zanata_cli_4_0_0', type: 'org.jenkinsci.plugins.zanata.zanatareposync.ZanataCLIInstall'
    
    withEnv(["CLI_HOME=${ tool 'zanata_cli_4_0_0' }"]) {
         sh '$CLI_HOME/bin/zanata-cli help'
    }
    
}

```

### Alternative build step (experimental)

Yu Shao yu.shao.gm@gmail.com

Find your zanata credential information according to:
http://docs.zanata.org/projects/zanata-client/en/release/configuration/

- Configure your build by selecting 'Zanata Sync via CLI' to use a pre-entered script template

Configure the sync details, there are two sync actions in the plugin, one, pushing the English source file(s) to Zanata server defined in zanata.xml file in the source repo property directories.  Two, committing the finished translation files from Zanata to the original Git repository. 

Both two types of sync are running the default shell scripts, you could customize the shell scripts, you could customize the two sync actions according to your build need by enabling or disabling them
