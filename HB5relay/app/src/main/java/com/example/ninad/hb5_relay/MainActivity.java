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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothLeScanner mBluetoothLeScanner;
    Handler mHandler = new Handler();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null
                    || result.getDevice() == null )
                return;
            try {
                StringBuilder builder = new StringBuilder();
                ScanRecord sr = result.getScanRecord();
                List<ParcelUuid> lt = sr.getServiceUuids();
                ParcelUuid pu = lt.get(0);
                byte[] mp = sr.getServiceData(pu);
                builder.append("\n").append(new String(mp, Charset.forName("UTF-8")));
                Log.i("DataReceived!", builder.toString()+" "+ result.getDevice().getAddress());
            }
            catch(Exception e){
                Log.e("", e.getMessage());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
