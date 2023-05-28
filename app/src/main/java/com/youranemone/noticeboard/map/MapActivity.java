package com.youranemone.noticeboard.map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;
import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.utils.MyConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView tvTitle, tvAddress;
    private Double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(getString(R.string.yandex_map_key));
        MapKitFactory.initialize(this);
        setContentView(R.layout.map_activity);
        tvTitle = findViewById(R.id.tvMapTitle);
        tvAddress = findViewById(R.id.tvMapAddress);
        mapView = findViewById(R.id.mapview);
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

//        mapView.getMap().move(
//                new CameraPosition(new Point(54.1882394, 45.177637), 15.0f, 0.0f, 0.0f),
//                new Animation(Animation.Type.SMOOTH, 0),
//                null);
        init();
    }


    private void init() {
        if (getIntent() != null) {
            Intent i = getIntent();
            String adr = i.getStringExtra(MyConstants.ADDRESS);
            tvAddress.setText(adr);
            String title = i.getStringExtra(MyConstants.TITLE);
            tvTitle.setText(title);
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addresses = geocoder.getFromLocationName(adr, 1);
                if (!addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    PlacemarkMapObject placemarkMapObject = mapView.getMap().getMapObjects().addPlacemark(new Point(latitude, longitude));
                    placemarkMapObject.setIcon(ImageProvider.fromResource(this,R.drawable.map_icon));
                    placemarkMapObject.setOpacity(0.8f);
                    placemarkMapObject.setDraggable(false);
                    mapView.getMap().move(
                            new CameraPosition(new Point(latitude, longitude), 15.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 0),
                            null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            getCoords(adr).thenAccept(list ->{
//               lat = list.get(0);
//               lon = list.get(1);
//                mapView.getMap().move(
//                        new CameraPosition(new Point(lat, lon), 15.0f, 0.0f, 0.0f),
//                        new Animation(Animation.Type.SMOOTH, 0),
//                        null);
//            });


            //Log.d("MAP-ACTIVITY", String.valueOf(Build.VERSION.SDK_INT));

        }
    }

    private CompletableFuture<List<Double>> getCoords(String adr) {
        Geocoder geocoder = new Geocoder(this);
        CompletableFuture<List<Double>> future = new CompletableFuture<>();
        geocoder.getFromLocationName(adr, 1, new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(@NonNull List<Address> list) {
                if (!list.isEmpty()) {
                    List<Double> result = new ArrayList<>();
                    Double latitude = list.get(0).getLatitude();
                    Double longitude = list.get(0).getLongitude();
                    result.add(latitude);
                    result.add(longitude);
                    future.complete(result);

                    Log.d("MapActivity", "Координаты: " + latitude + ", " + longitude);

                } else {
                    Log.d("MapActivity", "Адрес не найден!");
                }
            }
        });
        return future;
    }

//        mapView = findViewById(R.id.mapview);
//        mapView.onStart();
//        Geocoder geocoder = new Geocoder(this);
//        if(getIntent() != null) {
//            Intent i = getIntent();
//            String address = i.getStringExtra(MyConstants.ADDRESS);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                geocoder.getFromLocationName(address, 1, new Geocoder.GeocodeListener() {
//                    @Override
//                    public void onGeocode(@NonNull List<Address> list) {
//                        if(!list.isEmpty()){
//                            double latitude = list.get(0).getLatitude();
//                            double longitude = list.get(0).getLongitude();
//                            Log.d("MapActivity", "Координаты: " + latitude + ", " + longitude);
//                            mapView.getMap().move(
//                                    new CameraPosition(new Point(55.751574, 37.573856), 100.0f, 0.0f, 0.0f),
//                                    new Animation(Animation.Type.SMOOTH, 0),
//                                    null);
//
//                        }else {
//                            Log.d("MapActivity", "Адрес не найден!");
//                        }
//                    }
//                });
//            }
//        }


    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
}
