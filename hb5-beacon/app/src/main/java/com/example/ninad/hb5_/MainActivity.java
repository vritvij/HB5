package com.example.ninad.hb5_;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button sosButton;
    GPSTracker gps;
    BluetoothLeAdvertiser advertiser;
    AdvertiseCallback advertisingCallback;
    AdvertiseSettings settings;
    AdvertiseData advData;
    ParcelUuid pUuid;
    String latitude;
    String longitude;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() )
            Toast.makeText(getApplicationContext(), "Multiple advertisement not supported", Toast.LENGTH_LONG).show();

        sosButton = findViewById(R.id.sosButton);
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();
        pUuid = new ParcelUuid( UUID.fromString( getString(R.string.ble_uuid)));
        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sosButton.getText().equals("SOS")) {
                    gps = new GPSTracker(MainActivity.this);
                    // check if GPS enabled
                    if (gps.canGetLocation()) {
                        //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        latitude = String.format(Locale.ENGLISH,"%.6f", 33.910225);
                        longitude = String.format(Locale.ENGLISH,"%.6f", -118.388639);
                        data = latitude+" "+longitude;
                        advData = new AdvertiseData.Builder()
                                .setIncludeDeviceName(false)
                                .addServiceUuid(pUuid)
                                .addServiceData(pUuid, data.getBytes())
                                .build();

                        advertiser.startAdvertising(settings, advData, advertisingCallback);
                        sosButton.setText("Cancel");


                    } else {
                        gps.showSettingsAlert();
                    }
                }
                else{
                    gps.stopUsingGPS();
                    advertiser.stopAdvertising(advertisingCallback);
                    Toast.makeText(getApplicationContext(), "GPS is Stopped", Toast.LENGTH_LONG).show();
                    sosButton.setText("SOS");
                }
            }
        });
    }
}