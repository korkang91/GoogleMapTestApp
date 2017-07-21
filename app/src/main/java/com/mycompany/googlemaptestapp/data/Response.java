package com.mycompany.googlemaptestapp.data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import retrofit2.http.Body;
import retrofit2.http.Header;

/**
 * Created by kang on 2017. 7. 21..
 */

@Root(strict = false)
public class Response {
    @Element
    private Header header;
    @Element
    private Body body;


}
