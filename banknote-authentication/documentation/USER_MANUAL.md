# USER & ADMINISTRATOR OPERATING MANUAL
## BanknoteGuard DL4J - Banknote Authentication System

---

## 1. Introduction & Overview

BanknoteGuard DL4J is an intelligent currency screening application designed for bank tellers, cashiers, forensic auditors, and administrators. It uses DeepLearning4J (DL4J) and OpenCV Java to screen Indian banknotes (₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹2000) for structural and visual anomalies.

---

## 2. Getting Started

### 2.1 Starting the Application
Open a terminal in the project directory and execute:
```powershell
.\run.ps1
```
Or double-click `run.bat` in File Explorer.

Open your browser at: **`http://localhost:8080`**

### 2.2 Default Credentials

| User Type | Email | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@banknote.com` | `Admin@123` | Full access: audits, user management, threshold adjustment, retrain pipeline |
| **Standard Auditor** | `user@banknote.com` | `User@123` | Banknote screening, camera capture, audit history, personal profile |

---

## 3. Screening a Banknote

### 3.1 Step 1: Select Denomination
1. From the top navigation or dashboard, click **"Screen Banknote"**.
2. Select the denomination radio button matching your currency note (**₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹2000**).

### 3.2 Step 2: Upload or Capture
You can provide a banknote image in two ways:
- **File Upload (Drag-and-Drop):**
  - Drag your `.jpg`, `.jpeg`, or `.png` file into the upload dropzone.
  - Or click **"Browse Files"** to select a file from your hard drive.
  - A live preview of the note will appear.
- **Live Camera Capture:**
  - Click **"Use Camera"**.
  - Allow browser camera permissions.
  - Position the banknote squarely in front of the camera lens.
  - Click **"Take Photo & Analyze"**.

### 3.3 Step 3: Deep Learning Analysis
Click the blue **"Analyze with CNN Autoencoder"** button. The backend executes:
1. OpenCV blur and luminance checks.
2. High-quality $224 \times 224$ preprocessing and normalization.
3. DL4J CNN Autoencoder latent feature compression and reconstruction.
4. Calculation of Reconstruction Mean Squared Error (MSE).
5. Generation of the false-color Error Heatmap.

---

## 4. Interpreting the Screening Result Page

The result screen displays three critical components:

### 4.1 Visual Feature Inspection
- **Original Banknote:** The normalized input photograph.
- **Autoencoder Reconstructed:** What the deep learning model synthesized based on its knowledge of genuine currency patterns.
- **Anomaly Error Heatmap (OpenCV False-Color):**
  - **Cool Blue/Cyan:** Perfect reconstruction; visual patterns match genuine notes.
  - **Bright Red/Yellow/Orange:** High error regions; marks altered ink, foreign stamps, distorted security threads, or missing micro-patterns.

### 4.2 Quantitative Telemetry Cards
- **Reconstruction MSE:** The numeric Mean Squared Error between the input and reconstructed note (e.g. `0.01234`).
- **Calibrated Threshold:** The maximum allowable reconstruction error for a genuine note (e.g. `0.01850`).
- **Screening Result Badge:**
  - **`GENUINE-LIKE` (Green):** $MSE \le \text{Threshold}$. Features are consistent with learned genuine banknotes.
  - **`ANOMALY DETECTED` (Red):** $MSE > \text{Threshold}$. Unusual patterns or deviations detected.
  - **`INCONCLUSIVE` (Yellow):** Image quality failed sharpness standards (Laplacian variance $< 100.0$ or extreme lighting).

### 4.3 Official Disclaimer
Every result page includes the mandatory compliance statement:
> *"This application uses a Deep Learning-based image screening approach trained on genuine banknote images. The result indicates similarity to learned genuine image patterns and does not establish official banknote authenticity. An anomaly requires further verification."*

---

## 5. Audit History & Filtering

Click **"History"** in the navigation bar to:
- Browse all previous screening records with thumbnails.
- Filter records by **Denomination** (e.g. show only ₹500 notes).
- Filter records by **Result** (e.g. show only `ANOMALY_DETECTED`).
- Click the **Eye icon** to view full historical reconstruction artifacts.
- Click the **Trash icon** to delete audit records.

---

## 6. Administrator Controls

Log in as `admin@banknote.com`:
1. Click **"Admin"** in the navigation bar.
2. **Telemetry Cards:** View total system audits, genuine-like count, and anomaly counts.
3. **Threshold Calibration:**
   - View the active threshold.
   - Adjust the threshold manually if your lighting environment changes and click **"Update Threshold"**.
4. **Trigger Retraining Pipeline:**
   - Set epochs (default: `5`), learning rate (`0.001`), and version tag (`v1.1.0`).
   - Click **"Start Training & Calibration Pipeline"** to retrain the DL4J CNN Autoencoder on genuine banknote images.
5. **User Management:** View all registered officers and their roles.

