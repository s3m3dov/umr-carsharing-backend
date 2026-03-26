#!/bin/bash

################################################################################
# CarSharing GitOps Management Script
################################################################################
# Usage: ./gitops.sh <local|gcp> <command> [options]
#
# SCRIPT STRUCTURE:
#   1. Variables & Argument Parsing (lines 11-17)
#   2. Help Documentation (lines 19-362)
#   3. Validation & Error Handling (lines 364-394)
#   4. Helper Functions (lines 396-415)
#   5. Command Routing:
#      - LOCAL commands (lines 425-511)
#        • Setup & Initialization: setup, secrets
#        • Build & Deploy: build, deploy
#        • Access & Monitoring: ingress, status, diff
#        • Maintenance: restart, dashboard
#      - GCP commands (lines 513-591)
#        • Setup & Initialization: setup (unsupported), secrets
#        • Build & Deploy: build, deploy
#        • Access & Monitoring: ingress (unsupported), status, diff
#        • Maintenance: restart, dashboard
#
# KEY FEATURES:
#   - Automatic kubectl context switching
#   - Comprehensive error messages
#   - Side-by-side local/GCP workflow comparison
################################################################################

# Get the directory of this script and project root
GITOPS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$GITOPS_DIR")"

TARGET=$1      # local or gcp
COMMAND=$2     # setup, build, deploy, etc.
shift 2        # Remove first two args, rest are additional params

################################################################################
# HELP DOCUMENTATION
################################################################################

