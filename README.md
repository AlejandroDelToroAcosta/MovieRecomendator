# AWS-Based Movie Data Platform 

## Universidad de Las Palmas de Gran Canaria
### School of Computer Engineering
#### Bachelor's Degree in Data Science and Engineering 
##### Technology Services for Data Science

**Authors:**
* **Rafael Suárez Saavedra**
* **Alejandro Del Toro Acosta**

---

## Description

This project implements an AWS-based enterprise data platform designed following an **event-driven and loosely coupled architecture**. The system simulates a real-world business data pipeline where data is ingested, stored, processed, and consumed asynchronously.

The platform acts as a **movie information system**. It retrieves movie-related data from the **IMDb public API**, processes this information, and stores it in a graph-based Datamart. The architecture is composed of independent modules that communicate through cloud-managed services, ensuring high scalability and reliability. 

### Core Workflow:

1.  **Data Ingestion (Crawler):** A Java module connects to the IMDb API, retrieving raw movie data in iterative cycles (e.g., year by year).
2.  **Data Lake Storage:** The ingested data is stored as JSON objects in an **Amazon S3** bucket.
3.  **Event Notification:** Once stored, the system publishes a message to an **Amazon SQS** queue containing the S3 object URI.
4.  **Datamart Ingestion (Listener):** A consumer module listens to the SQS queue, retrieves the data from S3, and processes it for the graph model.
5.  **Graph Datamart:** Processed data is inserted into a **Neo4j** database running on a dedicated **Amazon EC2** instance.
6.  **Containerized API:** A **Spring Boot** application, dockerized and hosted on **Amazon EC2**, exposes REST endpoints to query the graph.
7.  **Managed Entry Point:** **Amazon API Gateway** acts as the public entry point, providing a secure, managed interface that routes traffic to the backend while handling URL encoding.

--- 

## Architecture Overview

The following diagram presents the complete system architecture, showing the decoupled ingestion pipeline and the managed API access layer via API Gateway.

![Architecture Diagram](/documentation/architechture_diagram.png)



--- 

## Technical Resources & Stack

### Data Infrastructure
* **Amazon S3:** Acts as the Data Lake layer for raw and intermediate data storage.
* **Amazon SQS:** Asynchronous communication bridge (Message Queue) between ingestion and processing modules.
* **Neo4j (on Amazon EC2):** Analytical graph database used as the Datamart layer for relationship-heavy movie data.

### Computation & API Layer
* **Spring Boot:** Backend framework providing RESTful endpoints. It communicates directly with Neo4j via the Bolt protocol.
* **Docker:** The API is containerized to ensure environment consistency and simplified deployment on EC2.
* **Amazon API Gateway:** A managed HTTP API layer that routes public requests to the private backend, ensuring correct handling of special characters and spaces in queries.

### Tools & Development
* **Terraform (IaC):** The entire infrastructure (Network, SQS, S3, EC2, API Gateway) is provisioned declaratively.
* **AWS SDK for Java:** Used for programmatic interaction with S3 and SQS.
* **Maven:** Project management and build tool for the Java modules.

---

## API Usage

The API is accessible through the **API Gateway** endpoint. To ensure robustness with special characters (such as movie titles or actor names with spaces), the system utilizes **Query Parameters**.

**Base URL:** `https://{api-id}.execute-api.us-east-1.amazonaws.com/api`

| Endpoint | Parameter | Example |
| :--- | :--- | :--- |
| `/test` | N/A | `/api/test` |
| `/movie` | `title` | `/api/movie?title=The%20Matrix` |
| `/actor` | `name` | `/api/actor?name=Grace%20Kelly` |
| `/director` | `name` | `/api/director?name=Alfred%20Hitchcock` |

---

## Deployment & Execution

Follow these steps to deploy the infrastructure and run the pipeline:

### 1. Infrastructure Provisioning
Navigate to the terraform directory and initialize the cloud resources:
```bash
terraform init
terraform apply -target=aws_s3_bucket.data_lake -target=aws_sqs_queue.event_queue
terraform apply # Deploys the rest: EC2s, API Gateway, and Networking
```

### 2. Database Setup (Neo4j)
Once the EC2 for Neo4j is running:
* Access the Neo4j Browser via http://<EC2_NEO4J_IP>:7474.

* Set the password and ensure the Bolt port (7687) is open

### 3. Data Ingestion & Processing (Crawler & Listener)

The data ingestion layer is composed of two independent Java modules: a **Crawler** and a **Datamart Listener**, both executed as isolated Docker containers.

