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
📋 DEPLOYMENT WORKFLOW - SIDE BY SIDE
═══════════════════════════════════════════════════════════════════════════

┌──────────────────────────────────┬───────────────────────────────────────┐
│   LOCAL (Kind Cluster)           │   GCP (GKE Autopilot)                 │
├──────────────────────────────────┼───────────────────────────────────────┤
│ 1. setup-local                   │ 1. build-gcp [services...]            │
│    • Creates Kind cluster        │    • Builds Docker images             │
│    • Installs ingress controller │    • Pushes to Google Container       │
│    • Sets up networking          │      Registry (GCR)                   │
│                                  │    • Tags with latest commit SHA      │
│                                  │                                       │
│ 2. connect-local                 │ 2. connect-gcp                        │
│    • Switches kubectl context    │    • Authenticates with GCP           │
│    • Points kubectl to Kind      │    • Configures kubectl for GKE       │
│    • Required before any kubectl │    • Switches kubectl context to GKE  │
│      commands work on local      │    • Required before deploy           │
│                                  │                                       │
│ 3. secrets                       │ 3. secrets                            │
│    • Reads .env file             │    • Reads .env file                  │
│    • Creates K8s Secret object   │    • Creates K8s Secret object        │
│    • Stores in carsharing ns     │    • Stores in carsharing namespace   │
│                                  │                                       │
│ 4. build [env]                   │ 4. deploy-gcp [services...]           │
│    • Builds Docker images        │    • Applies K8s manifests            │
│    • Loads into Kind cluster     │    • Deletes old pods (force refresh) │
│    • Tags images locally         │    • Creates new pods with new images │
│                                  │    • Pulls from GCR automatically     │
│                                  │                                       │
│ 5. apply [env]                   │ 5. Check status                       │
│    • Applies K8s manifests       │    • Use: ./gitops.sh status          │
│    • Creates/updates deployments │    • Or: kubectl get pods -n          │
│    • Restarts all deployments    │      carsharing                       │
│    • Pulls new images            │                                       │
│                                  │                                       │
│ 6. ingress                       │ 6. Access Application                 │
│    • Port-forwards ingress       │    • URL: http://34.160.91.182        │
│    • Maps localhost:8888 → :80   │    • External load balancer IP        │
│    • Access: localhost:8888      │    • No port-forward needed           │
│                                  │                                       │
│ 7. dashboard (optional)          │ 7. dashboard (optional)               │
│    • Deploys K8s Dashboard UI    │    • Deploys K8s Dashboard UI         │
└──────────────────────────────────┴───────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════
🏠 LOCAL DEVELOPMENT COMMANDS (Kind Cluster)
═══════════════════════════════════════════════════════════════════════════

  setup-local
      What it does:
        - Creates a local Kubernetes cluster using Kind (Kubernetes in Docker)
        - Installs NGINX ingress controller for routing
        - Configures port mappings (80, 443)
        - Sets up carsharing namespace
      When to use:
        - First time setup only
        - If cluster was deleted and needs recreation
      Run time: ~2-3 minutes

  connect-local
      What it does:
        - Exports Kind cluster kubeconfig to ~/.kube/config
        - Switches kubectl context to "kind-carsharing-local"
        - Makes kubectl commands target the local cluster
      When to use:
        - After setup-local
        - After working with GCP (to switch back to local)
        - If kubectl is pointing to wrong cluster
      ⚠️  REQUIRED: Must run before any kubectl commands work on local cluster

  secrets
      What it does:
        - Reads variables from .env file in project root
        - Creates Kubernetes Secret named "carsharing-secrets"
        - Stores in "carsharing" namespace
        - Makes secrets available to all pods via env vars
      When to use:
        - Before first deployment
        - After updating .env file
      Note: Works on whatever cluster kubectl is currently connected to

  build [env]
      What it does:
        - Reads service directories from services/ folder
        - Runs docker build for each service
        - Tags images with format: <service-name>:local
        - Loads images directly into Kind cluster (no registry needed)
        - Uses Kustomize to read overlays/<env> configuration
      When to use:
        - After code changes
        - Before running apply
      Default env: local
      Run time: ~3-5 minutes (depends on changes)

  apply [env]
      What it does:
        - Runs kubectl apply -k on gitops/overlays/<env>
        - Creates/updates all Kubernetes resources (deployments, services, etc)
        - Automatically restarts all deployments to pull new images
        - Applies ConfigMaps and environment configuration
      When to use:
        - After building new images
        - After changing Kubernetes manifests
        - To force deployment refresh
      Default env: local

  ingress
      What it does:
        - Runs kubectl port-forward on ingress-nginx-controller
        - Maps localhost:8888 to ingress controller port 80
        - Keeps running until Ctrl+C (foreground process)
        - Routes requests to services based on ingress rules
      When to use:
        - To access the application locally
        - After successful apply
      Access: http://localhost:8888/swagger-ui.html
      ⚠️  Blocks terminal - open new terminal for other commands

  diff [env]
      What it does:
        - Shows differences between current cluster state and manifests
        - Highlights what would change if you run apply
        - Uses kubectl diff under the hood
      When to use:
        - Before running apply (to preview changes)
        - To debug configuration issues
      Default env: local

  status
      What it does:
        - Lists all deployments in carsharing namespace
        - Shows ready/desired pod counts
        - Displays current deployment status
      When to use:
        - To check if deployments are running
        - After apply to verify success
        - To troubleshoot deployment issues

  restart [deployment]
      What it does:
        - Triggers rolling restart of deployment(s)
        - Creates new pods, terminates old ones gracefully
        - Forces pods to pull latest images
        - Zero-downtime restart
      When to use:
        - After building new images (if apply didn't work)
        - To refresh pods without changing manifests
        - To recover from pod errors
      Options:
        - No args: restarts all deployments in carsharing namespace
        - With name: restarts specific deployment (e.g., auth-service)

  dashboard
      What it does:
        - Deploys Kubernetes Dashboard UI (web interface)
        - Creates admin-user ServiceAccount with cluster-admin role
        - Sets up RBAC permissions
      When to use:
        - Optional: for visual cluster management
      Access:
        1. Run: kubectl proxy
        2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/
           services/https:kubernetes-dashboard:/proxy/
        3. Get token: kubectl -n kubernetes-dashboard create token admin-user

═══════════════════════════════════════════════════════════════════════════
☁️  GCP PRODUCTION COMMANDS (GKE Autopilot)
═══════════════════════════════════════════════════════════════════════════

  build-gcp [services...]
      What it does:
        - Reads GCP_PROJECT_ID from gitops/base/configmap.yaml
        - Gets current git commit SHA for image tagging
        - Builds Docker images for specified services (or all)
        - Tags images: gcr.io/<project-id>/<service>:<commit-sha>
        - Pushes images to Google Container Registry (GCR)
        - Updates image tags in gitops/overlays/production/kustomization.yaml
      When to use:
        - Before deploying to GCP
        - After code changes
        - Can build all services or specific ones
      Options:
        - No args: builds all services (auth, user, car, trip, notification)
        - With names: builds only specified services
      Examples:
        ./gitops.sh build-gcp                          # All services
        ./gitops.sh build-gcp auth-service user-service # Only auth & user
      Run time: ~5-10 minutes (full build)

  connect-gcp
      What it does:
        - Reads from gitops/base/configmap.yaml:
          • GKE_CLUSTER_NAME (autopilot-cluster-1)
          • GCP_REGION (europe-north1)
          • GCP_PROJECT_ID (project-b05bd45b-70e8-4b7f-a65)
        - Runs: gcloud container clusters get-credentials
        - Authenticates with Google Cloud
        - Configures kubectl to point to GKE cluster
        - Switches kubectl context to GKE
      When to use:
        - After build-gcp, before deploy-gcp
        - After working with local cluster (to switch back to GCP)
        - If kubectl is pointing to wrong cluster
      ⚠️  REQUIRED: Must run before deploy-gcp or any GKE kubectl commands
      Prerequisites:
        - gcloud CLI installed and authenticated (gcloud auth login)
        - Access to GCP project

  secrets
      What it does:
        - Same as local: reads .env file
        - Creates Kubernetes Secret in current kubectl context
        - If connected to GKE, creates secret in GKE cluster
      When to use:
        - After connect-gcp (first time only)
        - After updating .env file
      ⚠️  Make sure kubectl is connected to correct cluster (check with:
          kubectl config current-context)

  deploy-gcp [services...]
      What it does:
        - Applies manifests from gitops/overlays/production
        - If no services specified:
          • Deletes ALL pods in carsharing namespace (hard refresh)
          • Applies all Kubernetes manifests
          • Creates new pods with updated images from GCR
        - If services specified:
          • Applies all manifests first
          • Deletes only pods for specified services
          • GKE recreates pods with new images
        - Pods automatically pull images from GCR using latest tags
      When to use:
        - After build-gcp and connect-gcp
        - To deploy updated code to production
        - For selective service updates (faster)
      Options:
        - No args: full deployment (all services)
        - With names: selective deployment (specific services)
      Examples:
        ./gitops.sh deploy-gcp                    # Deploy everything
        ./gitops.sh deploy-gcp trip-service       # Deploy only trip service
      Run time: ~2-5 minutes (depends on scope)
      ⚠️  Causes brief downtime during pod deletion/recreation

  Production Access
      What it is:
        - External IP: http://34.160.91.182
        - Automatically configured by GKE load balancer
        - No port-forwarding needed
      When available:
        - After successful deploy-gcp
        - Load balancer takes ~2 minutes to route traffic to new pods
      How to verify:
        - Check: kubectl get svc -n ingress-nginx
        - Look for EXTERNAL-IP on ingress-nginx-controller

  dashboard
      What it does:
        - Same as local: deploys Kubernetes Dashboard
        - Works on current kubectl context (use after connect-gcp for GKE)
      When to use:
        - Optional: for visual cluster management

═══════════════════════════════════════════════════════════════════════════
📝 CONFIGURATION FILES
═══════════════════════════════════════════════════════════════════════════

  gitops/base/configmap.yaml
      Contains: GCP_PROJECT_ID, GKE_CLUSTER_NAME, GCP_REGION
      Used by: connect-gcp, build-gcp scripts

  .env
      Contains: Database credentials, API keys, JWT secrets, etc.
      Used by: secrets command (both local and GCP)
      ⚠️  Never commit to git - listed in .gitignore

  gitops/overlays/local/kustomization.yaml
      Contains: Local-specific configuration (image tags, replicas, etc.)

  gitops/overlays/production/kustomization.yaml
      Contains: Production configuration (updated by build-gcp with image tags)

═══════════════════════════════════════════════════════════════════════════
💡 COMPLETE WORKFLOW EXAMPLES
═══════════════════════════════════════════════════════════════════════════

LOCAL DEVELOPMENT - FIRST TIME SETUP:
  1. ./gitops.sh setup-local     # Create cluster (~2 min)
  2. ./gitops.sh connect-local   # Switch context (required!)
  3. ./gitops.sh secrets         # Load .env variables
  4. ./gitops.sh build           # Build images (~3-5 min)
  5. ./gitops.sh apply           # Deploy everything (~1 min)
  6. ./gitops.sh ingress         # Access at http://localhost:8888

LOCAL DEVELOPMENT - AFTER CODE CHANGES:
  1. ./gitops.sh connect-local   # Make sure kubectl points to local
  2. ./gitops.sh build           # Rebuild images
  3. ./gitops.sh apply           # Redeploy with new images
  (ingress should still be running in another terminal)

LOCAL DEVELOPMENT - QUICK RESTART:
  1. ./gitops.sh restart auth-service  # Restart specific service
     OR
  1. ./gitops.sh restart               # Restart all services

GCP PRODUCTION - FIRST TIME DEPLOYMENT:
  1. ./gitops.sh build-gcp       # Build all images, push to GCR (~5-10 min)
  2. ./gitops.sh connect-gcp     # Connect kubectl to GKE (required!)
  3. ./gitops.sh secrets         # Create secrets in GKE
  4. ./gitops.sh deploy-gcp      # Deploy to production (~2-5 min)
  5. Wait ~2 minutes for load balancer
  6. Access: http://34.160.91.182

GCP PRODUCTION - UPDATE SPECIFIC SERVICES:
  1. ./gitops.sh build-gcp auth-service user-service  # Build only 2 services
  2. ./gitops.sh connect-gcp                          # Switch to GKE context
  3. ./gitops.sh deploy-gcp auth-service user-service # Deploy only those 2
  (Faster than full deployment, ~2-3 minutes total)

GCP PRODUCTION - EMERGENCY ROLLBACK:
  1. git revert <bad-commit>
  2. ./gitops.sh build-gcp       # Build previous code
  3. ./gitops.sh connect-gcp     # Ensure GKE context
  4. ./gitops.sh deploy-gcp      # Deploy rollback

SWITCHING BETWEEN LOCAL AND GCP:
  From local to GCP:
    1. ./gitops.sh connect-gcp   # Switch kubectl context to GKE
    2. kubectl get pods -n carsharing  # Verify connected to GCP

  From GCP to local:
    1. ./gitops.sh connect-local # Switch kubectl context to Kind
    2. kubectl get pods -n carsharing  # Verify connected to local

  Check current context:
    kubectl config current-context
    # Should show: kind-carsharing-local OR gke_<project>_<region>_<cluster>

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
    setup-local)
        echo "Setting up local cluster..."
        ./gitops/scripts/local/setup-kind.sh
        ;;
    connect-local)
        echo "🔌 Connecting to local Kind cluster..."
        kind export kubeconfig --name "carsharing-local"
        ;;
    secrets)
        echo "Creating secrets..."
        ./gitops/scripts/local/create-secrets.sh
        ;;
    build)
        echo "Building images..."
        ./gitops/scripts/local/build-kind.sh "${ENV:-local}"
        ;;
    apply)
        echo "Applying configuration for environment: $ENV"
        kubectl apply -k "$OVERLAY_PATH"
        echo "Restarting all deployments in 'carsharing' namespace to pick up new images..."
        kubectl rollout restart deployment -n carsharing
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
    connect-gcp)
        echo "🔧 Configuring kubectl for GKE cluster..."
        ./gitops/scripts/gcp/connect-gcp.sh
        ;;
    build-gcp)
        echo "🐳 Building and pushing images to GCR..."
        ./gitops/scripts/gcp/build-gcp.sh "${@:2}"
        ;;
    deploy-gcp)
        if [ -z "$2" ]; then
            echo "🚀 Deploying all services to GKE..."
            echo "🗑️  Deleting old pods as fail-safe..."
            kubectl delete pods --all -n carsharing --grace-period=30
            echo "📦 Creating new pods from updated manifests..."
            kubectl apply -k gitops/overlays/production
        else
            echo "🔄 Deploying specific services to GKE..."
            echo "📦 Applying updated manifests..."
            kubectl apply -k gitops/overlays/production
            for service in "${@:2}"; do
                echo "🗑️  Deleting old pods for $service..."
                kubectl delete pods -l app="$service" -n carsharing --grace-period=30
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
