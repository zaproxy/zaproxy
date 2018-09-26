export DOCKER=snapcore/snapcraft:latest
docker pull $DOCKER
docker run -it -v "$PWD:$PWD" -w "$PWD" $DOCKER snapcraft
