# Running the Car Sharing Microservices

This guide explains how to set up and run the carsharing-backend microservices.

> [!IMPORTANT]
> Before you begin, please ensure you have read the [Architecture Overview](architecture.md) and completed the [Environment Setup](environment-setup.md).

## Prerequisites

- **Java**: Java 21+
- **Maven**: 3.6+
- **Docker**: Desktop or Engine
- **Kind**: [Installation Guide](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
- **kubectl**: [Installation Guide](https://kubernetes.io/docs/tasks/tools/)

---

## Option 1: Local Kubernetes (Kind) - Recommended 🚀

Running with Kind (Kubernetes in Docker) provides the closest environment to production and is the best way to test the full system.

### 1. Cluster Infrastructure Setup

First, create the cluster and install the necessary ingress controllers:

```bash
./scripts/local/setup-kind.sh
```

This script sets up a 3-node cluster named `carsharing-local`, installs NGINX Ingress, and creates the `carsharing` namespace.

### 2. Secrets & Configuration

Apply the required secrets and configurations:

```bash
./scripts/local/create-secrets.sh
kubectl apply -f k8s/base/secrets.yaml
```

### 3. Build & Load Application Images

Build all Docker images and load them directly into the Kind cluster:

```bash
./scripts/local/build-for-kind.sh
```

### 4. Deploy to Kubernetes

Deploy the services using the local overlay:

```bash
kubectl apply -k k8s/overlays/local/
```

### 5. Accessing the Application

- **API Gateway (Swagger)**: <http://localhost:8080/swagger-ui.html>
- **Health Checks**: <http://localhost:8080/actuator/health>

---

## Option 2: Individual Services (IDE/Terminal)

Useful for focused development on a single service.

1. **Start MongoDB**: Ensure you have a MongoDB instance running (local or Atlas).
2. **Configure Environment**: Export the variables defined in `environment-setup.md`.
3. **Run Service**: Navigate to the service directory and run:

```bash
mvn spring-boot:run
```

Typical ports (standardized to 8080 in K8s, but vary in local IDE mode if specified):

- API Gateway: 8080
- Other services: 8080 (multi-service local run may require port overrides)

---

## 🛠️ Kubernetes Monitoring & Operations

Use these commands to monitor and manage your local cluster:

### Checking Status

```bash
# Get all pods and their status
kubectl get pods -n carsharing

# Watch pods in real-time
kubectl get pods -n carsharing -w

# Get wide output (including IP and Node)
kubectl get pods -n carsharing -o wide

# Check service and ingress status
kubectl get svc,ingress -n carsharing
```

### Detailed Inspection

```bash
# Describe a pod (check for errors, pulled images, events)
kubectl describe pod <pod-name> -n carsharing

# Check events for the whole namespace
kubectl get events -n carsharing --sort-by='{.lastTimestamp}'

# View all resources in the namespace
kubectl get all -n carsharing
```

### Monitoring Logs

```bash
# Stream logs for a specific pod
kubectl logs -f <pod-name> -n carsharing

# Stream logs for all pods in a deployment
kubectl logs -f deployment/api-gateway -n carsharing

# Stream logs for all carsharing resources (requires Stern)
stern -n carsharing .
```

### Restarting & Debugging

```bash
# Restart all pods in a deployment (pulls new images)
kubectl rollout restart deployment <deployment-name> -n carsharing

# Check deployment status
kubectl rollout status deployment <deployment-name> -n carsharing

# Port forward to bypass ingress for testing
kubectl port-forward svc/<service-name> 8080:8080 -n carsharing

# Execute commands inside a pod
kubectl exec -it <pod-name> -n carsharing -- /bin/sh
```

---

## Troubleshooting

### Pods stuck in `ErrImagePull` or `ImagePullBackOff`

Ensure you have loaded the images into Kind:
`./scripts/local/build-for-kind.sh`

### Ingress not reachable

Verify the NGINX ingress controller is running:
`kubectl get pods -n ingress-nginx`

### Database connection issues

Check logs for `MongoTimeoutException`. Ensure your IP is whitelisted in MongoDB Atlas or that your local MongoDB is accessible.
