#!/usr/bin/env bash

# Don't forget to right-size minikube use of cpus and memory, e.g.,
# minikube config get memory
# minikube config set memory 16384


#docker context use desktop-linux && \
docker context use default && \
minikube start --driver=docker --addons=dashboard --addons=metrics-server --addons=ingress --addons=ingress-dns

# then run minikube dashboard
