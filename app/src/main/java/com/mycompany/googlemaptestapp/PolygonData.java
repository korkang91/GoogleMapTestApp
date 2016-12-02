package com.mycompany.googlemaptestapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

/**
 * Created by manager on 2016. 12. 1..
 */

public class PolygonData {
    private String name;
    private LatLng point;
    private List<LatLng> latLngs;
    private int[] colorArray;
    private IconGenerator iconFactory ;

    public List<LatLng> getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }

    public PolygonData(String name, LatLng point, List<LatLng> latLngs, int[] colorArray, IconGenerator iconFactory) {
        this.name = name;
        this.point = point;
        this.latLngs = latLngs;
        this.colorArray = colorArray;
        this.iconFactory = iconFactory;
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



    public int[] getColorArray() {
        return colorArray;
    }

    public void setColorArray(int[] colorArray) {
        this.colorArray = colorArray;
    }

    public IconGenerator getIconFactory() {
        return iconFactory;
    }

    public void setIconFactory(IconGenerator iconFactory) {
        this.iconFactory = iconFactory;
    }
}