Before running the containers, each module must be packaged into a runnable JAR:

```bash
mvn clean package
```
This command generates the corresponding .jar files inside the target/ directory of each module.

### **Containerized Execution**

Each module includes its own `Dockerfile` and is built into a separate Docker image.  
This ensures **isolation**, **scalability**, and a clear **separation of responsibilities** between ingestion and processing components.

Sensitive configuration parameters such as:

- **AWS credentials**
- **S3 bucket names**
- **SQS queue URLs**
- **Neo4j credentials**

are **not hardcoded** and must be provided through a `.env` file at runtime.


Example .env file:
```dotenv
AWS_ACCESS_KEY_ID=********
AWS_SECRET_ACCESS_KEY=********
AWS_REGION=us-east-1

S3_BUCKET_NAME=movies-datalake
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/xxxx/movies-ingestion-queue

NEO4J_URI=bolt://<NEO4J_EC2_IP>:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=********
```

The containers are executed using:

```bash
docker run --env-file .env crawler-image
docker run --env-file .env datamart-listener-image
```

---

## **4. API Deployment (User Query Layer)**

The user-facing API is implemented as a **Spring Boot REST application**, packaged as a standalone executable JAR.

---

### ** Building the API Module**

```bash
mvn clean package
```

The generated JAR is transferred to the dedicated EC2 instance using `scp`:

```bash
scp -i clave-hoy.pem target/movies-api.jar ubuntu@<API_EC2_PUBLIC_IP>:/home/ubuntu/
```

Once connected to the instance via `SSH`:

```bash
ssh -i key-name.pem ubuntu@<API_EC2_PUBLIC_IP>
```

The API is containerized and executed directly inside the EC2 instance using Docker:

```bash
docker build -t movies-api .
docker run -d -p 80:8080 \
  -e NEO4J_URI=bolt://<NEO4J_EC2_IP>:7687 \
  -e NEO4J_USER=neo4j \
  -e NEO4J_PASSWORD=******** \
  movies-api
```

The API exposes REST endpoints that allow users to query the Neo4j datamart for movies, actors, and directors, acting as the final access layer of the system.

This approach enables a clean separation between infrastructure provisioning (Terraform), application packaging (Maven), and runtime execution (Docker), closely following industry best practices.

---


### 5. CI/CD & DevOps

This project follows DevOps best practices by integrating **Continuous Integration (CI)** through **GitHub Actions**, ensuring code quality, test coverage, and build reliability across all modules.

#### Continuous Integration with GitHub Actions

A centralized CI pipeline is configured to automatically trigger on:
- Push events to `main` and `develop` branches
- Pull Requests targeting `main` and `develop`
- Manual execution via `workflow_dispatch` with custom inputs

The pipeline is responsible for:
- Checking out the repository
- Setting up the Java environment
- Building all Maven modules
- Executing unit tests written with **JUnit**
- Generating and collecting test coverage reports using **JaCoCo**

Each module in the project includes its own unit tests, ensuring correctness and regression prevention across ingestion, processing, and API layers.

#### Test Coverage with JaCoCo

JaCoCo is integrated into the Maven lifecycle and executed during the `verify` phase. After the build:
- Coverage reports are generated for each module
- Reports are uploaded as build artifacts for inspection and traceability

This guarantees visibility into code quality and enforces testing as a first-class citizen of the development process.

The workflow is specified in ci.yaml file.
  
---
### 6. Future Work

Several improvements and extensions are planned to further evolve the architecture and align it with a more cloud-native and serverless approach:

#### Serverless API with AWS Lambda

As a next step, the current Spring Boot API deployed on EC2 could be refactored to run as an **AWS Lambda function**.

This approach would provide:
- Automatic scaling based on demand
- Reduced operational overhead (no server management)
- Pay-per-use cost model
- Better integration with event-driven and serverless AWS services

The Lambda function would directly handle user queries and interact with the Neo4j datamart, acting as a lightweight and scalable query layer.

#### Container Registry with Amazon ECR

Another planned improvement is the introduction of **Amazon Elastic Container Registry (ECR)** to manage Docker images for all application components, including the API.

By pushing images to ECR:
- Manual steps such as `scp` and SSH-based deployments would be eliminated
- EC2 instances could pull versioned images directly from ECR
- CI/CD pipelines could be easily integrated for automated builds and deployments
- Rollbacks and version control of deployments would be simplified

This change would replace the current manual deployment process with a cleaner, more reproducible, and production-ready container workflow.

Together, these enhancements would move the system closer to a fully automated, scalable, and cloud-native architecture.
