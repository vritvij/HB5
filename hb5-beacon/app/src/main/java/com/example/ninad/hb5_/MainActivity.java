package com.example.ninad.hb5_;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
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
    AdvertisingSetCallback advertisingSetCallback;
    AdvertiseData advData;
    ParcelUuid pUuid;
    String latitude;
    String longitude;
    String data;
    AdvertisingSet currentAdvertisingSet;
    String LOG_TAG = "App";
    Runnable DataUpdateThread;
    volatile boolean ShouldUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() )
            Toast.makeText(getApplicationContext(), "Multiple advertisement not supported", Toast.LENGTH_LONG).show();

        sosButton = findViewById(R.id.sosButton);
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        pUuid = new ParcelUuid( UUID.fromString( getString(R.string.ble_uuid)));
        advertisingSetCallback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i(LOG_TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                        + status);
                currentAdvertisingSet = advertisingSet;
            }

            @Override
            public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(LOG_TAG, "onAdvertisingDataSet() :status:" + status);
            }

            @Override
            public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(LOG_TAG, "onScanResponseDataSet(): status:" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i(LOG_TAG, "onAdvertisingSetStopped():");
            }
        };

        advData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .build();

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShouldUpdate = sosButton.getText().equals("SOS");
                if(ShouldUpdate) {
                    gps = new GPSTracker(MainActivity.this);
                    // check if GPS enabled
                    if (gps.canGetLocation()) {

                        latitude = String.format(Locale.ENGLISH, "%.6f", gps.getLatitude());
                        longitude = String.format(Locale.ENGLISH, "%.6f", gps.getLongitude());
                        data = latitude + " " + longitude;
                        advData = new AdvertiseData.Builder()
                                .setIncludeDeviceName(false)
                                .addServiceUuid(pUuid)
                                .addServiceData(pUuid, data.getBytes())
                                .build();

                        AdvertisingSetParameters params  = new AdvertisingSetParameters.Builder()
                                .setLegacyMode(true)
                                .setConnectable(false)
                                .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
                                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
                                .build();

                        advertiser.startAdvertisingSet(params,advData,null,null,null,advertisingSetCallback);
                        sosButton.setText(R.string.cancel_text);

                        DataUpdateThread = new Runnable() {
                            @Override
                            public void run() {
                                while(ShouldUpdate) {
                                    if (currentAdvertisingSet != null) {
                                        String newLatitude = String.format(Locale.ENGLISH, "%.6f", gps.getLatitude());
                                        String newLongitude = String.format(Locale.ENGLISH, "%.6f", gps.getLongitude());
                                        if (!latitude.equals(newLatitude) && !longitude.equals(newLongitude)) {
                                            latitude = newLatitude;
                                            longitude = newLongitude;
                                            data = latitude + " " + longitude;

                                            advData = new AdvertiseData.Builder()
                                                    .setIncludeDeviceName(false)
                                                    .addServiceUuid(pUuid)
                                                    .addServiceData(pUuid, data.getBytes())
                                                    .build();

                                            currentAdvertisingSet.setAdvertisingData(advData);
                                        }
                                    }
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };

                        AsyncTask.execute(DataUpdateThread);
                    } else {
                        gps.showSettingsAlert();
                    }
                }
                else{
                    gps.stopUsingGPS();
                    advertiser.stopAdvertisingSet(advertisingSetCallback);
                    currentAdvertisingSet = null;
                    Toast.makeText(getApplicationContext(), "GPS is Stopped", Toast.LENGTH_LONG).show();
                    sosButton.setText(R.string.sos_text);
                }
            }
        });
    }
}