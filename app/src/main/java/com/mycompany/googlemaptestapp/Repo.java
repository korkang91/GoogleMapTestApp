package com.mycompany.googlemaptestapp;

/**
 * Created by mac on 2017. 6. 22..
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Header;


@Root(strict = false)
public class Repo {
    @Element
    private Header header;
    @Element
    private Body body;

    @Root
    private static class Header{
        @Element
        private String resultCode;

        @Element
        private String resultMsg;

    }

    @Root
    private static class Body{
        @Element
        private Item items;
    }

    @Root
    private static class Item {
        @Element(name = "stationName")
        String stationName;

        public String getStationName() {
            return stationName;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }


    }


}
