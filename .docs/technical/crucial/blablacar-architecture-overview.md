# BlaBlaCar’s System Architecture Overview

BlaBlaCar’s engineering has evolved from a single PHP monolith into a modern, cloud-native microservices platform. Today, the system comprises hundreds of microservices (on the order of ~1,500 services) running in containers on Google Cloud Platform (GCP). These services are organized by business domain – for example, dedicated services handle user profiles, ride matching, payments, ratings, etc. The architecture emphasizes loose coupling and independent deployment, enabling each feature team to develop and deploy their service at its own pace.

## Table 1: BlaBlaCar Technology Stack (Current vs. Legacy)

| Aspect | Current Architecture (circa 2021–2025) | Legacy (Pre-2017) |
| :--- | :--- | :--- |
| **Architecture Style** | Cloud-native microservices (~1,500 services) deployed on Kubernetes (GKE) with service mesh (Istio). Each service has a narrow functional scope (e.g. user service, payment service) and well-defined APIs. | Monolithic PHP application (built with Symfony) running on on-premises servers. Scaling was vertical and coupled, with all features in one codebase. |
| **Core Languages & Frameworks** | Java + Spring Boot for most backend microservices (the standard choice at BlaBlaCar). Some Node.js (TypeScript) services for web front-end rendering (SSR). Python/Go used for infrastructure tooling and data pipelines. | PHP 5/7 for the monolith (Symfony framework). Some components in Ruby (Rails) historically. |
| **Data Storage** | Polyglot persistence: MySQL/MariaDB clusters for core transactional data (deployed via Galera multi-master for HA); Redis for caching and ephemeral data; Elasticsearch for search indexes; Bigtable (NoSQL) for very large-scale logs and time-series (e.g. “user action” history); also some use of Cassandra in the past for distributed data. All database services are managed or hosted in GCP. | Single MySQL database for most data (initially a 1 TB monolithic schema). As load grew, some tables were sharded or replicated. Redis introduced for caching; Elasticsearch for search. No cloud-managed databases – all ran on bare-metal. |
| **Inter-Service Communication** | RESTful APIs and some gRPC between services. An internal API Gateway/orchestration layer ensures clients have a unified entry point. (Historically, BlaBlaCar built a custom “Gateway” using Domain-Driven Design to abstract business logic from data access.) Services are discoverable via Istio’s service mesh and Kubernetes DNS. | Internal APIs within the monolith (function calls) – no network calls since all logic was in one app. Limited external API exposure. Communication with auxiliary workers via RabbitMQ queues. |
| **Async Messaging & Events** | Apache Kafka for streaming and event-driven architecture – used for Change Data Capture (Debezium streams DB changes to Kafka topics) and inter-service event pub/sub. Google Pub/Sub is also leveraged for some data pipelines. Legacy RabbitMQ is still used for certain background jobs, though Kafka/PubSub have taken over many roles. | RabbitMQ as the primary message broker for asynchronous tasks and worker processes. Cron jobs for scheduled tasks. No streaming platform, so data integration between systems was limited. |
| **Deployment & Infrastructure** | Cloud: GCP across multiple regions (to support global users). Apps run on Kubernetes (GKE) containers. Istio service mesh provides traffic routing, resiliency (e.g. retries), and observability. GCP’s load balancers and global network provide high availability and low latency. CI/CD pipelines (with tools like Jenkins/GitLab CI) build Docker images and deploy to K8s clusters. Infrastructure as Code (Terraform & Kubernetes manifests) manages cloud resources. | On-Premises: ~50 bare-metal servers by 2015. Configuration via Chef automation. In 2015, began using CoreOS + rkt containers on identical servers to improve consistency. Orchestration initially via Fleet/etcd (no Kubernetes yet). Custom tools DGR (for building container images) and GGN (for deploying units) were developed and open-sourced. |

## Core Microservices and Domain Responsibilities

BlaBlaCar’s backend is divided into microservices aligned with specific business capabilities. Each core service is functionally scoped – it owns its data and logic for one aspect of the platform. Key microservices include:

*   **Search/Matching Service (Trip Search)** – Handles ride search queries and matching passengers with drivers offering rides. This service indexes available rides (carpool trips) and supports filtering by location, time, etc. It likely uses Elasticsearch or similar for quick geo-searches.
*   **Booking & Trip Management Services** – A set of services manages the lifecycle of a trip listing and booking. They enforce business rules (seat availability, booking confirmations, cancellations) and ensure consistency.
*   **Payment Service** – Responsible for processing payments for bookings and managing financial transactions. Implements strict layered architecture (hexagonal design).
*   **User Profile & Identity Service** – Manages user accounts, profiles, and authentication. Tries into the trust system (ratings, preferences).
*   **Ratings & Review Service** – Stores immutable ratings and reviews, ensuring they are available for profile/trust displays.
*   **Notification Service** – Centralized hub for all user communications (email, SMS, push notifications) triggered by events.
*   **Route & Mapping Service (“Ridemap”)** – Provides geographic computations, route mapping, and distance calculations using OpenStreetMap.
*   **Fraud Detection and Trust Services** – Systems to detect fraudulent behavior or block users with issues, integrating machine-learning models.
*   **Community Support (User Actions) Service** – Logs all user activities for customer support and auditing, backed by Google Bigtable for high volume.
*   **Multimodal Integration Services** – Services to integrate BlaBlaBus and partner train operators into the platform.

