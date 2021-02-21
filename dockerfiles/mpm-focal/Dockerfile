# MousePaw Media Build Environment (Bionic)
# Version: 1.2.1
#
# Author(s): Jason C. McDonald

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
    apt-utils tzdata software-properties-common && \
    ln -fs /usr/share/zoneinfo/America/

# Development Core
RUN apt-get install -y --no-install-recommends arcanist autoconf automake \
    build-essential ca-certificates checkinstall cmake git libtool php php-curl \
    php-cli php-xml

# C/C++ Core
RUN apt-get install -y --no-install-recommends cccc cppcheck valgrind

# Don't warn about apt key
ARG APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=1

# LLVM [C/C++]
RUN \
    echo "deb http://apt.llvm.org/`lsb_release -sc`/ llvm-toolchain-`lsb_release -sc`-9 main" > /etc/apt/sources.list.d/llvm.list && \
    wget -O - http://apt.llvm.org/llvm-snapshot.gpg.key | apt-key add - && \
    apt-get update && \
    apt-get install -y --no-install-recommends python-lldb-9 && \
    apt-get install -y --no-install-recommends libllvm9 llvm-9 llvm-9-dev \
    llvm-9-runtime clang-9 clang-tools-9 libclang-common-9-dev libclang-9-dev \
    libclang1-9 libfuzzer-9-dev lldb-9 lld-9 libc++-9-dev libc++abi-9-dev \
    libomp-9-dev && \
    ln -sf /usr/bin/llvm-symbolizer-9 /usr/bin/llvm-symbolizer && \
    update-alternatives --install /usr/bin/cc cc /usr/bin/clang-9 30 && \
    update-alternatives --install /usr/bin/c++ c++ /usr/bin/clang++-9 30

# GCC [C/C++]
RUN apt-get install -y --no-install-recommends gcc g++ gcc-multilib \
    g++-multilib libc6-dev-i386 && \
    update-alternatives --install /usr/bin/cc cc /usr/bin/gcc 10 && \
    update-alternatives --install /usr/bin/c++ c++ /usr/bin/g++ 10

# Python
RUN apt-get install -y --no-install-recommends python3 python3-pip \
    python3-virtualenv pylint3 python-dev python3-dev virtualenv

# Sphinx
RUN apt-get install -y --no-install-recommends python3-sphinx pandoc

# Python 3.8.8 manual install
RUN apt-get install -y --no-install-recommends libreadline-gplv2-dev \
    libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev \
    libbz2-dev libffi-dev zlib1g-dev && \
    cd /tmp && \
    wget https://www.python.org/ftp/python/3.8.8/Python-3.8.8.tgz && \
    tar xzf Python-3.8.8.tgz && \
    cd Python-3.8.8 && \
    ./configure --enable-optimizations && \
    make altinstall

# Install system-wide dependencies.
RUN apt-get install -y --no-install-recommends libcairo2-dev libsdl2-dev

# Clean up
RUN rm -rf /var/lib/apt/lists/*
