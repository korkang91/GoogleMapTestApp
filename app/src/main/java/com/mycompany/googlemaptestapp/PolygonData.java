package com.mycompany.googlemaptestapp;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by manager on 2016. 12. 5..
 */

public class PolygonData {

    String name;
    LatLng point;
    List<LatLng> points;

    public PolygonData(String name, LatLng point, List points) {
        this.name = name;
        this.point = point;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPoint() {
        return point;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }




}
