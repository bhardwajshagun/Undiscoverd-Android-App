package mobileappdev.undiscoverd.maps;

/**
 * Class for storing and retrieving relevant location information
 */
public class MapsLocation {

    private double latitude;
    private double longitude;

    private String city;
    private String state;
    private String country;

    public MapsLocation(double latitude, double longitude, String city, String state, String country) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getCity() {
        return this.city;
    }

    public String getState() {
        return this.state;
    }

    public String getCountry() {
        return this.country;
    }

}
