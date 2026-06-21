package com.blank.app.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreatePostcardRequest {
    @NotBlank(message = "请上传明信片图片")
    private String imageUrl;
    @NotNull(message = "请选择邮票")
    private Integer stampId;
    private String title;
    private String aspectRatio;
    private String postcardType;
    private Object elements;
    private Integer canvasWidth;
    private Integer canvasHeight;
    private Float imageOffsetX;
    private Float imageOffsetY;
    private Float imageScale;
    private Float imageRotation;
    private String recipientInput;
    private Boolean isPublic;
    private String scheduledAt;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStampId() { return stampId; }
    public void setStampId(Integer stampId) { this.stampId = stampId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }
    public String getPostcardType() { return postcardType; }
    public void setPostcardType(String postcardType) { this.postcardType = postcardType; }
    public Object getElements() { return elements; }
    public void setElements(Object elements) { this.elements = elements; }
    public Integer getCanvasWidth() { return canvasWidth; }
    public void setCanvasWidth(Integer canvasWidth) { this.canvasWidth = canvasWidth; }
    public Integer getCanvasHeight() { return canvasHeight; }
    public void setCanvasHeight(Integer canvasHeight) { this.canvasHeight = canvasHeight; }
    public Float getImageOffsetX() { return imageOffsetX; }
    public void setImageOffsetX(Float imageOffsetX) { this.imageOffsetX = imageOffsetX; }
    public Float getImageOffsetY() { return imageOffsetY; }
    public void setImageOffsetY(Float imageOffsetY) { this.imageOffsetY = imageOffsetY; }
    public Float getImageScale() { return imageScale; }
    public void setImageScale(Float imageScale) { this.imageScale = imageScale; }
    public Float getImageRotation() { return imageRotation; }
    public void setImageRotation(Float imageRotation) { this.imageRotation = imageRotation; }
    public String getRecipientInput() { return recipientInput; }
    public void setRecipientInput(String recipientInput) { this.recipientInput = recipientInput; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public String getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
}
