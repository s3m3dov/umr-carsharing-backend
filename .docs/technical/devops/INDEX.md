# DevOps Documentation Index

**Purpose:** Central index for deployment, infrastructure, and operations documentation  
**Version:** 1.0  
**Last Updated:** April 2026

---

## Overview

This section covers cloud infrastructure, deployment pipelines, container orchestration, and monitoring.

---

## Documentation Files

| Document | Description |
|----------|-------------|
| [gitops.md](./gitops.md) | Deployment rules, GitOps workflow, scripts, and commands |
| [gcp-initial-setup.md](./gcp-initial-setup.md) | First-time GCP setup (GKE, Artifact Registry, Static IP) |
| [gcp-deployment.md](./gcp-deployment.md) | Cloud deployment guide |
| [monitoring.md](./monitoring.md) | Kubernetes Dashboard, Prometheus, Grafana, Loki |

---

## Quick Reference

### Deployment Flow

```
Local → Git Push → CI/CD → Container Registry → GKE Cluster → Service Mesh
```

### Key Tools

| Tool | Purpose |
|------|---------|
| Docker | Containerization |
| Google Kubernetes Engine | Container orchestration |
| Artifact Registry | Container image storage |
| Cloud DNS | Domain & routing |

---

## Related Documentation

- [API Architecture](../api-architecture/architecture.md)
- [GitOps Workflow](./gitops.md)
- [Architecture Overview](../api-architecture/architecture.md)