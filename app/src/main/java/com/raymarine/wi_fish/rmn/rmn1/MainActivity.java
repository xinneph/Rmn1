package com.raymarine.wi_fish.rmn.rmn1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Button connect, print;
    private ISounder sounder;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected(" + name.toShortString() + ")");
            final SounderService.SndBinder sndBinder = (SounderService.SndBinder) service;
            sounder = sndBinder.getISounder();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDisconnected(" + name.toShortString() + ")");
            sounder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.connect);
        print = (Button) findViewById(R.id.print);
        boolean bound = bindService(new Intent(this, SounderService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "bound=" + bound);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sounder.connect();
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sounder.print();
            }
        });
    }
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
