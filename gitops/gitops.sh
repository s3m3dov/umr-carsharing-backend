#!/bin/bash

# Get the directory of this script and project root
GITOPS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$GITOPS_DIR")"

COMMAND=$1
ENV=$2

if [ -z "$COMMAND" ]; then
    echo "Usage: $0 [apply|diff|status|build|secrets|setup|dashboard|restart|ingress] [environment|deployment]"
    exit 1
fi

if [ -z "$ENV" ] && { [ "$COMMAND" == "apply" ] || [ "$COMMAND" == "diff" ]; }; then
    echo "Defaulting environment to 'local'"
    ENV="local"
fi

OVERLAY_PATH="gitops/overlays/$ENV"

# Always run from project root
cd "$PROJECT_ROOT"

case $COMMAND in
    apply)
        echo "Applying configuration for environment: $ENV"
        kubectl apply -k "$OVERLAY_PATH"
        ;;
    diff)
        echo "Showing differences for environment: $ENV"
        kubectl diff -k "$OVERLAY_PATH"
        ;;
    status)
        echo "Checking rollout status for all deployments in 'carsharing' namespace..."
        kubectl get deployments -n carsharing
        ;;
    build)
        echo "Building images..."
        ./gitops/scripts/local/build-for-kind.sh "${ENV:-local}"
        ;;
    secrets)
        echo "Creating secrets..."
        ./gitops/scripts/local/create-secrets.sh
        ;;
    setup)
        echo "Setting up local cluster..."
        ./gitops/scripts/local/setup-kind.sh
        ;;
    dashboard)
        echo "Deploying Kubernetes Dashboard..."
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
        kubectl apply -f gitops/dashboard/admin-user.yaml
        echo "Dashboard deployed. Use 'kubectl proxy' and visit:"
        echo "http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/"
        echo "Get your token with: kubectl -n kubernetes-dashboard create token admin-user"
        ;;
    restart)
        DEPLOYMENT=$2
        if [ -z "$DEPLOYMENT" ]; then
            echo "Restarting all deployments in 'carsharing' namespace..."
            kubectl rollout restart deployment -n carsharing
        else
            echo "Restarting deployment: $DEPLOYMENT"
            kubectl rollout restart deployment "$DEPLOYMENT" -n carsharing
        fi
        ;;
    ingress)
        echo "🚀 Starting automatic port-forwarding for Ingress Local..."
        echo "The Swagger UI will be available at: http://localhost:8888/swagger-ui.html"
        echo "Press Ctrl+C to stop port-forwarding."
        kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8888:80
        ;;
    *)
        echo "Unknown command: $COMMAND"
        echo "Available commands: apply, diff, status, build, secrets, setup, dashboard, restart, ingress"
        exit 1
        ;;
esac
