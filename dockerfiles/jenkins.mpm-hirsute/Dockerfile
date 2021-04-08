# Jenkins Slave: MousePaw Media Build Environment (Bionic)
# Version: 1.2.1
#
# Converts any Ubuntu-based image (FROM ...) into a Jenkins slave.
#
# Author(s): Jason C. McDonald
#
# Assistance from Gavin Mogan <halkeye>

# Based on https://hub.docker.com/r/jenkins/slave/dockerfile
#      and https://hub.docker.com/r/jenkins/jnlp-slave/dockerfile
# Copyright (c) 2015-2019, CloudBees, Inc. and other Jenkins contributors
# Licensed under MIT License

# LICENSE (BSD-3-Clause)
# Copyright (c) 2019 MousePaw Media.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
# this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# 3. Neither the name of the copyright holder nor the names of its contributors
# may be used to endorse or promote products derived from this software without
# specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
# THE POSSIBILITY OF SUCH DAMAGE.
#
# CONTRIBUTING
# See https://www.mousepawmedia.com/developers for information
# on how to contribute to our projects.

FROM registry.mousepawmedia.net/mpm-hirsute:latest

LABEL MAINTAINER="MousePaw Media <developers@mousepawmedia.com>"

# Install Java
RUN apt-get update \
    && apt-get install --no-install-recommends -y openjdk-8-jre-headless

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

# Download Jenkins client binaries and scripts
RUN apt-get install -y --no-install-recommends git-lfs curl

ARG VERSION=3.40
ARG AGENT_VERISON=3.40-1

RUN curl --create-dirs -fsSLo /usr/share/jenkins/agent.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${VERSION}/remoting-${VERSION}.jar \
  && chmod 755 /usr/share/jenkins \
  && chmod 644 /usr/share/jenkins/agent.jar \
  && ln -sf /usr/share/jenkins/agent.jar /usr/share/jenkins/slave.jar

RUN wget https://raw.githubusercontent.com/jenkinsci/docker-jnlp-slave/${AGENT_VERISON}/jenkins-agent && \
    chmod +x jenkins-agent && \
    mv jenkins-agent /usr/local/bin/jenkins-agent && \
    ln -s /usr/local/bin/jenkins-agent /usr/local/bin/jenkins-slave

# Clean up
RUN rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["/usr/local/bin/jenkins-agent"]
