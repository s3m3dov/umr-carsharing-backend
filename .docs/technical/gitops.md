# Manual GitOps Workflow

This project follows a "Manual GitOps" pattern. Git is the single source of truth for the desired state of the cluster.

## Rules

1. **Never** use `kubectl edit`.
2. **Never** use `kubectl patch` or `kubectl delete` for ad-hoc changes.
3. **Always** commit changes to the `gitops/` directory first.
4. **Always** use `gitops/gitops.sh` to apply changes.

## Commands

### Deploy Changes

```bash
./gitops/gitops.sh apply local
# or
./gitops/gitops.sh apply production
```

### Build Images

```bash
./gitops/gitops.sh build local
```

### Create Secrets

```bash
./gitops/gitops.sh secrets
```

### Setup Cluster

```bash
./gitops/gitops.sh setup
```

### Preview Changes (Dry-run)

```bash
./gitops/gitops.sh diff local
```

### Check Status

```bash
./gitops/gitops.sh status
```

### Restart Deployments (Force image pull)

```bash
# Restart all services
./gitops/gitops.sh restart

# Restart specific service
./gitops/gitops.sh restart vehicle-service
```

### Dashboard Access

1. Deploy the dashboard:

   ```bash
   ./gitops/gitops.sh dashboard
   ```

2. Start the proxy:

   ```bash
   kubectl proxy
   ```

3. Get login token:

   ```bash
   kubectl -n kubernetes-dashboard create token admin-user
   ```

4. Access at: [Kubernetes Dashboard Proxy](http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/)
