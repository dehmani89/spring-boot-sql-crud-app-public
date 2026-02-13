# CI/CD Deployment to AWS ECR and ECS (AWS Console Guide)

Deploy your Spring Boot Stock API to AWS using the AWS Console with automated CI/CD via GitHub, CodeBuild, and CodePipeline.

## 🏗️ Architecture Overview

```
GitHub → CodePipeline → CodeBuild → ECR → ECS Fargate → Application Load Balancer
```

## 📋 Prerequisites

- AWS account with billing enabled
- GitHub repository with your Spring Boot app
- Basic understanding of Docker and AWS services
- Your Springboot application

## 🚀 Step-by-Step Console Implementation

### Step 1: Create ECR Repository

1. **Navigate to ECR Console**
   - Go to AWS Console → Services → Elastic Container Registry (ECR)
   - Click "Create repository"

2. **Configure Repository**
   - Repository name: `stock-app-registry`
   - Visibility settings: Private
   - Tag immutability: Disabled
   - Scan on push: Enabled (recommended)
   - Click "Create repository"

3. **Note the Repository URI**
   - Copy the URI (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com/stock-app-registry`)
   - This URI will be used in buildspec.yml and task definition

### Step 2: Create CodeBuild Project

1. **Navigate to CodeBuild Console**
    - Go to AWS Console → Services → CodeBuild
    - Click "Create build project"

2. **Project Configuration**
    - Project name: `stock-app-build`
    - Description: "Build project for Stock App"

3. **Source**
    - Source provider: GitHub
    - Repository: Connect to GitHub (authorize if needed) https://docs.aws.amazon.com/dtconsole/latest/userguide/connections-create-github.html
    - Repository in my GitHub account: Select your repository
    - Source version: main

4. **Environment**
    - Environment image: Managed image
    - Operating system: Amazon Linux 2
    - Runtime: Standard
    - Image: aws/codebuild/amazonlinux2-x86_64-standard:4.0
    - Privileged: ✅ Enable (required for Docker)

5. **Service Role**
    - Service role: New service role
    - Role name: `codebuild-stock-app-build-service-role`

6. **Buildspec**
    - Build specifications: Use a buildspec file
    - Buildspec name: buildspec.yml

7. **Environment Variables**
   Add these variables:
   ```
   AWS_DEFAULT_REGION = us-east-1
   AWS_ACCOUNT_ID = [Your AWS Account ID]
   IMAGE_REPO_NAME = stock-app
   IMAGE_TAG = latest
   DOCKER_HUB_USERNAME = [Your Docker Hub Username/Email]
   DOCKER_HUB_PASSWORD = [Your Docker Hub Password] (mark as sensitive)
   ```
   
   **Note**: The buildspec.yml handles ECR authentication automatically using IAM roles.

8. **Click "Create build project"**

9. **Update Service Role Permissions**
    - Go to IAM Console → Roles
    - Find `codebuild-stock-app-build-service-role`
    - Attach additional policies:
        - `AmazonEC2ContainerRegistryPowerUser`
        - `AmazonEC2ContainerRegistryFullAccess`

### Step 3: Create Task Definition

1. **Create Task Definition**
    - In ECS Console → Task definitions
    - Click "Create new task definition"

2. **Task Definition Configuration**
    - Task definition family: `stock-app-task`
    - Launch type: AWS Fargate
    - Operating system: Linux/X86_64
    - CPU: 0.25 vCPU
    - Memory: 0.5 GB

3. **Task Role**
    - Task execution role: Create new role or use `ecsTaskExecutionRole`

4. **Container Definition**
    - Container name: `stock-app`
    - Image URI: `YOUR_REPOSTITORY_LINK_HERE`
    - Port mappings: Add a new Container port 8080, Protocol TCP, App Protocol HTTP

5. **Environment Variables**
   Add these environment variables (CRITICAL for database connectivity):
   ```
   DB_URL = jdbc:postgresql://[RDS-ENDPOINT]:5432/postgres
   DB_USERNAME = stockuser
   DB_PASSWORD = YourSecurePassword123
   ```
   
   **Important**: These must match your RDS configuration exactly.

6. **Logging**
    - Log driver: awslogs
    - Log group: `/ecs/stock-app-task` (will be created automatically)
    - Region: us-east-1
    - Stream prefix: ecs

7. **Click "Create"**


### Step 4: Create ECS Cluster

1. **Navigate to ECS Console**
    - Go to AWS Console → Services → Elastic Container Service (ECS)
    - Click "Create Cluster"

2. **Cluster Configuration**
    - Cluster name: `stock-app-cluster`
    - Infrastructure: AWS Fargate (serverless) [Fargate only]
    - Click "Create"

### Step 5: Create ECS Service

1. **Create Service**
    - In your ECS cluster `stock-app-cluster` → Services tab
    - Click "Create"

2. **Service Configuration**
    - Environmnet
      - Select Launch type Option 
      - under Launch Type drop down Select `Fargate`
      - Platform Version drop down select `LATEST`
    - Service Details
      - Task definition: `stock-app-task:3` or whatever the latest task definition version
      - Service name: `stock-app-service`
      - Number of tasks: 2

3. **Network Configuration**
    - VPC: Same as ALB and RDS (if used otherwise default)
    - Subnets: Select same subnets as ALB (if used otherwise default)
    - Security group: Create new
        - Name: `stock-app-ecs-sg`
        - Inbound Rules:
            - Type: HTTP, Port: 8080, Source: 0.0.0.0/0 (for testing; restrict to ALB in production)
        - Outbound Rules:
            - Type: All traffic, Destination: 0.0.0.0/0
            - Type: PostgreSQL, Port: 5432, Destination: RDS security group
    - Auto-assign public IP: Enabled

4. **Load Balancer** (Optional - for production use)
    - Load balancer type: None (for testing)
    - For production: Create Application Load Balancer
        - Load balancer: `stock-app-alb`
        - Container to load balance: `stock-app:8080`
        - Target group: `stock-app-targets`

5. **Click "Create"**

6. **Update Security Groups**
    - Go to EC2 → Security Groups
    - Edit `stock-app-db-sg` inbound rules
    - Change source to `stock-app-ecs-sg` for PostgreSQL rule

### Step 6: Create CodePipeline

1. **Navigate to CodePipeline Console**
    - Go to AWS Console → Services → CodePipeline
    - Click "Create pipeline"
    - Category: "Select Build Custom Pipeline"

2. **Pipeline Settings**
    - Pipeline name: `stock-app-pipeline`
    - Service role: New service role
    - Artifact store: Default location (or specify your S3 bucket)

2. **Pipeline Settings**
    - Pipeline name: `stock-app-pipeline`
    - Service role: New service role
    - Artifact store: Default location (or specify your S3 bucket)

3. **Source Stage**
    - Source provider: GitHub (Version 2)
    - Connection: Create new connection to GitHub [or use an existing one if already created]
    - Repository name: `yourusername/spring-boot-sql-crud-app`
    - Branch name: main
    - Output artifact format: CodePipeline default

4. **Build Stage**
    - Select "Other build provider"
    - Build provider: AWS CodeBuild
    - Project name: `stock-app-build`
    - Build type: Single build

5. **Deploy Stage**
    - Deploy provider: Amazon ECS
    - Cluster name: `stock-app-cluster`
    - Service name: `stock-app-service`
    - Image definitions file: `imagedefinitions.json`

6. **Review and Create**
    - Review all settings
    - Click "Create pipeline"


### Step 7: Create RDS PostgreSQL Database

1. **Navigate to RDS Console**
   - Go to AWS Console → Services → RDS
   - Click "Create database"

2. **Database Configuration**
   - Engine type: PostgreSQL
   - Version: PostgreSQL 15.4-R2 (or latest)
   - Templates: Free tier (for testing)
   - DB instance identifier: `stock-app-db`
   - Master username: `stockuser`
   - Master password: `Qwerty89!`

3. **Instance Configuration**
   - DB instance class: db.t3.micro
   - Storage type: General Purpose SSD (gp2)
   - Allocated storage: 20 GB

4. **Connectivity**
   - VPC: Default VPC (or create new)
   - Subnet group: Default
   - Public access: Yes [mainly because we are simply testing for now, for prod environment, this should be no and configure VPC access or enable IP specific access]
   - VPC security group: Create new
   - Security group name: `stock-app-db-sg`

5. **Additional Configuration**
   - Initial database name: `postgres`
   - Click "Create database"

6. **Update Security Group**
   - Go to EC2 Console → Security Groups
   - Find `stock-app-db-sg`
   - Edit inbound rules → Add rule:
   - Type: PostgreSQL, Port: 5432, Source: `stock-app-ecs-sg` (ECS security group)
   
   **Critical**: This allows ECS tasks to connect to RDS database.

### Step 8: Initialize Database Schema

1. **Connect to RDS**
    - Use a bastion host or RDS Query Editor
    - Connect to your PostgreSQL instance

2. **Create Schema and Table**
```sql
CREATE SCHEMA IF NOT EXISTS trading;

CREATE TABLE trading.stocks (
    id SERIAL PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    stock_price DECIMAL(10, 2) NOT NULL,
    description TEXT
);

INSERT INTO trading.stocks (ticker, company_name, stock_price, description)
VALUES
    ('AAPL', 'Apple Inc.', 190.25, 'Technology company specializing in consumer electronics.'),
    ('GOOGL', 'Alphabet Inc.', 135.80, 'Parent company of Google and related businesses.'),
    ('AMZN', 'Amazon.com Inc.', 145.12, 'E-commerce and cloud computing leader.'),
    ('MSFT', 'Microsoft Corp.', 365.50, 'Developer of software and cloud solutions.');
```

### Step 9: Test the Deployment

1. **Trigger Pipeline**
   - Make a code change and push to GitHub
   - Pipeline should automatically trigger
   - Monitor progress in CodePipeline console

2. **Verify ECS Deployment**
   - Check ECS service tasks are running
   - View CloudWatch logs for any errors
   - Ensure tasks have public IPs assigned

3. **Test API**
   ```bash
   # Get ECS task public IP from ECS console
   curl http://[TASK-PUBLIC-IP]:8080/api/stocks
   ```
### Step 10: Add OKTA Security

1. **Update Task Definition Environment Variables**
   - Go to ECS Console → Task definitions → stock-app-task
   - Create new revision
   - Add environment variables:
   ```
   OKTA_ISSUER_URI = https://your-okta-domain.okta.com/oauth2/default
   OKTA_AUDIENCE = api://default
   ```

2. **Update ECS Service**
   - Go to ECS Console → Clusters → stock-app-cluster → Services
   - Update service to use new task definition revision
   - Wait for deployment to complete

3. **Test Security**
   ```bash
   # Public endpoints (no token required)
   curl http://[TASK-IP]:8080/api/basic/hello
   curl http://[TASK-IP]:8080/api/basic/health
   
   # Protected endpoints (JWT token required)
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://[TASK-IP]:8080/api/stocks
   ```

4. **Configure OKTA Application**
   - Set up your OKTA application with correct audience
   - Generate JWT tokens for testing
   - Update issuer URI and audience values in task definition

### Step 11: Deploy to Amazon EKS (Alternative to ECS [Optional])

**Prerequisites:**
- Install kubectl and eksctl on your local machine
- AWS CLI configured with appropriate permissions

1. **Create EKS Cluster**
   ```bash
   # Create EKS cluster (takes 15-20 minutes)
   eksctl create cluster \
     --name stock-app-cluster \
     --region us-east-1 \
     --nodegroup-name stock-app-nodes \
     --node-type t3.medium \
     --nodes 2 \
     --nodes-min 1 \
     --nodes-max 3
   ```

2. **Create Kubernetes Manifests**
   Create `k8s/` directory in your repository with these files:

   **k8s/namespace.yaml:**
   ```yaml
   apiVersion: v1
   kind: Namespace
   metadata:
     name: stock-app
   ```

   **k8s/deployment.yaml:**
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: stock-app
     namespace: stock-app
   spec:
     replicas: 2
     selector:
       matchLabels:
         app: stock-app
     template:
       metadata:
         labels:
           app: stock-app
       spec:
         containers:
         - name: stock-app
           image: YOUR_REPOSTITORY_LINK_HERE
           ports:
           - containerPort: 8080
           env:
           - name: DB_URL
             value: "jdbc:postgresql://stock-app-db.cemupq078xab.us-east-1.rds.amazonaws.com:5432/postgres"
           - name: DB_USERNAME
             value: "stockuser"
           - name: DB_PASSWORD
             value: "Qwerty89!"
           - name: OKTA_ISSUER_URI
             value: "https://your-okta-domain.okta.com/oauth2/default"
           - name: OKTA_AUDIENCE
             value: "api://default"
   ```

   **k8s/service.yaml:**
   ```yaml
   apiVersion: v1
   kind: Service
   metadata:
     name: stock-app-service
     namespace: stock-app
   spec:
     selector:
       app: stock-app
     ports:
     - port: 80
       targetPort: 8080
     type: LoadBalancer
   ```

3. **Update CodePipeline for EKS**
   - Modify deploy stage in CodePipeline:
   - Deploy provider: Amazon EKS
   - Cluster name: `stock-app-cluster`
   - Service role: Create EKS service role with required permissions

4. **Deploy to EKS**
   ```bash
   # Apply Kubernetes manifests
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   
   # Get service URL
   kubectl get service stock-app-service -n stock-app
   ```

5. **Update Security Groups**
   - Find EKS node group security group
   - Add outbound rule for PostgreSQL (port 5432) to RDS
   - Update RDS security group to allow inbound from EKS nodes

6. **Test EKS Deployment**
   ```bash
   # Get external IP
   kubectl get service stock-app-service -n stock-app
   
   # Test API
   curl http://[EXTERNAL-IP]/api/stocks
   ```

**EKS vs ECS Comparison:**
- **EKS**: More control, Kubernetes-native, better for complex orchestration
- **ECS**: Simpler setup, AWS-native, better for straightforward containerization
- **Cost**: EKS has cluster management fee (~$73/month), ECS Fargate is pay-per-use


## 🔧 Troubleshooting

### Common Issues:

1. **Database Connection Failed**
   - Verify environment variables in ECS task definition
   - Check security group rules (ECS → RDS on port 5432)
   - Ensure both ECS and RDS are in same VPC

2. **Docker Pull Rate Limit**
   - Ensure Docker Hub credentials are set in CodeBuild
   - Verify buildspec.yml includes Docker Hub login

3. **ECS Tasks Not Starting**
   - Check CloudWatch logs for container errors
   - Verify ECR image exists and is accessible
   - Ensure task definition has correct image URI

4. **Pipeline Failures**
   - Check CodeBuild logs for build errors
   - Verify IAM permissions for CodeBuild service role
   - Ensure buildspec.yml is in repository root

## 🎯 Next Steps

- Add Application Load Balancer for production
- Implement HTTPS with SSL certificates
- Set up CloudWatch monitoring and alarms
- Configure auto-scaling policies
- Implement blue/green deploymentsFT', 'Microsoft Corp.', 365.50, 'Developer of software and cloud solutions.');
```

### Step 9: Create Required Files in GitHub

1. **Create buildspec.yml** in repository root:

```yaml
version: 0.2

phases:
  pre_build:
    commands:
      - echo Logging in to Docker Hub...
      - docker login --username $DOCKER_HUB_USERNAME --password $DOCKER_HUB_PASSWORD
      - echo Logging in to Amazon ECR...
      - aws --version
      - REPOSITORY_URI=YOUR_REPOSTITORY_LINK_HERE
      - aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REPOSITORY_URI
      - COMMIT_HASH=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)
      - IMAGE_TAG=build-$(echo $CODEBUILD_BUILD_ID | awk -F":" '{print $2}')
  build:
    commands:
      - echo Build started on `date`
      - echo Current directory is $(pwd)
      - echo Listing files in current directory
      - ls -la
      - echo Checking if Dockerfile exists
      - test -f Dockerfile && echo "Dockerfile found" || echo "Dockerfile NOT found"
      - echo Building the Docker image...
      - docker build -t $REPOSITORY_URI:latest .
      - docker tag $REPOSITORY_URI:latest $REPOSITORY_URI:$IMAGE_TAG
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing the Docker images...
      - docker push $REPOSITORY_URI:latest
      - docker push $REPOSITORY_URI:$IMAGE_TAG
      - echo Writing image definitions file...
      - DOCKER_CONTAINER_NAME=stock-app
      - printf '[{"name":"%s","imageUri":"%s"}]' $DOCKER_CONTAINER_NAME $REPOSITORY_URI:$IMAGE_TAG > imagedefinitions.json
      - echo $DOCKER_CONTAINER_NAME
      - echo printing imagedefinitions.json
      - cat imagedefinitions.json

