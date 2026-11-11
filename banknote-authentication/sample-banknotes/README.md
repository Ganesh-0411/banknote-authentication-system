# Banknote Authentication - Sample Test Banknotes Guide

This directory contains pre-prepared test banknotes ready for immediate upload and screening:

| Filename | Denomination | Expected Screening Result | Notes |
| :--- | :--- | :--- | :--- |
| `dataset/test/genuine/100/note_100_test_genuine_1.png` | ₹100 | **GENUINE-LIKE** | Features clean Guilloché curves, sharp Gandhi watermark, intact security thread. |
| `dataset/test/genuine/500/note_500_test_genuine_1.png` | ₹500 | **GENUINE-LIKE** | Stone grey series with standard micro-engravings. |
| `dataset/test/genuine/2000/note_2000_test_genuine_1.png` | ₹2000 | **GENUINE-LIKE** | Magenta series with full bleed lines and security thread. |
| `dataset/test/genuine/100/note_100_test_anomaly.png` | ₹100 | **ANOMALY DETECTED** | Features altered ink patch and distorted thread (simulated defect). |

---

### How to Test in the Web Interface:
1. Start the app via `.\run.ps1` or `mvnw.cmd spring-boot:run`.
2. Open **`http://localhost:8080`** and log in (`user@banknote.com` / `User@123`).
3. Click **"Screen Banknote"** on the dashboard.
4. Drag-and-drop any of the test images above into the upload zone.
5. Click **"Analyze with CNN Autoencoder"**.
6. Inspect the resulting:
   - Original vs Reconstructed image comparison
   - OpenCV false-color Error Heatmap (`COLORMAP_JET`)
   - Reconstruction MSE score against calibrated threshold ($\tau$).

