package com.example.shakeddesk.light;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendToggle extends Thread {

    private Context context;
    private String ip;
    private int port;
    private Button enableButton;


    public SendToggle(Context context, String ip, int port, Button enableButton) {
        this.context = context;
        this.ip = ip;
        this.port = port;
        this.enableButton = enableButton;
    }

    private String sendMessageToServer() {
        try {
            runOnUI(new Runnable() {
                @Override
                public void run() {
                    enableButton.setEnabled(false);
                }
            });

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.ip, this.port), 6 * 1000);
            socket.close();

            return "Success";
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            runOnUI(new Runnable() {
                @Override
                public void run() {
                    enableButton.setEnabled(true);
                }
            });
        }
    }

    @Override
    public void run() {
        final String answer = sendMessageToServer();
        Log.e("A", "sent: " + answer);

        Handler handler = new Handler(this.context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (answer.equals("Success")) {
                    Toast.makeText(context, "Send success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed: " + answer, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void runOnUI(Runnable r) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(r);
    }
}
