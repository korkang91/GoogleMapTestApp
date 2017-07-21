package com.mycompany.googlemaptestapp.data;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Created by kang on 2017. 7. 21..
 */

@Root(name = "item", strict = false)
public class Item {
    @Element(name = "stationName")
    String stationName;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }


}