# README.md

Instructions on how to build while running on a k8s cluster:

First and foremost, you need to create `application.yaml` in `src/main/resources`
Copy `src/main/resources/application.yml.template` to `src/main/resources/application.yaml` and
fill in the pertinent configuration data.

In one terminal:
1. `eval $(minikube -p minikube docker-env)`
2. `kubectl delete pods --all && skaffold delete && skaffold dev`

In another terminal:
1. `eval $(minikube -p minikube docker-env)`
2. `minikube tunnel --cleanup=true`
3. `kubectl get service chatbot-loadbalancer` to check if you have the external IP

You should see the following:
```bash
kubectl get service chatbot-loadbalancer
NAME                   TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
chatbot-loadbalancer   LoadBalancer   10.99.56.153   127.0.0.1     8080:31784/TCP   88s
```

In your browser:
1. Based upon that information directly above, open up the following URL `http://127.0.0.1:8080`
2. You should see the following from the app:
   ![running chatbot](./images/running_chatbot.png)