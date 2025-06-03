# eCommerce Microservices Deployment on Kubernetes

This directory contains the necessary configuration files to deploy the eCommerce Microservices application on Kubernetes.

## Configuration Structure

### Centralized Configuration Approach

To simplify configuration management, this project uses a centralized approach:

- **common-config.yaml**: A single ConfigMap that contains:
  - Shared configurations for all microservices (service connections, database, logging)
  - Specific sections for each microservice
  - Configuration variables in properties format

In Deployments, we use:
- Environment variables for critical and specific parameters
- Mounted volumes to access the common ConfigMap

This approach minimizes configuration duplication and facilitates centralized parameter management.

## Directory Structure

```
k8s/
  ├── common-config.yaml          # Centralized ConfigMap for all microservices
  ├── namespace.yaml              # Namespace definition
  ├── api-gateway/                # API Gateway
  │   ├── deployment.yaml         # Deployment configuration
  │   ├── service.yaml            # Service definition
  │   ├── ingress.yaml            # Ingress configuration for external access
  │   └── kustomization.yaml      # Resource organization
  ├── order-service/              # Order Service
  │   ├── deployment.yaml         # Deployment configuration
  │   ├── service.yaml            # Service definition
  │   └── kustomization.yaml      # Resource organization
  ├── favourite-service/          # Favourite Service
  │   ├── deployment.yaml         # Deployment configuration
  │   ├── service.yaml            # Service definition
  │   └── kustomization.yaml      # Resource organization
  └── ... (other services with similar structure)
```

## Deployment Steps

### 1. Create the namespace and common configuration

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/common-config.yaml
```

### 2. Deploy infrastructure services

```bash
kubectl apply -k k8s/service-discovery/
kubectl apply -k k8s/cloud-config/
```

### 3. Deploy API Gateway and microservices

```bash
kubectl apply -k k8s/api-gateway/
kubectl apply -k k8s/order-service/
kubectl apply -k k8s/favourite-service/
# ... continue with other services
```

### 4. Verify deployment

```bash
kubectl get pods -n ecommerce-app
kubectl get services -n ecommerce-app
```

## Application Access

To access the application through the API Gateway:

```bash
kubectl port-forward -n ecommerce-app svc/api-gateway 8080:8080
```

## Configuration Updates

To update the configuration for all services, simply edit the common ConfigMap:

```bash
kubectl edit configmap -n ecommerce-app common-config
```

Or apply a new version:

```bash
kubectl apply -f k8s/common-config.yaml
```

## Monitoring

To check logs for a specific service:

```bash
kubectl logs -n ecommerce-app deployment/[service-name]
```