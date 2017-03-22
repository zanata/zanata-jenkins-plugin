### Prerequisite

- Same [Prerequisite](/configuration/#prerequisite) applies (make sure you give your credentials a meaningful ID) 
- Install [Credentials Binding Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin).

### Next step

Both [Zanata Sync build step](/configuration/build-step/zanata-sync/) and
 [Zanata Sync via CLI build step](/configuration/build-step/zanata-sync-via-cli/)
 can be used as pipeline 'General Build Step'
 [![Pipeline Syntax: General build step](/images/select_pipeline_general_build_step.png)](/images/select_pipeline_general_build_step.png)
 
_Hint_: Use Pipeline Syntax and choose 'step: General Build Step', then select the build step you want to use, fill in all details then generate.

##### Complete sample pipeline scripts

- [Use Zanata Sync build step](/pipeline/zanata-sync-pipeline-example/)
- [Install and use Zanata CLI](/pipeline/install-use-cli-pipeline-example/)
- [Use Zanata Sync via CLI build step](/pipeline/zanata-sync-via-cli-pipeline-example/)