### Configure Zanata Sync as a Build Step

Under 'Build' section, select 'Zanata Sync' as a build step 
<figure>
![Select 'Zanata Sync' as a build step](/images/select_build_step.png)
</figure>

Once selected, you should see its configuration:
<figure>
![Zanata Sync build step configuration](/images/zanata_sync_build_step.png)
</figure>

Here you can:
- Choose the Zanata credential for your Zanata Server
- Choose to push source to Zanata and/or pull translation from Zanata
- Click on 'Advanced Options' to reveal more options

If you choose pull translation from Zanata, it will commit any changes if SCM is git.
If you are not using git, you will need to configure a step yourself to do it.

If you have translation commits in your build, you may also want to [push 
that back to the remote SCM repo](/configuration/post-build.md).