# Function to display help
show_help() {
    cat << 'EOF'
╔═══════════════════════════════════════════════════════════════════════════╗
║                    CarSharing GitOps Management Tool                      ║
╚═══════════════════════════════════════════════════════════════════════════╝

USAGE:
    ./gitops.sh <local|gcp> <command> [options]

    The first argument (local/gcp) automatically switches kubectl context
    before running the command.

═══════════════════════════════════════════════════════════════════════════
📋 DEPLOYMENT WORKFLOW - SIDE BY SIDE
═══════════════════════════════════════════════════════════════════════════

┌──────────────────────────────────┬───────────────────────────────────────┐
│   LOCAL (Kind Cluster)           │   GCP (GKE Autopilot)                 │
├──────────────────────────────────┼───────────────────────────────────────┤
│ 1. ./gitops.sh local setup       │ 1. ./gitops.sh gcp build [services]   │
│    • Creates Kind cluster        │    • Builds Docker images             │
│    • Installs ingress controller │    • Pushes to Google Container       │
│    • Sets up networking          │      Registry (GCR)                   │
│    • Switches context to local   │    • Tags with latest commit SHA      │
│                                  │    • Auto-switches to GCP context     │
│                                  │                                       │
│ 2. ./gitops.sh local secrets     │ 2. ./gitops.sh gcp secrets            │
│    • Auto-switches to local      │    • Auto-switches to GCP context     │
│    • Reads .env file             │    • Reads .env file                  │
│    • Creates K8s Secret object   │    • Creates K8s Secret in GKE        │
│                                  │                                       │
│ 3. ./gitops.sh local build       │ 3. ./gitops.sh gcp deploy [services]  │
│    • Auto-switches to local      │    • Auto-switches to GCP context     │
│    • Builds Docker images        │    • Applies K8s manifests            │
│    • Loads into Kind cluster     │    • Deletes old pods (force refresh) │
│                                  │    • Creates new pods with new images │
│                                  │                                       │
│ 4. ./gitops.sh local deploy      │ 4. ./gitops.sh gcp status             │
│    • Auto-switches to local      │    • Auto-switches to GCP context     │
│    • Applies K8s manifests       │    • Check deployment status          │
│    • Restarts all deployments    │                                       │
│                                  │                                       │
│ 5. ./gitops.sh local ingress     │ 5. Access External GCP IP      │
│    • Auto-switches to local      │    • External load balancer IP        │
│    • Port-forwards :8888 → :80   │    • No port-forward needed           │
│    • Access: localhost:8888      │                                       │
└──────────────────────────────────┴───────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════════
🏠 LOCAL COMMANDS
═══════════════════════════════════════════════════════════════════════════

  ./gitops.sh local setup
      What it does:
        - Creates a local Kubernetes cluster using Kind (Kubernetes in Docker)
        - Installs NGINX ingress controller for routing
        - Configures port mappings (80, 443)
        - Sets up carsharing namespace
        - Automatically switches kubectl context to local
      When to use:
        - First time setup only
        - If cluster was deleted and needs recreation
      Run time: ~2-3 minutes

  ./gitops.sh local secrets
      What it does:
        - Switches kubectl context to local Kind cluster
        - Reads variables from .env file in project root
        - Creates Kubernetes Secret named "carsharing-secrets"
        - Stores in "carsharing" namespace
      When to use:
        - Before first deployment
        - After updating .env file
      Run time: ~5 seconds

  ./gitops.sh local build
      What it does:
        - Switches kubectl context to local Kind cluster
        - Reads service directories from services/ folder
        - Runs docker build for each service
        - Tags images with format: <service-name>:local
        - Loads images directly into Kind cluster (no registry needed)
      When to use:
        - After code changes
        - Before running deploy
      Run time: ~3-5 minutes (depends on changes)

  ./gitops.sh local deploy
      What it does:
        - Switches kubectl context to local Kind cluster
        - Runs kubectl apply -k gitops/overlays/local
        - Creates/updates all Kubernetes resources (deployments, services, etc)
        - Automatically restarts all deployments to pull new images
      When to use:
        - After building new images
        - After changing Kubernetes manifests
        - To force deployment refresh
      Run time: ~30 seconds

  ./gitops.sh local ingress
      What it does:
        - Switches kubectl context to local Kind cluster
        - Runs kubectl port-forward on ingress-nginx-controller
        - Maps localhost:8888 to ingress controller port 80
        - Keeps running until Ctrl+C (foreground process)
      When to use:
        - To access the application locally
        - After successful deploy
      Access: http://localhost:8888/swagger-ui.html
      ⚠️  Blocks terminal - open new terminal for other commands

  ./gitops.sh local diff
      What it does:
        - Switches kubectl context to local Kind cluster
        - Shows differences between current cluster state and manifests
        - Highlights what would change if you run deploy
      When to use:
        - Before running deploy (to preview changes)
        - To debug configuration issues

  ./gitops.sh local status
      What it does:
        - Switches kubectl context to local Kind cluster
        - Lists all deployments in carsharing namespace
        - Shows ready/desired pod counts
      When to use:
        - To check if deployments are running
        - After deploy to verify success

  ./gitops.sh local restart [deployment]
      What it does:
        - Switches kubectl context to local Kind cluster
        - Triggers rolling restart of deployment(s)
        - Creates new pods, terminates old ones gracefully
        - Forces pods to pull latest images (zero-downtime restart)
      Options:
        - No args: restarts all deployments
        - With name: restarts specific deployment (e.g., auth-service)
      Examples:
        ./gitops.sh local restart
        ./gitops.sh local restart auth-service

  ./gitops.sh local dashboard
      What it does:
        - Switches kubectl context to local Kind cluster
        - Deploys Kubernetes Dashboard UI (web interface)
        - Creates admin-user ServiceAccount with cluster-admin role
      Access:
        1. Run: kubectl proxy
        2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/
           services/https:kubernetes-dashboard:/proxy/
        3. Get token: kubectl -n kubernetes-dashboard create token admin-user

═══════════════════════════════════════════════════════════════════════════
☁️  GCP COMMANDS
═══════════════════════════════════════════════════════════════════════════

  ./gitops.sh gcp build [services...]
      What it does:
        - Switches kubectl context to GKE cluster
        - Reads GCP_PROJECT_ID from gitops/base/configmap.yaml
        - Gets current git commit SHA for image tagging
        - Builds Docker images for specified services (or all)
        - Tags images: gcr.io/<project-id>/<service>:<commit-sha>
        - Pushes images to Google Container Registry (GCR)
        - Updates image tags in gitops/overlays/production/kustomization.yaml
      Options:
        - No args: builds all services (auth, user, car, trip, notification)
        - With names: builds only specified services
      Examples:
        ./gitops.sh gcp build                          # All services
        ./gitops.sh gcp build auth-service user-service # Only auth & user
      Run time: ~5-10 minutes (full build)
      Prerequisites:
        - gcloud CLI installed and authenticated (gcloud auth login)
        - Docker configured for GCR (gcloud auth configure-docker)

  ./gitops.sh gcp secrets
      What it does:
        - Switches kubectl context to GKE cluster
        - Reads variables from .env file
        - Creates Kubernetes Secret in GKE cluster
      When to use:
        - After first build (one-time setup)
        - After updating .env file
      Run time: ~5 seconds

  ./gitops.sh gcp deploy [services...]
      What it does:
        - Switches kubectl context to GKE cluster
        - Applies manifests from gitops/overlays/production
        - If no services specified:
          • Deletes ALL pods in carsharing namespace (hard refresh)
          • Applies all Kubernetes manifests
          • Creates new pods with updated images from GCR
        - If services specified:
          • Applies all manifests first
          • Deletes only pods for specified services
          • GKE recreates pods with new images
      Options:
        - No args: full deployment (all services)
        - With names: selective deployment (specific services)
      Examples:
        ./gitops.sh gcp deploy                    # Deploy everything
        ./gitops.sh gcp deploy trip-service       # Deploy only trip service
      Run time: ~2-5 minutes (depends on scope)
      ⚠️  Causes brief downtime during pod deletion/recreation

  ./gitops.sh gcp status
      What it does:
        - Switches kubectl context to GKE cluster
        - Lists all deployments in carsharing namespace
        - Shows ready/desired pod counts
      When to use:
        - After deploy to verify success
        - To check production deployment status

  ./gitops.sh gcp restart [deployment]
      What it does:
        - Switches kubectl context to GKE cluster
        - Triggers rolling restart of deployment(s) in production
        - Zero-downtime restart
      Options:
        - No args: restarts all deployments
        - With name: restarts specific deployment
      Examples:
        ./gitops.sh gcp restart
        ./gitops.sh gcp restart auth-service

  ./gitops.sh gcp dashboard
      What it does:
        - Switches kubectl context to GKE cluster
        - Deploys Kubernetes Dashboard to GKE cluster
      When to use:
        - Optional: for visual cluster management in production

  Production Access:
      URL: http://35.186.208.67
      Load balancer takes ~2 minutes after deploy to route traffic

═══════════════════════════════════════════════════════════════════════════
📝 CONFIGURATION FILES
═══════════════════════════════════════════════════════════════════════════

  gitops/base/configmap.yaml
      Contains: GCP_PROJECT_ID, GKE_CLUSTER_NAME, GCP_REGION
      Used by: gcp commands

  .env
      Contains: Database credentials, API keys, JWT secrets, etc.
      Used by: secrets command (both local and GCP)
      ⚠️  Never commit to git - listed in .gitignore

  gitops/overlays/local/kustomization.yaml
      Contains: Local-specific configuration (image tags, replicas, etc.)

  gitops/overlays/production/kustomization.yaml
      Contains: Production configuration (updated by gcp build)

═══════════════════════════════════════════════════════════════════════════
💡 COMPLETE WORKFLOW EXAMPLES
═══════════════════════════════════════════════════════════════════════════

LOCAL DEVELOPMENT - FIRST TIME SETUP:
  1. ./gitops.sh local setup     # Create cluster (~2 min)
  2. ./gitops.sh local secrets   # Load .env variables
  3. ./gitops.sh local build     # Build images (~3-5 min)
  4. ./gitops.sh local deploy    # Deploy everything (~30 sec)
  5. ./gitops.sh local ingress   # Access at http://localhost:8888

LOCAL DEVELOPMENT - AFTER CODE CHANGES:
  1. ./gitops.sh local build     # Rebuild images
  2. ./gitops.sh local deploy    # Redeploy with new images
  (ingress should still be running in another terminal)

LOCAL DEVELOPMENT - QUICK RESTART:
  ./gitops.sh local restart auth-service  # Restart specific service
  OR
  ./gitops.sh local restart               # Restart all services

GCP PRODUCTION - FIRST TIME DEPLOYMENT:
  1. ./gitops.sh gcp build       # Build all images, push to GCR (~5-10 min)
  2. ./gitops.sh gcp secrets     # Create secrets in GKE
  3. ./gitops.sh gcp deploy      # Deploy to production (~2-5 min)
  4. Wait ~2 minutes for load balancer
  5. Access the external IP

GCP PRODUCTION - UPDATE SPECIFIC SERVICES:
  1. ./gitops.sh gcp build auth-service user-service  # Build only 2 services
  2. ./gitops.sh gcp deploy auth-service user-service # Deploy only those 2
  (Faster than full deployment, ~2-3 minutes total)

GCP PRODUCTION - EMERGENCY ROLLBACK:
  1. git revert <bad-commit>
  2. ./gitops.sh gcp build       # Build previous code
  3. ./gitops.sh gcp deploy      # Deploy rollback

SWITCHING CONTEXTS (Now Automatic):
  Context switching is now automatic! Just use:
    ./gitops.sh local <command>  # Automatically switches to local
    ./gitops.sh gcp <command>    # Automatically switches to GCP

  To manually check current context:
    kubectl config current-context

  To manually switch (rarely needed):
    kind export kubeconfig --name "carsharing-local"  # Switch to local
    gcloud container clusters get-credentials ...      # Switch to GCP

EOF
}

