# Comprehensive DevOps Viva Q&A Guide (45+ Questions & Answers)

This guide is designed for viva examination, faculty evaluation, and technical portfolio reviews for the **Library Management System DevOps Project**.

---

## 1. DevOps Fundamentals & CI/CD Concepts

### Q1: What is DevOps and why is it needed?
**A:** DevOps is a cultural and technical practice combining software development (Dev) and IT operations (Ops). Its goal is to shorten the systems development life cycle, enable high deployment frequency, reduce failure rates, and provide dependable, automated delivery.

### Q2: What is the difference between Continuous Integration (CI), Continuous Delivery (CD), and Continuous Deployment?
**A:**
- **Continuous Integration (CI):** Developers frequently merge code into a central repository; automated builds and tests verify each commit to detect defects early.
- **Continuous Delivery (CD):** CI plus automated preparation of production-ready artifacts, where deployment to production can be triggered manually.
- **Continuous Deployment:** Every code change that passes all automated tests is deployed to production automatically without human intervention.

### Q3: Why is Jenkins SCM Polling used here instead of GitHub Webhooks?
**A:** GitHub Webhooks require the Jenkins server to be directly reachable over a public IP or DNS endpoint. Because Jenkins is running locally inside a Docker container on a macOS developer laptop behind NAT/firewall, GitHub cannot send inbound HTTP POST payloads. SCM polling (`H/1 * * * *`) allows Jenkins to initiate outbound requests to check for new commits.

---

## 2. Git & Version Control

### Q4: Explain the branching strategy used in this project.
**A:** We use a feature-branch workflow with 4 key branches:
1. `main`: Production-stable branch built by Jenkins.
2. `feature/book-management`: Dedicated to Book domain models, repositories, controllers, and services.
3. `feature/borrowing-management`: Dedicated to borrowing transactions, business rules, and error handlers.
4. `feature/ci-pipeline`: Dedicated to `Jenkinsfile` and container CI setup.

### Q5: How does a merge conflict occur and how is it resolved?
**A:** A merge conflict occurs when two branches modify the exact same lines in a file, or one branch deletes a file that another modified. Git cannot automatically determine which change to keep, inserting conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`). The developer manually inspects the conflicting blocks, selects the correct code, removes the markers, stages the file with `git add`, and commits the resolution.

---

## 3. Build Automation with Gradle

### Q6: Why use the Gradle Wrapper (`./gradlew`) instead of a globally installed Gradle?
**A:** The Gradle Wrapper ensures every developer and CI environment builds the project with the exact same Gradle version and runtime configuration, eliminating the "works on my machine" problem without requiring pre-installed build tools on the host.

### Q7: What is an executable "fat" (or "uber") JAR?
**A:** A fat JAR bundles both the compiled application bytecode and all third-party dependencies (plus an embedded servlet container like Apache Tomcat) into a single self-contained archive runnable with `java -jar`.

### Q8: What does task "up-to-date" mean in Gradle?
**A:** Gradle checks the inputs and outputs of each task. If the inputs (e.g. `.java` files) and outputs (e.g. `.class` files) have not changed since the last execution, Gradle skips the task and marks it `UP-TO-DATE`, accelerating build times.

---

## 4. Backend Architecture & Spring Boot

### Q9: Why use layered architecture with DTOs instead of exposing JPA entities?
**A:** Layered architecture (Controller → Service → Repository) enforces separation of concerns. DTOs (Data Transfer Objects) decouple internal database schemas from external API contracts, prevent accidental over-posting or mass-assignment vulnerabilities, and allow API payload validation without polluting persistence entities.

### Q10: How are borrowing and return operations kept safe?
**A:** They are annotated with `@Transactional`. If an exception occurs (e.g., zero copies available or database failure), the entire operation rolls back, preventing inconsistency between the book copy count and the borrowing status.

### Q11: Why is H2 in-memory database used, and what is its primary limitation in Kubernetes?
**A:** H2 in-memory is zero-configuration and runs embedded in the JVM without external infrastructure. However, each Kubernetes Pod runs its own independent JVM and memory space. Therefore, in a multi-replica deployment, Pod 1 and Pod 2 maintain separate in-memory databases. In production, a shared external database like PostgreSQL with persistent storage would be used.

---

## 5. Configuration Management with Ansible

### Q12: What does "agentless" mean in Ansible?
**A:** Unlike Chef or Puppet, Ansible requires no custom daemon or agent software installed on target nodes. It executes tasks over standard SSH (or local process execution via `ansible_connection=local`) using standard Python interpreters.

### Q13: What is idempotency and how did you prove it?
**A:** An operation is idempotent if executing it multiple times produces the exact same end result without unintended side effects. In our playbook, running the playbook for the first time reported `changed=5`, while running it a second and third time reported `changed=0`, proving state consistency.

### Q14: Why did we use a templated control script instead of the Ansible `systemd` module?
**A:** macOS does not use `systemd` or `systemctl`; it uses `launchd`. Using the Ansible `systemd` module on macOS would fail. Hence, a templated shell script (`run-app.sh.j2`) managing process lifecycle via PID files was used.

---

## 6. Containerization with Docker

### Q15: What is the difference between a Docker image and a Docker container?
**A:** A Docker image is a read-only, immutable template containing the application code, libraries, runtime, and configuration. A Docker container is a runnable, isolated instance of an image with a writable top layer.

### Q16: Why should containerized processes never run as `root`?
**A:** Running as `root` inside a container exposes the host system to privilege escalation attacks if a container escape vulnerability is exploited. Our Dockerfile creates a non-root dedicated system user (`appuser`, UID `10001`) with restricted permissions.

### Q17: Why use Eclipse Temurin JRE instead of JDK for the runtime container?
**A:** The Java Runtime Environment (JRE) contains only what is needed to run compiled bytecode. The JDK contains compilers, debugging tools, and profilers which are unnecessary in production and needlessly inflate image size and attack surface.

---

## 7. Kubernetes Orchestration

### Q18: What is a Kubernetes Pod?
**A:** A Pod is the smallest deployable compute unit in Kubernetes. It encapsulates one or more containers that share network namespaces, storage volumes, and IP addresses.

### Q19: What is the difference between a Deployment and a ReplicaSet?
**A:** A ReplicaSet ensures that a specified number of identical Pod replicas are running at all times. A Deployment is a higher-level controller that manages ReplicaSets, enabling declarative updates, rolling deployments, version tracking, and rollbacks.

### Q20: Explain the difference between Liveness and Readiness probes.
**A:**
- **Readiness Probe:** Determines whether a container is ready to accept user traffic. If it fails, Kubernetes stops routing Service traffic to that Pod without killing it.
- **Liveness Probe:** Determines if the application container is still healthy and operational. If it fails, Kubernetes automatically terminates and restarts the container to achieve self-healing.

### Q21: What are `maxSurge` and `maxUnavailable` in RollingUpdate?
**A:**
- `maxSurge`: Maximum number of Pods that can be scheduled above the desired replica count during an update (e.g., `maxSurge: 1` means up to 4 pods can run concurrently if desired count is 3).
- `maxUnavailable`: Maximum number of Pods that can be unavailable during the update (e.g., `maxUnavailable: 1` means at least 2 pods will always be ready to serve traffic).

### Q22: Why does Minikube need `minikube service --url` or `port-forward` on macOS?
**A:** On macOS, the Docker daemon runs inside a lightweight virtual machine. The NodePort allocated on the Minikube node is internal to the Docker bridge network and not directly routed on the macOS host network. `minikube service --url` or `kubectl port-forward` creates a network tunnel routing host traffic into the cluster.
