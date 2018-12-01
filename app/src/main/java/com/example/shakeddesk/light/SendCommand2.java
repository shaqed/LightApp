package com.example.shakeddesk.light;

import android.content.Context;
import android.os.Handler;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * How to use this class:
 * Create a new instance from it, provide everything to the constructor
 *
 * Call the "start()" method of it when you want to initiate a server call
 */
public class SendCommand2 extends Thread {


    public interface ServerAnswer {
        /**
         * A callback interface to be used from this class
         * write your code and make use of the "answer" variable
         * that this class is going to provide for you
         * @param answer One of the following:
         *               RESPONSE_STATE_ON
         *               RESPONSE_STATE_OFF
         *               RESPONSE_TOGGLE
         *               or "ERROR: " and the error
         */
        void callback(String answer);
    }

    public static final String COMMAND_TOGGLE = "TOGGLE";
    public static final String COMMAND_GET_STATE = "STATE";

    public static final String RESPONSE_STATE_ON = "1";
    public static final String RESPONSE_STATE_OFF = "0";
    public static final String RESPONSE_TOGGLE = "TOGGLED";

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

    private Context context;
    private String ip;
    private int port;
    private String command;

    private Runnable runBefore;
    private ServerAnswer runAfter;

    /**
     * The only constructor of this class
     * @param context The activity
     * @param ip IP of the server
     * @param port port as an integer
     * @param command One of the constants ONLY !
     * @param runBefore What to do before running?
     * @param runAfter What to do after completed
     */
    public SendCommand2(Context context, String ip, int port, String command, Runnable runBefore, ServerAnswer runAfter) {
        this.context = context;
        this.ip = ip;
        this.port = port;
        this.command = command;
        this.runBefore = runBefore;
        this.runAfter = runAfter;
    }

    private void runOnUI(final ServerAnswer serverAnswer, final String response) {
        runOnUI(new Runnable() {
            @Override
            public void run() {
                serverAnswer.callback(response);
            }
        });
    }

    private void runOnUI(Runnable r) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(r);
    }

    public String sendMessageToServer() {
        try {

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.ip, this.port), 6 * 1000);

            outputStreamWrite(socket.getOutputStream(), this.command);

            String answer = inputStreamRead(socket.getInputStream());

            socket.close();

            return answer;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }


    @Override
    public void run() {
        runOnUI(this.runBefore);

        String answer = sendMessageToServer();

        runOnUI(this.runAfter, answer);
    }
}
