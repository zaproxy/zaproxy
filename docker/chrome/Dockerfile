FROM --platform=linux/amd64 zaproxy/zap-nightly:latest

USER root

RUN apt-get update && \
	wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
	apt-get install -y ./google-chrome-stable_current_amd64.deb && \
	rm -rf /var/lib/apt/lists/*

USER zap