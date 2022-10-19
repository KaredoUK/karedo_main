FROM ubuntu:14.04.2
RUN sudo apt-get update
WORKDIR /
RUN mkdir -p $HOME/local/bin
RUN mkdir -p $HOME/local/lib
ENV PATH="$HOME/local/bin:$PATH"
ENV LD_LIBRARY_PATH="$HOME/local/lib:$LD_LIBRARY_PATH"
ENV PKG_CONFIG_PATH="$HOME/local/lib/pkgconfig/:$HOME/local/lib/pkg-config/"
RUN sudo apt-get install -y linux-tools-generic libbz2-dev python-dev scons libtool liblzma-dev libblas-dev make automake ccache ant openjdk-7-jdk libcppunit-dev doxygen libcrypto++-dev libACE-dev gfortran liblapack-dev libevent-dev libssh2-1-dev libicu-dev libv8-dev g++ google-perftools libgoogle-perftools-dev zlib1g-dev git pkg-config valgrind autoconf
RUN git clone https://github.com/rtbkit/rtbkit-deps.git
WORKDIR /rtbkit-deps
RUN git submodule update --init
RUN make all NODEJS_ENABLED=0
WORKDIR /
RUN git clone https://github.com/rtbkit/rtbkit.git
WORKDIR /rtbkit
RUN git checkout ubuntu14
RUN cp jml-build/sample.local.mk local.mk
RUN make compile NODEJS_ENABLED=0
