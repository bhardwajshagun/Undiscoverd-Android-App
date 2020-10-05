package mobileappdev.undiscoverd.Photos;


public class ImageModel {

    public String imageName;

    public String url;

    public ImageModel() {

    }

    public ImageModel(String name, String url) {

        this.imageName = name;
        this.url = url;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageURL() {
        return url;
    }

}
