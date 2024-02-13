package com.project_one.httpUrlConnection_with_thread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import httpUrlConnection_with_thread.R;

public class MainActivity extends AppCompatActivity {
    TextView tv_output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        tv_output.setMovementMethod(new ScrollingMovementMethod());
    }
    private void setupViews() {
        tv_output = (TextView) findViewById(R.id.tv_output);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int Id = item.getItemId();
        if (Id == R.id.action_do){
            //cannot access to network in the main thread so we create this thread
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String s = testHttpUrlConnection();
                    throwMsg(s);
                }
                //handler can access to UI thread (main thread) from inside the another thread
                Handler handler = new Handler(){
                    public void handleMessage(Message message){
                        String s = message.getData().getString("MSG_KEY");
                        if (s != null && !s.equals("")){
                            updateUi(s);
                        }
                    }
                };
                private void throwMsg(String s){
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();//in order to send strings in the form of key-value
                    bundle.putString("MSG_KEY", s);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            });
            thread.start();
        }
        return super.onOptionsItemSelected(item);
    }
    public String testHttpUrlConnection(){
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        URL url = null;
        String charset = "UTF-8";
        int response_code;
        String content = null;
        try {
//            url = new URL("https://developer.android.com"); ==> causes 403 error
            url = new URL("https://www.google.com");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(false);//we just use input
            httpURLConnection.setRequestMethod("GET");//GET is the keyword that mean receive data
            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            httpURLConnection.connect();
            response_code = httpURLConnection.getResponseCode();
            Log.d("RESPONSE_CODE_", "response_code : \n" + response_code);
            if (response_code >= 100 && response_code <= 399){
                /*
                        1xx-3xx mean connection is successful
                        4xx : client error
                        5xx : server error
                     */
                inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                content = InputStreamToString(inputStream);
                httpURLConnection.disconnect();
                return content;
            } else {
                Log.d("ERROR", "ERROR CODE : " + response_code);
                httpURLConnection.disconnect();
                return "" + response_code;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String InputStreamToString(InputStream inputStream){
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null){
                stringBuilder.append(line);
            }
            reader.close();
            return stringBuilder.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void updateUi(String s){
        tv_output.append(s + "\n");
    }
}

