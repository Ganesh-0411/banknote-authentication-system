package com.banknote.authentication.dto;

public class UploadResponse {
    private boolean success;
    private String message;
    private String imagePath;
    private String filename;
    private long fileSize;

    public UploadResponse() {}

    public UploadResponse(boolean success, String message, String imagePath, String filename, long fileSize) {
        this.success = success;
        this.message = message;
        this.imagePath = imagePath;
        this.filename = filename;
        this.fileSize = fileSize;
    }

    public static UploadResponse failure(String message) {
        return new UploadResponse(false, message, null, null, 0);
    }

    public static UploadResponse success(String message, String imagePath, String filename, long fileSize) {
        return new UploadResponse(true, message, imagePath, filename, fileSize);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

