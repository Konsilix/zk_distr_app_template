#!/bin/bash

APP=theApp
docker context use default
#docker context use desktop-linux

##eval $(minikube docker-env)

IMAGE=${APP}
VERSION=v1
JAR_FILE=${APP}-0.0.1-SNAPSHOT.jar
MYAPP_DOCKER_PREFIX="^docker\_zkApp.*$"
cd ..
if [ -f ${JAR_FILE} ]; then
    echo "Removing existing jar file"
    rm ${JAR_FILE}
fi
if mvn clean package -DskipTests=true; # creates jar file in target directory
then
    echo "Build successful"
else
    echo "Build failed"
    cd -
    exit 1
fi
cd -
cp ../target/${JAR_FILE} .
# Remove all volumes and images with prefix
#for v in $(docker volume ls | awk '{print $2}' | egrep "${MYAPP_DOCKER_PREFIX}"); do docker volume rm $v; done
# Remove image
if docker image rm ${IMAGE}:${VERSION};
then
    echo "Removing image from docker"
else
    echo "No image on docker."
fi
if minikube image rm ${IMAGE}:${VERSION};
then
    echo "Removing image from minikube"
else
    echo "No image on minikube."
fi
# Build image
if minikube image build -t ${IMAGE}:${VERSION} .;
then
    echo "Successfully built image in minikube"
else
    echo "Unsuccessfully built image in minikube"
fi
if [ -f ${JAR_FILE} ]; then
    echo "Removing existing jar file"
    rm ${JAR_FILE}
fi
