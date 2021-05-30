FROM ubuntu:20.04
RUN apt-get update && apt-get upgrade -y && apt-get install sudo
RUN useradd -ms /bin/bash me
RUN usermod -aG sudo me
RUN sed -i 's/^\(%sudo.*\)\(ALL\)$/\1NOPASSWD:\2/g' /etc/sudoers
USER me
