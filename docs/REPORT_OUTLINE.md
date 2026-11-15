# Academic Activity Report

## Library Management System – Automated CI/CD Pipeline and Kubernetes Deployment

**Submitted in partial fulfilment of the requirements for 5th Semester B.E. in Computer Science and Engineering (Cyber Security)**  
**Course Code:** DevOps (24CYAEC59)  
**Institution:** Department of Computer Science & Engineering, M. S. Ramaiah Institute of Technology, Bangalore  

**Student Name:** Raj Aryan  
**USN:** `<USN>`  
**Faculty Guide:** `<GUIDE NAME>`  

---

## BONAFIDE CERTIFICATE

Certified that the project work titled **"Library Management System – Automated CI/CD Pipeline and Kubernetes Deployment"** is a bonafide work carried out by **Raj Aryan** in partial fulfilment for the award of Bachelor of Engineering in Computer Science and Engineering (Cyber Security) of Visvesvaraya Technological University, Belagavi during the academic year 2026–2027.

<br><br>
__________________________ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; __________________________  
**Signature of Candidate** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Signature of Guide**  

---

## Evaluation Rubric & Component Mapping

| Component | Title | Tools Leveraged | Weightage |
|---|---|---|---|
| **Component 1** | Automated Continuous Integration Pipeline | Git, GitHub, Gradle Wrapper, Spring Boot 3.4.3, JUnit 5, Jenkins LTS Docker | 50% |
| **Component 2** | Configuration Management, Containerization & Orchestration | Ansible, Docker Desktop, Kubernetes (Minikube), Spring Boot Actuator | 50% |

### Detailed Marks Breakdown
1. **Concept Understanding (2 Marks):** Theoretical grasp of CI/CD, agentless architecture, container isolation, and desired-state orchestration.
2. **Core Implementation (2 Marks):** Verified execution of backend REST endpoints, Jenkinsfile pipeline stages, Ansible playbook, and Kubernetes manifests.
3. **Enhancement (2 Marks):** Automated pre-build test enforcement, non-root Docker security (`appuser`), rolling updates with rollback, and macOS PID process management.
4. **Analysis & Interpretation (2 Marks):** Evaluation of probe behaviors, scaling characteristics, idempotency proofs, and architectural tradeoffs.
5. **Documentation & Presentation (2 Marks):** Comprehensive report, viva preparation, and GitHub repository hygiene.

---

# COMPONENT 1: CI/CD Pipeline using Git, Gradle, and Jenkins

### 1. Abstract
Component 1 implements a fully automated Continuous Integration (CI) pipeline for an enterprise Java/Spring Boot Library Management REST API. Development utilizes a Git feature-branching workflow (`main`, `feature/book-management`, `feature/borrowing-management`, `feature/ci-pipeline`). Automated build and verification are executed by Jenkins LTS running in a custom Docker container, polling the repository for code changes, executing automated JUnit test suites, compiling the application into an executable fat JAR, and publishing fingerprinted build artifacts.

### 2. Implementation Details
- **Spring Boot 3.4.3 Backend:** Layered service design with JPA in-memory H2 database, Jakarta Bean Validation, and custom error mapping (`GlobalExceptionHandler`).
- **Git Branching Strategy:** Isolated feature development followed by non-fast-forward (`--no-ff`) merges into `main`. Intentional merge conflicts were simulated and cleanly resolved.
- **Gradle 8.12.1 Wrapper:** Consistent compilation, dependency resolution, and test reporting (`./gradlew test`, `./gradlew build`).
- **Jenkins Declarative Pipeline:** 4 core stages (`Checkout SCM`, `Test`, `Build`, `Archive Artifacts`) with automated JUnit XML parsing and fingerprint tracking.

### 3. Verification & Evidence
- **Automated Tests:** 10/10 unit and integration tests passed (`BUILD SUCCESSFUL in 4s`).
- **Artifact:** Executable JAR `library-management-api-1.0.0.jar` created (54 MB).
- **Jenkins Execution:** Initialized with admin password, SCM polling active, stage view showing all green stages.
- *[INSERT SCREENSHOT FIG 1.1 to FIG 1.23 as cataloged in docs/SCREENSHOTS.md]*

---

# COMPONENT 2: Configuration Management, Containerization & Orchestration

### 1. Abstract
Component 2 addresses production deployment, configuration management, and microservice orchestration. Ansible automates host provisioning and local execution via a templated control script on macOS. Docker containerizes the application into a minimal non-root image (`library-management-api:1.0.0`). Kubernetes (Minikube) orchestrates the container across 3 replicas, providing declarative self-healing, rolling updates, Actuator-driven readiness/liveness health checks, and horizontal scaling.

### 2. Implementation Details
- **Ansible Automation:** Agentless local playbook execution. Idempotency verified: First run reported `changed=5`, subsequent run reported `changed=0`. Handlers trigger restarts only upon artifact modification.
- **Docker Multi-Arch Containerization:** JRE 17 base runtime (`eclipse-temurin:17-jre`), dedicated non-root user (`appuser`, UID 10001), exposed port 8080.
- **Kubernetes Architecture:** Declarative deployment with 3 replicas, NodePort Service (30080), Spring Boot Actuator health probes (`/actuator/health`), and ConfigMap decoupled environment variables.
- **Resilience & Scaling:** Demonstrations of scaling (`3 -> 5 -> 3`), auto self-healing upon pod deletion, zero-downtime rolling update to `1.1.0`, and instant rollback (`kubectl rollout undo`).

### 3. Verification & Evidence
- **Ansible:** Playbook verified with `ok=9, changed=0` on re-run.
- **Container Security:** `docker exec library-api whoami` confirmed execution as `appuser`.
- **Kubernetes Cluster:** 3/3 Pods in `Running (1/1)` state, Service reachable, rolling update and rollback confirmed.
- **End-to-End Test:** `./scripts/verify.sh` verified all 7 critical API transactions.
- *[INSERT SCREENSHOT FIG 2.1 to FIG 2.18 as cataloged in docs/SCREENSHOTS.md]*

---

## 4. Overall Conclusion & Lessons Learned
The project successfully bridges theoretical software development and DevOps practices. By implementing automated CI checks, container isolation, configuration management idempotency, and container orchestration self-healing, the application achieves high availability, reliability, and reproducible delivery.

### References
1. Davis, J., & Daniels, R. (2016). *Effective DevOps: Building a Culture of Collaboration, Affinity, and Tooling at Scale*. O'Reilly Media.
2. Sharma, S. (2017). *The DevOps Adoption Playbook: A Guide to Adopting DevOps in a Multi-Speed IT Enterprise*. Wiley.
3. Official Documentation: Spring Boot, Gradle, Jenkins, Ansible, Docker, Kubernetes.
