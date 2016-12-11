#!/bin/bash

# Install curl if needed
if [[ ! -e /usr/bin/curl ]]; then
  sudo apt-get -yqq install curl
fi

# Download containerd, if it isn't already present on the system
if [[ ! -e /usr/local/bin/containerd ]]; then

  # Download containerd from GitHub
  curl -kLO https://github.com/docker/containerd/releases/download/0.0.5/containerd

  # Move the downloaded binary to /usr/local/bin
  sudo mv containerd /usr/local/bin/containerd
  sudo chmod a+x /usr/local/bin/containerd
fi

# Download containerd-shim, if it isn't already present on the system
if [[ ! -e /usr/local/bin/containerd-shim ]]; then

  # Download containerd-shim from GitHub
  curl -kLO https://github.com/docker/containerd/releases/download/0.0.5/containerd-shim

  # Move the downloaded binary to /usr/local/bin
  sudo mv containerd-shim /usr/local/bin/containerd-shim
  sudo chmod a+x /usr/local/bin/containerd-shim
fi

# Download ctr, if it isn't already present on the system
if [[ ! -e /usr/local/bin/ctr ]]; then

  # Download ctr from GitHub
  curl -kLO https://github.com/docker/containerd/releases/download/0.0.5/ctr

  # Move the downloaded binary to /usr/local/bin
  sudo mv ctr /usr/local/bin/ctr
  sudo chmod a+x /usr/local/bin/ctr
fi

# Download runc, if it isn't already present on the system
if [[ ! -e /usr/local/bin/runc ]]; then

  # Download runc from GitHub
  curl -kLO https://github.com/docker/containerd/releases/download/0.0.5/runc

  # Move the downloaded binary to /usr/local/bin
  sudo mv runc /usr/local/bin/runc
  sudo chmod a+x /usr/local/bin/runc
fi
