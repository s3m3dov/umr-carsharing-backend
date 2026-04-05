# GCP Initial Setup Guide

This guide covers the first-time setup for Google Cloud Platform (GCP) to host the CarSharing backend using the provided GitOps tools.

## 1. GCP Account and Project Setup

1.  **Sign up for GCP Free Tier**:
    *   Visit [Google Cloud Console](https://console.cloud.google.com/).
    *   Complete the signup process. New accounts typically get $300 in free credits.
2.  **Create a New Project**:
    *   Click on the project selector at the top and select **New Project**.
    *   **Project Name**: `carsharing-backend` (or similar).
    *   **Project ID**: Note this down (e.g., `carsharing-345678`).

## 2. Enable Required APIs

Enable the necessary services using the gcloud CLI:

```bash
# Set your project ID
gcloud config set project [PROJECT_ID]

# Enable GKE, Container Registry, and Compute Engine APIs
gcloud services enable \
  container.googleapis.com \
  containerregistry.googleapis.com \
  compute.googleapis.com
```

## 3. Configure Google Container Registry (GCR)

The project uses **Google Container Registry (GCR)** with the endpoint **`eu.gcr.io`**.

1.  **Configure Docker Authentication**:
    ```bash
    gcloud auth configure-docker eu.gcr.io
    ```
2.  **Note**: GCR creates repositories automatically upon the first image push.

## 4. Create GKE Autopilot Cluster

Create the cluster with the fixed name **`autopilot-cluster-1`**.

```bash
gcloud container clusters create-auto autopilot-cluster-1 \
  --region=europe-north1 \
  --project=[PROJECT_ID]
```

*Note: Provisioning the cluster can take 5-10 minutes.*

## 5. Reserve a Global Static External IP

Reserve a global static IP address with the fixed name **`carsharing-ip`**. This is used by the Global Load Balancer (Ingress).

```bash
# Create the global static IP
gcloud compute addresses create carsharing-ip --global
```

**Get the IP address**:
```bash
gcloud compute addresses describe carsharing-ip --global --format="get(address)"
```

Update your `gitops/base/configmap.yaml` with this IP address in the `EXTERNAL_IP` field.

## 6. Configure Local Environment

Utilize the GitOps command to connect your local `kubectl` to the new cluster:

```bash
./gitops/gitops.sh gcp status
```
*Note: The first time you run any GCP command, it will trigger `./gitops/scripts/gcp/connect-gcp.sh` to configure your context.*

## 7. Update Project Configuration

### Update ConfigMap
Update [`gitops/base/configmap.yaml`](../../gitops/base/configmap.yaml) with your specific GCP details:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: carsharing-config
  namespace: carsharing
data:
  GCP_PROJECT_ID: "[PROJECT_ID]"
  GCP_REGION: "europe-north1"
  GKE_CLUSTER_NAME: "autopilot-cluster-1"
  EXTERNAL_IP: "[RESERVED_GLOBAL_IP]"
```

## 8. Authorizing Access (Network Security)

If you encounter timeout errors when using `kubectl`, authorize your current IP address:

1.  **Get your public IP**: `curl -s ifconfig.me`
2.  **Update Cluster**:
    ```bash
    gcloud container clusters update autopilot-cluster-1 \
      --region europe-north1 \
      --enable-master-authorized-networks \
      --master-authorized-networks [YOUR_IP]/32
    ```

## 9. Maintenance and Global Updates

If you change your **Project ID**, **External IP**, or **Cluster Name** at any point, you must ensure consistency across the entire repository.

**Important Note**: Use a global **Find and Replace** (e.g., `Cmd+Shift+F` in VS Code) to update these values across the following key files:
- `gitops/base/configmap.yaml`: Primary source of truth for GitOps scripts.
- `gitops/overlays/production/ingress-production.yaml`: Static IP name reference.
- `.docs/` directory: All technical guides and reference material.

Always verify that your local environment is synchronized after a change:
```bash
# Re-run the connection script to update your local context
./gitops/gitops.sh gcp status
```

## Next Steps

1.  **Create Secrets**: `./gitops/gitops.sh gcp secrets`
2.  **Build and Push**: `./gitops/gitops.sh gcp build`
3.  **Deploy to GKE**: `./gitops/gitops.sh gcp deploy`
