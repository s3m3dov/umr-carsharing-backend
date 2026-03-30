# Car Sharing Application

A carpooling platform designed for Tanzania to facilitate ride sharing between students in the university.

## Project Overview

This application provides a robust backend for a carpooling service, allowing users to offer and find rides, manage vehicles, and track ride history. It leverages a **Kubernetes-native microservices architecture** for scalability and efficiency.

## 🚀 Getting Started

To get the application up and running on your local machine, please follow the detailed instructions in our **[Operations & Setup Guide](.docs/technical/operations.md)**.

Briefly:

1. Configure your `.env` file.
2. Set up a local Kind cluster with `./gitops/gitops.sh setup`.
3. Build and deploy with `./gitops/gitops.sh build` and `./gitops/gitops.sh apply local`.

## 📖 Documentation

For detailed information, please refer to the following documents:

1. **[GitOps Workflow](.docs/technical/devops/gitops.md)**  
   Local cluster setup (Kind), and GitOps workflow.

2. **[Architecture & Technology Stack](.docs/technical/crucial/architecture.md)**  
   Overview of microservices, tech stack, and key technical capabilities.

3. **[API Guide & Integration](.docs/technical/crucial/api-guide.md)**  
   Authentication, common response formats, and technical implementation details.

4. **[GCP Initial Setup](.docs/technical/devops/gcp-initial-setup.md)**  
   GCP first-time setup guide (GKE, Artifact Registry, Static IP).

5. **[GCP Deployment](.docs/technical/devops/gcp-deployment.md)**  
   Deployment guide for GCP production environment.

6. **[User Flows](.docs/product/user-flows.md)**  
   Detailed user journeys for passengers and drivers.

## Contributors

- Hikmat Samadov (<Samadov@students.uni-marburg.de>)
- Vipul Singh (<singhv@students.uni-marburg.de>)

## License

This project is licensed under the Apache 2.0 License.
