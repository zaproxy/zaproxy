This directory contains all of the files required to build a ZAP 2.7.0 https://snapcraft.io/ snap.

Docker is used to build the snap as snapcraft is only supported on a limited number of linux distros.

In order to build the snap run:

`./build-snap.sh`

To install the snap you've built locally run:

`snap install zaproxy_2.7.0_amd64.snap --dangerous --classic`

Note that the architecture component '_amd64' may be different on your system.

You should then be able to run the ZAP snap using:

`zaproxy`

In order to publish the snap you will need appropriate permissions.
Those people who have then can upload the snap using:

`snapcraft login`
`snapcraft push zaproxy_2.7.0_amd64.snap`
