# Releasing

## Main Release

The following steps should be followed to release the main version:
 1. Run the workflow [Prepare Release Main Version](https://github.com/zaproxy/zaproxy/actions/workflows/prepare-release-main-version.yml),
    to prepare the release. It creates a pull request updating the version and indicating the remaining release tasks;
 2. Finish the tasks indicated in the pull request;
 3. Merge the pull request, to create the tag and the draft release (done by
    [Release Main Version](https://github.com/zaproxy/zaproxy/actions/workflows/release-main-version.yml));
 4. Verify the draft release;
 5. Publish the release.

Once published the [Handle Release](https://github.com/zaproxy/zaproxy/actions/workflows/handle-release.yml) workflow
will trigger the update of the marketplace with the new release, it will also create a pull request preparing the next
development iteration.

## Weekly Release

The following steps should be followed to release the weekly:
 1. Run the workflow [Release Weekly](https://github.com/zaproxy/zaproxy/actions/workflows/release-weekly.yml),
    to create the tag and the draft release;
 2. Verify the draft release;
 3. Publish the release.

Once published the [Handle Release](https://github.com/zaproxy/zaproxy/actions/workflows/handle-release.yml) workflow
will trigger the update of the marketplace with the new release.

## Docker Images

The image `owasp/zap2docker-live` is automatically built from the default branch.  

The images `owasp/zap2docker-weekly`, `owasp/zap2docker-stable`, and `owasp/zap2docker-bare` are automatically built
after the corresponding release to the marketplace.  
The images `owasp/zap2docker-stable` and `owasp/zap2docker-bare` are built at the same time.

They can still be manually built by running the corresponding workflow:
 - [Release Weekly Docker](https://github.com/zaproxy/zaproxy/actions/workflows/release-weekly-docker.yml)
 - [Release Main Docker](https://github.com/zaproxy/zaproxy/actions/workflows/release-main-docker.yml)

