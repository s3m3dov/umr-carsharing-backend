# GCP Deployment Guide

## Overview

This guide covers deploying the CarSharing backend to Google Cloud Platform (GCP) using GKE Autopilot in the `europe-north1` region.

## Prerequisites

### Required Tools

- **gcloud CLI**: [Install Guide](https://cloud.google.com/sdk/docs/install)
- **kubectl**: Installed with gcloud or [standalone](https://kubernetes.io/docs/tasks/tools/)
- **Docker**: [Install Guide](https://docs.docker.com/get-docker/)

### GCP Resources

- **GKE Cluster**: `autopilot-carsharing-cluster-01` (already created)
- **Project ID**: `project-b05bd45b-70e8-4b7f-a65`
- **Region**: `europe-north1`
- **External IP**: `34.160.91.182`

## Configuration

All GCP settings are centralized in [`gitops/base/configmap.yaml`](file:///Users/hsamadov/Projects/_umr/umr-carsharing/carsharing-backend/gitops/base/configmap.yaml):

```yaml
GCP_PROJECT_ID: "project-b05bd45b-70e8-4b7f-a65"
GCP_REGION: "europe-north1"
GKE_CLUSTER_NAME: "autopilot-carsharing-cluster-01"
EXTERNAL_IP: "34.160.91.182"
```

## MongoDB Atlas Setup

### 1. Network Access Configuration

Get your GKE cluster's IP range:

```bash
gcloud container clusters describe autopilot-carsharing-cluster-01 \
  --region=europe-north1 \
  --format="get(clusterIpv4Cidr)"
```

### 2. Add to MongoDB Atlas

1. Go to MongoDB Atlas → Network Access
2. Click "Add IP Address"
3. Add the cluster IP range from step 1
4. Or use `0.0.0.0/0` for testing (not recommended for production)

### 3. Get Connection String

1. Go to MongoDB Atlas → Database → Connect
2. Choose "Connect your application"
3. Copy the connection string
4. Replace `<password>` with your actual password

## Deployment Steps

### 1. Configure kubectl

```bash
./gitops.sh setup-gcp
```

This will:

- Set your GCP project
- Verify the cluster exists
- Configure kubectl credentials
- Install required auth plugins

### 2. Create Secrets

Create `gitops/base/secrets.yaml` from the template:

```bash
cp gitops/base/secrets.yaml.example gitops/base/secrets.yaml
```

Encode your secrets:

```bash
# MongoDB URI
echo -n "mongodb+srv://user:pass@cluster.mongodb.net/carsharingDB" | base64

# JWT Secret
echo -n "your-jwt-secret-key" | base64

# Google API Key (for trip-service)
echo -n "your-google-maps-api-key" | base64
```

Edit `gitops/base/secrets.yaml` and add the base64-encoded values, then apply:

```bash
kubectl apply -f gitops/base/secrets.yaml
```

### 3. Build and Push Images

Build all services:

```bash
./gitops.sh build-gcp
```

Or build specific services:

```bash
./gitops.sh build-gcp auth-service user-service
```

This will:

- Read project ID from ConfigMap
- Build Docker images
- Push to Google Container Registry (GCR)
- Display build summary

### 4. Deploy to GKE

Deploy all services:

```bash
./gitops.sh deploy-gcp
```

Or restart specific services:

```bash
./gitops.sh deploy-gcp auth-service trip-service
```

### 5. Verify Deployment

Check pod status:

```bash
kubectl get pods -n carsharing
```

Check ingress:

```bash
kubectl get ingress -n carsharing
```

View logs:

```bash
kubectl logs -n carsharing -l app=auth-service --tail=50
```

### 6. Access the Application

- **Swagger UI**: <http://34.160.91.182/swagger-ui.html>
- **Health Check**: <http://34.160.91.182/actuator/health>
- **API Gateway**: <http://34.160.91.182>

## Selective Deployment

### Build Specific Services

```bash
# Build only auth and user services
./gitops.sh build-gcp auth-service user-service

# Build with custom tag
export GCP_IMAGE_TAG=v1.0.0
./gitops.sh build-gcp
```

### Deploy Specific Services

```bash
# Restart only trip service
./gitops.sh deploy-gcp trip-service

# Restart multiple services
./gitops.sh deploy-gcp auth-service user-service vehicle-service
```

## Resource Configuration

### Replica Counts

- **Initial replicas**: 1 per service
- **HPA min replicas**: 1
- **HPA max replicas**: 3-5 (varies by service)

Autopilot will automatically scale based on:

- CPU utilization > 70%
- Memory utilization > 80%

### Resource Limits

Per pod:

- **Requests**: 100m CPU, 256Mi memory
- **Limits**: 500m CPU, 512Mi memory

## Troubleshooting

### Pods Not Starting

Check events:

```bash
kubectl describe pod <pod-name> -n carsharing
```

Common issues:

- **ImagePullBackOff**: Image not in GCR or wrong project ID
- **CrashLoopBackOff**: Check logs for application errors
- **Pending**: Autopilot provisioning nodes (wait 2-3 minutes)

### MongoDB Connection Issues

1. Verify network access in MongoDB Atlas
2. Check secret is correctly base64-encoded:

   ```bash
   kubectl get secret carsharing-secrets -n carsharing -o yaml
   ```

3. Decode to verify:

   ```bash
   echo "<base64-value>" | base64 -d
   ```

### Ingress Not Accessible

Check ingress status:

```bash
kubectl describe ingress carsharing-ingress -n carsharing
```

Wait 5-10 minutes for GCE load balancer provisioning.

### Image Build Failures

Ensure Docker is authenticated:

```bash
gcloud auth configure-docker gcr.io
```

Check Docker daemon is running:

```bash
docker ps
```

## Useful Commands

### View All Resources

```bash
kubectl get all -n carsharing
```

### Scale Deployment Manually

```bash
kubectl scale deployment auth-service --replicas=2 -n carsharing
```

### Update Image

```bash
kubectl set image deployment/auth-service \
  auth-service=gcr.io/project-b05bd45b-70e8-4b7f-a65/auth-service:v1.0.0 \
  -n carsharing
```

### Port Forward for Debugging

```bash
kubectl port-forward svc/auth-service 8089:8080 -n carsharing
```

### View ConfigMap

```bash
kubectl get configmap carsharing-config -n carsharing -o yaml
```

## CI/CD Integration

For automated deployments, set these environment variables in your CI/CD pipeline:

```bash
export GCP_PROJECT_ID=project-b05bd45b-70e8-4b7f-a65
export GCP_REGION=europe-north1
export GKE_CLUSTER_NAME=autopilot-carsharing-cluster-01
export GCP_IMAGE_TAG=$CI_COMMIT_SHA  # or version tag
```

Then run:

```bash
./gitops.sh setup-gcp
./gitops.sh build-gcp
./gitops.sh deploy-gcp
```

## Cost Optimization

### Autopilot Pricing

- Charged per pod resource requests
- No node management fees
- Automatic bin-packing for efficiency

### Reduce Costs

1. **Lower resource requests** if services use less:

   ```yaml
   resources:
     requests:
       cpu: 50m      # Down from 100m
       memory: 128Mi # Down from 256Mi
   ```

2. **Reduce max replicas** in HPA if traffic is low

3. **Use committed use discounts** for predictable workloads

## Next Steps

1. **Set up monitoring**: Configure Cloud Monitoring for GKE
2. **Add alerts**: Set up alerting for pod failures and high resource usage
3. **Configure logging**: Enable Cloud Logging for centralized logs
4. **Add domain**: Configure custom domain with Cloud DNS
5. **Enable HTTPS**: Add SSL certificate for production traffic
