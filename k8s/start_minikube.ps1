#
# start_minikube.ps1
#
# Don't forget to right-size minikube use of cpus and memory, e.g.,
# minikube config get memory
# minikube config set memory 16384

#
docker context use desktop-linux
minikube start --mount --mount-string="C:\Users\robma\dev\theApp:/theApp" --addons=dashboard --addons=metrics-server --addons=ingress --addons=ingress-dns --memory=16242 --cpus=8 --driver=docker
