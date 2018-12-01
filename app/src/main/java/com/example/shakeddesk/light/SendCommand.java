package com.example.shakeddesk.light;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Deprecated
public class SendCommand extends Thread {

    private Context context;
    private String ip;
    private int port;
    private Button enableButton;
    private String command;

    public SendCommand(Context context, String ip, int port, Button enableButton, String command) {
        this.context = context;
        this.ip = ip;
        this.port = port;
        this.enableButton = enableButton;
        this.command = command;
    }

    public static void outputStreamWrite(OutputStream os, String msg) throws IOException {
        byte[] data = msg.getBytes();
        os.write(data);
        os.flush();
    }

    public static String inputStreamRead(InputStream is) throws IOException{
        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[1024];
        int bytesRead = 0;

        while (bytesRead != -1) {
            bytesRead = is.read(buffer);

            for (int i = 0; i < bytesRead; i++) {
                sb.append((char) buffer[i]);
            }

        }
        return sb.toString();
    }

    public String sendMessageToServer() {
        try {
            runOnUI(new Runnable() {
                @Override
                public void run() {
                    enableButton.setEnabled(false);
                }
            });

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.ip, this.port), 6 * 1000);

            outputStreamWrite(socket.getOutputStream(), this.command);

            String answer = inputStreamRead(socket.getInputStream());

            socket.close();

            return answer;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
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
        Log.e("A", "got back from server : " + answer);

        runOnUI(new Runnable() {
            @Override
            public void run() {
                if (!answer.startsWith("ERROR:")) {
                    String msg = "";
                    if (answer.startsWith(MainActivity.RESPONSE_STATE_OFF)) {
                        msg = "Power is OFF";
                    } else if (answer.startsWith(MainActivity.RESPONSE_STATE_ON)) {
                        msg = "Power is ON";
                    } else if (answer.startsWith(MainActivity.RESPONSE_TOGGLE)) {
                        msg = "Toggled";
                    } else {
                        Log.e("A", "Unexpected answer from server: " + msg);
                        msg = answer;
                    }
                    Toast.makeText(context, "Success: Server answered: " + msg , Toast.LENGTH_SHORT).show();
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
