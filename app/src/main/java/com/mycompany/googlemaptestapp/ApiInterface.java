package com.mycompany.googlemaptestapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mac on 2017. 6. 22..
 */

public interface ApiInterface {

    //@GET("/data/2.5/weather?lat={lat}&lon={lon}&appid=684b98e21b4f35b7d52abe9ff6279349")
    //Call<Repo> repo(@Path("lat") String lat, @Path("lon") String lon);

    //http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?sidoName=서울&pageNo=1&numOfRows=10&ServiceKey=서비스키&ver=1.3
//    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
//    Call<Repo> repo(@Query("serviceKey") String serviceKey, @Query("lat") double lat, @Query("lon") double lon);

//    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureSidoLIst")
//    Call<Repo> repo2(@Query("appid") String appid, @Query("lat") double lat, @Query("lon") double lon);
}