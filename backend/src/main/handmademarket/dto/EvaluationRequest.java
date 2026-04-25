package com.example.handmademarket.dto;

public class EvaluationRequest {

    private Integer score;
    private String content;
    private String images;

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
}
