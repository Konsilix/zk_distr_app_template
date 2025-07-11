# K8s README.md

Steps to run using docker - creates 3 nodes
1. `cluster_build_image.sh`
2. `minikube start --driver=docker`
3. `kubectl apply -f ./app_on_k8s.yaml`
4. `minikube tunnel` and let this run, then
5. Hit the output url in browser (`http://localhost:3000`) to see the app URL
6. Voila! You have the app running on k8s

