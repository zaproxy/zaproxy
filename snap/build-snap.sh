export DOCKER=snapcore/snapcraft:stable
docker pull $DOCKER
docker run -it -v "$PWD:$PWD" -w "$PWD" $DOCKER snapcraft
