package com.example.incidentaid;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

public class StreetView extends FragmentActivity implements OnStreetViewPanoramaReadyCallback {

    private TextView heading;
    private StreetViewPanorama mStreetViewPanorama;
    private String llong,llat, addr;
    LatLng p1 = null; //new LatLng(37.350870, -121.933775);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_street);

        final StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetview);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        Intent intent = getIntent();
        llong = intent.getStringExtra("long");
        llat = intent.getStringExtra("lat");
        addr = intent.getStringExtra("adde");

        p1 = new LatLng(Double.parseDouble(llat), Double.parseDouble(llong));

        Toast.makeText(getApplicationContext(), llat +" "+llong, Toast.LENGTH_SHORT).show();
        heading = (TextView) findViewById(R.id.street_address);
        heading.setText("INCIDENT ADDRESS: " + addr.toUpperCase());



//        onStreetViewPanoramaReady(mStreetViewPanorama);

    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        mStreetViewPanorama = streetViewPanorama;

        streetViewPanorama.setPosition(new LatLng(p1.latitude, p1.longitude));
        streetViewPanorama.setStreetNamesEnabled(true);
        streetViewPanorama.setPanningGesturesEnabled(true);
        streetViewPanorama.setZoomGesturesEnabled(true);
        streetViewPanorama.setUserNavigationEnabled(true);
        streetViewPanorama.animateTo(
                new StreetViewPanoramaCamera.Builder().
                        orientation(new StreetViewPanoramaOrientation(20, 20))
                        .zoom(streetViewPanorama.getPanoramaCamera().zoom)
                        .build(), 2000);

        streetViewPanorama.setOnStreetViewPanoramaChangeListener(panoramaChangeListener);
    }

    private StreetViewPanorama.OnStreetViewPanoramaChangeListener panoramaChangeListener = new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
        @Override
        public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
        }
    };


    @Override
    public void onBackPressed() {
//        startActivity(new Intent(StreetView.this, Incident_Cmd_DashBoard.class));
//        super.onBackPressed();
        finish();
    }


}
