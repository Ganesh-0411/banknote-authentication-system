# DEEP LEARNING-BASED BANKNOTE AUTHENTICATION SYSTEM USING CNN AUTOENCODER AND ANOMALY DETECTION

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![DeepLearning4J](https://img.shields.io/badge/DL4J-1.0.0--M2.1-blue.svg)](https://deeplearning4j.konduit.ai/)
[![ND4J](https://img.shields.io/badge/ND4J-Native%20Platform-blue.svg)](https://eclipse.dev/deeplearning4j/)
[![OpenCV](https://img.shields.io/badge/OpenCV%20Java-4.7.0-red.svg)](https://opencv.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

An enterprise-grade, Java-native banknote authentication and anomaly screening web application. The system learns the visual characteristics, micro-patterns, Guilloché geometry, and security features of genuine Indian currency notes using a **CNN Convolutional Autoencoder** in **DeepLearning4J (DL4J)** and **ND4J**, with image processing handled natively by **OpenCV Java**.

---

## 1. Executive Summary & One-Class Anomaly Detection Paradigm

Conventional counterfeit currency systems attempt binary classification (Genuine vs. Fake). However, in real-world currency security:
1. **Unbounded Negative Class:** Counterfeit techniques evolve unpredictably; training on a fixed set of fake samples overfits to known fakes and misses unseen falsifications.
2. **Dataset Asymmetry:** Genuine banknotes are accessible in high resolution; genuine counterfeit samples are legally restricted and rare.

### Our Solution: One-Class Reconstruction Anomaly Detection
- The **CNN Autoencoder** is trained **exclusively on genuine Indian banknotes** (₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹2000).
- When presented with an unseen banknote, the model compresses it through a 28×28×128 latent bottleneck and reconstructs it.
- **Genuine Banknotes:** Exhibit low Mean Squared Error ($MSE \le \tau$) because the network has learned the manifold of genuine textures.
- **Anomalies / Alterations:** Produce elevated reconstruction error ($MSE > \tau$) and bright regional anomalies on the false-color difference heatmap.

### Standard Screening Outcomes:
| Outcome | Visual Indicator | Condition | Description |
| :--- | :--- | :--- | :--- |
| **`GENUINE-LIKE`** | 🟢 Green Badge | $MSE \le \tau$ | Visual features match learned genuine banknote patterns |
| **`ANOMALY DETECTED`** | 🔴 Red Badge | $MSE > \tau$ | Structural deviations detected in security thread, watermark, or ink |
| **`INCONCLUSIVE`** | 🟡 Yellow Badge | Variance $< 100.0$ | Image is blurred, overexposed, or underexposed |

> **IMPORTANT FORENSIC DISCLAIMER:**  
> *"This system performs deep learning-based image screening trained on genuine banknote patterns. Results indicate similarity to learned genuine features and do not constitute official legal authentication. Physical examination by authorized monetary authorities remains mandatory for definitive verification."*

---

## 2. Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 17 / 21 | Core programming language |
| **Application Framework** | Spring Boot | 3.2.5 | Web MVC, Security, Dependency Injection |
| **Deep Learning** | DeepLearning4J (DL4J) | 1.0.0-M2.1 | 7-layer CNN Convolutional Autoencoder |
| **Tensor Mathematics** | ND4J Native Platform | 1.0.0-M2.1 | High-performance NCHW multi-dimensional tensors |
| **Computer Vision** | OpenCV Java (OpenPnP) | 4.7.0-0 | Laplacian blur test, CLAHE, resizing, Jet heatmap |
| **Security** | Spring Security 6 | 3.2.5 | BCrypt password hashing, role-based authorization |
| **Database** | Embedded H2 / MySQL 8 | Mode=MySQL | Relational persistence of audit records & telemetry |
| **Frontend UI** | Thymeleaf, Bootstrap 5 | 5.3.3 | Fintech UI (#101B35 Navy, #2563EB Royal Blue) |
| **Visualizations** | Chart.js | 4.4 | Real-time audit distribution & denomination charts |

---

## 3. End-to-End Processing Pipeline

```
[Uploaded Photograph / Webcam Frame]
              │
              ▼
  [OpenCV Java Preprocessing]
  ├── 1. Laplacian Variance Blur Quality Test (Variance >= 100.0)
  ├── 2. Luminance & Exposure Validation (25.0 <= Luminance <= 240.0)
  ├── 3. Bilinear / Bicubic Resizing (224 × 224)
  ├── 4. Color Conversion (BGR -> RGB)
  └── 5. Gaussian Filtering (Noise reduction)
              │
              ▼
  [ND4J Tensor Conversion]
  └── Normalized NCHW Tensor: [1, 3, 224, 224] ∈ [0.0, 1.0]
              │
              ▼
  [DL4J CNN Autoencoder]
  ├── ENCODER:
  │   ├── Conv2D (32, 3x3)  -> BatchNorm -> ReLU -> MaxPool (112x112x32)
  │   ├── Conv2D (64, 3x3)  -> BatchNorm -> ReLU -> MaxPool (56x56x64)
  │   └── Conv2D (128, 3x3) -> BatchNorm -> ReLU -> MaxPool (28x28x128) [Latent Bottleneck]
  └── DECODER:
      ├── UpSample2D (2x2) -> Conv2D (64, 3x3) -> BatchNorm -> ReLU (56x56x64)
      ├── UpSample2D (2x2) -> Conv2D (32, 3x3) -> BatchNorm -> ReLU (112x112x32)
      ├── UpSample2D (2x2) -> Conv2D (3, 3x3)  -> Sigmoid (224x224x3)
      └── CnnLossLayer (Mean Squared Error Loss)
              │
              ▼
  [Anomaly Detection & Evaluation Engine]
  ├── 1. Compute Reconstruction MSE: (1/N) * Σ(Input - Reconstructed)²
  ├── 2. Generate False-Color Error Heatmap (OpenCV COLORMAP_JET)
  └── 3. Decision Evaluation: MSE vs Calibrated Threshold (τ = μ + 2.5σ)
              │
              ▼
  [Decision Badge: GENUINE-LIKE | ANOMALY DETECTED | INCONCLUSIVE]
              │
              ▼
  [Persistence & Real-Time Side-by-Side Visual Inspection UI]
```

---

## 4. Quick Start Guide

### Launching the Application (Choose Either Method):

#### Method A: 1-Click Batch File (Windows)
Double-click [`run.bat`](./run.bat) or execute in your terminal:
```cmd
.\run.bat
```
*(Automatically resolves portable Maven and launches Spring Boot).*

#### Method B: Direct in VS Code / IDE
1. Open [`BanknoteAuthenticationApplication.java`](./banknote-authentication/src/main/java/com/banknote/authentication/BanknoteAuthenticationApplication.java).
2. Click **`Run`** above line 17 (`public static void main`).

---

## 5. Web Access & Default Credentials

Once the console displays `Started BanknoteAuthenticationApplication in ... seconds`:

🌐 **Open Web Interface:** [http://localhost:8080](http://localhost:8080)

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Forensic Admin** | `admin@banknote.com` | `Admin@123` | Full access, threshold calibration, CNN retraining, audit export |
| **Standard User** | `user@banknote.com` | `User@123` | Banknote upload, camera capture, screening history |

---

## 6. Supported Denominations

The system natively supports the complete Mahatma Gandhi New Series:
- **₹10** (Chocolate Brown)
- **₹20** (Greenish Yellow)
- **₹50** (Fluorescent Blue)
- **₹100** (Lavender)
- **₹200** (Bright Orange)
- **₹500** (Stone Grey)
- **₹2000** (Magenta)

---

## 7. Statistical Threshold Calibration Formula

The anomaly threshold $\tau$ is statistically calibrated against genuine validation banknotes:
$$\mu = \frac{1}{N} \sum_{i=1}^N MSE_i$$
$$\sigma = \sqrt{\frac{1}{N} \sum_{i=1}^N (MSE_i - \mu)^2}$$
$$\tau = \mu + k \cdot \sigma \quad (k \in [2.0, 3.5], \text{ default } k=2.5 \approx 99\% \text{ coverage})$$

Administrators can dynamically calibrate $k$ via the interactive sensitivity slider in the **Admin Control Panel** (`/admin`).

---

## 8. REST API Reference

The application exposes 10 REST endpoints (Postman collection included in `documentation/`):

| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user account | Public |
| `POST` | `/api/auth/login` | Authenticate and obtain session | Public |
| `POST` | `/api/banknote/upload` | Multipart image screening | User/Admin |
| `POST` | `/api/banknote/capture` | Base64 webcam frame screening | User/Admin |
| `GET` | `/api/banknote/result/{id}` | Retrieve screening result and metrics | User/Admin |
| `GET` | `/api/banknote/history` | Paginated user audit records | User/Admin |
| `GET` | `/api/banknote/dashboard` | Dashboard analytics and counters | User/Admin |
| `GET` | `/api/admin/model-info` | Active model architecture & threshold | Admin |
| `POST` | `/api/admin/recalibrate` | Calibrate threshold with new $k$ multiplier | Admin |
| `POST` | `/api/admin/retrain` | Trigger on-demand DL4J model retraining | Admin |

---

## 9. Project Directory Tree

```
banknote-authentication/
├── pom.xml                                 # Root Maven POM (DL4J, OpenCV, Spring Boot)
├── README.md                               # Project documentation
├── run.bat                                 # Automatic application launcher
├── train.bat                               # Standalone DL4J CLI trainer
├── push_to_github.bat                      # GitHub synchronization script
├── dataset/                                # Banknote datasets (train, validation, test)
│   ├── train/genuine/                      # Genuine training images across 7 denominations
│   ├── validation/genuine/                 # Genuine validation images for calibration
│   └── test/genuine/                       # Independent test set
├── documentation/
│   ├── ARCHITECTURE.md                     # Deep learning & system architecture document
│   ├── USER_MANUAL.md                      # End-user visual manual
│   └── Banknote_Authentication_API.json   # Postman collection
├── models/
│   ├── banknote-autoencoder.zip            # Trained DL4J MultiLayerNetwork archive
│   └── threshold.json                      # Calibrated threshold metadata
└── banknote-authentication/
    └── src/
        ├── main/java/com/banknote/authentication/
        │   ├── BanknoteAuthenticationApplication.java
        │   ├── anomalyDetection/           # MSE Calculator & Threshold Calibration
        │   ├── config/                     # Model, OpenCV, Database, Security config
        │   ├── controller/                 # Web & REST Controllers
        │   ├── deepLearning/               # DL4J CNN Autoencoder, Loader, Trainer
        │   ├── entity/                     # User, BanknoteRecord, ModelInformation
        │   ├── imageProcessing/            # OpenCV Preprocessing Service
        │   ├── repository/                 # Spring Data JPA repositories
        │   ├── security/                   # Custom UserDetails & SecurityFilterChain
        │   ├── service/                    # Core business logic services
        │   └── util/                       # Dataset initializers & sample generators
        └── main/resources/
            ├── application.properties      # Zero-setup H2 & MySQL configuration
            ├── static/                     # CSS, JavaScript, icons
            └── templates/                  # Thymeleaf Fintech UI templates
```

---

## 10. License & Academic Attribution
Developed as part of an Advanced Deep Learning in Computer Vision initiative. Licensed under the [Apache 2.0 License](LICENSE).
