package com.example.ninad.hb5_;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uriio.beacons.Beacons;
import com.uriio.beacons.model.Beacon;
import com.uriio.beacons.model.EddystoneURL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Beacons.initialize(this);
        final Beacon abc = new EddystoneURL("https://github.com");
        setContentView(R.layout.activity_main);
        final Button sosButton = (Button) findViewById(R.id.sos_button);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = (String)sosButton.getText();
                if(text.equals("Enable")){
                    abc.start();
                    sosButton.setText("Disable");
                }
                else{
                    abc.stop();
                    sosButton.setText("Enable");
                }

            }
        });
    }
}