## Deployment Model: From On-Premise to Cloud

### On-Premises Era (2006–2018)
BlaBlaCar began with a traditional on-premises deployment, reaching ~50 bare-metal servers by 2015. They transitioned to containerization (CoreOS + rkt) in 2015, achieving ~80% containerization by year-end. This improved deployment time from days to minutes.

### Migration to Cloud (2019 onwards)
In 2019, BlaBlaCar migrated to Google Cloud Platform (GCP) to leverage managed infrastructure (GKE) and GCP's global network. By 2024, they consolidated all workloads (including those previously on AWS) into GCP for operational simplicity.

## Table 2: Timeline of BlaBlaCar’s Architectural Evolution

| Period | Architecture & Infrastructure | Key Developments |
| :--- | :--- | :--- |
| 2006 – 2012 | Early startup stage. | Monolithic PHP web app. |
| 2013 – 2015 | PHP Monolith on on-premises. | Rebuilt on Symfony2. Single MySQL database. Chef for config. |
| 2015 | Containerization Big Bang. | Adopted Docker/rkt and CoreOS. Built in-house tools (DGR, GGN). |
| 2016 – 2017 | SOA begins. | Breaking monolith into microservices (Users, Ratings, etc.). Still on-prem. |
| 2017 | Hybrid Cloud trial. | Launched BlaBlaCar Daily on AWS. Adopted Kubernetes experimentally. |
| 2018 – 2019 | Migration to GCP & K8s. | Core services moved to GKE in GCP. Phased out on-prem DCs. |
| 2020 – 2021 | Microservices at scale. | Monolith phased out. CDC (Debezium/Kafka) used for sync during migration. |
| 2022 – 2023 | Fully Cloud-Native. | ~1,500 microservices. Istio service mesh deployed. |
| 2024 – 2025 | Single-Cloud Consolidation. | Moved remaining AWS workloads to GCP. |

## Infrastructure Stack: Tech Choices and Tools

*   **Backend:** Primarily Java with Spring Boot. Node.js (TypeScript) used for Server-Side Rendering (SSR) of the React SPA. Python/Go for tooling and data pipelines.
*   **Data Storage:** 
    *   **Relational:** MySQL/MariaDB with Galera for HA.
    *   **Caching:** Redis for ephemeral data and session management.
    *   **Search:** Elasticsearch for trip search and geospatial queries.
    *   **NoSQL:** Google Cloud Bigtable for high-write user action logs.
*   **Messaging:** Apache Kafka for CDC and event-driven architecture. RabbitMQ for legacy background jobs.
*   **API Management:** Internal Gateway pattern for domain-driven access; Istio Ingress for external traffic.
*   **CI/CD:** Automated pipelines (Jenkins/GitLab CI) building Docker images for Kubernetes deployment.

## Architectural Patterns and Practices

1.  **Domain-Driven Design (DDD):** Services aligned with bounded contexts using Hexagonal Architecture.
2.  **Service Mesh (Istio):** Uniform routing, observability, and resilience (e.g., pod warm-up for Java services).
3.  **Event-Driven Architecture:** Decoupling services via Kafka and CDC (Debezium).
4.  **Observability:** Prometheus/Grafana for metrics, ELK/GCP for logs, and formalized SLO monitoring.
5.  **Everything as Cattle:** High availability through redundancy and automated failover (Galera, Kubernetes).

## Sources
*   Dolbeau, O. & De Bernardi, B. “From 1 to 20 million users, the technical story of BlaBlaCar.” PHPCon Poland 2015.
*   Salvy, N. “BlaBlaCar: a journey to the cloud.” BlaBlaCar Engineering Blog, May 2021.
*   Beltrame, A. “Less is More: simplifying our cloud setup through consolidation.” BlaBlaCar Engineering Blog, Nov 2024.
*   Guitouni, S. “User Action Service: from the Monolith to the Cloud.” BlaBlaCar Engineering Blog, June 2020.
*   Peng, Y. “Streaming Data out of the Monolith: Building a Highly Reliable CDC Stack.” BlaBlaCar Engineering Blog, June 2018.
*   Wuip, G. “Operating Node.js in Kubernetes at scale at BlaBlaCar.” BlaBlaCar Engineering Blog, July 2022.
*   Gaudet, F. (Senior SRE, BlaBlaCar). KubeFM Podcast – “Solving Cold Starts: Uses Istio to Warm Up Java Pods.” Sep 2025.
*   Fouilleul, M. “The Expendables — Backends High Availability at BlaBlaCar.” BlaBlaCar Engineering Blog, Nov 2017.
*   Kubernetes.io Case Study: “BlaBlaCar Case Study.”
*   Ayeng, M. “Pragmatic Hexagonal Architecture – Balancing Clean Code and Practicality.” BlaBlaCar Engineering Blog, 2023.
