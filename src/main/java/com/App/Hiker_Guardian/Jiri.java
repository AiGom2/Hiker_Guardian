package com.App.Hiker_Guardian;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class Jiri extends AppCompatActivity implements OnMapReadyCallback {

    static MainHandler mainHandler;

    ClientThread clientThread;

    private GoogleMap mMap;

    private LocationManager locationManager;
    private Marker currentLocationMarker; // 현재 위치 표시

    double latitude;
    double longitude;
    double [] cmp_latitude = {35.33614821062403, 35.33644643515516};

    List<Marker> markerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiri);

        String Ip = "10.10.14.64";
        int Port = 5000;

        clientThread = new ClientThread(Ip, Port);
        clientThread.start();

        // id 참조 및 구글 맵 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mainHandler = new MainHandler();
    }
    public void buttonAddMaker(View v)
    {

        if(latitude == cmp_latitude[0])
        {
            final LatLng Jiri_5_10 = new LatLng(latitude, longitude);
            addMarker(Jiri_5_10);

        }
        else if(latitude == cmp_latitude[1])
        {
            final LatLng Jiri_1_52 = new LatLng(latitude, longitude);
            addMarker1(Jiri_1_52);
        }

    }

    public void Reset(View v)
    {
        try {
            currentLocationMarker.remove();
        } catch (NullPointerException e) {
            // NullPointerException 처리
        }
        for (Marker m : markerList) {
            m.remove();
        }
        markerList.clear();
    }

    void  addMarker(LatLng point) {
        final LatLng melbourneLocation = new LatLng(point.latitude, point.longitude);

        Marker marker_cal_1 = mMap.addMarker(
                new MarkerOptions()
                        .title("칼바위 코스 1번 카메라")
                        .position(melbourneLocation)
                        .draggable(true));
        markerList.add(marker_cal_1);

    }

    void  addMarker1(LatLng point) {
        final LatLng melbourneLocation = new LatLng(point.latitude, point.longitude);

        Marker marker_bak_1 = mMap.addMarker(
                new MarkerOptions()
                        .title("백무동 코스 2번 카메라")
                        .position(melbourneLocation)
                        .draggable(true));

        markerList.add(marker_bak_1);
    }


    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {

        mMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng Jiri = new LatLng(35.33691250741886, 127.73017952033848);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Jiri, 18));

    }

    public void onBackPressed() {
        // 뒤로 가기를 눌렀을 때 소켓을 닫는 코드
        if (clientThread != null) {
            clientThread.interrupt(); // 스레드를 중지
            clientThread.stopClient(); // 소켓 닫기
        }

        super.onBackPressed(); // 기본 동작 수행
    }
    class MainHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String data = bundle.getString("msg");
            Log.d("MainHandler", data);
            if (data.indexOf("New connect") != -1) {
                return;

            }
// 실수 변환

            String[] parts = data.split("]");
            String[] values = parts[1].split(",");
            String values2= values[4].replace("\n","");


            int length1 = values[3].length();
            int length2 = values2.length();

            String g_x = "";
            String g_y = "";

            char[] temp1 = values[3].toCharArray();
            char[] temp = values2.toCharArray();

            for (int i = 0; i < length1; i++) {
                if (Character.isDigit(temp1[i]) || temp1[i] == '.') {
                    g_x = g_x + temp1[i];
                } else {
                    break;
                }
            }

            for (int i = 0; i < length2; i++)
            {
                if (Character.isDigit(temp[i]) || temp[i] == '.')
                {
                    g_y = g_y + temp[i];
                }
                else {
                    break;
                }
            }

            try {
                latitude  = Double.parseDouble(g_x);
                longitude = Double.parseDouble(g_y);


            } catch (NumberFormatException e) {
                System.out.println("유효한 숫자 형식이 아닙니다.");
            }

            if(latitude == cmp_latitude[0])
            {
                final LatLng Jiri_5_10 = new LatLng(latitude, longitude);
                addMarker(Jiri_5_10);

            }
            else if(latitude == cmp_latitude[1])
            {
                final LatLng Jiri_1_52 = new LatLng(latitude, longitude);
                addMarker1(Jiri_1_52);
            }

            //여기서부터 토스트 메세지 조작
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast_layout_root));
            TextView text = layout.findViewById(R.id.text6);
            android.widget.Toast toast = new android.widget.Toast(getApplicationContext());


            if(latitude == cmp_latitude[0]) {

                String t_message = String.format("%s 발견!, 칼바위 코스 %s번 카메라", values[0], values[2]);
                text.setText(t_message);
                toast.setGravity(Gravity.CENTER, 0, -750);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
            else if(latitude == cmp_latitude[1])
            {
                String t_message = String.format("%s 발견!, 백무동 코스 %s번 카메라", values[0], values[2]);
                text.setText(t_message);
                toast.setGravity(Gravity.CENTER, 0, -750);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }




        }

        }


}
