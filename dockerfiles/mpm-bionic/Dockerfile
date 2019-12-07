# MousePaw Media Build Environment (Bionic)
# Version: 1.2.0
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

FROM ubuntu:bionic

LABEL MAINTAINER="MousePaw Media <developers@mousepawmedia.com>"

RUN export DEBIAN_FRONTEND=noninteractive

# Core Environment
RUN apt-get update && \
    apt-get install -y --no-install-recommends lsb-release wget gnupg2 \
    apt-utils tzdata && \
    ln -fs /usr/share/zoneinfo/America/

# Development Core
RUN apt-get install -y --no-install-recommends arcanist autoconf automake \
    build-essential checkinstall cmake git libtool php php-curl php-cli \
    php-xml

# C/C++ Core
RUN apt-get install -y --no-install-recommends cccc cppcheck valgrind

# LLVM [C/C++]
RUN \
    echo "deb http://apt.llvm.org/`lsb_release -sc`/ llvm-toolchain-`lsb_release -sc` main" > /etc/apt/sources.list.d/llvm.list && \
    wget -O - http://apt.llvm.org/llvm-snapshot.gpg.key | apt-key add - && \
    apt-get update && \
    apt-get install -y --no-install-recommends python-lldb-7 && \
    apt-get install -y --no-install-recommends libllvm7 llvm-7 llvm-7-dev \
    llvm-7-runtime clang-7 clang-tools-7 libclang-common-7-dev libclang-7-dev \
    libclang1-7 libfuzzer-7-dev lldb-7 lld-7 libc++-7-dev libc++abi-7-dev \
    libomp-7-dev && \
    ln -sf /usr/bin/llvm-symbolizer-7 /usr/bin/llvm-symbolizer && \
    ln -sf /usr/bin/lldb-server-7 /usr/lib/llvm-7/bin/lldb-server-7.0.0 && \
    update-alternatives --install /usr/bin/cc cc /usr/bin/clang-7 30 && \
    update-alternatives --install /usr/bin/c++ c++ /usr/bin/clang++-7 30

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

# Python 3.8.0 manual install
RUN apt-get install -y --no-install-recommends libreadline-gplv2-dev \
    libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev \
    libbz2-dev libffi-dev zlib1g-dev && \
    cd /tmp && \
    wget https://www.python.org/ftp/python/3.8.0/Python-3.8.0.tgz && \
    tar xzf Python-3.8.0.tgz && \
    cd Python-3.8.0 && \
    ./configure --enable-optimizations && \
    make altinstall

# Install system-wide dependencies.
RUN apt-get install -y --no-install-recommends libcairo2-dev libsdl2-dev

# Clean up
RUN rm -rf /var/lib/apt/lists/*
