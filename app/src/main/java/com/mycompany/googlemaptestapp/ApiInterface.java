package com.mycompany.googlemaptestapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by mac on 2017. 6. 22..
 */

public interface ApiInterface {
    String API_KEY = "jENXI1lavhLBnweHBWDKwAfCcvSEqooh5DshJSNDLGNa%2Bpsd3WMuAuswxdQydH8mbvffg3rWCcYfa5tIo7DVbw%3D%3D";
    //@GET("/data/2.5/weather?lat={lat}&lon={lon}&appid=684b98e21b4f35b7d52abe9ff6279349")
    //Call<Repo> repo(@Path("lat") String lat, @Path("lon") String lon);

    //http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?sidoName=서울&pageNo=1&numOfRows=10&ServiceKey=서비스키&ver=1.3
    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
    Call<Repo> repo(@Query("sidoName") String sidoName, @Query("pageNo") int pageNo, @Query("numOfRows") int numOfRows, @Query(value="serviceKey", encoded=true) String serviceKey, @Query("ver") double ver);
//    Call<Repo> repo(@Field("sidoName") String sidoName, @Field("pageNo") int pageNo, @Field("numOfRows") int numOfRows, @Field("serviceKey") String serviceKey, @Field("ver") double ver);

//    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
//    Call<Repo> repo2(@QueryMap map<String, String>;


//    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureSidoLIst")
//    Call<Repo> repo2(@Query("appid") String appid, @Query("lat") double lat, @Query("lon") double lon);
}