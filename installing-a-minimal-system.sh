#!/bin/bash

# Specify your docker credentials in the variables.

#If you get the following error ...
#ROVIDER_DOCKER_NEWGRP: "docker version --format <no value>-<no value>:<no value>" exit status 1: permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock: Get "http://%2Fvar%2Frun%2Fdocker.sock/v1.50/version": dial unix /var/run/docker.sock: connect: permission denied
#
#... the following command may help:
#sudo usermod -aG docker $USER && newgrp docker

dockeruser=

dockerpassword=

minikube start -p MK-minimalsystem --memory 12000 --cpus 4 --disk-size 60g --driver=docker

minikube profile MK-minimalsystem

clusterip=$( minikube ip )

echo "the cluster ip is $clusterip"

git clone https://github.com/OPTIMALSYSTEMS/yuuvis-api-helm-charts.git

cd yuuvis-api-helm-charts/

git pull

git checkout 2025autumn

cd infrastructure/

helm dep up

cd ../..

kubectl create namespace infrastructure

kubectl create namespace yuuvis

cd yuuvis-api-helm-charts

helm install infrastructure ./infrastructure/ --namespace infrastructure \
--set yuuvis.authentication.ip=$clusterip \
--set imageCredentials.dockeryuuvisorg.username=$dockeruser \
--set imageCredentials.dockeryuuvisorg.password=$dockerpassword

sleep 600

kubectl get po -n infrastructure

helm install yuuvis ./yuuvis --namespace yuuvis \
--set imageCredentials.yuuvisorg.username=$dockeruser \
--set imageCredentials.yuuvisorg.password=$dockerpassword \
--set yuuvis.keycloak.ip=$clusterip \
--set yuuvis.keycloak.publicaddress=$clusterip:30111/auth \
--set yuuvis.init.authurl=http://$clusterip:30080 \
--set yuuvis.init.services.authentication.nodeport.use=true

watch kubectl get po -n yuuvis



#To test your installation, for example, call the URL http://123.456.78.9:30080/api/dms/info in a Web Browser. You will be redirected to the Keycloak log-in page. Enter your user credentials for the initial user. If you kept the default configuration during the installation, you can log in with username root and password changeme.
