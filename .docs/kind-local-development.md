# Local Development with Kind

This guide explains how to run the carsharing-backend microservices locally using **Kind** (Kubernetes in Docker).

## Prerequisites

- **Docker Desktop** (or Docker Engine)
- **Kind**: [Installation Guide](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
- **kubectl**: [Installation Guide](https://kubernetes.io/docs/tasks/tools/)
- **Java 21** and **Maven 3.6+** (for building)

## Quick Start

### 1. Create Kind Cluster

```bash
./scripts/local/setup-kind.sh
```

This script will:

- Create a 3-node Kind cluster (`carsharing-local`)
- Install NGINX Ingress Controller
- Create the `carsharing` namespace
- Configure the cluster for local development

### 5. Create secrets

   ```bash
   ./scripts/local/create-secrets.sh
   kubectl apply -f k8s/base/secrets.yaml
   ```

### 3. Build and Load Images

```bash
./scripts/local/build-for-kind.sh
```

This builds all Docker images and loads them directly into the Kind cluster.

### 4. Deploy Services

```bash
kubectl apply -k k8s/overlays/local/
```

### 5. Verify Deployment

```bash
# Check pods
kubectl get pods -n carsharing

# Check services
kubectl get svc -n carsharing

# Check ingress
kubectl get ingress -n carsharing

# View logs
kubectl logs -f deployment/api-gateway -n carsharing
```

### 6. Access the Application

The API Gateway is accessible at:

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **API Endpoints**: <http://localhost:8080/api/>*

## Development Workflow

### Making Changes

1. **Update code** in your service
2. **Rebuild the image**:

   ```bash
   ./scripts/build-for-kind.sh
   ```

3. **Restart the deployment**:

   ```bash
   kubectl rollout restart deployment/SERVICE_NAME -n carsharing
   ```

### Viewing Logs

```bash
# All pods
kubectl logs -f -l app.kubernetes.io/name=carsharing -n carsharing

# Specific service
kubectl logs -f deployment/auth-service -n carsharing

# Follow logs with stern (if installed)
stern -n carsharing .
```

### Debugging

```bash
# Get pod details
kubectl describe pod POD_NAME -n carsharing

# Execute commands in a pod
kubectl exec -it POD_NAME -n carsharing -- /bin/sh

# Port forward to a specific service
kubectl port-forward svc/auth-service 8089:8089 -n carsharing
```

## Configuration

### Application Configuration

All services use a **single `application.yml`** file with Kubernetes-native configuration:

- **Service Discovery**: Kubernetes DNS (no Eureka)
- **Service URLs**: `http://service-name:port`
- **No Spring profiles needed** - same config for local and production

Example service-to-service call:

```java
// Works in both Kind (local) and GKE (production)
String url = "http://user-service:8081/api/users/" + userId;
```

### Environment Variables

Set in `k8s/configmap.yaml`:

- `ALLOWED_ORIGINS=*`
- Service ports

Secrets are loaded from `k8s/secrets.yaml`.

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n carsharing

# View pod events
kubectl describe pod POD_NAME -n carsharing

# Check logs
kubectl logs POD_NAME -n carsharing
```

### Image Pull Errors

Make sure images are loaded into Kind:

```bash
# List images in Kind
docker exec -it carsharing-local-control-plane crictl images

# Rebuild and reload
./scripts/build-for-kind.sh
```

### Ingress Not Working

```bash
# Check ingress controller
kubectl get pods -n ingress-nginx

# Check ingress resource
kubectl describe ingress carsharing-ingress -n carsharing

# Verify port mapping
docker ps | grep carsharing-local
```

### Service Communication Issues

```bash
# Test service connectivity from within a pod
kubectl run test-pod --image=curlimages/curl -it --rm -n carsharing -- sh

# Inside the pod:
curl http://auth-service:8089/actuator/health
curl http://user-service:8081/actuator/health
```

## Cleanup

### Delete Cluster

```bash
kind delete cluster --name carsharing-local
```

### Remove Images

```bash
docker rmi $(docker images | grep 'auth-service\|user-service\|vehicle-service\|trip-service\|ride-service\|notification-service\|api-gateway' | awk '{print $3}')
```

## Tips

1. **Use kubectl aliases**:

   ```bash
   alias k=kubectl
   alias kgp='kubectl get pods -n carsharing'
   alias kgs='kubectl get svc -n carsharing'
   alias kl='kubectl logs -f -n carsharing'
   ```

2. **Install k9s** for a better terminal UI:

   ```bash
   brew install k9s  # macOS
   k9s -n carsharing
   ```

3. **Use kubectx/kubens** for easier context switching:

   ```bash
   brew install kubectx
   kubens carsharing  # Set default namespace
   ```

## Differences from Production

| Aspect | Local (Kind) | Production (GKE) |
| ------ | ------------ | ---------------- |
| **Ingress** | NGINX | Google Cloud Load Balancer |
| **Images** | Local (`service:local`) | GCR (`gcr.io/PROJECT_ID/service:tag`) |
| **Image Pull** | Never (pre-loaded) | Always (from registry) |
| **Autoscaling** | Manual | HPA enabled |
| **SSL/TLS** | None | Managed certificates |

## Next Steps

- [Production Deployment Guide](kubernetes-deployment.md)
- [CI/CD Pipeline](ci-cd-guide.md)
- [Architecture Overview](architecture.md)
