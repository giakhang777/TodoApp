package com.example.apptodo.model;

public class Progress {
    private int imageResId;
    private String desc1;
    private String desc2;
    private int progress;

    public Progress(int imageResId, String desc1, String desc2, int progress) {
        this.imageResId = imageResId;
        this.desc1 = desc1;
        this.desc2 = desc2;
        this.progress = progress;
    }

    public String getDesc1() {
        return desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
