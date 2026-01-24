package com.example.myradio2;

public class RadioStation {
    private String name;
    private String country;
    private String streamUrl;

    public RadioStation(String name, String country, String streamUrl) {
        this.name = name;
        this.country = country;
        this.streamUrl = streamUrl;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getStreamUrl() {
        return streamUrl;
    }
}
