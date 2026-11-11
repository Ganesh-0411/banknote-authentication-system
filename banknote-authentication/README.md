# DEEP LEARNING-BASED BANKNOTE AUTHENTICATION SYSTEM USING CNN AUTOENCODER AND ANOMALY DETECTION

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![DeepLearning4J](https://img.shields.io/badge/DL4J-1.0.0--M2.1-blue.svg)](https://deeplearning4j.konduit.ai/)
[![ND4J](https://img.shields.io/badge/ND4J-Native%20Platform-blue.svg)](https://eclipse.dev/deeplearning4j/)
[![OpenCV](https://img.shields.io/badge/OpenCV%20Java-4.7.0-red.svg)](https://opencv.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

An enterprise-grade, Java-based banknote authentication and anomaly screening web application. The system learns the visual characteristics, Guilloché geometry, and security features of genuine Indian currency notes using a **CNN Convolutional Autoencoder** in **DeepLearning4J (DL4J)** and **ND4J**, with image processing handled natively by **OpenCV Java**.

---

## 1. Executive Summary & Anomaly Detection Philosophy

### The One-Class Anomaly Detection Paradigm
Conventional banknote classification systems attempt to train binary classifiers (Genuine vs. Fake). However, in real-world currency security:
1. **Unbounded Negative Class:** Counterfeit techniques evolve constantly; training on a fixed set of fake samples overfits to known counterfeits and misses unseen falsifications.
2. **Dataset Asymmetry:** High-resolution genuine currency samples are accessible; authentic counterfeit samples are legally restricted and rare.

**Solution:** This application implements **One-Class Reconstruction-Based Anomaly Detection**:
- The CNN Autoencoder is trained **exclusively on genuine Indian banknotes**.
- When presented with a banknote, the model attempts to reconstruct it through a compressed latent bottleneck.
- If the banknote is genuine, the reconstruction Mean Squared Error (**MSE**) is low ($\le \tau$).
- If anomalous patterns, altered ink, counterfeit security threads, or missing micro-elements are present, the autoencoder fails to reconstruct those unfamiliar features, resulting in elevated reconstruction error ($> \tau$).

### Fundamental Screening States:
- **`GENUINE-LIKE`**: Image visual features are consistent with learned genuine banknote patterns ($MSE \le \tau$).
- **`ANOMALY DETECTED`**: Image exhibits statistically significant visual deviations ($MSE > \tau$).
- **`INCONCLUSIVE`**: Image quality is blurred, underexposed, or overexposed (Laplacian variance $< 100.0$), rendering analysis unreliable.

> **CRITICAL DISCLAIMER:**  
> *"This application uses a Deep Learning-based image screening approach trained on genuine banknote images. The result indicates similarity to learned genuine image patterns and does not establish official banknote authenticity. An anomaly requires further physical verification."*

---

## 2. Technology Stack

| Layer | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 17 / 21 | Core programming language |
| **Backend Framework** | Spring Boot | 3.2.5 | Enterprise application platform |
| **Deep Learning** | DeepLearning4J (DL4J) | 1.0.0-M2.1 | CNN Convolutional Autoencoder architecture |
| **Tensor Computation** | ND4J Native Platform | 1.0.0-M2.1 | Numerical N-dimensional array processing |
| **Computer Vision** | OpenCV Java (OpenPnP) | 4.7.0-0 | Filtering, blur detection, resizing, heatmap generation |
| **Security** | Spring Security 6 | 3.2.5 | BCrypt hashing, role-based authorization |
| **Database** | MySQL 8 / H2 Fallback | 8.0+ | Relational persistence of audit records & telemetry |
| **ORM** | Hibernate / Spring Data JPA | 3.2.5 | Object-relational mapping |
| **Frontend** | HTML5 / Bootstrap 5 / JS | 5.3.3 | Fintech UI (#101B35, #2563EB, #F4F6FA) |
| **Visualizations** | Chart.js | 4.4 | Real-time audit distribution & denomination charts |
| **Build Tool** | Apache Maven | 3.8+ | Dependency management and compilation |

---

## 3. Architecture & Deep Learning Pipeline

```
[Uploaded Photograph / Camera Stream]
               │
               ▼
   [OpenCV Java Preprocessing]
   ├── Laplacian Variance Blur Test (Var >= 100.0)
   ├── Luminance & Exposure Verification
   ├── High-Fidelity Resizing (224 × 224 × 3)
   ├── BGR to RGB Conversion
   └── Gaussian Denoising
               │
               ▼
   [ND4J Tensor Conversion]
   └── Normalized NCHW Tensor: [1, 3, 224, 224] ∈ [0.0, 1.0]
               │
               ▼
   [DL4J CNN Autoencoder]
   ├── ENCODER:
   │   ├── Conv2D (32, 3x3) -> BatchNorm -> ReLU -> MaxPool (112x112x32)
   │   ├── Conv2D (64, 3x3) -> BatchNorm -> ReLU -> MaxPool (56x56x64)
   │   └── Conv2D (128, 3x3) -> BatchNorm -> ReLU -> MaxPool (28x28x128) [Latent Space]
   └── DECODER:
       ├── UpSample2D (2x2) -> Conv2D (64, 3x3) -> BatchNorm -> ReLU (56x56x64)
       ├── UpSample2D (2x2) -> Conv2D (32, 3x3) -> BatchNorm -> ReLU (112x112x32)
       ├── UpSample2D (2x2) -> Conv2D (3, 3x3) -> Sigmoid (224x224x3)
       └── CnnLossLayer (Mean Squared Error)
               │
               ▼
   [Anomaly Detection & Evaluation Engine]
   ├── Calculate Reconstruction MSE: (1/N) * Σ(Input - Reconstructed)²
   ├── Generate False-Color Error Heatmap (OpenCV COLORMAP_JET)
   └── Compare MSE vs Calibrated Threshold (τ = μ + 2.5σ)
               │
               ▼
   [Screening Outcome: GENUINE-LIKE | ANOMALY DETECTED | INCONCLUSIVE]
               │
               ▼
   [Persistence: MySQL Database & Web Interface Visualization]
```

---

## 4. Dataset Organization

The system supports all standard Indian Rupee denominations: **₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹2000**.

### Directory Structure:
```
dataset/
    train/
        genuine/
            10/
            20/
            50/
            100/
            200/
            500/
            2000/
    validation/
        genuine/
            ...
    test/
        genuine/
            ...
```

- **Training (70%):** Genuine banknote images only.
- **Validation (15%):** Used exclusively for statistical threshold calibration ($\mu, \sigma$).
- **Test (15%):** Independent test set for evaluation of genuine acceptance rate.

*Note:* A built-in synthetic dataset generator (`SampleDatasetGenerator.java`) auto-generates calibrated sample genuine banknotes across all denominations with Guilloché patterns, security threads, watermarks, and RBI text for immediate zero-friction training.

---

## 5. Statistical Threshold Calibration

The anomaly threshold is **not** an arbitrary number. It is calibrated using genuine validation images:
1. Reconstruct all validation genuine images through the trained autoencoder.
2. Record validation reconstruction errors: $\{MSE_1, MSE_2, \dots, MSE_N\}$.
3. Compute genuine sample mean:
   $$\mu = \frac{1}{N} \sum_{i=1}^N MSE_i$$
4. Compute standard deviation:
   $$\sigma = \sqrt{\frac{1}{N} \sum_{i=1}^N (MSE_i - \mu)^2}$$
5. Calibrate threshold at 99% coverage:
   $$\tau = \mu + 2.5 \cdot \sigma$$

---

## 6. Project Folder Structure

```
banknote-authentication/
├── pom.xml                                        # Maven configuration with DL4J, ND4J, OpenCV, Spring Boot
├── README.md                                      # Comprehensive documentation
├── src/
│   ├── main/
│   │   ├── java/com/banknote/authentication/
│   │   │   ├── BanknoteAuthenticationApplication.java # Spring Boot main entry
│   │   │   ├── config/
│   │   │   │   ├── ModelConfig.java               # DL4J model & directory configurations
│   │   │   │   ├── OpenCVConfig.java              # OpenCV native library initialization
│   │   │   │   ├── DatabaseConfig.java            # JPA repository configuration
│   │   │   │   └── SecurityConfig.java            # Spring Security 6 & BCrypt setup
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java                      # Users table entity
│   │   │   │   ├── BanknoteRecord.java            # Banknote screening audit records
│   │   │   │   ├── ModelInformation.java          # Model versioning & threshold metadata
│   │   │   │   └── ScreeningResult.java           # Screening outcome enum
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── BanknoteRepository.java
│   │   │   │   └── ModelInformationRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── UploadResponse.java
│   │   │   │   ├── AnalysisResponse.java
│   │   │   │   └── DashboardResponse.java
│   │   │   ├── imageProcessing/
│   │   │   │   └── ImagePreprocessingService.java # OpenCV blur check, resize, RGB, tensor conversion
│   │   │   ├── deepLearning/
│   │   │   │   ├── BanknoteAutoencoder.java       # DL4J CNN Autoencoder architecture
│   │   │   │   ├── BanknoteDatasetLoader.java     # Batch image loader for genuine banknotes
│   │   │   │   ├── BanknoteModelTrainer.java      # Training loop & checkpointing
│   │   │   │   ├── BanknoteModelEvaluator.java    # Evaluation & acceptance rate metrics
│   │   │   │   └── ModelPersistenceService.java   # ModelSerializer save & restore
│   │   │   ├── anomalyDetection/
│   │   │   │   ├── MSECalculator.java             # INDArray Mean Squared Error calculation
│   │   │   │   ├── ThresholdCalibrationService.java # Validation-based threshold calibration
│   │   │   │   └── AnomalyDetectionService.java   # Decision orchestrator
│   │   │   ├── service/
│   │   │   │   ├── DeepLearningService.java       # Singleton model cache & inference
│   │   │   │   ├── BanknoteService.java           # Upload, preprocessing, screening, persistence
│   │   │   │   ├── UserService.java               # User registration & management
│   │   │   │   └── HistoryService.java            # Audit record querying & dashboard metrics
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # Login, registration, profile
│   │   │   │   ├── DashboardController.java       # Dashboard view & chart metrics
│   │   │   │   ├── BanknoteController.java        # Upload, camera capture, result view
│   │   │   │   ├── HistoryController.java         # History list, filter, delete
│   │   │   │   ├── AdminController.java           # Admin dashboard, threshold update, retraining
│   │   │   │   └── BanknoteRestController.java    # REST API endpoints (Postman compatible)
│   │   │   ├── training/
│   │   │   │   └── TrainAutoencoderRunner.java    # Standalone CLI to train DL4J autoencoder
│   │   │   └── util/
│   │   │       └── SampleDatasetGenerator.java    # Genuine banknote sample generator
│   │   └── resources/
│   │       ├── application.properties             # MySQL 8 & model configuration
│   │       ├── application-h2.properties          # Fallback zero-setup in-memory database
│   │       ├── schema.sql                         # MySQL 8 table DDL
│   │       ├── data.sql                           # Initial admin & demo user seeds
│   │       ├── static/
│   │       │   ├── css/style.css                  # Fintech styling (#101B35, #2563EB)
│   │       │   └── js/main.js                     # Drag & drop, camera capture, preview
│   │       └── templates/
│   │           ├── index.html                     # Landing page
│   │           ├── login.html                     # Sign in page
│   │           ├── register.html                  # Account registration
│   │           ├── dashboard.html                 # Analytics dashboard with charts
│   │           ├── upload.html                    # Upload & camera screening
│   │           ├── result.html                    # Deep learning inspection with heatmap
│   │           ├── history.html                   # Audit history with search & filter
│   │           ├── detail.html                    # Record audit details
│   │           ├── profile.html                   # User profile
│   │           └── admin.html                     # Admin management & telemetry
│   └── test/java/com/banknote/authentication/
│       ├── ImagePreprocessingServiceTest.java     # OpenCV resizing & tensor test
│       ├── MSECalculatorTest.java                 # MSE calculation unit tests
│       └── AnomalyDetectionServiceTest.java       # Threshold decision tests
├── documentation/
│   └── Banknote_Authentication_API.postman_collection.json # Complete Postman API Collection
└── models/
    └── banknote-autoencoder.zip                   # Trained DL4J model archive
```

---

## 7. Setup & Execution Guide

### Prerequisites:
- **Java Development Kit (JDK):** Version 17 or 21 installed (`java -version`).
- **Apache Maven:** Version 3.8+ installed (`mvn -version`).
- **MySQL Server:** Version 8.0+ running on `localhost:3306` (or use H2 profile).

### Step 1: Database Setup (MySQL 8)
1. Start your local MySQL server.
2. The application will automatically create the database `banknote_authentication` and execute schema initialization.
3. Configure your MySQL credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/banknote_authentication?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   spring.datasource.username=root
   spring.datasource.password=root
   ```

*Alternative (Zero-Setup In-Memory Mode):*  
If you do not have MySQL running locally, run with the `h2` profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Step 2: Model Training & Threshold Calibration (CLI)
You can train the CNN Autoencoder directly using the standalone CLI runner:
```bash
cd banknote-authentication
mvn compile exec:java -Dexec.mainClass="com.banknote.authentication.training.TrainAutoencoderRunner"
```
This runner:
1. Automatically populates genuine banknote training images across all 7 denominations using OpenCV.
2. Initializes the DeepLearning4J CNN Autoencoder.
3. Trains the network for 10 epochs.
4. Computes Mean ($\mu$) and Standard Deviation ($\sigma$) on validation images.
5. Calibrates the anomaly threshold ($\tau = \mu + 2.5\sigma$).
6. Saves `models/banknote-autoencoder.zip` and `models/threshold.json`.
7. Evaluates performance on the test set.

### Step 3: Run the Spring Boot Web Application
```bash
mvn spring-boot:run
```
Once started, open your web browser at:
**`http://localhost:8080`**

---

## 8. Default System Credentials

| Role | Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@banknote.com` | `Admin@123` | Full access, user management, retrain trigger, threshold update |
| **Bank Officer / User** | `user@banknote.com` | `User@123` | Banknote upload, camera capture, audit history, profile |

---

## 9. REST API Specification

The application provides a complete RESTful API for programmatic integration and testing with Postman.

### Endpoints Overview:

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user account | Public |
| `POST` | `/api/auth/login` | Authenticate user credentials | Public |
| `POST` | `/api/banknote/upload` | Upload image file to server | Authenticated |
| `POST` | `/api/banknote/analyze` | Run OpenCV + DL4J anomaly screening | Authenticated |
| `GET` | `/api/banknote/history` | Get paginated screening audit records | Authenticated |
| `GET` | `/api/banknote/history/{id}` | Get specific screening record by ID | Authenticated |
| `DELETE` | `/api/banknote/history/{id}` | Delete screening record by ID | Authenticated |
| `GET` | `/api/dashboard/statistics` | Aggregate metrics (scans, outcomes, breakdown) | Authenticated |
| `GET` | `/api/admin/users` | List all registered users | Admin |
| `GET` | `/api/admin/model` | Model telemetry, active version & threshold | Admin |

### Postman Collection:
Import the included collection from:
`documentation/Banknote_Authentication_API.postman_collection.json`

---

## 10. Automated Testing

Run the test suite with Maven:
```bash
mvn test
```
The suite verifies:
- `ImagePreprocessingServiceTest`: Verifies OpenCV decoding, resizing, Laplacian variance calculation, and ND4J tensor shape `[1, 3, 224, 224]`.
- `MSECalculatorTest`: Verifies mathematical accuracy of Mean Squared Error calculation on known tensor values.
- `AnomalyDetectionServiceTest`: Tests classification logic for `GENUINE_LIKE`, `ANOMALY_DETECTED`, and `INCONCLUSIVE` outcomes.

---

## 11. Summary of Constraints Compliance

1. **Java Technology Stack:** Built 100% in Java 17, Spring Boot, DL4J, ND4J, and OpenCV Java. No Python, TensorFlow, PyTorch, Keras, or ONNX used.
2. **One-Class Autoencoder:** Trained exclusively on genuine banknote images.
3. **Calibrated Threshold:** Threshold calculated from genuine validation reconstruction error distribution ($\mu + k\sigma$).
4. **Transparent Terminology:** Flags images as `GENUINE-LIKE`, `ANOMALY DETECTED`, or `INCONCLUSIVE`. Never falsely certifies that an anomaly is automatically counterfeit.

