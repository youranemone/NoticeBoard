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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private String adsKey, adsCat;

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
        init();
    }


    private void init() {
        if (getIntent() != null) {
            Intent i = getIntent();
            String adr = i.getStringExtra(MyConstants.ADDRESS);
            tvAddress.setText(adr);
            String title = i.getStringExtra(MyConstants.TITLE);
            tvTitle.setText(title);
            lat = i.getDoubleExtra(MyConstants.ADS_LATITUDE,0);
            lon = i.getDoubleExtra(MyConstants.ADS_LONGITUDE,0);
            adsKey = i.getStringExtra(MyConstants.KEY);
            adsCat = i.getStringExtra(MyConstants.CAT);
            if(lat == 0 &&  lon == 0){
                Geocoder geocoder = new Geocoder(this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(adr, 1);
                    if (!addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(adsCat +"/" + adsKey + "/" + "anuncio");
                        dbRef.child("latitude").setValue(latitude);
                        dbRef.child("longitude").setValue(longitude);
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
            }else{
                PlacemarkMapObject placemarkMapObject = mapView.getMap().getMapObjects().addPlacemark(new Point(lat, lon));
                placemarkMapObject.setIcon(ImageProvider.fromResource(this,R.drawable.map_icon));
                placemarkMapObject.setOpacity(0.8f);
                placemarkMapObject.setDraggable(false);
                mapView.getMap().move(
                        new CameraPosition(new Point(lat, lon), 15.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 0),
                        null);
            }

        }
    }

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
