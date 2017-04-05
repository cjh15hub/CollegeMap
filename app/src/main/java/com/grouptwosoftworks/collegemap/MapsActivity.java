package com.grouptwosoftworks.collegemap;

import android.content.res.AssetManager;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private Button reset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start!=null){start.remove();start=null;}
                if(end!=null){end.remove(); end=null; path.remove(); path = null;}
                LatLngBounds bounds = prepareBounds();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
            }
        });
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
                pois[i] = new POI(record.get("key").toUpperCase(),Double.parseDouble(record.get("lat")),Double.parseDouble(record.get("lng")),record.get("name"), record.get("desc"));
                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(pois[i].lat,pois[i].lng));
                mo.title(pois[i].key);
                mo.snippet(pois[i].name + "\ncords: " + mo.getPosition().latitude + ", " + mo.getPosition().longitude);
                mo.icon(pointer_icon);
                mMap.addMarker(mo);
                i++;

            }

        }catch (IOException e){}

    }


    private LatLngBounds prepareBounds(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(POI poi : pois){
            LatLng ll = new LatLng(poi.lat,poi.lng);
            builder.include(ll);
        }
        return builder.build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {return null;}

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView keyname = (TextView) view.findViewById(R.id.keyname);
                TextView snippet = (TextView) view.findViewById(R.id.snippet);

                keyname.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());
                return  view;
            }
        });

        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
        readCSV();
        final LatLngBounds bounds =  prepareBounds();
//        for(POI poi : pois){
//            LatLng ll = new LatLng(poi.lat,poi.lng);
//            builder.include(ll);
//        }

        //final LatLngBounds bounds = builder.build();
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
            String dist_out = String.format("%.2f", dist);

            Toast.makeText(getApplicationContext(),"Distance is " +  dist_out + " meters",Toast.LENGTH_LONG).show();
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
