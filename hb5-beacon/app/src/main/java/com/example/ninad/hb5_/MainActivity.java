package com.example.ninad.hb5_;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.location.Location;
import com.uriio.beacons.Beacons;
import com.uriio.beacons.model.Beacon;
import com.uriio.beacons.model.EddystoneEID;
import com.uriio.beacons.model.EddystoneTLM;
import com.uriio.beacons.model.EddystoneURL;
import com.uriio.beacons.model.iBeacon;

import android.location.Location;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button sosButton;
    GPSTracker gps;
    Beacon first;
    Beacon second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Beacons.initialize(this);

        setContentView(R.layout.activity_main);
        sosButton = (Button) findViewById(R.id.sosButton);
        //abc = new EddystoneTLM(6000,"Hello_Niranjan");
        first = new EddystoneURL("Lat");
        second = new EddystoneURL("Long");
        first.start();
        second.start();


        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sosButton.getText().equals("SOS")) {
                    gps = new GPSTracker(MainActivity.this);
                    // check if GPS enabled
                    if (gps.canGetLocation()) {
                        double latitude=0.0;
                        double longitude=0.0;
                        String url_lat;
                        String url_lng;
                        sosButton.setText("Cancel");
                        while(true) {

                            if(latitude!=gps.getLatitude() && longitude!=gps.getLongitude()){
                                latitude = gps.getLatitude();
                                longitude = gps.getLongitude();
                                first.stop();
                                second.stop();
                                url_lat = "https://" + latitude;
                                url_lng = "http://" + longitude;
                                first = new EddystoneURL(url_lat);
                                second = new EddystoneURL(url_lng);

                                first.start();
                                second.start();
                            }
                            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        gps.showSettingsAlert();
                    }
                }
                else{
                    gps.stopUsingGPS();
                    first.stop();
                    second.stop();
                    Toast.makeText(getApplicationContext(), "GPS is Stopped", Toast.LENGTH_LONG).show();
                    sosButton.setText("SOS");
                }
            }
        });
    }
}
