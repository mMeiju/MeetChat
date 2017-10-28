package com.example.meiju.meetchat;

public class Users {
    public String name;
    public String image;
    public String thumbImage;

    public Users() {

    }

    public Users(String name, String image, String thumb_image) {
        this.name = name;
        this.image = image;
        this.thumbImage = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }
}
