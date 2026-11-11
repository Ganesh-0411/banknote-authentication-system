// ==========================================================================
// Banknote Authentication UI JavaScript
// ==========================================================================

document.addEventListener('DOMContentLoaded', function () {
    initDragAndDrop();
    initCameraCapture();
});

// Drag and Drop Upload Initializer
function initDragAndDrop() {
    const dropzone = document.getElementById('uploadDropzone');
    const fileInput = document.getElementById('banknoteFileInput');
    const previewContainer = document.getElementById('previewContainer');
    const imagePreview = document.getElementById('imagePreview');
    const uploadForm = document.getElementById('banknoteUploadForm');
    const submitBtn = document.getElementById('submitAnalyzeBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');

    if (!dropzone || !fileInput) return;

    ['dragenter', 'dragover'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.add('dragover');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropzone.classList.remove('dragover');
        }, false);
    });

    dropzone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files.length > 0) {
            fileInput.files = files;
            displayPreview(files[0]);
        }
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            displayPreview(fileInput.files[0]);
        }
    });

    function displayPreview(file) {
        if (!file.type.match('image.*')) {
            alert('Please select an image file (JPG, PNG, JPEG)');
            return;
        }
        const reader = new FileReader();
        reader.onload = function (e) {
            if (imagePreview) {
                imagePreview.src = e.target.result;
            }
            if (previewContainer) {
                previewContainer.classList.remove('d-none');
            }
            if (submitBtn) {
                submitBtn.removeAttribute('disabled');
            }
        };
        reader.readAsDataURL(file);
    }

    if (uploadForm) {
        uploadForm.addEventListener('submit', () => {
            if (loadingSpinner) {
                loadingSpinner.classList.remove('d-none');
            }
            if (submitBtn) {
                submitBtn.setAttribute('disabled', 'true');
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Deep Learning Screening in Progress...';
            }
        });
    }
}

// Camera Capture Initializer
function initCameraCapture() {
    const startCameraBtn = document.getElementById('startCameraBtn');
    const captureBtn = document.getElementById('capturePhotoBtn');
    const videoElem = document.getElementById('cameraVideo');
    const canvasElem = document.getElementById('cameraCanvas');
    const cameraModalElem = document.getElementById('cameraModal');
    const cameraDataInput = document.getElementById('cameraDataInput');
    const cameraForm = document.getElementById('cameraCaptureForm');

    let stream = null;

    if (!startCameraBtn || !videoElem) return;

    startCameraBtn.addEventListener('click', async () => {
        try {
            stream = await navigator.mediaDevices.getUserMedia({
                video: { width: { ideal: 1280 }, height: { ideal: 720 }, facingMode: 'environment' }
            });
            videoElem.srcObject = stream;
            videoElem.play();
        } catch (err) {
            alert('Unable to access camera: ' + err.message);
        }
    });

    if (cameraModalElem) {
        cameraModalElem.addEventListener('hidden.bs.modal', () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
                stream = null;
            }
        });
    }

    if (captureBtn && canvasElem && videoElem && cameraDataInput && cameraForm) {
        captureBtn.addEventListener('click', () => {
            canvasElem.width = videoElem.videoWidth || 640;
            canvasElem.height = videoElem.videoHeight || 480;
            const ctx = canvasElem.getContext('2d');
            ctx.drawImage(videoElem, 0, 0, canvasElem.width, canvasElem.height);
            const dataUrl = canvasElem.toDataURL('image/jpeg', 0.95);
            cameraDataInput.value = dataUrl;

            // Stop camera stream
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
            }

            // Submit form
            cameraForm.submit();
        });
    }
}

