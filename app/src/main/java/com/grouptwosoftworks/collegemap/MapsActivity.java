package com.grouptwosoftworks.collegemap;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.face.Landmark;
import org.apache.commons.csv.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private POI[] pois;

    private Marker start;
    private Marker end;
    private Polyline path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void readCSV(){
        try{
            AssetManager ass = getApplicationContext().getAssets();
            InputStream inputStream = ass.open("poi.csv");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            CSVParser csvParser = new CSVParser(bufferedReader,CSVFormat.DEFAULT.withHeader());

            List<CSVRecord> records = csvParser.getRecords();
            pois = new POI[records.size()];
            BitmapDescriptor pointer_icon = BitmapDescriptorFactory.fromResource(R.drawable.penn_college);
            int i =0;
            for( CSVRecord record: records){
                pois[i] = new POI(record.get("key"),Double.parseDouble(record.get("lat")),Double.parseDouble(record.get("lng")),record.get("name"), record.get("desc"));
                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(pois[i].lat,pois[i].lng));
                mo.title(pois[i].name);
                mo.icon(pointer_icon);
                mMap.addMarker(mo);
                i++;

            }

        }catch (IOException e){}

    }

    public void onMap(){

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        readCSV();
        for(POI poi : pois){
            LatLng ll = new LatLng(poi.lat,poi.lng);
            builder.include(ll);
        }

        final LatLngBounds bounds = builder.build();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                DrawLine(marker.getPosition());
                return false;
            }
        });

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                DrawLine(latLng);
            }
        });
    }

    private void DrawLine(LatLng latLng){
        if(start!=null && end != null){
            start.remove();
            start = null;
            end.remove();
            path.remove();
            path = null;
            end = null;

        }
        else if(start == null){
            start = mMap.addMarker(new MarkerOptions().position(latLng));
        }
        else {
            end = mMap.addMarker(new MarkerOptions().position(latLng));
            //draw line
            path = mMap.addPolyline(new PolylineOptions().add(start.getPosition()).add(end.getPosition()));
            Location location1 = new Location("");
            location1.setLatitude(start.getPosition().latitude);
            location1.setLongitude(start.getPosition().longitude);

            Location location2 = new Location("");
            location2.setLatitude(end.getPosition().latitude);
            location2.setLongitude(end.getPosition().longitude);

            float dist = location1.distanceTo(location2);

            Toast.makeText(getApplicationContext(),"Distance is " + dist + " meters",Toast.LENGTH_LONG).show();
        }
    }


    private class POI{
        protected String key;
        protected Double lat;
        protected Double lng;
        protected String name;
        protected String desc;
        protected POI(String key, Double lat, Double lng, String name, String desc){
            this.key = key;this.lat = lat;this.lng = lng;this.name = name;this.desc = desc;
        }
    }//poi class

}
