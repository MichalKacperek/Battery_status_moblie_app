package com.mmm.myapplication2;

import static org.json.JSONObject.numberToString;
import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.support.v4.content.ContextCompat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

//b'\x00g{"byteorder": "little", "content-type": "text/json", "content-encoding": "utf-8", "content-length": 83}{"action": "read", "value": "SELECT * FROM ParamsTable ORDER BY unix DESC LIMIT 1"}'


 //       host, port = "31.0.204.106", 65000


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SERVERPORT = 65000;

    public static final String query = "{\"byteorder\": \"little\", \"content-type\": \"text/json\", \"content-encoding\": \"utf-8\", \"content-length\": 83}{\"action\": \"read\", \"value\": \"SELECT * FROM ParamsTable ORDER BY unix DESC LIMIT 1\"}";
    public final char[] string_char = { };
    public static final String header = "\\x00g";
    public static final String SERVER_IP = "31.0.204.106";
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    //private EditText edMessage;
    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.black);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        //edMessage = findViewById(R.id.edMessage);
    }
    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message /* + " [" + getTime() + "]"*/);
        tv.setTextSize(12);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_conn) {
            msgList.removeAllViews();
            showMessage("Connecting to Server...", clientTextColor);
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            showMessage("Connected to Server...", clientTextColor);
            return;
        }

        if (view.getId() == R.id.btn_get_data) {
            MessageSender();
        }
    }

     public void MessageSender() {
            JSONObject jsonObject1 = new JSONObject();
            JSONObject jsonObject2 = new JSONObject();
            try {
                //{"byteorder":"little","content-type":"text/json","content-encoding":"utf-8","content-length": 83}
                //{"action": "read", "value": "SELECT * FROM ParamsTable ORDER BY unix DESC LIMIT 1"}
                jsonObject1.put("byteorder", "little");
                jsonObject1.put("content-type", "text" + "\u002F" + "json");
                jsonObject1.put("content-encoding", "utf-8");
                jsonObject1.put("content-length", 80);
                jsonObject2.put("action", "read");
                jsonObject2.put("value", "SELECT * FROM ParamsTable ORDER BY unix DESC LIMIT 1");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String clientMessage = jsonObject1.toString() + jsonObject2.toString();//query.trim();
            showMessage("DATA REQUEST", Color.BLUE);
            if (null != clientThread) {
                clientThread.sendMessage(clientMessage);
            }
        }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {
                    int first_bracket_open;
                    int first_bracket_close;
                    int second_bracket_open;
                    int second_bracket_close;
                    String first_JSON_String;
                    String second_JSON_String;
                    String result;
                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String message = input.readLine();
                    showMessage( message, clientTextColor);

                    try {
                        if (null != message) {
                            first_bracket_open = message.indexOf("{");
                            first_bracket_close = message.indexOf("}");
                            first_JSON_String = message.substring(first_bracket_open,first_bracket_close+1);
                            second_bracket_open = message.indexOf("{",first_bracket_open+1);
                            second_bracket_close = message.indexOf("}",first_bracket_close+1);
                            second_JSON_String = message.substring(second_bracket_open, second_bracket_close+1);
                            message = first_JSON_String+" "+second_JSON_String;
                            JSONObject first_JSON_Object = new JSONObject(first_JSON_String);
                            JSONObject second_JSON_Object = new JSONObject(second_JSON_String);
                            result = second_JSON_Object.getString("result");
                            message = result;
                            String[] results = result.split(",");
                            String unix = results[0];
                            String time = results[1];
                            String voltage = results[1];
                            String inverter_aux = results[2];
                            String unit = results[3];
                            String fuel_degree = results[4];
                            String alarm = results[5];
                            String temp_inside = results[7];
                            String temp_outside = results[8];
                            String insolation_degree = results[9];
                            String time_until_disabled = results[10];
                            message = "Czas: "+time + "\nNapięcie: " + voltage + "V\nRezerwa: "+ fuel_degree + "\nAlarm: " + alarm + "\nAgregat włączony: "+ unit + "\nStyk pomocniczy: " + inverter_aux;
                            



                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //jsonObject=  new JSONObject(message);

                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        showMessage(message, Color.RED);
                        break;
                    }
                    showMessage( message, clientTextColor);
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            OutputStreamWriter writer= new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                            //BufferedReader reader= new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));


                            //message_header = numberToString(length);

                            //dOut.writeByte(0x00);
                            dOut.flush();
                            writer.write( "\u0000"+"\u0061"+ message);
                            //dOut.writeUTF(message); // + "\n");

                            //writer.write( message_header+message); // + "\n");
                            writer.flush();

                            //String line= reader.readLine();
                            //jsonObject= new JSONObject(line);



                            /*PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            message.getChars(0,126,string_char,0);
                            String rawString = "Entwickeln Sie mit Vergnügen";
                            out.println(message);

                             */
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
/*
    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }

}