# MousePaw Media Build Environment (Bionic)
# Version: 1.3.0
#
# Author(s): Gerar Almonte, Jason C. McDonald

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

FROM ubuntu:focal

LABEL MAINTAINER="MousePaw Media <developers@mousepawmedia.com>"

RUN export DEBIAN_FRONTEND=noninteractive

# Core Environment
RUN apt-get update && \
    apt-get install -y --no-install-recommends lsb-release wget gnupg2 \
    apt-utils tzdata software-properties-common apt-transport-https ca-certificates && \
    ln -fs /usr/share/zoneinfo/America/

# Development Core
RUN apt-get install -y --no-install-recommends autoconf automake \
    build-essential checkinstall cmake git libtool php php-curl \
    php-cli php-xml

# C/C++ Core
RUN apt-get install -y --no-install-recommends cccc cppcheck valgrind

# Don't warn about apt key
ARG APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1

# LLVM/Clang 12 [C/C++]
RUN \
    echo "deb http://apt.llvm.org/`lsb_release -sc`/ llvm-toolchain-`lsb_release -sc`-12 main" > /etc/apt/sources.list.d/llvm.list && \
    wget -O - http://apt.llvm.org/llvm-snapshot.gpg.key | apt-key add - && \
    apt-get update && \
    apt-get install -y --no-install-recommends libllvm-12-ocaml-dev libllvm12 \
    llvm-12 llvm-12-dev llvm-12-doc llvm-12-examples llvm-12-runtime clang-12 \
    clang-tools-12 clang-12-doc libclang-common-12-dev libclang-12-dev \
    libclang1-12 clang-format-12 clangd-12 libfuzzer-12-dev \
    lldb-12 lld-12 libc++-12-dev libc++abi-12-dev libomp-12-dev && \
    ln -sf /usr/bin/llvm-symbolizer-12 /usr/bin/llvm-symbolizer && \
    update-alternatives --install /usr/bin/clang clang /usr/bin/clang-12 20 && \
    update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-12 20 && \
    update-alternatives --install /usr/bin/cc cc /usr/bin/clang 30 && \
    update-alternatives --install /usr/bin/c++ c++ /usr/bin/clang++ 30

# GCC 9 [C/C++]
RUN apt-get install -y --no-install-recommends gcc-9 g++-9 gcc-9-multilib \
    g++-9-multilib libc6-dev-i386 && \
    update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-9 20 && \
    update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-9 20 && \
    update-alternatives --install /usr/bin/cc cc /usr/bin/gcc 20 && \
    update-alternatives --install /usr/bin/c++ c++ /usr/bin/g++ 20

# Python
RUN apt-get install -y --no-install-recommends python3 python3-pip \
    python3-virtualenv pylint3 python-dev python3-dev virtualenv

# Sphinx
RUN apt-get install -y --no-install-recommends python3-sphinx pandoc

# Arcanist
RUN mkdir -p /usr/share/.arcanist && \
    cd /usr/share/.arcanist && \
    git clone https://github.com/phorgeit/arcanist.git && \
    ln -s /usr/share/.arcanist/arcanist/bin/arc /usr/bin/arc

# Arcanist Clang Format Linter.
RUN cd /usr/share/.arcanist && \
    git clone https://github.com/vhbit/clang-format-linter.git

# Docker
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl gnupg lsb-release && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y --no-install-recommends docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Python 3.8.8 manual install
# RUN cp /etc/apt/sources.list /etc/apt/sources.list~ && \
#     sed -Ei 's/^# deb-src /deb-src /' /etc/apt/sources.list && \
#     apt-get update && \
#     apt-get build-dep -y python3 && \
#     cd /tmp && \
#     wget https://www.python.org/ftp/python/3.8.8/Python-3.8.8.tgz && \
#     tar xzf Python-3.8.8.tgz && \
#     cd Python-3.8.8 && \
#     ./configure --enable-optimizations && \
#     make altinstall

# Install system-wide dependencies.
RUN apt-get install -y --no-install-recommends libcairo2-dev libsdl2-dev libsdl2-mixer-dev

# Install artifact publishing tools
RUN apt-get install -y --no-install-recommends sudo rsync openssh-client

# NOTE: Integrate this: https://github.com/Strider-CD/strider-docker-slave/issues/5

# Clean up
RUN rm -rf /var/lib/apt/lists/*

# Configure user (888 for uid/gid is also used by Jenkins; be consistent.)
RUN groupadd -g 888 mpm && \
    useradd -mr -u 888 -g mpm mpm && \
    echo "mpm  ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/mpm

# Switch to user
USER mpm
