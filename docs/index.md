### A Jenkins plugin to synchronize localization resources between SCM repository and Zanata

[Plugin home page](https://wiki.jenkins-ci.org/display/JENKINS/Zanata+Plugin)
 
[Zanata](https://zanata.org) is an open source translation management platform. 

Once you install this jenkins plugin, you can set up a job to perform typical localization workflow:

- check out from SCM
- push source (and/or translation) to Zanata server for translators to work on
- pull translation from Zanata server
- commit translation into SCM (currently only git is supported automatically. Other SCM will need to use scripting)
- push the commit to remote SCM (using git publisher or equivalent)