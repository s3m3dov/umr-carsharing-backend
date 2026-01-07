#!/bin/bash

# Get the directory of this script and project root
GITOPS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$GITOPS_DIR")"

COMMAND=$1
ENV=$2

# Function to display help
show_help() {
    cat << 'EOF'
╔═══════════════════════════════════════════════════════════════════════════╗
║                    CarSharing GitOps Management Tool                      ║
╚═══════════════════════════════════════════════════════════════════════════╝

USAGE:
    ./gitops.sh <command> [options]

═══════════════════════════════════════════════════════════════════════════

🏠 LOCAL DEVELOPMENT COMMANDS (Kind Cluster)
───────────────────────────────────────────────────────────────────────────
  setup                     Create local Kind cluster with ingress
  secrets                   Create secrets from .env file
  build [env]               Build Docker images for Kind (default: local)
  apply [env]               Apply Kubernetes manifests (default: local)
  ingress                   Port-forward ingress (access at localhost:8888)
  diff [env]                Show differences for environment (default: local)
  status                    Check deployment status in carsharing namespace
  restart [deployment]      Restart deployment(s) in carsharing namespace
                           - No args: restart all deployments
                           - With name: restart specific deployment
  dashboard                 Deploy Kubernetes Dashboard

WORKFLOW EXAMPLE:
  1. ./gitops.sh setup       # One-time cluster setup
  2. ./gitops.sh secrets     # Create secrets from .env
  3. ./gitops.sh build       # Build images
  4. ./gitops.sh apply       # Deploy to local cluster
  5. ./gitops.sh ingress     # Access at http://localhost:8888
  6. ./gitops.sh dashboard   # Deploy Kubernetes Dashboard

═══════════════════════════════════════════════════════════════════════════

☁️  GCP PRODUCTION COMMANDS (GKE Autopilot)
───────────────────────────────────────────────────────────────────────────
  setup-gcp                Configure kubectl for GKE cluster
                           Reads from ConfigMap:
                           - GKE_CLUSTER_NAME: autopilot-carsharing-cluster-01
                           - GCP_REGION: europe-north1
                           - GCP_PROJECT_ID: project-b05bd45b-70e8-4b7f-a65

  build-gcp [services...]   Build and push images to GCR
                           - No args: build all services
                           - With names: build specific services
                           Examples:
                             ./gitops.sh build-gcp
                             ./gitops.sh build-gcp auth-service user-service

  deploy-gcp [services...]  Deploy to GKE Autopilot cluster
                           - No args: deploy all services
                           - With names: restart specific deployments
                           Examples:
                             ./gitops.sh deploy-gcp
                             ./gitops.sh deploy-gcp auth-service

WORKFLOW EXAMPLE:
  1. ./gitops.sh setup-gcp       # Configure kubectl (one-time)
  2. ./gitops.sh secrets         # Create secrets from .env
  3. ./gitops.sh build-gcp       # Build and push images
  4. ./gitops.sh deploy-gcp      # Deploy to GKE
  5. Access at: http://34.160.91.182

SELECTIVE DEPLOYMENT:
  # Build only auth and user services
  ./gitops.sh build-gcp auth-service user-service
  
  # Restart only trip service
  ./gitops.sh deploy-gcp trip-service

═══════════════════════════════════════════════════════════════════════════

📝 CONFIGURATION
───────────────────────────────────────────────────────────────────────────
  - ConfigMap: gitops/base/configmap.yaml
  - Environment: .env

EOF
}

# Check if no command provided or help requested
if [ -z "$COMMAND" ] || [ "$COMMAND" == "help" ] || [ "$COMMAND" == "--help" ] || [ "$COMMAND" == "-h" ]; then
    show_help
    exit 0
fi

if [ -z "$ENV" ] && { [ "$COMMAND" == "apply" ] || [ "$COMMAND" == "diff" ]; }; then
    echo "Defaulting environment to 'local'"
    ENV="local"
fi

OVERLAY_PATH="gitops/overlays/$ENV"

# Always run from project root
cd "$PROJECT_ROOT"

case $COMMAND in
    setup)
        echo "Setting up local cluster..."
        ./gitops/scripts/local/setup-kind.sh
        ;;
    secrets)
        echo "Creating secrets..."
        ./gitops/scripts/local/create-secrets.sh
        ;;
    build)
        echo "Building images..."
        ./gitops/scripts/local/build-for-kind.sh "${ENV:-local}"
        ;;
    apply)
        echo "Applying configuration for environment: $ENV"
        kubectl apply -k "$OVERLAY_PATH"
        ;;
    ingress)
        echo "🚀 Starting automatic port-forwarding for Ingress Local..."
        echo "The Swagger UI will be available at: http://localhost:8888/swagger-ui.html"
        echo "Press Ctrl+C to stop port-forwarding."
        kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8888:80
        ;;
    diff)
        echo "Showing differences for environment: $ENV"
        kubectl diff -k "$OVERLAY_PATH"
        ;;
    status)
        echo "Checking rollout status for all deployments in 'carsharing' namespace..."
        kubectl get deployments -n carsharing
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
    dashboard)
        echo "Deploying Kubernetes Dashboard..."
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
        kubectl apply -f gitops/dashboard/admin-user.yaml
        echo "Dashboard deployed. Use 'kubectl proxy' and visit:"
        echo "http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/"
        echo "Get your token with: kubectl -n kubernetes-dashboard create token admin-user"
        ;;
    setup-gcp)
        echo "🔧 Configuring kubectl for GKE cluster..."
        ./gitops/scripts/gcp/setup-gcp.sh
        ;;
    build-gcp)
        echo "🐳 Building and pushing images to GCR..."
        ./gitops/scripts/gcp/build-gcp.sh "${@:2}"
        ;;
    deploy-gcp)
        if [ -z "$2" ]; then
            echo "🚀 Deploying all services to GKE..."
            kubectl apply -k gitops/overlays/production
        else
            echo "🔄 Restarting specific services in GKE..."
            for service in "${@:2}"; do
                echo "  - Restarting $service"
                kubectl rollout restart deployment "$service" -n carsharing
            done
        fi
        ;;
    *)
        echo "❌ Unknown command: $COMMAND"
        echo ""
        echo "Run './gitops.sh help' for usage information"
        exit 1
        ;;
esac