artifacts:
  files:
    - imagedefinitions.json
```

2. **Commit and push** to your GitHub repository



## 🧪 Testing Your Deployment

### 1. Test the Pipeline
1. **Trigger Pipeline**
   - Make a small change to your code
   - Commit and push to GitHub
   - Pipeline should trigger automatically

2. **Monitor Pipeline**
   - Go to CodePipeline Console
   - Watch the pipeline execution
   - Check each stage for success/failure

### 2. Test the Application
1. **Test API Endpoints**
```bash
# you can use the Swagger API
curl http://localhost:8080/swagger-ui/index.html
# Get all stocks
curl http://[ALB-DNS-NAME]/api/stocks

# Get specific stock
curl http://[ALB-DNS-NAME]/api/stocks/AAPL

# Create new stock
curl -X POST http://[ALB-DNS-NAME]/api/stocks \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "TSLA",
    "companyName": "Tesla Inc.",
    "price": 250.75,
    "description": "Electric vehicle company"
  }'
```

### 3. Monitor Services

1. **ECS Service Health**
   - Go to ECS Console → Clusters → stock-app-cluster
   - Check service status and task health

2. **CloudWatch Logs**
   - Go to CloudWatch Console → Log groups
   - Check `/ecs/stock-app` for application logs

3. **Load Balancer Health**
   - Go to EC2 Console → Target Groups
   - Check `stock-app-targets` for healthy targets

## 🔧 Console Navigation Quick Reference

| Service | Console Path | Purpose |
|---------|-------------|---------|
| ECR | Services → ECR | Container registry |
| ECS | Services → ECS | Container orchestration |
| RDS | Services → RDS | Database management |
| EC2 | Services → EC2 | Load balancers, security groups |
| CodeBuild | Services → CodeBuild | Build automation |
| CodePipeline | Services → CodePipeline | CI/CD orchestration |
| IAM | Services → IAM | Roles and permissions |
| CloudWatch | Services → CloudWatch | Monitoring and logs |

## 🚨 Troubleshooting

### Pipeline Fails
1. **Check CodeBuild Logs**
   - CodeBuild Console → Build projects → stock-app-build
   - Click on failed build → View logs

2. **Common Issues**
   - Missing environment variables
   - Insufficient IAM permissions
   - Docker build failures

### ECS Service Issues
1. **Check Service Events**
   - ECS Console → Clusters → Services → Events tab
   - Look for deployment or health check failures

2. **Check Task Logs**
   - CloudWatch Console → Log groups → /ecs/stock-app
   - Review application startup logs

### Database Connection Issues
1. **Security Group Rules**
   - Ensure ECS security group can reach RDS on port 5432
   - Check RDS security group inbound rules

2. **Environment Variables**
   - Verify RDS endpoint in task definition
   - Check database credentials

## 💰 Cost Optimization Tips

- Use Fargate Spot for development environments
- Set up auto-scaling policies
- Use smaller RDS instances for testing
- Monitor CloudWatch costs

## 🔒 Security Best Practices

- Store database passwords in AWS Secrets Manager
- Use least privilege IAM policies
- Enable VPC Flow Logs
- Configure security groups with minimal access
- Enable ALB access logs

## 🎯 Next Steps

1. **Set up monitoring dashboards**
2. **Implement auto-scaling (HPA for EKS, Service auto-scaling for ECS)**
3. **Add blue/green deployments**
4. **Configure backup strategies**
5. **Set up CloudWatch alarms**
6. **Choose between ECS and EKS based on your requirements**

Your Spring Boot Stock API is now deployed on AWS with full CI/CD automation through the AWS Console! Every GitHub push will trigger a new deployment.