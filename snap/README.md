This directory contains all of the files required to build a ZAP [snap].

In order to manually build the snap run `snapcraft` in the current directory.

To install the snap you've built locally run:

`snap install zaproxy_<version>_amd64.snap --dangerous --classic`

Where `<version>` is the version of the snap, defined in [snapcraft.yaml].
Note that the architecture component `_amd64` may be different on your system.

You should then be able to run the ZAP snap using:

`zaproxy`

In order to manually publish the snap you will need appropriate permissions.
Those people who have then can upload the snap using:

```
snapcraft login
snapcraft upload zaproxy_<version>_amd64.snap
```

[snap]: https://snapcraft.io/
[snapcraft.yaml]: snapcraft.yaml