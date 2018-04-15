package com.example.ninad.hb5_relay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothLeScanner mBluetoothLeScanner;

    final String TAG = "HB5Relay-MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get the BluetoothScanner to scan for BLE advertisements
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(UUID.fromString( getString(R.string.ble_uuid ) ) ) )
                .build();
        filters.add( filter );

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null || result.getDevice() == null )
                return;
            try {
                StringBuilder builder = new StringBuilder();
                ScanRecord sr = result.getScanRecord();
                List<ParcelUuid> lt = sr.getServiceUuids();
                ParcelUuid pu = lt.get(0);
                byte[] mp = sr.getServiceData(pu);
                builder.append(new String(mp, Charset.forName("UTF-8")));
                Log.i("DataReceived!", builder.toString()+" "+ result.getDevice().getAddress());
                String[] LatLng = builder.toString().split(" ");
                sendToServer(LatLng[0],LatLng[1],result.getDevice().getAddress());
            }
            catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };


    /**
     * Sends data to the server
     * @param lat Latitude of the user
     * @param lng Longitude of the user
     * @param user_id User ID of the user
     */
    private void sendToServer(final String lat, final String lng, final String user_id){
        AsyncTask.execute(new Runnable() { // Async Runnable request
            @Override
            public void run() {
                try {
                    URL httpEndPoint = new URL("http://18.217.115.154:3000/locations/" + user_id);
                    Log.d(TAG, "Data : " + lat );
                    HttpURLConnection myConnection = (HttpURLConnection) httpEndPoint.openConnection();
                    myConnection.setRequestMethod("PUT");
                    myConnection.setRequestProperty("User-Agent", "relay-01");
                    myConnection.setRequestProperty("Accept","application/json");
                    //Construct the request data
                    String myData = "";
                    myData += "lat=" + lat + "&";
                    myData += "lng=" + lng + "&";
                    myData += "user_id=" + user_id;

                    // Enable writing
                    myConnection.setDoOutput(true);
                    myConnection.getOutputStream().write(myData.getBytes());
                    if (myConnection.getResponseCode() == 200) {
                        // Request was a success.
                        // Read the response and store in a String.
                        InputStream responseBody = myConnection.getInputStream();
                        BufferedReader r = new BufferedReader(new InputStreamReader(responseBody));
                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        //Logging the same for debugging.
                        Log.d(TAG,total.toString());
                        //End the connection.
                        myConnection.disconnect();
                    } else {
                        //Toast.makeText(getApplicationContext(),"Failed to send the data to server" , Toast.LENGTH_LONG).show();
                        Log.d(TAG,"Server returned something wrong.");
                        Log.d(TAG,myConnection.getResponseMessage());
                    }
                } catch (Exception e) {
                    //Handle the exceptions that may arrise in Request construction.
                    //Toast.makeText(getApplicationContext(),"Request Construction error" , Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
