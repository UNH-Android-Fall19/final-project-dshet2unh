package com.example.snapit.models;

public class Bean_Document {
    private String imageId;
    private String fileUrl;
    private String name;
    private String type;
    private String docSubjectName;

    public Bean_Document() {
    }

    public void setDocSubjectName(String docSubjectName) {
        this.docSubjectName = docSubjectName;
    }

    public String getDocSubjectName() {
        return docSubjectName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
