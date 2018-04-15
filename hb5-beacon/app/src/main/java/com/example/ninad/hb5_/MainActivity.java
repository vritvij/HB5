package com.example.ninad.hb5_;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uriio.beacons.Beacons;
import com.uriio.beacons.model.Beacon;
import com.uriio.beacons.model.EddystoneURL;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private Button sosButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView textView;
    private Beacon abc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Beacons.initialize(this);
        abc = new EddystoneURL("https://github.com");
        setContentView(R.layout.activity_main);

        sosButton = (Button) findViewById(R.id.sos_button);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = (String) sosButton.getText();
                if (text.equals("Enable")) {
                    abc.start();
                    sosButton.setText("Disable");
                } else {
                    abc.stop();
                    sosButton.setText("Enable");
                }

            }
        });

        textView = (TextView) findViewById(R.id.location);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textView.append("\n " + location.getLatitude() + " " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);

                return;
            }
        }else {
            configureButton();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    private void configureButton() {
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = (String) sosButton.getText();
                if (text.equals("Enable")) {
                    abc.start();
                    locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                    sosButton.setText("Disable");
                } else {
                    abc.stop();
                    sosButton.setText("Enable");
                }
            }
        });
    }
}
