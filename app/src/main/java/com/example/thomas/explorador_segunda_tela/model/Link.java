package com.example.thomas.explorador_segunda_tela.model;

import com.example.thomas.explorador_segunda_tela.R;

enum ColorLink {
    RED("RED", R.drawable.ic_red_magnify),
    GREEN("GREEN", R.drawable.ic_green_magnify),
    YELLOW("YELLOW", R.drawable.ic_yellow_magnify),
    BLUE("BLUE", R.drawable.ic_blue_magnify);

    private String linkColor;
    private int resIdDrawable;
    ColorLink(String linkColor, int resIdDrawable) {
        this.linkColor = linkColor;
        this.resIdDrawable = resIdDrawable;
    }

    @Override
    public String toString() {
        return linkColor;
    }

    public int getResIdDrawable(){
        return resIdDrawable;
    }
}

public class Link {

    private String title;
    private String description;
    private int resIdImage;
    private ColorLink colorLink;
    private int getResIdVideo;

    public Link(String title, String description, int resIdImage) {
        this.title = title;
        this.description = description;
        this.resIdImage = resIdImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getResColorLink() {
        return colorLink.getResIdDrawable();
    }

    public String getColorLink() {
        return colorLink.toString();
    }

    public int getResIdImage() {
        return resIdImage;
    }

    public int getGetResIdVideo() {
        return getResIdVideo;
    }

    public void setColor(String color) {
        this.colorLink = ColorLink.valueOf(color);
    }
}
