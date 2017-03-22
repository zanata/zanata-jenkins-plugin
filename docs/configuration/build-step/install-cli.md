_NOTE:_ You will need to install [Credentials Binding Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin) to use this option.

### Zanata CLI Installation

##### Global Configuration
- Go to 'Manage Jenkins'
- Go to 'Global Tool Configuration' (For Jenkins 1.6.x, tools configuration is under 'Configure System')
- Scroll down until you find 'Zanata CLI', then click on 'Zanata CLI installations'
- Click 'Add Zanata CLI' to open up an installer
    - Check 'Install automatically' if applicable
    - Input a version for the Zanata CLI you want to install. The version will become part of the tool name.
    - Change the 'Download URL for binary archive' and 'Subdirectory of extracted archive' according to the version you want to install (__note__: Subdirectory must match what's in the zip/tar.gz. You should only need to change the version number in the template)
- Repeat previous step if you want to install multiple version of Zanata CLI
- Click 'Save'
<figure>
![Global Zanata CLI Installation Configuration](/images/cli_installation.png)
</figure>

##### Individual Job Configuration
Under 'Build Environment' section:

- Check 'Install Zanata CLI' then in its opened configuration
    - Click 'Add Tool'
    - Select the CLI from the dropdown in 'Zanata CLI selection'
    - Optionally check 'Convert #CLIName_HOME variables to the upper-case'
- Check 'Use secret text(s) or file(s)' then
    - Select 'Username and password (separated)' from the 'Add' dropdown
    - Input a username variable name for Zanata username
    - Input a password variable name for Zanata API key
    - Select the Zanata credential
<figure>
![Install Zanata CLI for a job](/images/job_installs_cli.png)
</figure>

### Use the installed Zanata CLI in build

You can use it in two ways:

1. Add a build step such as 'Execute shell' and invoke Zanata CLI in it

For example assuming you follow above configuration and chose 'zanata_cli_4_0_0' and checked convert to uppercase,
Below shell script will do zanata push and pull and then git commit
```bash
Z=$ZANATA_CLI_4_0_0_HOME/bin/zanata-cli

$Z -B push --file-types "PLAIN_TEXT[adoc]" --username $Z_U --key $Z_P

$Z -B pull --username $Z_U --key $Z_P

# only needed if .zanata-cache/ is not in your .gitignore
rm -rf .zanata-cache/

git add .
# only needed if you haven't configure this globally in your Jenkins
git config user.name "Jenkins"
git config user.email "jenkins-noreply@redhat.com"
git commit -m "$BUILD_URL did this"

```
2. Choose another build step [Zanata Sync via CLI](/configuration/zanata-sync-via-cli.md)

__NOTE__: You may still want to do normal git push in a [post build step](/configuration/post-build.md)