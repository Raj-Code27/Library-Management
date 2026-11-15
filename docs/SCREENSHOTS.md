# Exact Screenshots Guide for College Report & Viva

Use this exact mapping to take your 42 screenshots. Each screenshot corresponds directly to an executed step in the project.

---

## Component 1: CI/CD Pipeline using Git, Gradle and Jenkins

| Fig. # | Title / Content to Capture | How to Capture |
|---|---|---|
| **Fig 1.1** | Tool Versions (`java -version`, `git --version`, `docker --version`, `brew --version`) | Open terminal and run `java -version && git --version && docker --version` |
| **Fig 1.2** | Automated Tests Execution (`./gradlew test`) | Terminal showing `BUILD SUCCESSFUL` with all 10 unit/integration tests passed |
| **Fig 1.3** | Executable JAR Output (`./gradlew build && ls -lh build/libs/`) | Terminal showing `library-management-api-1.0.0.jar` created |
| **Fig 1.4** | Spring Boot Application Startup Log | Terminal showing Spring Boot ASCII banner and `Tomcat started on port 8080` |
| **Fig 1.5** | Browser / Curl `GET /books` JSON Output | Browser or terminal displaying the 8 seeded books |
| **Fig 1.6** | Book Creation (`POST /books` returning `201 Created` with Location header) | Postman / terminal curl showing `201` status |
| **Fig 1.7** | Borrowing Request (`POST /borrowings`) | Response showing status `BORROWED` and `availableCopies` decreased |
| **Fig 1.8** | Return Request (`PUT /borrowings/{id}/return`) | Response showing status `RETURNED` and `availableCopies` restored |
| **Fig 1.9** | Business Rule Rejections (409 Conflict & 400 Validation) | Terminal showing `409` on zero copies and `400` on validation errors |
| **Fig 1.10** | Spring Boot Actuator Health (`/actuator/health`) | Browser showing `{"status":"UP"}` |
| **Fig 1.11** | GitHub Repository Home Page | Browser view of `library-management-devops` showing repo files and branch count |
| **Fig 1.12** | GitHub Branches List | GitHub UI showing `main`, `feature/book-management`, `feature/borrowing-management`, `feature/ci-pipeline` |
| **Fig 1.13** | Git Graph Log (`git log --oneline --graph --all`) | Terminal showing branching, commits, and clean merge history |
| **Fig 1.14** | Merge Conflict Resolution Demonstration | Terminal showing conflict markers `<<<<<<<` and successful resolved merge |
| **Fig 1.15** | Jenkins Dashboard | Jenkins UI at `http://localhost:8081` showing pipeline `LibraryManagement-CI` |
| **Fig 1.16** | Jenkins Job Configuration | UI showing Pipeline script from SCM, Git repo URL, branch `*/main`, and Poll SCM |
| **Fig 1.17** | Successful Pipeline Stage View | Stage View showing all 5 stages in green (`Checkout SCM`, `Test`, `Build`, `Archive Artifacts`, `Post Actions`) |
| **Fig 1.18** | Jenkins Console Output | Terminal / UI showing checkout, `./gradlew clean test`, and build logs |
| **Fig 1.19** | Jenkins Archived Artifacts Page | Build summary page showing archived `library-management-api-1.0.0.jar` and fingerprint |
| **Fig 1.20** | Jenkins Automatic Trigger (Poll SCM) | Build cause showing: "Started by an SCM change" |
| **Fig 1.21** | Intentional Failure Pipeline | Jenkins stage view showing red Test stage and skipped Build/Archive stages |
| **Fig 1.22** | Fixed Pipeline Restored Green | Stage view showing green build following the test fix |
| **Fig 1.23** | Jenkins JUnit Test Results / Trend | Jenkins Test Result page showing 10 passing tests |

---

## Component 2: Configuration Management using Ansible, Docker and Kubernetes

| Fig. # | Title / Content to Capture | How to Capture |
|---|---|---|
| **Fig 2.1** | Ansible Version & Syntax Check (`ansible --version`, `--syntax-check`) | Terminal showing clean syntax check |
| **Fig 2.2** | First Ansible Playbook Run | Terminal showing `changed=5` and handler `Restart Library API` executed |
| **Fig 2.3** | Second Ansible Playbook Run (Idempotency) | Terminal showing `changed=0`, proving complete idempotency |
| **Fig 2.4** | Ansible Deployed Directory | `ls -la ~/library-api-deploy` showing JAR, `app.pid`, `run-app.sh`, and `logs/` |
| **Fig 2.5** | Docker Image Build (`docker build -t library-management-api:1.0.0 .`) | Terminal showing layer caching, non-root user setup, and `docker images` |
| **Fig 2.6** | Docker Container Running Non-Root | `docker ps` and `docker exec library-api whoami` showing `appuser` (not root) |
| **Fig 2.7** | API Verification against Container | Curl commands against container port `8080` |
| **Fig 2.8** | Minikube Cluster Status (`minikube status`, `kubectl get nodes`) | Terminal showing control-plane node `Ready` |
| **Fig 2.9** | Kubernetes Resources Applied | `kubectl get deployments,pods,services,configmaps` showing 3/3 pods Running |
| **Fig 2.10** | Pod Liveness and Readiness Probes | `kubectl describe deployment library-api` showing probe settings |
| **Fig 2.11** | Minikube Service URL / Port Forward Access | Terminal showing response via NodePort / tunnel |
| **Fig 2.12** | Replica Scaling Up: 3 → 5 Pods | `kubectl scale deployment library-api --replicas=5` and `kubectl get pods` |
| **Fig 2.13** | Replica Scaling Down: 5 → 3 Pods | `kubectl scale deployment library-api --replicas=3` and `kubectl get pods` |
| **Fig 2.14** | Kubernetes Self-Healing Demonstration | `kubectl delete pod <pod-name>` followed by immediate recreation |
| **Fig 2.15** | Rolling Update in Progress (`1.0.0` → `1.1.0`) | `kubectl rollout status deployment/library-api` showing rolling replacement |
| **Fig 2.16** | Application Version Verification after Rollout | `/actuator/info` showing updated version `1.1.0` |
| **Fig 2.17** | Rollback Execution (`kubectl rollout undo`) | `kubectl rollout undo deployment/library-api` and old ReplicaSet restored |
| **Fig 2.18** | End-to-End Automated Verification Script | Execution of `./scripts/verify.sh` showing all 7 automated checks passing |
