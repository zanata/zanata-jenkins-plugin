### Alternative build step (experimental and subject to change without notice)

From Yu Shao yu.shao.gm@gmail.com

##### Global configuration

- Go to 'Manage Jenkins'
- Go to 'Configure System'
- Scroll down to find 'Zanata cli script template'
    
There are two fields under 'Zanata cli script template', each of them contains a script template to use in your job.

##### Things to note

The default script templates expect:

- zanata username to be provided in an environment variable 'ZANATA_USERNAME', 
and zanata API key provided in environment variable 'ZANATA_APIKEY'.
- zanata-cli is on PATH

See [CLI installation](/configuration/build-step/install-cli/) for how to install CLI and how to bind Zanata credentials to environment variables.
 
##### Job configuration 

- Configure your build by selecting 'Zanata Sync via CLI' to use the pre-entered script template
- Select Zanata credentials from the dropdown and it will automatically expose ZANATA_USERNAME and ZANATA_APIKEY to environment variables
- Fill in 'Extra PATH entries' with the installed Zanata CLI location (e.g. *cli_name*_HOME/bin)

Configure the sync details, there are two sync actions in the plugin, one, pushing the English source file(s) to Zanata server defined in zanata.xml file in the source repo property directories.  Two, committing the finished translation files from Zanata to the original Git repository. 

Both two types of sync are running the default shell scripts, you could customize the shell scripts, you could customize the two sync actions according to your build need by enabling or disabling them


Example configuration:
<figure>
[![Example configuration](/images/zanata_sync_via_cli_step.png)](/images/zanata_sync_via_cli_step.png)
</figure>