################################################################################
# VALIDATION & ARGUMENT PARSING
################################################################################

# Check if no arguments provided or help requested
if [ -z "$TARGET" ] || [ "$TARGET" == "help" ] || [ "$TARGET" == "--help" ] || [ "$TARGET" == "-h" ]; then
    show_help
    exit 0
fi

# Validate target
if [ "$TARGET" != "local" ] && [ "$TARGET" != "gcp" ]; then
    echo "❌ Error: First argument must be 'local' or 'gcp'"
    echo ""
    echo "Usage: ./gitops.sh <local|gcp> <command> [options]"
    echo "Run './gitops.sh help' for full documentation"
    exit 1
fi

# Check if command provided
if [ -z "$COMMAND" ]; then
    echo "❌ Error: No command specified"
    echo ""
    echo "Usage: ./gitops.sh $TARGET <command> [options]"
    echo "Run './gitops.sh help' for full documentation"
    exit 1
fi

# Always run from project root
cd "$PROJECT_ROOT"

################################################################################
# HELPER FUNCTIONS
################################################################################

# Function to switch context
switch_context() {
    local target=$1
    if [ "$target" == "local" ]; then
        echo "🔌 Switching to local Kind cluster..."
        kind export kubeconfig --name "carsharing-local" 2>/dev/null
        if [ $? -ne 0 ]; then
            echo "⚠️  Warning: Could not switch to local cluster. Is it running?"
            echo "   Run './gitops.sh local setup' if you haven't created the cluster yet."
        fi
    elif [ "$target" == "gcp" ]; then
        echo "🔌 Switching to GCP GKE cluster..."
        ./gitops/scripts/gcp/connect-gcp.sh
    fi
}

