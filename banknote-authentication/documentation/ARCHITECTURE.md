# SYSTEM ARCHITECTURE & DEEP LEARNING SPECIFICATION
## Deep Learning-Based Banknote Authentication System Using Java & CNN Autoencoder

---

## 1. Mathematical Framework & Anomaly Detection Theory

### 1.1 The One-Class Anomaly Formulation
In currency verification, counterfeit techniques are unbounded and non-stationary. Training a conventional binary classifier $f: \mathcal{X} \to \{0, 1\}$ on a fixed set of fake samples overfits to known counterfeit artifacts and fails to generalize to novel falsifications.

Instead, we formulate banknote screening as **One-Class Reconstruction-Based Anomaly Detection**:
- The model is an **Autoencoder** comprising an encoder $E_\theta$ and a decoder $D_\phi$:
  $$z = E_\theta(x), \quad \hat{x} = D_\phi(z)$$
  where $x \in \mathbb{R}^{3 \times 224 \times 224}$ is the preprocessed banknote image, $z \in \mathbb{R}^{128 \times 28 \times 28}$ is the compressed latent representation, and $\hat{x}$ is the reconstructed image.
- The model parameters $(\theta, \phi)$ are trained exclusively on genuine Indian banknotes by minimizing the Mean Squared Error (MSE) reconstruction loss:
  $$\mathcal{L}_{MSE}(x, \hat{x}) = \frac{1}{C \times H \times W} \sum_{c=1}^C \sum_{h=1}^H \sum_{w=1}^W \left(x_{c,h,w} - \hat{x}_{c,h,w}\right)^2$$
  where $C=3, H=224, W=224$ (total $N = 150,528$ pixel values).

### 1.2 Statistical Threshold Calibration
The decision threshold $\tau$ is statistically calibrated on the genuine validation partition $\mathcal{D}_{val}$:
1. For each validation image $x_i \in \mathcal{D}_{val}$, compute its reconstruction error $e_i = \mathcal{L}_{MSE}(x_i, \hat{x}_i)$.
2. Calculate the sample mean:
   $$\mu_{val} = \frac{1}{|\mathcal{D}_{val}|} \sum_{i=1}^{|\mathcal{D}_{val}|} e_i$$
3. Calculate the sample standard deviation:
   $$\sigma_{val} = \sqrt{\frac{1}{|\mathcal{D}_{val}|} \sum_{i=1}^{|\mathcal{D}_{val}|} (e_i - \mu_{val})^2}$$
4. Define the calibrated threshold at a 99% coverage upper bound:
   $$\tau = \mu_{val} + 2.5 \cdot \sigma_{val}$$

### 1.3 Decision Rules:
$$\text{Screening Result} = 
\begin{cases} 
\text{INCONCLUSIVE}, & \text{if } \text{Var}(\text{Laplacian}(x)) < 100.0 \text{ or } \bar{L}(x) \notin [25, 240] \\
\text{GENUINE-LIKE}, & \text{if } \mathcal{L}_{MSE}(x, \hat{x}) \le \tau \\
\text{ANOMALY DETECTED}, & \text{if } \mathcal{L}_{MSE}(x, \hat{x}) > \tau
\end{cases}$$

---

## 2. DeepLearning4J CNN Architecture Detailed Spec

```
Layer (type)                 Output Shape              Param #     Activation
=============================================================================
Input                        [Batch, 3, 224, 224]      0           -
0: Convolution2D (3x3, 32)   [Batch, 32, 224, 224]     896         Identity
1: BatchNormalization        [Batch, 32, 224, 224]     64          -
2: ActivationLayer           [Batch, 32, 224, 224]     0           ReLU
3: MaxPooling2D (2x2, s=2)   [Batch, 32, 112, 112]     0           -
4: Convolution2D (3x3, 64)   [Batch, 64, 112, 112]     18,496      Identity
5: BatchNormalization        [Batch, 64, 112, 112]     128         -
6: ActivationLayer           [Batch, 64, 112, 112]     0           ReLU
7: MaxPooling2D (2x2, s=2)   [Batch, 64, 56, 56]       0           -
8: Convolution2D (3x3, 128)  [Batch, 128, 56, 56]      73,856      Identity
9: BatchNormalization        [Batch, 128, 56, 56]      256         -
10: ActivationLayer          [Batch, 128, 56, 56]      0           ReLU
11: MaxPooling2D (2x2, s=2)  [Batch, 128, 28, 28]      0           - [Bottleneck]
-----------------------------------------------------------------------------
12: UpSampling2D (2x2)       [Batch, 128, 56, 56]      0           -
13: Convolution2D (3x3, 64)  [Batch, 64, 56, 56]       73,792      Identity
14: BatchNormalization       [Batch, 64, 56, 56]       128         -
15: ActivationLayer          [Batch, 64, 56, 56]       0           ReLU
16: UpSampling2D (2x2)       [Batch, 64, 112, 112]     0           -
17: Convolution2D (3x3, 32)  [Batch, 32, 112, 112]     18,464      Identity
18: BatchNormalization       [Batch, 32, 112, 112]     64          -
19: ActivationLayer          [Batch, 32, 112, 112]     0           ReLU
20: UpSampling2D (2x2)       [Batch, 32, 224, 224]     0           -
21: Convolution2D (3x3, 3)   [Batch, 3, 224, 224]      867         Sigmoid
22: CnnLossLayer (MSE)       [Batch, 3, 224, 224]      0           -
=============================================================================
Total Parameters: 186,947 (All Trainable)
Updater: Adam (Learning Rate: 0.001)
Weight Initialization: XAVIER
```

---

## 3. Computer Vision Pipeline (OpenCV Java)

### 3.1 Quality Assessment (Laplacian Blur Check)
Image blur is assessed by taking the 2nd-order spatial derivative using the discrete Laplacian operator:
$$\Delta I = \frac{\partial^2 I}{\partial x^2} + \frac{\partial^2 I}{\partial y^2}$$
The blur metric is the sample variance of the Laplacian output:
$$\text{Var}(\Delta I) = \frac{1}{M} \sum_{i=1}^M (\Delta I_i - \bar{\Delta I})^2$$
If $\text{Var}(\Delta I) < 100.0$, the input lacks high-frequency edges and is flagged as **`INCONCLUSIVE`**.

### 3.2 False-Color Error Heatmap Generation
To help human analysts inspect anomalous patterns, an absolute error matrix is computed:
$$E(h, w) = \frac{1}{3} \sum_{c=1}^3 |x(c, h, w) - \hat{x}(c, h, w)|$$
This grayscale difference map is amplified and mapped to an 8-bit visual range $[0, 255]$:
$$E_{8bit} = \min(255, \lfloor 4.0 \times 255 \times E \rfloor)$$
Finally, OpenCV's `Imgproc.applyColorMap(E_8bit, Heatmap, Imgproc.COLORMAP_JET)` is applied, highlighting areas of high reconstruction error in bright red/yellow while genuine features remain cool blue.

