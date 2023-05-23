package com.youranemone.noticeboard.map;

import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.utils.MyConstants;

import java.util.List;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(getString(R.string.yandex_map_key));
        MapKitFactory.initialize(this);
        setContentView(R.layout.map_activity);


//        mapView.getMap().move(
//                new CameraPosition(new Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
//                new Animation(Animation.Type.SMOOTH, 0),
//                null);
    }

    private void init(){
        mapView = findViewById(R.id.mapview);
        Geocoder geocoder = new Geocoder(this);
        if(getIntent() != null) {
            Intent i = getIntent();
            String address = i.getStringExtra(MyConstants.ADDRESS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(address, 1, new Geocoder.GeocodeListener() {
                    @Override
                    public void onGeocode(@NonNull List<Address> list) {
                        if(!list.isEmpty() && list.size() > 0){
                            double latitude = list.get(0).getLatitude();
                            double longitude = list.get(0).getLongitude();
                            Log.d("MapActivity", "Координаты: " + latitude + ", " + longitude);

                        }else {
                            Log.d("MapActivity", "Адрес не найден!");
                        }
                    }
                });
            }
        }
    }
}
