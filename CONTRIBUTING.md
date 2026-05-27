\# 👥 Contributing to Nyas Upasthiti (न्याय उपस्थिति)



Welcome to the Team APEX core repository! This document serves as a reference guide for all team members to ensure uniform coding standards, branch management, and seamless feature deployment.



\---



\## 🚀 1. Quick Project Overview

\*\*Nyas Upasthiti\*\* is a secure, offline-first biometric authentication sandbox engineered with a declarative Jetpack Compose layout framework.

\* \*\*Core Backend Architecture:\*\* SQLite database structures handling localized AES-128 block-cipher data encryption.

\* \*\*Biometric Handling:\*\* Native CameraX abstractions backed by a sandbox expression-matching simulation framework (Liveness Verification Engine).



\---



\## 🛠️ 2. Local Environment Setup



\### Prerequisites

\* \*\*Android Studio:\*\* Ladybug (or newer)

\* \*\*JDK Version:\*\* Ensure Java JDK 17+ is installed locally.

\* \*\*Environment Paths:\*\* If your system command terminal throws errors executing `./gradlew`, verify that your `JAVA\_HOME` path variable points cleanly to your JDK installation path.



\### Build Verification Checklist

Before pushing code modifications to remote branches, always ensure local verification compiles seamlessly:

1\. Fire up your target phone emulator device manager instance (e.g., Pixel 7 AVD).

2\. Clean and compile the debug module via the project root terminal:

&#x20;  ```bash

&#x20;  gradlew installDebug

