# Zanata Plugin for Jenkins

Yu Shao yu.shao.gm@gmail.com

This plugin adds the zanata configuration into Jenkins.

You need to install zanata client on the Jenkins server:
http://docs.zanata.org/projects/zanata-client/en/release/

- zanata.ini configuration

Find your zanata.ini information according to:
http://docs.zanata.org/projects/zanata-client/en/release/configuration/
Put the zanata.ini informtaion into Jenkins -> Manage Jenkins -> Configure System

- Configure your build to include Zanata sync

Configure Zanata Sync details, there are two sync actions in the plugin, one, pushing the English property file(s) to Zanata server defined in zanata.xml file in the source repo property directories.  Two, committing the finished Java property files from Zanata to the original Git repository. 

Both two types of sync are running the default shell scripts, you could customize the shell scripts, you could customize the two sync actions according to your build need by enabling or disabling them
