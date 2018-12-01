package com.example.shakeddesk.light;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public static final String COMMAND_TOGGLE = "TOGGLE";
    public static final String COMMAND_GET_STATE = "STATE";
    public static final String RESPONSE_STATE_ON = "1";
    public static final String RESPONSE_STATE_OFF = "0";
    public static final String RESPONSE_TOGGLE = "TOGGLED";
    private static final String SP_IP = "ip";
    private static final String SP_PORT = "port";
    public Button goBtn, btnState;
    private EditText etIP, etPort;
    private ImageView imageState;
    private ProgressBar progressBar;
    private boolean changedIP = false;
    private boolean changedPort = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIP = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        goBtn = findViewById(R.id.btnGo);
        goBtn.setOnClickListener(this);
        imageState = findViewById(R.id.imageState);
        progressBar = findViewById(R.id.progressBar);
        btnState = findViewById(R.id.btnState);
        btnState.setOnClickListener(this);

        progressBar.setVisibility(View.GONE);


        loadData(); // loads initial data

        getLightState();


        findViewById(R.id.defaultButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etIP.setText("10.100.102.44");
                etPort.setText("1340");
            }
        });


        etIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("A", "edited IP");
                changedIP = true;
            }
        });

        etPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("A", "edited Port");
                changedPort = true;
            }
        });


    }

    private void getLightState() {

        setProgressBarON();


        final SendCommand2 sm2 = new SendCommand2(this,
                etIP.getText().toString(), Integer.valueOf(etPort.getText().toString()),
                SendCommand2.COMMAND_GET_STATE, new Runnable() {
            @Override
            public void run() {


            }
        }, new SendCommand2.ServerAnswer() {
            @Override
            public void callback(String answer) {


                if (answer.equals(SendCommand2.RESPONSE_STATE_ON)) {

                    imageState.setImageResource(R.drawable.lighton);
                    Toast.makeText(MainActivity.this, "Light is ON ! ", Toast.LENGTH_SHORT).show();

                } else if (answer.equals(SendCommand2.RESPONSE_STATE_OFF)) {

                    imageState.setImageResource(R.drawable.lightoff);
                    Toast.makeText(MainActivity.this, "Light is OFF ! ", Toast.LENGTH_SHORT).show();

                } else {

                    imageState.setImageResource(R.drawable.erorrimage);
                    Toast.makeText(MainActivity.this, "getLightStateError " + answer
                            , Toast.LENGTH_SHORT).show();
                    Log.e("GET-LIGHT-STATE-ERROR ", answer);
                }

                setProgressBarOFF();
            }

        });

        sm2.start();

    }

    private void doToggle() {


        final SendCommand2 sm2 = new SendCommand2(this,
                etIP.getText().toString(), Integer.valueOf(etPort.getText().toString()),
                SendCommand2.COMMAND_TOGGLE, new Runnable() {
            @Override
            public void run() {

              setProgressBarON();

            }
        }, new SendCommand2.ServerAnswer() {


            @Override
            public void callback(String answer) {


                if (answer.equals(SendCommand2.RESPONSE_TOGGLE)) {

                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();


                } else {

                    // change view error .
                    Toast.makeText(MainActivity.this, "doToggleERROR " + answer
                            , Toast.LENGTH_SHORT).show();
                    Log.e("DO-TOGGLE-ERROR ", answer);
                }
                getLightState();
                setProgressBarOFF();
            }
        });

        sm2.start();
    }

    private void setProgressBarON(){

        progressBar.setVisibility(View.VISIBLE);
        imageState.setVisibility(View.GONE);
        btnState.setEnabled(false);
        goBtn.setEnabled(false);
        Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();

    }

    private void setProgressBarOFF(){
        progressBar.setVisibility(View.GONE);
        imageState.setVisibility(View.VISIBLE);
        btnState.setEnabled(true);
        goBtn.setEnabled(true);
        Toast.makeText(this, "Finished Waiting", Toast.LENGTH_SHORT).show();

    }


    private boolean permission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // good to go
            return true;
        }

        // need to request permission

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 100);
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for permission, click again", Toast.LENGTH_SHORT).show();
//                go(COMMA);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnGo) {
            if (permission()) {
                saveChanges(); // each time you click "go" I save the current ip and port (smartly)
                doToggle();
            } else {
                Toast.makeText(this, "Don't have permission to do so", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.btnState) {
            getLightState();
        }
    }
/*
    private void go(String command) {
        try {
            Thread t = new SendCommand(this, etIP.getText().toString(), Integer.valueOf(etPort.getText().toString()), goBtn, command);
            t.start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }*/


    private void saveChanges() {
        if (this.changedIP) {
            saveIP(this.etIP.getText().toString());
        }

        if (this.changedPort) {
            savePort(this.etPort.getText().toString());
        }
    }

    private void loadData() {
        String ip = getIP();
        if (ip == null) {
            Log.w("A", "loaded default IP");
            ip = "10.100.102.44";
        }

        String port = getPort();
        if (port == null) {
            Log.w("A", "loaded default port");
            port = "1340";
        }

        this.etIP.setText(ip);
        this.etPort.setText(port);
    }


    private void saveIP(String newIP) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_IP, newIP);

        editor.apply();

    }

    private void savePort(String newPort) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_PORT, newPort);

        editor.apply();

    }

    public String getIP() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        return sp.getString(SP_IP, null);
    }

    public String getPort() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);

        return sp.getString(SP_PORT, null);
    }

}
