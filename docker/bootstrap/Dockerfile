FROM openjdk:8-stretch
MAINTAINER daemonsthere@gmail.com

ENV LEIN_ROOT 1
ENV LEIN_VERSION="2.8.3"

# Get dependencies
RUN apt-get update &&\
	apt-get install -y \
		curl \
		git

# Get lein 2.7.1
ADD https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein /usr/local/bin/lein
RUN chmod +x /usr/local/bin/lein &&\
	lein
