# 🎬 DAPM – End-to-End Streaming Platform (Backend)

This repository (`dapm-integration-platform`) contains the **backend** for the
DAPM end-to-end streaming process mining platform, developed as part of a
master's thesis.

Each backend instance represents one organization and supports:

- Secure, trusted communication between organizations  
- Streaming data ingestion and anonymization  
- Configuration, build, and execution of distributed streaming pipelines  

The backend uses the **`dapm-pipeline`** project (from previous DAPM theses) as a
library for pipeline orchestration and Processing Element (PE) support.

---

## 🧠 Prerequisites

- Java 21  
- Docker and Docker Compose  
- Git  
- (Optional) Unix-like shell to run `run.sh`

---

## 📁 Related Repositories

To use this backend as intended in the thesis, you should clone:

- **Pipeline Library (previous work)**  
  [`dapm-pipeline`](https://github.com/DAPM-Thesis-25/dapm-pipeline)  
  Provides the core pipeline and annotation-processing functionality used as a
  dependency in this backend.  
  👉 **Clone this and follow its README** to build and install the JARs (locally
  or via JitPack) before running `dapm-integration-platform`.

- **Processing Elements – templates and examples**  
  [`processing-elements-templates`](https://github.com/DAPM-Thesis-25/processing-elements-templates)  
  Example and template Processing Elements (PEs) that can be built and installed
  for use in pipelines.

- **Web Frontend**  
  [`dapm-frontend`](https://github.com/DAPM-Thesis-25/dapm-frontend)  
  React/TypeScript GUI for configuring organizations, pipelines, and interacting
  with the running system.

---

## ⚡ Quick run (backend only)

```bash
git clone https://github.com/DAPM-Thesis-25/dapm-integration-platform.git
cd dapm-integration-platform
./run.sh
