package com.mycompany.googlemaptestapp;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.ui.IconGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Polygon polygon;
    Document doc = null;
    HashMap<String, String> hmap = new HashMap<String, String>();// 행정구역 , 미세먼지 지수
    HashMap<Integer, String> namehmap = new HashMap<Integer, String>();// 해쉬코드, 이름
    HashMap<Integer, Integer> colorhmap = new HashMap<Integer, Integer>();// 해쉬코드, color
    int clkclr;
    ArrayList<Marker> mList = new ArrayList<Marker>(); // 마커의 집합

    public MapsActivity(){
        Log.d("log","kbc 생성자 시작");
        airAPI("서울");
        airAPIcity("경기");
        Log.d("log","kbc 생성자 끝");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng seoul = new LatLng(37.56, 126.990786);  //서울의 위도, 경도
//        mMap.addMarker(new MarkerOptions().position(seoul).title("Marker in Seoul!!!!")); // 마커추가, 타이틀설정
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));  // 초기위치 설정

        UiSettings mapSettings;
        mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);  // 줌 설정

//        Log.d("log","kbc -----zoom level    "+mMap.getCameraPosition().zoom);

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            public void onPolygonClick(Polygon polygon) {
                if (polygon.getFillColor() == Color.TRANSPARENT) {
                    polygon.setFillColor(colorhmap.get(polygon.hashCode()));
                    Toast.makeText(MapsActivity.this, "Clicked "+namehmap.get(polygon.hashCode()), Toast.LENGTH_SHORT).show();
//                    Log.d("log","kbc -------- "+polygon.hashCode());//폴리곤 구분
                    Log.d("log","kbc -----zoom level    "+mMap.getCameraPosition().zoom);
                } else {
                    clkclr = polygon.getFillColor();
                    polygon.setFillColor(Color.TRANSPARENT); //Color.TRANSPARENT
                    Toast.makeText(MapsActivity.this, "Clicked Transparent...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                if(arg0.zoom > 11){
                    Log.d("log","kbc -----zoom level if   "+mMap.getCameraPosition().zoom);
                    for(int i =0; i< mList.size(); i++){
                        mList.get(i).setVisible(true);
                    }
                }else{
                    Log.d("log","kbc -----zoom level else   "+mMap.getCameraPosition().zoom);
                    for(int i =0; i< mList.size(); i++){
                        mList.get(i).setVisible(false);
                    }
                }
            }
        });

    }


    //미세먼지 API 연동 서울시
    public void airAPI(String input){
        Log.d("tag","kbc +++++++airAPI");//7
        String addr;
        String serviceKey = "jENXI1lavhLBnweHBWDKwAfCcvSEqooh5DshJSNDLGNa%2Bpsd3WMuAuswxdQydH8mbvffg3rWCcYfa5tIo7DVbw%3D%3D";
        addr = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?sidoName="+input+"&pageNo=1&numOfRows=99&ServiceKey="+serviceKey+"&ver=1.3";
        GetXMLTask task = new GetXMLTask();
        task.execute(addr);
        task.getStatus();
    }
    //미세먼지 시도명(경기
    public void airAPIcity(String input){
        Log.d("tag","kbc +++++++airAPIcity");//7
        String addr;
        String serviceKey = "jENXI1lavhLBnweHBWDKwAfCcvSEqooh5DshJSNDLGNa%2Bpsd3WMuAuswxdQydH8mbvffg3rWCcYfa5tIo7DVbw%3D%3D";
        addr = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureSidoLIst?sidoName="+input+"&searchCondition=DAILY&pageNo=1&numOfRows=31&ServiceKey="+serviceKey;
        GetXMLTask2 task = new GetXMLTask2();
        task.execute(addr);
    }

    // 행정구역 경계 그리기
    public void draw(){
        Log.d("log","kbc 드로우안");
        drawPolygon143(mMap); // 서울 성동구
        Log.d("log","kbc 성동구후");
        drawPolygon145(mMap);
        drawPolygon151(mMap);
        drawPolygon158(mMap);
        drawPolygon155(mMap);
        drawPolygon160(mMap);
        drawPolygon164(mMap);
        drawPolygon156(mMap);
        drawPolygon152(mMap);
        drawPolygon153(mMap);
        drawPolygon144(mMap);
        drawPolygon157(mMap);
        drawPolygon161(mMap);
        drawPolygon154(mMap);
        drawPolygon146(mMap);
        drawPolygon150(mMap);
        drawPolygon163(mMap);
        drawPolygon147(mMap);
        drawPolygon169(mMap);
        drawPolygon159(mMap);
        drawPolygon142(mMap);
        drawPolygon162(mMap);
        drawPolygon148(mMap);
        drawPolygon166(mMap);
    }
    public void draw2(){
        drawPolygon41(mMap);
        drawPolygon57(mMap);
        drawPolygon33(mMap);
        drawPolygon47(mMap);
        drawPolygon22(mMap);
        drawPolygon32(mMap);
        drawPolygon59(mMap);
        drawPolygon24(mMap);
        drawPolygon25(mMap);
        drawPolygon45(mMap);
        drawPolygon51(mMap);
        drawPolygon40(mMap);
        drawPolygon46(mMap);
        drawPolygon50(mMap);
    }


    //서울시
    public void drawPolygon143(GoogleMap googlemap) { //서울 성동구
        String name = "성동구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.57275246810175, 127.04241813085706),
                        new LatLng(37.57038253579033, 127.04794980454399),
                        new LatLng(37.56231553903832, 127.05876047165025),
                        new LatLng(37.5594131360664,  127.07373408220053),
                        new LatLng(37.52832388381049, 127.05621773388143),
                        new LatLng(37.52832388381049, 127.05621773388143),
                        new LatLng(37.53423885672233, 127.04604398310076),
                        new LatLng(37.53582328355087, 127.03979942567628),
                        new LatLng(37.53581367627865, 127.0211714455564),
                        new LatLng(37.53378887274516, 127.01719284893274),
                        new LatLng(37.537681185520256, 127.01726163044557),
                        new LatLng(37.53938672166098, 127.00993448922989),
                        new LatLng(37.54157804358092, 127.00879872996808),
                        new LatLng(37.54502351209897, 127.00815187343248),
                        new LatLng(37.547466935106435, 127.00931996404753),
                        new LatLng(37.55264513061776, 127.01620129137214),
                        new LatLng(37.556850715898484, 127.01807638779917),
                        new LatLng(37.55779412537163, 127.0228934248264),
                        new LatLng(37.5607276739534, 127.02339232029838),
                        new LatLng(37.563390358462826, 127.02652159646888),
                        new LatLng(37.56505173515675, 127.02678930885806),
                        new LatLng(37.565200182350495, 127.02358387477513),
                        new LatLng(37.57190723475508, 127.02337770475695),
                        new LatLng(37.56978273516453, 127.03099733100001),
                        new LatLng(37.57302061077518, 127.0381755492195),
                        new LatLng(37.57275246810175, 127.04241813085706))
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"성동구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "성동구\n   "+hmap.get(name), new LatLng(37.551220, 127.041004));

    }//서울 성동구
    public void drawPolygon145(GoogleMap googlemap) { //서울 용산
        String name = "용산구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
            Log.d("log","kbc   white");
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
            Log.d("log","kbc   red");
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
            Log.d("log","kbc   yellow");
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
            Log.d("log","kbc   green");
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
            Log.d("log","kbc   blue");
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(37.5548768201904, 126.96966524449994),
                        new LatLng(37.55308718044556, 126.97642899633566),
                        new LatLng(37.55522076659584, 126.97654602427454),
                        new LatLng(37.55320655210504, 126.97874667968763),
                        new LatLng(37.55368689494708, 126.98541456064552),
                        new LatLng(37.54722934282707, 126.995229135048),
                        new LatLng(37.549694559809545, 126.99832516302801),
                        new LatLng(37.550159406110104, 127.00436818301327),
                        new LatLng(37.54820235864802, 127.0061334023129),
                        new LatLng(37.546169758665414, 127.00499711608721),
                        new LatLng(37.54385947805103, 127.00727818360471),
                        new LatLng(37.54413326436179, 127.00898460651953),
                        new LatLng(37.539639030116945, 127.00959054834321),
                        new LatLng(37.537681185520256, 127.01726163044557),
                        new LatLng(37.53378887274516, 127.01719284893274),
                        new LatLng(37.52290225898471, 127.00614038053493),
                        new LatLng(37.51309192794448, 126.99070240960813),
                        new LatLng(37.50654651085339, 126.98553683648308),
                        new LatLng(37.50702053393398, 126.97524914998174),
                        new LatLng(37.51751820477105, 126.94988506562748),
                        new LatLng(37.52702918583156, 126.94987870367682),
                        new LatLng(37.534519656862926, 126.94481851935942),
                        new LatLng(37.537500243531994, 126.95335659960566),
                        new LatLng(37.54231338779177, 126.95817394011969),
                        new LatLng(37.54546318600178, 126.95790512689311),
                        new LatLng(37.548791603525764, 126.96371984820232),
                        new LatLng(37.55155543391863, 126.96233786542686),
                        new LatLng(37.5541513366375, 126.9657135934734),
                        new LatLng(37.55566236579088, 126.9691850696746),
                        new LatLng(37.5548768201904, 126.96966524449994))
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"용산구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "용산구\n   "+hmap.get(name), new LatLng(37.531597, 126.979828));
    }//서울 용산구
    public void drawPolygon151(GoogleMap googlemap) { //서울 강남구
        String name = "강남구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
            Log.d("log","kbc   white");
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
            Log.d("log","kbc   red");
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
            Log.d("log","kbc   yellow");
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
            Log.d("log","kbc   green");
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
            Log.d("log","kbc   blue");
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.466521, 127.124207),
                        new LatLng(37.462201, 127.117475),
                        new LatLng(37.458641, 127.116896),
                        new LatLng(37.462407, 127.106167),
                        new LatLng(37.456219, 127.099096),
                        new LatLng(37.456394, 127.095223),
                        new LatLng(37.461004, 127.095677),
                        new LatLng(37.4712, 127.085028),
                        new LatLng(37.475512, 127.084882),
                        new LatLng(37.475158, 127.076911),
                        new LatLng(37.469336, 127.065099),
                        new LatLng(37.467268, 127.050802),
                        new LatLng(37.47006, 127.048669),
                        new LatLng(37.471635, 127.050826),
                        new LatLng(37.479942, 127.044005),
                        new LatLng(37.480561, 127.043889),
                        new LatLng(37.485436, 127.041751),
                        new LatLng(37.484369, 127.034087),
                        new LatLng(37.521812, 127.017822),
                        new LatLng(37.524849, 127.015224),
                        new LatLng(37.522588, 127.013177),
                        new LatLng(37.525595, 127.008578),
                        new LatLng(37.525913, 127.008899),
                        new LatLng(37.526474, 127.009444),
                        new LatLng(37.527003, 127.009962),
                        new LatLng(37.527042, 127.010001),
                        new LatLng(37.527135, 127.010085),
                        new LatLng(37.527528, 127.010439),
                        new LatLng(37.527572, 127.010478),
                        new LatLng(37.528617, 127.011418),
                        new LatLng(37.529536, 127.012291),
                        new LatLng(37.52968, 127.012453),
                        new LatLng(37.53055, 127.013345),
                        new LatLng(37.531206, 127.014034),
                        new LatLng(37.531566, 127.014419),
                        new LatLng(37.532132, 127.01502),
                        new LatLng(37.532812, 127.016008),
                        new LatLng(37.533048, 127.016294),
                        new LatLng(37.533779, 127.017183),
                        new LatLng(37.533928, 127.017361),
                        new LatLng(37.533993, 127.01744),
                        new LatLng(37.53582, 127.021173),
                        new LatLng(37.535774, 127.040193),
                        new LatLng(37.528324, 127.056219),
                        new LatLng(37.52507, 127.065456),
                        new LatLng(37.525069, 127.065468),
                        new LatLng(37.524584, 127.067501),
                        new LatLng(37.502739, 127.069817),
                        new LatLng(37.502535, 127.071938),
                        new LatLng(37.502504, 127.072271),
                        new LatLng(37.496681, 127.094794),
                        new LatLng(37.490333, 127.10695),
                        new LatLng(37.466521, 127.124207))
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"강남구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "강남구\n   "+hmap.get(name), new LatLng(37.496918, 127.063319));
    }//서울 강남구
    public void drawPolygon158(GoogleMap googlemap) { //서울 동대문구
        String name = "동대문구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
            Log.d("log","kbc   white");
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
            Log.d("log","kbc   red");
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
            Log.d("log","kbc   yellow");
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
            Log.d("log","kbc   green");
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
            Log.d("log","kbc   blue");
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.607062869017085, 127.07111288773496),
                        new LatLng(37.60107201319839, 127.07287376670605),
                        new LatLng(37.59724304056685, 127.06949105186925),
                        new LatLng(37.58953367466315, 127.07030363208528),
                        new LatLng(37.58651213184981, 127.07264218709383),
                        new LatLng(37.5849555116177, 127.07216063016078),
                        new LatLng(37.58026781100598, 127.07619547037923),
                        new LatLng(37.571869232268774, 127.0782018408153),
                        new LatLng(37.559961773835425, 127.07239004251258),
                        new LatLng(37.56231553903832, 127.05876047165025),
                        new LatLng(37.57038253579033, 127.04794980454399),
                        new LatLng(37.572878529071055, 127.04263554582458),
                        new LatLng(37.57302061077518, 127.0381755492195),
                        new LatLng(37.56978273516453, 127.03099733100001),
                        new LatLng(37.57190723475508, 127.02337770475695),
                        new LatLng(37.57838361223621, 127.0232348231103),
                        new LatLng(37.58268174514337, 127.02953994610249),
                        new LatLng(37.58894739851823, 127.03553876830637),
                        new LatLng(37.5911852565689, 127.03621919708065),
                        new LatLng(37.59126734230753, 127.03875553445558),
                        new LatLng(37.5956815721534, 127.04062845365279),
                        new LatLng(37.5969637344377, 127.04302522879048),
                        new LatLng(37.59617641777492, 127.04734129391157),
                        new LatLng(37.60117358544485, 127.05101351973708),
                        new LatLng(37.600149587503246, 127.05209540476308),
                        new LatLng(37.60132672748398, 127.05508130598699),
                        new LatLng(37.6010580545608, 127.05917142337097),
                        new LatLng(37.605121767227374, 127.06219611364686),
                        new LatLng(37.607062869017085, 127.07111288773496)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"동대문구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "동대문구\n    "+hmap.get(name), new LatLng(37.582350, 127.055016));
    }//서울 동대문구
    public void drawPolygon155(GoogleMap googlemap) { //서울 중구
        String name = "중구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
            Log.d("log","kbc   white");
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
            Log.d("log","kbc   red");
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
            Log.d("log","kbc   yellow");
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
            Log.d("log","kbc   green");
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
            Log.d("log","kbc   blue");
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.563894, 127.02675),
                        new LatLng(37.557829, 127.022889),
                        new LatLng(37.557495, 127.019577),
                        new LatLng(37.557263, 127.019681),
                        new LatLng(37.55261, 127.016162),
                        new LatLng(37.552589, 127.015983),
                        new LatLng(37.547475, 127.009323),
                        new LatLng(37.544135, 127.008988),
                        new LatLng(37.543943, 127.007823),
                        new LatLng(37.543886, 127.007459),
                        new LatLng(37.54386, 127.007295),
                        new LatLng(37.543863, 127.007277),
                        new LatLng(37.543857, 127.007266),
                        new LatLng(37.544693, 127.006446),
                        new LatLng(37.546198, 127.005011),
                        new LatLng(37.546875, 127.005306),
                        new LatLng(37.547349, 127.00533),
                        new LatLng(37.547525, 127.005567),
                        new LatLng(37.547603, 127.005603),
                        new LatLng(37.547657, 127.00565),
                        new LatLng(37.547768, 127.005745),
                        new LatLng(37.548356, 127.006082),
                        new LatLng(37.548371, 127.006076),
                        new LatLng(37.548282, 127.005695),
                        new LatLng(37.549624, 127.004572),
                        new LatLng(37.550154, 127.004373),
                        new LatLng(37.550211, 127.004336),
                        new LatLng(37.550113, 127.003996),
                        new LatLng(37.550034, 127.003467),
                        new LatLng(37.549636, 127.002843),
                        new LatLng(37.549633, 127.002827),
                        new LatLng(37.549639, 127.00265),
                        new LatLng(37.55006, 127.001684),
                        new LatLng(37.550022, 127.00134),
                        new LatLng(37.550015, 127.001327),
                        new LatLng(37.549902, 127.001121),
                        new LatLng(37.549897, 127.001069),
                        new LatLng(37.549936, 127.000945),
                        new LatLng(37.550021, 127.000523),
                        new LatLng(37.550013, 127.000497),
                        new LatLng(37.549818, 127.000124),
                        new LatLng(37.549818, 127.000104),
                        new LatLng(37.549733, 126.999095),
                        new LatLng(37.549795, 126.998909),
                        new LatLng(37.549159, 126.997429),
                        new LatLng(37.548521, 126.996748),
                        new LatLng(37.548239, 126.996755),
                        new LatLng(37.54797, 126.996747),
                        new LatLng(37.547449, 126.996355),
                        new LatLng(37.547328, 126.994809),
                        new LatLng(37.547365, 126.994697),
                        new LatLng(37.547488, 126.994459),
                        new LatLng(37.548869, 126.993325),
                        new LatLng(37.549104, 126.993111),
                        new LatLng(37.550528, 126.991297),
                        new LatLng(37.55079, 126.990976),
                        new LatLng(37.55106, 126.990786),
                        new LatLng(37.551118, 126.990706),
                        new LatLng(37.551281, 126.990239),
                        new LatLng(37.55126, 126.989892),
                        new LatLng(37.551322, 126.989477),
                        new LatLng(37.551277, 126.989379),
                        new LatLng(37.551475, 126.988066),
                        new LatLng(37.551504, 126.988033),
                        new LatLng(37.551451, 126.987719),
                        new LatLng(37.551493, 126.987564),
                        new LatLng(37.551671, 126.987424),
                        new LatLng(37.551812, 126.987407),
                        new LatLng(37.553521, 126.985818),
                        new LatLng(37.553705, 126.985418),
                        new LatLng(37.553697, 126.985399),
                        new LatLng(37.553129, 126.984603),
                        new LatLng(37.553116, 126.983683),
                        new LatLng(37.553383, 126.983153),
                        new LatLng(37.553373, 126.983044),
                        new LatLng(37.553635, 126.982198),
                        new LatLng(37.55381, 126.981187),
                        new LatLng(37.553848, 126.980942),
                        new LatLng(37.553868, 126.98089),
                        new LatLng(37.55377, 126.980775),
                        new LatLng(37.553323, 126.979651),
                        new LatLng(37.553205, 126.978732),
                        new LatLng(37.554567, 126.977857),
                        new LatLng(37.554639, 126.977751),
                        new LatLng(37.555133, 126.976845),
                        new LatLng(37.555139, 126.976827),
                        new LatLng(37.555219, 126.976548),
                        new LatLng(37.554651, 126.976523),
                        new LatLng(37.554198, 126.976503),
                        new LatLng(37.554066, 126.9765),
                        new LatLng(37.55323, 126.976434),
                        new LatLng(37.553089, 126.976428),
                        new LatLng(37.553388, 126.975832),
                        new LatLng(37.554114, 126.973354),
                        new LatLng(37.554123, 126.973339),
                        new LatLng(37.554262, 126.97309),
                        new LatLng(37.554876, 126.969172),
                        new LatLng(37.555662, 126.969183),
                        new LatLng(37.554621, 126.967161),
                        new LatLng(37.55454, 126.966996),
                        new LatLng(37.554496, 126.966875),
                        new LatLng(37.55447, 126.966754),
                        new LatLng(37.554435, 126.966637),
                        new LatLng(37.554348, 126.966354),
                        new LatLng(37.554224, 126.965951),
                        new LatLng(37.554206, 126.965895),
                        new LatLng(37.554152, 126.96571),
                        new LatLng(37.554145, 126.9657),
                        new LatLng(37.55337, 126.964634),
                        new LatLng(37.553264, 126.964516),
                        new LatLng(37.551549, 126.962343),
                        new LatLng(37.558982, 126.961689),
                        new LatLng(37.561976, 126.969464),
                        new LatLng(37.56582, 126.966698),
                        new LatLng(37.569319, 126.973205),
                        new LatLng(37.568136, 126.990151),
                        new LatLng(37.571914, 127.023365),
                        new LatLng(37.571907, 127.023392),
                        new LatLng(37.565179, 127.023585),
                        new LatLng(37.563894, 127.02675)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"중구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "중구\n "+hmap.get(name), new LatLng(37.560041, 126.995786));


    }//서울 중구
    public void drawPolygon160(GoogleMap googlemap) { //서울 종로구
        String name = "종로구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.571914, 127.023365),
                        new LatLng(37.568136, 126.990151),
                        new LatLng(37.569319, 126.973205),
                        new LatLng(37.56582, 126.966698),
                        new LatLng(37.578749, 126.953559),
                        new LatLng(37.580985, 126.958173),
                        new LatLng(37.595034, 126.959051),
                        new LatLng(37.598367, 126.957714),
                        new LatLng(37.598759, 126.953061),
                        new LatLng(37.605073, 126.954061),
                        new LatLng(37.610617, 126.950362),
                        new LatLng(37.624335, 126.949002),
                        new LatLng(37.62947, 126.958418),
                        new LatLng(37.631698, 126.975369),
                        new LatLng(37.631233, 126.975095),
                        new LatLng(37.629101, 126.976362),
                        new LatLng(37.629124, 126.976623),
                        new LatLng(37.629289, 126.978419),
                        new LatLng(37.629287, 126.978439),
                        new LatLng(37.628971, 126.979296),
                        new LatLng(37.628933, 126.979409),
                        new LatLng(37.6289, 126.979507),
                        new LatLng(37.628894, 126.979525),
                        new LatLng(37.628888, 126.979537),
                        new LatLng(37.628561, 126.97993),
                        new LatLng(37.628432, 126.979982),
                        new LatLng(37.627928, 126.980186),
                        new LatLng(37.624497, 126.981688),
                        new LatLng(37.624323, 126.98183),
                        new LatLng(37.615999, 126.984543),
                        new LatLng(37.615504, 126.985163),
                        new LatLng(37.615141, 126.985415),
                        new LatLng(37.613979, 126.986584),
                        new LatLng(37.613223, 126.986138),
                        new LatLng(37.61302, 126.986184),
                        new LatLng(37.604471, 126.986779),
                        new LatLng(37.604017, 126.986666),
                        new LatLng(37.603643, 126.985935),
                        new LatLng(37.603452, 126.985757),
                        new LatLng(37.602258, 126.982566),
                        new LatLng(37.601828, 126.981287),
                        new LatLng(37.601623, 126.980669),
                        new LatLng(37.601471, 126.980475),
                        new LatLng(37.601, 126.978502),
                        new LatLng(37.600798, 126.97825),
                        new LatLng(37.600307, 126.977935),
                        new LatLng(37.60023, 126.977683),
                        new LatLng(37.599416, 126.977416),
                        new LatLng(37.599392, 126.977413),
                        new LatLng(37.599101, 126.977415),
                        new LatLng(37.598643, 126.977209),
                        new LatLng(37.598455, 126.977142),
                        new LatLng(37.598291, 126.977029),
                        new LatLng(37.597906, 126.976719),
                        new LatLng(37.597864, 126.976761),
                        new LatLng(37.597716, 126.976905),
                        new LatLng(37.59766, 126.977301),
                        new LatLng(37.597684, 126.977954),
                        new LatLng(37.5975, 126.978165),
                        new LatLng(37.597441, 126.978353),
                        new LatLng(37.596786, 126.978572),
                        new LatLng(37.5967, 126.978652),
                        new LatLng(37.596394, 126.979111),
                        new LatLng(37.596352, 126.979358),
                        new LatLng(37.595913, 126.980628),
                        new LatLng(37.595862, 126.980723),
                        new LatLng(37.595839, 126.98091),
                        new LatLng(37.595757, 126.981026),
                        new LatLng(37.595336, 126.981488),
                        new LatLng(37.595317, 126.981556),
                        new LatLng(37.594685, 126.983467),
                        new LatLng(37.594435, 126.983844),
                        new LatLng(37.594375, 126.984504),
                        new LatLng(37.594389, 126.984616),
                        new LatLng(37.594396, 126.984642),
                        new LatLng(37.594467, 126.984879),
                        new LatLng(37.594456, 126.984995),
                        new LatLng(37.594449, 126.985429),
                        new LatLng(37.594255, 126.985779),
                        new LatLng(37.594066, 126.985914),
                        new LatLng(37.594011, 126.985898),
                        new LatLng(37.593777, 126.986119),
                        new LatLng(37.593698, 126.98612),
                        new LatLng(37.593509, 126.986173),
                        new LatLng(37.593276, 126.986268),
                        new LatLng(37.59275, 126.986698),
                        new LatLng(37.592729, 126.987061),
                        new LatLng(37.592039, 126.988215),
                        new LatLng(37.591941, 126.988517),
                        new LatLng(37.591712, 126.993586),
                        new LatLng(37.591696, 126.993594),
                        new LatLng(37.591902, 126.994952),
                        new LatLng(37.59202, 126.995097),
                        new LatLng(37.592062, 126.995144),
                        new LatLng(37.592218, 126.995257),
                        new LatLng(37.59228, 126.995362),
                        new LatLng(37.592268, 126.995442),
                        new LatLng(37.592197, 126.995838),
                        new LatLng(37.59213, 126.996103),
                        new LatLng(37.592123, 126.996184),
                        new LatLng(37.592101, 126.996414),
                        new LatLng(37.592122, 126.996507),
                        new LatLng(37.592221, 126.996771),
                        new LatLng(37.592219, 126.996865),
                        new LatLng(37.592218, 126.996972),
                        new LatLng(37.592285, 126.997654),
                        new LatLng(37.592291, 126.997674),
                        new LatLng(37.592332, 126.997783),
                        new LatLng(37.592354, 126.997879),
                        new LatLng(37.592395, 126.998063),
                        new LatLng(37.592432, 126.99825),
                        new LatLng(37.592468, 126.998442),
                        new LatLng(37.592468, 126.998728),
                        new LatLng(37.592451, 126.998781),
                        new LatLng(37.592446, 126.998802),
                        new LatLng(37.592432, 126.99886),
                        new LatLng(37.59233, 126.999201),
                        new LatLng(37.592237, 126.999768),
                        new LatLng(37.592213, 126.999906),
                        new LatLng(37.592307, 127.00044),
                        new LatLng(37.592313, 127.000526),
                        new LatLng(37.592321, 127.000753),
                        new LatLng(37.59231, 127.000859),
                        new LatLng(37.592301, 127.000953),
                        new LatLng(37.592095, 127.001126),
                        new LatLng(37.591911, 127.001374),
                        new LatLng(37.591828, 127.001445),
                        new LatLng(37.591352, 127.001732),
                        new LatLng(37.590876, 127.001649),
                        new LatLng(37.590803, 127.00161),
                        new LatLng(37.590719, 127.001553),
                        new LatLng(37.590698, 127.001541),
                        new LatLng(37.590571, 127.001491),
                        new LatLng(37.590547, 127.001483),
                        new LatLng(37.590551, 127.001463),
                        new LatLng(37.590349, 127.00147),
                        new LatLng(37.590266, 127.001472),
                        new LatLng(37.590149, 127.001469),
                        new LatLng(37.590023, 127.001666),
                        new LatLng(37.589861, 127.001868),
                        new LatLng(37.589684, 127.002006),
                        new LatLng(37.589468, 127.002147),
                        new LatLng(37.589044, 127.002345),
                        new LatLng(37.588887, 127.002629),
                        new LatLng(37.588772, 127.002823),
                        new LatLng(37.58873, 127.002941),
                        new LatLng(37.588604, 127.003353),
                        new LatLng(37.588569, 127.003393),
                        new LatLng(37.588531, 127.003415),
                        new LatLng(37.588415, 127.003472),
                        new LatLng(37.58833, 127.003463),
                        new LatLng(37.588145, 127.003517),
                        new LatLng(37.588096, 127.003566),
                        new LatLng(37.588043, 127.003717),
                        new LatLng(37.587945, 127.003988),
                        new LatLng(37.587747, 127.003892),
                        new LatLng(37.587803, 127.004002),
                        new LatLng(37.585546, 127.006551),
                        new LatLng(37.585423, 127.006655),
                        new LatLng(37.584293, 127.00701),
                        new LatLng(37.584091, 127.00707),
                        new LatLng(37.580468, 127.008643),
                        new LatLng(37.58157, 127.011788),
                        new LatLng(37.581575, 127.012083),
                        new LatLng(37.582325, 127.014828),
                        new LatLng(37.582239, 127.014916),
                        new LatLng(37.582153, 127.015003),
                        new LatLng(37.582082, 127.015182),
                        new LatLng(37.582043, 127.015235),
                        new LatLng(37.582033, 127.015247),
                        new LatLng(37.582026, 127.015256),
                        new LatLng(37.581987, 127.015317),
                        new LatLng(37.581974, 127.01533),
                        new LatLng(37.581936, 127.015342),
                        new LatLng(37.581779, 127.015612),
                        new LatLng(37.581811, 127.015761),
                        new LatLng(37.581849, 127.015978),
                        new LatLng(37.581885, 127.016127),
                        new LatLng(37.581918, 127.016276),
                        new LatLng(37.581936, 127.016358),
                        new LatLng(37.581925, 127.016384),
                        new LatLng(37.58182, 127.016477),
                        new LatLng(37.581714, 127.016683),
                        new LatLng(37.581665, 127.016704),
                        new LatLng(37.58143, 127.016828),
                        new LatLng(37.581216, 127.016806),
                        new LatLng(37.581176, 127.016812),
                        new LatLng(37.580844, 127.016917),
                        new LatLng(37.580839, 127.016924),
                        new LatLng(37.580831, 127.01694),
                        new LatLng(37.580778, 127.017034),
                        new LatLng(37.580722, 127.017138),
                        new LatLng(37.580714, 127.017146),
                        new LatLng(37.580695, 127.01716),
                        new LatLng(37.580561, 127.017254),
                        new LatLng(37.580553, 127.017277),
                        new LatLng(37.580468, 127.017285),
                        new LatLng(37.580351, 127.017306),
                        new LatLng(37.580282, 127.017302),
                        new LatLng(37.580272, 127.017301),
                        new LatLng(37.580265, 127.0173),
                        new LatLng(37.580248, 127.017304),
                        new LatLng(37.58024, 127.017328),
                        new LatLng(37.580238, 127.017333),
                        new LatLng(37.580194, 127.017349),
                        new LatLng(37.579925, 127.017487),
                        new LatLng(37.579847, 127.017534),
                        new LatLng(37.579788, 127.017556),
                        new LatLng(37.579706, 127.017597),
                        new LatLng(37.579687, 127.017655),
                        new LatLng(37.579667, 127.017699),
                        new LatLng(37.579583, 127.017842),
                        new LatLng(37.579063, 127.018267),
                        new LatLng(37.579029, 127.01828),
                        new LatLng(37.578139, 127.017695),
                        new LatLng(37.578113, 127.017693),
                        new LatLng(37.577977, 127.017806),
                        new LatLng(37.577961, 127.017779),
                        new LatLng(37.577939, 127.017814),
                        new LatLng(37.577826, 127.017925),
                        new LatLng(37.577737, 127.018019),
                        new LatLng(37.577578, 127.018107),
                        new LatLng(37.577671, 127.018418),
                        new LatLng(37.577666, 127.018936),
                        new LatLng(37.57769, 127.018986),
                        new LatLng(37.577706, 127.019022),
                        new LatLng(37.577733, 127.019114),
                        new LatLng(37.577814, 127.019187),
                        new LatLng(37.577908, 127.019502),
                        new LatLng(37.57808, 127.019371),
                        new LatLng(37.578099, 127.019415),
                        new LatLng(37.578145, 127.019533),
                        new LatLng(37.578324, 127.019999),
                        new LatLng(37.578326, 127.020003),
                        new LatLng(37.578329, 127.02001),
                        new LatLng(37.578337, 127.020031),
                        new LatLng(37.578347, 127.020058),
                        new LatLng(37.578363, 127.0201),
                        new LatLng(37.578368, 127.020111),
                        new LatLng(37.57837, 127.020117),
                        new LatLng(37.578372, 127.020123),
                        new LatLng(37.578382, 127.020148),
                        new LatLng(37.578432, 127.020275),
                        new LatLng(37.57844, 127.020297),
                        new LatLng(37.578467, 127.020365),
                        new LatLng(37.578481, 127.020401),
                        new LatLng(37.578517, 127.020491),
                        new LatLng(37.57852, 127.0205),
                        new LatLng(37.578535, 127.020534),
                        new LatLng(37.578609, 127.020709),
                        new LatLng(37.578693, 127.020898),
                        new LatLng(37.578721, 127.020977),
                        new LatLng(37.578754, 127.021063),
                        new LatLng(37.578819, 127.021262),
                        new LatLng(37.578855, 127.021406),
                        new LatLng(37.578863, 127.021644),
                        new LatLng(37.578861, 127.021703),
                        new LatLng(37.578871, 127.021793),
                        new LatLng(37.578862, 127.021825),
                        new LatLng(37.57884, 127.021846),
                        new LatLng(37.578808, 127.021884),
                        new LatLng(37.578789, 127.021928),
                        new LatLng(37.578752, 127.021961),
                        new LatLng(37.578743, 127.021981),
                        new LatLng(37.578682, 127.022154),
                        new LatLng(37.578646, 127.022264),
                        new LatLng(37.578642, 127.022285),
                        new LatLng(37.578578, 127.022371),
                        new LatLng(37.578519, 127.022429),
                        new LatLng(37.578441, 127.022513),
                        new LatLng(37.578374, 127.022648),
                        new LatLng(37.578335, 127.022757),
                        new LatLng(37.578275, 127.022706),
                        new LatLng(37.578261, 127.022693),
                        new LatLng(37.577827, 127.022883),
                        new LatLng(37.577825, 127.022892),
                        new LatLng(37.577827, 127.022976),
                        new LatLng(37.577889, 127.02303),
                        new LatLng(37.57799, 127.023115),
                        new LatLng(37.578018, 127.023132),
                        new LatLng(37.578021, 127.023139),
                        new LatLng(37.571914, 127.023365)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"종로구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "종로구\n   "+hmap.get(name), new LatLng(37.595601, 126.977230));
    }//서울 종로구
    public void drawPolygon164(GoogleMap googlemap) { //서울 성북구
        String name = "성북구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.607629, 127.071061),
                        new LatLng(37.607469, 127.070573),
                        new LatLng(37.607364, 127.070526),
                        new LatLng(37.607036, 127.070371),
                        new LatLng(37.607038, 127.070355),
                        new LatLng(37.607036, 127.070301),
                        new LatLng(37.607033, 127.070256),
                        new LatLng(37.607266, 127.07006),
                        new LatLng(37.607258, 127.069985),
                        new LatLng(37.607271, 127.069901),
                        new LatLng(37.607278, 127.069853),
                        new LatLng(37.606961, 127.069125),
                        new LatLng(37.606913, 127.069069),
                        new LatLng(37.606779, 127.068739),
                        new LatLng(37.606775, 127.068734),
                        new LatLng(37.60634, 127.067435),
                        new LatLng(37.606323, 127.067391),
                        new LatLng(37.605139, 127.062241),
                        new LatLng(37.60512, 127.0622),
                        new LatLng(37.605125, 127.062188),
                        new LatLng(37.60503, 127.062165),
                        new LatLng(37.604937, 127.062178),
                        new LatLng(37.604927, 127.06216),
                        new LatLng(37.604896, 127.062098),
                        new LatLng(37.604897, 127.062022),
                        new LatLng(37.604893, 127.061962),
                        new LatLng(37.604869, 127.06193),
                        new LatLng(37.60424, 127.06144),
                        new LatLng(37.603484, 127.060765),
                        new LatLng(37.603531, 127.060712),
                        new LatLng(37.603414, 127.060428),
                        new LatLng(37.603055, 127.060101),
                        new LatLng(37.602843, 127.060139),
                        new LatLng(37.602562, 127.060059),
                        new LatLng(37.602446, 127.059955),
                        new LatLng(37.600148, 127.052085),
                        new LatLng(37.600488, 127.0518),
                        new LatLng(37.599757, 127.049746),
                        new LatLng(37.599146, 127.0494),
                        new LatLng(37.599028, 127.049321),
                        new LatLng(37.598992, 127.049296),
                        new LatLng(37.596153, 127.047306),
                        new LatLng(37.596042, 127.046769),
                        new LatLng(37.596433, 127.045807),
                        new LatLng(37.59661, 127.045527),
                        new LatLng(37.596366, 127.044795),
                        new LatLng(37.596365, 127.044792),
                        new LatLng(37.596363, 127.044785),
                        new LatLng(37.596362, 127.044777),
                        new LatLng(37.596368, 127.044643),
                        new LatLng(37.596555, 127.042155),
                        new LatLng(37.596532, 127.042089),
                        new LatLng(37.596453, 127.041856),
                        new LatLng(37.596458, 127.04184),
                        new LatLng(37.595509, 127.041157),
                        new LatLng(37.595519, 127.041144),
                        new LatLng(37.59555, 127.041072),
                        new LatLng(37.595288, 127.040759),
                        new LatLng(37.591233, 127.037399),
                        new LatLng(37.591186, 127.03623),
                        new LatLng(37.591122, 127.036223),
                        new LatLng(37.59048, 127.036295),
                        new LatLng(37.590444, 127.036299),
                        new LatLng(37.590419, 127.036303),
                        new LatLng(37.590406, 127.036304),
                        new LatLng(37.590141, 127.036332),
                        new LatLng(37.590118, 127.036294),
                        new LatLng(37.589696, 127.036057),
                        new LatLng(37.588167, 127.034629),
                        new LatLng(37.588139, 127.034595),
                        new LatLng(37.588077, 127.034524),
                        new LatLng(37.588029, 127.03447),
                        new LatLng(37.587914, 127.03434),
                        new LatLng(37.587877, 127.034297),
                        new LatLng(37.587628, 127.034011),
                        new LatLng(37.587515, 127.033882),
                        new LatLng(37.58728, 127.033667),
                        new LatLng(37.585806, 127.032143),
                        new LatLng(37.585801, 127.032174),
                        new LatLng(37.585778, 127.032226),
                        new LatLng(37.585769, 127.032236),
                        new LatLng(37.58575, 127.032261),
                        new LatLng(37.585679, 127.032302),
                        new LatLng(37.58567, 127.032297),
                        new LatLng(37.585661, 127.032278),
                        new LatLng(37.58559, 127.032147),
                        new LatLng(37.585577, 127.032134),
                        new LatLng(37.58554, 127.032078),
                        new LatLng(37.585529, 127.032053),
                        new LatLng(37.585474, 127.031952),
                        new LatLng(37.58545, 127.031921),
                        new LatLng(37.585368, 127.031745),
                        new LatLng(37.585356, 127.031708),
                        new LatLng(37.585355, 127.031701),
                        new LatLng(37.585366, 127.031675),
                        new LatLng(37.585357, 127.031639),
                        new LatLng(37.585286, 127.031618),
                        new LatLng(37.585208, 127.031596),
                        new LatLng(37.585181, 127.031589),
                        new LatLng(37.585113, 127.031551),
                        new LatLng(37.584933, 127.031433),
                        new LatLng(37.584898, 127.03141),
                        new LatLng(37.584888, 127.031402),
                        new LatLng(37.584818, 127.031339),
                        new LatLng(37.5847, 127.031228),
                        new LatLng(37.584631, 127.031189),
                        new LatLng(37.584589, 127.031166),
                        new LatLng(37.584316, 127.031007),
                        new LatLng(37.584252, 127.030953),
                        new LatLng(37.583915, 127.030651),
                        new LatLng(37.58268, 127.029539),
                        new LatLng(37.582582, 127.029392),
                        new LatLng(37.582337, 127.02902),
                        new LatLng(37.582325, 127.028993),
                        new LatLng(37.582284, 127.028952),
                        new LatLng(37.580637, 127.026548),
                        new LatLng(37.580586, 127.026477),
                        new LatLng(37.579987, 127.025608),
                        new LatLng(37.579688, 127.025167),
                        new LatLng(37.579561, 127.024976),
                        new LatLng(37.57925, 127.024511),
                        new LatLng(37.579188, 127.024407),
                        new LatLng(37.578975, 127.024098),
                        new LatLng(37.578842, 127.023908),
                        new LatLng(37.578841, 127.023906),
                        new LatLng(37.578825, 127.023883),
                        new LatLng(37.578822, 127.023879),
                        new LatLng(37.578816, 127.023869),
                        new LatLng(37.578812, 127.023863),
                        new LatLng(37.578804, 127.023852),
                        new LatLng(37.578702, 127.023706),
                        new LatLng(37.578462, 127.023355),
                        new LatLng(37.578447, 127.023333),
                        new LatLng(37.578445, 127.02333),
                        new LatLng(37.578442, 127.023326),
                        new LatLng(37.578382, 127.023269),
                        new LatLng(37.578201, 127.023268),
                        new LatLng(37.578126, 127.02323),
                        new LatLng(37.578108, 127.023222),
                        new LatLng(37.578021, 127.023139),
                        new LatLng(37.578018, 127.023132),
                        new LatLng(37.57799, 127.023115),
                        new LatLng(37.577889, 127.02303),
                        new LatLng(37.577827, 127.022976),
                        new LatLng(37.577825, 127.022892),
                        new LatLng(37.577827, 127.022883),
                        new LatLng(37.578261, 127.022693),
                        new LatLng(37.578275, 127.022706),
                        new LatLng(37.578335, 127.022757),
                        new LatLng(37.578374, 127.022648),
                        new LatLng(37.578441, 127.022513),
                        new LatLng(37.578519, 127.022429),
                        new LatLng(37.578578, 127.022371),
                        new LatLng(37.578642, 127.022285),
                        new LatLng(37.578646, 127.022264),
                        new LatLng(37.578682, 127.022154),
                        new LatLng(37.578743, 127.021981),
                        new LatLng(37.578752, 127.021961),
                        new LatLng(37.578789, 127.021928),
                        new LatLng(37.578808, 127.021884),
                        new LatLng(37.57884, 127.021846),
                        new LatLng(37.578862, 127.021825),
                        new LatLng(37.578871, 127.021793),
                        new LatLng(37.578861, 127.021703),
                        new LatLng(37.578863, 127.021644),
                        new LatLng(37.578855, 127.021406),
                        new LatLng(37.578819, 127.021262),
                        new LatLng(37.578754, 127.021063),
                        new LatLng(37.578721, 127.020977),
                        new LatLng(37.578693, 127.020898),
                        new LatLng(37.578609, 127.020709),
                        new LatLng(37.578535, 127.020534),
                        new LatLng(37.57852, 127.0205),
                        new LatLng(37.578517, 127.020491),
                        new LatLng(37.578481, 127.020401),
                        new LatLng(37.578467, 127.020365),
                        new LatLng(37.57844, 127.020297),
                        new LatLng(37.578432, 127.020275),
                        new LatLng(37.578382, 127.020148),
                        new LatLng(37.578372, 127.020123),
                        new LatLng(37.57837, 127.020117),
                        new LatLng(37.578368, 127.020111),
                        new LatLng(37.578363, 127.0201),
                        new LatLng(37.578347, 127.020058),
                        new LatLng(37.578337, 127.020031),
                        new LatLng(37.578329, 127.02001),
                        new LatLng(37.578326, 127.020003),
                        new LatLng(37.578324, 127.019999),
                        new LatLng(37.578145, 127.019533),
                        new LatLng(37.578099, 127.019415),
                        new LatLng(37.57808, 127.019371),
                        new LatLng(37.577908, 127.019502),
                        new LatLng(37.577814, 127.019187),
                        new LatLng(37.577733, 127.019114),
                        new LatLng(37.577706, 127.019022),
                        new LatLng(37.57769, 127.018986),
                        new LatLng(37.577666, 127.018936),
                        new LatLng(37.577671, 127.018418),
                        new LatLng(37.577578, 127.018107),
                        new LatLng(37.577737, 127.018019),
                        new LatLng(37.577826, 127.017925),
                        new LatLng(37.577939, 127.017814),
                        new LatLng(37.577961, 127.017779),
                        new LatLng(37.577977, 127.017806),
                        new LatLng(37.578113, 127.017693),
                        new LatLng(37.578139, 127.017695),
                        new LatLng(37.579029, 127.01828),
                        new LatLng(37.579063, 127.018267),
                        new LatLng(37.579583, 127.017842),
                        new LatLng(37.579667, 127.017699),
                        new LatLng(37.579687, 127.017655),
                        new LatLng(37.579706, 127.017597),
                        new LatLng(37.579788, 127.017556),
                        new LatLng(37.579847, 127.017534),
                        new LatLng(37.579925, 127.017487),
                        new LatLng(37.580194, 127.017349),
                        new LatLng(37.580238, 127.017333),
                        new LatLng(37.58024, 127.017328),
                        new LatLng(37.580248, 127.017304),
                        new LatLng(37.580265, 127.0173),
                        new LatLng(37.580272, 127.017301),
                        new LatLng(37.580282, 127.017302),
                        new LatLng(37.580351, 127.017306),
                        new LatLng(37.580468, 127.017285),
                        new LatLng(37.580553, 127.017277),
                        new LatLng(37.580561, 127.017254),
                        new LatLng(37.580695, 127.01716),
                        new LatLng(37.580714, 127.017146),
                        new LatLng(37.580722, 127.017138),
                        new LatLng(37.580778, 127.017034),
                        new LatLng(37.580831, 127.01694),
                        new LatLng(37.580839, 127.016924),
                        new LatLng(37.580844, 127.016917),
                        new LatLng(37.581176, 127.016812),
                        new LatLng(37.581216, 127.016806),
                        new LatLng(37.58143, 127.016828),
                        new LatLng(37.581665, 127.016704),
                        new LatLng(37.581714, 127.016683),
                        new LatLng(37.58182, 127.016477),
                        new LatLng(37.581925, 127.016384),
                        new LatLng(37.581936, 127.016358),
                        new LatLng(37.581918, 127.016276),
                        new LatLng(37.581885, 127.016127),
                        new LatLng(37.581849, 127.015978),
                        new LatLng(37.581811, 127.015761),
                        new LatLng(37.581779, 127.015612),
                        new LatLng(37.581936, 127.015342),
                        new LatLng(37.581974, 127.01533),
                        new LatLng(37.581987, 127.015317),
                        new LatLng(37.582026, 127.015256),
                        new LatLng(37.582033, 127.015247),
                        new LatLng(37.582043, 127.015235),
                        new LatLng(37.582082, 127.015182),
                        new LatLng(37.582153, 127.015003),
                        new LatLng(37.582239, 127.014916),
                        new LatLng(37.582325, 127.014828),
                        new LatLng(37.581575, 127.012083),
                        new LatLng(37.58157, 127.011788),
                        new LatLng(37.580468, 127.008643),
                        new LatLng(37.584091, 127.00707),
                        new LatLng(37.584293, 127.00701),
                        new LatLng(37.585423, 127.006655),
                        new LatLng(37.585546, 127.006551),
                        new LatLng(37.587803, 127.004002),
                        new LatLng(37.587747, 127.003892),
                        new LatLng(37.587945, 127.003988),
                        new LatLng(37.588043, 127.003717),
                        new LatLng(37.588096, 127.003566),
                        new LatLng(37.588145, 127.003517),
                        new LatLng(37.58833, 127.003463),
                        new LatLng(37.588415, 127.003472),
                        new LatLng(37.588531, 127.003415),
                        new LatLng(37.588569, 127.003393),
                        new LatLng(37.588604, 127.003353),
                        new LatLng(37.58873, 127.002941),
                        new LatLng(37.588772, 127.002823),
                        new LatLng(37.588887, 127.002629),
                        new LatLng(37.589044, 127.002345),
                        new LatLng(37.589468, 127.002147),
                        new LatLng(37.589684, 127.002006),
                        new LatLng(37.589861, 127.001868),
                        new LatLng(37.590023, 127.001666),
                        new LatLng(37.590149, 127.001469),
                        new LatLng(37.590266, 127.001472),
                        new LatLng(37.590349, 127.00147),
                        new LatLng(37.590551, 127.001463),
                        new LatLng(37.590547, 127.001483),
                        new LatLng(37.590571, 127.001491),
                        new LatLng(37.590698, 127.001541),
                        new LatLng(37.590719, 127.001553),
                        new LatLng(37.590803, 127.00161),
                        new LatLng(37.590876, 127.001649),
                        new LatLng(37.591352, 127.001732),
                        new LatLng(37.591828, 127.001445),
                        new LatLng(37.591911, 127.001374),
                        new LatLng(37.592095, 127.001126),
                        new LatLng(37.592301, 127.000953),
                        new LatLng(37.59231, 127.000859),
                        new LatLng(37.592321, 127.000753),
                        new LatLng(37.592313, 127.000526),
                        new LatLng(37.592307, 127.00044),
                        new LatLng(37.592213, 126.999906),
                        new LatLng(37.592237, 126.999768),
                        new LatLng(37.59233, 126.999201),
                        new LatLng(37.592432, 126.99886),
                        new LatLng(37.592446, 126.998802),
                        new LatLng(37.592451, 126.998781),
                        new LatLng(37.592468, 126.998728),
                        new LatLng(37.592468, 126.998442),
                        new LatLng(37.592432, 126.99825),
                        new LatLng(37.592395, 126.998063),
                        new LatLng(37.592354, 126.997879),
                        new LatLng(37.592332, 126.997783),
                        new LatLng(37.592291, 126.997674),
                        new LatLng(37.592285, 126.997654),
                        new LatLng(37.592218, 126.996972),
                        new LatLng(37.592219, 126.996865),
                        new LatLng(37.592221, 126.996771),
                        new LatLng(37.592122, 126.996507),
                        new LatLng(37.592101, 126.996414),
                        new LatLng(37.592123, 126.996184),
                        new LatLng(37.59213, 126.996103),
                        new LatLng(37.592197, 126.995838),
                        new LatLng(37.592268, 126.995442),
                        new LatLng(37.59228, 126.995362),
                        new LatLng(37.592218, 126.995257),
                        new LatLng(37.592062, 126.995144),
                        new LatLng(37.59202, 126.995097),
                        new LatLng(37.591902, 126.994952),
                        new LatLng(37.591696, 126.993594),
                        new LatLng(37.591712, 126.993586),
                        new LatLng(37.591941, 126.988517),
                        new LatLng(37.592039, 126.988215),
                        new LatLng(37.592729, 126.987061),
                        new LatLng(37.59275, 126.986698),
                        new LatLng(37.593276, 126.986268),
                        new LatLng(37.593509, 126.986173),
                        new LatLng(37.593698, 126.98612),
                        new LatLng(37.593777, 126.986119),
                        new LatLng(37.594011, 126.985898),
                        new LatLng(37.594066, 126.985914),
                        new LatLng(37.594255, 126.985779),
                        new LatLng(37.594449, 126.985429),
                        new LatLng(37.594456, 126.984995),
                        new LatLng(37.594467, 126.984879),
                        new LatLng(37.594396, 126.984642),
                        new LatLng(37.594389, 126.984616),
                        new LatLng(37.594375, 126.984504),
                        new LatLng(37.594435, 126.983844),
                        new LatLng(37.594685, 126.983467),
                        new LatLng(37.595317, 126.981556),
                        new LatLng(37.595336, 126.981488),
                        new LatLng(37.595757, 126.981026),
                        new LatLng(37.595839, 126.98091),
                        new LatLng(37.595862, 126.980723),
                        new LatLng(37.595913, 126.980628),
                        new LatLng(37.596352, 126.979358),
                        new LatLng(37.596394, 126.979111),
                        new LatLng(37.5967, 126.978652),
                        new LatLng(37.596786, 126.978572),
                        new LatLng(37.597441, 126.978353),
                        new LatLng(37.5975, 126.978165),
                        new LatLng(37.597684, 126.977954),
                        new LatLng(37.59766, 126.977301),
                        new LatLng(37.597716, 126.976905),
                        new LatLng(37.597864, 126.976761),
                        new LatLng(37.597906, 126.976719),
                        new LatLng(37.598291, 126.977029),
                        new LatLng(37.598455, 126.977142),
                        new LatLng(37.598643, 126.977209),
                        new LatLng(37.599101, 126.977415),
                        new LatLng(37.599392, 126.977413),
                        new LatLng(37.599416, 126.977416),
                        new LatLng(37.60023, 126.977683),
                        new LatLng(37.600307, 126.977935),
                        new LatLng(37.600798, 126.97825),
                        new LatLng(37.601, 126.978502),
                        new LatLng(37.601471, 126.980475),
                        new LatLng(37.601623, 126.980669),
                        new LatLng(37.601828, 126.981287),
                        new LatLng(37.602258, 126.982566),
                        new LatLng(37.603452, 126.985757),
                        new LatLng(37.603643, 126.985935),
                        new LatLng(37.604017, 126.986666),
                        new LatLng(37.604471, 126.986779),
                        new LatLng(37.61302, 126.986184),
                        new LatLng(37.613223, 126.986138),
                        new LatLng(37.613979, 126.986584),
                        new LatLng(37.615141, 126.985415),
                        new LatLng(37.615504, 126.985163),
                        new LatLng(37.615999, 126.984543),
                        new LatLng(37.624323, 126.98183),
                        new LatLng(37.624497, 126.981688),
                        new LatLng(37.627928, 126.980186),
                        new LatLng(37.628432, 126.979982),
                        new LatLng(37.628561, 126.97993),
                        new LatLng(37.628888, 126.979537),
                        new LatLng(37.628894, 126.979525),
                        new LatLng(37.6289, 126.979507),
                        new LatLng(37.628933, 126.979409),
                        new LatLng(37.628971, 126.979296),
                        new LatLng(37.629287, 126.978439),
                        new LatLng(37.629289, 126.978419),
                        new LatLng(37.629124, 126.976623),
                        new LatLng(37.629101, 126.976362),
                        new LatLng(37.631233, 126.975095),
                        new LatLng(37.631698, 126.975369),
                        new LatLng(37.636438, 126.983959),
                        new LatLng(37.636337, 126.9842),
                        new LatLng(37.636336, 126.984203),
                        new LatLng(37.636335, 126.984206),
                        new LatLng(37.636331, 126.984215),
                        new LatLng(37.63627, 126.984414),
                        new LatLng(37.635935, 126.985461),
                        new LatLng(37.635797, 126.985894),
                        new LatLng(37.635789, 126.985918),
                        new LatLng(37.635772, 126.98594),
                        new LatLng(37.635739, 126.985983),
                        new LatLng(37.63546, 126.986362),
                        new LatLng(37.634489, 126.988179),
                        new LatLng(37.634193, 126.988391),
                        new LatLng(37.633792, 126.988572),
                        new LatLng(37.633961, 126.989488),
                        new LatLng(37.633154, 126.989883),
                        new LatLng(37.632995, 126.989903),
                        new LatLng(37.632802, 126.989931),
                        new LatLng(37.632799, 126.989934),
                        new LatLng(37.632666, 126.990707),
                        new LatLng(37.632605, 126.99138),
                        new LatLng(37.631448, 126.993722),
                        new LatLng(37.631108, 126.994112),
                        new LatLng(37.630859, 126.994243),
                        new LatLng(37.630847, 126.994257),
                        new LatLng(37.630611, 126.994607),
                        new LatLng(37.629347, 126.996041),
                        new LatLng(37.62909, 126.9965),
                        new LatLng(37.629067, 126.996517),
                        new LatLng(37.628004, 126.997595),
                        new LatLng(37.627841, 126.997694),
                        new LatLng(37.626536, 126.998993),
                        new LatLng(37.62604, 126.999849),
                        new LatLng(37.625645, 127.001762),
                        new LatLng(37.625272, 127.002977),
                        new LatLng(37.624226, 127.003887),
                        new LatLng(37.624055, 127.004433),
                        new LatLng(37.624079, 127.004562),
                        new LatLng(37.624011, 127.004647),
                        new LatLng(37.623824, 127.00488),
                        new LatLng(37.623823, 127.004897),
                        new LatLng(37.62393, 127.005065),
                        new LatLng(37.623846, 127.005565),
                        new LatLng(37.623848, 127.005583),
                        new LatLng(37.623982, 127.005714),
                        new LatLng(37.623987, 127.005785),
                        new LatLng(37.623994, 127.005866),
                        new LatLng(37.624008, 127.006029),
                        new LatLng(37.624013, 127.006097),
                        new LatLng(37.624033, 127.006334),
                        new LatLng(37.624132, 127.00733),
                        new LatLng(37.624022, 127.007856),
                        new LatLng(37.623985, 127.007896),
                        new LatLng(37.6237, 127.007888),
                        new LatLng(37.623348, 127.007744),
                        new LatLng(37.622939, 127.007786),
                        new LatLng(37.622691, 127.007554),
                        new LatLng(37.622656, 127.007544),
                        new LatLng(37.622425, 127.007596),
                        new LatLng(37.621754, 127.007445),
                        new LatLng(37.621653, 127.007495),
                        new LatLng(37.621595, 127.007527),
                        new LatLng(37.621496, 127.007582),
                        new LatLng(37.621471, 127.007577),
                        new LatLng(37.62104, 127.007322),
                        new LatLng(37.620954, 127.00724),
                        new LatLng(37.620828, 127.00721),
                        new LatLng(37.620786, 127.007249),
                        new LatLng(37.620716, 127.00731),
                        new LatLng(37.620582, 127.007258),
                        new LatLng(37.620512, 127.007214),
                        new LatLng(37.620235, 127.007416),
                        new LatLng(37.620044, 127.007571),
                        new LatLng(37.62001, 127.0076),
                        new LatLng(37.619983, 127.007634),
                        new LatLng(37.61986, 127.007864),
                        new LatLng(37.619638, 127.00775),
                        new LatLng(37.619631, 127.007752),
                        new LatLng(37.619319, 127.008167),
                        new LatLng(37.619112, 127.008405),
                        new LatLng(37.618896, 127.008494),
                        new LatLng(37.618672, 127.008553),
                        new LatLng(37.618433, 127.008868),
                        new LatLng(37.618342, 127.008998),
                        new LatLng(37.618253, 127.009363),
                        new LatLng(37.617783, 127.009948),
                        new LatLng(37.617275, 127.010004),
                        new LatLng(37.616944, 127.010139),
                        new LatLng(37.616893, 127.010115),
                        new LatLng(37.616763, 127.01026),
                        new LatLng(37.616288, 127.010938),
                        new LatLng(37.616287, 127.010949),
                        new LatLng(37.616313, 127.01103),
                        new LatLng(37.616421, 127.011441),
                        new LatLng(37.61585, 127.012453),
                        new LatLng(37.615752, 127.01269),
                        new LatLng(37.615535, 127.013399),
                        new LatLng(37.614978, 127.014186),
                        new LatLng(37.614868, 127.014199),
                        new LatLng(37.614675, 127.015254),
                        new LatLng(37.614862, 127.016114),
                        new LatLng(37.614941, 127.016521),
                        new LatLng(37.614942, 127.016534),
                        new LatLng(37.614883, 127.016672),
                        new LatLng(37.614869, 127.016798),
                        new LatLng(37.614819, 127.016907),
                        new LatLng(37.614778, 127.017082),
                        new LatLng(37.614788, 127.017097),
                        new LatLng(37.61456, 127.017637),
                        new LatLng(37.614431, 127.017777),
                        new LatLng(37.614391, 127.017818),
                        new LatLng(37.614387, 127.017818),
                        new LatLng(37.614371, 127.017816),
                        new LatLng(37.613999, 127.018145),
                        new LatLng(37.613842, 127.018521),
                        new LatLng(37.613856, 127.01854),
                        new LatLng(37.613856, 127.018545),
                        new LatLng(37.613115, 127.019948),
                        new LatLng(37.612859, 127.020338),
                        new LatLng(37.612654, 127.020384),
                        new LatLng(37.612598, 127.020397),
                        new LatLng(37.612587, 127.020432),
                        new LatLng(37.612523, 127.020636),
                        new LatLng(37.612513, 127.020666),
                        new LatLng(37.612511, 127.020672),
                        new LatLng(37.612452, 127.021128),
                        new LatLng(37.612406, 127.021646),
                        new LatLng(37.612391, 127.021692),
                        new LatLng(37.612352, 127.021811),
                        new LatLng(37.611719, 127.022056),
                        new LatLng(37.611381, 127.022136),
                        new LatLng(37.611371, 127.022198),
                        new LatLng(37.611399, 127.022273),
                        new LatLng(37.611443, 127.022416),
                        new LatLng(37.611368, 127.022497),
                        new LatLng(37.611368, 127.022504),
                        new LatLng(37.611401, 127.022576),
                        new LatLng(37.611471, 127.022668),
                        new LatLng(37.611513, 127.022735),
                        new LatLng(37.611685, 127.023292),
                        new LatLng(37.611734, 127.023442),
                        new LatLng(37.612, 127.024073),
                        new LatLng(37.612069, 127.024406),
                        new LatLng(37.61208, 127.02448),
                        new LatLng(37.612141, 127.024565),
                        new LatLng(37.612029, 127.024995),
                        new LatLng(37.612014, 127.025181),
                        new LatLng(37.612132, 127.025724),
                        new LatLng(37.612219, 127.02598),
                        new LatLng(37.61223, 127.02601),
                        new LatLng(37.612313, 127.026075),
                        new LatLng(37.612552, 127.026253),
                        new LatLng(37.612694, 127.026434),
                        new LatLng(37.6127, 127.026711),
                        new LatLng(37.612679, 127.026829),
                        new LatLng(37.612678, 127.026836),
                        new LatLng(37.612672, 127.026866),
                        new LatLng(37.612629, 127.026964),
                        new LatLng(37.612605, 127.027065),
                        new LatLng(37.612568, 127.02721),
                        new LatLng(37.612541, 127.027472),
                        new LatLng(37.612586, 127.027615),
                        new LatLng(37.612605, 127.027709),
                        new LatLng(37.612569, 127.027935),
                        new LatLng(37.612404, 127.029101),
                        new LatLng(37.612378, 127.02941),
                        new LatLng(37.612376, 127.029447),
                        new LatLng(37.612374, 127.029519),
                        new LatLng(37.612375, 127.02978),
                        new LatLng(37.612366, 127.029971),
                        new LatLng(37.612362, 127.03018),
                        new LatLng(37.611482, 127.030317),
                        new LatLng(37.611308, 127.030344),
                        new LatLng(37.611297, 127.030345),
                        new LatLng(37.611284, 127.030344),
                        new LatLng(37.611264, 127.030343),
                        new LatLng(37.611162, 127.030338),
                        new LatLng(37.610701, 127.030318),
                        new LatLng(37.610644, 127.030316),
                        new LatLng(37.610606, 127.030314),
                        new LatLng(37.610488, 127.03031),
                        new LatLng(37.610385, 127.030306),
                        new LatLng(37.6103, 127.030303),
                        new LatLng(37.608979, 127.03025),
                        new LatLng(37.608974, 127.030259),
                        new LatLng(37.609802, 127.031765),
                        new LatLng(37.609894, 127.031962),
                        new LatLng(37.610329, 127.03277),
                        new LatLng(37.610465, 127.032929),
                        new LatLng(37.611509, 127.034647),
                        new LatLng(37.611956, 127.035335),
                        new LatLng(37.612084, 127.035505),
                        new LatLng(37.612218, 127.03588),
                        new LatLng(37.612411, 127.036003),
                        new LatLng(37.612433, 127.036049),
                        new LatLng(37.612428, 127.036112),
                        new LatLng(37.612341, 127.036096),
                        new LatLng(37.612358, 127.036176),
                        new LatLng(37.612421, 127.036766),
                        new LatLng(37.612425, 127.036778),
                        new LatLng(37.612567, 127.036979),
                        new LatLng(37.612642, 127.037084),
                        new LatLng(37.612778, 127.037279),
                        new LatLng(37.61279, 127.037296),
                        new LatLng(37.612798, 127.037307),
                        new LatLng(37.612801, 127.037311),
                        new LatLng(37.612809, 127.037322),
                        new LatLng(37.612826, 127.037342),
                        new LatLng(37.612838, 127.037356),
                        new LatLng(37.612917, 127.037457),
                        new LatLng(37.613103, 127.037619),
                        new LatLng(37.613251, 127.037721),
                        new LatLng(37.613575, 127.037821),
                        new LatLng(37.613628, 127.037852),
                        new LatLng(37.613919, 127.038092),
                        new LatLng(37.613994, 127.038221),
                        new LatLng(37.614089, 127.038298),
                        new LatLng(37.614336, 127.038653),
                        new LatLng(37.614652, 127.039085),
                        new LatLng(37.61511, 127.03959),
                        new LatLng(37.615133, 127.039623),
                        new LatLng(37.615145, 127.039641),
                        new LatLng(37.615181, 127.039669),
                        new LatLng(37.615433, 127.039879),
                        new LatLng(37.615488, 127.039894),
                        new LatLng(37.61558, 127.039901),
                        new LatLng(37.615586, 127.039901),
                        new LatLng(37.615716, 127.040002),
                        new LatLng(37.61618, 127.040027),
                        new LatLng(37.616217, 127.040051),
                        new LatLng(37.616112, 127.040411),
                        new LatLng(37.616139, 127.040491),
                        new LatLng(37.616247, 127.040627),
                        new LatLng(37.616571, 127.040968),
                        new LatLng(37.616686, 127.041165),
                        new LatLng(37.616689, 127.041171),
                        new LatLng(37.616761, 127.041273),
                        new LatLng(37.616841, 127.041386),
                        new LatLng(37.616856, 127.041409),
                        new LatLng(37.616869, 127.041427),
                        new LatLng(37.616879, 127.041442),
                        new LatLng(37.616913, 127.041509),
                        new LatLng(37.617132, 127.041834),
                        new LatLng(37.617348, 127.042122),
                        new LatLng(37.617544, 127.042397),
                        new LatLng(37.617614, 127.042494),
                        new LatLng(37.617672, 127.042577),
                        new LatLng(37.617754, 127.042692),
                        new LatLng(37.61781, 127.042772),
                        new LatLng(37.61807, 127.043138),
                        new LatLng(37.61812, 127.043209),
                        new LatLng(37.61818, 127.043292),
                        new LatLng(37.618219, 127.043348),
                        new LatLng(37.618348, 127.043531),
                        new LatLng(37.618383, 127.04358),
                        new LatLng(37.618418, 127.043628),
                        new LatLng(37.618491, 127.043732),
                        new LatLng(37.618501, 127.043747),
                        new LatLng(37.618627, 127.043924),
                        new LatLng(37.618655, 127.043959),
                        new LatLng(37.618726, 127.044023),
                        new LatLng(37.618743, 127.044038),
                        new LatLng(37.619281, 127.044531),
                        new LatLng(37.619326, 127.044572),
                        new LatLng(37.619444, 127.044679),
                        new LatLng(37.619493, 127.044705),
                        new LatLng(37.619856, 127.044899),
                        new LatLng(37.620098, 127.045028),
                        new LatLng(37.620138, 127.045049),
                        new LatLng(37.620447, 127.045214),
                        new LatLng(37.621078, 127.045552),
                        new LatLng(37.621154, 127.045593),
                        new LatLng(37.621207, 127.045622),
                        new LatLng(37.621363, 127.045706),
                        new LatLng(37.621427, 127.045752),
                        new LatLng(37.621601, 127.045881),
                        new LatLng(37.621875, 127.046081),
                        new LatLng(37.621921, 127.046127),
                        new LatLng(37.622097, 127.046303),
                        new LatLng(37.622117, 127.046323),
                        new LatLng(37.622325, 127.046528),
                        new LatLng(37.622355, 127.04656),
                        new LatLng(37.622439, 127.046665),
                        new LatLng(37.622603, 127.046868),
                        new LatLng(37.622622, 127.046899),
                        new LatLng(37.622947, 127.047438),
                        new LatLng(37.622989, 127.047508),
                        new LatLng(37.623236, 127.047926),
                        new LatLng(37.623407, 127.048226),
                        new LatLng(37.623576, 127.048525),
                        new LatLng(37.623616, 127.048593),
                        new LatLng(37.623961, 127.049198),
                        new LatLng(37.623977, 127.049228),
                        new LatLng(37.624011, 127.049286),
                        new LatLng(37.624052, 127.049357),
                        new LatLng(37.624248, 127.049705),
                        new LatLng(37.624266, 127.049737),
                        new LatLng(37.624074, 127.049964),
                        new LatLng(37.623853, 127.050207),
                        new LatLng(37.623551, 127.050492),
                        new LatLng(37.622693, 127.051613),
                        new LatLng(37.622559, 127.051758),
                        new LatLng(37.622242, 127.052112),
                        new LatLng(37.622157, 127.05207),
                        new LatLng(37.620522, 127.053678),
                        new LatLng(37.620459, 127.053735),
                        new LatLng(37.620431, 127.05376),
                        new LatLng(37.620115, 127.054054),
                        new LatLng(37.620099, 127.054069),
                        new LatLng(37.620086, 127.054082),
                        new LatLng(37.620051, 127.054122),
                        new LatLng(37.619796, 127.054408),
                        new LatLng(37.619749, 127.05448),
                        new LatLng(37.619552, 127.054808),
                        new LatLng(37.619419, 127.055078),
                        new LatLng(37.619384, 127.055149),
                        new LatLng(37.619179, 127.055536),
                        new LatLng(37.619148, 127.055586),
                        new LatLng(37.618915, 127.055934),
                        new LatLng(37.618739, 127.056132),
                        new LatLng(37.61867, 127.05621),
                        new LatLng(37.618547, 127.056341),
                        new LatLng(37.617846, 127.057053),
                        new LatLng(37.617734, 127.057162),
                        new LatLng(37.61747, 127.057438),
                        new LatLng(37.617425, 127.057519),
                        new LatLng(37.617385, 127.057593),
                        new LatLng(37.617125, 127.058079),
                        new LatLng(37.616985, 127.058325),
                        new LatLng(37.616969, 127.058353),
                        new LatLng(37.616937, 127.058409),
                        new LatLng(37.61689, 127.058488),
                        new LatLng(37.616639, 127.058878),
                        new LatLng(37.616569, 127.058986),
                        new LatLng(37.616371, 127.05935),
                        new LatLng(37.616307, 127.059467),
                        new LatLng(37.616298, 127.059486),
                        new LatLng(37.616262, 127.059562),
                        new LatLng(37.615972, 127.060204),
                        new LatLng(37.615665, 127.060666),
                        new LatLng(37.615553, 127.060722),
                        new LatLng(37.615356, 127.060824),
                        new LatLng(37.615303, 127.060853),
                        new LatLng(37.615254, 127.060879),
                        new LatLng(37.615232, 127.060891),
                        new LatLng(37.61522, 127.060899),
                        new LatLng(37.615166, 127.060933),
                        new LatLng(37.615142, 127.06095),
                        new LatLng(37.615133, 127.060956),
                        new LatLng(37.615121, 127.060965),
                        new LatLng(37.615104, 127.060976),
                        new LatLng(37.614938, 127.06109),
                        new LatLng(37.614915, 127.061113),
                        new LatLng(37.614755, 127.061288),
                        new LatLng(37.614494, 127.061757),
                        new LatLng(37.614433, 127.062004),
                        new LatLng(37.614427, 127.062032),
                        new LatLng(37.614373, 127.062378),
                        new LatLng(37.614352, 127.062584),
                        new LatLng(37.614314, 127.062936),
                        new LatLng(37.614296, 127.063081),
                        new LatLng(37.614267, 127.063247),
                        new LatLng(37.61427, 127.063285),
                        new LatLng(37.615019, 127.066065),
                        new LatLng(37.615452, 127.068157),
                        new LatLng(37.615398, 127.070164),
                        new LatLng(37.607629, 127.071061)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"성북구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "성북구\n   "+hmap.get(name), new LatLng(37.606220, 127.017533));
    }//서울 성북구
    public void drawPolygon156(GoogleMap googlemap) { //서울 서대문구
        String name = "서대문구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.561976    ,126.969464),
                        new LatLng(37.558982    ,126.961689),
                        new LatLng(37.556546    ,126.941372),
                        new LatLng(37.553451    ,126.933945),
                        new LatLng(37.55658 ,126.932792),
                        new LatLng(37.55885 ,126.926597),
                        new LatLng(37.563332    ,126.928282),
                        new LatLng(37.565105    ,126.926014),
                        new LatLng(37.576005    ,126.902081),
                        new LatLng(37.587225    ,126.912845),
                        new LatLng(37.585881    ,126.916373),
                        new LatLng(37.583095    ,126.915939),
                        new LatLng(37.583456    ,126.92175),
                        new LatLng(37.588387    ,126.927997),
                        new LatLng(37.59455 ,126.930246),
                        new LatLng(37.598716    ,126.940878),
                        new LatLng(37.603076    ,126.939957),
                        new LatLng(37.610617    ,126.950362),
                        new LatLng(37.605073    ,126.954061),
                        new LatLng(37.598759    ,126.953061),
                        new LatLng(37.598367    ,126.957714),
                        new LatLng(37.595034    ,126.959051),
                        new LatLng(37.580985    ,126.958173),
                        new LatLng(37.578749    ,126.953559),
                        new LatLng(37.56582 ,126.966698),
                        new LatLng(37.561976    ,126.969464)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"서대문구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "서대문구\n   "+hmap.get(name), new LatLng(37.578481, 126.939437));
    }//서울 서대문구
    public void drawPolygon153(GoogleMap googlemap) { //서울 마포구
        String name = "마포구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.549763,   126.963898),
                        new LatLng(37.549572,   126.963832),
                        new LatLng(37.549521,   126.963807),
                        new LatLng(37.549496,   126.9638),
                        new LatLng(37.548673,   126.963439),
                        new LatLng(37.548642,   126.963339),
                        new LatLng(37.548693,   126.962937),
                        new LatLng(37.548559,   126.962202),
                        new LatLng(37.548128,   126.962222),
                        new LatLng(37.547593,   126.961701),
                        new LatLng(37.547432,   126.961102),
                        new LatLng(37.547419,   126.961077),
                        new LatLng(37.546994,   126.960754),
                        new LatLng(37.546375,   126.960132),
                        new LatLng(37.546068,   126.960051),
                        new LatLng(37.546003,   126.959697),
                        new LatLng(37.545997,   126.959665),
                        new LatLng(37.545973,   126.959533),
                        new LatLng(37.545973,   126.959528),
                        new LatLng(37.546014,   126.959413),
                        new LatLng(37.545999,   126.959264),
                        new LatLng(37.545901,   126.959098),
                        new LatLng(37.545683,   126.958718),
                        new LatLng(37.545605,   126.958572),
                        new LatLng(37.545508,   126.958413),
                        new LatLng(37.545493,   126.958314),
                        new LatLng(37.545478,   126.958112),
                        new LatLng(37.545468,   126.958006),
                        new LatLng(37.545458,   126.957908),
                        new LatLng(37.545356,   126.957878),
                        new LatLng(37.545155,   126.957819),
                        new LatLng(37.545064,   126.957846),
                        new LatLng(37.544912,   126.957911),
                        new LatLng(37.544427,   126.958106),
                        new LatLng(37.544263,   126.957669),
                        new LatLng(37.544004,   126.95745),
                        new LatLng(37.543829,   126.95746),
                        new LatLng(37.54372 ,126.957535),
                        new LatLng(37.543102,   126.957493),
                        new LatLng(37.542913,   126.958078),
                        new LatLng(37.54291 ,126.958091),
                        new LatLng(37.542818,   126.95825),
                        new LatLng(37.542759,   126.95835),
                        new LatLng(37.542312,   126.958176),
                        new LatLng(37.541928,   126.957439),
                        new LatLng(37.541568,   126.956963),
                        new LatLng(37.541488,   126.956935),
                        new LatLng(37.541382,   126.95693),
                        new LatLng(37.541376,   126.956935),
                        new LatLng(37.541136,   126.956854),
                        new LatLng(37.541091,   126.956831),
                        new LatLng(37.541008,   126.956785),
                        new LatLng(37.540966,   126.956762),
                        new LatLng(37.540706,   126.956521),
                        new LatLng(37.540497,   126.956563),
                        new LatLng(37.540311,   126.956409),
                        new LatLng(37.540126,   126.956262),
                        new LatLng(37.540045,   126.956168),
                        new LatLng(37.540024,   126.956144),
                        new LatLng(37.539195,   126.955213),
                        new LatLng(37.539014,   126.954323),
                        new LatLng(37.538772,   126.953966),
                        new LatLng(37.538481,   126.95377),
                        new LatLng(37.538188,   126.953619),
                        new LatLng(37.537644,   126.953441),
                        new LatLng(37.537554,   126.953381),
                        new LatLng(37.53704 ,126.95255),
                        new LatLng(37.536961,   126.952423),
                        new LatLng(37.53693 ,126.952372),
                        new LatLng(37.536895,   126.952315),
                        new LatLng(37.536774,   126.952114),
                        new LatLng(37.536699,   126.951972),
                        new LatLng(37.536705,   126.951947),
                        new LatLng(37.536632,   126.951777),
                        new LatLng(37.536582,   126.951654),
                        new LatLng(37.536448,   126.951495),
                        new LatLng(37.536394,   126.951464),
                        new LatLng(37.536276,   126.951248),
                        new LatLng(37.536269,   126.951233),
                        new LatLng(37.536168,   126.950937),
                        new LatLng(37.536105,   126.950555),
                        new LatLng(37.536083,   126.950442),
                        new LatLng(37.536073,   126.950342),
                        new LatLng(37.536045,   126.949904),
                        new LatLng(37.535955,   126.949686),
                        new LatLng(37.535824,   126.949235),
                        new LatLng(37.535781,   126.949085),
                        new LatLng(37.535716,   126.948941),
                        new LatLng(37.53571 ,126.948937),
                        new LatLng(37.53565 ,126.948901),
                        new LatLng(37.535594,   126.948663),
                        new LatLng(37.535579,   126.9486),
                        new LatLng(37.535648,   126.948313),
                        new LatLng(37.535654,   126.948282),
                        new LatLng(37.535641,   126.948249),
                        new LatLng(37.535619,   126.947918),
                        new LatLng(37.535618,   126.947908),
                        new LatLng(37.535619,   126.94787),
                        new LatLng(37.535619,   126.947846),
                        new LatLng(37.535664,   126.947719),
                        new LatLng(37.535744,   126.947635),
                        new LatLng(37.535798,   126.947615),
                        new LatLng(37.535833,   126.947549),
                        new LatLng(37.535826,   126.947506),
                        new LatLng(37.535592,   126.947441),
                        new LatLng(37.535421,   126.9474),
                        new LatLng(37.535398,   126.947395),
                        new LatLng(37.535378,   126.947389),
                        new LatLng(37.535323,   126.947464),
                        new LatLng(37.535267,   126.947431),
                        new LatLng(37.535547,   126.946695),
                        new LatLng(37.535568,   126.946639),
                        new LatLng(37.535721,   126.946433),
                        new LatLng(37.53574 ,126.946412),
                        new LatLng(37.535692,   126.946275),
                        new LatLng(37.535023,   126.945564),
                        new LatLng(37.534985,   126.945562),
                        new LatLng(37.535002,   126.945413),
                        new LatLng(37.534964,   126.945099),
                        new LatLng(37.534732,   126.944952),
                        new LatLng(37.534709,   126.944938),
                        new LatLng(37.534317,   126.944931),
                        new LatLng(37.534235,   126.944932),
                        new LatLng(37.533882,   126.944661),
                        new LatLng(37.533781,   126.944585),
                        new LatLng(37.539352,   126.935338),
                        new LatLng(37.539918,   126.934159),
                        new LatLng(37.541425,   126.917139),
                        new LatLng(37.541426,   126.916765),
                        new LatLng(37.541417,   126.904833),
                        new LatLng(37.541433,   126.904815),
                        new LatLng(37.545976,   126.900898),
                        new LatLng(37.54598 ,126.900894),
                        new LatLng(37.545986,   126.900889),
                        new LatLng(37.555811,   126.881392),
                        new LatLng(37.556269,   126.880464),
                        new LatLng(37.57179 ,126.853632),
                        new LatLng(37.573799,   126.853629),
                        new LatLng(37.577472,   126.864941),
                        new LatLng(37.578185,   126.876261),
                        new LatLng(37.58444 ,126.876538),
                        new LatLng(37.590793,   126.882056),
                        new LatLng(37.576005,   126.902081),
                        new LatLng(37.565105,   126.926014),
                        new LatLng(37.563332,   126.928282),
                        new LatLng(37.55885 ,126.926597),
                        new LatLng(37.55658 ,126.932792),
                        new LatLng(37.553451,   126.933945),
                        new LatLng(37.556546,   126.941372),
                        new LatLng(37.558982,   126.961689),
                        new LatLng(37.551549,   126.962343),
                        new LatLng(37.551485,   126.962366),
                        new LatLng(37.55148 ,126.962369),
                        new LatLng(37.551379,   126.962397),
                        new LatLng(37.551163,   126.962646),
                        new LatLng(37.551024,   126.962842),
                        new LatLng(37.550661,   126.963187),
                        new LatLng(37.550576,   126.963229),
                        new LatLng(37.550523,   126.963236),
                        new LatLng(37.55046 ,126.963234),
                        new LatLng(37.55044 ,126.963253),
                        new LatLng(37.550131,   126.963505),
                        new LatLng(37.550076,   126.963527),
                        new LatLng(37.550042,   126.963546),
                        new LatLng(37.549948,   126.963549),
                        new LatLng(37.549796,   126.963772),
                        new LatLng(37.549763,   126.963898)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"마포구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "마포구\n  "+hmap.get(name), new LatLng(37.560098, 126.908128));
    }//서울 마포구
    public void drawPolygon152(GoogleMap googlemap) { //서울 영등포구
        String name = "영등포구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.517514    ,126.949887 ),
                        new LatLng(37.514457    ,126.926762 ),
                        new LatLng(37.514396    ,126.926672 ),
                        new LatLng(37.514222    ,126.926748 ),
                        new LatLng(37.514007    ,126.926981 ),
                        new LatLng(37.513161    ,126.926823 ),
                        new LatLng(37.513136    ,126.926759 ),
                        new LatLng(37.512805    ,126.926793 ),
                        new LatLng(37.512799    ,126.926789 ),
                        new LatLng(37.512567    ,126.925544 ),
                        new LatLng(37.512528    ,126.925392 ),
                        new LatLng(37.512523    ,126.925386 ),
                        new LatLng(37.501576    ,126.920959 ),
                        new LatLng(37.501112    ,126.920776 ),
                        new LatLng(37.500035    ,126.920347 ),
                        new LatLng(37.500034    ,126.920297 ),
                        new LatLng(37.500032    ,126.92029  ),
                        new LatLng(37.49881 ,126.920255     ),
                        new LatLng(37.498507    ,126.920245 ),
                        new LatLng(37.496569    ,126.914433 ),
                        new LatLng(37.496547    ,126.914206 ),
                        new LatLng(37.496487    ,126.91358  ),
                        new LatLng(37.496486    ,126.913562 ),
                        new LatLng(37.4964  ,126.912794     ),
                        new LatLng(37.496353    ,126.912525 ),
                        new LatLng(37.488987    ,126.906458 ),
                        new LatLng(37.488683    ,126.90621  ),
                        new LatLng(37.485176    ,126.90334  ),
                        new LatLng(37.485003    ,126.903199 ),
                        new LatLng(37.485002    ,126.903198 ),
                        new LatLng(37.485006    ,126.90303  ),
                        new LatLng(37.486932    ,126.898146 ),
                        new LatLng(37.486937    ,126.898143 ),
                        new LatLng(37.489339    ,126.896124 ),
                        new LatLng(37.489523    ,126.896113 ),
                        new LatLng(37.49021 ,126.896013     ),
                        new LatLng(37.490493    ,126.895864 ),
                        new LatLng(37.491766    ,126.895472 ),
                        new LatLng(37.492001    ,126.895413 ),
                        new LatLng(37.492114    ,126.895383 ),
                        new LatLng(37.497841    ,126.894118 ),
                        new LatLng(37.497848    ,126.894123 ),
                        new LatLng(37.499249    ,126.893719 ),
                        new LatLng(37.499509    ,126.893646 ),
                        new LatLng(37.499586    ,126.893625 ),
                        new LatLng(37.499607    ,126.893609 ),
                        new LatLng(37.500357    ,126.892926 ),
                        new LatLng(37.500419    ,126.892903 ),
                        new LatLng(37.501276    ,126.89283  ),
                        new LatLng(37.501289    ,126.892835 ),
                        new LatLng(37.501301    ,126.892839 ),
                        new LatLng(37.501318    ,126.892845 ),
                        new LatLng(37.501575    ,126.892933 ),
                        new LatLng(37.501606    ,126.892922 ),
                        new LatLng(37.502477    ,126.892992 ),
                        new LatLng(37.502514    ,126.893019 ),
                        new LatLng(37.502668    ,126.893132 ),
                        new LatLng(37.502677    ,126.893134 ),
                        new LatLng(37.503668    ,126.893009 ),
                        new LatLng(37.50372 ,126.893106     ),
                        new LatLng(37.503954    ,126.893303 ),
                        new LatLng(37.503965    ,126.893306 ),
                        new LatLng(37.504625    ,126.893226 ),
                        new LatLng(37.50463 ,126.89325  ),
                        new LatLng(37.506031    ,126.893532 ),
                        new LatLng(37.506109    ,126.893501 ),
                        new LatLng(37.507817    ,126.892794 ),
                        new LatLng(37.50785 ,126.892783     ),
                        new LatLng(37.511894    ,126.889688 ),
                        new LatLng(37.512041    ,126.889787 ),
                        new LatLng(37.517775    ,126.879484 ),
                        new LatLng(37.520598    ,126.879437 ),
                        new LatLng(37.520828    ,126.879408 ),
                        new LatLng(37.522458    ,126.879002 ),
                        new LatLng(37.52252 ,126.879053     ),
                        new LatLng(37.522918    ,126.878924 ),
                        new LatLng(37.523136    ,126.878891 ),
                        new LatLng(37.523147    ,126.87889  ),
                        new LatLng(37.523895    ,126.878892 ),
                        new LatLng(37.524088    ,126.87886  ),
                        new LatLng(37.524373    ,126.87875  ),
                        new LatLng(37.524458    ,126.878709 ),
                        new LatLng(37.524618    ,126.878631 ),
                        new LatLng(37.524643    ,126.878621 ),
                        new LatLng(37.524684    ,126.878614 ),
                        new LatLng(37.524722    ,126.878613 ),
                        new LatLng(37.525048    ,126.878606 ),
                        new LatLng(37.525069    ,126.878609 ),
                        new LatLng(37.525355    ,126.878754 ),
                        new LatLng(37.525427    ,126.878819 ),
                        new LatLng(37.525506    ,126.878917 ),
                        new LatLng(37.525658    ,126.879146 ),
                        new LatLng(37.525676    ,126.879187 ),
                        new LatLng(37.525716    ,126.879285 ),
                        new LatLng(37.525725    ,126.879329 ),
                        new LatLng(37.52573 ,126.879361     ),
                        new LatLng(37.525737    ,126.879407 ),
                        new LatLng(37.525738    ,126.880238 ),
                        new LatLng(37.525737    ,126.880325 ),
                        new LatLng(37.525739    ,126.880402 ),
                        new LatLng(37.525739    ,126.880446 ),
                        new LatLng(37.52574 ,126.880473     ),
                        new LatLng(37.525741    ,126.880486 ),
                        new LatLng(37.52575 ,126.880576     ),
                        new LatLng(37.52578 ,126.880709     ),
                        new LatLng(37.525835    ,126.88081  ),
                        new LatLng(37.525905    ,126.880933 ),
                        new LatLng(37.525908    ,126.880938 ),
                        new LatLng(37.525912    ,126.880942 ),
                        new LatLng(37.525974    ,126.880993 ),
                        new LatLng(37.525983    ,126.881    ),
                        new LatLng(37.526114    ,126.881108 ),
                        new LatLng(37.526134    ,126.881123 ),
                        new LatLng(37.526142    ,126.881127 ),
                        new LatLng(37.526145    ,126.881128 ),
                        new LatLng(37.526155    ,126.881132 ),
                        new LatLng(37.526161    ,126.881134 ),
                        new LatLng(37.526173    ,126.881138 ),
                        new LatLng(37.526185    ,126.881141 ),
                        new LatLng(37.526204    ,126.881148 ),
                        new LatLng(37.526483    ,126.881177 ),
                        new LatLng(37.526501    ,126.881175 ),
                        new LatLng(37.526514    ,126.881175 ),
                        new LatLng(37.526536    ,126.881169 ),
                        new LatLng(37.526543    ,126.881167 ),
                        new LatLng(37.52667 ,126.88107  ),
                        new LatLng(37.527241    ,126.880629 ),
                        new LatLng(37.527267    ,126.880621 ),
                        new LatLng(37.527319    ,126.88061  ),
                        new LatLng(37.527401    ,126.880603 ),
                        new LatLng(37.527418    ,126.880604 ),
                        new LatLng(37.527551    ,126.880675 ),
                        new LatLng(37.527562    ,126.880687 ),
                        new LatLng(37.5276  ,126.880739     ),
                        new LatLng(37.527648    ,126.880804 ),
                        new LatLng(37.527707    ,126.880906 ),
                        new LatLng(37.527688    ,126.881687 ),
                        new LatLng(37.527668    ,126.881763 ),
                        new LatLng(37.527327    ,126.88256  ),
                        new LatLng(37.527318    ,126.882579 ),
                        new LatLng(37.527249    ,126.882727 ),
                        new LatLng(37.527229    ,126.882907 ),
                        new LatLng(37.52725 ,126.883222     ),
                        new LatLng(37.527268    ,126.883283 ),
                        new LatLng(37.527475    ,126.883651 ),
                        new LatLng(37.527501    ,126.883675 ),
                        new LatLng(37.527558    ,126.883728 ),
                        new LatLng(37.527581    ,126.883743 ),
                        new LatLng(37.527587    ,126.883747 ),
                        new LatLng(37.5278  ,126.883846     ),
                        new LatLng(37.52782 ,126.883855     ),
                        new LatLng(37.527987    ,126.883889 ),
                        new LatLng(37.528045    ,126.883895 ),
                        new LatLng(37.52807 ,126.883896     ),
                        new LatLng(37.528406    ,126.883917 ),
                        new LatLng(37.52841 ,126.883919     ),
                        new LatLng(37.528437    ,126.88394  ),
                        new LatLng(37.528453    ,126.883952 ),
                        new LatLng(37.528508    ,126.883996 ),
                        new LatLng(37.528537    ,126.884018 ),
                        new LatLng(37.528545    ,126.884027 ),
                        new LatLng(37.528551    ,126.884033 ),
                        new LatLng(37.528641    ,126.884144 ),
                        new LatLng(37.528663    ,126.88417  ),
                        new LatLng(37.528737    ,126.884291 ),
                        new LatLng(37.528744    ,126.884303 ),
                        new LatLng(37.528759    ,126.884332 ),
                        new LatLng(37.5288  ,126.88442  ),
                        new LatLng(37.528813    ,126.884456 ),
                        new LatLng(37.528827    ,126.884493 ),
                        new LatLng(37.528831    ,126.884504 ),
                        new LatLng(37.528837    ,126.884521 ),
                        new LatLng(37.528928    ,126.884695 ),
                        new LatLng(37.529075    ,126.884983 ),
                        new LatLng(37.529091    ,126.885015 ),
                        new LatLng(37.529103    ,126.885041 ),
                        new LatLng(37.529123    ,126.885083 ),
                        new LatLng(37.529166    ,126.885181 ),
                        new LatLng(37.529183    ,126.885224 ),
                        new LatLng(37.529382    ,126.885817 ),
                        new LatLng(37.529399    ,126.88587  ),
                        new LatLng(37.529404    ,126.885886 ),
                        new LatLng(37.529422    ,126.885944 ),
                        new LatLng(37.529554    ,126.886202 ),
                        new LatLng(37.529568    ,126.886224 ),
                        new LatLng(37.529577    ,126.886239 ),
                        new LatLng(37.530135    ,126.887071 ),
                        new LatLng(37.530154    ,126.887102 ),
                        new LatLng(37.530157    ,126.88711  ),
                        new LatLng(37.530185    ,126.887166 ),
                        new LatLng(37.530195    ,126.88719  ),
                        new LatLng(37.530366    ,126.887685 ),
                        new LatLng(37.530396    ,126.887924 ),
                        new LatLng(37.530118    ,126.889412 ),
                        new LatLng(37.530099    ,126.889437 ),
                        new LatLng(37.530047    ,126.889488 ),
                        new LatLng(37.530689    ,126.889972 ),
                        new LatLng(37.530788    ,126.890041 ),
                        new LatLng(37.531053    ,126.890227 ),
                        new LatLng(37.53109 ,126.890253     ),
                        new LatLng(37.531102    ,126.890262 ),
                        new LatLng(37.531361    ,126.890446 ),
                        new LatLng(37.531396    ,126.89047  ),
                        new LatLng(37.531656    ,126.890684 ),
                        new LatLng(37.531725    ,126.890581 ),
                        new LatLng(37.531869    ,126.890289 ),
                        new LatLng(37.531908    ,126.889658 ),
                        new LatLng(37.531914    ,126.889523 ),
                        new LatLng(37.531912    ,126.889448 ),
                        new LatLng(37.53191 ,126.889391     ),
                        new LatLng(37.531909    ,126.889087 ),
                        new LatLng(37.532248    ,126.888562 ),
                        new LatLng(37.532283    ,126.888549 ),
                        new LatLng(37.532392    ,126.888538 ),
                        new LatLng(37.532555    ,126.888531 ),
                        new LatLng(37.532597    ,126.888547 ),
                        new LatLng(37.532639    ,126.888564 ),
                        new LatLng(37.532805    ,126.888693 ),
                        new LatLng(37.532838    ,126.888738 ),
                        new LatLng(37.53321 ,126.889462     ),
                        new LatLng(37.533309    ,126.889649 ),
                        new LatLng(37.533358    ,126.889734 ),
                        new LatLng(37.533378    ,126.889769 ),
                        new LatLng(37.5334  ,126.889803     ),
                        new LatLng(37.533418    ,126.889823 ),
                        new LatLng(37.533426    ,126.889832 ),
                        new LatLng(37.533562    ,126.889937 ),
                        new LatLng(37.53371 ,126.890037     ),
                        new LatLng(37.533956    ,126.890112 ),
                        new LatLng(37.534082    ,126.89015  ),
                        new LatLng(37.534098    ,126.890152 ),
                        new LatLng(37.534112    ,126.890154 ),
                        new LatLng(37.534139    ,126.890152 ),
                        new LatLng(37.534229    ,126.890148 ),
                        new LatLng(37.534264    ,126.890143 ),
                        new LatLng(37.534376    ,126.890128 ),
                        new LatLng(37.534708    ,126.889996 ),
                        new LatLng(37.534763    ,126.889969 ),
                        new LatLng(37.535144    ,126.889742 ),
                        new LatLng(37.535197    ,126.889705 ),
                        new LatLng(37.536715    ,126.88826  ),
                        new LatLng(37.536973    ,126.888078 ),
                        new LatLng(37.538587    ,126.887107 ),
                        new LatLng(37.538629    ,126.887079 ),
                        new LatLng(37.538675    ,126.887038 ),
                        new LatLng(37.53871 ,126.886998     ),
                        new LatLng(37.538781    ,126.8869   ),
                        new LatLng(37.539362    ,126.886532 ),
                        new LatLng(37.539373    ,126.886526 ),
                        new LatLng(37.539469    ,126.886477 ),
                        new LatLng(37.539551    ,126.886433 ),
                        new LatLng(37.539628    ,126.886391 ),
                        new LatLng(37.539826    ,126.886282 ),
                        new LatLng(37.540101    ,126.886151 ),
                        new LatLng(37.540238    ,126.886089 ),
                        new LatLng(37.541576    ,126.885507 ),
                        new LatLng(37.541634    ,126.885494 ),
                        new LatLng(37.541839    ,126.885451 ),
                        new LatLng(37.54185 ,126.885449     ),
                        new LatLng(37.541984    ,126.885446 ),
                        new LatLng(37.542307    ,126.885437 ),
                        new LatLng(37.542312    ,126.885436 ),
                        new LatLng(37.543326    ,126.884705 ),
                        new LatLng(37.543331    ,126.884698 ),
                        new LatLng(37.543754    ,126.884268 ),
                        new LatLng(37.543862    ,126.884266 ),
                        new LatLng(37.544319    ,126.88424  ),
                        new LatLng(37.5444  ,126.884225     ),
                        new LatLng(37.546846    ,126.882006 ),
                        new LatLng(37.546855    ,126.881997 ),
                        new LatLng(37.546869    ,126.881982 ),
                        new LatLng(37.546874    ,126.881976 ),
                        new LatLng(37.548088    ,126.880792 ),
                        new LatLng(37.55031 ,126.879213     ),
                        new LatLng(37.550412    ,126.879124 ),
                        new LatLng(37.553036    ,126.878005 ),
                        new LatLng(37.55317 ,126.877952     ),
                        new LatLng(37.556269    ,126.880464 ),
                        new LatLng(37.555811    ,126.881392 ),
                        new LatLng(37.545986    ,126.900889 ),
                        new LatLng(37.54598 ,126.900894     ),
                        new LatLng(37.545976    ,126.900898 ),
                        new LatLng(37.541433    ,126.904815 ),
                        new LatLng(37.541417    ,126.904833 ),
                        new LatLng(37.541426    ,126.916765 ),
                        new LatLng(37.541425    ,126.917139 ),
                        new LatLng(37.539918    ,126.934159 ),
                        new LatLng(37.539352    ,126.935338 ),
                        new LatLng(37.533781    ,126.944585 ),
                        new LatLng(37.532627    ,126.945514 ),
                        new LatLng(37.530172    ,126.947411 ),
                        new LatLng(37.530128    ,126.947445 ),
                        new LatLng(37.529822    ,126.947685 ),
                        new LatLng(37.52935 ,126.948056     ),
                        new LatLng(37.529079    ,126.948269 ),
                        new LatLng(37.52875 ,126.948526     ),
                        new LatLng(37.528429    ,126.948778 ),
                        new LatLng(37.528242    ,126.948925 ),
                        new LatLng(37.528111    ,126.949028 ),
                        new LatLng(37.527145    ,126.949787 ),
                        new LatLng(37.526805    ,126.949879 ),
                        new LatLng(37.526583    ,126.949879 ),
                        new LatLng(37.525681    ,126.94988  ),
                        new LatLng(37.525096    ,126.949881 ),
                        new LatLng(37.523868    ,126.949882 ),
                        new LatLng(37.521623    ,126.949884 ),
                        new LatLng(37.517514    ,126.949887 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"영등포구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "영등포구\n   "+hmap.get(name), new LatLng(37.523416, 126.910407));
    }//서울 영등포구
    public void drawPolygon144(GoogleMap googlemap) { //서울 동작구
        String name = "동작구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.499861,  126.985385),
                        new LatLng(37.496994,  126.982926),
                        new LatLng(37.476531,  126.981691),
                        new LatLng(37.475377,  126.970523),
                        new LatLng(37.477495,  126.968045),
                        new LatLng(37.477836,  126.967782),
                        new LatLng(37.483561,  126.961075),
                        new LatLng(37.485176,  126.961986),
                        new LatLng(37.485412,  126.961895),
                        new LatLng(37.490822,  126.960781),
                        new LatLng(37.490831,  126.960782),
                        new LatLng(37.491492,  126.9614),
                        new LatLng(37.491554,  126.961399),
                        new LatLng(37.493803,  126.959338),
                        new LatLng(37.490753,  126.953904),
                        new LatLng(37.490626,  126.953715),
                        new LatLng(37.494005,  126.947171),
                        new LatLng(37.494083,  126.946974),
                        new LatLng(37.493097,  126.94453),
                        new LatLng(37.493079,  126.944463),
                        new LatLng(37.492449,  126.943826),
                        new LatLng(37.492383,  126.94366),
                        new LatLng(37.492894,  126.939948),
                        new LatLng(37.492791,  126.939739),
                        new LatLng(37.492784,  126.939735),
                        new LatLng(37.492642,  126.939725),
                        new LatLng(37.492557,  126.939565),
                        new LatLng(37.492471,  126.93949),
                        new LatLng(37.492207,  126.939235),
                        new LatLng(37.49224,   126.939138),
                        new LatLng(37.492923,  126.932442),
                        new LatLng(37.492973,  126.9324),
                        new LatLng(37.493226,  126.931347),
                        new LatLng(37.493262,  126.931341),
                        new LatLng(37.493988,  126.930073),
                        new LatLng(37.493993,  126.929863),
                        new LatLng(37.494816,  126.928491),
                        new LatLng(37.494824,  126.928478),
                        new LatLng(37.495071,  126.92788),
                        new LatLng(37.495051,  126.927781),
                        new LatLng(37.495028,  126.927672),
                        new LatLng(37.493274,  126.926223),
                        new LatLng(37.493256,  126.926188),
                        new LatLng(37.493199,  126.926138),
                        new LatLng(37.493182,  126.926101),
                        new LatLng(37.491664,  126.92512),
                        new LatLng(37.491655,  126.925114),
                        new LatLng(37.490857,  126.924779),
                        new LatLng(37.490841,  126.924765),
                        new LatLng(37.490154,  126.922162),
                        new LatLng(37.490168,  126.922115),
                        new LatLng(37.490202,  126.919007),
                        new LatLng(37.490143,  126.918708),
                        new LatLng(37.488672,  126.914831),
                        new LatLng(37.488496,  126.914489),
                        new LatLng(37.48753,   126.91319),
                        new LatLng(37.487398,  126.913079),
                        new LatLng(37.48693,   126.912451),
                        new LatLng(37.486892,  126.912417),
                        new LatLng(37.486774,  126.912312),
                        new LatLng(37.486748,  126.912273),
                        new LatLng(37.485003,  126.903199),
                        new LatLng(37.485176,  126.90334),
                        new LatLng(37.488683,  126.90621),
                        new LatLng(37.488987,  126.906458),
                        new LatLng(37.496353,  126.912525),
                        new LatLng(37.4964   , 126.912794),
                        new LatLng(37.496486,  126.913562),
                        new LatLng(37.496487,  126.91358),
                        new LatLng(37.496547,  126.914206),
                        new LatLng(37.496569,  126.914433),
                        new LatLng(37.498507,  126.920245),
                        new LatLng(37.49881,   126.920255),
                        new LatLng(37.500032,  126.92029),
                        new LatLng(37.500034,  126.920297),
                        new LatLng(37.500035,  126.920347),
                        new LatLng(37.501112,  126.920776),
                        new LatLng(37.501576,  126.920959),
                        new LatLng(37.512523,  126.925386),
                        new LatLng(37.512528,  126.925392),
                        new LatLng(37.512567,  126.925544),
                        new LatLng(37.512799,  126.926789),
                        new LatLng(37.512805,  126.926793),
                        new LatLng(37.513136,  126.926759),
                        new LatLng(37.513161,  126.926823),
                        new LatLng(37.514007,  126.926981),
                        new LatLng(37.514222,  126.926748),
                        new LatLng(37.514396,  126.926672),
                        new LatLng(37.514457,  126.926762),
                        new LatLng(37.517514,  126.949887),
                        new LatLng(37.517394,  126.950292),
                        new LatLng(37.517221,  126.950833),
                        new LatLng(37.517073,  126.951304),
                        new LatLng(37.516451,  126.953276),
                        new LatLng(37.516397,  126.953444),
                        new LatLng(37.516384,  126.953489),
                        new LatLng(37.516224,  126.954087),
                        new LatLng(37.515711,  126.955545),
                        new LatLng(37.51527,   126.956608),
                        new LatLng(37.514907,  126.957483),
                        new LatLng(37.514757,  126.957853),
                        new LatLng(37.514645,  126.958125),
                        new LatLng(37.514529,  126.95842),
                        new LatLng(37.514257,  126.959084),
                        new LatLng(37.514068,  126.959534),
                        new LatLng(37.513865,  126.960055),
                        new LatLng(37.513587,  126.960837),
                        new LatLng(37.513239,  126.961611),
                        new LatLng(37.51309,   126.9619),
                        new LatLng(37.510999,  126.96474),
                        new LatLng(37.5108   , 126.965008),
                        new LatLng(37.509875,  126.966921),
                        new LatLng(37.509586,  126.967962),
                        new LatLng(37.509021,  126.970073),
                        new LatLng(37.509014,  126.970098),
                        new LatLng(37.508978,  126.970197),
                        new LatLng(37.508919,  126.970349),
                        new LatLng(37.508866,  126.970486),
                        new LatLng(37.508338,  126.971852),
                        new LatLng(37.507471,  126.974084),
                        new LatLng(37.507003,  126.97535),
                        new LatLng(37.506685,  126.977371),
                        new LatLng(37.506559,  126.978171),
                        new LatLng(37.506555,  126.97853),
                        new LatLng(37.506554,  126.978676),
                        new LatLng(37.506554,  126.978782),
                        new LatLng(37.506553,  126.978984),
                        new LatLng(37.506545,  126.980404),
                        new LatLng(37.502863,  126.980388),
                        new LatLng(37.499861,  126.985385)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"동작구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "동작구\n   "+hmap.get(name), new LatLng(37.499630, 126.951588));
    }//서울 동작구
    public void drawPolygon157(GoogleMap googlemap) { //서울 서초구
        String name = "서초구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.461004,   127.095677  ),
                        new LatLng(37.456394,   127.095223  ),
                        new LatLng(37.44975   , 127.088821  ),
                        new LatLng(37.444894,   127.087855  ),
                        new LatLng(37.441351,   127.082097  ),
                        new LatLng(37.442263,   127.072138  ),
                        new LatLng(37.437408,   127.073844  ),
                        new LatLng(37.430188,   127.070885  ),
                        new LatLng(37.430274,   127.070706  ),
                        new LatLng(37.428297,   127.052325  ),
                        new LatLng(37.430702,   127.047369  ),
                        new LatLng(37.437769,   127.04109   ),
                        new LatLng(37.439004,   127.035574  ),
                        new LatLng(37.445859,   127.038206  ),
                        new LatLng(37.46147   , 127.033799  ),
                        new LatLng(37.46153   , 127.033822  ),
                        new LatLng(37.463452,   127.034534  ),
                        new LatLng(37.463458,   127.034537  ),
                        new LatLng(37.465374,   127.029527  ),
                        new LatLng(37.457817,   127.025995  ),
                        new LatLng(37.455509,   127.011939  ),
                        new LatLng(37.455408,   127.011368  ),
                        new LatLng(37.46772   , 127.003675  ),
                        new LatLng(37.467072,   126.996752  ),
                        new LatLng(37.461873,   126.996769  ),
                        new LatLng(37.458166,   126.988636  ),
                        new LatLng(37.466411,   126.987728  ),
                        new LatLng(37.466426,   126.987719  ),
                        new LatLng(37.466593,   126.987623  ),
                        new LatLng(37.466627,   126.987603  ),
                        new LatLng(37.476531,   126.981691  ),
                        new LatLng(37.496994,   126.982926  ),
                        new LatLng(37.499861,   126.985385  ),
                        new LatLng(37.502863,   126.980388  ),
                        new LatLng(37.506545,   126.980404  ),
                        new LatLng(37.506538,   126.981527  ),
                        new LatLng(37.506539,   126.98216   ),
                        new LatLng(37.506543,   126.98554   ),
                        new LatLng(37.506551,   126.985544  ),
                        new LatLng(37.506989,   126.985885  ),
                        new LatLng(37.507503,   126.986285  ),
                        new LatLng(37.510206,   126.988399  ),
                        new LatLng(37.511843,   126.989697  ),
                        new LatLng(37.513096,   126.990684  ),
                        new LatLng(37.513366,   126.991114  ),
                        new LatLng(37.515119,   126.993884  ),
                        new LatLng(37.516893,   126.996681  ),
                        new LatLng(37.517364,   126.997423  ),
                        new LatLng(37.517423,   126.997516  ),
                        new LatLng(37.518324,   126.998939  ),
                        new LatLng(37.518837,   126.999747  ),
                        new LatLng(37.51912   , 127.000191  ),
                        new LatLng(37.519171,   127.000272  ),
                        new LatLng(37.519245,   127.000388  ),
                        new LatLng(37.519354,   127.00056   ),
                        new LatLng(37.519485,   127.000766  ),
                        new LatLng(37.519501,   127.000792  ),
                        new LatLng(37.519626,   127.000979  ),
                        new LatLng(37.520039,   127.001574  ),
                        new LatLng(37.520935,   127.002862  ),
                        new LatLng(37.521227,   127.00328   ),
                        new LatLng(37.522073,   127.004494  ),
                        new LatLng(37.523229,   127.006151  ),
                        new LatLng(37.524574,   127.007573  ),
                        new LatLng(37.525595,   127.008578  ),
                        new LatLng(37.522588,   127.013177  ),
                        new LatLng(37.524849,   127.015224  ),
                        new LatLng(37.521812,   127.017822  ),
                        new LatLng(37.484369,   127.034087  ),
                        new LatLng(37.485436,   127.041751  ),
                        new LatLng(37.480561,   127.043889  ),
                        new LatLng(37.479942,   127.044005  ),
                        new LatLng(37.471635,   127.050826  ),
                        new LatLng(37.47006   , 127.048669  ),
                        new LatLng(37.467268,   127.050802  ),
                        new LatLng(37.469336,   127.065099  ),
                        new LatLng(37.475158,   127.076911  ),
                        new LatLng(37.475512,   127.084882  ),
                        new LatLng(37.4712    , 127.085028  ),
                        new LatLng(37.461004,   127.095677  )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"서초구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "서초구\n   "+hmap.get(name), new LatLng(37.474795, 127.030850));
    }//서울 서초구
    public void drawPolygon161(GoogleMap googlemap) { //서울 송파구
        String name = "송파구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.499142    ,127.161011 ),
                        new LatLng(37.499066    ,127.161005 ),
                        new LatLng(37.490612    ,127.158235 ),
                        new LatLng(37.486417    ,127.152687 ),
                        new LatLng(37.486293    ,127.152316 ),
                        new LatLng(37.486236    ,127.152178 ),
                        new LatLng(37.486132    ,127.152032 ),
                        new LatLng(37.48601     ,127.151894 ),
                        new LatLng(37.480922    ,127.149353 ),
                        new LatLng(37.480915    ,127.149351 ),
                        new LatLng(37.48087     ,127.149334 ),
                        new LatLng(37.480858    ,127.149302 ),
                        new LatLng(37.480801    ,127.149185 ),
                        new LatLng(37.480781    ,127.14917  ),
                        new LatLng(37.479648    ,127.148592 ),
                        new LatLng(37.479597    ,127.148552 ),
                        new LatLng(37.474722    ,127.141398 ),
                        new LatLng(37.474718    ,127.14139  ),
                        new LatLng(37.473439    ,127.139364 ),
                        new LatLng(37.473999    ,127.137367 ),
                        new LatLng(37.474119    ,127.137067 ),
                        new LatLng(37.475105    ,127.13441  ),
                        new LatLng(37.475475    ,127.133946 ),
                        new LatLng(37.475094    ,127.13023  ),
                        new LatLng(37.472247    ,127.130276 ),
                        new LatLng(37.469286    ,127.135426 ),
                        new LatLng(37.468414    ,127.127034 ),
                        new LatLng(37.468626    ,127.126666 ),
                        new LatLng(37.469598    ,127.124878 ),
                        new LatLng(37.466644    ,127.124488 ),
                        new LatLng(37.466521    ,127.124207 ),
                        new LatLng(37.490333    ,127.10695  ),
                        new LatLng(37.496681    ,127.094794 ),
                        new LatLng(37.502504    ,127.072271 ),
                        new LatLng(37.502535    ,127.071938 ),
                        new LatLng(37.502739    ,127.069817 ),
                        new LatLng(37.524584    ,127.067501 ),
                        new LatLng(37.523335    ,127.072387 ),
                        new LatLng(37.523292    ,127.07257  ),
                        new LatLng(37.522528    ,127.076845 ),
                        new LatLng(37.522515    ,127.076946 ),
                        new LatLng(37.526996    ,127.090121 ),
                        new LatLng(37.534113    ,127.099095 ),
                        new LatLng(37.53421     ,127.099214 ),
                        new LatLng(37.543166    ,127.109143 ),
                        new LatLng(37.538645    ,127.12344  ),
                        new LatLng(37.52796     ,127.119093 ),
                        new LatLng(37.516835    ,127.145092 ),
                        new LatLng(37.515589    ,127.140614 ),
                        new LatLng(37.514363    ,127.142595 ),
                        new LatLng(37.514357    ,127.142606 ),
                        new LatLng(37.508997    ,127.140085 ),
                        new LatLng(37.508904    ,127.140064 ),
                        new LatLng(37.505407    ,127.141048 ),
                        new LatLng(37.503306    ,127.145503 ),
                        new LatLng(37.504742    ,127.150205 ),
                        new LatLng(37.501859    ,127.156438 ),
                        new LatLng(37.503179    ,127.157729 ),
                        new LatLng(37.499142    ,127.161011 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"송파구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "송파구\n   "+hmap.get(name), new LatLng(37.506098, 127.115838));
    }//서울 송파구
    public void drawPolygon154(GoogleMap googlemap) { //서울 광진구
        String name = "광진구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.55676     ,127.115253 ),
                        new LatLng(37.553851    ,127.113922 ),
                        new LatLng(37.553737    ,127.113841 ),
                        new LatLng(37.552142    ,127.112715 ),
                        new LatLng(37.551057    ,127.111945 ),
                        new LatLng(37.550378    ,127.111534 ),
                        new LatLng(37.543166    ,127.109143 ),
                        new LatLng(37.53421     ,127.099214 ),
                        new LatLng(37.534113    ,127.099095 ),
                        new LatLng(37.526996    ,127.090121 ),
                        new LatLng(37.522515    ,127.076946 ),
                        new LatLng(37.522528    ,127.076845 ),
                        new LatLng(37.523292    ,127.07257  ),
                        new LatLng(37.523335    ,127.072387 ),
                        new LatLng(37.524584    ,127.067501 ),
                        new LatLng(37.525069    ,127.065468 ),
                        new LatLng(37.52507     ,127.065456 ),
                        new LatLng(37.528324    ,127.056219 ),
                        new LatLng(37.531934    ,127.058212 ),
                        new LatLng(37.542144    ,127.063974 ),
                        new LatLng(37.548329    ,127.067465 ),
                        new LatLng(37.559414    ,127.073735 ),
                        new LatLng(37.559963    ,127.072384 ),
                        new LatLng(37.559979    ,127.072393 ),
                        new LatLng(37.564915    ,127.074372 ),
                        new LatLng(37.566909    ,127.076031 ),
                        new LatLng(37.571239    ,127.077991 ),
                        new LatLng(37.57187     ,127.078202 ),
                        new LatLng(37.571887    ,127.078226 ),
                        new LatLng(37.569694    ,127.090496 ),
                        new LatLng(37.573763    ,127.100888 ),
                        new LatLng(37.573746    ,127.100893 ),
                        new LatLng(37.573355    ,127.100936 ),
                        new LatLng(37.572988    ,127.101219 ),
                        new LatLng(37.572433    ,127.101642 ),
                        new LatLng(37.572155    ,127.102238 ),
                        new LatLng(37.572279    ,127.10313  ),
                        new LatLng(37.571918    ,127.103661 ),
                        new LatLng(37.571896    ,127.103684 ),
                        new LatLng(37.571388    ,127.10423  ),
                        new LatLng(37.569398    ,127.10334  ),
                        new LatLng(37.568821    ,127.103206 ),
                        new LatLng(37.567997    ,127.102979 ),
                        new LatLng(37.566689    ,127.102634 ),
                        new LatLng(37.565637    ,127.102387 ),
                        new LatLng(37.564896    ,127.102255 ),
                        new LatLng(37.564311    ,127.102234 ),
                        new LatLng(37.563291    ,127.101625 ),
                        new LatLng(37.56261     ,127.101657 ),
                        new LatLng(37.56158     ,127.101205 ),
                        new LatLng(37.560916    ,127.101158 ),
                        new LatLng(37.56025     ,127.101399 ),
                        new LatLng(37.559567    ,127.101801 ),
                        new LatLng(37.559374    ,127.102045 ),
                        new LatLng(37.559278    ,127.102167 ),
                        new LatLng(37.559035    ,127.102648 ),
                        new LatLng(37.558708    ,127.102854 ),
                        new LatLng(37.558205    ,127.103433 ),
                        new LatLng(37.558013    ,127.103655 ),
                        new LatLng(37.55771     ,127.104042 ),
                        new LatLng(37.557408    ,127.104352 ),
                        new LatLng(37.557207    ,127.10445  ),
                        new LatLng(37.556814    ,127.104657 ),
                        new LatLng(37.556766    ,127.10664  ),
                        new LatLng(37.55709     ,127.107109 ),
                        new LatLng(37.557363    ,127.107337 ),
                        new LatLng(37.557607    ,127.107519 ),
                        new LatLng(37.557709    ,127.107651 ),
                        new LatLng(37.558031    ,127.108306 ),
                        new LatLng(37.558224    ,127.10852  ),
                        new LatLng(37.558245    ,127.108543 ),
                        new LatLng(37.55833     ,127.108928 ),
                        new LatLng(37.558448    ,127.109322 ),
                        new LatLng(37.558253    ,127.11035  ),
                        new LatLng(37.55836     ,127.110827 ),
                        new LatLng(37.558408    ,127.111023 ),
                        new LatLng(37.558446    ,127.111168 ),
                        new LatLng(37.558473    ,127.111266 ),
                        new LatLng(37.558616    ,127.111643 ),
                        new LatLng(37.558747    ,127.111891 ),
                        new LatLng(37.558991    ,127.112282 ),
                        new LatLng(37.559001    ,127.112298 ),
                        new LatLng(37.558972    ,127.112402 ),
                        new LatLng(37.558885    ,127.112711 ),
                        new LatLng(37.558644    ,127.113363 ),
                        new LatLng(37.55857     ,127.11356  ),
                        new LatLng(37.558537    ,127.113646 ),
                        new LatLng(37.55849     ,127.11378  ),
                        new LatLng(37.558443    ,127.113845 ),
                        new LatLng(37.558133    ,127.113679 ),
                        new LatLng(37.557097    ,127.113333 ),
                        new LatLng(37.556891    ,127.113325 ),
                        new LatLng(37.556839    ,127.11332  ),
                        new LatLng(37.55683     ,127.113315 ),
                        new LatLng(37.55683     ,127.113523 ),
                        new LatLng(37.556808    ,127.114365 ),
                        new LatLng(37.55676     ,127.115252 ),
                        new LatLng(37.55676     ,127.115253 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"광진구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "광진구\n   "+hmap.get(name), new LatLng(37.546406, 127.086471));
    }//서울 광진구
    public void drawPolygon146(GoogleMap googlemap) { //서울 강동구
        String name = "강동구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.54517     ,127.183539 ),
                        new LatLng(37.546302    ,127.178261 ),
                        new LatLng(37.546142    ,127.177837 ),
                        new LatLng(37.545582    ,127.173906 ),
                        new LatLng(37.54554     ,127.173615 ),
                        new LatLng(37.545569    ,127.173407 ),
                        new LatLng(37.54557     ,127.173205 ),
                        new LatLng(37.544437    ,127.165254 ),
                        new LatLng(37.544487    ,127.165188 ),
                        new LatLng(37.541284    ,127.159647 ),
                        new LatLng(37.541083    ,127.159597 ),
                        new LatLng(37.532858    ,127.153756 ),
                        new LatLng(37.532663    ,127.153802 ),
                        new LatLng(37.521941    ,127.145769 ),
                        new LatLng(37.521932    ,127.145666 ),
                        new LatLng(37.521581    ,127.145643 ),
                        new LatLng(37.521479    ,127.14559  ),
                        new LatLng(37.516835    ,127.145092 ),
                        new LatLng(37.52796     ,127.119093 ),
                        new LatLng(37.538645    ,127.12344  ),
                        new LatLng(37.543166    ,127.109143 ),
                        new LatLng(37.550378    ,127.111534 ),
                        new LatLng(37.551057    ,127.111945 ),
                        new LatLng(37.552142    ,127.112715 ),
                        new LatLng(37.553737    ,127.113841 ),
                        new LatLng(37.553851    ,127.113922 ),
                        new LatLng(37.55676     ,127.115253 ),
                        new LatLng(37.559426    ,127.117303 ),
                        new LatLng(37.559713    ,127.117742 ),
                        new LatLng(37.560078    ,127.118321 ),
                        new LatLng(37.560138    ,127.118418 ),
                        new LatLng(37.56053     ,127.119042 ),
                        new LatLng(37.560678    ,127.119278 ),
                        new LatLng(37.560947    ,127.119706 ),
                        new LatLng(37.5612      ,127.120096 ),
                        new LatLng(37.564809    ,127.125783 ),
                        new LatLng(37.56677     ,127.13054  ),
                        new LatLng(37.567243    ,127.132018 ),
                        new LatLng(37.567701    ,127.133458 ),
                        new LatLng(37.567904    ,127.134106 ),
                        new LatLng(37.568025    ,127.134741 ),
                        new LatLng(37.568249    ,127.136404 ),
                        new LatLng(37.568426    ,127.139926 ),
                        new LatLng(37.568434    ,127.14895  ),
                        new LatLng(37.572044    ,127.154981 ),
                        new LatLng(37.572954    ,127.156899 ),
                        new LatLng(37.576387    ,127.162007 ),
                        new LatLng(37.578997    ,127.168648 ),
                        new LatLng(37.581201    ,127.177155 ),
                        new LatLng(37.577235    ,127.175328 ),
                        new LatLng(37.572188    ,127.177731 ),
                        new LatLng(37.572171    ,127.177742 ),
                        new LatLng(37.571153    ,127.177982 ),
                        new LatLng(37.571067    ,127.178058 ),
                        new LatLng(37.570413    ,127.178383 ),
                        new LatLng(37.570374    ,127.178397 ),
                        new LatLng(37.569769    ,127.178654 ),
                        new LatLng(37.569738    ,127.178679 ),
                        new LatLng(37.569237    ,127.179063 ),
                        new LatLng(37.569124    ,127.179133 ),
                        new LatLng(37.569003    ,127.179181 ),
                        new LatLng(37.568946    ,127.179204 ),
                        new LatLng(37.547876    ,127.182676 ),
                        new LatLng(37.547748    ,127.182674 ),
                        new LatLng(37.54517     ,127.183539 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"강동구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "강동구\n   "+hmap.get(name), new LatLng(37.550795, 127.147114));
    }//서울 강동구
    public void drawPolygon150(GoogleMap googlemap) { //서울 중랑구
        String name = "중랑구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.605444    ,127.118096),
                        new LatLng(37.604602    ,127.118047),
                        new LatLng(37.603395    ,127.117016),
                        new LatLng(37.603053    ,127.116872),
                        new LatLng(37.601841    ,127.115633),
                        new LatLng(37.601404    ,127.115384),
                        new LatLng(37.60127     ,127.115294),
                        new LatLng(37.600129    ,127.1141),
                        new LatLng(37.600121    ,127.114082),
                        new LatLng(37.599793    ,127.11404),
                        new LatLng(37.599521    ,127.114064),
                        new LatLng(37.599055    ,127.114471),
                        new LatLng(37.598077    ,127.115179),
                        new LatLng(37.597314    ,127.115727),
                        new LatLng(37.596192    ,127.116443),
                        new LatLng(37.593625    ,127.11569),
                        new LatLng(37.59356     ,127.115527),
                        new LatLng(37.59351     ,127.115182),
                        new LatLng(37.593433    ,127.114454),
                        new LatLng(37.592857    ,127.113095),
                        new LatLng(37.591859    ,127.112414),
                        new LatLng(37.58943     ,127.110855),
                        new LatLng(37.588458    ,127.110524),
                        new LatLng(37.587746    ,127.110298),
                        new LatLng(37.586637    ,127.110023),
                        new LatLng(37.584561    ,127.10938),
                        new LatLng(37.583867    ,127.109231),
                        new LatLng(37.583379    ,127.108967),
                        new LatLng(37.583238    ,127.108438),
                        new LatLng(37.582463    ,127.107383),
                        new LatLng(37.582263    ,127.106915),
                        new LatLng(37.581996    ,127.106223),
                        new LatLng(37.581937    ,127.105995),
                        new LatLng(37.58174     ,127.105584),
                        new LatLng(37.581662    ,127.105421),
                        new LatLng(37.581404    ,127.104974),
                        new LatLng(37.579786    ,127.102909),
                        new LatLng(37.579277    ,127.102977),
                        new LatLng(37.578597    ,127.102808),
                        new LatLng(37.578528    ,127.102758),
                        new LatLng(37.578043    ,127.102318),
                        new LatLng(37.577552    ,127.101958),
                        new LatLng(37.576967    ,127.101733),
                        new LatLng(37.576071    ,127.101144),
                        new LatLng(37.575573    ,127.101202),
                        new LatLng(37.57497     ,127.101162),
                        new LatLng(37.574204    ,127.100919),
                        new LatLng(37.573763    ,127.100888),
                        new LatLng(37.569694    ,127.090496),
                        new LatLng(37.571887    ,127.078226),
                        new LatLng(37.571921    ,127.078155),
                        new LatLng(37.57292     ,127.077231),
                        new LatLng(37.573509    ,127.077226),
                        new LatLng(37.574388    ,127.07711),
                        new LatLng(37.574395    ,127.077109),
                        new LatLng(37.577531    ,127.076697),
                        new LatLng(37.577597    ,127.076687),
                        new LatLng(37.577616    ,127.076685),
                        new LatLng(37.579549    ,127.076421),
                        new LatLng(37.579553    ,127.07642),
                        new LatLng(37.579587    ,127.076412),
                        new LatLng(37.579592    ,127.076411),
                        new LatLng(37.579858    ,127.076342),
                        new LatLng(37.579864    ,127.076341),
                        new LatLng(37.580259    ,127.076197),
                        new LatLng(37.580296    ,127.076181),
                        new LatLng(37.582477    ,127.074254),
                        new LatLng(37.58287     ,127.073932),
                        new LatLng(37.582863    ,127.073929),
                        new LatLng(37.582894    ,127.073902),
                        new LatLng(37.582899    ,127.073901),
                        new LatLng(37.582905    ,127.073903),
                        new LatLng(37.582911    ,127.073902),
                        new LatLng(37.583955    ,127.07304),
                        new LatLng(37.583961    ,127.073035),
                        new LatLng(37.584165    ,127.072855),
                        new LatLng(37.584264    ,127.072768),
                        new LatLng(37.584538    ,127.072528),
                        new LatLng(37.584956    ,127.072161),
                        new LatLng(37.584982    ,127.072169),
                        new LatLng(37.585425    ,127.072309),
                        new LatLng(37.585543    ,127.072345),
                        new LatLng(37.586509    ,127.072643),
                        new LatLng(37.586533    ,127.072607),
                        new LatLng(37.586825    ,127.072365),
                        new LatLng(37.587049    ,127.072227),
                        new LatLng(37.58706     ,127.07222),
                        new LatLng(37.587091    ,127.072195),
                        new LatLng(37.587163    ,127.072137),
                        new LatLng(37.587178    ,127.072128),
                        new LatLng(37.588531    ,127.071352),
                        new LatLng(37.588904    ,127.071079),
                        new LatLng(37.589008    ,127.07092),
                        new LatLng(37.589011    ,127.070915),
                        new LatLng(37.589019    ,127.070894),
                        new LatLng(37.589022    ,127.070888),
                        new LatLng(37.589024    ,127.070884),
                        new LatLng(37.589025    ,127.070882),
                        new LatLng(37.589028    ,127.070873),
                        new LatLng(37.589065    ,127.070798),
                        new LatLng(37.589078    ,127.070758),
                        new LatLng(37.58909     ,127.070722),
                        new LatLng(37.58913     ,127.070645),
                        new LatLng(37.58915     ,127.070606),
                        new LatLng(37.589174    ,127.070582),
                        new LatLng(37.589534    ,127.070303),
                        new LatLng(37.589815    ,127.070271),
                        new LatLng(37.589957    ,127.07023),
                        new LatLng(37.59019     ,127.070312),
                        new LatLng(37.590552    ,127.07044),
                        new LatLng(37.590588    ,127.070452),
                        new LatLng(37.590612    ,127.07046),
                        new LatLng(37.590651    ,127.070474),
                        new LatLng(37.590688    ,127.070487),
                        new LatLng(37.590706    ,127.070493),
                        new LatLng(37.590726    ,127.070502),
                        new LatLng(37.590841    ,127.070553),
                        new LatLng(37.590848    ,127.070556),
                        new LatLng(37.590876    ,127.070568),
                        new LatLng(37.590955    ,127.070604),
                        new LatLng(37.591356    ,127.070691),
                        new LatLng(37.591891    ,127.070724),
                        new LatLng(37.59245     ,127.070569),
                        new LatLng(37.595121    ,127.069435),
                        new LatLng(37.595209    ,127.069429),
                        new LatLng(37.595327    ,127.069424),
                        new LatLng(37.595498    ,127.069449),
                        new LatLng(37.597729    ,127.069839),
                        new LatLng(37.598625    ,127.070792),
                        new LatLng(37.600243    ,127.072492),
                        new LatLng(37.600838    ,127.072802),
                        new LatLng(37.606307    ,127.071233),
                        new LatLng(37.606347    ,127.071227),
                        new LatLng(37.606419    ,127.071218),
                        new LatLng(37.606686    ,127.071173),
                        new LatLng(37.607322    ,127.071079),
                        new LatLng(37.607364    ,127.071076),
                        new LatLng(37.607629    ,127.071061),
                        new LatLng(37.615398    ,127.070164),
                        new LatLng(37.616401    ,127.071381),
                        new LatLng(37.616517    ,127.071834),
                        new LatLng(37.616534    ,127.071927),
                        new LatLng(37.616535    ,127.071931),
                        new LatLng(37.616538    ,127.071953),
                        new LatLng(37.616509    ,127.071963),
                        new LatLng(37.616531    ,127.072181),
                        new LatLng(37.616532    ,127.072193),
                        new LatLng(37.616792    ,127.073928),
                        new LatLng(37.616794    ,127.073935),
                        new LatLng(37.616796    ,127.073949),
                        new LatLng(37.616799    ,127.073962),
                        new LatLng(37.616833    ,127.074157),
                        new LatLng(37.616916    ,127.074213),
                        new LatLng(37.616937    ,127.074241),
                        new LatLng(37.616978    ,127.074297),
                        new LatLng(37.616981    ,127.074303),
                        new LatLng(37.617038    ,127.074474),
                        new LatLng(37.617004    ,127.074564),
                        new LatLng(37.617143    ,127.07536),
                        new LatLng(37.617145    ,127.075367),
                        new LatLng(37.617168    ,127.075373),
                        new LatLng(37.617163    ,127.075534),
                        new LatLng(37.617365    ,127.076196),
                        new LatLng(37.617428    ,127.07632),
                        new LatLng(37.617467    ,127.076526),
                        new LatLng(37.617497    ,127.076518),
                        new LatLng(37.617751    ,127.077255),
                        new LatLng(37.617869    ,127.077729),
                        new LatLng(37.617903    ,127.077697),
                        new LatLng(37.617915    ,127.077727),
                        new LatLng(37.619188    ,127.081402),
                        new LatLng(37.619193    ,127.081428),
                        new LatLng(37.619773    ,127.083789),
                        new LatLng(37.619778    ,127.083789),
                        new LatLng(37.619843    ,127.084023),
                        new LatLng(37.620274    ,127.08668),
                        new LatLng(37.620235    ,127.086961),
                        new LatLng(37.619933    ,127.087946),
                        new LatLng(37.619908    ,127.088056),
                        new LatLng(37.619895    ,127.088297),
                        new LatLng(37.619912    ,127.088335),
                        new LatLng(37.620139    ,127.088859),
                        new LatLng(37.620135    ,127.088865),
                        new LatLng(37.620127    ,127.088877),
                        new LatLng(37.620064    ,127.088934),
                        new LatLng(37.619406    ,127.0902),
                        new LatLng(37.6194      ,127.090219),
                        new LatLng(37.619396    ,127.090232),
                        new LatLng(37.619394    ,127.090238),
                        new LatLng(37.619392    ,127.090244),
                        new LatLng(37.619386    ,127.090265),
                        new LatLng(37.619352    ,127.090371),
                        new LatLng(37.61929     ,127.090568),
                        new LatLng(37.619122    ,127.090871),
                        new LatLng(37.619048    ,127.091081),
                        new LatLng(37.619011    ,127.091171),
                        new LatLng(37.618986    ,127.091277),
                        new LatLng(37.618958    ,127.091384),
                        new LatLng(37.618947    ,127.091405),
                        new LatLng(37.618101    ,127.092921),
                        new LatLng(37.618097    ,127.092931),
                        new LatLng(37.618071    ,127.093018),
                        new LatLng(37.618073    ,127.093054),
                        new LatLng(37.618029    ,127.093228),
                        new LatLng(37.618036    ,127.093222),
                        new LatLng(37.618067    ,127.093642),
                        new LatLng(37.618068    ,127.093654),
                        new LatLng(37.619021    ,127.096196),
                        new LatLng(37.619025    ,127.096205),
                        new LatLng(37.61903     ,127.096211),
                        new LatLng(37.619216    ,127.096398),
                        new LatLng(37.619224    ,127.096406),
                        new LatLng(37.61923     ,127.096412),
                        new LatLng(37.619234    ,127.096416),
                        new LatLng(37.619239    ,127.096421),
                        new LatLng(37.61924     ,127.096422),
                        new LatLng(37.619243    ,127.096425),
                        new LatLng(37.619248    ,127.09643),
                        new LatLng(37.619253    ,127.096435),
                        new LatLng(37.619256    ,127.096439),
                        new LatLng(37.619261    ,127.096443),
                        new LatLng(37.619265    ,127.096447),
                        new LatLng(37.619274    ,127.096457),
                        new LatLng(37.619278    ,127.09646),
                        new LatLng(37.61928     ,127.096463),
                        new LatLng(37.619282    ,127.096465),
                        new LatLng(37.619287    ,127.096469),
                        new LatLng(37.619289    ,127.096471),
                        new LatLng(37.61929     ,127.096473),
                        new LatLng(37.619294    ,127.096477),
                        new LatLng(37.619298    ,127.09648),
                        new LatLng(37.619304    ,127.096487),
                        new LatLng(37.619309    ,127.096491),
                        new LatLng(37.619311    ,127.096493),
                        new LatLng(37.619312    ,127.096495),
                        new LatLng(37.619316    ,127.096499),
                        new LatLng(37.619321    ,127.096503),
                        new LatLng(37.619323    ,127.096505),
                        new LatLng(37.619324    ,127.096507),
                        new LatLng(37.619328    ,127.096511),
                        new LatLng(37.619338    ,127.096521),
                        new LatLng(37.619343    ,127.096525),
                        new LatLng(37.619346    ,127.096529),
                        new LatLng(37.61936     ,127.096542),
                        new LatLng(37.619367    ,127.09655),
                        new LatLng(37.619372    ,127.096555),
                        new LatLng(37.619379    ,127.096562),
                        new LatLng(37.619385    ,127.096567),
                        new LatLng(37.619389    ,127.096572),
                        new LatLng(37.619393    ,127.096576),
                        new LatLng(37.619404    ,127.096587),
                        new LatLng(37.619413    ,127.096596),
                        new LatLng(37.619415    ,127.096598),
                        new LatLng(37.61942     ,127.096603),
                        new LatLng(37.619423    ,127.096606),
                        new LatLng(37.619433    ,127.096616),
                        new LatLng(37.619562    ,127.096651),
                        new LatLng(37.619565    ,127.09666),
                        new LatLng(37.619569    ,127.096672),
                        new LatLng(37.619598    ,127.096762),
                        new LatLng(37.619612    ,127.096803),
                        new LatLng(37.619745    ,127.097176),
                        new LatLng(37.619747    ,127.097182),
                        new LatLng(37.619759    ,127.097217),
                        new LatLng(37.619777    ,127.097265),
                        new LatLng(37.619779    ,127.097272),
                        new LatLng(37.619781    ,127.097279),
                        new LatLng(37.619783    ,127.097284),
                        new LatLng(37.619787    ,127.097295),
                        new LatLng(37.61979     ,127.097301),
                        new LatLng(37.619792    ,127.097307),
                        new LatLng(37.619793    ,127.09731),
                        new LatLng(37.619795    ,127.097316),
                        new LatLng(37.619797    ,127.097321),
                        new LatLng(37.619798    ,127.097324),
                        new LatLng(37.619799    ,127.097327),
                        new LatLng(37.6198      ,127.09733),
                        new LatLng(37.619801    ,127.097333),
                        new LatLng(37.619802    ,127.097336),
                        new LatLng(37.619803    ,127.097339),
                        new LatLng(37.619806    ,127.097348),
                        new LatLng(37.619864    ,127.097524),
                        new LatLng(37.619865    ,127.097527),
                        new LatLng(37.619868    ,127.097537),
                        new LatLng(37.619883    ,127.097582),
                        new LatLng(37.619898    ,127.097625),
                        new LatLng(37.619901    ,127.097633),
                        new LatLng(37.619904    ,127.09764),
                        new LatLng(37.619906    ,127.097647),
                        new LatLng(37.61997     ,127.097829),
                        new LatLng(37.62        ,127.097916),
                        new LatLng(37.620006    ,127.097934),
                        new LatLng(37.620008    ,127.097941),
                        new LatLng(37.620102    ,127.098206),
                        new LatLng(37.620175    ,127.098409),
                        new LatLng(37.620176    ,127.098412),
                        new LatLng(37.620334    ,127.098926),
                        new LatLng(37.620331    ,127.098933),
                        new LatLng(37.620328    ,127.098938),
                        new LatLng(37.620324    ,127.098945),
                        new LatLng(37.62032     ,127.098952),
                        new LatLng(37.620316    ,127.098958),
                        new LatLng(37.620313    ,127.098963),
                        new LatLng(37.620309    ,127.098969),
                        new LatLng(37.620303    ,127.098978),
                        new LatLng(37.6203      ,127.098984),
                        new LatLng(37.620281    ,127.099013),
                        new LatLng(37.620278    ,127.099019),
                        new LatLng(37.620277    ,127.099021),
                        new LatLng(37.620274    ,127.099026),
                        new LatLng(37.620271    ,127.09903),
                        new LatLng(37.620215    ,127.099119),
                        new LatLng(37.620004    ,127.101698),
                        new LatLng(37.620025    ,127.101694),
                        new LatLng(37.620371    ,127.105556),
                        new LatLng(37.620213    ,127.105562),
                        new LatLng(37.620158    ,127.105565),
                        new LatLng(37.620341    ,127.106041),
                        new LatLng(37.620465    ,127.107085),
                        new LatLng(37.620507    ,127.107243),
                        new LatLng(37.620517    ,127.108946),
                        new LatLng(37.620515    ,127.109128),
                        new LatLng(37.620811    ,127.110175),
                        new LatLng(37.621055    ,127.110519),
                        new LatLng(37.620307    ,127.112143),
                        new LatLng(37.620063    ,127.113399),
                        new LatLng(37.619896    ,127.113939),
                        new LatLng(37.619538    ,127.115021),
                        new LatLng(37.619634    ,127.115715),
                        new LatLng(37.619115    ,127.116079),
                        new LatLng(37.617888    ,127.117147),
                        new LatLng(37.617676    ,127.11709),
                        new LatLng(37.617401    ,127.117016),
                        new LatLng(37.616367    ,127.116905),
                        new LatLng(37.616015    ,127.116672),
                        new LatLng(37.615484    ,127.116736),
                        new LatLng(37.614955    ,127.116796),
                        new LatLng(37.614144    ,127.117061),
                        new LatLng(37.612245    ,127.117374),
                        new LatLng(37.611771    ,127.117483),
                        new LatLng(37.611181    ,127.117287),
                        new LatLng(37.610706    ,127.116962),
                        new LatLng(37.610426    ,127.116828),
                        new LatLng(37.609531    ,127.116878),
                        new LatLng(37.608849    ,127.116698),
                        new LatLng(37.60886     ,127.116798),
                        new LatLng(37.608479    ,127.117538),
                        new LatLng(37.605886    ,127.117998),
                        new LatLng(37.605567    ,127.118094),
                        new LatLng(37.605444    ,127.118096)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"중랑구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "중랑구\n   "+hmap.get(name), new LatLng(37.598449, 127.092959));
    }//서울 중랑구
    public void drawPolygon163(GoogleMap googlemap) { //서울 노원구
        String name = "노원구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.636489    ,127.112483),
                        new LatLng(37.635564    ,127.112252),
                        new LatLng(37.634757    ,127.112347),
                        new LatLng(37.631228    ,127.111371),
                        new LatLng(37.629302    ,127.108885),
                        new LatLng(37.627331    ,127.105964),
                        new LatLng(37.625265    ,127.105015),
                        new LatLng(37.623271    ,127.104328),
                        new LatLng(37.621567    ,127.104909),
                        new LatLng(37.620902    ,127.105383),
                        new LatLng(37.620371    ,127.105556),
                        new LatLng(37.620025    ,127.101694),
                        new LatLng(37.620004    ,127.101698),
                        new LatLng(37.620215    ,127.099119),
                        new LatLng(37.620271    ,127.09903),
                        new LatLng(37.620274    ,127.099026),
                        new LatLng(37.620277    ,127.099021),
                        new LatLng(37.620278    ,127.099019),
                        new LatLng(37.620281    ,127.099013),
                        new LatLng(37.6203      ,127.098984),
                        new LatLng(37.620303    ,127.098978),
                        new LatLng(37.620309    ,127.098969),
                        new LatLng(37.620313    ,127.098963),
                        new LatLng(37.620316    ,127.098958),
                        new LatLng(37.62032     ,127.098952),
                        new LatLng(37.620324    ,127.098945),
                        new LatLng(37.620328    ,127.098938),
                        new LatLng(37.620331    ,127.098933),
                        new LatLng(37.620334    ,127.098926),
                        new LatLng(37.620176    ,127.098412),
                        new LatLng(37.620175    ,127.098409),
                        new LatLng(37.620102    ,127.098206),
                        new LatLng(37.620008    ,127.097941),
                        new LatLng(37.620006    ,127.097934),
                        new LatLng(37.62        ,127.097916),
                        new LatLng(37.61997     ,127.097829),
                        new LatLng(37.619906    ,127.097647),
                        new LatLng(37.619904    ,127.09764),
                        new LatLng(37.619901    ,127.097633),
                        new LatLng(37.619898    ,127.097625),
                        new LatLng(37.619883    ,127.097582),
                        new LatLng(37.619868    ,127.097537),
                        new LatLng(37.619865    ,127.097527),
                        new LatLng(37.619864    ,127.097524),
                        new LatLng(37.619806    ,127.097348),
                        new LatLng(37.619803    ,127.097339),
                        new LatLng(37.619802    ,127.097336),
                        new LatLng(37.619801    ,127.097333),
                        new LatLng(37.6198      ,127.09733),
                        new LatLng(37.619799    ,127.097327),
                        new LatLng(37.619798    ,127.097324),
                        new LatLng(37.619797    ,127.097321),
                        new LatLng(37.619795    ,127.097316),
                        new LatLng(37.619793    ,127.09731),
                        new LatLng(37.619792    ,127.097307),
                        new LatLng(37.61979     ,127.097301),
                        new LatLng(37.619787    ,127.097295),
                        new LatLng(37.619783    ,127.097284),
                        new LatLng(37.619781    ,127.097279),
                        new LatLng(37.619779    ,127.097272),
                        new LatLng(37.619777    ,127.097265),
                        new LatLng(37.619759    ,127.097217),
                        new LatLng(37.619747    ,127.097182),
                        new LatLng(37.619745    ,127.097176),
                        new LatLng(37.619612    ,127.096803),
                        new LatLng(37.619598    ,127.096762),
                        new LatLng(37.619569    ,127.096672),
                        new LatLng(37.619565    ,127.09666),
                        new LatLng(37.619562    ,127.096651),
                        new LatLng(37.619433    ,127.096616),
                        new LatLng(37.619423    ,127.096606),
                        new LatLng(37.61942     ,127.096603),
                        new LatLng(37.619415    ,127.096598),
                        new LatLng(37.619413    ,127.096596),
                        new LatLng(37.619404    ,127.096587),
                        new LatLng(37.619393    ,127.096576),
                        new LatLng(37.619389    ,127.096572),
                        new LatLng(37.619385    ,127.096567),
                        new LatLng(37.619379    ,127.096562),
                        new LatLng(37.619372    ,127.096555),
                        new LatLng(37.619367    ,127.09655),
                        new LatLng(37.61936     ,127.096542),
                        new LatLng(37.619346    ,127.096529),
                        new LatLng(37.619343    ,127.096525),
                        new LatLng(37.619338    ,127.096521),
                        new LatLng(37.619328    ,127.096511),
                        new LatLng(37.619324    ,127.096507),
                        new LatLng(37.619323    ,127.096505),
                        new LatLng(37.619321    ,127.096503),
                        new LatLng(37.619316    ,127.096499),
                        new LatLng(37.619312    ,127.096495),
                        new LatLng(37.619311    ,127.096493),
                        new LatLng(37.619309    ,127.096491),
                        new LatLng(37.619304    ,127.096487),
                        new LatLng(37.619298    ,127.09648),
                        new LatLng(37.619294    ,127.096477),
                        new LatLng(37.61929     ,127.096473),
                        new LatLng(37.619289    ,127.096471),
                        new LatLng(37.619287    ,127.096469),
                        new LatLng(37.619282    ,127.096465),
                        new LatLng(37.61928     ,127.096463),
                        new LatLng(37.619278    ,127.09646),
                        new LatLng(37.619274    ,127.096457),
                        new LatLng(37.619265    ,127.096447),
                        new LatLng(37.619261    ,127.096443),
                        new LatLng(37.619256    ,127.096439),
                        new LatLng(37.619253    ,127.096435),
                        new LatLng(37.619248    ,127.09643),
                        new LatLng(37.619243    ,127.096425),
                        new LatLng(37.61924     ,127.096422),
                        new LatLng(37.619239    ,127.096421),
                        new LatLng(37.619234    ,127.096416),
                        new LatLng(37.61923     ,127.096412),
                        new LatLng(37.619224    ,127.096406),
                        new LatLng(37.619216    ,127.096398),
                        new LatLng(37.61903     ,127.096211),
                        new LatLng(37.619025    ,127.096205),
                        new LatLng(37.619021    ,127.096196),
                        new LatLng(37.618068    ,127.093654),
                        new LatLng(37.618067    ,127.093642),
                        new LatLng(37.618036    ,127.093222),
                        new LatLng(37.618029    ,127.093228),
                        new LatLng(37.618073    ,127.093054),
                        new LatLng(37.618071    ,127.093018),
                        new LatLng(37.618097    ,127.092931),
                        new LatLng(37.618101    ,127.092921),
                        new LatLng(37.618947    ,127.091405),
                        new LatLng(37.618958    ,127.091384),
                        new LatLng(37.618986    ,127.091277),
                        new LatLng(37.619011    ,127.091171),
                        new LatLng(37.619048    ,127.091081),
                        new LatLng(37.619122    ,127.090871),
                        new LatLng(37.61929     ,127.090568),
                        new LatLng(37.619352    ,127.090371),
                        new LatLng(37.619386    ,127.090265),
                        new LatLng(37.619392    ,127.090244),
                        new LatLng(37.619394    ,127.090238),
                        new LatLng(37.619396    ,127.090232),
                        new LatLng(37.6194      ,127.090219),
                        new LatLng(37.619406    ,127.0902),
                        new LatLng(37.620064    ,127.088934),
                        new LatLng(37.620127    ,127.088877),
                        new LatLng(37.620135    ,127.088865),
                        new LatLng(37.620139    ,127.088859),
                        new LatLng(37.619912    ,127.088335),
                        new LatLng(37.619895    ,127.088297),
                        new LatLng(37.619908    ,127.088056),
                        new LatLng(37.619933    ,127.087946),
                        new LatLng(37.620235    ,127.086961),
                        new LatLng(37.620274    ,127.08668),
                        new LatLng(37.619843    ,127.084023),
                        new LatLng(37.619778    ,127.083789),
                        new LatLng(37.619773    ,127.083789),
                        new LatLng(37.619193    ,127.081428),
                        new LatLng(37.619188    ,127.081402),
                        new LatLng(37.617915    ,127.077727),
                        new LatLng(37.617903    ,127.077697),
                        new LatLng(37.617869    ,127.077729),
                        new LatLng(37.617751    ,127.077255),
                        new LatLng(37.617497    ,127.076518),
                        new LatLng(37.617467    ,127.076526),
                        new LatLng(37.617428    ,127.07632),
                        new LatLng(37.617365    ,127.076196),
                        new LatLng(37.617163    ,127.075534),
                        new LatLng(37.617168    ,127.075373),
                        new LatLng(37.617145    ,127.075367),
                        new LatLng(37.617143    ,127.07536),
                        new LatLng(37.617004    ,127.074564),
                        new LatLng(37.617038    ,127.074474),
                        new LatLng(37.616981    ,127.074303),
                        new LatLng(37.616978    ,127.074297),
                        new LatLng(37.616937    ,127.074241),
                        new LatLng(37.616916    ,127.074213),
                        new LatLng(37.616833    ,127.074157),
                        new LatLng(37.616799    ,127.073962),
                        new LatLng(37.616796    ,127.073949),
                        new LatLng(37.616794    ,127.073935),
                        new LatLng(37.616792    ,127.073928),
                        new LatLng(37.616532    ,127.072193),
                        new LatLng(37.616531    ,127.072181),
                        new LatLng(37.616509    ,127.071963),
                        new LatLng(37.616538    ,127.071953),
                        new LatLng(37.616535    ,127.071931),
                        new LatLng(37.616534    ,127.071927),
                        new LatLng(37.616517    ,127.071834),
                        new LatLng(37.616401    ,127.071381),
                        new LatLng(37.615398    ,127.070164),
                        new LatLng(37.615452    ,127.068157),
                        new LatLng(37.615019    ,127.066065),
                        new LatLng(37.61427     ,127.063285),
                        new LatLng(37.614267    ,127.063247),
                        new LatLng(37.614296    ,127.063081),
                        new LatLng(37.614314    ,127.062936),
                        new LatLng(37.614352    ,127.062584),
                        new LatLng(37.614373    ,127.062378),
                        new LatLng(37.614427    ,127.062032),
                        new LatLng(37.614433    ,127.062004),
                        new LatLng(37.614494    ,127.061757),
                        new LatLng(37.614755    ,127.061288),
                        new LatLng(37.614915    ,127.061113),
                        new LatLng(37.614938    ,127.06109),
                        new LatLng(37.615104    ,127.060976),
                        new LatLng(37.615121    ,127.060965),
                        new LatLng(37.615133    ,127.060956),
                        new LatLng(37.615142    ,127.06095),
                        new LatLng(37.615166    ,127.060933),
                        new LatLng(37.61522     ,127.060899),
                        new LatLng(37.615232    ,127.060891),
                        new LatLng(37.615254    ,127.060879),
                        new LatLng(37.615303    ,127.060853),
                        new LatLng(37.615356    ,127.060824),
                        new LatLng(37.615553    ,127.060722),
                        new LatLng(37.615665    ,127.060666),
                        new LatLng(37.615972    ,127.060204),
                        new LatLng(37.616262    ,127.059562),
                        new LatLng(37.616298    ,127.059486),
                        new LatLng(37.616307    ,127.059467),
                        new LatLng(37.616371    ,127.05935),
                        new LatLng(37.616569    ,127.058986),
                        new LatLng(37.616639    ,127.058878),
                        new LatLng(37.61689     ,127.058488),
                        new LatLng(37.616937    ,127.058409),
                        new LatLng(37.616969    ,127.058353),
                        new LatLng(37.616985    ,127.058325),
                        new LatLng(37.617125    ,127.058079),
                        new LatLng(37.617385    ,127.057593),
                        new LatLng(37.617425    ,127.057519),
                        new LatLng(37.61747     ,127.057438),
                        new LatLng(37.617734    ,127.057162),
                        new LatLng(37.617846    ,127.057053),
                        new LatLng(37.618547    ,127.056341),
                        new LatLng(37.61867     ,127.05621),
                        new LatLng(37.618739    ,127.056132),
                        new LatLng(37.618915    ,127.055934),
                        new LatLng(37.619148    ,127.055586),
                        new LatLng(37.619179    ,127.055536),
                        new LatLng(37.619384    ,127.055149),
                        new LatLng(37.619419    ,127.055078),
                        new LatLng(37.619552    ,127.054808),
                        new LatLng(37.619749    ,127.05448),
                        new LatLng(37.619796    ,127.054408),
                        new LatLng(37.620051    ,127.054122),
                        new LatLng(37.620086    ,127.054082),
                        new LatLng(37.620099    ,127.054069),
                        new LatLng(37.620115    ,127.054054),
                        new LatLng(37.620431    ,127.05376),
                        new LatLng(37.620459    ,127.053735),
                        new LatLng(37.620522    ,127.053678),
                        new LatLng(37.622157    ,127.05207),
                        new LatLng(37.622242    ,127.052112),
                        new LatLng(37.622559    ,127.051758),
                        new LatLng(37.622693    ,127.051613),
                        new LatLng(37.623551    ,127.050492),
                        new LatLng(37.623853    ,127.050207),
                        new LatLng(37.624074    ,127.049964),
                        new LatLng(37.624266    ,127.049737),
                        new LatLng(37.624335    ,127.049659),
                        new LatLng(37.624855    ,127.049273),
                        new LatLng(37.625415    ,127.048963),
                        new LatLng(37.625839    ,127.048651),
                        new LatLng(37.626287    ,127.048188),
                        new LatLng(37.627522    ,127.047057),
                        new LatLng(37.628417    ,127.045267),
                        new LatLng(37.629368    ,127.043801),
                        new LatLng(37.630451    ,127.042599),
                        new LatLng(37.630614    ,127.042549),
                        new LatLng(37.631857    ,127.042034),
                        new LatLng(37.631897    ,127.042072),
                        new LatLng(37.632167    ,127.042332),
                        new LatLng(37.63277     ,127.042956),
                        new LatLng(37.632812    ,127.04309),
                        new LatLng(37.633254    ,127.043492),
                        new LatLng(37.633282    ,127.043579),
                        new LatLng(37.633273    ,127.043586),
                        new LatLng(37.633478    ,127.043917),
                        new LatLng(37.633577    ,127.043996),
                        new LatLng(37.633816    ,127.044139),
                        new LatLng(37.633976    ,127.044162),
                        new LatLng(37.634024    ,127.044207),
                        new LatLng(37.634112    ,127.044262),
                        new LatLng(37.634278    ,127.044277),
                        new LatLng(37.634488    ,127.044437),
                        new LatLng(37.634774    ,127.044524),
                        new LatLng(37.634839    ,127.044504),
                        new LatLng(37.634968    ,127.044501),
                        new LatLng(37.635264    ,127.04458),
                        new LatLng(37.636254    ,127.04504),
                        new LatLng(37.636555    ,127.045003),
                        new LatLng(37.637077    ,127.045178),
                        new LatLng(37.637199    ,127.045243),
                        new LatLng(37.637398    ,127.045359),
                        new LatLng(37.637486    ,127.045334),
                        new LatLng(37.637704    ,127.045546),
                        new LatLng(37.638773    ,127.046267),
                        new LatLng(37.638849    ,127.046239),
                        new LatLng(37.639147    ,127.04647),
                        new LatLng(37.639271    ,127.046522),
                        new LatLng(37.639499    ,127.046652),
                        new LatLng(37.639722    ,127.04675),
                        new LatLng(37.640642    ,127.046508),
                        new LatLng(37.640927    ,127.046548),
                        new LatLng(37.641141    ,127.046587),
                        new LatLng(37.641223    ,127.046718),
                        new LatLng(37.641198    ,127.047812),
                        new LatLng(37.641404    ,127.047786),
                        new LatLng(37.641557    ,127.048023),
                        new LatLng(37.64187     ,127.048537),
                        new LatLng(37.642728    ,127.049382),
                        new LatLng(37.642863    ,127.049414),
                        new LatLng(37.643525    ,127.049759),
                        new LatLng(37.64355     ,127.049934),
                        new LatLng(37.643622    ,127.050099),
                        new LatLng(37.643667    ,127.050398),
                        new LatLng(37.643687    ,127.050445),
                        new LatLng(37.643706    ,127.050484),
                        new LatLng(37.643711    ,127.050496),
                        new LatLng(37.643737    ,127.050551),
                        new LatLng(37.643761    ,127.050602),
                        new LatLng(37.643806    ,127.050693),
                        new LatLng(37.6441      ,127.050524),
                        new LatLng(37.644253    ,127.05052),
                        new LatLng(37.644494    ,127.050411),
                        new LatLng(37.644609    ,127.050353),
                        new LatLng(37.644831    ,127.050467),
                        new LatLng(37.64482     ,127.05053),
                        new LatLng(37.644784    ,127.050734),
                        new LatLng(37.6447      ,127.050926),
                        new LatLng(37.644735    ,127.051052),
                        new LatLng(37.64475     ,127.051114),
                        new LatLng(37.64478     ,127.05123),
                        new LatLng(37.644662    ,127.051418),
                        new LatLng(37.644564    ,127.051494),
                        new LatLng(37.644444    ,127.051556),
                        new LatLng(37.644402    ,127.051565),
                        new LatLng(37.644021    ,127.051694),
                        new LatLng(37.643969    ,127.051729),
                        new LatLng(37.643957    ,127.051737),
                        new LatLng(37.643833    ,127.051791),
                        new LatLng(37.643624    ,127.051879),
                        new LatLng(37.64344     ,127.051937),
                        new LatLng(37.643397    ,127.051958),
                        new LatLng(37.643353    ,127.051981),
                        new LatLng(37.643342    ,127.051987),
                        new LatLng(37.643325    ,127.051996),
                        new LatLng(37.643316    ,127.052),
                        new LatLng(37.643291    ,127.052013),
                        new LatLng(37.643235    ,127.052042),
                        new LatLng(37.642861    ,127.052206),
                        new LatLng(37.642834    ,127.052221),
                        new LatLng(37.642793    ,127.052244),
                        new LatLng(37.642782    ,127.05225),
                        new LatLng(37.64262     ,127.052325),
                        new LatLng(37.642498    ,127.0524),
                        new LatLng(37.642444    ,127.05243),
                        new LatLng(37.642353    ,127.052501),
                        new LatLng(37.642309    ,127.052534),
                        new LatLng(37.64224     ,127.052583),
                        new LatLng(37.64212     ,127.05267),
                        new LatLng(37.641806    ,127.052852),
                        new LatLng(37.6415      ,127.053151),
                        new LatLng(37.641104    ,127.05358),
                        new LatLng(37.640925    ,127.053775),
                        new LatLng(37.6409      ,127.053812),
                        new LatLng(37.640858    ,127.053875),
                        new LatLng(37.640847    ,127.053892),
                        new LatLng(37.640827    ,127.053918),
                        new LatLng(37.640774    ,127.053986),
                        new LatLng(37.64074     ,127.054031),
                        new LatLng(37.640718    ,127.054054),
                        new LatLng(37.640691    ,127.054081),
                        new LatLng(37.640322    ,127.054535),
                        new LatLng(37.640268    ,127.054586),
                        new LatLng(37.640253    ,127.0546),
                        new LatLng(37.64023     ,127.054621),
                        new LatLng(37.640196    ,127.054654),
                        new LatLng(37.640165    ,127.054693),
                        new LatLng(37.640153    ,127.054708),
                        new LatLng(37.640147    ,127.054716),
                        new LatLng(37.640137    ,127.054729),
                        new LatLng(37.640453    ,127.055408),
                        new LatLng(37.640626    ,127.055777),
                        new LatLng(37.641334    ,127.055123),
                        new LatLng(37.642355    ,127.054994),
                        new LatLng(37.642747    ,127.055051),
                        new LatLng(37.642893    ,127.055072),
                        new LatLng(37.643601    ,127.055211),
                        new LatLng(37.643642    ,127.055229),
                        new LatLng(37.644598    ,127.055651),
                        new LatLng(37.645951    ,127.055873),
                        new LatLng(37.647049    ,127.055825),
                        new LatLng(37.647066    ,127.055821),
                        new LatLng(37.648969    ,127.055002),
                        new LatLng(37.649049    ,127.054955),
                        new LatLng(37.649395    ,127.054748),
                        new LatLng(37.649436    ,127.054727),
                        new LatLng(37.650144    ,127.054226),
                        new LatLng(37.650576    ,127.053949),
                        new LatLng(37.651149    ,127.05383),
                        new LatLng(37.6521      ,127.053748),
                        new LatLng(37.653058    ,127.053924),
                        new LatLng(37.654158    ,127.054025),
                        new LatLng(37.654471    ,127.054053),
                        new LatLng(37.654751    ,127.054038),
                        new LatLng(37.654786    ,127.054036),
                        new LatLng(37.655044    ,127.054021),
                        new LatLng(37.655417    ,127.053949),
                        new LatLng(37.655731    ,127.053788),
                        new LatLng(37.656639    ,127.053599),
                        new LatLng(37.65677     ,127.053598),
                        new LatLng(37.657518    ,127.053408),
                        new LatLng(37.657616    ,127.053348),
                        new LatLng(37.65765     ,127.053327),
                        new LatLng(37.657674    ,127.053312),
                        new LatLng(37.659922    ,127.051795),
                        new LatLng(37.661867    ,127.05157),
                        new LatLng(37.663966    ,127.05141),
                        new LatLng(37.664369    ,127.051031),
                        new LatLng(37.664823    ,127.050869),
                        new LatLng(37.665844    ,127.050282),
                        new LatLng(37.666842    ,127.04946),
                        new LatLng(37.670478    ,127.04819),
                        new LatLng(37.670874    ,127.048173),
                        new LatLng(37.671289    ,127.048176),
                        new LatLng(37.673766    ,127.048747),
                        new LatLng(37.675152    ,127.04885),
                        new LatLng(37.676718    ,127.049721),
                        new LatLng(37.677413    ,127.049959),
                        new LatLng(37.679683    ,127.050688),
                        new LatLng(37.680058    ,127.0509),
                        new LatLng(37.680069    ,127.050907),
                        new LatLng(37.680094    ,127.05092),
                        new LatLng(37.680331    ,127.050969),
                        new LatLng(37.680418    ,127.050987),
                        new LatLng(37.680591    ,127.051023),
                        new LatLng(37.681534    ,127.051164),
                        new LatLng(37.681589    ,127.051172),
                        new LatLng(37.682594    ,127.051791),
                        new LatLng(37.68261     ,127.051801),
                        new LatLng(37.683323    ,127.051991),
                        new LatLng(37.684521    ,127.052072),
                        new LatLng(37.685738    ,127.051821),
                        new LatLng(37.685814    ,127.051804),
                        new LatLng(37.694922    ,127.063386),
                        new LatLng(37.6938      ,127.072658),
                        new LatLng(37.696137    ,127.081105),
                        new LatLng(37.691782    ,127.083905),
                        new LatLng(37.69039     ,127.085177),
                        new LatLng(37.68957     ,127.090552),
                        new LatLng(37.689966    ,127.093037),
                        new LatLng(37.689132    ,127.09429),
                        new LatLng(37.689385    ,127.095097),
                        new LatLng(37.689071    ,127.095996),
                        new LatLng(37.685637    ,127.096423),
                        new LatLng(37.68464     ,127.09581),
                        new LatLng(37.683419    ,127.094288),
                        new LatLng(37.682742    ,127.093883),
                        new LatLng(37.681551    ,127.092919),
                        new LatLng(37.677528    ,127.092575),
                        new LatLng(37.677345    ,127.092968),
                        new LatLng(37.676143    ,127.093886),
                        new LatLng(37.675617    ,127.093963),
                        new LatLng(37.673536    ,127.094615),
                        new LatLng(37.672891    ,127.095147),
                        new LatLng(37.67185     ,127.096019),
                        new LatLng(37.67116     ,127.095806),
                        new LatLng(37.670079    ,127.096013),
                        new LatLng(37.666172    ,127.094581),
                        new LatLng(37.659335    ,127.091207),
                        new LatLng(37.655492    ,127.092352),
                        new LatLng(37.652962    ,127.093916),
                        new LatLng(37.65254     ,127.094041),
                        new LatLng(37.652295    ,127.094),
                        new LatLng(37.651901    ,127.093725),
                        new LatLng(37.649346    ,127.092716),
                        new LatLng(37.648361    ,127.092917),
                        new LatLng(37.647322    ,127.093834),
                        new LatLng(37.645924    ,127.094369),
                        new LatLng(37.645506    ,127.094538),
                        new LatLng(37.644657    ,127.094529),
                        new LatLng(37.645205    ,127.1028),
                        new LatLng(37.645493    ,127.103921),
                        new LatLng(37.645404    ,127.105312),
                        new LatLng(37.644693    ,127.107863),
                        new LatLng(37.64446     ,127.108006),
                        new LatLng(37.643666    ,127.10867),
                        new LatLng(37.643613    ,127.108712),
                        new LatLng(37.642859    ,127.109289),
                        new LatLng(37.642488    ,127.109899),
                        new LatLng(37.64273     ,127.110751),
                        new LatLng(37.642564    ,127.111155),
                        new LatLng(37.642376    ,127.111258),
                        new LatLng(37.642315    ,127.11129),
                        new LatLng(37.641929    ,127.11143),
                        new LatLng(37.641247    ,127.111615),
                        new LatLng(37.639897    ,127.111702),
                        new LatLng(37.639944    ,127.111136),
                        new LatLng(37.638774    ,127.110761),
                        new LatLng(37.638309    ,127.111184),
                        new LatLng(37.638222    ,127.111454),
                        new LatLng(37.637991    ,127.111599),
                        new LatLng(37.637846    ,127.111687),
                        new LatLng(37.637408    ,127.111951),
                        new LatLng(37.636489    ,127.112483)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"노원구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "노원구\n   "+hmap.get(name), new LatLng(37.653511, 127.075081));
    }//서울 노원구
    public void drawPolygon147(GoogleMap googlemap) { //서울 도봉구
        String name = "도봉구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.645951    ,127.055873 ),
                        new LatLng(37.644598    ,127.055651 ),
                        new LatLng(37.643642    ,127.055229 ),
                        new LatLng(37.643601    ,127.055211 ),
                        new LatLng(37.642893    ,127.055072 ),
                        new LatLng(37.642747    ,127.055051 ),
                        new LatLng(37.642355    ,127.054994 ),
                        new LatLng(37.641334    ,127.055123 ),
                        new LatLng(37.640626    ,127.055777 ),
                        new LatLng(37.640453    ,127.055408 ),
                        new LatLng(37.640137    ,127.054729 ),
                        new LatLng(37.640147    ,127.054716 ),
                        new LatLng(37.640153    ,127.054708 ),
                        new LatLng(37.640165    ,127.054693 ),
                        new LatLng(37.640196    ,127.054654 ),
                        new LatLng(37.64023     ,127.054621 ),
                        new LatLng(37.640253    ,127.0546   ),
                        new LatLng(37.640268    ,127.054586 ),
                        new LatLng(37.640322    ,127.054535 ),
                        new LatLng(37.640691    ,127.054081 ),
                        new LatLng(37.640718    ,127.054054 ),
                        new LatLng(37.64074     ,127.054031 ),
                        new LatLng(37.640774    ,127.053986 ),
                        new LatLng(37.640827    ,127.053918 ),
                        new LatLng(37.640847    ,127.053892 ),
                        new LatLng(37.640858    ,127.053875 ),
                        new LatLng(37.6409      ,127.053812 ),
                        new LatLng(37.640925    ,127.053775 ),
                        new LatLng(37.641104    ,127.05358  ),
                        new LatLng(37.6415      ,127.053151 ),
                        new LatLng(37.641806    ,127.052852 ),
                        new LatLng(37.64212     ,127.05267  ),
                        new LatLng(37.64224     ,127.052583 ),
                        new LatLng(37.642309    ,127.052534 ),
                        new LatLng(37.642353    ,127.052501 ),
                        new LatLng(37.642444    ,127.05243  ),
                        new LatLng(37.642498    ,127.0524   ),
                        new LatLng(37.64262     ,127.052325 ),
                        new LatLng(37.642782    ,127.05225  ),
                        new LatLng(37.642793    ,127.052244 ),
                        new LatLng(37.642834    ,127.052221 ),
                        new LatLng(37.642861    ,127.052206 ),
                        new LatLng(37.643235    ,127.052042 ),
                        new LatLng(37.643291    ,127.052013 ),
                        new LatLng(37.643316    ,127.052    ),
                        new LatLng(37.643325    ,127.051996 ),
                        new LatLng(37.643342    ,127.051987 ),
                        new LatLng(37.643353    ,127.051981 ),
                        new LatLng(37.643397    ,127.051958 ),
                        new LatLng(37.64344     ,127.051937 ),
                        new LatLng(37.643624    ,127.051879 ),
                        new LatLng(37.643833    ,127.051791 ),
                        new LatLng(37.643957    ,127.051737 ),
                        new LatLng(37.643969    ,127.051729 ),
                        new LatLng(37.644021    ,127.051694 ),
                        new LatLng(37.644402    ,127.051565 ),
                        new LatLng(37.644444    ,127.051556 ),
                        new LatLng(37.644564    ,127.051494 ),
                        new LatLng(37.644662    ,127.051418 ),
                        new LatLng(37.64478     ,127.05123  ),
                        new LatLng(37.64475     ,127.051114 ),
                        new LatLng(37.644735    ,127.051052 ),
                        new LatLng(37.6447      ,127.050926 ),
                        new LatLng(37.644784    ,127.050734 ),
                        new LatLng(37.64482     ,127.05053  ),
                        new LatLng(37.644831    ,127.050467 ),
                        new LatLng(37.644609    ,127.050353 ),
                        new LatLng(37.644494    ,127.050411 ),
                        new LatLng(37.644253    ,127.05052  ),
                        new LatLng(37.6441      ,127.050524 ),
                        new LatLng(37.643806    ,127.050693 ),
                        new LatLng(37.643761    ,127.050602 ),
                        new LatLng(37.643737    ,127.050551 ),
                        new LatLng(37.643711    ,127.050496 ),
                        new LatLng(37.643706    ,127.050484 ),
                        new LatLng(37.643687    ,127.050445 ),
                        new LatLng(37.643667    ,127.050398 ),
                        new LatLng(37.643622    ,127.050099 ),
                        new LatLng(37.64355     ,127.049934 ),
                        new LatLng(37.643525    ,127.049759 ),
                        new LatLng(37.642863    ,127.049414 ),
                        new LatLng(37.642728    ,127.049382 ),
                        new LatLng(37.64187     ,127.048537 ),
                        new LatLng(37.641557    ,127.048023 ),
                        new LatLng(37.641404    ,127.047786 ),
                        new LatLng(37.641198    ,127.047812 ),
                        new LatLng(37.641223    ,127.046718 ),
                        new LatLng(37.641141    ,127.046587 ),
                        new LatLng(37.640927    ,127.046548 ),
                        new LatLng(37.640642    ,127.046508 ),
                        new LatLng(37.639722    ,127.04675  ),
                        new LatLng(37.639499    ,127.046652 ),
                        new LatLng(37.639271    ,127.046522 ),
                        new LatLng(37.639147    ,127.04647  ),
                        new LatLng(37.638849    ,127.046239 ),
                        new LatLng(37.638773    ,127.046267 ),
                        new LatLng(37.637704    ,127.045546 ),
                        new LatLng(37.637486    ,127.045334 ),
                        new LatLng(37.637398    ,127.045359 ),
                        new LatLng(37.637199    ,127.045243 ),
                        new LatLng(37.637077    ,127.045178 ),
                        new LatLng(37.636555    ,127.045003 ),
                        new LatLng(37.636254    ,127.04504  ),
                        new LatLng(37.635264    ,127.04458  ),
                        new LatLng(37.634968    ,127.044501 ),
                        new LatLng(37.634839    ,127.044504 ),
                        new LatLng(37.634774    ,127.044524 ),
                        new LatLng(37.634488    ,127.044437 ),
                        new LatLng(37.634278    ,127.044277 ),
                        new LatLng(37.634112    ,127.044262 ),
                        new LatLng(37.634024    ,127.044207 ),
                        new LatLng(37.633976    ,127.044162 ),
                        new LatLng(37.633816    ,127.044139 ),
                        new LatLng(37.633577    ,127.043996 ),
                        new LatLng(37.633478    ,127.043917 ),
                        new LatLng(37.633273    ,127.043586 ),
                        new LatLng(37.633282    ,127.043579 ),
                        new LatLng(37.633254    ,127.043492 ),
                        new LatLng(37.632812    ,127.04309  ),
                        new LatLng(37.63277     ,127.042956 ),
                        new LatLng(37.632167    ,127.042332 ),
                        new LatLng(37.631897    ,127.042072 ),
                        new LatLng(37.631857    ,127.042034 ),
                        new LatLng(37.630614    ,127.042549 ),
                        new LatLng(37.630451    ,127.042599 ),
                        new LatLng(37.630508    ,127.04239  ),
                        new LatLng(37.630676    ,127.042086 ),
                        new LatLng(37.631548    ,127.040799 ),
                        new LatLng(37.631784    ,127.040438 ),
                        new LatLng(37.647343    ,127.024682 ),
                        new LatLng(37.650747    ,127.012905 ),
                        new LatLng(37.670855    ,127.018707 ),
                        new LatLng(37.679591    ,127.009383 ),
                        new LatLng(37.68445     ,127.008664 ),
                        new LatLng(37.685432    ,127.00844  ),
                        new LatLng(37.688507    ,127.008327 ),
                        new LatLng(37.691618    ,127.007621 ),
                        new LatLng(37.6967      ,127.009666 ),
                        new LatLng(37.700428    ,127.014961 ),
                        new LatLng(37.701455    ,127.015415 ),
                        new LatLng(37.699581    ,127.025315 ),
                        new LatLng(37.701115    ,127.027022 ),
                        new LatLng(37.700281    ,127.028216 ),
                        new LatLng(37.699292    ,127.029286 ),
                        new LatLng(37.694776    ,127.030454 ),
                        new LatLng(37.69369     ,127.030827 ),
                        new LatLng(37.693394    ,127.030993 ),
                        new LatLng(37.693075    ,127.031026 ),
                        new LatLng(37.692828    ,127.031541 ),
                        new LatLng(37.692815    ,127.03217  ),
                        new LatLng(37.691839    ,127.032414 ),
                        new LatLng(37.692021    ,127.033597 ),
                        new LatLng(37.692492    ,127.035625 ),
                        new LatLng(37.692374    ,127.035777 ),
                        new LatLng(37.69264     ,127.036763 ),
                        new LatLng(37.693221    ,127.037528 ),
                        new LatLng(37.694046    ,127.039572 ),
                        new LatLng(37.694684    ,127.039997 ),
                        new LatLng(37.6953      ,127.041109 ),
                        new LatLng(37.695382    ,127.041813 ),
                        new LatLng(37.695054    ,127.04233  ),
                        new LatLng(37.694912    ,127.04322  ),
                        new LatLng(37.694402    ,127.043398 ),
                        new LatLng(37.693793    ,127.043158 ),
                        new LatLng(37.693511    ,127.043709 ),
                        new LatLng(37.693098    ,127.044327 ),
                        new LatLng(37.693057    ,127.044584 ),
                        new LatLng(37.692936    ,127.044817 ),
                        new LatLng(37.692717    ,127.044853 ),
                        new LatLng(37.692402    ,127.044934 ),
                        new LatLng(37.692364    ,127.045296 ),
                        new LatLng(37.692564    ,127.046205 ),
                        new LatLng(37.692717    ,127.046755 ),
                        new LatLng(37.692812    ,127.046864 ),
                        new LatLng(37.693008    ,127.047274 ),
                        new LatLng(37.69325     ,127.047449 ),
                        new LatLng(37.693314    ,127.047527 ),
                        new LatLng(37.693716    ,127.048141 ),
                        new LatLng(37.69386     ,127.048186 ),
                        new LatLng(37.693952    ,127.048237 ),
                        new LatLng(37.694063    ,127.048632 ),
                        new LatLng(37.693664    ,127.048721 ),
                        new LatLng(37.692811    ,127.04892  ),
                        new LatLng(37.689207    ,127.049751 ),
                        new LatLng(37.688252    ,127.049805 ),
                        new LatLng(37.685922    ,127.051368 ),
                        new LatLng(37.685738    ,127.051821 ),
                        new LatLng(37.684521    ,127.052072 ),
                        new LatLng(37.683323    ,127.051991 ),
                        new LatLng(37.68261     ,127.051801 ),
                        new LatLng(37.682594    ,127.051791 ),
                        new LatLng(37.681589    ,127.051172 ),
                        new LatLng(37.681534    ,127.051164 ),
                        new LatLng(37.680591    ,127.051023 ),
                        new LatLng(37.680418    ,127.050987 ),
                        new LatLng(37.680331    ,127.050969 ),
                        new LatLng(37.680094    ,127.05092  ),
                        new LatLng(37.680069    ,127.050907 ),
                        new LatLng(37.680058    ,127.0509   ),
                        new LatLng(37.679683    ,127.050688 ),
                        new LatLng(37.677413    ,127.049959 ),
                        new LatLng(37.676718    ,127.049721 ),
                        new LatLng(37.675152    ,127.04885  ),
                        new LatLng(37.673766    ,127.048747 ),
                        new LatLng(37.671289    ,127.048176 ),
                        new LatLng(37.670874    ,127.048173 ),
                        new LatLng(37.670478    ,127.04819  ),
                        new LatLng(37.666842    ,127.04946  ),
                        new LatLng(37.665844    ,127.050282 ),
                        new LatLng(37.664823    ,127.050869 ),
                        new LatLng(37.664369    ,127.051031 ),
                        new LatLng(37.663966    ,127.05141  ),
                        new LatLng(37.661867    ,127.05157  ),
                        new LatLng(37.659922    ,127.051795 ),
                        new LatLng(37.657674    ,127.053312 ),
                        new LatLng(37.65765     ,127.053327 ),
                        new LatLng(37.657616    ,127.053348 ),
                        new LatLng(37.657518    ,127.053408 ),
                        new LatLng(37.65677     ,127.053598 ),
                        new LatLng(37.656639    ,127.053599 ),
                        new LatLng(37.655731    ,127.053788 ),
                        new LatLng(37.655417    ,127.053949 ),
                        new LatLng(37.655044    ,127.054021 ),
                        new LatLng(37.654786    ,127.054036 ),
                        new LatLng(37.654751    ,127.054038 ),
                        new LatLng(37.654471    ,127.054053 ),
                        new LatLng(37.654158    ,127.054025 ),
                        new LatLng(37.653058    ,127.053924 ),
                        new LatLng(37.6521      ,127.053748 ),
                        new LatLng(37.651149    ,127.05383  ),
                        new LatLng(37.650576    ,127.053949 ),
                        new LatLng(37.650144    ,127.054226 ),
                        new LatLng(37.649436    ,127.054727 ),
                        new LatLng(37.649395    ,127.054748 ),
                        new LatLng(37.649049    ,127.054955 ),
                        new LatLng(37.648969    ,127.055002 ),
                        new LatLng(37.647066    ,127.055821 ),
                        new LatLng(37.647049    ,127.055825 ),
                        new LatLng(37.645951    ,127.055873 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"도봉구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "도봉구\n   "+hmap.get(name), new LatLng(37.669760, 127.032619));
    }//서울 도봉구
    public void drawPolygon169(GoogleMap googlemap) { //서울 강북구
        String name = "강북구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.624266    ,127.049737 ),
                        new LatLng(37.624248    ,127.049705 ),
                        new LatLng(37.624052    ,127.049357 ),
                        new LatLng(37.624011    ,127.049286 ),
                        new LatLng(37.623977    ,127.049228 ),
                        new LatLng(37.623961    ,127.049198 ),
                        new LatLng(37.623616    ,127.048593 ),
                        new LatLng(37.623576    ,127.048525 ),
                        new LatLng(37.623407    ,127.048226 ),
                        new LatLng(37.623236    ,127.047926 ),
                        new LatLng(37.622989    ,127.047508 ),
                        new LatLng(37.622947    ,127.047438 ),
                        new LatLng(37.622622    ,127.046899 ),
                        new LatLng(37.622603    ,127.046868 ),
                        new LatLng(37.622439    ,127.046665 ),
                        new LatLng(37.622355    ,127.04656  ),
                        new LatLng(37.622325    ,127.046528 ),
                        new LatLng(37.622117    ,127.046323 ),
                        new LatLng(37.622097    ,127.046303 ),
                        new LatLng(37.621921    ,127.046127 ),
                        new LatLng(37.621875    ,127.046081 ),
                        new LatLng(37.621601    ,127.045881 ),
                        new LatLng(37.621427    ,127.045752 ),
                        new LatLng(37.621363    ,127.045706 ),
                        new LatLng(37.621207    ,127.045622 ),
                        new LatLng(37.621154    ,127.045593 ),
                        new LatLng(37.621078    ,127.045552 ),
                        new LatLng(37.620447    ,127.045214 ),
                        new LatLng(37.620138    ,127.045049 ),
                        new LatLng(37.620098    ,127.045028 ),
                        new LatLng(37.619856    ,127.044899 ),
                        new LatLng(37.619493    ,127.044705 ),
                        new LatLng(37.619444    ,127.044679 ),
                        new LatLng(37.619326    ,127.044572 ),
                        new LatLng(37.619281    ,127.044531 ),
                        new LatLng(37.618743    ,127.044038 ),
                        new LatLng(37.618726    ,127.044023 ),
                        new LatLng(37.618655    ,127.043959 ),
                        new LatLng(37.618627    ,127.043924 ),
                        new LatLng(37.618501    ,127.043747 ),
                        new LatLng(37.618491    ,127.043732 ),
                        new LatLng(37.618418    ,127.043628 ),
                        new LatLng(37.618383    ,127.04358  ),
                        new LatLng(37.618348    ,127.043531 ),
                        new LatLng(37.618219    ,127.043348 ),
                        new LatLng(37.61818     ,127.043292 ),
                        new LatLng(37.61812     ,127.043209 ),
                        new LatLng(37.61807     ,127.043138 ),
                        new LatLng(37.61781     ,127.042772 ),
                        new LatLng(37.617754    ,127.042692 ),
                        new LatLng(37.617672    ,127.042577 ),
                        new LatLng(37.617614    ,127.042494 ),
                        new LatLng(37.617544    ,127.042397 ),
                        new LatLng(37.617348    ,127.042122 ),
                        new LatLng(37.617132    ,127.041834 ),
                        new LatLng(37.616913    ,127.041509 ),
                        new LatLng(37.616879    ,127.041442 ),
                        new LatLng(37.616869    ,127.041427 ),
                        new LatLng(37.616856    ,127.041409 ),
                        new LatLng(37.616841    ,127.041386 ),
                        new LatLng(37.616761    ,127.041273 ),
                        new LatLng(37.616689    ,127.041171 ),
                        new LatLng(37.616686    ,127.041165 ),
                        new LatLng(37.616571    ,127.040968 ),
                        new LatLng(37.616247    ,127.040627 ),
                        new LatLng(37.616139    ,127.040491 ),
                        new LatLng(37.616112    ,127.040411 ),
                        new LatLng(37.616217    ,127.040051 ),
                        new LatLng(37.61618     ,127.040027 ),
                        new LatLng(37.615716    ,127.040002 ),
                        new LatLng(37.615586    ,127.039901 ),
                        new LatLng(37.61558     ,127.039901 ),
                        new LatLng(37.615488    ,127.039894 ),
                        new LatLng(37.615433    ,127.039879 ),
                        new LatLng(37.615181    ,127.039669 ),
                        new LatLng(37.615145    ,127.039641 ),
                        new LatLng(37.615133    ,127.039623 ),
                        new LatLng(37.61511     ,127.03959  ),
                        new LatLng(37.614652    ,127.039085 ),
                        new LatLng(37.614336    ,127.038653 ),
                        new LatLng(37.614089    ,127.038298 ),
                        new LatLng(37.613994    ,127.038221 ),
                        new LatLng(37.613919    ,127.038092 ),
                        new LatLng(37.613628    ,127.037852 ),
                        new LatLng(37.613575    ,127.037821 ),
                        new LatLng(37.613251    ,127.037721 ),
                        new LatLng(37.613103    ,127.037619 ),
                        new LatLng(37.612917    ,127.037457 ),
                        new LatLng(37.612838    ,127.037356 ),
                        new LatLng(37.612826    ,127.037342 ),
                        new LatLng(37.612809    ,127.037322 ),
                        new LatLng(37.612801    ,127.037311 ),
                        new LatLng(37.612798    ,127.037307 ),
                        new LatLng(37.61279     ,127.037296 ),
                        new LatLng(37.612778    ,127.037279 ),
                        new LatLng(37.612642    ,127.037084 ),
                        new LatLng(37.612567    ,127.036979 ),
                        new LatLng(37.612425    ,127.036778 ),
                        new LatLng(37.612421    ,127.036766 ),
                        new LatLng(37.612358    ,127.036176 ),
                        new LatLng(37.612341    ,127.036096 ),
                        new LatLng(37.612428    ,127.036112 ),
                        new LatLng(37.612433    ,127.036049 ),
                        new LatLng(37.612411    ,127.036003 ),
                        new LatLng(37.612218    ,127.03588  ),
                        new LatLng(37.612084    ,127.035505 ),
                        new LatLng(37.611956    ,127.035335 ),
                        new LatLng(37.611509    ,127.034647 ),
                        new LatLng(37.610465    ,127.032929 ),
                        new LatLng(37.610329    ,127.03277  ),
                        new LatLng(37.609894    ,127.031962 ),
                        new LatLng(37.609802    ,127.031765 ),
                        new LatLng(37.608974    ,127.030259 ),
                        new LatLng(37.608979    ,127.03025  ),
                        new LatLng(37.6103      ,127.030303 ),
                        new LatLng(37.610385    ,127.030306 ),
                        new LatLng(37.610488    ,127.03031  ),
                        new LatLng(37.610606    ,127.030314 ),
                        new LatLng(37.610644    ,127.030316 ),
                        new LatLng(37.610701    ,127.030318 ),
                        new LatLng(37.611162    ,127.030338 ),
                        new LatLng(37.611264    ,127.030343 ),
                        new LatLng(37.611284    ,127.030344 ),
                        new LatLng(37.611297    ,127.030345 ),
                        new LatLng(37.611308    ,127.030344 ),
                        new LatLng(37.611482    ,127.030317 ),
                        new LatLng(37.612362    ,127.03018  ),
                        new LatLng(37.612366    ,127.029971 ),
                        new LatLng(37.612375    ,127.02978  ),
                        new LatLng(37.612374    ,127.029519 ),
                        new LatLng(37.612376    ,127.029447 ),
                        new LatLng(37.612378    ,127.02941  ),
                        new LatLng(37.612404    ,127.029101 ),
                        new LatLng(37.612569    ,127.027935 ),
                        new LatLng(37.612605    ,127.027709 ),
                        new LatLng(37.612586    ,127.027615 ),
                        new LatLng(37.612541    ,127.027472 ),
                        new LatLng(37.612568    ,127.02721  ),
                        new LatLng(37.612605    ,127.027065 ),
                        new LatLng(37.612629    ,127.026964 ),
                        new LatLng(37.612672    ,127.026866 ),
                        new LatLng(37.612678    ,127.026836 ),
                        new LatLng(37.612679    ,127.026829 ),
                        new LatLng(37.6127      ,127.026711 ),
                        new LatLng(37.612694    ,127.026434 ),
                        new LatLng(37.612552    ,127.026253 ),
                        new LatLng(37.612313    ,127.026075 ),
                        new LatLng(37.61223     ,127.02601  ),
                        new LatLng(37.612219    ,127.02598  ),
                        new LatLng(37.612132    ,127.025724 ),
                        new LatLng(37.612014    ,127.025181 ),
                        new LatLng(37.612029    ,127.024995 ),
                        new LatLng(37.612141    ,127.024565 ),
                        new LatLng(37.61208     ,127.02448  ),
                        new LatLng(37.612069    ,127.024406 ),
                        new LatLng(37.612       ,127.024073 ),
                        new LatLng(37.611734    ,127.023442 ),
                        new LatLng(37.611685    ,127.023292 ),
                        new LatLng(37.611513    ,127.022735 ),
                        new LatLng(37.611471    ,127.022668 ),
                        new LatLng(37.611401    ,127.022576 ),
                        new LatLng(37.611368    ,127.022504 ),
                        new LatLng(37.611368    ,127.022497 ),
                        new LatLng(37.611443    ,127.022416 ),
                        new LatLng(37.611399    ,127.022273 ),
                        new LatLng(37.611371    ,127.022198 ),
                        new LatLng(37.611381    ,127.022136 ),
                        new LatLng(37.611719    ,127.022056 ),
                        new LatLng(37.612352    ,127.021811 ),
                        new LatLng(37.612391    ,127.021692 ),
                        new LatLng(37.612406    ,127.021646 ),
                        new LatLng(37.612452    ,127.021128 ),
                        new LatLng(37.612511    ,127.020672 ),
                        new LatLng(37.612513    ,127.020666 ),
                        new LatLng(37.612523    ,127.020636 ),
                        new LatLng(37.612587    ,127.020432 ),
                        new LatLng(37.612598    ,127.020397 ),
                        new LatLng(37.612654    ,127.020384 ),
                        new LatLng(37.612859    ,127.020338 ),
                        new LatLng(37.613115    ,127.019948 ),
                        new LatLng(37.613856    ,127.018545 ),
                        new LatLng(37.613856    ,127.01854  ),
                        new LatLng(37.613842    ,127.018521 ),
                        new LatLng(37.613999    ,127.018145 ),
                        new LatLng(37.614371    ,127.017816 ),
                        new LatLng(37.614387    ,127.017818 ),
                        new LatLng(37.614391    ,127.017818 ),
                        new LatLng(37.614431    ,127.017777 ),
                        new LatLng(37.61456     ,127.017637 ),
                        new LatLng(37.614788    ,127.017097 ),
                        new LatLng(37.614778    ,127.017082 ),
                        new LatLng(37.614819    ,127.016907 ),
                        new LatLng(37.614869    ,127.016798 ),
                        new LatLng(37.614883    ,127.016672 ),
                        new LatLng(37.614942    ,127.016534 ),
                        new LatLng(37.614941    ,127.016521 ),
                        new LatLng(37.614862    ,127.016114 ),
                        new LatLng(37.614675    ,127.015254 ),
                        new LatLng(37.614868    ,127.014199 ),
                        new LatLng(37.614978    ,127.014186 ),
                        new LatLng(37.615535    ,127.013399 ),
                        new LatLng(37.615752    ,127.01269  ),
                        new LatLng(37.61585     ,127.012453 ),
                        new LatLng(37.616421    ,127.011441 ),
                        new LatLng(37.616313    ,127.01103  ),
                        new LatLng(37.616287    ,127.010949 ),
                        new LatLng(37.616288    ,127.010938 ),
                        new LatLng(37.616763    ,127.01026  ),
                        new LatLng(37.616893    ,127.010115 ),
                        new LatLng(37.616944    ,127.010139 ),
                        new LatLng(37.617275    ,127.010004 ),
                        new LatLng(37.617783    ,127.009948 ),
                        new LatLng(37.618253    ,127.009363 ),
                        new LatLng(37.618342    ,127.008998 ),
                        new LatLng(37.618433    ,127.008868 ),
                        new LatLng(37.618672    ,127.008553 ),
                        new LatLng(37.618896    ,127.008494 ),
                        new LatLng(37.619112    ,127.008405 ),
                        new LatLng(37.619319    ,127.008167 ),
                        new LatLng(37.619631    ,127.007752 ),
                        new LatLng(37.619638    ,127.00775  ),
                        new LatLng(37.61986     ,127.007864 ),
                        new LatLng(37.619983    ,127.007634 ),
                        new LatLng(37.62001     ,127.0076   ),
                        new LatLng(37.620044    ,127.007571 ),
                        new LatLng(37.620235    ,127.007416 ),
                        new LatLng(37.620512    ,127.007214 ),
                        new LatLng(37.620582    ,127.007258 ),
                        new LatLng(37.620716    ,127.00731  ),
                        new LatLng(37.620786    ,127.007249 ),
                        new LatLng(37.620828    ,127.00721  ),
                        new LatLng(37.620954    ,127.00724  ),
                        new LatLng(37.62104     ,127.007322 ),
                        new LatLng(37.621471    ,127.007577 ),
                        new LatLng(37.621496    ,127.007582 ),
                        new LatLng(37.621595    ,127.007527 ),
                        new LatLng(37.621653    ,127.007495 ),
                        new LatLng(37.621754    ,127.007445 ),
                        new LatLng(37.622425    ,127.007596 ),
                        new LatLng(37.622656    ,127.007544 ),
                        new LatLng(37.622691    ,127.007554 ),
                        new LatLng(37.622939    ,127.007786 ),
                        new LatLng(37.623348    ,127.007744 ),
                        new LatLng(37.6237      ,127.007888 ),
                        new LatLng(37.623985    ,127.007896 ),
                        new LatLng(37.624022    ,127.007856 ),
                        new LatLng(37.624132    ,127.00733  ),
                        new LatLng(37.624033    ,127.006334 ),
                        new LatLng(37.624013    ,127.006097 ),
                        new LatLng(37.624008    ,127.006029 ),
                        new LatLng(37.623994    ,127.005866 ),
                        new LatLng(37.623987    ,127.005785 ),
                        new LatLng(37.623982    ,127.005714 ),
                        new LatLng(37.623848    ,127.005583 ),
                        new LatLng(37.623846    ,127.005565 ),
                        new LatLng(37.62393     ,127.005065 ),
                        new LatLng(37.623823    ,127.004897 ),
                        new LatLng(37.623824    ,127.00488  ),
                        new LatLng(37.624011    ,127.004647 ),
                        new LatLng(37.624079    ,127.004562 ),
                        new LatLng(37.624055    ,127.004433 ),
                        new LatLng(37.624226    ,127.003887 ),
                        new LatLng(37.625272    ,127.002977 ),
                        new LatLng(37.625645    ,127.001762 ),
                        new LatLng(37.62604     ,126.999849 ),
                        new LatLng(37.626536    ,126.998993 ),
                        new LatLng(37.627841    ,126.997694 ),
                        new LatLng(37.628004    ,126.997595 ),
                        new LatLng(37.629067    ,126.996517 ),
                        new LatLng(37.62909     ,126.9965   ),
                        new LatLng(37.629347    ,126.996041 ),
                        new LatLng(37.630611    ,126.994607 ),
                        new LatLng(37.630847    ,126.994257 ),
                        new LatLng(37.630859    ,126.994243 ),
                        new LatLng(37.631108    ,126.994112 ),
                        new LatLng(37.631448    ,126.993722 ),
                        new LatLng(37.632605    ,126.99138  ),
                        new LatLng(37.632666    ,126.990707 ),
                        new LatLng(37.632799    ,126.989934 ),
                        new LatLng(37.632802    ,126.989931 ),
                        new LatLng(37.632995    ,126.989903 ),
                        new LatLng(37.633154    ,126.989883 ),
                        new LatLng(37.633961    ,126.989488 ),
                        new LatLng(37.633792    ,126.988572 ),
                        new LatLng(37.634193    ,126.988391 ),
                        new LatLng(37.634489    ,126.988179 ),
                        new LatLng(37.63546     ,126.986362 ),
                        new LatLng(37.635739    ,126.985983 ),
                        new LatLng(37.635772    ,126.98594  ),
                        new LatLng(37.635789    ,126.985918 ),
                        new LatLng(37.635796    ,126.985896 ),
                        new LatLng(37.635797    ,126.985894 ),
                        new LatLng(37.63597     ,126.985378 ),
                        new LatLng(37.63627     ,126.984414 ),
                        new LatLng(37.636335    ,126.984206 ),
                        new LatLng(37.636336    ,126.984204 ),
                        new LatLng(37.636336    ,126.984203 ),
                        new LatLng(37.636337    ,126.984201 ),
                        new LatLng(37.63634     ,126.984204 ),
                        new LatLng(37.636508    ,126.984412 ),
                        new LatLng(37.636815    ,126.984856 ),
                        new LatLng(37.637165    ,126.984937 ),
                        new LatLng(37.637529    ,126.985045 ),
                        new LatLng(37.638066    ,126.985234 ),
                        new LatLng(37.638418    ,126.985294 ),
                        new LatLng(37.639088    ,126.985408 ),
                        new LatLng(37.640193    ,126.985897 ),
                        new LatLng(37.640428    ,126.986133 ),
                        new LatLng(37.64135     ,126.985268 ),
                        new LatLng(37.641636    ,126.985224 ),
                        new LatLng(37.641645    ,126.984257 ),
                        new LatLng(37.642149    ,126.983831 ),
                        new LatLng(37.642365    ,126.983854 ),
                        new LatLng(37.643039    ,126.983691 ),
                        new LatLng(37.643683    ,126.983247 ),
                        new LatLng(37.643804    ,126.983352 ),
                        new LatLng(37.64405     ,126.983726 ),
                        new LatLng(37.644428    ,126.98419  ),
                        new LatLng(37.645338    ,126.984643 ),
                        new LatLng(37.645446    ,126.984795 ),
                        new LatLng(37.645947    ,126.985413 ),
                        new LatLng(37.646056    ,126.985723 ),
                        new LatLng(37.645929    ,126.985101 ),
                        new LatLng(37.646067    ,126.984929 ),
                        new LatLng(37.64961     ,126.983937 ),
                        new LatLng(37.651254    ,126.982298 ),
                        new LatLng(37.651688    ,126.981874 ),
                        new LatLng(37.652713    ,126.981048 ),
                        new LatLng(37.654815    ,126.979944 ),
                        new LatLng(37.656039    ,126.979658 ),
                        new LatLng(37.656488    ,126.981194 ),
                        new LatLng(37.66117     ,126.987284 ),
                        new LatLng(37.662732    ,126.987751 ),
                        new LatLng(37.663989    ,126.988033 ),
                        new LatLng(37.66453     ,126.989453 ),
                        new LatLng(37.66619     ,126.993261 ),
                        new LatLng(37.66703     ,126.994129 ),
                        new LatLng(37.66802     ,126.993629 ),
                        new LatLng(37.669811    ,126.994351 ),
                        new LatLng(37.672761    ,126.994074 ),
                        new LatLng(37.674226    ,126.993796 ),
                        new LatLng(37.674801    ,126.993958 ),
                        new LatLng(37.675252    ,126.99381  ),
                        new LatLng(37.675832    ,126.993125 ),
                        new LatLng(37.677225    ,126.99312  ),
                        new LatLng(37.679148    ,126.992433 ),
                        new LatLng(37.679627    ,126.992198 ),
                        new LatLng(37.679759    ,126.992433 ),
                        new LatLng(37.680192    ,126.993742 ),
                        new LatLng(37.680324    ,126.994076 ),
                        new LatLng(37.680569    ,126.994156 ),
                        new LatLng(37.680588    ,126.994172 ),
                        new LatLng(37.681869    ,126.995853 ),
                        new LatLng(37.682204    ,126.996351 ),
                        new LatLng(37.682646    ,126.996971 ),
                        new LatLng(37.682679    ,126.996975 ),
                        new LatLng(37.683114    ,126.997027 ),
                        new LatLng(37.683586    ,126.997754 ),
                        new LatLng(37.683842    ,126.999112 ),
                        new LatLng(37.683967    ,127.000402 ),
                        new LatLng(37.684235    ,127.000973 ),
                        new LatLng(37.684344    ,127.001687 ),
                        new LatLng(37.684239    ,127.003584 ),
                        new LatLng(37.684824    ,127.003777 ),
                        new LatLng(37.684941    ,127.004277 ),
                        new LatLng(37.685089    ,127.004527 ),
                        new LatLng(37.68508     ,127.004567 ),
                        new LatLng(37.685054    ,127.006051 ),
                        new LatLng(37.68445     ,127.008664 ),
                        new LatLng(37.679591    ,127.009383 ),
                        new LatLng(37.670855    ,127.018707 ),
                        new LatLng(37.650747    ,127.012905 ),
                        new LatLng(37.647343    ,127.024682 ),
                        new LatLng(37.631784    ,127.040438 ),
                        new LatLng(37.631548    ,127.040799 ),
                        new LatLng(37.630676    ,127.042086 ),
                        new LatLng(37.630508    ,127.04239  ),
                        new LatLng(37.630451    ,127.042599 ),
                        new LatLng(37.629368    ,127.043801 ),
                        new LatLng(37.628417    ,127.045267 ),
                        new LatLng(37.627522    ,127.047057 ),
                        new LatLng(37.626287    ,127.048188 ),
                        new LatLng(37.625839    ,127.048651 ),
                        new LatLng(37.625415    ,127.048963 ),
                        new LatLng(37.624855    ,127.049273 ),
                        new LatLng(37.624335    ,127.049659 ),
                        new LatLng(37.624266    ,127.049737 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"강북구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "강북구\n   "+hmap.get(name), new LatLng(37.644060, 127.011241));
    }//서울 강북구
    public void drawPolygon166(GoogleMap googlemap) { //서울 관악구
        String name = "관악구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.458166    ,126.988636 ),
                        new LatLng(37.454413    ,126.97458  ),
                        new LatLng(37.446267    ,126.96429  ),
                        new LatLng(37.440787    ,126.963803 ),
                        new LatLng(37.440377    ,126.963114 ),
                        new LatLng(37.44028     ,126.962943 ),
                        new LatLng(37.440279    ,126.961763 ),
                        new LatLng(37.440299    ,126.9615   ),
                        new LatLng(37.440356    ,126.96     ),
                        new LatLng(37.440005    ,126.959571 ),
                        new LatLng(37.43991     ,126.959497 ),
                        new LatLng(37.439407    ,126.959169 ),
                        new LatLng(37.43907     ,126.958966 ),
                        new LatLng(37.43893     ,126.957567 ),
                        new LatLng(37.438803    ,126.956705 ),
                        new LatLng(37.43901     ,126.953904 ),
                        new LatLng(37.439095    ,126.953167 ),
                        new LatLng(37.43919     ,126.952411 ),
                        new LatLng(37.438976    ,126.952072 ),
                        new LatLng(37.438734    ,126.950925 ),
                        new LatLng(37.43838     ,126.949843 ),
                        new LatLng(37.438281    ,126.949513 ),
                        new LatLng(37.43825     ,126.949284 ),
                        new LatLng(37.438329    ,126.949189 ),
                        new LatLng(37.438344    ,126.949161 ),
                        new LatLng(37.438507    ,126.948945 ),
                        new LatLng(37.438646    ,126.948766 ),
                        new LatLng(37.438713    ,126.948363 ),
                        new LatLng(37.43869     ,126.948289 ),
                        new LatLng(37.43863     ,126.948155 ),
                        new LatLng(37.438576    ,126.948034 ),
                        new LatLng(37.438492    ,126.947854 ),
                        new LatLng(37.438412    ,126.947599 ),
                        new LatLng(37.438167    ,126.946789 ),
                        new LatLng(37.437305    ,126.945676 ),
                        new LatLng(37.437088    ,126.94509  ),
                        new LatLng(37.437506    ,126.94198  ),
                        new LatLng(37.437404    ,126.941424 ),
                        new LatLng(37.436918    ,126.941103 ),
                        new LatLng(37.436479    ,126.940708 ),
                        new LatLng(37.43628     ,126.94053  ),
                        new LatLng(37.436123    ,126.940503 ),
                        new LatLng(37.435982    ,126.940472 ),
                        new LatLng(37.435802    ,126.940427 ),
                        new LatLng(37.435712    ,126.94022  ),
                        new LatLng(37.436096    ,126.938615 ),
                        new LatLng(37.43642     ,126.938575 ),
                        new LatLng(37.437184    ,126.938015 ),
                        new LatLng(37.437209    ,126.937989 ),
                        new LatLng(37.437382    ,126.937752 ),
                        new LatLng(37.437392    ,126.937751 ),
                        new LatLng(37.437582    ,126.937742 ),
                        new LatLng(37.437879    ,126.937744 ),
                        new LatLng(37.438094    ,126.937656 ),
                        new LatLng(37.438327    ,126.937563 ),
                        new LatLng(37.438632    ,126.937273 ),
                        new LatLng(37.439866    ,126.937758 ),
                        new LatLng(37.440198    ,126.937864 ),
                        new LatLng(37.441299    ,126.936949 ),
                        new LatLng(37.441438    ,126.936812 ),
                        new LatLng(37.441954    ,126.936202 ),
                        new LatLng(37.442142    ,126.935986 ),
                        new LatLng(37.442178    ,126.935643 ),
                        new LatLng(37.442497    ,126.935441 ),
                        new LatLng(37.442898    ,126.935126 ),
                        new LatLng(37.442928    ,126.93511  ),
                        new LatLng(37.443209    ,126.934898 ),
                        new LatLng(37.443213    ,126.934894 ),
                        new LatLng(37.443258    ,126.934451 ),
                        new LatLng(37.443896    ,126.933469 ),
                        new LatLng(37.445136    ,126.931326 ),
                        new LatLng(37.445466    ,126.93056  ),
                        new LatLng(37.445468    ,126.930554 ),
                        new LatLng(37.445818    ,126.930394 ),
                        new LatLng(37.446142    ,126.930285 ),
                        new LatLng(37.446153    ,126.930282 ),
                        new LatLng(37.446378    ,126.930163 ),
                        new LatLng(37.446654    ,126.930266 ),
                        new LatLng(37.447393    ,126.930365 ),
                        new LatLng(37.448544    ,126.930047 ),
                        new LatLng(37.448881    ,126.929786 ),
                        new LatLng(37.449093    ,126.929607 ),
                        new LatLng(37.449472    ,126.929286 ),
                        new LatLng(37.450201    ,126.928416 ),
                        new LatLng(37.450213    ,126.928399 ),
                        new LatLng(37.450733    ,126.928469 ),
                        new LatLng(37.450811    ,126.928453 ),
                        new LatLng(37.456575    ,126.922251 ),
                        new LatLng(37.456627    ,126.922187 ),
                        new LatLng(37.456763    ,126.921319 ),
                        new LatLng(37.456829    ,126.920883 ),
                        new LatLng(37.457386    ,126.916416 ),
                        new LatLng(37.457342    ,126.916261 ),
                        new LatLng(37.45789     ,126.914026 ),
                        new LatLng(37.465116    ,126.913533 ),
                        new LatLng(37.465379    ,126.913687 ),
                        new LatLng(37.468787    ,126.909969 ),
                        new LatLng(37.468915    ,126.910045 ),
                        new LatLng(37.472682    ,126.9082   ),
                        new LatLng(37.472729    ,126.908194 ),
                        new LatLng(37.474755    ,126.911302 ),
                        new LatLng(37.474785    ,126.911301 ),
                        new LatLng(37.477806    ,126.911823 ),
                        new LatLng(37.478018    ,126.909477 ),
                        new LatLng(37.478028    ,126.909461 ),
                        new LatLng(37.479379    ,126.908698 ),
                        new LatLng(37.479384    ,126.908697 ),
                        new LatLng(37.480766    ,126.909786 ),
                        new LatLng(37.480113    ,126.905239 ),
                        new LatLng(37.479855    ,126.903834 ),
                        new LatLng(37.479149    ,126.89899  ),
                        new LatLng(37.484977    ,126.903177 ),
                        new LatLng(37.485002    ,126.903198 ),
                        new LatLng(37.485003    ,126.903199 ),
                        new LatLng(37.486748    ,126.912273 ),
                        new LatLng(37.486774    ,126.912312 ),
                        new LatLng(37.486892    ,126.912417 ),
                        new LatLng(37.48693     ,126.912451 ),
                        new LatLng(37.487398    ,126.913079 ),
                        new LatLng(37.48753     ,126.91319  ),
                        new LatLng(37.488496    ,126.914489 ),
                        new LatLng(37.488672    ,126.914831 ),
                        new LatLng(37.490143    ,126.918708 ),
                        new LatLng(37.490202    ,126.919007 ),
                        new LatLng(37.490168    ,126.922115 ),
                        new LatLng(37.490154    ,126.922162 ),
                        new LatLng(37.490841    ,126.924765 ),
                        new LatLng(37.490857    ,126.924779 ),
                        new LatLng(37.491655    ,126.925114 ),
                        new LatLng(37.491664    ,126.92512  ),
                        new LatLng(37.493182    ,126.926101 ),
                        new LatLng(37.493199    ,126.926138 ),
                        new LatLng(37.493256    ,126.926188 ),
                        new LatLng(37.493274    ,126.926223 ),
                        new LatLng(37.495028    ,126.927672 ),
                        new LatLng(37.495051    ,126.927781 ),
                        new LatLng(37.495071    ,126.92788  ),
                        new LatLng(37.494824    ,126.928478 ),
                        new LatLng(37.494816    ,126.928491 ),
                        new LatLng(37.493993    ,126.929863 ),
                        new LatLng(37.493988    ,126.930073 ),
                        new LatLng(37.493262    ,126.931341 ),
                        new LatLng(37.493226    ,126.931347 ),
                        new LatLng(37.492973    ,126.9324   ),
                        new LatLng(37.492923    ,126.932442 ),
                        new LatLng(37.49224     ,126.939138 ),
                        new LatLng(37.492207    ,126.939235 ),
                        new LatLng(37.492471    ,126.93949  ),
                        new LatLng(37.492557    ,126.939565 ),
                        new LatLng(37.492642    ,126.939725 ),
                        new LatLng(37.492784    ,126.939735 ),
                        new LatLng(37.492791    ,126.939739 ),
                        new LatLng(37.492894    ,126.939948 ),
                        new LatLng(37.492383    ,126.94366  ),
                        new LatLng(37.492449    ,126.943826 ),
                        new LatLng(37.493079    ,126.944463 ),
                        new LatLng(37.493097    ,126.94453  ),
                        new LatLng(37.494083    ,126.946974 ),
                        new LatLng(37.494005    ,126.947171 ),
                        new LatLng(37.490626    ,126.953715 ),
                        new LatLng(37.490753    ,126.953904 ),
                        new LatLng(37.493803    ,126.959338 ),
                        new LatLng(37.491554    ,126.961399 ),
                        new LatLng(37.491492    ,126.9614   ),
                        new LatLng(37.490831    ,126.960782 ),
                        new LatLng(37.490822    ,126.960781 ),
                        new LatLng(37.485412    ,126.961895 ),
                        new LatLng(37.485176    ,126.961986 ),
                        new LatLng(37.483561    ,126.961075 ),
                        new LatLng(37.477836    ,126.967782 ),
                        new LatLng(37.477495    ,126.968045 ),
                        new LatLng(37.475377    ,126.970523 ),
                        new LatLng(37.476531    ,126.981691 ),
                        new LatLng(37.466627    ,126.987603 ),
                        new LatLng(37.466593    ,126.987623 ),
                        new LatLng(37.466426    ,126.987719 ),
                        new LatLng(37.466411    ,126.987728 ),
                        new LatLng(37.458166    ,126.988636 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"관악구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "관악구\n   "+hmap.get(name), new LatLng(37.467836, 126.945449));
    }//서울 관악구
    public void drawPolygon148(GoogleMap googlemap) { //서울 금천구
        String name = "금천구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.450733    ,126.928469 ),
                        new LatLng(37.450213    ,126.928399 ),
                        new LatLng(37.450212    ,126.928399 ),
                        new LatLng(37.449899    ,126.928357 ),
                        new LatLng(37.449549    ,126.928308 ),
                        new LatLng(37.449336    ,126.928256 ),
                        new LatLng(37.448391    ,126.926518 ),
                        new LatLng(37.448158    ,126.926354 ),
                        new LatLng(37.447206    ,126.925463 ),
                        new LatLng(37.445773    ,126.923253 ),
                        new LatLng(37.445173    ,126.922897 ),
                        new LatLng(37.444673    ,126.922903 ),
                        new LatLng(37.44356     ,126.922247 ),
                        new LatLng(37.443251    ,126.922007 ),
                        new LatLng(37.441428    ,126.920668 ),
                        new LatLng(37.440985    ,126.920497 ),
                        new LatLng(37.440471    ,126.920279 ),
                        new LatLng(37.440285    ,126.919977 ),
                        new LatLng(37.440045    ,126.916109 ),
                        new LatLng(37.439645    ,126.915346 ),
                        new LatLng(37.439401    ,126.914873 ),
                        new LatLng(37.439198    ,126.913536 ),
                        new LatLng(37.437187    ,126.9112   ),
                        new LatLng(37.435676    ,126.910852 ),
                        new LatLng(37.434614    ,126.910141 ),
                        new LatLng(37.434282    ,126.90997  ),
                        new LatLng(37.434227    ,126.909904 ),
                        new LatLng(37.433865    ,126.909406 ),
                        new LatLng(37.43386     ,126.909396 ),
                        new LatLng(37.433684    ,126.90856  ),
                        new LatLng(37.433682    ,126.908549 ),
                        new LatLng(37.433694    ,126.908399 ),
                        new LatLng(37.433725    ,126.907934 ),
                        new LatLng(37.43362     ,126.907527 ),
                        new LatLng(37.433518    ,126.907254 ),
                        new LatLng(37.433547    ,126.907089 ),
                        new LatLng(37.433695    ,126.906935 ),
                        new LatLng(37.433699    ,126.906927 ),
                        new LatLng(37.433673    ,126.906748 ),
                        new LatLng(37.433757    ,126.906498 ),
                        new LatLng(37.433966    ,126.906137 ),
                        new LatLng(37.433996    ,126.905844 ),
                        new LatLng(37.433998    ,126.905391 ),
                        new LatLng(37.433997    ,126.905225 ),
                        new LatLng(37.434163    ,126.903472 ),
                        new LatLng(37.434112    ,126.903386 ),
                        new LatLng(37.434068    ,126.902988 ),
                        new LatLng(37.434081    ,126.902989 ),
                        new LatLng(37.434187    ,126.902943 ),
                        new LatLng(37.434232    ,126.902926 ),
                        new LatLng(37.434614    ,126.902822 ),
                        new LatLng(37.434776    ,126.902789 ),
                        new LatLng(37.434892    ,126.902784 ),
                        new LatLng(37.435457    ,126.902801 ),
                        new LatLng(37.435642    ,126.902803 ),
                        new LatLng(37.435642    ,126.90276  ),
                        new LatLng(37.435857    ,126.90275  ),
                        new LatLng(37.435878    ,126.902706 ),
                        new LatLng(37.435963    ,126.902586 ),
                        new LatLng(37.436009    ,126.902484 ),
                        new LatLng(37.436024    ,126.902464 ),
                        new LatLng(37.436284    ,126.902023 ),
                        new LatLng(37.436387    ,126.901879 ),
                        new LatLng(37.436714    ,126.901388 ),
                        new LatLng(37.436984    ,126.900849 ),
                        new LatLng(37.437323    ,126.900547 ),
                        new LatLng(37.43742     ,126.900508 ),
                        new LatLng(37.437733    ,126.900337 ),
                        new LatLng(37.437826    ,126.900211 ),
                        new LatLng(37.438052    ,126.899884 ),
                        new LatLng(37.438351    ,126.899512 ),
                        new LatLng(37.438504    ,126.899327 ),
                        new LatLng(37.438631    ,126.899185 ),
                        new LatLng(37.438686    ,126.898999 ),
                        new LatLng(37.438702    ,126.898978 ),
                        new LatLng(37.452717    ,126.893983 ),
                        new LatLng(37.452315    ,126.889642 ),
                        new LatLng(37.455568    ,126.88786  ),
                        new LatLng(37.455537    ,126.88766  ),
                        new LatLng(37.459863    ,126.88535  ),
                        new LatLng(37.460947    ,126.888876 ),
                        new LatLng(37.464391    ,126.882746 ),
                        new LatLng(37.466012    ,126.884618 ),
                        new LatLng(37.482425    ,126.872744 ),
                        new LatLng(37.485269    ,126.871763 ),
                        new LatLng(37.485367    ,126.874556 ),
                        new LatLng(37.486662    ,126.878463 ),
                        new LatLng(37.484046    ,126.883715 ),
                        new LatLng(37.483888    ,126.88394  ),
                        new LatLng(37.47979     ,126.888785 ),
                        new LatLng(37.478731    ,126.89666  ),
                        new LatLng(37.478811    ,126.897112 ),
                        new LatLng(37.479149    ,126.89899  ),
                        new LatLng(37.479855    ,126.903834 ),
                        new LatLng(37.480113    ,126.905239 ),
                        new LatLng(37.480766    ,126.909786 ),
                        new LatLng(37.479384    ,126.908697 ),
                        new LatLng(37.479379    ,126.908698 ),
                        new LatLng(37.478028    ,126.909461 ),
                        new LatLng(37.478018    ,126.909477 ),
                        new LatLng(37.477806    ,126.911823 ),
                        new LatLng(37.474785    ,126.911301 ),
                        new LatLng(37.474755    ,126.911302 ),
                        new LatLng(37.472729    ,126.908194 ),
                        new LatLng(37.472682    ,126.9082   ),
                        new LatLng(37.468915    ,126.910045 ),
                        new LatLng(37.468787    ,126.909969 ),
                        new LatLng(37.465379    ,126.913687 ),
                        new LatLng(37.465116    ,126.913533 ),
                        new LatLng(37.45789     ,126.914026 ),
                        new LatLng(37.457342    ,126.916261 ),
                        new LatLng(37.457386    ,126.916416 ),
                        new LatLng(37.456829    ,126.920883 ),
                        new LatLng(37.456763    ,126.921319 ),
                        new LatLng(37.456627    ,126.922187 ),
                        new LatLng(37.456575    ,126.922251 ),
                        new LatLng(37.450811    ,126.928453 ),
                        new LatLng(37.450733    ,126.928469 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"금천구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "금천구\n   "+hmap.get(name), new LatLng(37.461215, 126.900751));
    }//서울 금천구
    public void drawPolygon142(GoogleMap googlemap) { //서울 강서구
        String name = "강서구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.548088    ,126.880792),
                        new LatLng(37.547072    ,126.872644),
                        new LatLng(37.551152    ,126.864208),
                        new LatLng(37.544356    ,126.862115),
                        new LatLng(37.529789    ,126.863973),
                        new LatLng(37.526484    ,126.840487),
                        new LatLng(37.541849    ,126.833103),
                        new LatLng(37.541656    ,126.829985),
                        new LatLng(37.547732    ,126.829759),
                        new LatLng(37.540678    ,126.822121),
                        new LatLng(37.540737    ,126.812334),
                        new LatLng(37.544408    ,126.806241),
                        new LatLng(37.535829    ,126.794425),
                        new LatLng(37.541372    ,126.794944),
                        new LatLng(37.543746    ,126.791819),
                        new LatLng(37.548318    ,126.771552),
                        new LatLng(37.554242    ,126.766204),
                        new LatLng(37.556917    ,126.766284),
                        new LatLng(37.556985    ,126.772381),
                        new LatLng(37.561898    ,126.775636),
                        new LatLng(37.559933    ,126.777842),
                        new LatLng(37.567708    ,126.774762),
                        new LatLng(37.566051    ,126.776723),
                        new LatLng(37.567504    ,126.780318),
                        new LatLng(37.573606    ,126.782408),
                        new LatLng(37.575581    ,126.789202),
                        new LatLng(37.581414    ,126.791349),
                        new LatLng(37.576894    ,126.793219),
                        new LatLng(37.581594    ,126.793695),
                        new LatLng(37.58452     ,126.793441),
                        new LatLng(37.582622    ,126.794698),
                        new LatLng(37.585155    ,126.79554),
                        new LatLng(37.583121    ,126.795722),
                        new LatLng(37.588133    ,126.798856),
                        new LatLng(37.58817     ,126.799022),
                        new LatLng(37.589349    ,126.801227),
                        new LatLng(37.596712    ,126.797231),
                        new LatLng(37.596898    ,126.797204),
                        new LatLng(37.597663    ,126.797092),
                        new LatLng(37.597743    ,126.797115),
                        new LatLng(37.605033    ,126.802581),
                        new LatLng(37.593716    ,126.818683),
                        new LatLng(37.59285     ,126.819303),
                        new LatLng(37.57232     ,126.852925),
                        new LatLng(37.571896    ,126.853522),
                        new LatLng(37.57179     ,126.853632),
                        new LatLng(37.556269    ,126.880464),
                        new LatLng(37.55317     ,126.877952),
                        new LatLng(37.553036    ,126.878005),
                        new LatLng(37.550412    ,126.879124),
                        new LatLng(37.55031     ,126.879213),
                        new LatLng(37.548088    ,126.880792)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"강서구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "강서구\n   "+hmap.get(name), new LatLng(37.562385, 126.823067));
    }//서울 강서구
    public void drawPolygon162(GoogleMap googlemap) { //서울 양천구
        String name = "양천구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.531656    ,126.890684),
                        new LatLng(37.531396    ,126.89047),
                        new LatLng(37.531361    ,126.890446),
                        new LatLng(37.531102    ,126.890262),
                        new LatLng(37.53109     ,126.890253),
                        new LatLng(37.531053    ,126.890227),
                        new LatLng(37.530788    ,126.890041),
                        new LatLng(37.530689    ,126.889972),
                        new LatLng(37.530047    ,126.889488),
                        new LatLng(37.530099    ,126.889437),
                        new LatLng(37.530118    ,126.889412),
                        new LatLng(37.530396    ,126.887924),
                        new LatLng(37.530366    ,126.887685),
                        new LatLng(37.530195    ,126.88719),
                        new LatLng(37.530185    ,126.887166),
                        new LatLng(37.530157    ,126.88711),
                        new LatLng(37.530154    ,126.887102),
                        new LatLng(37.530135    ,126.887071),
                        new LatLng(37.529577    ,126.886239),
                        new LatLng(37.529568    ,126.886224),
                        new LatLng(37.529554    ,126.886202),
                        new LatLng(37.529422    ,126.885944),
                        new LatLng(37.529404    ,126.885886),
                        new LatLng(37.529399    ,126.88587),
                        new LatLng(37.529382    ,126.885817),
                        new LatLng(37.529183    ,126.885224),
                        new LatLng(37.529166    ,126.885181),
                        new LatLng(37.529123    ,126.885083),
                        new LatLng(37.529103    ,126.885041),
                        new LatLng(37.529091    ,126.885015),
                        new LatLng(37.529075    ,126.884983),
                        new LatLng(37.528928    ,126.884695),
                        new LatLng(37.528837    ,126.884521),
                        new LatLng(37.528831    ,126.884504),
                        new LatLng(37.528827    ,126.884493),
                        new LatLng(37.528813    ,126.884456),
                        new LatLng(37.5288      ,126.88442),
                        new LatLng(37.528759    ,126.884332),
                        new LatLng(37.528744    ,126.884303),
                        new LatLng(37.528737    ,126.884291),
                        new LatLng(37.528663    ,126.88417),
                        new LatLng(37.528641    ,126.884144),
                        new LatLng(37.528551    ,126.884033),
                        new LatLng(37.528545    ,126.884027),
                        new LatLng(37.528537    ,126.884018),
                        new LatLng(37.528508    ,126.883996),
                        new LatLng(37.528453    ,126.883952),
                        new LatLng(37.528437    ,126.88394),
                        new LatLng(37.52841     ,126.883919),
                        new LatLng(37.528406    ,126.883917),
                        new LatLng(37.52807     ,126.883896),
                        new LatLng(37.528045    ,126.883895),
                        new LatLng(37.527987    ,126.883889),
                        new LatLng(37.52782     ,126.883855),
                        new LatLng(37.5278      ,126.883846),
                        new LatLng(37.527587    ,126.883747),
                        new LatLng(37.527581    ,126.883743),
                        new LatLng(37.527558    ,126.883728),
                        new LatLng(37.527501    ,126.883675),
                        new LatLng(37.527475    ,126.883651),
                        new LatLng(37.527268    ,126.883283),
                        new LatLng(37.52725     ,126.883222),
                        new LatLng(37.527229    ,126.882907),
                        new LatLng(37.527249    ,126.882727),
                        new LatLng(37.527318    ,126.882579),
                        new LatLng(37.527327    ,126.88256),
                        new LatLng(37.527668    ,126.881763),
                        new LatLng(37.527688    ,126.881687),
                        new LatLng(37.527707    ,126.880906),
                        new LatLng(37.527648    ,126.880804),
                        new LatLng(37.5276      ,126.880739),
                        new LatLng(37.527562    ,126.880687),
                        new LatLng(37.527551    ,126.880675),
                        new LatLng(37.527418    ,126.880604),
                        new LatLng(37.527401    ,126.880603),
                        new LatLng(37.527319    ,126.88061),
                        new LatLng(37.527267    ,126.880621),
                        new LatLng(37.527241    ,126.880629),
                        new LatLng(37.52667     ,126.88107),
                        new LatLng(37.526543    ,126.881167),
                        new LatLng(37.526536    ,126.881169),
                        new LatLng(37.526514    ,126.881175),
                        new LatLng(37.526501    ,126.881175),
                        new LatLng(37.526483    ,126.881177),
                        new LatLng(37.526204    ,126.881148),
                        new LatLng(37.526185    ,126.881141),
                        new LatLng(37.526173    ,126.881138),
                        new LatLng(37.526161    ,126.881134),
                        new LatLng(37.526155    ,126.881132),
                        new LatLng(37.526145    ,126.881128),
                        new LatLng(37.526142    ,126.881127),
                        new LatLng(37.526134    ,126.881123),
                        new LatLng(37.526114    ,126.881108),
                        new LatLng(37.525983    ,126.881),
                        new LatLng(37.525974    ,126.880993),
                        new LatLng(37.525912    ,126.880942),
                        new LatLng(37.525908    ,126.880938),
                        new LatLng(37.525905    ,126.880933),
                        new LatLng(37.525835    ,126.88081),
                        new LatLng(37.52578     ,126.880709),
                        new LatLng(37.52575     ,126.880576),
                        new LatLng(37.525741    ,126.880486),
                        new LatLng(37.52574     ,126.880473),
                        new LatLng(37.525739    ,126.880446),
                        new LatLng(37.525739    ,126.880402),
                        new LatLng(37.525737    ,126.880325),
                        new LatLng(37.525738    ,126.880238),
                        new LatLng(37.525737    ,126.879407),
                        new LatLng(37.52573     ,126.879361),
                        new LatLng(37.525725    ,126.879329),
                        new LatLng(37.525716    ,126.879285),
                        new LatLng(37.525676    ,126.879187),
                        new LatLng(37.525658    ,126.879146),
                        new LatLng(37.525506    ,126.878917),
                        new LatLng(37.525427    ,126.878819),
                        new LatLng(37.525355    ,126.878754),
                        new LatLng(37.525069    ,126.878609),
                        new LatLng(37.525048    ,126.878606),
                        new LatLng(37.524722    ,126.878613),
                        new LatLng(37.524684    ,126.878614),
                        new LatLng(37.524643    ,126.878621),
                        new LatLng(37.524618    ,126.878631),
                        new LatLng(37.524458    ,126.878709),
                        new LatLng(37.524373    ,126.87875),
                        new LatLng(37.524088    ,126.87886),
                        new LatLng(37.523895    ,126.878892),
                        new LatLng(37.523147    ,126.87889),
                        new LatLng(37.523136    ,126.878891),
                        new LatLng(37.522918    ,126.878924),
                        new LatLng(37.52252     ,126.879053),
                        new LatLng(37.522458    ,126.879002),
                        new LatLng(37.520828    ,126.879408),
                        new LatLng(37.520598    ,126.879437),
                        new LatLng(37.517775    ,126.879484),
                        new LatLng(37.516413    ,126.878569),
                        new LatLng(37.516299    ,126.87839),
                        new LatLng(37.511634    ,126.875461),
                        new LatLng(37.511513    ,126.87551),
                        new LatLng(37.509342    ,126.873979),
                        new LatLng(37.50933     ,126.873969),
                        new LatLng(37.503555    ,126.873569),
                        new LatLng(37.505846    ,126.870256),
                        new LatLng(37.505014    ,126.863513),
                        new LatLng(37.508339    ,126.864082),
                        new LatLng(37.506616    ,126.860316),
                        new LatLng(37.509918    ,126.858328),
                        new LatLng(37.510686    ,126.852867),
                        new LatLng(37.510477    ,126.852158),
                        new LatLng(37.502869    ,126.835247),
                        new LatLng(37.508031    ,126.831025),
                        new LatLng(37.508503    ,126.826989),
                        new LatLng(37.51622     ,126.823105),
                        new LatLng(37.522808    ,126.825196),
                        new LatLng(37.522814    ,126.825193),
                        new LatLng(37.526516    ,126.828837),
                        new LatLng(37.534903    ,126.821868),
                        new LatLng(37.540678    ,126.822121),
                        new LatLng(37.547732    ,126.829759),
                        new LatLng(37.541656    ,126.829985),
                        new LatLng(37.541849    ,126.833103),
                        new LatLng(37.526484    ,126.840487),
                        new LatLng(37.529789    ,126.863973),
                        new LatLng(37.544356    ,126.862115),
                        new LatLng(37.551152    ,126.864208),
                        new LatLng(37.547072    ,126.872644),
                        new LatLng(37.548088    ,126.880792),
                        new LatLng(37.546874    ,126.881976),
                        new LatLng(37.546869    ,126.881982),
                        new LatLng(37.546855    ,126.881997),
                        new LatLng(37.546846    ,126.882006),
                        new LatLng(37.5444      ,126.884225),
                        new LatLng(37.544319    ,126.88424),
                        new LatLng(37.543862    ,126.884266),
                        new LatLng(37.543754    ,126.884268),
                        new LatLng(37.543331    ,126.884698),
                        new LatLng(37.543326    ,126.884705),
                        new LatLng(37.542312    ,126.885436),
                        new LatLng(37.542307    ,126.885437),
                        new LatLng(37.541984    ,126.885446),
                        new LatLng(37.54185     ,126.885449),
                        new LatLng(37.541839    ,126.885451),
                        new LatLng(37.541634    ,126.885494),
                        new LatLng(37.541576    ,126.885507),
                        new LatLng(37.540238    ,126.886089),
                        new LatLng(37.540101    ,126.886151),
                        new LatLng(37.539826    ,126.886282),
                        new LatLng(37.539628    ,126.886391),
                        new LatLng(37.539551    ,126.886433),
                        new LatLng(37.539469    ,126.886477),
                        new LatLng(37.539373    ,126.886526),
                        new LatLng(37.539362    ,126.886532),
                        new LatLng(37.538781    ,126.8869),
                        new LatLng(37.53871     ,126.886998),
                        new LatLng(37.538675    ,126.887038),
                        new LatLng(37.538629    ,126.887079),
                        new LatLng(37.538587    ,126.887107),
                        new LatLng(37.536973    ,126.888078),
                        new LatLng(37.536715    ,126.88826),
                        new LatLng(37.535197    ,126.889705),
                        new LatLng(37.535144    ,126.889742),
                        new LatLng(37.534763    ,126.889969),
                        new LatLng(37.534708    ,126.889996),
                        new LatLng(37.534376    ,126.890128),
                        new LatLng(37.534264    ,126.890143),
                        new LatLng(37.534229    ,126.890148),
                        new LatLng(37.534139    ,126.890152),
                        new LatLng(37.534112    ,126.890154),
                        new LatLng(37.534098    ,126.890152),
                        new LatLng(37.534082    ,126.89015),
                        new LatLng(37.533956    ,126.890112),
                        new LatLng(37.53371     ,126.890037),
                        new LatLng(37.533562    ,126.889937),
                        new LatLng(37.533426    ,126.889832),
                        new LatLng(37.533418    ,126.889823),
                        new LatLng(37.5334      ,126.889803),
                        new LatLng(37.533378    ,126.889769),
                        new LatLng(37.533358    ,126.889734),
                        new LatLng(37.533309    ,126.889649),
                        new LatLng(37.53321     ,126.889462),
                        new LatLng(37.532838    ,126.888738),
                        new LatLng(37.532805    ,126.888693),
                        new LatLng(37.532639    ,126.888564),
                        new LatLng(37.532597    ,126.888547),
                        new LatLng(37.532555    ,126.888531),
                        new LatLng(37.532392    ,126.888538),
                        new LatLng(37.532283    ,126.888549),
                        new LatLng(37.532248    ,126.888562),
                        new LatLng(37.531909    ,126.889087),
                        new LatLng(37.53191     ,126.889391),
                        new LatLng(37.531912    ,126.889448),
                        new LatLng(37.531914    ,126.889523),
                        new LatLng(37.531908    ,126.889658),
                        new LatLng(37.531869    ,126.890289),
                        new LatLng(37.531725    ,126.890581),
                        new LatLng(37.531656    ,126.890684)

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"양천구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "양천구\n   "+hmap.get(name), new LatLng(37.525320, 126.855535));
    }//서울 양천구
    public void drawPolygon159(GoogleMap googlemap) { //서울 구로구
        String name = "구로구";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.485003    ,126.903199 ),
                        new LatLng(37.484977    ,126.903177 ),
                        new LatLng(37.479149    ,126.89899  ),
                        new LatLng(37.478811    ,126.897112 ),
                        new LatLng(37.478731    ,126.89666  ),
                        new LatLng(37.47979     ,126.888785 ),
                        new LatLng(37.483888    ,126.88394  ),
                        new LatLng(37.484046    ,126.883715 ),
                        new LatLng(37.486662    ,126.878463 ),
                        new LatLng(37.485367    ,126.874556 ),
                        new LatLng(37.488571    ,126.87679  ),
                        new LatLng(37.488431    ,126.872748 ),
                        new LatLng(37.491067    ,126.87356  ),
                        new LatLng(37.490845    ,126.873298 ),
                        new LatLng(37.490697    ,126.872448 ),
                        new LatLng(37.490602    ,126.872255 ),
                        new LatLng(37.489596    ,126.870228 ),
                        new LatLng(37.492131    ,126.869512 ),
                        new LatLng(37.492171    ,126.869616 ),
                        new LatLng(37.494533    ,126.869243 ),
                        new LatLng(37.49408     ,126.866907 ),
                        new LatLng(37.493987    ,126.866977 ),
                        new LatLng(37.489668    ,126.861119 ),
                        new LatLng(37.489547    ,126.861016 ),
                        new LatLng(37.485807    ,126.857484 ),
                        new LatLng(37.485795    ,126.857449 ),
                        new LatLng(37.485761    ,126.85729  ),
                        new LatLng(37.485754    ,126.857215 ),
                        new LatLng(37.481787    ,126.85262  ),
                        new LatLng(37.482025    ,126.847455 ),
                        new LatLng(37.48191     ,126.847016 ),
                        new LatLng(37.473875    ,126.845367 ),
                        new LatLng(37.473812    ,126.845361 ),
                        new LatLng(37.474689    ,126.841215 ),
                        new LatLng(37.474677    ,126.841158 ),
                        new LatLng(37.474805    ,126.840031 ),
                        new LatLng(37.474862    ,126.839949 ),
                        new LatLng(37.475178    ,126.839472 ),
                        new LatLng(37.475177    ,126.839446 ),
                        new LatLng(37.475292    ,126.838904 ),
                        new LatLng(37.475315    ,126.838778 ),
                        new LatLng(37.475361    ,126.838445 ),
                        new LatLng(37.475372    ,126.838395 ),
                        new LatLng(37.474975    ,126.837455 ),
                        new LatLng(37.474911    ,126.837257 ),
                        new LatLng(37.474363    ,126.83464  ),
                        new LatLng(37.477246    ,126.833052 ),
                        new LatLng(37.475184    ,126.819311 ),
                        new LatLng(37.475177    ,126.819284 ),
                        new LatLng(37.473188    ,126.81764  ),
                        new LatLng(37.474649    ,126.814629 ),
                        new LatLng(37.479162    ,126.818997 ),
                        new LatLng(37.480281    ,126.819526 ),
                        new LatLng(37.485002    ,126.819413 ),
                        new LatLng(37.485035    ,126.819401 ),
                        new LatLng(37.485393    ,126.81932  ),
                        new LatLng(37.48543     ,126.819307 ),
                        new LatLng(37.485502    ,126.819317 ),
                        new LatLng(37.485504    ,126.819327 ),
                        new LatLng(37.485659    ,126.819764 ),
                        new LatLng(37.485671    ,126.819804 ),
                        new LatLng(37.485707    ,126.819923 ),
                        new LatLng(37.48602     ,126.820857 ),
                        new LatLng(37.486062    ,126.820974 ),
                        new LatLng(37.488187    ,126.823038 ),
                        new LatLng(37.488381    ,126.823041 ),
                        new LatLng(37.490702    ,126.8209   ),
                        new LatLng(37.493193    ,126.814575 ),
                        new LatLng(37.497045    ,126.813645 ),
                        new LatLng(37.497352    ,126.814037 ),
                        new LatLng(37.499211    ,126.81964  ),
                        new LatLng(37.506114    ,126.822383 ),
                        new LatLng(37.508503    ,126.826989 ),
                        new LatLng(37.508031    ,126.831025 ),
                        new LatLng(37.502869    ,126.835247 ),
                        new LatLng(37.510477    ,126.852158 ),
                        new LatLng(37.510686    ,126.852867 ),
                        new LatLng(37.509918    ,126.858328 ),
                        new LatLng(37.506616    ,126.860316 ),
                        new LatLng(37.508339    ,126.864082 ),
                        new LatLng(37.505014    ,126.863513 ),
                        new LatLng(37.505846    ,126.870256 ),
                        new LatLng(37.503555    ,126.873569 ),
                        new LatLng(37.50933     ,126.873969 ),
                        new LatLng(37.509342    ,126.873979 ),
                        new LatLng(37.511513    ,126.87551  ),
                        new LatLng(37.511634    ,126.875461 ),
                        new LatLng(37.516299    ,126.87839  ),
                        new LatLng(37.516413    ,126.878569 ),
                        new LatLng(37.517775    ,126.879484 ),
                        new LatLng(37.512041    ,126.889787 ),
                        new LatLng(37.511894    ,126.889688 ),
                        new LatLng(37.50785     ,126.892783 ),
                        new LatLng(37.507817    ,126.892794 ),
                        new LatLng(37.506109    ,126.893501 ),
                        new LatLng(37.506031    ,126.893532 ),
                        new LatLng(37.50463     ,126.89325  ),
                        new LatLng(37.504625    ,126.893226 ),
                        new LatLng(37.503965    ,126.893306 ),
                        new LatLng(37.503954    ,126.893303 ),
                        new LatLng(37.50372     ,126.893106 ),
                        new LatLng(37.503668    ,126.893009 ),
                        new LatLng(37.502677    ,126.893134 ),
                        new LatLng(37.502668    ,126.893132 ),
                        new LatLng(37.502514    ,126.893019 ),
                        new LatLng(37.502477    ,126.892992 ),
                        new LatLng(37.501606    ,126.892922 ),
                        new LatLng(37.501575    ,126.892933 ),
                        new LatLng(37.501318    ,126.892845 ),
                        new LatLng(37.501301    ,126.892839 ),
                        new LatLng(37.501289    ,126.892835 ),
                        new LatLng(37.501276    ,126.89283  ),
                        new LatLng(37.500419    ,126.892903 ),
                        new LatLng(37.500357    ,126.892926 ),
                        new LatLng(37.499607    ,126.893609 ),
                        new LatLng(37.499586    ,126.893625 ),
                        new LatLng(37.499509    ,126.893646 ),
                        new LatLng(37.499249    ,126.893719 ),
                        new LatLng(37.497848    ,126.894123 ),
                        new LatLng(37.497841    ,126.894118 ),
                        new LatLng(37.492114    ,126.895383 ),
                        new LatLng(37.492001    ,126.895413 ),
                        new LatLng(37.491766    ,126.895472 ),
                        new LatLng(37.490493    ,126.895864 ),
                        new LatLng(37.49021     ,126.896013 ),
                        new LatLng(37.489523    ,126.896113 ),
                        new LatLng(37.489339    ,126.896124 ),
                        new LatLng(37.486937    ,126.898143 ),
                        new LatLng(37.486932    ,126.898146 ),
                        new LatLng(37.485006    ,126.90303  ),
                        new LatLng(37.485003    ,126.903199 )

                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"구로구");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "구로구\n   "+hmap.get(name), new LatLng(37.494959, 126.856236));
    }//서울 구로구
    //경기
    public void drawPolygon41(GoogleMap googlemap) { //과천시
        String name = "과천시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.430702    ,127.047369  ),
                        new LatLng(37.428103    ,127.042016  ),
                        new LatLng(37.415539    ,127.042077  ),
                        new LatLng(37.415613    ,127.041897  ),
                        new LatLng(37.415552    ,127.041727  ),
                        new LatLng(37.415003    ,127.041263  ),
                        new LatLng(37.414589    ,127.040111  ),
                        new LatLng(37.414193    ,127.038981  ),
                        new LatLng(37.413724    ,127.038224  ),
                        new LatLng(37.413996    ,127.035016  ),
                        new LatLng(37.413834    ,127.034282  ),
                        new LatLng(37.41341 ,127.033084      ),
                        new LatLng(37.412942    ,127.031977  ),
                        new LatLng(37.412997    ,127.030769  ),
                        new LatLng(37.412727    ,127.029695  ),
                        new LatLng(37.412546    ,127.029379  ),
                        new LatLng(37.412303    ,127.028983  ),
                        new LatLng(37.412321    ,127.028792  ),
                        new LatLng(37.412132    ,127.028554  ),
                        new LatLng(37.411988    ,127.027876  ),
                        new LatLng(37.412385    ,127.025143  ),
                        new LatLng(37.412431    ,127.022218  ),
                        new LatLng(37.412314    ,127.021811  ),
                        new LatLng(37.412503    ,127.020907  ),
                        new LatLng(37.413116    ,127.018592  ),
                        new LatLng(37.413098    ,127.018174  ),
                        new LatLng(37.413567    ,127.017643  ),
                        new LatLng(37.413666    ,127.017394  ),
                        new LatLng(37.413513    ,127.015621  ),
                        new LatLng(37.413874    ,127.013317  ),
                        new LatLng(37.412964    ,127.013791  ),
                        new LatLng(37.412306    ,127.00899   ),
                        new LatLng(37.410414    ,127.00716   ),
                        new LatLng(37.410099    ,127.006957  ),
                        new LatLng(37.409369    ,127.007126  ),
                        new LatLng(37.407864    ,127.004958  ),
                        new LatLng(37.407594    ,127.004291  ),
                        new LatLng(37.407396    ,127.003681  ),
                        new LatLng(37.406963    ,127.00289   ),
                        new LatLng(37.406684    ,127.001942  ),
                        new LatLng(37.406333    ,127.001512  ),
                        new LatLng(37.406336    ,127.001498  ),
                        new LatLng(37.405974    ,127.001062  ),
                        new LatLng(37.405253    ,127.000461  ),
                        new LatLng(37.404616    ,126.999683   ),
                        new LatLng(37.404796    ,126.999157   ),
                        new LatLng(37.404883    ,126.998545   ),
                        new LatLng(37.404765    ,126.997827   ),
                        new LatLng(37.404785    ,126.997692   ),
                        new LatLng(37.404932    ,126.997164   ),
                        new LatLng(37.404882    ,126.996808   ),
                        new LatLng(37.405062    ,126.996379   ),
                        new LatLng(37.405071    ,126.996059   ),
                        new LatLng(37.405084    ,126.996022   ),
                        new LatLng(37.405247    ,126.99569    ),
                        new LatLng(37.405125    ,126.994993   ),
                        new LatLng(37.405032    ,126.994627   ),
                        new LatLng(37.405008    ,126.994273   ),
                        new LatLng(37.405043    ,126.994254   ),
                        new LatLng(37.405474    ,126.993717   ),
                        new LatLng(37.406091    ,126.99338    ),
                        new LatLng(37.406697    ,126.992736   ),
                        new LatLng(37.406711    ,126.992369   ),
                        new LatLng(37.407642    ,126.99208    ),
                        new LatLng(37.407718    ,126.991732   ),
                        new LatLng(37.4078  ,126.991363       ),
                        new LatLng(37.407828    ,126.991228   ),
                        new LatLng(37.40789 ,126.991004       ),
                        new LatLng(37.4079  ,126.991008       ),
                        new LatLng(37.407903    ,126.990972   ),
                        new LatLng(37.407879    ,126.990827   ),
                        new LatLng(37.408211    ,126.989974   ),
                        new LatLng(37.407736    ,126.988448   ),
                        new LatLng(37.407847    ,126.98833    ),
                        new LatLng(37.407833    ,126.987865   ),
                        new LatLng(37.407776    ,126.987806   ),
                        new LatLng(37.407666    ,126.987529   ),
                        new LatLng(37.407566    ,126.9874     ),
                        new LatLng(37.407538    ,126.987362   ),
                        new LatLng(37.407443    ,126.987249   ),
                        new LatLng(37.407161    ,126.986795   ),
                        new LatLng(37.407094    ,126.986634   ),
                        new LatLng(37.407078    ,126.986606   ),
                        new LatLng(37.407024    ,126.986514   ),
                        new LatLng(37.406998    ,126.98648    ),
                        new LatLng(37.406932    ,126.986368   ),
                        new LatLng(37.40691 ,126.986322       ),
                        new LatLng(37.406849    ,126.986172   ),
                        new LatLng(37.406812    ,126.986071   ),
                        new LatLng(37.406744    ,126.98591    ),
                        new LatLng(37.406507    ,126.985713   ),
                        new LatLng(37.406393    ,126.985611   ),
                        new LatLng(37.406345    ,126.985547   ),
                        new LatLng(37.406285    ,126.985502   ),
                        new LatLng(37.406201    ,126.985451   ),
                        new LatLng(37.406094    ,126.985378   ),
                        new LatLng(37.405962    ,126.985271   ),
                        new LatLng(37.405868    ,126.985177   ),
                        new LatLng(37.405845    ,126.985158   ),
                        new LatLng(37.405305    ,126.985074   ),
                        new LatLng(37.405209    ,126.985015   ),
                        new LatLng(37.405143    ,126.984973   ),
                        new LatLng(37.404907    ,126.984897   ),
                        new LatLng(37.404818    ,126.984892   ),
                        new LatLng(37.404802    ,126.98489    ),
                        new LatLng(37.404755    ,126.984899   ),
                        new LatLng(37.404744    ,126.9849     ),
                        new LatLng(37.404557    ,126.984908   ),
                        new LatLng(37.404403    ,126.984894   ),
                        new LatLng(37.404295    ,126.984921   ),
                        new LatLng(37.403926    ,126.984932   ),
                        new LatLng(37.403844    ,126.984964   ),
                        new LatLng(37.403151    ,126.984944   ),
                        new LatLng(37.402945    ,126.984849   ),
                        new LatLng(37.402683    ,126.984704   ),
                        new LatLng(37.402612    ,126.984657   ),
                        new LatLng(37.402521    ,126.984645   ),
                        new LatLng(37.402475    ,126.984611   ),
                        new LatLng(37.402352    ,126.98448    ),
                        new LatLng(37.402326    ,126.984385   ),
                        new LatLng(37.402216    ,126.984143   ),
                        new LatLng(37.402214    ,126.984041   ),
                        new LatLng(37.402174    ,126.983925   ),
                        new LatLng(37.402105    ,126.983804   ),
                        new LatLng(37.402088    ,126.983769   ),
                        new LatLng(37.401994    ,126.983674   ),
                        new LatLng(37.401953    ,126.983628   ),
                        new LatLng(37.401927    ,126.983554   ),
                        new LatLng(37.40192 ,126.983499       ),
                        new LatLng(37.401878    ,126.983295   ),
                        new LatLng(37.40186 ,126.983252       ),
                        new LatLng(37.401868    ,126.983243   ),
                        new LatLng(37.401816    ,126.983201   ),
                        new LatLng(37.401404    ,126.983065   ),
                        new LatLng(37.401115    ,126.982662   ),
                        new LatLng(37.400688    ,126.982365   ),
                        new LatLng(37.400685    ,126.982371   ),
                        new LatLng(37.400745    ,126.981985   ),
                        new LatLng(37.401133    ,126.9819     ),
                        new LatLng(37.401339    ,126.981838   ),
                        new LatLng(37.401439    ,126.981782   ),
                        new LatLng(37.401593    ,126.981878   ),
                        new LatLng(37.401718    ,126.981838   ),
                        new LatLng(37.401844    ,126.981748   ),
                        new LatLng(37.402141    ,126.980788   ),
                        new LatLng(37.402556    ,126.980844   ),
                        new LatLng(37.403141    ,126.97819    ),
                        new LatLng(37.403222    ,126.977964   ),
                        new LatLng(37.403033    ,126.977264   ),
                        new LatLng(37.403132    ,126.977399   ),
                        new LatLng(37.403834    ,126.977376   ),
                        new LatLng(37.403934    ,126.97741    ),
                        new LatLng(37.404303    ,126.977873   ),
                        new LatLng(37.404492    ,126.97828    ),
                        new LatLng(37.404673    ,126.978517   ),
                        new LatLng(37.405303    ,126.978641   ),
                        new LatLng(37.405375    ,126.978607   ),
                        new LatLng(37.405511    ,126.978505   ),
                        new LatLng(37.405691    ,126.978178   ),
                        new LatLng(37.405538    ,126.978189   ),
                        new LatLng(37.405862    ,126.977613   ),
                        new LatLng(37.406141    ,126.976653   ),
                        new LatLng(37.406132    ,126.976111   ),
                        new LatLng(37.406006    ,126.976054   ),
                        new LatLng(37.405897    ,126.975874   ),
                        new LatLng(37.406672    ,126.975602   ),
                        new LatLng(37.408916    ,126.975579   ),
                        new LatLng(37.409447    ,126.975737   ),
                        new LatLng(37.409538    ,126.975759   ),
                        new LatLng(37.409772    ,126.975804   ),
                        new LatLng(37.410222    ,126.97568    ),
                        new LatLng(37.410709    ,126.975352   ),
                        new LatLng(37.41134 ,126.975623       ),
                        new LatLng(37.41149 ,126.9757         ),
                        new LatLng(37.412096    ,126.975273   ),
                        new LatLng(37.413024    ,126.975114   ),
                        new LatLng(37.413331    ,126.974877   ),
                        new LatLng(37.413799    ,126.974267   ),
                        new LatLng(37.413988    ,126.974154   ),
                        new LatLng(37.415078    ,126.973758   ),
                        new LatLng(37.415204    ,126.973758   ),
                        new LatLng(37.415907    ,126.972933   ),
                        new LatLng(37.415979    ,126.972402   ),
                        new LatLng(37.415988    ,126.972379   ),
                        new LatLng(37.415997    ,126.97186    ),
                        new LatLng(37.416042    ,126.971408   ),
                        new LatLng(37.416715    ,126.970626   ),
                        new LatLng(37.417267    ,126.970312   ),
                        new LatLng(37.417312    ,126.970074   ),
                        new LatLng(37.417267    ,126.969578   ),
                        new LatLng(37.417339    ,126.969317   ),
                        new LatLng(37.417158    ,126.968301   ),
                        new LatLng(37.417239    ,126.967262   ),
                        new LatLng(37.417302    ,126.966889   ),
                        new LatLng(37.418041    ,126.965985   ),
                        new LatLng(37.419562    ,126.963356   ),
                        new LatLng(37.419813    ,126.963386   ),
                        new LatLng(37.42138 ,126.96239        ),
                        new LatLng(37.421569    ,126.962367   ),
                        new LatLng(37.421941    ,126.962345   ),
                        new LatLng(37.423572    ,126.962108   ),
                        new LatLng(37.424562    ,126.961689   ),
                        new LatLng(37.425184    ,126.961191   ),
                        new LatLng(37.426085    ,126.960931   ),
                        new LatLng(37.426088    ,126.960935   ),
                        new LatLng(37.427743    ,126.962037   ),
                        new LatLng(37.429437    ,126.962172   ),
                        new LatLng(37.430551    ,126.962451   ),
                        new LatLng(37.431258    ,126.963956   ),
                        new LatLng(37.434853    ,126.963197   ),
                        new LatLng(37.437601    ,126.964032   ),
                        new LatLng(37.439223    ,126.965478   ),
                        new LatLng(37.440787    ,126.963803   ),
                        new LatLng(37.446267    ,126.96429    ),
                        new LatLng(37.454413    ,126.97458    ),
                        new LatLng(37.458166    ,126.988636   ),
                        new LatLng(37.461873    ,126.996769   ),
                        new LatLng(37.467072    ,126.996752   ),
                        new LatLng(37.46772 ,127.003675      ),
                        new LatLng(37.455408    ,127.011368  ),
                        new LatLng(37.455509    ,127.011939  ),
                        new LatLng(37.457817    ,127.025995  ),
                        new LatLng(37.465374    ,127.029527  ),
                        new LatLng(37.463458    ,127.034537  ),
                        new LatLng(37.463452    ,127.034534  ),
                        new LatLng(37.46153 ,127.033822      ),
                        new LatLng(37.46147 ,127.033799      ),
                        new LatLng(37.445859    ,127.038206  ),
                        new LatLng(37.439004    ,127.035574  ),
                        new LatLng(37.437769    ,127.04109   ),
                        new LatLng(37.430702    ,127.047369  ))
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"과천시");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "과천시\n   "+hmap.get(name), new LatLng(37.434498, 127.002946));
    }//과천시
    public void drawPolygon57(GoogleMap googlemap) { //구리시
        String name = "구리시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.581029	,127.170598),
                        new LatLng(37.57977 	,127.168428),
                        new LatLng(37.578997	,127.168648),
                        new LatLng(37.576387	,127.162007),
                        new LatLng(37.572954	,127.156899),
                        new LatLng(37.572044	,127.154981),
                        new LatLng(37.568434	,127.14895),
                        new LatLng(37.568426	,127.139926),
                        new LatLng(37.568249	,127.136404),
                        new LatLng(37.568025	,127.134741),
                        new LatLng(37.567904	,127.134106),
                        new LatLng(37.567701	,127.133458),
                        new LatLng(37.567243	,127.132018),
                        new LatLng(37.56677 	,127.13054),
                        new LatLng(37.564809	,127.125783),
                        new LatLng(37.5612   	,127.120096),
                        new LatLng(37.560947	,127.119706),
                        new LatLng(37.560678	,127.119278),
                        new LatLng(37.56053 	,127.119042),
                        new LatLng(37.560138	,127.118418),
                        new LatLng(37.560078	,127.118321),
                        new LatLng(37.559713	,127.117742),
                        new LatLng(37.559426	,127.117303),
                        new LatLng(37.55676 	,127.115252),
                        new LatLng(37.556808	,127.114365),
                        new LatLng(37.55683 	,127.113523),
                        new LatLng(37.55683 	,127.113315),
                        new LatLng(37.556839	,127.11332),
                        new LatLng(37.556891	,127.113325),
                        new LatLng(37.557097	,127.113333),
                        new LatLng(37.558133	,127.113679),
                        new LatLng(37.558443	,127.113845),
                        new LatLng(37.55849 	,127.11378),
                        new LatLng(37.558537	,127.113646),
                        new LatLng(37.55857 	,127.11356),
                        new LatLng(37.558644	,127.113363),
                        new LatLng(37.558885	,127.112711),
                        new LatLng(37.558972	,127.112402),
                        new LatLng(37.559001	,127.112298),
                        new LatLng(37.558991	,127.112282),
                        new LatLng(37.558747	,127.111891),
                        new LatLng(37.558616	,127.111643),
                        new LatLng(37.558473	,127.111266),
                        new LatLng(37.558446	,127.111168),
                        new LatLng(37.558408	,127.111023),
                        new LatLng(37.55836 	,127.110827),
                        new LatLng(37.558253	,127.11035),
                        new LatLng(37.558448	,127.109322),
                        new LatLng(37.55833 	,127.108928),
                        new LatLng(37.558245	,127.108543),
                        new LatLng(37.558224	,127.10852),
                        new LatLng(37.558031	,127.108306),
                        new LatLng(37.557709	,127.107651),
                        new LatLng(37.557607	,127.107519),
                        new LatLng(37.557363	,127.107337),
                        new LatLng(37.55709 	,127.107109),
                        new LatLng(37.556766	,127.10664),
                        new LatLng(37.556814	,127.104657),
                        new LatLng(37.557207	,127.10445),
                        new LatLng(37.557408	,127.104352),
                        new LatLng(37.55771 	,127.104042),
                        new LatLng(37.558013	,127.103655),
                        new LatLng(37.558205	,127.103433),
                        new LatLng(37.558708	,127.102854),
                        new LatLng(37.559035	,127.102648),
                        new LatLng(37.559278	,127.102167),
                        new LatLng(37.559374	,127.102045),
                        new LatLng(37.559567	,127.101801),
                        new LatLng(37.56025 	,127.101399),
                        new LatLng(37.560916	,127.101158),
                        new LatLng(37.56158 	,127.101205),
                        new LatLng(37.56261 	,127.101657),
                        new LatLng(37.563291	,127.101625),
                        new LatLng(37.564311	,127.102234),
                        new LatLng(37.564896	,127.102255),
                        new LatLng(37.565637	,127.102387),
                        new LatLng(37.566689	,127.102634),
                        new LatLng(37.567997	,127.102979),
                        new LatLng(37.568821	,127.103206),
                        new LatLng(37.569398	,127.10334),
                        new LatLng(37.571388	,127.10423),
                        new LatLng(37.571896	,127.103684),
                        new LatLng(37.571918	,127.103661),
                        new LatLng(37.572279	,127.10313),
                        new LatLng(37.572155	,127.102238),
                        new LatLng(37.572433	,127.101642),
                        new LatLng(37.572988	,127.101219),
                        new LatLng(37.573355	,127.100936),
                        new LatLng(37.573746	,127.100893),
                        new LatLng(37.573763	,127.100888),
                        new LatLng(37.574204	,127.100919),
                        new LatLng(37.57497 	,127.101162),
                        new LatLng(37.575573	,127.101202),
                        new LatLng(37.576071	,127.101144),
                        new LatLng(37.576967	,127.101733),
                        new LatLng(37.577552	,127.101958),
                        new LatLng(37.578043	,127.102318),
                        new LatLng(37.578528	,127.102758),
                        new LatLng(37.578597	,127.102808),
                        new LatLng(37.579277	,127.102977),
                        new LatLng(37.579786	,127.102909),
                        new LatLng(37.581404	,127.104974),
                        new LatLng(37.581662	,127.105421),
                        new LatLng(37.58174 	,127.105584),
                        new LatLng(37.581937	,127.105995),
                        new LatLng(37.581996	,127.106223),
                        new LatLng(37.582263	,127.106915),
                        new LatLng(37.582463	,127.107383),
                        new LatLng(37.583238	,127.108438),
                        new LatLng(37.583379	,127.108967),
                        new LatLng(37.583867	,127.109231),
                        new LatLng(37.584561	,127.10938),
                        new LatLng(37.586637	,127.110023),
                        new LatLng(37.587746	,127.110298),
                        new LatLng(37.588458	,127.110524),
                        new LatLng(37.58943 	,127.110855),
                        new LatLng(37.591859	,127.112414),
                        new LatLng(37.592857	,127.113095),
                        new LatLng(37.593433	,127.114454),
                        new LatLng(37.59351 	,127.115182),
                        new LatLng(37.59356 	,127.115527),
                        new LatLng(37.593625	,127.11569),
                        new LatLng(37.596192	,127.116443),
                        new LatLng(37.597314	,127.115727),
                        new LatLng(37.598077	,127.115179),
                        new LatLng(37.599055	,127.114471),
                        new LatLng(37.599521	,127.114064),
                        new LatLng(37.599793	,127.11404),
                        new LatLng(37.600121	,127.114082),
                        new LatLng(37.600129	,127.1141),
                        new LatLng(37.60127 	,127.115294),
                        new LatLng(37.601404	,127.115384),
                        new LatLng(37.601841	,127.115633),
                        new LatLng(37.603053	,127.116872),
                        new LatLng(37.603395	,127.117016),
                        new LatLng(37.604602	,127.118047),
                        new LatLng(37.605444	,127.118096),
                        new LatLng(37.605567	,127.118094),
                        new LatLng(37.605886	,127.117998),
                        new LatLng(37.608479	,127.117538),
                        new LatLng(37.60886 	,127.116798),
                        new LatLng(37.608849	,127.116698),
                        new LatLng(37.609531	,127.116878),
                        new LatLng(37.610426	,127.116828),
                        new LatLng(37.610706	,127.116962),
                        new LatLng(37.611181	,127.117287),
                        new LatLng(37.611771	,127.117483),
                        new LatLng(37.612245	,127.117374),
                        new LatLng(37.614144	,127.117061),
                        new LatLng(37.614955	,127.116796),
                        new LatLng(37.615484	,127.116736),
                        new LatLng(37.616015	,127.116672),
                        new LatLng(37.616367	,127.116905),
                        new LatLng(37.617401	,127.117016),
                        new LatLng(37.617676	,127.11709),
                        new LatLng(37.617888	,127.117147),
                        new LatLng(37.619115	,127.116079),
                        new LatLng(37.619634	,127.115715),
                        new LatLng(37.619538	,127.115021),
                        new LatLng(37.619896	,127.113939),
                        new LatLng(37.620063	,127.113399),
                        new LatLng(37.620307	,127.112143),
                        new LatLng(37.621055	,127.110519),
                        new LatLng(37.620811	,127.110175),
                        new LatLng(37.620515	,127.109128),
                        new LatLng(37.620517	,127.108946),
                        new LatLng(37.620507	,127.107243),
                        new LatLng(37.620465	,127.107085),
                        new LatLng(37.620341	,127.106041),
                        new LatLng(37.620158	,127.105565),
                        new LatLng(37.620213	,127.105562),
                        new LatLng(37.620371	,127.105556),
                        new LatLng(37.620902	,127.105383),
                        new LatLng(37.621567	,127.104909),
                        new LatLng(37.623271	,127.104328),
                        new LatLng(37.625265	,127.105015),
                        new LatLng(37.627331	,127.105964),
                        new LatLng(37.629302	,127.108885),
                        new LatLng(37.631228	,127.111371),
                        new LatLng(37.634757	,127.112347),
                        new LatLng(37.635564	,127.112252),
                        new LatLng(37.636489	,127.112483),
                        new LatLng(37.637408	,127.111951),
                        new LatLng(37.637846	,127.111687),
                        new LatLng(37.637991	,127.111599),
                        new LatLng(37.638222	,127.111454),
                        new LatLng(37.638309	,127.111184),
                        new LatLng(37.638774	,127.110761),
                        new LatLng(37.639944	,127.111136),
                        new LatLng(37.639897	,127.111702),
                        new LatLng(37.641247	,127.111615),
                        new LatLng(37.641929	,127.11143),
                        new LatLng(37.642315	,127.11129),
                        new LatLng(37.642375	,127.111257),
                        new LatLng(37.642376	,127.111258),
                        new LatLng(37.643798	,127.1156),
                        new LatLng(37.641209	,127.127238),
                        new LatLng(37.646081	,127.140419),
                        new LatLng(37.644246	,127.146058),
                        new LatLng(37.63463 	,127.149903),
                        new LatLng(37.627712	,127.14397),
                        new LatLng(37.626933	,127.143702),
                        new LatLng(37.625699	,127.143304),
                        new LatLng(37.625211	,127.143145),
                        new LatLng(37.599944	,127.150603),
                        new LatLng(37.596288	,127.156122),
                        new LatLng(37.594042	,127.167696),
                        new LatLng(37.590834	,127.16847),
                        new LatLng(37.589689	,127.162619),
                        new LatLng(37.586772	,127.16049),
                        new LatLng(37.581029	,127.170598)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"구리시");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "구리시\n   "+hmap.get(name), new LatLng(37.599407, 127.131271));
    }//구리시
    public void drawPolygon33(GoogleMap googlemap) { //
    String name = "광명시";
    int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

    Polygon polygon = mMap.addPolygon(new PolygonOptions()
            .add(
                    new LatLng(37.434827	,126.899841	),
                    new LatLng(37.434031	,126.899152	),
                    new LatLng(37.433401	,126.899207	),
                    new LatLng(37.433327	,126.898715	),
                    new LatLng(37.432728	,126.898349	),
                    new LatLng(37.432399	,126.898472	),
                    new LatLng(37.431881	,126.898319	),
                    new LatLng(37.43172 	,126.898247	),
                    new LatLng(37.431269	,126.898137	),
                    new LatLng(37.431251	,126.898124	),
                    new LatLng(37.430954	,126.897446	),
                    new LatLng(37.43116 	,126.896889	),
                    new LatLng(37.431197	,126.896505	),
                    new LatLng(37.431045	,126.896235	),
                    new LatLng(37.430775	,126.895992	),
                    new LatLng(37.430596	,126.895832	),
                    new LatLng(37.430569	,126.895799	),
                    new LatLng(37.430369	,126.895529	),
                    new LatLng(37.429681	,126.894665	),
                    new LatLng(37.429534	,126.89465	),
                    new LatLng(37.429187	,126.894417	),
                    new LatLng(37.429017	,126.894263	),
                    new LatLng(37.428845	,126.894336	),
                    new LatLng(37.428324	,126.894521	),
                    new LatLng(37.428278	,126.894507	),
                    new LatLng(37.427962	,126.89423	),
                    new LatLng(37.427801	,126.894215	),
                    new LatLng(37.427575	,126.894351	),
                    new LatLng(37.427358	,126.894836	),
                    new LatLng(37.427523	,126.895337	),
                    new LatLng(37.427727	,126.895542	),
                    new LatLng(37.427837	,126.895804	),
                    new LatLng(37.427421	,126.896814	),
                    new LatLng(37.42719 	,126.896892	),
                    new LatLng(37.426848	,126.896719	),
                    new LatLng(37.426772	,126.896613	),
                    new LatLng(37.426562	,126.896311	),
                    new LatLng(37.426394	,126.896172	),
                    new LatLng(37.42634 	,126.896156	),
                    new LatLng(37.426039	,126.896287	),
                    new LatLng(37.425184	,126.896434	),
                    new LatLng(37.424963	,126.896045	),
                    new LatLng(37.424775	,126.895617	),
                    new LatLng(37.424385	,126.895004	),
                    new LatLng(37.424169	,126.894973	),
                    new LatLng(37.42365 	,126.894953	),
                    new LatLng(37.422526	,126.894087	),
                    new LatLng(37.421177	,126.893906	),
                    new LatLng(37.420916	,126.893942	),
                    new LatLng(37.420535	,126.893914	),
                    new LatLng(37.420436	,126.89384	),
                    new LatLng(37.420228	,126.893592	),
                    new LatLng(37.420419	,126.892601	),
                    new LatLng(37.420533	,126.891761	),
                    new LatLng(37.420291	,126.891189	),
                    new LatLng(37.419924	,126.890512	),
                    new LatLng(37.419658	,126.890066	),
                    new LatLng(37.41917 	,126.889355	),
                    new LatLng(37.41861 	,126.88891	),
                    new LatLng(37.41813 	,126.889126	),
                    new LatLng(37.418007	,126.889336	),
                    new LatLng(37.417771	,126.889819	),
                    new LatLng(37.417649	,126.8898	),
                    new LatLng(37.417624	,126.889769	),
                    new LatLng(37.417439	,126.889744	),
                    new LatLng(37.416215	,126.889501	),
                    new LatLng(37.416195	,126.889608	),
                    new LatLng(37.416185	,126.889626	),
                    new LatLng(37.415825	,126.889547	),
                    new LatLng(37.415774	,126.889518	),
                    new LatLng(37.415533	,126.889365	),
                    new LatLng(37.415291	,126.889222	),
                    new LatLng(37.414956	,126.889182	),
                    new LatLng(37.414854	,126.889173	),
                    new LatLng(37.414359	,126.889119	),
                    new LatLng(37.413887	,126.889333	),
                    new LatLng(37.413353	,126.889415	),
                    new LatLng(37.413314	,126.889409	),
                    new LatLng(37.41328 	,126.889404	),
                    new LatLng(37.413283	,126.889357	),
                    new LatLng(37.412486	,126.889263	),
                    new LatLng(37.412463	,126.889272	),
                    new LatLng(37.412385	,126.889268	),
                    new LatLng(37.411977	,126.889141	),
                    new LatLng(37.411335	,126.888644	),
                    new LatLng(37.411159	,126.887875	),
                    new LatLng(37.411124	,126.886713	),
                    new LatLng(37.411564	,126.88587	),
                    new LatLng(37.411701	,126.885442	),
                    new LatLng(37.411688	,126.885307	),
                    new LatLng(37.411671	,126.885182	),
                    new LatLng(37.411649	,126.885023	),
                    new LatLng(37.411616	,126.884795	),
                    new LatLng(37.411663	,126.884809	),
                    new LatLng(37.411644	,126.884702	),
                    new LatLng(37.411602	,126.884614	),
                    new LatLng(37.411589	,126.884284	),
                    new LatLng(37.411493	,126.884104	),
                    new LatLng(37.41079 	,126.882139	),
                    new LatLng(37.410795	,126.881948	),
                    new LatLng(37.410841	,126.881728	),
                    new LatLng(37.410846	,126.881313	),
                    new LatLng(37.410639	,126.880703	),
                    new LatLng(37.410382	,126.880316	),
                    new LatLng(37.41014 	,126.879909	),
                    new LatLng(37.408965	,126.879147	),
                    new LatLng(37.408894	,126.87837	),
                    new LatLng(37.40932 	,126.877629	),
                    new LatLng(37.410394	,126.876039	),
                    new LatLng(37.410867	,126.876056	),
                    new LatLng(37.411158	,126.875327	),
                    new LatLng(37.411578	,126.875	),
                    new LatLng(37.411577	,126.87499	),
                    new LatLng(37.411457	,126.874174	),
                    new LatLng(37.411795	,126.872214	),
                    new LatLng(37.412523	,126.871485	),
                    new LatLng(37.412711	,126.871161	),
                    new LatLng(37.412417	,126.871103	),
                    new LatLng(37.411295	,126.870653	),
                    new LatLng(37.410804	,126.870386	),
                    new LatLng(37.410137	,126.870669	),
                    new LatLng(37.409263	,126.870923	),
                    new LatLng(37.408138	,126.870242	),
                    new LatLng(37.407523	,126.870322	),
                    new LatLng(37.407044	,126.870411	),
                    new LatLng(37.406799	,126.871215	),
                    new LatLng(37.406485	,126.875848	),
                    new LatLng(37.40559 	,126.876272	),
                    new LatLng(37.4055  	,126.876347	),
                    new LatLng(37.405255	,126.876553	),
                    new LatLng(37.402193	,126.876962	),
                    new LatLng(37.401971	,126.876943	),
                    new LatLng(37.401929	,126.876918	),
                    new LatLng(37.401719	,126.865804	),
                    new LatLng(37.401713	,126.86573	),
                    new LatLng(37.403443	,126.855108	),
                    new LatLng(37.403444	,126.855107	),
                    new LatLng(37.403478	,126.855063	),
                    new LatLng(37.413783	,126.846214	),
                    new LatLng(37.413909	,126.846193	),
                    new LatLng(37.422885	,126.8454	),
                    new LatLng(37.426701	,126.841724	),
                    new LatLng(37.426754	,126.84173	),
                    new LatLng(37.434687	,126.841447	),
                    new LatLng(37.438063	,126.837521	),
                    new LatLng(37.458122	,126.838332	),
                    new LatLng(37.460295	,126.833081	),
                    new LatLng(37.457741	,126.829148	),
                    new LatLng(37.459479	,126.826101	),
                    new LatLng(37.465865	,126.832996	),
                    new LatLng(37.473365	,126.832162	),
                    new LatLng(37.473865	,126.832412	),
                    new LatLng(37.477246	,126.833052	),
                    new LatLng(37.474363	,126.83464	),
                    new LatLng(37.474911	,126.837257	),
                    new LatLng(37.474975	,126.837455	),
                    new LatLng(37.475372	,126.838395	),
                    new LatLng(37.475361	,126.838445	),
                    new LatLng(37.475315	,126.838778	),
                    new LatLng(37.475292	,126.838904	),
                    new LatLng(37.475177	,126.839446	),
                    new LatLng(37.475178	,126.839472	),
                    new LatLng(37.474862	,126.839949	),
                    new LatLng(37.474805	,126.840031	),
                    new LatLng(37.474677	,126.841158	),
                    new LatLng(37.474689	,126.841215	),
                    new LatLng(37.473812	,126.845361	),
                    new LatLng(37.473875	,126.845367	),
                    new LatLng(37.48191 	,126.847016	),
                    new LatLng(37.482025	,126.847455	),
                    new LatLng(37.481787	,126.85262	),
                    new LatLng(37.485754	,126.857215	),
                    new LatLng(37.485761	,126.85729	),
                    new LatLng(37.485795	,126.857449	),
                    new LatLng(37.485807	,126.857484	),
                    new LatLng(37.489547	,126.861016	),
                    new LatLng(37.489668	,126.861119	),
                    new LatLng(37.493987	,126.866977	),
                    new LatLng(37.49408 	,126.866907	),
                    new LatLng(37.494533	,126.869243	),
                    new LatLng(37.492171	,126.869616	),
                    new LatLng(37.492131	,126.869512	),
                    new LatLng(37.489596	,126.870228	),
                    new LatLng(37.490602	,126.872255	),
                    new LatLng(37.490697	,126.872448	),
                    new LatLng(37.490845	,126.873298	),
                    new LatLng(37.491067	,126.87356	),
                    new LatLng(37.488431	,126.872748	),
                    new LatLng(37.488571	,126.87679	),
                    new LatLng(37.485367	,126.874556	),
                    new LatLng(37.485269	,126.871763	),
                    new LatLng(37.482425	,126.872744	),
                    new LatLng(37.466012	,126.884618	),
                    new LatLng(37.464391	,126.882746	),
                    new LatLng(37.460947	,126.888876	),
                    new LatLng(37.459863	,126.88535	),
                    new LatLng(37.455537	,126.88766	),
                    new LatLng(37.455568	,126.88786	),
                    new LatLng(37.452315	,126.889642	),
                    new LatLng(37.452717	,126.893983	),
                    new LatLng(37.438702	,126.898978	),
                    new LatLng(37.438652	,126.899017	),
                    new LatLng(37.438662	,126.899039	),
                    new LatLng(37.438114	,126.899409	),
                    new LatLng(37.438092	,126.899439	),
                    new LatLng(37.438025	,126.899529	),
                    new LatLng(37.436745	,126.899498	),
                    new LatLng(37.436103	,126.899427	),
                    new LatLng(37.435883	,126.899556	),
                    new LatLng(37.43583 	,126.899596	),
                    new LatLng(37.435607	,126.899554	),
                    new LatLng(37.435081	,126.899417	),
                    new LatLng(37.434995	,126.89961	),
                    new LatLng(37.434827	,126.899841	)
            )
            .strokeColor(Color.WHITE)             .strokeWidth(2)
            .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"광명시");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "광명시\n   "+hmap.get(name), new LatLng(37.445600, 126.865055));
}//광명시
    public void drawPolygon47(GoogleMap googlemap) { //
        String name = "하남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.514398	,127.285701	),
                        new LatLng(37.506363	,127.2683	),
                        new LatLng(37.506312	,127.254621	),
                        new LatLng(37.496986	,127.255081	),
                        new LatLng(37.491053	,127.259188	),
                        new LatLng(37.485037	,127.253605	),
                        new LatLng(37.485097	,127.24443	),
                        new LatLng(37.480523	,127.247015	),
                        new LatLng(37.47665 	,127.2446	),
                        new LatLng(37.477479	,127.228391	),
                        new LatLng(37.481992	,127.219297	),
                        new LatLng(37.480301	,127.205019	),
                        new LatLng(37.4841  	,127.201863	),
                        new LatLng(37.479601	,127.198403	),
                        new LatLng(37.480339	,127.188584	),
                        new LatLng(37.487206	,127.17772	),
                        new LatLng(37.478502	,127.17546	),
                        new LatLng(37.47842 	,127.175438	),
                        new LatLng(37.474808	,127.178754	),
                        new LatLng(37.471958	,127.175615	),
                        new LatLng(37.468773	,127.16778	),
                        new LatLng(37.472944	,127.1545	),
                        new LatLng(37.473439	,127.139364	),
                        new LatLng(37.474718	,127.14139	),
                        new LatLng(37.474722	,127.141398	),
                        new LatLng(37.479597	,127.148552	),
                        new LatLng(37.479648	,127.148592	),
                        new LatLng(37.480781	,127.14917	),
                        new LatLng(37.480801	,127.149185	),
                        new LatLng(37.480858	,127.149302	),
                        new LatLng(37.48087 	,127.149334	),
                        new LatLng(37.480915	,127.149351	),
                        new LatLng(37.480922	,127.149353	),
                        new LatLng(37.48601 	,127.151894	),
                        new LatLng(37.486132	,127.152032	),
                        new LatLng(37.486236	,127.152178	),
                        new LatLng(37.486293	,127.152316	),
                        new LatLng(37.486417	,127.152687	),
                        new LatLng(37.490612	,127.158235	),
                        new LatLng(37.499066	,127.161005	),
                        new LatLng(37.499142	,127.161011	),
                        new LatLng(37.503179	,127.157729	),
                        new LatLng(37.501859	,127.156438	),
                        new LatLng(37.504742	,127.150205	),
                        new LatLng(37.503306	,127.145503	),
                        new LatLng(37.505407	,127.141048	),
                        new LatLng(37.508904	,127.140064	),
                        new LatLng(37.508997	,127.140085	),
                        new LatLng(37.514357	,127.142606	),
                        new LatLng(37.514363	,127.142595	),
                        new LatLng(37.515589	,127.140614	),
                        new LatLng(37.516835	,127.145092	),
                        new LatLng(37.521479	,127.14559	),
                        new LatLng(37.521581	,127.145643	),
                        new LatLng(37.521932	,127.145666	),
                        new LatLng(37.521941	,127.145769	),
                        new LatLng(37.532663	,127.153802	),
                        new LatLng(37.532858	,127.153756	),
                        new LatLng(37.541083	,127.159597	),
                        new LatLng(37.541284	,127.159647	),
                        new LatLng(37.544487	,127.165188	),
                        new LatLng(37.544437	,127.165254	),
                        new LatLng(37.54557 	,127.173205	),
                        new LatLng(37.545569	,127.173407	),
                        new LatLng(37.54554 	,127.173615	),
                        new LatLng(37.545582	,127.173906	),
                        new LatLng(37.546142	,127.177837	),
                        new LatLng(37.546302	,127.178261	),
                        new LatLng(37.54517 	,127.183539	),
                        new LatLng(37.547748	,127.182674	),
                        new LatLng(37.547876	,127.182676	),
                        new LatLng(37.568946	,127.179204	),
                        new LatLng(37.569003	,127.179181	),
                        new LatLng(37.569124	,127.179133	),
                        new LatLng(37.569237	,127.179063	),
                        new LatLng(37.569738	,127.178679	),
                        new LatLng(37.569769	,127.178654	),
                        new LatLng(37.570374	,127.178397	),
                        new LatLng(37.570413	,127.178383	),
                        new LatLng(37.571067	,127.178058	),
                        new LatLng(37.571153	,127.177982	),
                        new LatLng(37.572171	,127.177742	),
                        new LatLng(37.572188	,127.177731	),
                        new LatLng(37.577235	,127.175328	),
                        new LatLng(37.581201	,127.177155	),
                        new LatLng(37.578997	,127.168648	),
                        new LatLng(37.57977 	,127.168428	),
                        new LatLng(37.581029	,127.170598	),
                        new LatLng(37.585515	,127.176888	),
                        new LatLng(37.586249	,127.177844	),
                        new LatLng(37.586548	,127.17833	),
                        new LatLng(37.588462	,127.180579	),
                        new LatLng(37.589025	,127.181044	),
                        new LatLng(37.589624	,127.181448	),
                        new LatLng(37.591717	,127.183333	),
                        new LatLng(37.591762	,127.1852	),
                        new LatLng(37.591516	,127.188905	),
                        new LatLng(37.591266	,127.192088	),
                        new LatLng(37.591131	,127.193656	),
                        new LatLng(37.591012	,127.195955	),
                        new LatLng(37.591079	,127.196486	),
                        new LatLng(37.590914	,127.196995	),
                        new LatLng(37.586113	,127.205375	),
                        new LatLng(37.584797	,127.206433	),
                        new LatLng(37.58365 	,127.20732	),
                        new LatLng(37.582902	,127.207857	),
                        new LatLng(37.579167	,127.211343	),
                        new LatLng(37.576713	,127.213591	),
                        new LatLng(37.575315	,127.215918	),
                        new LatLng(37.574137	,127.218982	),
                        new LatLng(37.573351	,127.220033	),
                        new LatLng(37.570093	,127.220048	),
                        new LatLng(37.568477	,127.219973	),
                        new LatLng(37.566475	,127.220426	),
                        new LatLng(37.566261	,127.220574	),
                        new LatLng(37.565432	,127.221487	),
                        new LatLng(37.561273	,127.230495	),
                        new LatLng(37.559874	,127.23223	),
                        new LatLng(37.559455	,127.232545	),
                        new LatLng(37.55791 	,127.233327	),
                        new LatLng(37.55725 	,127.233772	),
                        new LatLng(37.55436 	,127.235081	),
                        new LatLng(37.549997	,127.236403	),
                        new LatLng(37.549795	,127.236441	),
                        new LatLng(37.548415	,127.237028	),
                        new LatLng(37.548418	,127.237066	),
                        new LatLng(37.547215	,127.237504	),
                        new LatLng(37.545372	,127.239195	),
                        new LatLng(37.544839	,127.239764	),
                        new LatLng(37.543896	,127.240876	),
                        new LatLng(37.543155	,127.241794	),
                        new LatLng(37.542283	,127.244264	),
                        new LatLng(37.541807	,127.245756	),
                        new LatLng(37.541039	,127.247798	),
                        new LatLng(37.5405  	,127.248927	),
                        new LatLng(37.539281	,127.251801	),
                        new LatLng(37.538674	,127.253036	),
                        new LatLng(37.537702	,127.254919	),
                        new LatLng(37.53762 	,127.25536	),
                        new LatLng(37.537103	,127.256289	),
                        new LatLng(37.536879	,127.256785	),
                        new LatLng(37.536503	,127.257432	),
                        new LatLng(37.536346	,127.257971	),
                        new LatLng(37.535987	,127.258701	),
                        new LatLng(37.534906	,127.260177	),
                        new LatLng(37.533997	,127.26155	),
                        new LatLng(37.532684	,127.26364	),
                        new LatLng(37.532058	,127.264873	),
                        new LatLng(37.531617	,127.266096	),
                        new LatLng(37.531084	,127.267324	),
                        new LatLng(37.529085	,127.272337	),
                        new LatLng(37.528638	,127.273607	),
                        new LatLng(37.528318	,127.274296	),
                        new LatLng(37.527104	,127.276821	),
                        new LatLng(37.526572	,127.278157	),
                        new LatLng(37.526077	,127.278969	),
                        new LatLng(37.525817	,127.27953	),
                        new LatLng(37.5255  	,127.280229	),
                        new LatLng(37.525112	,127.281279	),
                        new LatLng(37.52398 	,127.282264	),
                        new LatLng(37.522719	,127.283312	),
                        new LatLng(37.521578	,127.28437	),
                        new LatLng(37.521411	,127.284427	),
                        new LatLng(37.521236	,127.284539	),
                        new LatLng(37.518635	,127.285264	),
                        new LatLng(37.518252	,127.285349	),
                        new LatLng(37.518007	,127.285388	),
                        new LatLng(37.517612	,127.285471	),
                        new LatLng(37.517077	,127.285577	),
                        new LatLng(37.516628	,127.285591	),
                        new LatLng(37.514398	,127.285701	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"하남시");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "하남시\n   "+hmap.get(name), new LatLng(37.523546, 127.205783));
    }//하남시
    public void drawPolygon22(GoogleMap googlemap) { //
    String name = "의왕시";
    int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

    Polygon polygon = mMap.addPolygon(new PolygonOptions()
            .add(
                    new LatLng(37.413748	,127.046843	),
                    new LatLng(37.412271	,127.04674	),
                    new LatLng(37.408299	,127.042762	),
                    new LatLng(37.406767	,127.042479	),
                    new LatLng(37.405235	,127.042749	),
                    new LatLng(37.403884	,127.043245	),
                    new LatLng(37.403425	,127.042003	),
                    new LatLng(37.402587	,127.040624	),
                    new LatLng(37.402326	,127.03998	),
                    new LatLng(37.402182	,127.038422	),
                    new LatLng(37.401759	,127.03745	),
                    new LatLng(37.39856 	,127.037991	),
                    new LatLng(37.397695	,127.038103	),
                    new LatLng(37.397245	,127.038069	),
                    new LatLng(37.396605	,127.038148	),
                    new LatLng(37.396101	,127.038227	),
                    new LatLng(37.395245	,127.038102	),
                    new LatLng(37.394767	,127.038192	),
                    new LatLng(37.393686	,127.038214	),
                    new LatLng(37.393578	,127.038237	),
                    new LatLng(37.393028	,127.038372	),
                    new LatLng(37.392226	,127.038496	),
                    new LatLng(37.391938	,127.038733	),
                    new LatLng(37.390857	,127.038439	),
                    new LatLng(37.390505	,127.038416	),
                    new LatLng(37.38968 	,127.037397	),
                    new LatLng(37.389379	,127.036846	),
                    new LatLng(37.388812	,127.035355	),
                    new LatLng(37.388664	,127.034717	),
                    new LatLng(37.388262	,127.03497	),
                    new LatLng(37.38824 	,127.034985	),
                    new LatLng(37.387971	,127.035167	),
                    new LatLng(37.386326	,127.035265	),
                    new LatLng(37.385981	,127.034925	),
                    new LatLng(37.38576 	,127.033941	),
                    new LatLng(37.385429	,127.033874	),
                    new LatLng(37.385389	,127.033862	),
                    new LatLng(37.384621	,127.03381	),
                    new LatLng(37.383903	,127.033191	),
                    new LatLng(37.383614	,127.033113	),
                    new LatLng(37.383118	,127.033134	),
                    new LatLng(37.383059	,127.0331	),
                    new LatLng(37.380362	,127.032174	),
                    new LatLng(37.380337	,127.032163	),
                    new LatLng(37.38021 	,127.03205	),
                    new LatLng(37.38003 	,127.031898	),
                    new LatLng(37.379362	,127.031398	),
                    new LatLng(37.379542	,127.029754	),
                    new LatLng(37.379551	,127.029277	),
                    new LatLng(37.379373	,127.02874	),
                    new LatLng(37.379173	,127.028192	),
                    new LatLng(37.378484	,127.028172	),
                    new LatLng(37.378286	,127.028044	),
                    new LatLng(37.377587	,127.028013	),
                    new LatLng(37.376657	,127.028345	),
                    new LatLng(37.376007	,127.028006	),
                    new LatLng(37.376006	,127.028005	),
                    new LatLng(37.375631	,127.027918	),
                    new LatLng(37.374036	,127.027898	),
                    new LatLng(37.372866	,127.027706	),
                    new LatLng(37.372547	,127.027762	),
                    new LatLng(37.372263	,127.027831	),
                    new LatLng(37.371965	,127.027913	),
                    new LatLng(37.37196 	,127.027916	),
                    new LatLng(37.36946	    ,127.027902	),
                    new LatLng(37.369376	,127.027752	),
                    new LatLng(37.359677	,127.017705	),
                    new LatLng(37.353673	,127.017163	),
                    new LatLng(37.353644	,127.017142	),
                    new LatLng(37.350952	,127.018191	),
                    new LatLng(37.349428	,127.014283	),
                    new LatLng(37.339304	,127.008901	),
                    new LatLng(37.339214	,127.004863	),
                    new LatLng(37.338792	,127.004404	),
                    new LatLng(37.338728	,127.001323	),
                    new LatLng(37.338381	,127.00048	),
                    new LatLng(37.328389	,126.984563	),
                    new LatLng(37.328321	,126.984491	),
                    new LatLng(37.326578	,126.981604	),
                    new LatLng(37.331438	,126.972335	),
                    new LatLng(37.331569	,126.971922	),
                    new LatLng(37.331779	,126.971261	),
                    new LatLng(37.326164	,126.968413	),
                    new LatLng(37.32607 	,126.968382	),
                    new LatLng(37.323951	,126.966624	),
                    new LatLng(37.319447	,126.968715	),
                    new LatLng(37.319204	,126.968622	),
                    new LatLng(37.317683	,126.966434	),
                    new LatLng(37.307796	,126.966218	),
                    new LatLng(37.30241 	,126.962034	),
                    new LatLng(37.302393	,126.961939	),
                    new LatLng(37.302373	,126.961428	),
                    new LatLng(37.302364	,126.961292	),
                    new LatLng(37.302347	,126.960868	),
                    new LatLng(37.302264	,126.960475	),
                    new LatLng(37.302235	,126.960112	),
                    new LatLng(37.302223	,126.9596	),
                    new LatLng(37.303142	,126.954363	),
                    new LatLng(37.300522	,126.947773	),
                    new LatLng(37.300537	,126.947331	),
                    new LatLng(37.300555	,126.946912	),
                    new LatLng(37.300564	,126.946765	),
                    new LatLng(37.302837	,126.93656	),
                    new LatLng(37.302861	,126.935723	),
                    new LatLng(37.303042	,126.932933	),
                    new LatLng(37.30356 	,126.932428	),
                    new LatLng(37.303637	,126.931767	),
                    new LatLng(37.305978	,126.930722	),
                    new LatLng(37.306546	,126.930586	),
                    new LatLng(37.306609	,126.930552	),
                    new LatLng(37.307221	,126.930235	),
                    new LatLng(37.307244	,126.930248	),
                    new LatLng(37.308399	,126.932907	),
                    new LatLng(37.309975	,126.93171	),
                    new LatLng(37.310417	,126.931777	),
                    new LatLng(37.313795	,126.930296	),
                    new LatLng(37.314812	,126.929855	),
                    new LatLng(37.315479	,126.929584	),
                    new LatLng(37.315632	,126.929471	),
                    new LatLng(37.316452	,126.929323	),
                    new LatLng(37.316749	,126.929312	),
                    new LatLng(37.317227	,126.92974	),
                    new LatLng(37.317234	,126.929756	),
                    new LatLng(37.317615	,126.930597	),
                    new LatLng(37.317615	,126.930608	),
                    new LatLng(37.317931	,126.931138	),
                    new LatLng(37.318138	,126.931589	),
                    new LatLng(37.318341	,126.931946	),
                    new LatLng(37.31849 	,126.932322	),
                    new LatLng(37.318589	,126.932582	),
                    new LatLng(37.318968	,126.932886	),
                    new LatLng(37.319806	,126.933348	),
                    new LatLng(37.320365	,126.93434	),
                    new LatLng(37.320735	,126.935028	),
                    new LatLng(37.321871	,126.935602	),
                    new LatLng(37.321952	,126.935625	),
                    new LatLng(37.322321	,126.935681	),
                    new LatLng(37.322484	,126.935658	),
                    new LatLng(37.322565	,126.935692	),
                    new LatLng(37.322601	,126.935692	),
                    new LatLng(37.322754	,126.935692	),
                    new LatLng(37.322826	,126.935714	),
                    new LatLng(37.322853	,126.935793	),
                    new LatLng(37.322862	,126.935906	),
                    new LatLng(37.322898	,126.936154	),
                    new LatLng(37.322925	,126.936346	),
                    new LatLng(37.322916	,126.93647	),
                    new LatLng(37.322989	,126.936696	),
                    new LatLng(37.32298 	,126.936921	),
                    new LatLng(37.322971	,126.937012	),
                    new LatLng(37.322953	,126.937406	),
                    new LatLng(37.322944	,126.93753	),
                    new LatLng(37.322953	,126.937632	),
                    new LatLng(37.322854	,126.938095	),
                    new LatLng(37.322503	,126.938772	),
                    new LatLng(37.322143	,126.939517	),
                    new LatLng(37.322116	,126.939607	),
                    new LatLng(37.32199 	,126.939991	),
                    new LatLng(37.321747	,126.940352	),
                    new LatLng(37.321774	,126.940769	),
                    new LatLng(37.321738	,126.940916	),
                    new LatLng(37.321622	,126.941345	),
                    new LatLng(37.321372	,126.941971	),
                    new LatLng(37.32083 	,126.943433	),
                    new LatLng(37.320734	,126.943636	),
                    new LatLng(37.320659	,126.943794	),
                    new LatLng(37.320515	,126.944042	),
                    new LatLng(37.320353	,126.94508	),
                    new LatLng(37.320344	,126.945103	),
                    new LatLng(37.320263	,126.945193	),
                    new LatLng(37.32056 	,126.945306	),
                    new LatLng(37.320795	,126.945317	),
                    new LatLng(37.320894	,126.945283	),
                    new LatLng(37.321047	,126.945294	),
                    new LatLng(37.321254	,126.945215	),
                    new LatLng(37.321416	,126.94526	),
                    new LatLng(37.321822	,126.945316	),
                    new LatLng(37.321903	,126.945226	),
                    new LatLng(37.322209	,126.94535	),
                    new LatLng(37.322417	,126.94553	),
                    new LatLng(37.322588	,126.945981	),
                    new LatLng(37.322561	,126.94606	),
                    new LatLng(37.322602	,126.946173	),
                    new LatLng(37.322678	,126.946241	),
                    new LatLng(37.32284 	,126.946359	),
                    new LatLng(37.323084	,126.946534	),
                    new LatLng(37.323642	,126.946691	),
                    new LatLng(37.323778	,126.946725	),
                    new LatLng(37.324165	,126.946849	),
                    new LatLng(37.324363	,126.94686	),
                    new LatLng(37.324607	,126.946826	),
                    new LatLng(37.324661	,126.946905	),
                    new LatLng(37.324796	,126.946882	),
                    new LatLng(37.325057	,126.946848	),
                    new LatLng(37.32549 	,126.946904	),
                    new LatLng(37.325535	,126.946916	),
                    new LatLng(37.325535	,126.946927	),
                    new LatLng(37.325706	,126.946848	),
                    new LatLng(37.325742	,126.946758	),
                    new LatLng(37.325805	,126.946701	),
                    new LatLng(37.325872	,126.94665	),
                    new LatLng(37.325994	,126.946577	),
                    new LatLng(37.326273	,126.946622	),
                    new LatLng(37.3263  	,126.94661	),
                    new LatLng(37.326346	,126.946622	),
                    new LatLng(37.326371	,126.946622	),
                    new LatLng(37.326337	,126.946644	),
                    new LatLng(37.326111	,126.946656	),
                    new LatLng(37.326204	,126.947208	),
                    new LatLng(37.326202	,126.947211	),
                    new LatLng(37.326211	,126.947231	),
                    new LatLng(37.326263	,126.947328	),
                    new LatLng(37.326332	,126.947366	),
                    new LatLng(37.326301	,126.947412	),
                    new LatLng(37.326391	,126.947524	),
                    new LatLng(37.326427	,126.947558	),
                    new LatLng(37.326616	,126.947569	),
                    new LatLng(37.326643	,126.947569	),
                    new LatLng(37.326688	,126.947716	),
                    new LatLng(37.327202	,126.948043	),
                    new LatLng(37.327725	,126.948065	),
                    new LatLng(37.328707	,126.948042	),
                    new LatLng(37.328833	,126.94803	),
                    new LatLng(37.330347	,126.948131	),
                    new LatLng(37.331149	,126.948153	),
                    new LatLng(37.331338	,126.948141	),
                    new LatLng(37.331951	,126.948169	),
                    new LatLng(37.332392	,126.948186	),
                    new LatLng(37.33282 	,126.948174	),
                    new LatLng(37.332933	,126.948163	),
                    new LatLng(37.333392	,126.948219	),
                    new LatLng(37.334032	,126.948151	),
                    new LatLng(37.334194	,126.948151	),
                    new LatLng(37.334194	,126.948184	),
                    new LatLng(37.334365	,126.948241	),
                    new LatLng(37.334654	,126.948291	),
                    new LatLng(37.335042	,126.948281	),
                    new LatLng(37.335203	,126.948263	),
                    new LatLng(37.335501	,126.948268	),
                    new LatLng(37.335627	,126.948274	),
                    new LatLng(37.335807	,126.948285	),
                    new LatLng(37.335861	,126.948285	),
                    new LatLng(37.335853	,126.948307	),
                    new LatLng(37.335888	,126.948285	),
                    new LatLng(37.336041	,126.948307	),
                    new LatLng(37.336636	,126.948363	),
                    new LatLng(37.337699	,126.948509	),
                    new LatLng(37.338033	,126.948441	),
                    new LatLng(37.33878 	,126.948362	),
                    new LatLng(37.339231	,126.948361	),
                    new LatLng(37.339222	,126.948316	),
                    new LatLng(37.339195	,126.948215	),
                    new LatLng(37.33924 	,126.948316	),
                    new LatLng(37.339524	,126.948316	),
                    new LatLng(37.339857	,126.94831	),
                    new LatLng(37.340141	,126.948304	),
                    new LatLng(37.340321	,126.948316	),
                    new LatLng(37.340258	,126.948564	),
                    new LatLng(37.340267	,126.948688	),
                    new LatLng(37.3402  	,126.948993	),
                    new LatLng(37.340673	,126.949658	),
                    new LatLng(37.340871	,126.949805	),
                    new LatLng(37.341169	,126.950019	),
                    new LatLng(37.341818	,126.95047	),
                    new LatLng(37.341764	,126.950617	),
                    new LatLng(37.341656	,126.950933	),
                    new LatLng(37.34117 	,126.952208	),
                    new LatLng(37.340918	,126.952897	),
                    new LatLng(37.340819	,126.953157	),
                    new LatLng(37.340819	,126.953258	),
                    new LatLng(37.340972	,126.953495	),
                    new LatLng(37.341022	,126.953591	),
                    new LatLng(37.341098	,126.953732	),
                    new LatLng(37.341107	,126.953732	),
                    new LatLng(37.341189	,126.954025	),
                    new LatLng(37.341279	,126.954341	),
                    new LatLng(37.341432	,126.954533	),
                    new LatLng(37.341612	,126.954612	),
                    new LatLng(37.341811	,126.954792	),
                    new LatLng(37.342167	,126.955198	),
                    new LatLng(37.342288	,126.955356	),
                    new LatLng(37.342523	,126.955514	),
                    new LatLng(37.342901	,126.955638	),
                    new LatLng(37.343181	,126.955728	),
                    new LatLng(37.343478	,126.955649	),
                    new LatLng(37.344181	,126.957251	),
                    new LatLng(37.344731	,126.95777	),
                    new LatLng(37.345272	,126.957939	),
                    new LatLng(37.345605	,126.958029	),
                    new LatLng(37.346362	,126.958181	),
                    new LatLng(37.34711 	,126.958355	),
                    new LatLng(37.347122	,126.958356	),
                    new LatLng(37.347281	,126.958366	),
                    new LatLng(37.347299	,126.958366	),
                    new LatLng(37.347525	,126.958705	),
                    new LatLng(37.347696	,126.958908	),
                    new LatLng(37.34784 	,126.959145	),
                    new LatLng(37.348029	,126.959325	),
                    new LatLng(37.348192	,126.959517	),
                    new LatLng(37.349688	,126.961119	),
                    new LatLng(37.350346	,126.961288	),
                    new LatLng(37.351724	,126.961298	),
                    new LatLng(37.351877	,126.961377	),
                    new LatLng(37.352094	,126.961524	),
                    new LatLng(37.352229	,126.961546	),
                    new LatLng(37.352355	,126.961546	),
                    new LatLng(37.352463	,126.961546	),
                    new LatLng(37.35276 	,126.961546	),
                    new LatLng(37.352797	,126.961535	),
                    new LatLng(37.353328	,126.961467	),
                    new LatLng(37.35358 	,126.961455	),
                    new LatLng(37.353607	,126.961444	),
                    new LatLng(37.353851	,126.961421	),
                    new LatLng(37.353968	,126.961489	),
                    new LatLng(37.354436	,126.961816	),
                    new LatLng(37.354545	,126.961884	),
                    new LatLng(37.354572	,126.961895	),
                    new LatLng(37.354725	,126.961963	),
                    new LatLng(37.354941	,126.962166	),
                    new LatLng(37.355194	,126.962414	),
                    new LatLng(37.35541  	,126.962233	),
                    new LatLng(37.355482	,126.962143	),
                    new LatLng(37.355689	,126.961748	),
                    new LatLng(37.355779	,126.96177	),
                    new LatLng(37.355968	,126.961759	),
                    new LatLng(37.355995	,126.961703	),
                    new LatLng(37.356166	,126.961341	),
                    new LatLng(37.35649 	,126.960709	),
                    new LatLng(37.356562	,126.960573	),
                    new LatLng(37.356788	,126.960731	),
                    new LatLng(37.357076	,126.960991	),
                    new LatLng(37.357202	,126.96107	),
                    new LatLng(37.357193	,126.961103	),
                    new LatLng(37.357211	,126.961115	),
                    new LatLng(37.357247	,126.961081	),
                    new LatLng(37.357274	,126.961058	),
                    new LatLng(37.357887	,126.961634	),
                    new LatLng(37.358464	,126.961724	),
                    new LatLng(37.358617	,126.961712	),
                    new LatLng(37.358896	,126.961633	),
                    new LatLng(37.359203	,126.961396	),
                    new LatLng(37.35923 	,126.961396	),
                    new LatLng(37.359275	,126.961396	),
                    new LatLng(37.359653	,126.961565	),
                    new LatLng(37.359825	,126.961655	),
                    new LatLng(37.360023	,126.961802	),
                    new LatLng(37.360149	,126.961892	),
                    new LatLng(37.36032 	,126.961926	),
                    new LatLng(37.361005	,126.961959	),
                    new LatLng(37.361257	,126.961993	),
                    new LatLng(37.361492	,126.962061	),
                    new LatLng(37.361635	,126.96206	),
                    new LatLng(37.361762	,126.962252	),
                    new LatLng(37.361681	,126.962332	),
                    new LatLng(37.361906	,126.962602	),
                    new LatLng(37.361942	,126.962794	),
                    new LatLng(37.362826	,126.966169	),
                    new LatLng(37.362944	,126.966157	),
                    new LatLng(37.364997	,126.964384	),
                    new LatLng(37.373801	,126.966547	),
                    new LatLng(37.374206	,126.966773	),
                    new LatLng(37.37563 	,126.967743	),
                    new LatLng(37.376099	,126.968093	),
                    new LatLng(37.38065 	,126.97254	),
                    new LatLng(37.389725	,126.975178	),
                    new LatLng(37.389733	,126.975201	),
                    new LatLng(37.390698	,126.977132	),
                    new LatLng(37.390734	,126.977188	),
                    new LatLng(37.390878	,126.977392	),
                    new LatLng(37.390891	,126.977404	),
                    new LatLng(37.394807	,126.982732	),
                    new LatLng(37.396167	,126.97975	),
                    new LatLng(37.400685	,126.982371	),
                    new LatLng(37.400688	,126.982365	),
                    new LatLng(37.401115	,126.982662	),
                    new LatLng(37.401404	,126.983065	),
                    new LatLng(37.401816	,126.983201	),
                    new LatLng(37.401868	,126.983243	),
                    new LatLng(37.40186 	,126.983252	),
                    new LatLng(37.401878	,126.983295	),
                    new LatLng(37.40192 	,126.983499	),
                    new LatLng(37.401927	,126.983554	),
                    new LatLng(37.401953	,126.983628	),
                    new LatLng(37.401994	,126.983674	),
                    new LatLng(37.402088	,126.983769	),
                    new LatLng(37.402105	,126.983804	),
                    new LatLng(37.402174	,126.983925	),
                    new LatLng(37.402214	,126.984041	),
                    new LatLng(37.402216	,126.984143	),
                    new LatLng(37.402326	,126.984385	),
                    new LatLng(37.402352	,126.98448	),
                    new LatLng(37.402475	,126.984611	),
                    new LatLng(37.402521	,126.984645	),
                    new LatLng(37.402612	,126.984657	),
                    new LatLng(37.402683	,126.984704	),
                    new LatLng(37.402945	,126.984849	),
                    new LatLng(37.403151	,126.984944	),
                    new LatLng(37.403844	,126.984964	),
                    new LatLng(37.403926	,126.984932	),
                    new LatLng(37.404295	,126.984921	),
                    new LatLng(37.404403	,126.984894	),
                    new LatLng(37.404557	,126.984908	),
                    new LatLng(37.404744	,126.9849	),
                    new LatLng(37.404755	,126.984899	),
                    new LatLng(37.404802	,126.98489	),
                    new LatLng(37.404818	,126.984892	),
                    new LatLng(37.404907	,126.984897	),
                    new LatLng(37.405143	,126.984973	),
                    new LatLng(37.405209	,126.985015	),
                    new LatLng(37.405305	,126.985074	),
                    new LatLng(37.405845	,126.985158	),
                    new LatLng(37.405868	,126.985177	),
                    new LatLng(37.405962	,126.985271	),
                    new LatLng(37.406094	,126.985378	),
                    new LatLng(37.406201	,126.985451	),
                    new LatLng(37.406285	,126.985502	),
                    new LatLng(37.406345	,126.985547	),
                    new LatLng(37.406393	,126.985611	),
                    new LatLng(37.406507	,126.985713	),
                    new LatLng(37.406744	,126.98591	),
                    new LatLng(37.406812	,126.986071	),
                    new LatLng(37.406849	,126.986172	),
                    new LatLng(37.40691 	,126.986322	),
                    new LatLng(37.406932	,126.986368	),
                    new LatLng(37.406998	,126.98648	),
                    new LatLng(37.407024	,126.986514	),
                    new LatLng(37.407078	,126.986606	),
                    new LatLng(37.407094	,126.986634	),
                    new LatLng(37.407161	,126.986795	),
                    new LatLng(37.407443	,126.987249	),
                    new LatLng(37.407538	,126.987362	),
                    new LatLng(37.407566	,126.9874	),
                    new LatLng(37.407666	,126.987529	),
                    new LatLng(37.407776	,126.987806	),
                    new LatLng(37.407833	,126.987865	),
                    new LatLng(37.407847	,126.98833	),
                    new LatLng(37.407736	,126.988448	),
                    new LatLng(37.408211	,126.989974	),
                    new LatLng(37.407879	,126.990827	),
                    new LatLng(37.407903	,126.990972	),
                    new LatLng(37.4079  	,126.991008	),
                    new LatLng(37.40789 	,126.991004	),
                    new LatLng(37.407828	,126.991228	),
                    new LatLng(37.4078  	,126.991363	),
                    new LatLng(37.407718	,126.991732	),
                    new LatLng(37.407642	,126.99208	),
                    new LatLng(37.406711	,126.992369	),
                    new LatLng(37.406697	,126.992736	),
                    new LatLng(37.406091	,126.99338	),
                    new LatLng(37.405474	,126.993717	),
                    new LatLng(37.405043	,126.994254	),
                    new LatLng(37.405008	,126.994273	),
                    new LatLng(37.405032	,126.994627	),
                    new LatLng(37.405125	,126.994993	),
                    new LatLng(37.405247	,126.99569	),
                    new LatLng(37.405084	,126.996022	),
                    new LatLng(37.405071	,126.996059	),
                    new LatLng(37.405062	,126.996379	),
                    new LatLng(37.404882	,126.996808	),
                    new LatLng(37.404932	,126.997164	),
                    new LatLng(37.404785	,126.997692	),
                    new LatLng(37.404765	,126.997827	),
                    new LatLng(37.404883	,126.998545	),
                    new LatLng(37.404796	,126.999157	),
                    new LatLng(37.404616	,126.999683	),
                    new LatLng(37.405253	,127.000461	),
                    new LatLng(37.405974	,127.001062	),
                    new LatLng(37.406336	,127.001498	),
                    new LatLng(37.406333	,127.001512	),
                    new LatLng(37.406684	,127.001942	),
                    new LatLng(37.406963	,127.00289	),
                    new LatLng(37.407396	,127.003681	),
                    new LatLng(37.407594	,127.004291	),
                    new LatLng(37.407864	,127.004958	),
                    new LatLng(37.409369	,127.007126	),
                    new LatLng(37.410099	,127.006957	),
                    new LatLng(37.410414	,127.00716	),
                    new LatLng(37.412306	,127.00899	),
                    new LatLng(37.412964	,127.013791	),
                    new LatLng(37.413874	,127.013317	),
                    new LatLng(37.413513	,127.015621	),
                    new LatLng(37.413666	,127.017394	),
                    new LatLng(37.413567	,127.017643	),
                    new LatLng(37.413098	,127.018174	),
                    new LatLng(37.413116	,127.018592	),
                    new LatLng(37.412503	,127.020907	),
                    new LatLng(37.412314	,127.021811	),
                    new LatLng(37.412431	,127.022218	),
                    new LatLng(37.412385	,127.025143	),
                    new LatLng(37.411988	,127.027876	),
                    new LatLng(37.412132	,127.028554	),
                    new LatLng(37.412321	,127.028792	),
                    new LatLng(37.412303	,127.028983	),
                    new LatLng(37.412546	,127.029379	),
                    new LatLng(37.412727	,127.029695	),
                    new LatLng(37.412997	,127.030769	),
                    new LatLng(37.412942	,127.031977	),
                    new LatLng(37.41341 	,127.033084	),
                    new LatLng(37.413834	,127.034282	),
                    new LatLng(37.413996	,127.035016	),
                    new LatLng(37.413724	,127.038224	),
                    new LatLng(37.414193	,127.038981	),
                    new LatLng(37.414589	,127.040111	),
                    new LatLng(37.415003	,127.041263	),
                    new LatLng(37.415552	,127.041727	),
                    new LatLng(37.415613	,127.041897	),
                    new LatLng(37.415539	,127.042077	),
                    new LatLng(37.415408	,127.04298	),
                    new LatLng(37.414804	,127.043974	),
                    new LatLng(37.414308	,127.0449	),
                    new LatLng(37.413748	,127.046843	)
                    )
            .strokeColor(Color.WHITE)             .strokeWidth(2)
            .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),"의왕시");
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, "의왕시\n   "+hmap.get(name), new LatLng(37.363525, 126.990049));
}//의왕시
    public void drawPolygon32(GoogleMap googlemap) { //
        String name = "군포시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.361942	,126.962794	),
                        new LatLng(37.361906	,126.962602	),
                        new LatLng(37.361681	,126.962332	),
                        new LatLng(37.361762	,126.962252	),
                        new LatLng(37.361492	,126.962061	),
                        new LatLng(37.361257	,126.961993	),
                        new LatLng(37.361005	,126.961959	),
                        new LatLng(37.36032 	,126.961926	),
                        new LatLng(37.360149	,126.961892	),
                        new LatLng(37.360023	,126.961802	),
                        new LatLng(37.359825	,126.961655	),
                        new LatLng(37.359653	,126.961565	),
                        new LatLng(37.359275	,126.961396	),
                        new LatLng(37.35923 	,126.961396	),
                        new LatLng(37.359203	,126.961396	),
                        new LatLng(37.358896	,126.961633	),
                        new LatLng(37.358617	,126.961712	),
                        new LatLng(37.358464	,126.961724	),
                        new LatLng(37.357887	,126.961634	),
                        new LatLng(37.357274	,126.961058	),
                        new LatLng(37.357247	,126.961081	),
                        new LatLng(37.357211	,126.961115	),
                        new LatLng(37.357193	,126.961103	),
                        new LatLng(37.357202	,126.96107	),
                        new LatLng(37.357076	,126.960991	),
                        new LatLng(37.356788	,126.960731	),
                        new LatLng(37.356562	,126.960573	),
                        new LatLng(37.35649 	,126.960709	),
                        new LatLng(37.356166	,126.961341	),
                        new LatLng(37.355995	,126.961703	),
                        new LatLng(37.355968	,126.961759	),
                        new LatLng(37.355779	,126.96177	),
                        new LatLng(37.355689	,126.961748	),
                        new LatLng(37.355482	,126.962143	),
                        new LatLng(37.35541 	,126.962233	),
                        new LatLng(37.355194	,126.962414	),
                        new LatLng(37.354941	,126.962166	),
                        new LatLng(37.354725	,126.961963	),
                        new LatLng(37.354572	,126.961895	),
                        new LatLng(37.354545	,126.961884	),
                        new LatLng(37.354436	,126.961816	),
                        new LatLng(37.353968	,126.961489	),
                        new LatLng(37.353851	,126.961421	),
                        new LatLng(37.353607	,126.961444	),
                        new LatLng(37.35358 	,126.961455	),
                        new LatLng(37.353328	,126.961467	),
                        new LatLng(37.352797	,126.961535	),
                        new LatLng(37.35276 	,126.961546	),
                        new LatLng(37.352463	,126.961546	),
                        new LatLng(37.352355	,126.961546	),
                        new LatLng(37.352229	,126.961546	),
                        new LatLng(37.352094	,126.961524	),
                        new LatLng(37.351877	,126.961377	),
                        new LatLng(37.351724	,126.961298	),
                        new LatLng(37.350346	,126.961288	),
                        new LatLng(37.349688	,126.961119	),
                        new LatLng(37.348192	,126.959517	),
                        new LatLng(37.348029	,126.959325	),
                        new LatLng(37.34784 	,126.959145	),
                        new LatLng(37.347696	,126.958908	),
                        new LatLng(37.347525	,126.958705	),
                        new LatLng(37.347299	,126.958366	),
                        new LatLng(37.347281	,126.958366	),
                        new LatLng(37.347122	,126.958356	),
                        new LatLng(37.34711 	,126.958355	),
                        new LatLng(37.346362	,126.958181	),
                        new LatLng(37.345605	,126.958029	),
                        new LatLng(37.345272	,126.957939	),
                        new LatLng(37.344731	,126.95777	),
                        new LatLng(37.344181	,126.957251	),
                        new LatLng(37.343478	,126.955649	),
                        new LatLng(37.343181	,126.955728	),
                        new LatLng(37.342901	,126.955638	),
                        new LatLng(37.342523	,126.955514	),
                        new LatLng(37.342288	,126.955356	),
                        new LatLng(37.342167	,126.955198	),
                        new LatLng(37.341811	,126.954792	),
                        new LatLng(37.341612	,126.954612	),
                        new LatLng(37.341432	,126.954533	),
                        new LatLng(37.341279	,126.954341	),
                        new LatLng(37.341189	,126.954025	),
                        new LatLng(37.341107	,126.953732	),
                        new LatLng(37.341098	,126.953732	),
                        new LatLng(37.341022	,126.953591	),
                        new LatLng(37.340972	,126.953495	),
                        new LatLng(37.340819	,126.953258	),
                        new LatLng(37.340819	,126.953157	),
                        new LatLng(37.340918	,126.952897	),
                        new LatLng(37.34117 	,126.952208	),
                        new LatLng(37.341656	,126.950933	),
                        new LatLng(37.341764	,126.950617	),
                        new LatLng(37.341818	,126.95047	),
                        new LatLng(37.341169	,126.950019	),
                        new LatLng(37.340871	,126.949805	),
                        new LatLng(37.340673	,126.949658	),
                        new LatLng(37.3402  	,126.948993	),
                        new LatLng(37.340267	,126.948688	),
                        new LatLng(37.340258	,126.948564	),
                        new LatLng(37.340321	,126.948316	),
                        new LatLng(37.340141	,126.948304	),
                        new LatLng(37.339857	,126.94831	),
                        new LatLng(37.339524	,126.948316	),
                        new LatLng(37.33924 	,126.948316	),
                        new LatLng(37.339195	,126.948215	),
                        new LatLng(37.339222	,126.948316	),
                        new LatLng(37.339231	,126.948361	),
                        new LatLng(37.33878 	,126.948362	),
                        new LatLng(37.338033	,126.948441	),
                        new LatLng(37.337699	,126.948509	),
                        new LatLng(37.336636	,126.948363	),
                        new LatLng(37.336041	,126.948307	),
                        new LatLng(37.335888	,126.948285	),
                        new LatLng(37.335853	,126.948307	),
                        new LatLng(37.335861	,126.948285	),
                        new LatLng(37.335807	,126.948285	),
                        new LatLng(37.335627	,126.948274	),
                        new LatLng(37.335501	,126.948268	),
                        new LatLng(37.335203	,126.948263	),
                        new LatLng(37.335042	,126.948281	),
                        new LatLng(37.334654	,126.948291	),
                        new LatLng(37.334365	,126.948241	),
                        new LatLng(37.334194	,126.948184	),
                        new LatLng(37.334194	,126.948151	),
                        new LatLng(37.334032	,126.948151	),
                        new LatLng(37.333392	,126.948219	),
                        new LatLng(37.332933	,126.948163	),
                        new LatLng(37.33282 	,126.948174	),
                        new LatLng(37.332392	,126.948186	),
                        new LatLng(37.331951	,126.948169	),
                        new LatLng(37.331338	,126.948141	),
                        new LatLng(37.331149	,126.948153	),
                        new LatLng(37.330347	,126.948131	),
                        new LatLng(37.328833	,126.94803	),
                        new LatLng(37.328707	,126.948042	),
                        new LatLng(37.327725	,126.948065	),
                        new LatLng(37.327202	,126.948043	),
                        new LatLng(37.326688	,126.947716	),
                        new LatLng(37.326643	,126.947569	),
                        new LatLng(37.326616	,126.947569	),
                        new LatLng(37.326427	,126.947558	),
                        new LatLng(37.326391	,126.947524	),
                        new LatLng(37.326301	,126.947412	),
                        new LatLng(37.326332	,126.947366	),
                        new LatLng(37.326263	,126.947328	),
                        new LatLng(37.326211	,126.947231	),
                        new LatLng(37.326202	,126.947211	),
                        new LatLng(37.326204	,126.947208	),
                        new LatLng(37.326111	,126.946656	),
                        new LatLng(37.326337	,126.946644	),
                        new LatLng(37.326371	,126.946622	),
                        new LatLng(37.326346	,126.946622	),
                        new LatLng(37.3263  	,126.94661	),
                        new LatLng(37.326273	,126.946622	),
                        new LatLng(37.325994	,126.946577	),
                        new LatLng(37.325872	,126.94665	),
                        new LatLng(37.325805	,126.946701	),
                        new LatLng(37.325742	,126.946758	),
                        new LatLng(37.325706	,126.946848	),
                        new LatLng(37.325535	,126.946927	),
                        new LatLng(37.325535	,126.946916	),
                        new LatLng(37.32549 	,126.946904	),
                        new LatLng(37.325057	,126.946848	),
                        new LatLng(37.324796	,126.946882	),
                        new LatLng(37.324661	,126.946905	),
                        new LatLng(37.324607	,126.946826	),
                        new LatLng(37.324363	,126.94686	),
                        new LatLng(37.324165	,126.946849	),
                        new LatLng(37.323778	,126.946725	),
                        new LatLng(37.323642	,126.946691	),
                        new LatLng(37.323084	,126.946534	),
                        new LatLng(37.32284 	,126.946359	),
                        new LatLng(37.322678	,126.946241	),
                        new LatLng(37.322602	,126.946173	),
                        new LatLng(37.322561	,126.94606	),
                        new LatLng(37.322588	,126.945981	),
                        new LatLng(37.322417	,126.94553	),
                        new LatLng(37.322209	,126.94535	),
                        new LatLng(37.321903	,126.945226	),
                        new LatLng(37.321822	,126.945316	),
                        new LatLng(37.321416	,126.94526	),
                        new LatLng(37.321254	,126.945215	),
                        new LatLng(37.321047	,126.945294	),
                        new LatLng(37.320894	,126.945283	),
                        new LatLng(37.320795	,126.945317	),
                        new LatLng(37.32056 	,126.945306	),
                        new LatLng(37.320263	,126.945193	),
                        new LatLng(37.320344	,126.945103	),
                        new LatLng(37.320353	,126.94508	),
                        new LatLng(37.320515	,126.944042	),
                        new LatLng(37.320659	,126.943794	),
                        new LatLng(37.320734	,126.943636	),
                        new LatLng(37.32083 	,126.943433	),
                        new LatLng(37.321372	,126.941971	),
                        new LatLng(37.321622	,126.941345	),
                        new LatLng(37.321738	,126.940916	),
                        new LatLng(37.321774	,126.940769	),
                        new LatLng(37.321747	,126.940352	),
                        new LatLng(37.32199 	,126.939991	),
                        new LatLng(37.322116	,126.939607	),
                        new LatLng(37.322143	,126.939517	),
                        new LatLng(37.322503	,126.938772	),
                        new LatLng(37.322854	,126.938095	),
                        new LatLng(37.322953	,126.937632	),
                        new LatLng(37.322944	,126.93753	),
                        new LatLng(37.322953	,126.937406	),
                        new LatLng(37.322971	,126.937012	),
                        new LatLng(37.32298 	,126.936921	),
                        new LatLng(37.322989	,126.936696	),
                        new LatLng(37.322916	,126.93647	),
                        new LatLng(37.322925	,126.936346	),
                        new LatLng(37.322898	,126.936154	),
                        new LatLng(37.322862	,126.935906	),
                        new LatLng(37.322853	,126.935793	),
                        new LatLng(37.322826	,126.935714	),
                        new LatLng(37.322754	,126.935692	),
                        new LatLng(37.322601	,126.935692	),
                        new LatLng(37.322565	,126.935692	),
                        new LatLng(37.322484	,126.935658	),
                        new LatLng(37.322321	,126.935681	),
                        new LatLng(37.321952	,126.935625	),
                        new LatLng(37.321871	,126.935602	),
                        new LatLng(37.320735	,126.935028	),
                        new LatLng(37.320365	,126.93434	),
                        new LatLng(37.319806	,126.933348	),
                        new LatLng(37.318968	,126.932886	),
                        new LatLng(37.318589	,126.932582	),
                        new LatLng(37.31849 	,126.932322	),
                        new LatLng(37.318341	,126.931946	),
                        new LatLng(37.318138	,126.931589	),
                        new LatLng(37.317931	,126.931138	),
                        new LatLng(37.317615	,126.930608	),
                        new LatLng(37.317615	,126.930597	),
                        new LatLng(37.317234	,126.929756	),
                        new LatLng(37.317227	,126.92974	),
                        new LatLng(37.316749	,126.929312	),
                        new LatLng(37.316452	,126.929323	),
                        new LatLng(37.315632	,126.929471	),
                        new LatLng(37.315479	,126.929584	),
                        new LatLng(37.314812	,126.929855	),
                        new LatLng(37.313795	,126.930296	),
                        new LatLng(37.310417	,126.931777	),
                        new LatLng(37.309975	,126.93171	),
                        new LatLng(37.308399	,126.932907	),
                        new LatLng(37.307244	,126.930248	),
                        new LatLng(37.307222	,126.930236	),
                        new LatLng(37.307221	,126.930235	),
                        new LatLng(37.30554 	,126.914964	),
                        new LatLng(37.315568	,126.915723	),
                        new LatLng(37.315677	,126.915742	),
                        new LatLng(37.316673	,126.910235	),
                        new LatLng(37.318341	,126.911846	),
                        new LatLng(37.321357	,126.908289	),
                        new LatLng(37.321827	,126.907635	),
                        new LatLng(37.317644	,126.887355	),
                        new LatLng(37.321064	,126.882713	),
                        new LatLng(37.319572	,126.878011	),
                        new LatLng(37.321082	,126.874285	),
                        new LatLng(37.329217	,126.873437	),
                        new LatLng(37.334393	,126.881793	),
                        new LatLng(37.334515	,126.881586	),
                        new LatLng(37.339916	,126.88608	),
                        new LatLng(37.346936	,126.886893	),
                        new LatLng(37.359451	,126.896412	),
                        new LatLng(37.359485	,126.896379	),
                        new LatLng(37.359489	,126.896383	),
                        new LatLng(37.359294	,126.896904	),
                        new LatLng(37.359029	,126.898564	),
                        new LatLng(37.359483	,126.90213	),
                        new LatLng(37.359956	,126.903775	),
                        new LatLng(37.373374	,126.913848	),
                        new LatLng(37.374608	,126.914412	),
                        new LatLng(37.374699	,126.914453	),
                        new LatLng(37.375682	,126.915785	),
                        new LatLng(37.375803	,126.916202	),
                        new LatLng(37.375945	,126.916696	),
                        new LatLng(37.377032	,126.91927	),
                        new LatLng(37.37709 	,126.923509	),
                        new LatLng(37.377248	,126.924406	),
                        new LatLng(37.377355	,126.925294	),
                        new LatLng(37.377363	,126.927703	),
                        new LatLng(37.377365	,126.92835	),
                        new LatLng(37.377416	,126.929146	),
                        new LatLng(37.377451	,126.929627	),
                        new LatLng(37.377546	,126.930928	),
                        new LatLng(37.377609	,126.93179	),
                        new LatLng(37.377526	,126.932719	),
                        new LatLng(37.377233	,126.933336	),
                        new LatLng(37.376882	,126.934074	),
                        new LatLng(37.37641 	,126.937675	),
                        new LatLng(37.376542	,126.938518	),
                        new LatLng(37.378629	,126.941815	),
                        new LatLng(37.378526	,126.942221	),
                        new LatLng(37.378394	,126.942638	),
                        new LatLng(37.378366	,126.942726	),
                        new LatLng(37.37832 	,126.942883	),
                        new LatLng(37.378284	,126.943028	),
                        new LatLng(37.378145	,126.943463	),
                        new LatLng(37.378014	,126.943798	),
                        new LatLng(37.377153	,126.944739	),
                        new LatLng(37.377124	,126.944754	),
                        new LatLng(37.376946	,126.944841	),
                        new LatLng(37.376661	,126.944985	),
                        new LatLng(37.376647	,126.945015	),
                        new LatLng(37.376646	,126.945017	),
                        new LatLng(37.376652	,126.945027	),
                        new LatLng(37.375026	,126.94681	),
                        new LatLng(37.374973	,126.946674	),
                        new LatLng(37.374951	,126.946625	),
                        new LatLng(37.37493 	,126.946275	),
                        new LatLng(37.374927	,126.946267	),
                        new LatLng(37.374342	,126.945649	),
                        new LatLng(37.374234	,126.945632	),
                        new LatLng(37.374229	,126.945632	),
                        new LatLng(37.374032	,126.945619	),
                        new LatLng(37.373606	,126.945811	),
                        new LatLng(37.373307	,126.945739	),
                        new LatLng(37.373078	,126.94581	),
                        new LatLng(37.373005	,126.945863	),
                        new LatLng(37.3727  	,126.946096	),
                        new LatLng(37.372486	,126.946246	),
                        new LatLng(37.372338	,126.946323	),
                        new LatLng(37.371686	,126.946802	),
                        new LatLng(37.371016	,126.947289	),
                        new LatLng(37.370496	,126.947395	),
                        new LatLng(37.369882	,126.947644	),
                        new LatLng(37.369311	,126.947932	),
                        new LatLng(37.369112	,126.948391	),
                        new LatLng(37.368651	,126.949412	),
                        new LatLng(37.368447	,126.949732	),
                        new LatLng(37.368064	,126.94995	),
                        new LatLng(37.367722	,126.950324	),
                        new LatLng(37.367371	,126.950717	),
                        new LatLng(37.366945	,126.950895	),
                        new LatLng(37.366737	,126.950799	),
                        new LatLng(37.366534	,126.951037	),
                        new LatLng(37.366624	,126.951028	),
                        new LatLng(37.366661	,126.95104	),
                        new LatLng(37.366708	,126.951056	),
                        new LatLng(37.366175	,126.953789	),
                        new LatLng(37.366179	,126.953842	),
                        new LatLng(37.366204	,126.955986	),
                        new LatLng(37.366186	,126.956054	),
                        new LatLng(37.366147	,126.956179	),
                        new LatLng(37.366131	,126.956231	),
                        new LatLng(37.366128	,126.956254	),
                        new LatLng(37.366124	,126.956286	),
                        new LatLng(37.365987	,126.956904	),
                        new LatLng(37.364431	,126.95879	),
                        new LatLng(37.363998	,126.959293	),
                        new LatLng(37.363819	,126.959501	),
                        new LatLng(37.36353 	,126.959886	),
                        new LatLng(37.36307 	,126.960447	),
                        new LatLng(37.363007	,126.960516	),
                        new LatLng(37.362702	,126.960843	),
                        new LatLng(37.361942	,126.962794	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.343961, 126.921249));
    }//군포시
    public void drawPolygon59(GoogleMap googlemap) { //
        String name = "남양주시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.677263	,127.380986	),
                        new LatLng(37.676715	,127.380734	),
                        new LatLng(37.676409	,127.380585	),
                        new LatLng(37.676265	,127.380528	),
                        new LatLng(37.672764	,127.379331	),
                        new LatLng(37.672206	,127.379215	),
                        new LatLng(37.671782	,127.379122	),
                        new LatLng(37.671197	,127.379006	),
                        new LatLng(37.670972	,127.378959	),
                        new LatLng(37.670171	,127.378683	),
                        new LatLng(37.669649	,127.378533	),
                        new LatLng(37.667606	,127.377979	),
                        new LatLng(37.667219	,127.377875	),
                        new LatLng(37.666691	,127.37776	),
                        new LatLng(37.665832	,127.377539	),
                        new LatLng(37.665382	,127.377424	),
                        new LatLng(37.661654	,127.376759	),
                        new LatLng(37.660952	,127.376631	),
                        new LatLng(37.66043 	,127.376526	),
                        new LatLng(37.658584	,127.376211	),
                        new LatLng(37.658286	,127.376164	),
                        new LatLng(37.656972	,127.375942	),
                        new LatLng(37.655238	,127.375594	),
                        new LatLng(37.65407 	,127.374919	),
                        new LatLng(37.653846	,127.374789	),
                        new LatLng(37.653574	,127.374632	),
                        new LatLng(37.652927	,127.374257	),
                        new LatLng(37.651673	,127.373609	),
                        new LatLng(37.650485	,127.373021	),
                        new LatLng(37.649856	,127.372717	),
                        new LatLng(37.649707	,127.372645	),
                        new LatLng(37.649429	,127.37251	),
                        new LatLng(37.648738	,127.372096	),
                        new LatLng(37.647652	,127.371495	),
                        new LatLng(37.647652	,127.371497	),
                        new LatLng(37.647505	,127.371482	),
                        new LatLng(37.647143	,127.371414	),
                        new LatLng(37.645509	,127.370609	),
                        new LatLng(37.645393	,127.370513	),
                        new LatLng(37.644904	,127.370119	),
                        new LatLng(37.644531	,127.369818	),
                        new LatLng(37.643596	,127.369237	),
                        new LatLng(37.643065	,127.368893	),
                        new LatLng(37.642135	,127.368311	),
                        new LatLng(37.641613	,127.368007	),
                        new LatLng(37.641023	,127.367639	),
                        new LatLng(37.640742	,127.367459	),
                        new LatLng(37.64046 	,127.367258	),
                        new LatLng(37.639937	,127.366735	),
                        new LatLng(37.639398	,127.366193	),
                        new LatLng(37.638845	,127.365591	),
                        new LatLng(37.638408	,127.365115	),
                        new LatLng(37.638203	,127.364892	),
                        new LatLng(37.637967	,127.364605	),
                        new LatLng(37.63773 	,127.364318	),
                        new LatLng(37.637632	,127.364199	),
                        new LatLng(37.63745 	,127.363978	),
                        new LatLng(37.637146	,127.363609	),
                        new LatLng(37.636735	,127.363071	),
                        new LatLng(37.634735	,127.358769	),
                        new LatLng(37.634277	,127.358177	),
                        new LatLng(37.632595	,127.356665	),
                        new LatLng(37.631024	,127.355284	),
                        new LatLng(37.628958	,127.353349	),
                        new LatLng(37.627481	,127.352402	),
                        new LatLng(37.624795	,127.350355	),
                        new LatLng(37.623832	,127.349994	),
                        new LatLng(37.622333	,127.349285	),
                        new LatLng(37.621646	,127.348942	),
                        new LatLng(37.620775	,127.348649	),
                        new LatLng(37.619838	,127.348095	),
                        new LatLng(37.618854	,127.347613	),
                        new LatLng(37.618647	,127.347516	),
                        new LatLng(37.618614	,127.347501	),
                        new LatLng(37.618178	,127.347341	),
                        new LatLng(37.61802 	,127.347294	),
                        new LatLng(37.617192	,127.347064	),
                        new LatLng(37.617103	,127.347041	),
                        new LatLng(37.616868	,127.346994	),
                        new LatLng(37.6163  	,127.346878	),
                        new LatLng(37.614537	,127.34593	),
                        new LatLng(37.613034	,127.345368	),
                        new LatLng(37.609432	,127.344785	),
                        new LatLng(37.608063	,127.344575	),
                        new LatLng(37.606091	,127.344136	),
                        new LatLng(37.603947	,127.343956	),
                        new LatLng(37.602228	,127.343427	),
                        new LatLng(37.60103 	,127.343184	),
                        new LatLng(37.59988 	,127.342942	),
                        new LatLng(37.599509	,127.34286	),
                        new LatLng(37.598923	,127.342767	),
                        new LatLng(37.598626	,127.34272	),
                        new LatLng(37.598194	,127.342684	),
                        new LatLng(37.597942	,127.342683	),
                        new LatLng(37.597082	,127.342668	),
                        new LatLng(37.595411	,127.342433	),
                        new LatLng(37.594818	,127.342268	),
                        new LatLng(37.594414	,127.342165	),
                        new LatLng(37.594132	,127.342088	),
                        new LatLng(37.593097	,127.341823	),
                        new LatLng(37.591575	,127.341095	),
                        new LatLng(37.590415	,127.340622	),
                        new LatLng(37.589866	,127.340461	),
                        new LatLng(37.589686	,127.340426	),
                        new LatLng(37.588154	,127.339594	),
                        new LatLng(37.586987	,127.339021	),
                        new LatLng(37.586033	,127.338655	),
                        new LatLng(37.585442	,127.338369	),
                        new LatLng(37.584226	,127.337401	),
                        new LatLng(37.583258	,127.336937	),
                        new LatLng(37.580625	,127.336321	),
                        new LatLng(37.580445	,127.336241	),
                        new LatLng(37.579591	,127.335671	),
                        new LatLng(37.578628	,127.335078	),
                        new LatLng(37.57826 	,127.334748	),
                        new LatLng(37.577918	,127.334452	),
                        new LatLng(37.577226	,127.33403	),
                        new LatLng(37.577084	,127.333953	),
                        new LatLng(37.576353	,127.333675	),
                        new LatLng(37.575632	,127.333491	),
                        new LatLng(37.574084	,127.33302	),
                        new LatLng(37.568309	,127.329644	),
                        new LatLng(37.568084	,127.329507	),
                        new LatLng(37.565682	,127.328444	),
                        new LatLng(37.564747	,127.327738	),
                        new LatLng(37.564513	,127.327559	),
                        new LatLng(37.563415	,127.326906	),
                        new LatLng(37.562921	,127.326553	),
                        new LatLng(37.562633	,127.326393	),
                        new LatLng(37.561967	,127.326039	),
                        new LatLng(37.56051 	,127.325162	),
                        new LatLng(37.560393	,127.325071	),
                        new LatLng(37.557929	,127.323283	),
                        new LatLng(37.554612	,127.320711	),
                        new LatLng(37.554063	,127.320324	),
                        new LatLng(37.553137	,127.319641	),
                        new LatLng(37.552508	,127.319163	),
                        new LatLng(37.550566	,127.317593	),
                        new LatLng(37.549972	,127.317036	),
                        new LatLng(37.549496	,127.316593	),
                        new LatLng(37.548912	,127.31607	),
                        new LatLng(37.548525	,127.315729	),
                        new LatLng(37.547734	,127.315058	),
                        new LatLng(37.547416	,127.314822	),
                        new LatLng(37.547051	,127.314591	),
                        new LatLng(37.546187	,127.314044	),
                        new LatLng(37.545782	,127.313794	),
                        new LatLng(37.544937	,127.313258	),
                        new LatLng(37.54419 	,127.312814	),
                        new LatLng(37.544064	,127.312734	),
                        new LatLng(37.543579	,127.312449	),
                        new LatLng(37.543314	,127.312294	),
                        new LatLng(37.542985	,127.312096	),
                        new LatLng(37.542075	,127.311418	),
                        new LatLng(37.541736	,127.311166	),
                        new LatLng(37.541051	,127.310787	),
                        new LatLng(37.540911	,127.310712	),
                        new LatLng(37.540664	,127.31057	),
                        new LatLng(37.540225	,127.310356	),
                        new LatLng(37.539764	,127.310165	),
                        new LatLng(37.539305	,127.309976	),
                        new LatLng(37.539153	,127.309911	),
                        new LatLng(37.538376	,127.309594	),
                        new LatLng(37.538106	,127.309494	),
                        new LatLng(37.537451	,127.309267	),
                        new LatLng(37.537149	,127.309166	),
                        new LatLng(37.536011	,127.308786	),
                        new LatLng(37.535102	,127.308488	),
                        new LatLng(37.534319	,127.308123	),
                        new LatLng(37.533749	,127.307874	),
                        new LatLng(37.532734	,127.307573	),
                        new LatLng(37.531717	,127.307275	),
                        new LatLng(37.531268	,127.307121	),
                        new LatLng(37.53084 	,127.30697	),
                        new LatLng(37.530406	,127.306846	),
                        new LatLng(37.529824	,127.306692	),
                        new LatLng(37.529105	,127.306569	),
                        new LatLng(37.52825 	,127.306424	),
                        new LatLng(37.527342	,127.306319	),
                        new LatLng(37.526996	,127.30629	),
                        new LatLng(37.525377	,127.306186	),
                        new LatLng(37.524872	,127.30615	),
                        new LatLng(37.524125	,127.306102	),
                        new LatLng(37.523665	,127.306077	),
                        new LatLng(37.523377	,127.306053	),
                        new LatLng(37.523044	,127.306007	),
                        new LatLng(37.522939	,127.305997	),
                        new LatLng(37.52211 	,127.305936	),
                        new LatLng(37.521611	,127.305969	),
                        new LatLng(37.521161	,127.306044	),
                        new LatLng(37.52054 	,127.306232	),
                        new LatLng(37.520254	,127.306324	),
                        new LatLng(37.519808	,127.30648	),
                        new LatLng(37.51951 	,127.306761	),
                        new LatLng(37.517249	,127.306662	),
                        new LatLng(37.515834	,127.30665	),
                        new LatLng(37.514717	,127.306764	),
                        new LatLng(37.51459 	,127.306697	),
                        new LatLng(37.512974	,127.3058	),
                        new LatLng(37.512233	,127.305403	),
                        new LatLng(37.511748	,127.304966	),
                        new LatLng(37.510587	,127.303887	),
                        new LatLng(37.510083	,127.303446	),
                        new LatLng(37.509644	,127.303057	),
                        new LatLng(37.509287	,127.302746	),
                        new LatLng(37.509016	,127.302499	),
                        new LatLng(37.508739	,127.302247	),
                        new LatLng(37.508496	,127.301908	),
                        new LatLng(37.507723	,127.300504	),
                        new LatLng(37.507516	,127.299927	),
                        new LatLng(37.507047	,127.298638	),
                        new LatLng(37.506843	,127.298082	),
                        new LatLng(37.506683	,127.297651	),
                        new LatLng(37.506598	,127.297422	),
                        new LatLng(37.506375	,127.29681	),
                        new LatLng(37.506233	,127.296413	),
                        new LatLng(37.506072	,127.29596	),
                        new LatLng(37.505618	,127.29405	),
                        new LatLng(37.505499	,127.292604	),
                        new LatLng(37.505509	,127.292081	),
                        new LatLng(37.505602	,127.290671	),
                        new LatLng(37.506291	,127.288282	),
                        new LatLng(37.507089	,127.287156	),
                        new LatLng(37.510197	,127.28659	),
                        new LatLng(37.510334	,127.286566	),
                        new LatLng(37.511569	,127.286311	),
                        new LatLng(37.512793	,127.286054	),
                        new LatLng(37.513413	,127.28592	),
                        new LatLng(37.514232	,127.285762	),
                        new LatLng(37.514398	,127.285701	),
                        new LatLng(37.516628	,127.285591	),
                        new LatLng(37.517077	,127.285577	),
                        new LatLng(37.517612	,127.285471	),
                        new LatLng(37.518007	,127.285388	),
                        new LatLng(37.518252	,127.285349	),
                        new LatLng(37.518635	,127.285264	),
                        new LatLng(37.521236	,127.284539	),
                        new LatLng(37.521411	,127.284427	),
                        new LatLng(37.521578	,127.28437	),
                        new LatLng(37.522719	,127.283312	),
                        new LatLng(37.52398 	,127.282264	),
                        new LatLng(37.525112	,127.281279	),
                        new LatLng(37.5255  	,127.280229	),
                        new LatLng(37.525817	,127.27953	),
                        new LatLng(37.526077	,127.278969	),
                        new LatLng(37.526572	,127.278157	),
                        new LatLng(37.527104	,127.276821	),
                        new LatLng(37.528318	,127.274296	),
                        new LatLng(37.528638	,127.273607	),
                        new LatLng(37.529085	,127.272337	),
                        new LatLng(37.531084	,127.267324	),
                        new LatLng(37.531617	,127.266096	),
                        new LatLng(37.532058	,127.264873	),
                        new LatLng(37.532684	,127.26364	),
                        new LatLng(37.533997	,127.26155	),
                        new LatLng(37.534906	,127.260177	),
                        new LatLng(37.535987	,127.258701	),
                        new LatLng(37.536346	,127.257971	),
                        new LatLng(37.536503	,127.257432	),
                        new LatLng(37.536879	,127.256785	),
                        new LatLng(37.537103	,127.256289	),
                        new LatLng(37.53762 	,127.25536	),
                        new LatLng(37.537702	,127.254919	),
                        new LatLng(37.538674	,127.253036	),
                        new LatLng(37.539281	,127.251801	),
                        new LatLng(37.5405  	,127.248927	),
                        new LatLng(37.541039	,127.247798	),
                        new LatLng(37.541807	,127.245756	),
                        new LatLng(37.542283	,127.244264	),
                        new LatLng(37.543155	,127.241794	),
                        new LatLng(37.543896	,127.240876	),
                        new LatLng(37.544839	,127.239764	),
                        new LatLng(37.545372	,127.239195	),
                        new LatLng(37.547215	,127.237504	),
                        new LatLng(37.548418	,127.237066	),
                        new LatLng(37.548415	,127.237028	),
                        new LatLng(37.549795	,127.236441	),
                        new LatLng(37.549997	,127.236403	),
                        new LatLng(37.55436 	,127.235081	),
                        new LatLng(37.55725 	,127.233772	),
                        new LatLng(37.55791 	,127.233327	),
                        new LatLng(37.559455	,127.232545	),
                        new LatLng(37.559874	,127.23223	),
                        new LatLng(37.561273	,127.230495	),
                        new LatLng(37.565432	,127.221487	),
                        new LatLng(37.566261	,127.220574	),
                        new LatLng(37.566475	,127.220426	),
                        new LatLng(37.568477	,127.219973	),
                        new LatLng(37.570093	,127.220048	),
                        new LatLng(37.573351	,127.220033	),
                        new LatLng(37.574137	,127.218982	),
                        new LatLng(37.575315	,127.215918	),
                        new LatLng(37.576713	,127.213591	),
                        new LatLng(37.579167	,127.211343	),
                        new LatLng(37.582902	,127.207857	),
                        new LatLng(37.58365 	,127.20732	),
                        new LatLng(37.584797	,127.206433	),
                        new LatLng(37.586113	,127.205375	),
                        new LatLng(37.590914	,127.196995	),
                        new LatLng(37.591079	,127.196486	),
                        new LatLng(37.591012	,127.195955	),
                        new LatLng(37.591131	,127.193656	),
                        new LatLng(37.591266	,127.192088	),
                        new LatLng(37.591516	,127.188905	),
                        new LatLng(37.591762	,127.1852	),
                        new LatLng(37.591717	,127.183333	),
                        new LatLng(37.589624	,127.181448	),
                        new LatLng(37.589025	,127.181044	),
                        new LatLng(37.588462	,127.180579	),
                        new LatLng(37.586548	,127.17833	),
                        new LatLng(37.586249	,127.177844	),
                        new LatLng(37.585515	,127.176888	),
                        new LatLng(37.581029	,127.170598	),
                        new LatLng(37.586772	,127.16049	),
                        new LatLng(37.589689	,127.162619	),
                        new LatLng(37.590834	,127.16847	),
                        new LatLng(37.594042	,127.167696	),
                        new LatLng(37.596288	,127.156122	),
                        new LatLng(37.599944	,127.150603	),
                        new LatLng(37.625211	,127.143145	),
                        new LatLng(37.625699	,127.143304	),
                        new LatLng(37.626933	,127.143702	),
                        new LatLng(37.627712	,127.14397	),
                        new LatLng(37.63463 	,127.149903	),
                        new LatLng(37.644246	,127.146058	),
                        new LatLng(37.646081	,127.140419	),
                        new LatLng(37.641209	,127.127238	),
                        new LatLng(37.643798	,127.1156	),
                        new LatLng(37.642376	,127.111258	),
                        new LatLng(37.642375	,127.111257	),
                        new LatLng(37.642564	,127.111155	),
                        new LatLng(37.64273 	,127.110751	),
                        new LatLng(37.642488	,127.109899	),
                        new LatLng(37.642859	,127.109289	),
                        new LatLng(37.643613	,127.108712	),
                        new LatLng(37.643666	,127.10867	),
                        new LatLng(37.64446 	,127.108006	),
                        new LatLng(37.644693	,127.107863	),
                        new LatLng(37.645404	,127.105312	),
                        new LatLng(37.645493	,127.103921	),
                        new LatLng(37.645205	,127.1028	),
                        new LatLng(37.644657	,127.094529	),
                        new LatLng(37.645506	,127.094538	),
                        new LatLng(37.645924	,127.094369	),
                        new LatLng(37.647322	,127.093834	),
                        new LatLng(37.648361	,127.092917	),
                        new LatLng(37.649346	,127.092716	),
                        new LatLng(37.651901	,127.093725	),
                        new LatLng(37.652295	,127.094	),
                        new LatLng(37.65254 	,127.094041	),
                        new LatLng(37.652962	,127.093916	),
                        new LatLng(37.655492	,127.092352	),
                        new LatLng(37.659335	,127.091207	),
                        new LatLng(37.666172	,127.094581	),
                        new LatLng(37.670079	,127.096013	),
                        new LatLng(37.67116 	,127.095806	),
                        new LatLng(37.67185 	,127.096019	),
                        new LatLng(37.672891	,127.095147	),
                        new LatLng(37.673536	,127.094615	),
                        new LatLng(37.675617	,127.093963	),
                        new LatLng(37.676143	,127.093886	),
                        new LatLng(37.677345	,127.092968	),
                        new LatLng(37.677528	,127.092575	),
                        new LatLng(37.681551	,127.092919	),
                        new LatLng(37.682742	,127.093883	),
                        new LatLng(37.683419	,127.094288	),
                        new LatLng(37.68464 	,127.09581	),
                        new LatLng(37.685637	,127.096423	),
                        new LatLng(37.689071	,127.095996	),
                        new LatLng(37.689385	,127.095097	),
                        new LatLng(37.689132	,127.09429	),
                        new LatLng(37.689966	,127.093037	),
                        new LatLng(37.68957 	,127.090552	),
                        new LatLng(37.69039 	,127.085177	),
                        new LatLng(37.691782	,127.083905	),
                        new LatLng(37.696137	,127.081105	),
                        new LatLng(37.697454	,127.080602	),
                        new LatLng(37.699513	,127.080233	),
                        new LatLng(37.701755	,127.083781	),
                        new LatLng(37.701885	,127.083978	),
                        new LatLng(37.701919	,127.084086	),
                        new LatLng(37.702366	,127.088774	),
                        new LatLng(37.70258	    ,127.091157	),
                        new LatLng(37.703048	,127.092649	),
                        new LatLng(37.703736	,127.093982	),
                        new LatLng(37.702734	,127.095039	),
                        new LatLng(37.702373	,127.095721	),
                        new LatLng(37.702176	,127.096516	),
                        new LatLng(37.703601	,127.098153	),
                        new LatLng(37.704795	,127.099772	),
                        new LatLng(37.704833	,127.101043	),
                        new LatLng(37.704843	,127.101082	),
                        new LatLng(37.705413	,127.10159	),
                        new LatLng(37.705672	,127.101956	),
                        new LatLng(37.706009	,127.102886	),
                        new LatLng(37.70654 	,127.103157	),
                        new LatLng(37.706703	,127.103621	),
                        new LatLng(37.70721 	,127.10391	),
                        new LatLng(37.707209	,127.103912	),
                        new LatLng(37.707214	,127.103915	),
                        new LatLng(37.70704 	,127.104432	),
                        new LatLng(37.707539	,127.105753	),
                        new LatLng(37.7076  	,127.105889	),
                        new LatLng(37.707578	,127.107189	),
                        new LatLng(37.707851	,127.10759	),
                        new LatLng(37.707954	,127.107942	),
                        new LatLng(37.708177	,127.108376	),
                        new LatLng(37.708775	,127.109014	),
                        new LatLng(37.70904 	,127.109109	),
                        new LatLng(37.709602	,127.109281	),
                        new LatLng(37.709637	,127.109286	),
                        new LatLng(37.710025	,127.109291	),
                        new LatLng(37.710577	,127.109224	),
                        new LatLng(37.710863	,127.109182	),
                        new LatLng(37.71153 	,127.108701	),
                        new LatLng(37.711854	,127.108867	),
                        new LatLng(37.711918	,127.108912	),
                        new LatLng(37.712166	,127.109284	),
                        new LatLng(37.712472	,127.109265	),
                        new LatLng(37.713453	,127.109129	),
                        new LatLng(37.713744	,127.109631	),
                        new LatLng(37.713794	,127.109705	),
                        new LatLng(37.714232	,127.110005	),
                        new LatLng(37.714916	,127.110195	),
                        new LatLng(37.714946	,127.110206	),
                        new LatLng(37.715925	,127.110624	),
                        new LatLng(37.716258	,127.111072	),
                        new LatLng(37.716674	,127.111321	),
                        new LatLng(37.716901	,127.111415	),
                        new LatLng(37.717262	,127.111749	),
                        new LatLng(37.717481	,127.111973	),
                        new LatLng(37.717695	,127.113195	),
                        new LatLng(37.717707	,127.113258	),
                        new LatLng(37.717765	,127.114007	),
                        new LatLng(37.717313	,127.115044	),
                        new LatLng(37.717305	,127.115059	),
                        new LatLng(37.716988	,127.115482	),
                        new LatLng(37.717888	,127.116298	),
                        new LatLng(37.718212	,127.116482	),
                        new LatLng(37.718375	,127.116788	),
                        new LatLng(37.718689	,127.116992	),
                        new LatLng(37.718727	,127.117002	),
                        new LatLng(37.719028	,127.117082	),
                        new LatLng(37.7195  	,127.117157	),
                        new LatLng(37.72107  	,127.118486	),
                        new LatLng(37.721261	,127.118839	),
                        new LatLng(37.722347	,127.120894	),
                        new LatLng(37.721987	,127.121834	),
                        new LatLng(37.721932	,127.123063	),
                        new LatLng(37.722755	,127.123759	),
                        new LatLng(37.723238	,127.122936	),
                        new LatLng(37.723245	,127.122933	),
                        new LatLng(37.723385	,127.122859	),
                        new LatLng(37.725616	,127.123509	),
                        new LatLng(37.725945	,127.123519	),
                        new LatLng(37.729256	,127.125698	),
                        new LatLng(37.729697	,127.125441	),
                        new LatLng(37.731022	,127.125643	),
                        new LatLng(37.731182	,127.125623	),
                        new LatLng(37.732496	,127.125556	),
                        new LatLng(37.732889	,127.125402	),
                        new LatLng(37.733358	,127.125479	),
                        new LatLng(37.733399	,127.125491	),
                        new LatLng(37.733992	,127.125679	),
                        new LatLng(37.734444	,127.125383	),
                        new LatLng(37.73501 	,127.123873	),
                        new LatLng(37.735373	,127.123831	),
                        new LatLng(37.735663	,127.123687	),
                        new LatLng(37.736134	,127.124126	),
                        new LatLng(37.737105	,127.124031	),
                        new LatLng(37.737486	,127.123952	),
                        new LatLng(37.737523	,127.123889	),
                        new LatLng(37.738608	,127.123746	),
                        new LatLng(37.738807	,127.123719	),
                        new LatLng(37.740287	,127.124467	),
                        new LatLng(37.740652	,127.124496	),
                        new LatLng(37.740801	,127.124508	),
                        new LatLng(37.741074	,127.124664	),
                        new LatLng(37.74111 	,127.12468	),
                        new LatLng(37.741771	,127.125008	),
                        new LatLng(37.741772	,127.125008	),
                        new LatLng(37.742237	,127.125083	),
                        new LatLng(37.742318	,127.125077	),
                        new LatLng(37.742579	,127.125035	),
                        new LatLng(37.743552	,127.126017	),
                        new LatLng(37.743842	,127.126269	),
                        new LatLng(37.744167	,127.126497	),
                        new LatLng(37.74419 	,127.12652	),
                        new LatLng(37.744735	,127.127631	),
                        new LatLng(37.744814	,127.127768	),
                        new LatLng(37.744961	,127.127979	),
                        new LatLng(37.745254	,127.128366	),
                        new LatLng(37.747217	,127.128337	),
                        new LatLng(37.747349	,127.128392	),
                        new LatLng(37.748081	,127.128638	),
                        new LatLng(37.748515	,127.130721	),
                        new LatLng(37.74892 	,127.132032	),
                        new LatLng(37.748919	,127.1329	),
                        new LatLng(37.74952 	,127.136342	),
                        new LatLng(37.748154	,127.137442	),
                        new LatLng(37.747416	,127.138368	),
                        new LatLng(37.747915	,127.142616	),
                        new LatLng(37.747972	,127.142807	),
                        new LatLng(37.748842	,127.144398	),
                        new LatLng(37.7514  	,127.147055	),
                        new LatLng(37.752015	,127.147246	),
                        new LatLng(37.753744	,127.146615	),
                        new LatLng(37.754479	,127.146435	),
                        new LatLng(37.755498	,127.146178	),
                        new LatLng(37.75552 	,127.146172	),
                        new LatLng(37.755523	,127.146175	),
                        new LatLng(37.756112	,127.148307	),
                        new LatLng(37.755517	,127.14937	),
                        new LatLng(37.75511 	,127.150098	),
                        new LatLng(37.75113 	,127.155309	),
                        new LatLng(37.749217	,127.157972	),
                        new LatLng(37.746929	,127.163811	),
                        new LatLng(37.746968	,127.164042	),
                        new LatLng(37.749059	,127.167934	),
                        new LatLng(37.749674	,127.17219	),
                        new LatLng(37.750169	,127.172441	),
                        new LatLng(37.750648	,127.171693	),
                        new LatLng(37.753291	,127.173255	),
                        new LatLng(37.754655	,127.17303	),
                        new LatLng(37.755719	,127.172783	),
                        new LatLng(37.75767 	,127.172537	),
                        new LatLng(37.758369	,127.176135	),
                        new LatLng(37.759164	,127.176615	),
                        new LatLng(37.763042	,127.18012	),
                        new LatLng(37.764398	,127.182824	),
                        new LatLng(37.764159	,127.186092	),
                        new LatLng(37.762841	,127.18736	),
                        new LatLng(37.762532	,127.189356	),
                        new LatLng(37.762388	,127.189504	),
                        new LatLng(37.762458	,127.19065	),
                        new LatLng(37.763467	,127.190834	),
                        new LatLng(37.764266	,127.192618	),
                        new LatLng(37.763164	,127.193977	),
                        new LatLng(37.762984	,127.194124	),
                        new LatLng(37.762939	,127.194101	),
                        new LatLng(37.762848	,127.194237	),
                        new LatLng(37.762668	,127.194452	),
                        new LatLng(37.762577	,127.194611	),
                        new LatLng(37.762387	,127.195405	),
                        new LatLng(37.762124	,127.197086	),
                        new LatLng(37.762294	,127.197334	),
                        new LatLng(37.76321 	,127.198732	),
                        new LatLng(37.763938	,127.199744	),
                        new LatLng(37.764018	,127.199905	),
                        new LatLng(37.765134	,127.20162	),
                        new LatLng(37.765238	,127.202066	),
                        new LatLng(37.765295	,127.202109	),
                        new LatLng(37.763244	,127.205144	),
                        new LatLng(37.763405	,127.205789	),
                        new LatLng(37.76389 	,127.2072	),
                        new LatLng(37.764524	,127.207543	),
                        new LatLng(37.766501	,127.208093	),
                        new LatLng(37.767339	,127.207925	),
                        new LatLng(37.769376	,127.211504	),
                        new LatLng(37.769369	,127.211642	),
                        new LatLng(37.769366	,127.211644	),
                        new LatLng(37.767735	,127.213249	),
                        new LatLng(37.76765 	,127.21314	),
                        new LatLng(37.766934	,127.212781	),
                        new LatLng(37.766024	,127.21304	),
                        new LatLng(37.765834	,127.213039	),
                        new LatLng(37.764738	,127.212655	),
                        new LatLng(37.764364	,127.212713	),
                        new LatLng(37.761537	,127.212868	),
                        new LatLng(37.760815	,127.213853	),
                        new LatLng(37.760814	,127.214284	),
                        new LatLng(37.760858	,127.214659	),
                        new LatLng(37.759855	,127.216154	),
                        new LatLng(37.758303	,127.217761	),
                        new LatLng(37.758434	,127.219679	),
                        new LatLng(37.758443	,127.219725	),
                        new LatLng(37.758452	,127.219781	),
                        new LatLng(37.758425	,127.219827	),
                        new LatLng(37.758389	,127.220008	),
                        new LatLng(37.758325	,127.220167	),
                        new LatLng(37.758235	,127.220314	),
                        new LatLng(37.757991	,127.220529	),
                        new LatLng(37.757784	,127.220562	),
                        new LatLng(37.757748	,127.220574	),
                        new LatLng(37.757843	,127.220664	),
                        new LatLng(37.757874	,127.220688	),
                        new LatLng(37.75791 	,127.220756	),
                        new LatLng(37.757955	,127.220779	),
                        new LatLng(37.757994	,127.220854	),
                        new LatLng(37.758026	,127.221335	),
                        new LatLng(37.758205	,127.221744	),
                        new LatLng(37.758007	,127.221902	),
                        new LatLng(37.757862	,127.222072	),
                        new LatLng(37.757862	,127.222265	),
                        new LatLng(37.757825	,127.222571	),
                        new LatLng(37.7575  	,127.222888	),
                        new LatLng(37.757104	,127.223091	),
                        new LatLng(37.756517	,127.223555	),
                        new LatLng(37.755365	,127.222802	),
                        new LatLng(37.7553  	,127.22272	),
                        new LatLng(37.754843	,127.222835	),
                        new LatLng(37.754373	,127.223219	),
                        new LatLng(37.754265	,127.223491	),
                        new LatLng(37.753738	,127.225986	),
                        new LatLng(37.754394	,127.22659	),
                        new LatLng(37.754111	,127.228381	),
                        new LatLng(37.754499	,127.228349	),
                        new LatLng(37.756477	,127.230193	),
                        new LatLng(37.75736 	,127.23057	),
                        new LatLng(37.757414	,127.230616	),
                        new LatLng(37.757557	,127.231116	),
                        new LatLng(37.757833	,127.232546	),
                        new LatLng(37.758867	,127.234002	),
                        new LatLng(37.759254	,127.234094	),
                        new LatLng(37.759731	,127.234186	),
                        new LatLng(37.760405	,127.234847	),
                        new LatLng(37.762285	,127.236566	),
                        new LatLng(37.763591	,127.236979	),
                        new LatLng(37.763553	,127.238011	),
                        new LatLng(37.763344	,127.238465	),
                        new LatLng(37.763044	,127.239814	),
                        new LatLng(37.762728	,127.240506	),
                        new LatLng(37.762826	,127.240903	),
                        new LatLng(37.763758	,127.243301	),
                        new LatLng(37.764116	,127.244312	),
                        new LatLng(37.764483	,127.245437	),
                        new LatLng(37.765066	,127.246676	),
                        new LatLng(37.765218	,127.247357	),
                        new LatLng(37.765245	,127.247573	),
                        new LatLng(37.765189	,127.248378	),
                        new LatLng(37.765782	,127.249254	),
                        new LatLng(37.76604 	,127.250526	),
                        new LatLng(37.766578	,127.252128	),
                        new LatLng(37.767521	,127.253516	),
                        new LatLng(37.76777 	,127.254811	),
                        new LatLng(37.768011	,127.256151	),
                        new LatLng(37.768037	,127.256351	),
                        new LatLng(37.767919	,127.256741	),
                        new LatLng(37.768689	,127.259002	),
                        new LatLng(37.768291	,127.259942	),
                        new LatLng(37.768307	,127.259943	),
                        new LatLng(37.769849	,127.260175	),
                        new LatLng(37.769965	,127.260448	),
                        new LatLng(37.770451	,127.260824	),
                        new LatLng(37.770622	,127.260836	),
                        new LatLng(37.771089	,127.261553	),
                        new LatLng(37.773991	,127.260816	),
                        new LatLng(37.77401 	,127.260814	),
                        new LatLng(37.776664	,127.262276	),
                        new LatLng(37.77821 	,127.263973	),
                        new LatLng(37.778191	,127.264358	),
                        new LatLng(37.778973	,127.265122	),
                        new LatLng(37.779068	,127.26541	),
                        new LatLng(37.779144	,127.265486	),
                        new LatLng(37.780522	,127.265729	),
                        new LatLng(37.779871	,127.266914	),
                        new LatLng(37.779256	,127.268573	),
                        new LatLng(37.77752 	,127.270225	),
                        new LatLng(37.776897	,127.271062	),
                        new LatLng(37.776193	,127.271684	),
                        new LatLng(37.774721	,127.272916	),
                        new LatLng(37.773801	,127.273503	),
                        new LatLng(37.773883	,127.273836	),
                        new LatLng(37.774177	,127.274492	),
                        new LatLng(37.774509	,127.275151	),
                        new LatLng(37.774884	,127.276492	),
                        new LatLng(37.774991	,127.277207	),
                        new LatLng(37.774458	,127.277796	),
                        new LatLng(37.774328	,127.278259	),
                        new LatLng(37.774321	,127.278306	),
                        new LatLng(37.774634	,127.279544	),
                        new LatLng(37.774974	,127.280238	),
                        new LatLng(37.775108	,127.280999	),
                        new LatLng(37.774712	,127.28277	),
                        new LatLng(37.774671	,127.282915	),
                        new LatLng(37.774761	,127.282973	),
                        new LatLng(37.774778	,127.283483	),
                        new LatLng(37.774946	,127.284494	),
                        new LatLng(37.77468 	,127.286457	),
                        new LatLng(37.774868	,127.286866	),
                        new LatLng(37.774895	,127.287264	),
                        new LatLng(37.774757	,127.288353	),
                        new LatLng(37.775024	,127.289738	),
                        new LatLng(37.774834	,127.290056	),
                        new LatLng(37.775093	,127.290965	),
                        new LatLng(37.775542	,127.291273	),
                        new LatLng(37.775774	,127.292386	),
                        new LatLng(37.775665	,127.292896	),
                        new LatLng(37.775916	,127.293204	),
                        new LatLng(37.774749	,127.295768	),
                        new LatLng(37.774482	,127.297398	),
                        new LatLng(37.774451	,127.298919	),
                        new LatLng(37.774166	,127.29978	),
                        new LatLng(37.773651	,127.301742	),
                        new LatLng(37.773448	,127.303614	),
                        new LatLng(37.773483	,127.304306	),
                        new LatLng(37.773318	,127.304406	),
                        new LatLng(37.771823	,127.305083	),
                        new LatLng(37.771543	,127.305184	),
                        new LatLng(37.770919	,127.306214	),
                        new LatLng(37.769897	,127.307674	),
                        new LatLng(37.769805	,127.308468	),
                        new LatLng(37.76891 	,127.30969	),
                        new LatLng(37.768149	,127.311378	),
                        new LatLng(37.768495	,127.313071	),
                        new LatLng(37.767056	,127.319171	),
                        new LatLng(37.76636 	,127.319826	),
                        new LatLng(37.766095	,127.320476	),
                        new LatLng(37.765078	,127.320706	),
                        new LatLng(37.763677	,127.322629	),
                        new LatLng(37.763199	,127.322695	),
                        new LatLng(37.762665	,127.323646	),
                        new LatLng(37.761492	,127.324367	),
                        new LatLng(37.761189	,127.325503	),
                        new LatLng(37.760297	,127.326439	),
                        new LatLng(37.760086	,127.32755	),
                        new LatLng(37.754706	,127.331521	),
                        new LatLng(37.753586	,127.332537	),
                        new LatLng(37.750864	,127.332684	),
                        new LatLng(37.748881	,127.333095	),
                        new LatLng(37.744019	,127.335172	),
                        new LatLng(37.742818	,127.335995	),
                        new LatLng(37.741968	,127.337885	),
                        new LatLng(37.741919	,127.338691	),
                        new LatLng(37.740863	,127.339299	),
                        new LatLng(37.740772	,127.339616	),
                        new LatLng(37.740204	,127.33967	),
                        new LatLng(37.739411	,127.339814	),
                        new LatLng(37.731968	,127.343456	),
                        new LatLng(37.730572	,127.34312	),
                        new LatLng(37.728477	,127.34369	),
                        new LatLng(37.728127	,127.344187	),
                        new LatLng(37.72748 	,127.346634	),
                        new LatLng(37.726882	,127.347947	),
                        new LatLng(37.726617	,127.34925	),
                        new LatLng(37.726384	,127.350152	),
                        new LatLng(37.725601	,127.351446	),
                        new LatLng(37.724642	,127.352814	),
                        new LatLng(37.724069	,127.354615	),
                        new LatLng(37.723662	,127.355409	),
                        new LatLng(37.722069	,127.354719	),
                        new LatLng(37.720961	,127.354441	),
                        new LatLng(37.7207  	,127.354542	),
                        new LatLng(37.719314	,127.354082	),
                        new LatLng(37.717583	,127.354391	),
                        new LatLng(37.716762	,127.35475	),
                        new LatLng(37.716123	,127.354498	),
                        new LatLng(37.714394	,127.354138	),
                        new LatLng(37.713621	,127.35376	),
                        new LatLng(37.711287	,127.353749	),
                        new LatLng(37.710285	,127.354527	),
                        new LatLng(37.710069	,127.354514	),
                        new LatLng(37.709536	,127.354897	),
                        new LatLng(37.70903 	,127.355201	),
                        new LatLng(37.707301	,127.355068	),
                        new LatLng(37.70669 	,127.354419	),
                        new LatLng(37.705494	,127.353597	),
                        new LatLng(37.705179	,127.353493	),
                        new LatLng(37.70436 	,127.353353	),
                        new LatLng(37.703402	,127.354437	),
                        new LatLng(37.703103	,127.355071	),
                        new LatLng(37.702742	,127.355228	),
                        new LatLng(37.702362	,127.355782	),
                        new LatLng(37.701658	,127.356141	),
                        new LatLng(37.700763	,127.357214	),
                        new LatLng(37.699941	,127.357811	),
                        new LatLng(37.699597	,127.358331	),
                        new LatLng(37.698893	,127.358793	),
                        new LatLng(37.698297	,127.359051	),
                        new LatLng(37.697461	,127.358661	),
                        new LatLng(37.696929	,127.358659	),
                        new LatLng(37.696372	,127.358112	),
                        new LatLng(37.695604	,127.358743	),
                        new LatLng(37.695244	,127.358662	),
                        new LatLng(37.694785	,127.358637	),
                        new LatLng(37.694569	,127.358647	),
                        new LatLng(37.694285	,127.359961	),
                        new LatLng(37.69371 	,127.362328	),
                        new LatLng(37.69316 	,127.368505	),
                        new LatLng(37.693029	,127.370046	),
                        new LatLng(37.692855	,127.370597	),
                        new LatLng(37.692692	,127.37111	),
                        new LatLng(37.692402	,127.371834	),
                        new LatLng(37.692239	,127.37206	),
                        new LatLng(37.69106 	,127.371476	),
                        new LatLng(37.690729	,127.370885	),
                        new LatLng(37.690383	,127.369262	),
                        new LatLng(37.690421	,127.368639	),
                        new LatLng(37.690367	,127.368332	),
                        new LatLng(37.689068	,127.366206	),
                        new LatLng(37.685972	,127.367959	),
                        new LatLng(37.68513 	,127.369225	),
                        new LatLng(37.685048	,127.369394	),
                        new LatLng(37.685075	,127.369406	),
                        new LatLng(37.685039	,127.369485	),
                        new LatLng(37.684848	,127.370119	),
                        new LatLng(37.684278	,127.370966	),
                        new LatLng(37.684087	,127.371283	),
                        new LatLng(37.683906	,127.371588	),
                        new LatLng(37.683725	,127.371893	),
                        new LatLng(37.683146	,127.372786	),
                        new LatLng(37.683064	,127.37291	),
                        new LatLng(37.682602	,127.373656	),
                        new LatLng(37.681744	,127.374502	),
                        new LatLng(37.68142 	,127.374206	),
                        new LatLng(37.68115 	,127.374261	),
                        new LatLng(37.680825	,127.374316	),
                        new LatLng(37.680645	,127.374315	),
                        new LatLng(37.68052 	,127.374156	),
                        new LatLng(37.680331	,127.374121	),
                        new LatLng(37.680195	,127.37412	),
                        new LatLng(37.679835	,127.374005	),
                        new LatLng(37.679538	,127.374026	),
                        new LatLng(37.679241	,127.3739	),
                        new LatLng(37.679052	,127.373888	),
                        new LatLng(37.678719	,127.373682	),
                        new LatLng(37.678295	,127.373895	),
                        new LatLng(37.678205	,127.373997	),
                        new LatLng(37.678033	,127.373996	),
                        new LatLng(37.677907	,127.374222	),
                        new LatLng(37.67667 	,127.375066	),
                        new LatLng(37.67657 	,127.375168	),
                        new LatLng(37.675006	,127.377474	),
                        new LatLng(37.676101	,127.378294	),
                        new LatLng(37.677263	,127.380986	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.663679, 127.244232));
    }//남양주시
    public void drawPolygon24(GoogleMap googlemap) { //
        String name = "안양시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.394807	,126.982732	),
                        new LatLng(37.390891	,126.977404	),
                        new LatLng(37.390878	,126.977392	),
                        new LatLng(37.390734	,126.977188	),
                        new LatLng(37.390698	,126.977132	),
                        new LatLng(37.389733	,126.975201	),
                        new LatLng(37.389725	,126.975178	),
                        new LatLng(37.38065 	,126.97254	),
                        new LatLng(37.376099	,126.968093	),
                        new LatLng(37.37563	    ,126.967743	),
                        new LatLng(37.374206	,126.966773	),
                        new LatLng(37.373801	,126.966547	),
                        new LatLng(37.364997	,126.964384	),
                        new LatLng(37.362944	,126.966157	),
                        new LatLng(37.362826	,126.966169	),
                        new LatLng(37.361942	,126.962794	),
                        new LatLng(37.361906	,126.962602	),
                        new LatLng(37.361681	,126.962332	),
                        new LatLng(37.361762	,126.962252	),
                        new LatLng(37.361635	,126.96206	),
                        new LatLng(37.362702	,126.960843	),
                        new LatLng(37.363007	,126.960516	),
                        new LatLng(37.36307 	,126.960447	),
                        new LatLng(37.36353 	,126.959886	),
                        new LatLng(37.363819	,126.959501	),
                        new LatLng(37.363998	,126.959293	),
                        new LatLng(37.364431	,126.95879	),
                        new LatLng(37.365987	,126.956904	),
                        new LatLng(37.366124	,126.956286	),
                        new LatLng(37.366128	,126.956254	),
                        new LatLng(37.366131	,126.956231	),
                        new LatLng(37.366147	,126.956179	),
                        new LatLng(37.366186	,126.956054	),
                        new LatLng(37.366204	,126.955986	),
                        new LatLng(37.366179	,126.953842	),
                        new LatLng(37.366175	,126.953789	),
                        new LatLng(37.366708	,126.951056	),
                        new LatLng(37.366661	,126.95104	),
                        new LatLng(37.366624	,126.951028	),
                        new LatLng(37.366534	,126.951037	),
                        new LatLng(37.366737	,126.950799	),
                        new LatLng(37.366945	,126.950895	),
                        new LatLng(37.367371	,126.950717	),
                        new LatLng(37.367722	,126.950324	),
                        new LatLng(37.368064	,126.94995	),
                        new LatLng(37.368447	,126.949732	),
                        new LatLng(37.368651	,126.949412	),
                        new LatLng(37.369112	,126.948391	),
                        new LatLng(37.369311	,126.947932	),
                        new LatLng(37.369882	,126.947644	),
                        new LatLng(37.370496	,126.947395	),
                        new LatLng(37.371016	,126.947289	),
                        new LatLng(37.371686	,126.946802	),
                        new LatLng(37.372338	,126.946323	),
                        new LatLng(37.372486	,126.946246	),
                        new LatLng(37.3727  	,126.946096	),
                        new LatLng(37.373005	,126.945863	),
                        new LatLng(37.373078	,126.94581	),
                        new LatLng(37.373307	,126.945739	),
                        new LatLng(37.373606	,126.945811	),
                        new LatLng(37.374032	,126.945619	),
                        new LatLng(37.374229	,126.945632	),
                        new LatLng(37.374234	,126.945632	),
                        new LatLng(37.374342	,126.945649	),
                        new LatLng(37.374927	,126.946267	),
                        new LatLng(37.37493 	,126.946275	),
                        new LatLng(37.374951	,126.946625	),
                        new LatLng(37.374973	,126.946674	),
                        new LatLng(37.375026	,126.94681	),
                        new LatLng(37.376652	,126.945027	),
                        new LatLng(37.376646	,126.945017	),
                        new LatLng(37.376647	,126.945015	),
                        new LatLng(37.376661	,126.944985	),
                        new LatLng(37.376946	,126.944841	),
                        new LatLng(37.377124	,126.944754	),
                        new LatLng(37.377153	,126.944739	),
                        new LatLng(37.378014	,126.943798	),
                        new LatLng(37.378145	,126.943463	),
                        new LatLng(37.378284	,126.943028	),
                        new LatLng(37.37832 	,126.942883	),
                        new LatLng(37.378366	,126.942726	),
                        new LatLng(37.378394	,126.942638	),
                        new LatLng(37.378526	,126.942221	),
                        new LatLng(37.378629	,126.941815	),
                        new LatLng(37.379431	,126.940807	),
                        new LatLng(37.391748	,126.941116	),
                        new LatLng(37.399414	,126.929179	),
                        new LatLng(37.408229	,126.927358	),
                        new LatLng(37.415042	,126.929764	),
                        new LatLng(37.414444	,126.93825	),
                        new LatLng(37.419125	,126.941829	),
                        new LatLng(37.42207 	,126.948462	),
                        new LatLng(37.43863 	,126.948155	),
                        new LatLng(37.43869 	,126.948289	),
                        new LatLng(37.438713	,126.948363	),
                        new LatLng(37.438646	,126.948766	),
                        new LatLng(37.438507	,126.948945	),
                        new LatLng(37.438344	,126.949161	),
                        new LatLng(37.438329	,126.949189	),
                        new LatLng(37.43825 	,126.949284	),
                        new LatLng(37.438281	,126.949513	),
                        new LatLng(37.43838 	,126.949843	),
                        new LatLng(37.438734	,126.950925	),
                        new LatLng(37.438976	,126.952072	),
                        new LatLng(37.43919 	,126.952411	),
                        new LatLng(37.439095	,126.953167	),
                        new LatLng(37.43901 	,126.953904	),
                        new LatLng(37.438803	,126.956705	),
                        new LatLng(37.43893 	,126.957567	),
                        new LatLng(37.43907 	,126.958966	),
                        new LatLng(37.439407	,126.959169	),
                        new LatLng(37.43991 	,126.959497	),
                        new LatLng(37.440005	,126.959571	),
                        new LatLng(37.440356	,126.96	    ),
                        new LatLng(37.440299	,126.9615	),
                        new LatLng(37.440279	,126.961763	),
                        new LatLng(37.44028 	,126.962943	),
                        new LatLng(37.440377	,126.963114	),
                        new LatLng(37.440787	,126.963803	),
                        new LatLng(37.439223	,126.965478	),
                        new LatLng(37.437601	,126.964032	),
                        new LatLng(37.434853	,126.963197	),
                        new LatLng(37.431258	,126.963956	),
                        new LatLng(37.430551	,126.962451	),
                        new LatLng(37.429437	,126.962172	),
                        new LatLng(37.427743	,126.962037	),
                        new LatLng(37.426088	,126.960935	),
                        new LatLng(37.426085	,126.960931	),
                        new LatLng(37.425184	,126.961191	),
                        new LatLng(37.424562	,126.961689	),
                        new LatLng(37.423572	,126.962108	),
                        new LatLng(37.421941	,126.962345	),
                        new LatLng(37.421569	,126.962367	),
                        new LatLng(37.42138 	,126.96239	),
                        new LatLng(37.419813	,126.963386	),
                        new LatLng(37.419562	,126.963356	),
                        new LatLng(37.418041	,126.965985	),
                        new LatLng(37.417302	,126.966889	),
                        new LatLng(37.417239	,126.967262	),
                        new LatLng(37.417158	,126.968301	),
                        new LatLng(37.417339	,126.969317	),
                        new LatLng(37.417267	,126.969578	),
                        new LatLng(37.417312	,126.970074	),
                        new LatLng(37.417267	,126.970312	),
                        new LatLng(37.416715	,126.970626	),
                        new LatLng(37.416042	,126.971408	),
                        new LatLng(37.415997	,126.97186	),
                        new LatLng(37.415988	,126.972379	),
                        new LatLng(37.415979	,126.972402	),
                        new LatLng(37.415907	,126.972933	),
                        new LatLng(37.415204	,126.973758	),
                        new LatLng(37.415078	,126.973758	),
                        new LatLng(37.413988	,126.974154	),
                        new LatLng(37.413799	,126.974267	),
                        new LatLng(37.413331	,126.974877	),
                        new LatLng(37.413024	,126.975114	),
                        new LatLng(37.412096	,126.975273	),
                        new LatLng(37.41149 	,126.9757	),
                        new LatLng(37.41134 	,126.975623	),
                        new LatLng(37.410709	,126.975352	),
                        new LatLng(37.410222	,126.97568	),
                        new LatLng(37.409772	,126.975804	),
                        new LatLng(37.409538	,126.975759	),
                        new LatLng(37.409447	,126.975737	),
                        new LatLng(37.408916	,126.975579	),
                        new LatLng(37.406672	,126.975602	),
                        new LatLng(37.405897	,126.975874	),
                        new LatLng(37.406006	,126.976054	),
                        new LatLng(37.406132	,126.976111	),
                        new LatLng(37.406141	,126.976653	),
                        new LatLng(37.405862	,126.977613	),
                        new LatLng(37.405538	,126.978189	),
                        new LatLng(37.405691	,126.978178	),
                        new LatLng(37.405511	,126.978505	),
                        new LatLng(37.405375	,126.978607	),
                        new LatLng(37.405303	,126.978641	),
                        new LatLng(37.404673	,126.978517	),
                        new LatLng(37.404492	,126.97828	),
                        new LatLng(37.404303	,126.977873	),
                        new LatLng(37.403934	,126.97741	),
                        new LatLng(37.403834	,126.977376	),
                        new LatLng(37.403132	,126.977399	),
                        new LatLng(37.403033	,126.977264	),
                        new LatLng(37.403222	,126.977964	),
                        new LatLng(37.403141	,126.97819	),
                        new LatLng(37.402556	,126.980844	),
                        new LatLng(37.402141	,126.980788	),
                        new LatLng(37.401844	,126.981748	),
                        new LatLng(37.401718	,126.981838	),
                        new LatLng(37.401593	,126.981878	),
                        new LatLng(37.401439	,126.981782	),
                        new LatLng(37.401339	,126.981838	),
                        new LatLng(37.401133	,126.9819	),
                        new LatLng(37.400745	,126.981985	),
                        new LatLng(37.400685	,126.982371	),
                        new LatLng(37.396167	,126.97975	),
                        new LatLng(37.394807	,126.982732	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.403955, 126.927608));
    }//안양시 동안구
    public void drawPolygon25(GoogleMap googlemap) { //안양시 만안구
        String name = "안양시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.42207 	,126.948462	),
                        new LatLng(37.419125	,126.941829	),
                        new LatLng(37.414444	,126.93825	),
                        new LatLng(37.415042	,126.929764	),
                        new LatLng(37.408229	,126.927358	),
                        new LatLng(37.399414	,126.929179	),
                        new LatLng(37.391748	,126.941116	),
                        new LatLng(37.379431	,126.940807	),
                        new LatLng(37.376542	,126.938518	),
                        new LatLng(37.37641 	,126.937675	),
                        new LatLng(37.376882	,126.934074	),
                        new LatLng(37.377233	,126.933336	),
                        new LatLng(37.377526	,126.932719	),
                        new LatLng(37.377609	,126.93179	),
                        new LatLng(37.377546	,126.930928	),
                        new LatLng(37.377451	,126.929627	),
                        new LatLng(37.377416	,126.929146	),
                        new LatLng(37.377365	,126.92835	),
                        new LatLng(37.377363	,126.927703	),
                        new LatLng(37.377355	,126.925294	),
                        new LatLng(37.377248	,126.924406	),
                        new LatLng(37.37709 	,126.923509	),
                        new LatLng(37.377032	,126.91927	),
                        new LatLng(37.375945	,126.916696	),
                        new LatLng(37.375803	,126.916202	),
                        new LatLng(37.375682	,126.915785	),
                        new LatLng(37.374699	,126.914453	),
                        new LatLng(37.374608	,126.914412	),
                        new LatLng(37.373374	,126.913848	),
                        new LatLng(37.358969	,126.906648	),
                        new LatLng(37.359956	,126.903775	),
                        new LatLng(37.359483	,126.90213	),
                        new LatLng(37.359029	,126.898564	),
                        new LatLng(37.359294	,126.896904	),
                        new LatLng(37.359489	,126.896383	),
                        new LatLng(37.359486	,126.896378	),
                        new LatLng(37.35991 	,126.895949	),
                        new LatLng(37.360369	,126.895384	),
                        new LatLng(37.361297	,126.894807	),
                        new LatLng(37.362225	,126.89476	),
                        new LatLng(37.362801	,126.894737	),
                        new LatLng(37.363694	,126.894826	),
                        new LatLng(37.364648	,126.894463	),
                        new LatLng(37.367152	,126.892879	),
                        new LatLng(37.367539	,126.892359	),
                        new LatLng(37.3675  	,126.891801	),
                        new LatLng(37.367908	,126.892122	),
                        new LatLng(37.368124	,126.892223	),
                        new LatLng(37.368845	,126.892087	),
                        new LatLng(37.37025 	,126.891735	),
                        new LatLng(37.371178	,126.891869	),
                        new LatLng(37.372106	,126.8911	),
                        new LatLng(37.372367	,126.89128	),
                        new LatLng(37.373268	,126.891098	),
                        new LatLng(37.374709	,126.890723	),
                        new LatLng(37.375448	,126.890621	),
                        new LatLng(37.375843	,126.88948	),
                        new LatLng(37.376041	,126.889174	),
                        new LatLng(37.375905	,126.888396	),
                        new LatLng(37.37591 	,126.888377	),
                        new LatLng(37.376076	,126.887752	),
                        new LatLng(37.376891	,126.884455	),
                        new LatLng(37.376442	,126.883834	),
                        new LatLng(37.376531	,126.88345	),
                        new LatLng(37.377152	,126.882782	),
                        new LatLng(37.377152	,126.882117	),
                        new LatLng(37.377422	,126.881362	),
                        new LatLng(37.377583	,126.881266	),
                        new LatLng(37.378903	,126.881151	),
                        new LatLng(37.379427	,126.880826	),
                        new LatLng(37.379981	,126.880754	),
                        new LatLng(37.3805  	,126.880617	),
                        new LatLng(37.38071 	,126.880561	),
                        new LatLng(37.381637	,126.879862	),
                        new LatLng(37.3827	    ,126.880209	),
                        new LatLng(37.383428	,126.879349	),
                        new LatLng(37.383875	,126.879256	),
                        new LatLng(37.384153	,126.879093	),
                        new LatLng(37.384685	,126.878996	),
                        new LatLng(37.385563	,126.878636	),
                        new LatLng(37.386146	,126.878676	),
                        new LatLng(37.386564	,126.878275	),
                        new LatLng(37.386629	,126.878225	),
                        new LatLng(37.387176	,126.877841	),
                        new LatLng(37.395381	,126.87542	),
                        new LatLng(37.395527	,126.875391	),
                        new LatLng(37.396729	,126.877868	),
                        new LatLng(37.397251	,126.877606	),
                        new LatLng(37.399791	,126.87767	),
                        new LatLng(37.400488	,126.877112	),
                        new LatLng(37.400711	,126.877075	),
                        new LatLng(37.400852	,126.877036	),
                        new LatLng(37.401929	,126.876918	),
                        new LatLng(37.401971	,126.876943	),
                        new LatLng(37.402193	,126.876962	),
                        new LatLng(37.405255	,126.876553	),
                        new LatLng(37.4055  	,126.876347	),
                        new LatLng(37.40559 	,126.876272	),
                        new LatLng(37.406485	,126.875848	),
                        new LatLng(37.406799	,126.871215	),
                        new LatLng(37.407044	,126.870411	),
                        new LatLng(37.407523	,126.870322	),
                        new LatLng(37.408138	,126.870242	),
                        new LatLng(37.409263	,126.870923	),
                        new LatLng(37.410137	,126.870669	),
                        new LatLng(37.410804	,126.870386	),
                        new LatLng(37.411295	,126.870653	),
                        new LatLng(37.412417	,126.871103	),
                        new LatLng(37.412711	,126.871161	),
                        new LatLng(37.412523	,126.871485	),
                        new LatLng(37.411795	,126.872214	),
                        new LatLng(37.411457	,126.874174	),
                        new LatLng(37.411577	,126.87499	),
                        new LatLng(37.411578	,126.875	),
                        new LatLng(37.411158	,126.875327	),
                        new LatLng(37.410867	,126.876056	),
                        new LatLng(37.410394	,126.876039	),
                        new LatLng(37.40932 	,126.877629	),
                        new LatLng(37.408894	,126.87837	),
                        new LatLng(37.408965	,126.879147	),
                        new LatLng(37.41014 	,126.879909	),
                        new LatLng(37.410382	,126.880316	),
                        new LatLng(37.410639	,126.880703	),
                        new LatLng(37.410846	,126.881313	),
                        new LatLng(37.410841	,126.881728	),
                        new LatLng(37.410795	,126.881948	),
                        new LatLng(37.41079 	,126.882139	),
                        new LatLng(37.411493	,126.884104	),
                        new LatLng(37.411589	,126.884284	),
                        new LatLng(37.411602	,126.884614	),
                        new LatLng(37.411644	,126.884702	),
                        new LatLng(37.411663	,126.884809	),
                        new LatLng(37.411616	,126.884795	),
                        new LatLng(37.411649	,126.885023	),
                        new LatLng(37.411671	,126.885182	),
                        new LatLng(37.411688	,126.885307	),
                        new LatLng(37.411701	,126.885442	),
                        new LatLng(37.411564	,126.88587	),
                        new LatLng(37.411124	,126.886713	),
                        new LatLng(37.411159	,126.887875	),
                        new LatLng(37.411335	,126.888644	),
                        new LatLng(37.411977	,126.889141	),
                        new LatLng(37.412385	,126.889268	),
                        new LatLng(37.412463	,126.889272	),
                        new LatLng(37.412486	,126.889263	),
                        new LatLng(37.413283	,126.889357	),
                        new LatLng(37.41328 	,126.889404	),
                        new LatLng(37.413314	,126.889409	),
                        new LatLng(37.413353	,126.889415	),
                        new LatLng(37.413887	,126.889333	),
                        new LatLng(37.414359	,126.889119	),
                        new LatLng(37.414854	,126.889173	),
                        new LatLng(37.414956	,126.889182	),
                        new LatLng(37.415291	,126.889222	),
                        new LatLng(37.415533	,126.889365	),
                        new LatLng(37.415774	,126.889518	),
                        new LatLng(37.415825	,126.889547	),
                        new LatLng(37.416185	,126.889626	),
                        new LatLng(37.416195	,126.889608	),
                        new LatLng(37.416215	,126.889501	),
                        new LatLng(37.417439	,126.889744	),
                        new LatLng(37.417624	,126.889769	),
                        new LatLng(37.417649	,126.8898	),
                        new LatLng(37.417771	,126.889819	),
                        new LatLng(37.418007	,126.889336	),
                        new LatLng(37.41813 	,126.889126	),
                        new LatLng(37.41861 	,126.88891	),
                        new LatLng(37.41917 	,126.889355	),
                        new LatLng(37.419658	,126.890066	),
                        new LatLng(37.419924	,126.890512	),
                        new LatLng(37.420291	,126.891189	),
                        new LatLng(37.420533	,126.891761	),
                        new LatLng(37.420419	,126.892601	),
                        new LatLng(37.420228	,126.893592	),
                        new LatLng(37.420436	,126.89384	),
                        new LatLng(37.420535	,126.893914	),
                        new LatLng(37.420916	,126.893942	),
                        new LatLng(37.421177	,126.893906	),
                        new LatLng(37.422526	,126.894087	),
                        new LatLng(37.42365 	,126.894953	),
                        new LatLng(37.424169	,126.894973	),
                        new LatLng(37.424385	,126.895004	),
                        new LatLng(37.424775	,126.895617	),
                        new LatLng(37.424963	,126.896045	),
                        new LatLng(37.425184	,126.896434	),
                        new LatLng(37.426039	,126.896287	),
                        new LatLng(37.42634 	,126.896156	),
                        new LatLng(37.426394	,126.896172	),
                        new LatLng(37.426562	,126.896311	),
                        new LatLng(37.426772	,126.896613	),
                        new LatLng(37.426848	,126.896719	),
                        new LatLng(37.42719 	,126.896892	),
                        new LatLng(37.427421	,126.896814	),
                        new LatLng(37.427837	,126.895804	),
                        new LatLng(37.427727	,126.895542	),
                        new LatLng(37.427523	,126.895337	),
                        new LatLng(37.427358	,126.894836	),
                        new LatLng(37.427575	,126.894351	),
                        new LatLng(37.427801	,126.894215	),
                        new LatLng(37.427962	,126.89423	),
                        new LatLng(37.428278	,126.894507	),
                        new LatLng(37.428324	,126.894521	),
                        new LatLng(37.428845	,126.894336	),
                        new LatLng(37.429017	,126.894263	),
                        new LatLng(37.429187	,126.894417	),
                        new LatLng(37.429534	,126.89465	),
                        new LatLng(37.429681	,126.894665	),
                        new LatLng(37.430369	,126.895529	),
                        new LatLng(37.430569	,126.895799	),
                        new LatLng(37.430596	,126.895832	),
                        new LatLng(37.430775	,126.895992	),
                        new LatLng(37.431045	,126.896235	),
                        new LatLng(37.431197	,126.896505	),
                        new LatLng(37.43116 	,126.896889	),
                        new LatLng(37.430954	,126.897446	),
                        new LatLng(37.431251	,126.898124	),
                        new LatLng(37.431269	,126.898137	),
                        new LatLng(37.43172 	,126.898247	),
                        new LatLng(37.431881	,126.898319	),
                        new LatLng(37.432399	,126.898472	),
                        new LatLng(37.432728	,126.898349	),
                        new LatLng(37.433327	,126.898715	),
                        new LatLng(37.433401	,126.899207	),
                        new LatLng(37.434031	,126.899152	),
                        new LatLng(37.434827	,126.899841	),
                        new LatLng(37.434995	,126.89961	),
                        new LatLng(37.435081	,126.899417	),
                        new LatLng(37.435607	,126.899554	),
                        new LatLng(37.43583 	,126.899596	),
                        new LatLng(37.435883	,126.899556	),
                        new LatLng(37.436103	,126.899427	),
                        new LatLng(37.436745	,126.899498	),
                        new LatLng(37.438025	,126.899529	),
                        new LatLng(37.438092	,126.899439	),
                        new LatLng(37.438114	,126.899409	),
                        new LatLng(37.438662	,126.899039	),
                        new LatLng(37.438652	,126.899017	),
                        new LatLng(37.438702	,126.898978	),
                        new LatLng(37.438686	,126.898999	),
                        new LatLng(37.438631	,126.899185	),
                        new LatLng(37.438504	,126.899327	),
                        new LatLng(37.438351	,126.899512	),
                        new LatLng(37.438052	,126.899884	),
                        new LatLng(37.437826	,126.900211	),
                        new LatLng(37.437733	,126.900337	),
                        new LatLng(37.43742 	,126.900508	),
                        new LatLng(37.437323	,126.900547	),
                        new LatLng(37.436984	,126.900849	),
                        new LatLng(37.436714	,126.901388	),
                        new LatLng(37.436387	,126.901879	),
                        new LatLng(37.436284	,126.902023	),
                        new LatLng(37.436024	,126.902464	),
                        new LatLng(37.436009	,126.902484	),
                        new LatLng(37.435963	,126.902586	),
                        new LatLng(37.435878	,126.902706	),
                        new LatLng(37.435857	,126.90275	),
                        new LatLng(37.435642	,126.90276	),
                        new LatLng(37.435642	,126.902803	),
                        new LatLng(37.435457	,126.902801	),
                        new LatLng(37.434892	,126.902784	),
                        new LatLng(37.434776	,126.902789	),
                        new LatLng(37.434614	,126.902822	),
                        new LatLng(37.434232	,126.902926	),
                        new LatLng(37.434187	,126.902943	),
                        new LatLng(37.434081	,126.902989	),
                        new LatLng(37.434068	,126.902988	),
                        new LatLng(37.434112	,126.903386	),
                        new LatLng(37.434163	,126.903472	),
                        new LatLng(37.433997	,126.905225	),
                        new LatLng(37.433998	,126.905391	),
                        new LatLng(37.433996	,126.905844	),
                        new LatLng(37.433966	,126.906137	),
                        new LatLng(37.433757	,126.906498	),
                        new LatLng(37.433673	,126.906748	),
                        new LatLng(37.433699	,126.906927	),
                        new LatLng(37.433695	,126.906935	),
                        new LatLng(37.433547	,126.907089	),
                        new LatLng(37.433518	,126.907254	),
                        new LatLng(37.43362 	,126.907527	),
                        new LatLng(37.433725	,126.907934	),
                        new LatLng(37.433694	,126.908399	),
                        new LatLng(37.433682	,126.908549	),
                        new LatLng(37.433684	,126.90856	),
                        new LatLng(37.43386 	,126.909396	),
                        new LatLng(37.433865	,126.909406	),
                        new LatLng(37.434227	,126.909904	),
                        new LatLng(37.434282	,126.90997	),
                        new LatLng(37.434614	,126.910141	),
                        new LatLng(37.435676	,126.910852	),
                        new LatLng(37.437187	,126.9112	),
                        new LatLng(37.439198	,126.913536	),
                        new LatLng(37.439401	,126.914873	),
                        new LatLng(37.439645	,126.915346	),
                        new LatLng(37.440045	,126.916109	),
                        new LatLng(37.440285	,126.919977	),
                        new LatLng(37.440471	,126.920279	),
                        new LatLng(37.440985	,126.920497	),
                        new LatLng(37.441428	,126.920668	),
                        new LatLng(37.443251	,126.922007	),
                        new LatLng(37.44356 	,126.922247	),
                        new LatLng(37.444673	,126.922903	),
                        new LatLng(37.445173	,126.922897	),
                        new LatLng(37.445773	,126.923253	),
                        new LatLng(37.447206	,126.925463	),
                        new LatLng(37.448158	,126.926354	),
                        new LatLng(37.448391	,126.926518	),
                        new LatLng(37.449336	,126.928256	),
                        new LatLng(37.449549	,126.928308	),
                        new LatLng(37.449899	,126.928357	),
                        new LatLng(37.450212	,126.928399	),
                        new LatLng(37.450201	,126.928416	),
                        new LatLng(37.449472	,126.929286	),
                        new LatLng(37.449093	,126.929607	),
                        new LatLng(37.448881	,126.929786	),
                        new LatLng(37.448544	,126.930047	),
                        new LatLng(37.447393	,126.930365	),
                        new LatLng(37.446654	,126.930266	),
                        new LatLng(37.446378	,126.930163	),
                        new LatLng(37.446153	,126.930282	),
                        new LatLng(37.446142	,126.930285	),
                        new LatLng(37.445818	,126.930394	),
                        new LatLng(37.445468	,126.930554	),
                        new LatLng(37.445466	,126.93056	),
                        new LatLng(37.445136	,126.931326	),
                        new LatLng(37.443896	,126.933469	),
                        new LatLng(37.443258	,126.934451	),
                        new LatLng(37.443213	,126.934894	),
                        new LatLng(37.443209	,126.934898	),
                        new LatLng(37.442928	,126.93511	),
                        new LatLng(37.442898	,126.935126	),
                        new LatLng(37.442497	,126.935441	),
                        new LatLng(37.442178	,126.935643	),
                        new LatLng(37.442142	,126.935986	),
                        new LatLng(37.441954	,126.936202	),
                        new LatLng(37.441438	,126.936812	),
                        new LatLng(37.441299	,126.936949	),
                        new LatLng(37.440198	,126.937864	),
                        new LatLng(37.439866	,126.937758	),
                        new LatLng(37.438632	,126.937273	),
                        new LatLng(37.438327	,126.937563	),
                        new LatLng(37.438094	,126.937656	),
                        new LatLng(37.437879	,126.937744	),
                        new LatLng(37.437582	,126.937742	),
                        new LatLng(37.437392	,126.937751	),
                        new LatLng(37.437382	,126.937752	),
                        new LatLng(37.437209	,126.937989	),
                        new LatLng(37.437184	,126.938015	),
                        new LatLng(37.43642 	,126.938575	),
                        new LatLng(37.436096	,126.938615	),
                        new LatLng(37.435712	,126.94022	),
                        new LatLng(37.435802	,126.940427	),
                        new LatLng(37.435982	,126.940472	),
                        new LatLng(37.436123	,126.940503	),
                        new LatLng(37.43628 	,126.94053	),
                        new LatLng(37.436479	,126.940708	),
                        new LatLng(37.436918	,126.941103	),
                        new LatLng(37.437404	,126.941424	),
                        new LatLng(37.437506	,126.94198	),
                        new LatLng(37.437088	,126.94509	),
                        new LatLng(37.437305	,126.945676	),
                        new LatLng(37.438167	,126.946789	),
                        new LatLng(37.438412	,126.947599	),
                        new LatLng(37.438492	,126.947854	),
                        new LatLng(37.438576	,126.948034	),
                        new LatLng(37.43863 	,126.948155	),
                        new LatLng(37.42207 	,126.948462	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.403955, 126.927608));
    }//안양시 만안구
    public void drawPolygon45(GoogleMap googlemap) { //성남시 분당구
        String name = "성남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.387099	,127.177235	),
                        new LatLng(37.385206	,127.176557	),
                        new LatLng(37.380299	,127.167921	),
                        new LatLng(37.37969 	,127.161822	),
                        new LatLng(37.372682	,127.160592	),
                        new LatLng(37.36342 	,127.152746	),
                        new LatLng(37.359541	,127.145053	),
                        new LatLng(37.358982	,127.136246	),
                        new LatLng(37.353828	,127.134258	),
                        new LatLng(37.353268	,127.13437	),
                        new LatLng(37.349282	,127.132693	),
                        new LatLng(37.349184	,127.132727	),
                        new LatLng(37.344508	,127.137373	),
                        new LatLng(37.339722	,127.137872	),
                        new LatLng(37.339678	,127.137974	),
                        new LatLng(37.339668	,127.137973	),
                        new LatLng(37.339376	,127.137956	),
                        new LatLng(37.339449	,127.137426	),
                        new LatLng(37.339674	,127.137054	),
                        new LatLng(37.339482	,127.135434	),
                        new LatLng(37.339115	,127.13499	),
                        new LatLng(37.338625	,127.13401	),
                        new LatLng(37.338772	,127.13316	),
                        new LatLng(37.338705	,127.132888	),
                        new LatLng(37.338785	,127.132349	),
                        new LatLng(37.338752	,127.132212	),
                        new LatLng(37.338536	,127.131826	),
                        new LatLng(37.338528	,127.131797	),
                        new LatLng(37.33842 	,127.131664	),
                        new LatLng(37.338154	,127.131455	),
                        new LatLng(37.337962	,127.131306	),
                        new LatLng(37.337913	,127.131265	),
                        new LatLng(37.337675	,127.130892	),
                        new LatLng(37.337409	,127.125972	),
                        new LatLng(37.33752 	,127.125501	),
                        new LatLng(37.33734 	,127.125438	),
                        new LatLng(37.33712 	,127.12527	),
                        new LatLng(37.336817	,127.124867	),
                        new LatLng(37.336742	,127.123736	),
                        new LatLng(37.336846	,127.123324	),
                        new LatLng(37.337018	,127.122775	),
                        new LatLng(37.336519	,127.122108	),
                        new LatLng(37.335238	,127.121486	),
                        new LatLng(37.334997	,127.121383	),
                        new LatLng(37.334832	,127.12133	),
                        new LatLng(37.334598	,127.121167	),
                        new LatLng(37.334569	,127.121127	),
                        new LatLng(37.334398	,127.120846	),
                        new LatLng(37.334091	,127.119743	),
                        new LatLng(37.333967	,127.119225	),
                        new LatLng(37.33389 	,127.118512	),
                        new LatLng(37.333887	,127.118251	),
                        new LatLng(37.333892	,127.118188	),
                        new LatLng(37.333893	,127.118166	),
                        new LatLng(37.33389 	,127.118124	),
                        new LatLng(37.333896	,127.117872	),
                        new LatLng(37.333897	,127.117838	),
                        new LatLng(37.33342 	,127.117443	),
                        new LatLng(37.333375	,127.117036	),
                        new LatLng(37.333375	,127.116924	),
                        new LatLng(37.333887	,127.116408	),
                        new LatLng(37.33418 	,127.115795	),
                        new LatLng(37.334467	,127.115312	),
                        new LatLng(37.334683	,127.115141	),
                        new LatLng(37.334888	,127.11505	),
                        new LatLng(37.335109	,127.114785	),
                        new LatLng(37.335354	,127.114437	),
                        new LatLng(37.335383	,127.11438	),
                        new LatLng(37.335645	,127.113768	),
                        new LatLng(37.335675	,127.113702	),
                        new LatLng(37.335769	,127.113144	),
                        new LatLng(37.335802	,127.113068	),
                        new LatLng(37.335753	,127.11304	),
                        new LatLng(37.335463	,127.113124	),
                        new LatLng(37.335301	,127.113194	),
                        new LatLng(37.335201	,127.113279	),
                        new LatLng(37.335745	,127.112455	),
                        new LatLng(37.335702	,127.112295	),
                        new LatLng(37.335554	,127.111881	),
                        new LatLng(37.335482	,127.1117	),
                        new LatLng(37.335314	,127.11134	),
                        new LatLng(37.335159	,127.111126	),
                        new LatLng(37.334925	,127.110914	),
                        new LatLng(37.334913	,127.110904	),
                        new LatLng(37.334675	,127.110798	),
                        new LatLng(37.334617	,127.110758	),
                        new LatLng(37.334503	,127.110623	),
                        new LatLng(37.334651	,127.110199	),
                        new LatLng(37.334708	,127.110117	),
                        new LatLng(37.334757	,127.110018	),
                        new LatLng(37.334803	,127.109797	),
                        new LatLng(37.335121	,127.109501	),
                        new LatLng(37.335283	,127.109491	),
                        new LatLng(37.335382	,127.109446	),
                        new LatLng(37.335449	,127.109383	),
                        new LatLng(37.335488	,127.109315	),
                        new LatLng(37.335526	,127.109238	),
                        new LatLng(37.335611	,127.109193	),
                        new LatLng(37.335655	,127.109168	),
                        new LatLng(37.335662	,127.109163	),
                        new LatLng(37.335784	,127.109065	),
                        new LatLng(37.335806	,127.108983	),
                        new LatLng(37.335804	,127.108836	),
                        new LatLng(37.335801	,127.108747	),
                        new LatLng(37.335797	,127.10867	),
                        new LatLng(37.335798	,127.108667	),
                        new LatLng(37.335804	,127.108646	),
                        new LatLng(37.33582 	,127.108555	),
                        new LatLng(37.33582 	,127.108553	),
                        new LatLng(37.335865	,127.108387	),
                        new LatLng(37.335887	,127.108287	),
                        new LatLng(37.335964	,127.108141	),
                        new LatLng(37.336081	,127.107952	),
                        new LatLng(37.336212	,127.107853	),
                        new LatLng(37.336422	,127.107689	),
                        new LatLng(37.336713	,127.10732	),
                        new LatLng(37.336867	,127.107118	),
                        new LatLng(37.336952	,127.107002	),
                        new LatLng(37.337215	,127.106499	),
                        new LatLng(37.337496	,127.105968	),
                        new LatLng(37.337545	,127.105864	),
                        new LatLng(37.338059	,127.104718	),
                        new LatLng(37.338362	,127.104385	),
                        new LatLng(37.338414	,127.104225	),
                        new LatLng(37.338432	,127.103953	),
                        new LatLng(37.338485	,127.103872	),
                        new LatLng(37.338566	,127.10378	),
                        new LatLng(37.338651	,127.103722	),
                        new LatLng(37.338954	,127.10362	),
                        new LatLng(37.339047	,127.103588	),
                        new LatLng(37.339228	,127.103499	),
                        new LatLng(37.339487	,127.103355	),
                        new LatLng(37.339802	,127.102859	),
                        new LatLng(37.33984 	,127.10283	),
                        new LatLng(37.339889	,127.102797	),
                        new LatLng(37.339943	,127.102745	),
                        new LatLng(37.340162	,127.102542	),
                        new LatLng(37.340303	,127.102409	),
                        new LatLng(37.340552	,127.102328	),
                        new LatLng(37.340651	,127.102338	),
                        new LatLng(37.341048	,127.10239	),
                        new LatLng(37.341325	,127.102424	),
                        new LatLng(37.341867	,127.102441	),
                        new LatLng(37.342027	,127.102409	),
                        new LatLng(37.342323	,127.102319	),
                        new LatLng(37.342647	,127.102143	),
                        new LatLng(37.342924	,127.101911	),
                        new LatLng(37.343025	,127.101866	),
                        new LatLng(37.343111	,127.10185	),
                        new LatLng(37.342923	,127.100067	),
                        new LatLng(37.342914	,127.100168	),
                        new LatLng(37.342905	,127.100214	),
                        new LatLng(37.342923	,127.100541	),
                        new LatLng(37.342806	,127.100507	),
                        new LatLng(37.342725	,127.100484	),
                        new LatLng(37.342689	,127.100292	),
                        new LatLng(37.342626	,127.100156	),
                        new LatLng(37.342672	,127.099141	),
                        new LatLng(37.342726	,127.099175	),
                        new LatLng(37.342726	,127.099209	),
                        new LatLng(37.342761	,127.099412	),
                        new LatLng(37.342806	,127.09957	),
                        new LatLng(37.343057	,127.097659	),
                        new LatLng(37.343352	,127.097329	),
                        new LatLng(37.344241	,127.09623	),
                        new LatLng(37.344819	,127.095499	),
                        new LatLng(37.345349	,127.094827	),
                        new LatLng(37.345482	,127.094391	),
                        new LatLng(37.345522	,127.094136	),
                        new LatLng(37.345559	,127.093861	),
                        new LatLng(37.345895	,127.093183	),
                        new LatLng(37.346002	,127.093145	),
                        new LatLng(37.346223	,127.093078	),
                        new LatLng(37.346235	,127.093077	),
                        new LatLng(37.346713	,127.09314	),
                        new LatLng(37.346958	,127.092989	),
                        new LatLng(37.347081	,127.092797	),
                        new LatLng(37.34781 	,127.091054	),
                        new LatLng(37.348063	,127.090031	),
                        new LatLng(37.348134	,127.089632	),
                        new LatLng(37.348166	,127.089442	),
                        new LatLng(37.348518	,127.088821	),
                        new LatLng(37.348976	,127.088165	),
                        new LatLng(37.349716	,127.087353	),
                        new LatLng(37.350101	,127.087166	),
                        new LatLng(37.350344	,127.087052	),
                        new LatLng(37.350917	,127.08684	),
                        new LatLng(37.351851	,127.0843	),
                        new LatLng(37.351838	,127.083328	),
                        new LatLng(37.350775	,127.081682	),
                        new LatLng(37.350695	,127.08155	),
                        new LatLng(37.350282	,127.080975	),
                        new LatLng(37.349607	,127.078101	),
                        new LatLng(37.349611	,127.078086	),
                        new LatLng(37.350038	,127.077102	),
                        new LatLng(37.350545	,127.076258	),
                        new LatLng(37.350932	,127.076089	),
                        new LatLng(37.352096	,127.075613	),
                        new LatLng(37.352377	,127.075484	),
                        new LatLng(37.352989	,127.075268	),
                        new LatLng(37.354477	,127.074466	),
                        new LatLng(37.354791	,127.074295	),
                        new LatLng(37.354907	,127.074233	),
                        new LatLng(37.355107	,127.074151	),
                        new LatLng(37.355451	,127.07401	),
                        new LatLng(37.356866	,127.072479	),
                        new LatLng(37.357038	,127.072262	),
                        new LatLng(37.357396	,127.071838	),
                        new LatLng(37.357585	,127.071422	),
                        new LatLng(37.357987	,127.070598	),
                        new LatLng(37.358227	,127.069978	),
                        new LatLng(37.358233	,127.069964	),
                        new LatLng(37.358461	,127.069313	),
                        new LatLng(37.358786	,127.068242	),
                        new LatLng(37.358878	,127.067904	),
                        new LatLng(37.358957	,127.067609	),
                        new LatLng(37.359345	,127.066625	),
                        new LatLng(37.359392	,127.066426	),
                        new LatLng(37.359483	,127.066042	),
                        new LatLng(37.359522	,127.065877	),
                        new LatLng(37.359595	,127.065596	),
                        new LatLng(37.360045	,127.064163	),
                        new LatLng(37.360187	,127.063817	),
                        new LatLng(37.360441	,127.063186	),
                        new LatLng(37.360446	,127.062851	),
                        new LatLng(37.360446	,127.062488	),
                        new LatLng(37.360393	,127.062039	),
                        new LatLng(37.360685	,127.061058	),
                        new LatLng(37.360771	,127.060377	),
                        new LatLng(37.360774	,127.059916	),
                        new LatLng(37.360746	,127.058132	),
                        new LatLng(37.360706	,127.05774	),
                        new LatLng(37.360538	,127.057212	),
                        new LatLng(37.360421	,127.056851	),
                        new LatLng(37.360082	,127.056037	),
                        new LatLng(37.359954	,127.055727	),
                        new LatLng(37.359946	,127.055548	),
                        new LatLng(37.359964	,127.055239	),
                        new LatLng(37.359964	,127.055229	),
                        new LatLng(37.359995	,127.055052	),
                        new LatLng(37.360126	,127.054289	),
                        new LatLng(37.360479	,127.053667	),
                        new LatLng(37.360573	,127.053545	),
                        new LatLng(37.361685	,127.052935	),
                        new LatLng(37.362137	,127.052897	),
                        new LatLng(37.363775	,127.052686	),
                        new LatLng(37.364017	,127.052398	),
                        new LatLng(37.364257	,127.051971	),
                        new LatLng(37.364866	,127.050891	),
                        new LatLng(37.364876	,127.050865	),
                        new LatLng(37.364883	,127.050752	),
                        new LatLng(37.364928	,127.050286	),
                        new LatLng(37.365165	,127.050133	),
                        new LatLng(37.365211	,127.049994	),
                        new LatLng(37.365244	,127.049891	),
                        new LatLng(37.365261	,127.049824	),
                        new LatLng(37.365314	,127.049542	),
                        new LatLng(37.365332	,127.049484	),
                        new LatLng(37.365693	,127.04908	),
                        new LatLng(37.365846	,127.048823	),
                        new LatLng(37.365999	,127.048554	),
                        new LatLng(37.366267	,127.047998	),
                        new LatLng(37.366358	,127.047781	),
                        new LatLng(37.366469	,127.047485	),
                        new LatLng(37.366881	,127.047005	),
                        new LatLng(37.366934	,127.046969	),
                        new LatLng(37.367429	,127.046735	),
                        new LatLng(37.367588	,127.04663	),
                        new LatLng(37.367653	,127.046584	),
                        new LatLng(37.367749	,127.046516	),
                        new LatLng(37.368298	,127.046073	),
                        new LatLng(37.368314	,127.045943	),
                        new LatLng(37.368297	,127.045704	),
                        new LatLng(37.368318	,127.045656	),
                        new LatLng(37.368456	,127.045384	),
                        new LatLng(37.368545	,127.045094	),
                        new LatLng(37.368631	,127.045033	),
                        new LatLng(37.368731	,127.044962	),
                        new LatLng(37.36882 	,127.044834	),
                        new LatLng(37.368811	,127.044569	),
                        new LatLng(37.368803	,127.044378	),
                        new LatLng(37.368979	,127.043463	),
                        new LatLng(37.369024	,127.043328	),
                        new LatLng(37.369035	,127.043135	),
                        new LatLng(37.369041	,127.042977	),
                        new LatLng(37.369032	,127.042741	),
                        new LatLng(37.369031	,127.042718	),
                        new LatLng(37.369027	,127.042436	),
                        new LatLng(37.369035	,127.042339	),
                        new LatLng(37.369237	,127.041665	),
                        new LatLng(37.369407	,127.040314	),
                        new LatLng(37.369355	,127.039921	),
                        new LatLng(37.369673	,127.038347	),
                        new LatLng(37.369697	,127.038279	),
                        new LatLng(37.369953	,127.037892	),
                        new LatLng(37.370004	,127.037798	),
                        new LatLng(37.370038	,127.037738	),
                        new LatLng(37.370192	,127.03748	),
                        new LatLng(37.370183	,127.03726	),
                        new LatLng(37.370388	,127.036759	),
                        new LatLng(37.370425	,127.036652	),
                        new LatLng(37.370447	,127.036416	),
                        new LatLng(37.37049 	,127.035988	),
                        new LatLng(37.37049 	,127.035985	),
                        new LatLng(37.370599	,127.035701	),
                        new LatLng(37.370644	,127.03558	),
                        new LatLng(37.37067 	,127.035455	),
                        new LatLng(37.370545	,127.035178	),
                        new LatLng(37.370533	,127.03503	),
                        new LatLng(37.370535	,127.034912	),
                        new LatLng(37.370659	,127.034691	),
                        new LatLng(37.370681	,127.034664	),
                        new LatLng(37.370948	,127.034507	),
                        new LatLng(37.371121	,127.034008	),
                        new LatLng(37.371129	,127.033974	),
                        new LatLng(37.371219	,127.033625	),
                        new LatLng(37.371286	,127.033115	),
                        new LatLng(37.371311	,127.033016	),
                        new LatLng(37.371479	,127.032542	),
                        new LatLng(37.371523	,127.032324	),
                        new LatLng(37.371535	,127.031935	),
                        new LatLng(37.371663	,127.031748	),
                        new LatLng(37.371744	,127.031466	),
                        new LatLng(37.371761	,127.031308	),
                        new LatLng(37.371794	,127.031233	),
                        new LatLng(37.371831	,127.031143	),
                        new LatLng(37.371834	,127.031136	),
                        new LatLng(37.371816	,127.030954	),
                        new LatLng(37.37186 	,127.030868	),
                        new LatLng(37.371895	,127.030764	),
                        new LatLng(37.371931	,127.030697	),
                        new LatLng(37.372073	,127.030583	),
                        new LatLng(37.372122	,127.030369	),
                        new LatLng(37.3721 	    ,127.030233	),
                        new LatLng(37.372111	,127.030039	),
                        new LatLng(37.372095	,127.029996	),
                        new LatLng(37.372109	,127.02991	),
                        new LatLng(37.37214	    ,127.029793	),
                        new LatLng(37.372127	,127.029748	),
                        new LatLng(37.372131	,127.029655	),
                        new LatLng(37.372192	,127.029529	),
                        new LatLng(37.372244	,127.029409	),
                        new LatLng(37.37232 	,127.02926	),
                        new LatLng(37.372249	,127.029208	),
                        new LatLng(37.372041	,127.028827	),
                        new LatLng(37.372022	,127.028788	),
                        new LatLng(37.371951	,127.028658	),
                        new LatLng(37.371942	,127.028641	),
                        new LatLng(37.37196 	,127.027916	),
                        new LatLng(37.371965	,127.027913	),
                        new LatLng(37.372263	,127.027831	),
                        new LatLng(37.372547	,127.027762	),
                        new LatLng(37.372866	,127.027706	),
                        new LatLng(37.374036	,127.027898	),
                        new LatLng(37.375631	,127.027918	),
                        new LatLng(37.376006	,127.028005	),
                        new LatLng(37.376007	,127.028006	),
                        new LatLng(37.376657	,127.028345	),
                        new LatLng(37.377587	,127.028013	),
                        new LatLng(37.378286	,127.028044	),
                        new LatLng(37.378484	,127.028172	),
                        new LatLng(37.379173	,127.028192	),
                        new LatLng(37.379373	,127.02874	),
                        new LatLng(37.379551	,127.029277	),
                        new LatLng(37.379542	,127.029754	),
                        new LatLng(37.379362	,127.031398	),
                        new LatLng(37.38003 	,127.031898	),
                        new LatLng(37.38021 	,127.03205	),
                        new LatLng(37.380337	,127.032163	),
                        new LatLng(37.380362	,127.032174	),
                        new LatLng(37.383059	,127.0331	),
                        new LatLng(37.383118	,127.033134	),
                        new LatLng(37.383614	,127.033113	),
                        new LatLng(37.383903	,127.033191	),
                        new LatLng(37.384621	,127.03381	),
                        new LatLng(37.385389	,127.033862	),
                        new LatLng(37.385429	,127.033874	),
                        new LatLng(37.38576 	,127.033941	),
                        new LatLng(37.385981	,127.034925	),
                        new LatLng(37.386326	,127.035265	),
                        new LatLng(37.387971	,127.035167	),
                        new LatLng(37.38824 	,127.034985	),
                        new LatLng(37.388262	,127.03497	),
                        new LatLng(37.388664	,127.034717	),
                        new LatLng(37.388812	,127.035355	),
                        new LatLng(37.389379	,127.036846	),
                        new LatLng(37.38968 	,127.037397	),
                        new LatLng(37.390505	,127.038416	),
                        new LatLng(37.390857	,127.038439	),
                        new LatLng(37.391938	,127.038733	),
                        new LatLng(37.392226	,127.038496	),
                        new LatLng(37.393028	,127.038372	),
                        new LatLng(37.393578	,127.038237	),
                        new LatLng(37.393686	,127.038214	),
                        new LatLng(37.394767	,127.038192	),
                        new LatLng(37.395245	,127.038102	),
                        new LatLng(37.396101	,127.038227	),
                        new LatLng(37.396605	,127.038148	),
                        new LatLng(37.397245	,127.038069	),
                        new LatLng(37.397695	,127.038103	),
                        new LatLng(37.39856 	,127.037991	),
                        new LatLng(37.401759	,127.03745	),
                        new LatLng(37.402182	,127.038422	),
                        new LatLng(37.402326	,127.03998	),
                        new LatLng(37.402587	,127.040624	),
                        new LatLng(37.403425	,127.042003	),
                        new LatLng(37.403884	,127.043245	),
                        new LatLng(37.40243 	,127.056148	),
                        new LatLng(37.395605	,127.076372	),
                        new LatLng(37.398108	,127.081254	),
                        new LatLng(37.397115	,127.084157	),
                        new LatLng(37.406847	,127.094505	),
                        new LatLng(37.406903	,127.099789	),
                        new LatLng(37.409722	,127.102446	),
                        new LatLng(37.407057	,127.107139	),
                        new LatLng(37.40865 	,127.115931	),
                        new LatLng(37.41349 	,127.120472	),
                        new LatLng(37.411554	,127.117666	),
                        new LatLng(37.415661	,127.119206	),
                        new LatLng(37.416998	,127.134435	),
                        new LatLng(37.41509 	,127.143369	),
                        new LatLng(37.410798	,127.149256	),
                        new LatLng(37.411099	,127.155638	),
                        new LatLng(37.406477	,127.165071	),
                        new LatLng(37.4055  	,127.173875	),
                        new LatLng(37.392633	,127.168245	),
                        new LatLng(37.387099	,127.177235	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.408563, 127.116230));
    }//성남시 분당구
    public void drawPolygon51(GoogleMap googlemap) { //
        String name = "시흥시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.377583	,126.881266	),
                        new LatLng(37.377466	,126.881247	),
                        new LatLng(37.369548	,126.874576	),
                        new LatLng(37.369377	,126.874317	),
                        new LatLng(37.364379	,126.868387	),
                        new LatLng(37.364324	,126.868331	),
                        new LatLng(37.359453	,126.855833	),
                        new LatLng(37.364767	,126.848242	),
                        new LatLng(37.364759	,126.848113	),
                        new LatLng(37.362786	,126.841468	),
                        new LatLng(37.362968	,126.829524	),
                        new LatLng(37.363175	,126.829298	),
                        new LatLng(37.365907	,126.82428	),
                        new LatLng(37.36569 	,126.824066	),
                        new LatLng(37.361828	,126.820463	),
                        new LatLng(37.361774	,126.820237	),
                        new LatLng(37.358706	,126.817581	),
                        new LatLng(37.35849 	,126.817322	),
                        new LatLng(37.355451	,126.816279	),
                        new LatLng(37.357014	,126.813047	),
                        new LatLng(37.356449	,126.812093	),
                        new LatLng(37.356084	,126.811898	),
                        new LatLng(37.355615	,126.811504	),
                        new LatLng(37.35569 	,126.807542	),
                        new LatLng(37.352911	,126.805168	),
                        new LatLng(37.355075	,126.79541	),
                        new LatLng(37.350272	,126.790288	),
                        new LatLng(37.350006	,126.790309	),
                        new LatLng(37.343574	,126.793185	),
                        new LatLng(37.336746	,126.784391	),
                        new LatLng(37.340411	,126.783432	),
                        new LatLng(37.342723	,126.767276	),
                        new LatLng(37.342716	,126.767139	),
                        new LatLng(37.341133	,126.760612	),
                        new LatLng(37.340056	,126.75965	),
                        new LatLng(37.338003	,126.757601	),
                        new LatLng(37.336913	,126.755637	),
                        new LatLng(37.336618	,126.755092	),
                        new LatLng(37.335686	,126.753357	),
                        new LatLng(37.333499	,126.749674	),
                        new LatLng(37.329683	,126.748564	),
                        new LatLng(37.324723	,126.74655	),
                        new LatLng(37.32967 	,126.738544	),
                        new LatLng(37.314594	,126.723924	),
                        new LatLng(37.334074	,126.691643	),
                        new LatLng(37.339246	,126.691742	),
                        new LatLng(37.341148	,126.685954	),
                        new LatLng(37.345099	,126.687352	),
                        new LatLng(37.345021	,126.685384	),
                        new LatLng(37.348619	,126.688681	),
                        new LatLng(37.345229	,126.696289	),
                        new LatLng(37.346475	,126.698951	),
                        new LatLng(37.351282	,126.703299	),
                        new LatLng(37.354457	,126.700818	),
                        new LatLng(37.385962	,126.730443	),
                        new LatLng(37.382474	,126.742073	),
                        new LatLng(37.388858	,126.744955	),
                        new LatLng(37.384562	,126.741966	),
                        new LatLng(37.386975	,126.740875	),
                        new LatLng(37.388984	,126.733913	),
                        new LatLng(37.398361	,126.743929	),
                        new LatLng(37.397377	,126.76222	),
                        new LatLng(37.390911	,126.763003	),
                        new LatLng(37.394108	,126.764319	),
                        new LatLng(37.39414 	,126.772795	),
                        new LatLng(37.39186 	,126.777194	),
                        new LatLng(37.394779	,126.780016	),
                        new LatLng(37.39098 	,126.784701	),
                        new LatLng(37.388427	,126.782283	),
                        new LatLng(37.395471	,126.789511	),
                        new LatLng(37.393105	,126.791882	),
                        new LatLng(37.401993	,126.78893	),
                        new LatLng(37.402986	,126.792068	),
                        new LatLng(37.403517	,126.789993	),
                        new LatLng(37.40207 	,126.788368	),
                        new LatLng(37.402641	,126.786728	),
                        new LatLng(37.401644	,126.788741	),
                        new LatLng(37.39595 	,126.789548	),
                        new LatLng(37.392164	,126.784625	),
                        new LatLng(37.395234	,126.780138	),
                        new LatLng(37.393513	,126.77815	),
                        new LatLng(37.39594 	,126.772009	),
                        new LatLng(37.394963	,126.76648	),
                        new LatLng(37.399332	,126.76383	),
                        new LatLng(37.400782	,126.750293	),
                        new LatLng(37.403936	,126.749039	),
                        new LatLng(37.407842	,126.751081	),
                        new LatLng(37.407521	,126.753598	),
                        new LatLng(37.417573	,126.754465	),
                        new LatLng(37.422702	,126.753254	),
                        new LatLng(37.426967	,126.762419	),
                        new LatLng(37.42824 	,126.760769	),
                        new LatLng(37.426034	,126.764526	),
                        new LatLng(37.427471	,126.769161	),
                        new LatLng(37.430737	,126.770838	),
                        new LatLng(37.430944	,126.770905	),
                        new LatLng(37.443411	,126.767491	),
                        new LatLng(37.449317	,126.771626	),
                        new LatLng(37.451021	,126.779023	),
                        new LatLng(37.451642	,126.77924	),
                        new LatLng(37.462036	,126.778529	),
                        new LatLng(37.462008	,126.778473	),
                        new LatLng(37.462066	,126.778434	),
                        new LatLng(37.471795	,126.778197	),
                        new LatLng(37.472393	,126.784304	),
                        new LatLng(37.465645	,126.789207	),
                        new LatLng(37.464244	,126.796312	),
                        new LatLng(37.464027	,126.796468	),
                        new LatLng(37.462128	,126.808895	),
                        new LatLng(37.458817	,126.811831	),
                        new LatLng(37.460262	,126.813794	),
                        new LatLng(37.460353	,126.814212	),
                        new LatLng(37.458376	,126.82318	),
                        new LatLng(37.4588  	,126.823518	),
                        new LatLng(37.459479	,126.826101	),
                        new LatLng(37.457741	,126.829148	),
                        new LatLng(37.460295	,126.833081	),
                        new LatLng(37.458122	,126.838332	),
                        new LatLng(37.438063	,126.837521	),
                        new LatLng(37.434687	,126.841447	),
                        new LatLng(37.426754	,126.84173	),
                        new LatLng(37.426701	,126.841724	),
                        new LatLng(37.422885	,126.8454	),
                        new LatLng(37.413909	,126.846193	),
                        new LatLng(37.413783	,126.846214	),
                        new LatLng(37.403478	,126.855063	),
                        new LatLng(37.403444	,126.855107	),
                        new LatLng(37.403444	,126.855108	),
                        new LatLng(37.403443	,126.855108	),
                        new LatLng(37.401713	,126.86573	),
                        new LatLng(37.401719	,126.865804	),
                        new LatLng(37.401929	,126.876918	),
                        new LatLng(37.401971	,126.876943	),
                        new LatLng(37.400852	,126.877036	),
                        new LatLng(37.400711	,126.877075	),
                        new LatLng(37.400488	,126.877112	),
                        new LatLng(37.399791	,126.87767	),
                        new LatLng(37.397251	,126.877606	),
                        new LatLng(37.396729	,126.877868	),
                        new LatLng(37.395527	,126.875391	),
                        new LatLng(37.395381	,126.87542	),
                        new LatLng(37.387176	,126.877841	),
                        new LatLng(37.386629	,126.878225	),
                        new LatLng(37.386564	,126.878275	),
                        new LatLng(37.386146	,126.878676	),
                        new LatLng(37.385563	,126.878636	),
                        new LatLng(37.384685	,126.878996	),
                        new LatLng(37.384153	,126.879093	),
                        new LatLng(37.383875	,126.879256	),
                        new LatLng(37.383428	,126.879349	),
                        new LatLng(37.3827  	,126.880209	),
                        new LatLng(37.381637	,126.879862	),
                        new LatLng(37.38071 	,126.880561	),
                        new LatLng(37.3805  	,126.880617	),
                        new LatLng(37.379981	,126.880754	),
                        new LatLng(37.379427	,126.880826	),
                        new LatLng(37.378903	,126.881151	),
                        new LatLng(37.377583	,126.881266	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.381675, 126.769944));
    }//시흥시
    public void drawPolygon46(GoogleMap googlemap) { //
        String name = "성남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.471958	,127.175615	),
                        new LatLng(37.465668	,127.173935	),
                        new LatLng(37.454178	,127.160933	),
                        new LatLng(37.444612	,127.156433	),
                        new LatLng(37.43533 	,127.136399	),
                        new LatLng(37.432344	,127.118454	),
                        new LatLng(37.421987	,127.116979	),
                        new LatLng(37.420336	,127.119705	),
                        new LatLng(37.415661	,127.119206	),
                        new LatLng(37.411554	,127.117666	),
                        new LatLng(37.41349 	,127.120472	),
                        new LatLng(37.40865 	,127.115931	),
                        new LatLng(37.407057	,127.107139	),
                        new LatLng(37.409722	,127.102446	),
                        new LatLng(37.406903	,127.099789	),
                        new LatLng(37.406847	,127.094505	),
                        new LatLng(37.397115	,127.084157	),
                        new LatLng(37.398108	,127.081254	),
                        new LatLng(37.395605	,127.076372	),
                        new LatLng(37.40243 	,127.056148	),
                        new LatLng(37.403884	,127.043245	),
                        new LatLng(37.405235	,127.042749	),
                        new LatLng(37.406767	,127.042479	),
                        new LatLng(37.408299	,127.042762	),
                        new LatLng(37.412271	,127.04674	),
                        new LatLng(37.413748	,127.046843	),
                        new LatLng(37.414308	,127.0449	),
                        new LatLng(37.414804	,127.043974	),
                        new LatLng(37.415408	,127.04298	),
                        new LatLng(37.415539	,127.042077	),
                        new LatLng(37.428103	,127.042016	),
                        new LatLng(37.430702	,127.047369	),
                        new LatLng(37.428297	,127.052325	),
                        new LatLng(37.430274	,127.070706	),
                        new LatLng(37.430188	,127.070885	),
                        new LatLng(37.437408	,127.073844	),
                        new LatLng(37.442263	,127.072138	),
                        new LatLng(37.441351	,127.082097	),
                        new LatLng(37.444894	,127.087855	),
                        new LatLng(37.44975 	,127.088821	),
                        new LatLng(37.456394	,127.095223	),
                        new LatLng(37.456219	,127.099096	),
                        new LatLng(37.462407	,127.106167	),
                        new LatLng(37.458641	,127.116896	),
                        new LatLng(37.462201	,127.117475	),
                        new LatLng(37.466521	,127.124207	),
                        new LatLng(37.466644	,127.124488	),
                        new LatLng(37.469598	,127.124878	),
                        new LatLng(37.468626	,127.126666	),
                        new LatLng(37.468414	,127.127034	),
                        new LatLng(37.469286	,127.135426	),
                        new LatLng(37.472247	,127.130276	),
                        new LatLng(37.475094	,127.13023	),
                        new LatLng(37.475475	,127.133946	),
                        new LatLng(37.475105	,127.13441	),
                        new LatLng(37.474119	,127.137067	),
                        new LatLng(37.473999	,127.137367	),
                        new LatLng(37.473439	,127.139364	),
                        new LatLng(37.472944	,127.1545	),
                        new LatLng(37.468773	,127.16778	),
                        new LatLng(37.471958	,127.175615	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.408563, 127.116230));
    }//성남시 수정구
    public void drawPolygon40(GoogleMap googlemap) { //
        String name = "성남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.451171	,127.194842	),
                        new LatLng(37.437378	,127.193759	),
                        new LatLng(37.43678 	,127.193923	),
                        new LatLng(37.424675	,127.194336	),
                        new LatLng(37.419852	,127.187783	),
                        new LatLng(37.419738	,127.18772	),
                        new LatLng(37.415516	,127.184719	),
                        new LatLng(37.415486	,127.181087	),
                        new LatLng(37.415771	,127.180501	),
                        new LatLng(37.415499	,127.178614	),
                        new LatLng(37.415312	,127.178374	),
                        new LatLng(37.415069	,127.17712	),
                        new LatLng(37.414807	,127.177228	),
                        new LatLng(37.4055  	,127.173875	),
                        new LatLng(37.406477	,127.165071	),
                        new LatLng(37.411099	,127.155638	),
                        new LatLng(37.410798	,127.149256	),
                        new LatLng(37.41509 	,127.143369	),
                        new LatLng(37.416998	,127.134435	),
                        new LatLng(37.415661	,127.119206	),
                        new LatLng(37.420336	,127.119705	),
                        new LatLng(37.421987	,127.116979	),
                        new LatLng(37.432344	,127.118454	),
                        new LatLng(37.43533 	,127.136399	),
                        new LatLng(37.444612	,127.156433	),
                        new LatLng(37.454178	,127.160933	),
                        new LatLng(37.465668	,127.173935	),
                        new LatLng(37.471958	,127.175615	),
                        new LatLng(37.474808	,127.178754	),
                        new LatLng(37.467112	,127.181996	),
                        new LatLng(37.46649 	,127.182005	),
                        new LatLng(37.45931 	,127.184651	),
                        new LatLng(37.459006	,127.184729	),
                        new LatLng(37.457033	,127.191664	),
                        new LatLng(37.451171	,127.194842	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.408563, 127.116230));
    }//성남시 중원구
    public void drawPolygon50(GoogleMap googlemap) { //
        String name = "의정부시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.752015	,127.147246	),
                        new LatLng(37.7514  	,127.147055	),
                        new LatLng(37.748842	,127.144398	),
                        new LatLng(37.747972	,127.142807	),
                        new LatLng(37.747915	,127.142616	),
                        new LatLng(37.747416	,127.138368	),
                        new LatLng(37.748154	,127.137442	),
                        new LatLng(37.74952 	,127.136342	),
                        new LatLng(37.748919	,127.1329	),
                        new LatLng(37.74892 	,127.132032	),
                        new LatLng(37.748515	,127.130721	),
                        new LatLng(37.748081	,127.128638	),
                        new LatLng(37.747349	,127.128392	),
                        new LatLng(37.747217	,127.128337	),
                        new LatLng(37.745254	,127.128366	),
                        new LatLng(37.744961	,127.127979	),
                        new LatLng(37.744814	,127.127768	),
                        new LatLng(37.744735	,127.127631	),
                        new LatLng(37.74419 	,127.12652	),
                        new LatLng(37.744167	,127.126497	),
                        new LatLng(37.743842	,127.126269	),
                        new LatLng(37.743552	,127.126017	),
                        new LatLng(37.742579	,127.125035	),
                        new LatLng(37.742318	,127.125077	),
                        new LatLng(37.742237	,127.125083	),
                        new LatLng(37.741772	,127.125008	),
                        new LatLng(37.741771	,127.125008	),
                        new LatLng(37.74111 	,127.12468	),
                        new LatLng(37.741074	,127.124664	),
                        new LatLng(37.740801	,127.124508	),
                        new LatLng(37.740652	,127.124496	),
                        new LatLng(37.740287	,127.124467	),
                        new LatLng(37.738807	,127.123719	),
                        new LatLng(37.738608	,127.123746	),
                        new LatLng(37.737523	,127.123889	),
                        new LatLng(37.737486	,127.123952	),
                        new LatLng(37.737105	,127.124031	),
                        new LatLng(37.736134	,127.124126	),
                        new LatLng(37.735663	,127.123687	),
                        new LatLng(37.735373	,127.123831	),
                        new LatLng(37.73501 	,127.123873	),
                        new LatLng(37.734444	,127.125383	),
                        new LatLng(37.733992	,127.125679	),
                        new LatLng(37.733399	,127.125491	),
                        new LatLng(37.733358	,127.125479	),
                        new LatLng(37.732889	,127.125402	),
                        new LatLng(37.732496	,127.125556	),
                        new LatLng(37.731182	,127.125623	),
                        new LatLng(37.731022	,127.125643	),
                        new LatLng(37.729697	,127.125441	),
                        new LatLng(37.729256	,127.125698	),
                        new LatLng(37.725945	,127.123519	),
                        new LatLng(37.725616	,127.123509	),
                        new LatLng(37.723385	,127.122859	),
                        new LatLng(37.723245	,127.122933	),
                        new LatLng(37.723238	,127.122936	),
                        new LatLng(37.722755	,127.123759	),
                        new LatLng(37.721932	,127.123063	),
                        new LatLng(37.721987	,127.121834	),
                        new LatLng(37.722347	,127.120894	),
                        new LatLng(37.721261	,127.118839	),
                        new LatLng(37.72107 	,127.118486	),
                        new LatLng(37.7195  	,127.117157	),
                        new LatLng(37.719028	,127.117082	),
                        new LatLng(37.718727	,127.117002	),
                        new LatLng(37.718689	,127.116992	),
                        new LatLng(37.718375	,127.116788	),
                        new LatLng(37.718212	,127.116482	),
                        new LatLng(37.717888	,127.116298	),
                        new LatLng(37.716988	,127.115482	),
                        new LatLng(37.717305	,127.115059	),
                        new LatLng(37.717313	,127.115044	),
                        new LatLng(37.717765	,127.114007	),
                        new LatLng(37.717707	,127.113258	),
                        new LatLng(37.717695	,127.113195	),
                        new LatLng(37.717481	,127.111973	),
                        new LatLng(37.717262	,127.111749	),
                        new LatLng(37.716901	,127.111415	),
                        new LatLng(37.716674	,127.111321	),
                        new LatLng(37.716258	,127.111072	),
                        new LatLng(37.715925	,127.110624	),
                        new LatLng(37.714946	,127.110206	),
                        new LatLng(37.714916	,127.110195	),
                        new LatLng(37.714232	,127.110005	),
                        new LatLng(37.713794	,127.109705	),
                        new LatLng(37.713744	,127.109631	),
                        new LatLng(37.713453	,127.109129	),
                        new LatLng(37.712472	,127.109265	),
                        new LatLng(37.712166	,127.109284	),
                        new LatLng(37.711918	,127.108912	),
                        new LatLng(37.711854	,127.108867	),
                        new LatLng(37.71153 	,127.108701	),
                        new LatLng(37.710863	,127.109182	),
                        new LatLng(37.710577	,127.109224	),
                        new LatLng(37.710025	,127.109291	),
                        new LatLng(37.709637	,127.109286	),
                        new LatLng(37.709602	,127.109281	),
                        new LatLng(37.70904 	,127.109109	),
                        new LatLng(37.708775	,127.109014	),
                        new LatLng(37.708177	,127.108376	),
                        new LatLng(37.707954	,127.107942	),
                        new LatLng(37.707851	,127.10759	),
                        new LatLng(37.707578	,127.107189	),
                        new LatLng(37.7076  	,127.105889	),
                        new LatLng(37.707539	,127.105753	),
                        new LatLng(37.70704 	,127.104432	),
                        new LatLng(37.707214	,127.103915	),
                        new LatLng(37.707209	,127.103912	),
                        new LatLng(37.70721 	,127.10391	),
                        new LatLng(37.706703	,127.103621	),
                        new LatLng(37.70654 	,127.103157	),
                        new LatLng(37.706009	,127.102886	),
                        new LatLng(37.705672	,127.101956	),
                        new LatLng(37.705413	,127.10159	),
                        new LatLng(37.704843	,127.101082	),
                        new LatLng(37.704833	,127.101043	),
                        new LatLng(37.704795	,127.099772	),
                        new LatLng(37.703601	,127.098153	),
                        new LatLng(37.702176	,127.096516	),
                        new LatLng(37.702373	,127.095721	),
                        new LatLng(37.702734	,127.095039	),
                        new LatLng(37.703736	,127.093982	),
                        new LatLng(37.703048	,127.092649	),
                        new LatLng(37.70258	    ,127.091157	),
                        new LatLng(37.702366	,127.088774	),
                        new LatLng(37.701919	,127.084086	),
                        new LatLng(37.701885	,127.083978	),
                        new LatLng(37.701755	,127.083781	),
                        new LatLng(37.699513	,127.080233	),
                        new LatLng(37.697454	,127.080602	),
                        new LatLng(37.696137	,127.081105	),
                        new LatLng(37.6938  	,127.072658	),
                        new LatLng(37.694922	,127.063386	),
                        new LatLng(37.685814	,127.051804	),
                        new LatLng(37.685922	,127.051368	),
                        new LatLng(37.688252	,127.049805	),
                        new LatLng(37.689207	,127.049751	),
                        new LatLng(37.692811	,127.04892	),
                        new LatLng(37.693664	,127.048721	),
                        new LatLng(37.694063	,127.048632	),
                        new LatLng(37.693952	,127.048237	),
                        new LatLng(37.69386 	,127.048186	),
                        new LatLng(37.693716	,127.048141	),
                        new LatLng(37.693314	,127.047527	),
                        new LatLng(37.69325 	,127.047449	),
                        new LatLng(37.693008	,127.047274	),
                        new LatLng(37.692812	,127.046864	),
                        new LatLng(37.692717	,127.046755	),
                        new LatLng(37.692564	,127.046205	),
                        new LatLng(37.692364	,127.045296	),
                        new LatLng(37.692402	,127.044934	),
                        new LatLng(37.692717	,127.044853	),
                        new LatLng(37.692936	,127.044817	),
                        new LatLng(37.693057	,127.044584	),
                        new LatLng(37.693098	,127.044327	),
                        new LatLng(37.693511	,127.043709	),
                        new LatLng(37.693793	,127.043158	),
                        new LatLng(37.694402	,127.043398	),
                        new LatLng(37.694912	,127.04322	),
                        new LatLng(37.695054	,127.04233	),
                        new LatLng(37.695382	,127.041813	),
                        new LatLng(37.6953  	,127.041109	),
                        new LatLng(37.694684	,127.039997	),
                        new LatLng(37.694046	,127.039572	),
                        new LatLng(37.693221	,127.037528	),
                        new LatLng(37.69264 	,127.036763	),
                        new LatLng(37.692374	,127.035777	),
                        new LatLng(37.692492	,127.035625	),
                        new LatLng(37.692021	,127.033597	),
                        new LatLng(37.691839	,127.032414	),
                        new LatLng(37.692815	,127.03217	),
                        new LatLng(37.692828	,127.031541	),
                        new LatLng(37.693075	,127.031026	),
                        new LatLng(37.693394	,127.030993	),
                        new LatLng(37.69369 	,127.030827	),
                        new LatLng(37.694776	,127.030454	),
                        new LatLng(37.699292	,127.029286	),
                        new LatLng(37.700281	,127.028216	),
                        new LatLng(37.701115	,127.027022	),
                        new LatLng(37.699581	,127.025315	),
                        new LatLng(37.701455	,127.015415	),
                        new LatLng(37.705145	,127.018205	),
                        new LatLng(37.713687	,127.012117	),
                        new LatLng(37.726084	,127.012232	),
                        new LatLng(37.733743	,127.005665	),
                        new LatLng(37.744203	,127.002227	),
                        new LatLng(37.744753	,127.002568	),
                        new LatLng(37.747699	,127.006641	),
                        new LatLng(37.755159	,127.00242	),
                        new LatLng(37.762619	,127.002046	),
                        new LatLng(37.762393	,127.008503	),
                        new LatLng(37.768861	,127.017436	),
                        new LatLng(37.766454	,127.027241	),
                        new LatLng(37.768434	,127.033825	),
                        new LatLng(37.764673	,127.046137	),
                        new LatLng(37.764764	,127.046398	),
                        new LatLng(37.76095 	,127.051344	),
                        new LatLng(37.763018	,127.062812	),
                        new LatLng(37.763089	,127.06308	),
                        new LatLng(37.763682	,127.065293	),
                        new LatLng(37.763916	,127.065759	),
                        new LatLng(37.773536	,127.084427	),
                        new LatLng(37.780291	,127.105743	),
                        new LatLng(37.777024	,127.115124	),
                        new LatLng(37.772681	,127.116128	),
                        new LatLng(37.772959	,127.124055	),
                        new LatLng(37.773123	,127.124255	),
                        new LatLng(37.768956	,127.12889	),
                        new LatLng(37.763643	,127.125806	),
                        new LatLng(37.762094	,127.133713	),
                        new LatLng(37.764011	,127.139656	),
                        new LatLng(37.763447	,127.14015	),
                        new LatLng(37.755498	,127.146178	),
                        new LatLng(37.754479	,127.146435	),
                        new LatLng(37.753744	,127.146615	),
                        new LatLng(37.752015	,127.147246	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.736893, 127.068307));
    }//의정부시


    public void drawPolygon402(GoogleMap googlemap) { //
        String name = "성남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.451171	,127.194842	),
                        new LatLng(37.437378	,127.193759	),
                        new LatLng(37.43678 	,127.193923	),
                        new LatLng(37.424675	,127.194336	),
                        new LatLng(37.419852	,127.187783	),
                        new LatLng(37.419738	,127.18772	),
                        new LatLng(37.415516	,127.184719	),
                        new LatLng(37.415486	,127.181087	),
                        new LatLng(37.415771	,127.180501	),
                        new LatLng(37.415499	,127.178614	),
                        new LatLng(37.415312	,127.178374	),
                        new LatLng(37.415069	,127.17712	),
                        new LatLng(37.414807	,127.177228	),
                        new LatLng(37.4055  	,127.173875	),
                        new LatLng(37.406477	,127.165071	),
                        new LatLng(37.411099	,127.155638	),
                        new LatLng(37.410798	,127.149256	),
                        new LatLng(37.41509 	,127.143369	),
                        new LatLng(37.416998	,127.134435	),
                        new LatLng(37.415661	,127.119206	),
                        new LatLng(37.420336	,127.119705	),
                        new LatLng(37.421987	,127.116979	),
                        new LatLng(37.432344	,127.118454	),
                        new LatLng(37.43533 	,127.136399	),
                        new LatLng(37.444612	,127.156433	),
                        new LatLng(37.454178	,127.160933	),
                        new LatLng(37.465668	,127.173935	),
                        new LatLng(37.471958	,127.175615	),
                        new LatLng(37.474808	,127.178754	),
                        new LatLng(37.467112	,127.181996	),
                        new LatLng(37.46649 	,127.182005	),
                        new LatLng(37.45931 	,127.184651	),
                        new LatLng(37.459006	,127.184729	),
                        new LatLng(37.457033	,127.191664	),
                        new LatLng(37.451171	,127.194842	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.408563, 127.116230));
    }//
    public void drawPolygon403(GoogleMap googlemap) { //
        String name = "성남시";
        int clr = Color.argb(100,255,0,0);
        int mclr;
        Log.d("log","kbc ++++++++"+hmap.get(name));//값 가져옴
        if(hmap.get(name)==null){
            Log.d("log","kbc ------------------------------------hmap.get(name)==null");
            mclr = 1;
        } else if(hmap.get(name).equals("-")){
            clr = Color.argb(100,140,140,140);
            mclr = 2;
        }else if(Integer.parseInt(hmap.get(name))>151){
            clr = Color.argb(100,255,0,0);
            mclr = 3;
        }else if(Integer.parseInt(hmap.get(name))>81){
            clr = Color.argb(100,255,255,0);
            mclr = 7;
        }else if(Integer.parseInt(hmap.get(name))>31){
            clr = Color.argb(100,0,255,0);
            mclr = 5;
        }else {
            clr = Color.argb(100,0,0,255);
            mclr = 4;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(37.451171	,127.194842	),
                        new LatLng(37.437378	,127.193759	),
                        new LatLng(37.43678 	,127.193923	),
                        new LatLng(37.424675	,127.194336	),
                        new LatLng(37.419852	,127.187783	),
                        new LatLng(37.419738	,127.18772	),
                        new LatLng(37.415516	,127.184719	),
                        new LatLng(37.415486	,127.181087	),
                        new LatLng(37.415771	,127.180501	),
                        new LatLng(37.415499	,127.178614	),
                        new LatLng(37.415312	,127.178374	),
                        new LatLng(37.415069	,127.17712	),
                        new LatLng(37.414807	,127.177228	),
                        new LatLng(37.4055  	,127.173875	),
                        new LatLng(37.406477	,127.165071	),
                        new LatLng(37.411099	,127.155638	),
                        new LatLng(37.410798	,127.149256	),
                        new LatLng(37.41509 	,127.143369	),
                        new LatLng(37.416998	,127.134435	),
                        new LatLng(37.415661	,127.119206	),
                        new LatLng(37.420336	,127.119705	),
                        new LatLng(37.421987	,127.116979	),
                        new LatLng(37.432344	,127.118454	),
                        new LatLng(37.43533 	,127.136399	),
                        new LatLng(37.444612	,127.156433	),
                        new LatLng(37.454178	,127.160933	),
                        new LatLng(37.465668	,127.173935	),
                        new LatLng(37.471958	,127.175615	),
                        new LatLng(37.474808	,127.178754	),
                        new LatLng(37.467112	,127.181996	),
                        new LatLng(37.46649 	,127.182005	),
                        new LatLng(37.45931 	,127.184651	),
                        new LatLng(37.459006	,127.184729	),
                        new LatLng(37.457033	,127.191664	),
                        new LatLng(37.451171	,127.194842	)
                )
                .strokeColor(Color.WHITE)             .strokeWidth(2)
                .fillColor(clr));
        polygon.setClickable(true);
        namehmap.put(polygon.hashCode(),name);
        colorhmap.put(polygon.hashCode(),clr);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(clr);
        if(mclr == 2){
            iconFactory.setStyle(IconGenerator.STYLE_WHITE);
        }else if(mclr == 3){
            iconFactory.setStyle(IconGenerator.STYLE_RED);
        }else if(mclr == 7){
            iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
        }else if(mclr == 5){
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        }else {
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
        }
        addIcon(iconFactory, name+"\n   "+hmap.get(name), new LatLng(37.408563, 127.116230));
    }//


    //privat inner class extending AsyncTask
    private class GetXMLTask extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        public void onPostExecute(Document doc) {
            Log.d("tag","kbc +++++++ in onPostExecute");//12
            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("item");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환
            for(int i = 0; i < nodeList.getLength(); i++){ // 원래 i < nodeList.getLength()
                s +=i+". ";
                //날씨 데이터를 추출
                Node node = nodeList.item(i); //item 엘리먼트 노드
                Element fstElmnt = (Element) node; // type casting
                Node ch = node.getFirstChild();

                Log.d("log", ch.getNextSibling().getNodeName());
                NodeList nameList  = fstElmnt.getElementsByTagName("stationName"); //지역명
                NodeList pm10Value = fstElmnt.getElementsByTagName("pm10Value");   //미세먼지 지수
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();
                s += ch.getNextSibling().getNodeName()+" : "+ ((Node) nameList.item(0)).getNodeValue() +" \t";
                s += "미세먼지지수 :  "+  pm10Value.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                Log.d("log","kbc======="+((Node) nameList.item(0)).getNodeValue()+" "+pm10Value.item(0).getChildNodes().item(0).getNodeValue());
                hmap.put(((Node) nameList.item(0)).getNodeValue(), pm10Value.item(0).getChildNodes().item(0).getNodeValue());  //(시군구 이름, 미세먼지 지수)
//                (Node) nameList.item(0)).getNodeValue() 구이름
//                pm10Value.item(0).getChildNodes().item(0).getNodeValue() 미세먼지 값
            }
            Log.d("log","kbc "+s);
            draw(); // 행정구역 경계 그리기
//            super.onPostExecute(doc); // 왜 하는지 모르겠음
        }
    }//end inner class - GetXMLTask

    private class GetXMLTask2 extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        public void onPostExecute(Document doc) {
            Log.d("tag","kbc +++++++ in onPostExecute");//12
            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("item");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환
            for(int i = 0; i < nodeList.getLength(); i++){ // 원래 i < nodeList.getLength()
                s +=i+". ";
                //날씨 데이터를 추출
                Node node = nodeList.item(i); //item 엘리먼트 노드
                Element fstElmnt = (Element) node; // type casting
                Node ch = node.getFirstChild();
                Log.d("log", ch.getNextSibling().getNodeName());
                NodeList nameList  = fstElmnt.getElementsByTagName("cityName"); //지역명
                NodeList pm10Value = fstElmnt.getElementsByTagName("pm10Value");   //미세먼지 지수
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();
                s += ch.getNextSibling().getNodeName()+" : "+ ((Node) nameList.item(0)).getNodeValue() +" \t";
                s += "미세먼지지수 :  "+  pm10Value.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                Log.d("log","kbc======="+((Node) nameList.item(0)).getNodeValue()+" "+pm10Value.item(0).getChildNodes().item(0).getNodeValue());
                hmap.put(((Node) nameList.item(0)).getNodeValue(), pm10Value.item(0).getChildNodes().item(0).getNodeValue());  //(시군구 이름, 미세먼지 지수)
            }
            Log.d("log","kbc "+s);
            draw2();
        }
    }//end inner class - GetXMLTask

    //아이콘 마커
    private void addIcon(IconGenerator iconFactory, String text, LatLng position) {
        Marker marker;
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        markerOptions.visible(false);
        marker = mMap.addMarker(markerOptions);
        mList.add(marker);
    }// 어레이리스트에 마커 추가
}