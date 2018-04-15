package com.example.ninad.hb5_relay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {
    private static final char PERMISSION_REQUEST_COARSE_LOCATION = 'a';
    private static String TAG = "HB5-Relay-MainActivity";
    private BeaconManager mBeaconManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        setContentView(R.layout.activity_main);
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(1000l);
        sendToServer("+123.1111111" , "-111.789456123" , "1234");
    }


    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
            mBeaconManager.bind(this);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            final Beacon curr = beacon;
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");
                logToDisplay(url,beacon.getDistance());
                sendToServer("+123.1111111" , "-111.789456123" , "1234");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    private void logToDisplay(final String url, final Double distance) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView text = findViewById(R.id.text1);
                String originalText = (String) text.getText();
                text.setText(originalText + "Found : " + url + "at :" + distance.toString()+  "\n");
            }
        });
    }

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
                    URL httpEndPoint = new URL("http://18.217.115.154:3000/locations");
                    HttpURLConnection myConnection = (HttpURLConnection) httpEndPoint.openConnection();
                    myConnection.setRequestMethod("POST");
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
                        Toast.makeText(getApplicationContext(),"Failed to send the data to server" , Toast.LENGTH_LONG).show();
                        Log.d(TAG,"Server returned something wrong.");

                    }
                } catch (Exception e) {
                    //Handle the exceptions that may arrise in Request construction.
                    Toast.makeText(getApplicationContext(),"Request Construction error" , Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
