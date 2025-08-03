# NOTES.md

# zk_distr_app_template
K8s-driven Zookeeper-based Distributed Java Application

This is the standard way to expose an application to the outside world in a production-like environment. You want Kubernetes to provide you with a single, stable IP address that balances traffic across your running chatbot pods.
The Kubernetes resource for this is a Service of type: LoadBalancer.
However, there is one critical concept to understand because you are using Minikube.
On a Real Cloud (like GKE, AWS, Azure): When you create a Service of type: LoadBalancer, the cloud provider automatically provisions a real, internet-facing load balancer and assigns it a public IP address.
On Minikube: Minikube runs locally. It doesn't have a real cloud load balancer to provision. Instead, it has a built-in tool called the minikube tunnel that simulates this behavior. You have to run this tool in a separate terminal to "activate" the load balancer and assign it an IP address that is accessible from your host machine.
Here is the complete step-by-step guide to achieve this.
Step 1: Modify Your deployment-chatbot.yaml
You need to change the type of your chatbot-loadbalancer service from NodePort to LoadBalancer.
Open k8s/deployment-chatbot.yaml.
Find the chatbot-loadbalancer service definition.
Change this:
Generated yaml
# --- Chatbot Load Balancer Service ---
apiVersion: v1
kind: Service
metadata:
name: chatbot-loadbalancer
spec:
type: NodePort # <--- CHANGE THIS
ports:
- name: http
  port: 8080
  targetPort: 8081
  selector:
  app: chatbot
  Use code with caution.
  Yaml
  To this:
  Generated yaml
# --- Chatbot Load Balancer Service ---
apiVersion: v1
kind: Service
metadata:
name: chatbot-loadbalancer
spec:
type: LoadBalancer # <--- TO THIS
ports:
- name: http
  port: 8080
  targetPort: 8081
  selector:
  app: chatbot
  Use code with caution.
  Yaml
  Step 2: Deploy Your Application
  As always, start with a clean slate to make sure the new service type is created correctly.
  Stop Skaffold: Ctrl+C.
  Run the Clean Deploy Cycle:
  Generated bash
  skaffold delete && skaffold dev
  Use code with caution.
  Bash
  Let Skaffold deploy everything. Your Zookeeper and Chatbot pods should all come up and be in a Running state.
  Step 3: Start the Minikube Tunnel
  This is the special step required for Minikube. You must run this in a new, separate terminal window.
  Open a new terminal.
  Run the following command:
  Generated bash
  minikube tunnel
  Use code with caution.
  Bash
  You will likely be prompted for your local user's password (e.g., [sudo] password for rob:). This is because the tunnel needs to modify your computer's network routes to expose the service.
  The command will then run continuously, showing output like this:
  Generated code
  Status:
  machine: minikube
  pid: 12345
  route: 10.96.0.0/12 -> 192.168.49.2
  services: [chatbot-loadbalancer]
  |-----------|----------------------|-------------|-------------------|
  | NAMESPACE |         NAME         | TARGET PORT |        URL        |
  |-----------|----------------------|-------------|-------------------|
  | default   | chatbot-loadbalancer |             | http://10.108.144.24:8080 |
  |-----------|----------------------|-------------|-------------------|
  Your terminal is now blocked...
**Leave this terminal window open!** If you close it, the tunnel stops, and the load balancer IP will no longer be accessible.
Use code with caution.
Step 4: Find Your Load Balancer's IP Address
Now that the tunnel is active, you can ask Kubernetes for the external IP address.
Go back to your original terminal (or open a third one).
Run this command:
Generated bash
kubectl get service chatbot-loadbalancer
Use code with caution.
Bash
Analyze the output. The EXTERNAL-IP column, which might have been <pending>, will now show the IP address of your Minikube node.
Generated code
NAME                   TYPE           CLUSTER-IP      EXTERNAL-IP    PORT(S)          AGE
chatbot-loadbalancer   LoadBalancer   10.108.144.24   192.168.49.2   8080:31567/TCP   5m
Use code with caution.
The EXTERNAL-IP is 192.168.49.2.
Step 5: Access Your Application
You can now access your application using the external IP and the service port (8080, not the NodePort).
Open your web browser and navigate to:
http://192.168.49.2:8080
(Replace 192.168.49.2 with whatever IP is shown in your EXTERNAL-IP column).
Your application's web page should now load successfully via its new, simulated external load balancer.

When you choose Option 1 (using type: LoadBalancer and minikube tunnel), you are only changing how you access the running service from the outside. You are not changing Skaffold's powerful inner development loop.
Here's Your Workflow with Option 1:
Initial Setup:
Terminal 1: You run skaffold dev. Skaffold will:
Build your chatbot image using Jib.
Deploy Zookeeper and the Chatbot using your YAML files.
Begin watching your Java source code files for changes.
Terminal 2: You run minikube tunnel. This activates the LoadBalancer service and gives it an external IP. You only need to do this once per session.
Access the App: You open your browser and go to http://<EXTERNAL-IP>:8080 to see the running application.
Making a Change (The Magic Happens Here):
You open your IDE (like IntelliJ or VS Code) and make a change to a .java file in your chatbot project.
You save the file.
Skaffold's Automated Response:
Skaffold detects the file change in Terminal 1. The console will light up with activity.
It will automatically trigger a new build of your chatbot image using Jib. This is usually very fast because Jib is intelligent about only rebuilding the layers that changed.
Once the new image is built, Skaffold will trigger a new deployment.
For a StatefulSet, this means Kubernetes will perform a rolling update. It will terminate the old chatbot-0 pod and replace it with a new one running your updated code. Then it will do the same for chatbot-1, and chatbot-2. You will see this happening in the Skaffold logs.
Testing the New Code:
Once the rolling update is complete (Skaffold's log will stabilize), you can simply refresh your browser at http://<EXTERNAL-IP>:8080.
The load balancer will now direct your request to one of the new pods, and you will be interacting with your updated Java code.
You get the best of both worlds:
A realistic, production-like network setup (LoadBalancer and minikube tunnel).
A fast, automated, "inner loop" development experience where code changes are built and redeployed for you instantly.
This is precisely the workflow that Skaffold is designed to enable.