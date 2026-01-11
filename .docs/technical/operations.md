# 🛠️ Operations & Setup Guide

This guide covers everything you need to set up, run, and manage the Car Sharing microservices.

---

## 1. Environment Setup

This project uses a `.env` file to manage sensitive configuration.

### Quick Start

1. **Create your `.env` file** from the example:

   ```bash
   cp .env.example .env
   ```

2. **Update the MongoDB URI**: Get your connection string from [MongoDB Atlas](https://cloud.mongodb.com/) and replace the placeholder.
3. **Configure other variables** (JWT Secret, Google Maps API Key, etc.) as detailed below.

### Required Variables

| Variable | Description | Source |
| :--- | :--- | :--- |
| `MONGODB_URI` | MongoDB Connection String | MongoDB Atlas |
| `JWT_SECRET_KEY` | Secret for JWT (Base64) | Secure random string |
| `GOOGLE_API_KEY` | Maps/Directions API Key | Google Cloud Console |

> [!NOTE]
> CORS is managed at the **Kubernetes Ingress level**. The `ALLOWED_ORIGINS` variable is no longer used by the application.

---

## 2. Running with Local Kubernetes (Kind) 🚀

This is the recommended way to run the full system.

### Step 1: Cluster Setup

Initialize the cluster and ingress controller:

```bash
./gitops/gitops.sh setup
```

### Step 2: Secrets & Configuration

Generate and apply Kubernetes secrets from your `.env` file:

```bash
./gitops/gitops.sh secrets
```

### Step 3: Build & Deploy

Build Docker images and apply the local manifests:

```bash
./gitops/gitops.sh build
./gitops/gitops.sh apply local
```

### Access Points

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Checks**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 3. GitOps Workflow

We follow a "Manual GitOps" pattern where Git is the source of truth for the cluster state.

### Rules

1. **Never** use `kubectl edit` or ad-hoc patches.
2. **Always** commit changes to the `gitops/` directory first.
3. **Always** use `gitops/gitops.sh` to apply changes.

### Key Commands

| Action | Command |
| :--- | :--- |
| **Apply Changes** | `./gitops/gitops.sh apply [local\|production]` |
| **Preview Changes** | `./gitops/gitops.sh diff local` |
| **Check Status** | `./gitops/gitops.sh status` |
| **Dashboard** | `./gitops/gitops.sh dashboard` |
| **Swagger UI** | `./gitops/gitops.sh swagger` |

---

## 4. Troubleshooting & Operations

### Monitoring Pods

```bash
kubectl get pods -n carsharing -w
kubectl logs -f <pod-name> -n carsharing
```

### Image Pull Issues

If pods are stuck in `ErrImagePull`, ensure images are loaded into Kind:

```bash
./gitops/gitops.sh build
```

### Restarting Services

To pull new images or force a restart:

```bash
kubectl rollout restart deployment <deployment-name> -n carsharing
```
