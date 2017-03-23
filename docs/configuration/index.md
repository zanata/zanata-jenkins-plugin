### Prerequisite

##### On the Zanata server you use

- Register a Zanata account and [have a generated API key](http://docs.zanata.org/en/release/user-guide/account/account-settings/#client)
- [Create the project](http://docs.zanata.org/en/release/user-guide/projects/create-project/) and [version](http://docs.zanata.org/en/release/user-guide/versions/create-version/) to use (configure your locales, permissions etc)
- [Download the Zanata project version config](http://docs.zanata.org/en/release/client/configuration/), customize it if needed and check it into your SCM

##### Global Configuration in your Jenkins server

- Configure Zanata credentials (username and API key) in Jenkins Credentials view 
- Configure Github credential or equivalent remote SCM credentials if you want
 to push commit
- JAVA_HOME defined as environment variable (You should install JDKs and then expose one's installation path as environment variable. This way the JAVA_HOME variable is correct both in master and in slave)

### Next step

##### Configure a job to listen to Zanata webhook event

In your job configuration, check the *Accept Zanata Webhook* checkbox under 
'General' section. It will open up more fields:
- The 'URL to register on Zanata' field is readonly for you to register your
webhook in Zanata. See [Zanata webhook](http://docs.zanata.org/en/release/user-guide/projects/project-settings/#adding-a-new-webhook) for more detail.
- The optional webhook secret field is for verification of incoming webhooks
<figure>
![Accept Zanata Webhook](/images/zanata_webhook.png)
</figure>

Configure your SCM for your Jenkins job as usual.
If you want to trigger a build when SCM changes, you can leverage other 
Jenkins plugins.
e.g. Github plugin to listen to github webhook event and trigger a build to 
push resources to Zanata.

Then in the 'Build' section, you will have two options to push source to and/or pull translation from Zanata server.

1. [Use the plugin as a build step](/configuration/build-step/zanata-sync)    
2. [Install Zanata CLI on Jenkins node and use scripting to invoke it](/configuration/build-step/install-cli)

Option 1 has the advantage of being installation free and simple to use. It will work on all type of jenkins slave nodes.
It will commit translation after pull automatically if you use Git as SCM. 
Disadvantage being that it uses only the included version of Zanata CLI java classes. and you can't do much customization for push and pull.

Option 2 has the advantage of being flexible. You can use all the features and options of [Zanata CLI](http://docs.zanata.org/en/release/client/).
The disadvantage is, you will need to know how to use Zanata CLI. You also need to manually manage source control in your shell script.