# Function to handle unsupported commands
command_not_supported() {
    local target=$1
    local command=$2
    echo "❌ Error: Command '$command' is not supported for target '$target'"
    echo ""
    echo "Run './gitops.sh help' to see available commands"
    exit 1
}

# Function to handle unknown commands
command_not_found() {
    local command=$1
    echo "❌ Error: Unknown command '$command'"
    echo ""
    echo "Run './gitops.sh help' to see available commands"
    exit 1
}

################################################################################
# COMMAND ROUTING
################################################################################
# Commands are organized in logical workflow order:
# 1. Setup/Initialization (setup, secrets)
# 2. Build & Deploy (build, deploy)
# 3. Access & Monitoring (ingress, status, diff)
# 4. Maintenance (restart, dashboard)
################################################################################

if [ "$TARGET" == "local" ]; then
    case $COMMAND in
        # ============================================================
        # SETUP & INITIALIZATION
        # ============================================================
        setup)
            echo "Setting up local cluster..."
            ./gitops/scripts/local/setup-kind.sh
            ;;
        
        secrets)
            switch_context "local"
            echo "Creating secrets in local cluster..."
            ./gitops/scripts/local/create-secrets.sh
            ;;
        
        # ============================================================
        # BUILD & DEPLOY
        # ============================================================
        build)
            switch_context "local"
            echo "Building images for local cluster..."
            ./gitops/scripts/local/build-kind.sh "local"
            ;;
        
        deploy)
            switch_context "local"
            echo "Deploying to local cluster..."
            kubectl apply -k "gitops/overlays/local"
            echo "Restarting all deployments in 'carsharing' namespace to pick up new images..."
            kubectl rollout restart deployment -n carsharing
            ;;
        
        # ============================================================
        # ACCESS & MONITORING
        # ============================================================
        ingress)
            switch_context "local"
            echo "🚀 Starting port-forwarding for Ingress (localhost:8888)..."
            echo "The application will be available at: http://localhost:8888/swagger-ui.html"
            echo "Press Ctrl+C to stop port-forwarding."
            kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8888:80
            ;;
        
        status)
            switch_context "local"
            echo "Checking deployment status in local cluster..."
            kubectl get deployments -n carsharing
            ;;
        
        diff)
            switch_context "local"
            echo "Showing differences for local environment..."
            kubectl diff -k "gitops/overlays/local"
            ;;
        
        # ============================================================
        # MAINTENANCE & UTILITIES
        # ============================================================
        restart)
            switch_context "local"
            if [ -z "$1" ]; then
                echo "Restarting all deployments in local cluster..."
                kubectl rollout restart deployment -n carsharing
            else
                echo "Restarting deployment: $1"
                kubectl rollout restart deployment "$1" -n carsharing
            fi
            ;;
        
        dashboard)
            switch_context "local"
            echo "Deploying Kubernetes Dashboard to local cluster..."
            kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
            kubectl apply -f gitops/dashboard/admin-user.yaml
            echo ""
            echo "✅ Dashboard deployed. Access it with:"
            echo "   1. Run: kubectl proxy"
            echo "   2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/"
            echo "   3. Get token: kubectl -n kubernetes-dashboard create token admin-user"
            ;;
        
        # ============================================================
        # ERROR HANDLING
        # ============================================================
        *)
            command_not_found "$COMMAND"
            ;;
    esac

