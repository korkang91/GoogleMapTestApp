package com.mycompany.googlemaptestapp;

/**
 * Created by mac on 2017. 6. 22..
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root(name = "response", strict = false)
public class Repo {
    @Element
    private Header header;
    @Element
    private Body body;

    public String getResultCode() {
        return header.resultCode;
    }

    public String getResultMsg() {
        return header.resultMsg;
    }

    public List<Item> getItems() {
        return body.items.item;
    }

    public String getStationName(int i) {
        return body.items.item.get(i).stationName;
    }

    public String getMangName(int i) {
        return body.items.item.get(i).mangName;
    }

    public String getDataTime(int i) {
        return body.items.item.get(i).dataTime;
    }

    public String getSo2Value(int i) {
        return body.items.item.get(i).so2Value;
    }

    public String getCoValue(int i) {
        return body.items.item.get(i).coValue;
    }

    public String getO3Value(int i) {
        return body.items.item.get(i).o3Value;
    }
    public String getNo2Value(int i) {
        return body.items.item.get(i).no2Value;
    }

    public String getPm10Value(int i) {
        return body.items.item.get(i).pm10Value;
    }

    public String getPm10Value24(int i) {
        return body.items.item.get(i).pm10Value24;
    }

    public String getPm25Value(int i) {
        return body.items.item.get(i).pm25Value;
    }

    public String getPm25Value24(int i) {
        return body.items.item.get(i).pm25Value24;
    }

    public String getKhaiValue(int i) {
        return body.items.item.get(i).khaiValue;
    }

    public String getKhaiGrade(int i) {
        return body.items.item.get(i).khaiGrade;
    }

    public String getSo2Grade(int i) {
        return body.items.item.get(i).so2Grade;
    }

    public String getCoGrade(int i) {
        return body.items.item.get(i).coGrade;
    }

    public String getO3Grade(int i) {
        return body.items.item.get(i).o3Grade;
    }

    public String getNo2Grade(int i) {
        return body.items.item.get(i).no2Grade;
    }

    public String getPm10Grade(int i) {
        return body.items.item.get(i).pm10Grade;
    }

    public String getPm25Grade(int i) {
        return body.items.item.get(i).pm25Grade;
    }

    public String getPm10Grade1h(int i) {
        return body.items.item.get(i).pm10Grade1h;
    }

    public String getPm25Grade1h(int i) {
        return body.items.item.get(i).pm25Grade1h;
    }

    public String getCityName(int i) {
        return body.items.item.get(i).cityName;
    }

    public String getTotalCount() {
        return body.totalCount;
    }

    @Root(name = "header")
    private static class Header{
        @Element
        private String resultCode;

        @Element
        private String resultMsg;

    }

    @Root(name = "body")
    private static class Body{
        @Element
        private Items items;

        @Element
        private String numOfRows;

        @Element
        private String pageNo;

        @Element
        private String totalCount;
    }

    @Root(name = "items")
    private static class Items {
        @ElementList(inline = true, required=false)
        private List<Item> item;
    }

    @Root(name = "item")
    private static class Item {
        @Element(required=false)
        String stationName;
        @Element(required=false)
        String mangName;
        @Element(required=false)
        String dataTime;
        @Element(required=false)
        String so2Value;
        @Element(required=false)
        String coValue;
        @Element(required=false)
        String o3Value;
        @Element(required=false)
        String no2Value;
        @Element(required=false)
        String pm10Value;
        @Element(required=false)
        String pm10Value24;
        @Element(required=false)
        String pm25Value;
        @Element(required=false)
        String pm25Value24;
        @Element(required=false)
        String khaiValue;
        @Element(required=false)
        String khaiGrade;
        @Element(required=false)
        String so2Grade;
        @Element(required=false)
        String coGrade;
        @Element(required=false)
        String o3Grade;
        @Element(required=false)
        String no2Grade;
        @Element(required=false)
        String pm10Grade;
        @Element(required=false)
        String pm25Grade;
        @Element(required=false)
        String pm10Grade1h;
        @Element(required=false)
        String pm25Grade1h;

        @Element(required=false)
        String cityName;
    }

}