elif [ "$TARGET" == "gcp" ]; then
    case $COMMAND in
        # ============================================================
        # SETUP & INITIALIZATION
        # ============================================================
        setup)
            echo "❌ Error: Command 'setup' is not supported for target 'gcp'"
            echo ""
            echo "💡 GKE cluster is already provisioned. Available GCP commands:"
            echo "   ./gitops.sh gcp build     - Build and push images to GCR"
            echo "   ./gitops.sh gcp deploy    - Deploy to GKE"
            echo "   ./gitops.sh gcp secrets   - Create secrets in GKE"
            exit 1
            ;;
        
        secrets)
            switch_context "gcp"
            echo "Creating secrets in GKE cluster..."
            ./gitops/scripts/local/create-secrets.sh
            ;;
        
        # ============================================================
        # BUILD & DEPLOY
        # ============================================================
        build)
            switch_context "gcp"
            echo "🐳 Building and pushing images to GCR..."
            ./gitops/scripts/gcp/build-gcp.sh "$@"
            ;;
        
        deploy)
            switch_context "gcp"
            if [ -z "$1" ]; then
                echo "🚀 Deploying all services to GKE..."
                echo "🗑️  Deleting old pods as fail-safe..."
                kubectl delete pods --all -n carsharing --grace-period=30
                echo "📦 Creating new pods from updated manifests..."
                kubectl apply -k gitops/overlays/production
            else
                echo "🔄 Deploying specific services to GKE..."
                echo "📦 Applying updated manifests..."
                kubectl apply -k gitops/overlays/production
                for service in "$@"; do
                    echo "🗑️  Deleting old pods for $service..."
                    kubectl delete pods -l app="$service" -n carsharing --grace-period=30
                done
            fi
            ;;
        
        # ============================================================
        # ACCESS & MONITORING
        # ============================================================
        ingress)
            echo "❌ Error: Command 'ingress' is not supported for target 'gcp'"
            echo ""
            echo "💡 GKE uses an external load balancer. No port-forwarding needed."
            echo "   Access your application at: http://35.186.208.67"
            exit 1
            ;;
        
        status)
            switch_context "gcp"
            echo "Checking deployment status in GKE cluster..."
            kubectl get deployments -n carsharing
            ;;
        
        diff)
            switch_context "gcp"
            echo "Showing differences for production environment..."
            kubectl diff -k "gitops/overlays/production"
            ;;
        
        # ============================================================
        # MAINTENANCE & UTILITIES
        # ============================================================
        restart)
            switch_context "gcp"
            if [ -z "$1" ]; then
                echo "Restarting all deployments in GKE cluster..."
                kubectl rollout restart deployment -n carsharing
            else
                echo "Restarting deployment: $1"
                kubectl rollout restart deployment "$1" -n carsharing
            fi
            ;;
        
        dashboard)
            switch_context "gcp"
            echo "Deploying Kubernetes Dashboard to GKE cluster..."
            kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
            kubectl apply -f gitops/dashboard/admin-user.yaml
            echo ""
            echo "✅ Dashboard deployed. Access it with:"
            echo "   1. Run: kubectl proxy"
            echo "   2. Visit: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/"
            echo "   3. Get token: kubectl -n kubernetes-dashboard create token admin-user"
            ;;
        
        # ============================================================
        # ERROR HANDLING
        # ============================================================
        *)
            command_not_found "$COMMAND"
            ;;
    esac
fi
