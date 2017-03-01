package com.example.cindy.directline;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.os.StrictMode;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MainActivity extends AppCompatActivity {

    String TAG="Cindy";

    public String botName ="Cindy";
    public final String secretKey="your key";
    public String token ="";
    public String conversationId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Step1
        // Exchanging a secret for a token
        getTokenAndConversationid();

        //step2
        //Starting a conversation
        startConversation();

        //step3
        // Sending an Activity to the bot
        sendMessageToBot();

        //step4
        // Receiving the bot response by GET
        receiveMessageByBot();
    }

    private void getTokenAndConversationid(){
        Log.d(TAG, "step1 getTokenAndConversationid");
        String UrlText="https://directline.botframework.com/v3/directline/tokens/generate";

        URL url = null;
        HttpURLConnection urlConnection = null;
        String responseValue = "";

        try {
            url = new URL(UrlText);

            //  urlConnection = (HttpURLConnection) url.openConnection();
            //ignore https certificate validation
            if (url.getProtocol().toUpperCase().equals("HTTPS")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url
                        .openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlConnection = https;
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }


            String basicAuth = "Bearer "  + secretKey;
            Log.d(TAG, "basicAuth: "+basicAuth);

            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");



            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "responseCode :"+responseCode);
            if (responseCode >= 400 && responseCode <= 499) {
                throw new Exception("Errorcode: "+responseCode);
            }
            else {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                responseValue = readStream(in);
                Log.d(TAG,"responseValue:  "+responseValue);

                JSONObject jsonObject = null;
                jsonObject = new JSONObject(responseValue);

                conversationId=jsonObject.getString("conversationId");
                Log.d(TAG, "conversationId: "+conversationId);

                token= jsonObject.getString("token");
                Log.d(TAG, "token: "+token);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    private void startConversation(){
        Log.d(TAG, "step2 startConversation");

        String UrlText = "https://directline.botframework.com/v3/directline/conversations";
        URL url = null;
        HttpURLConnection urlConnection = null;
        String responseValue = "";

        try {
            url = new URL(UrlText);

            urlConnection = (HttpURLConnection) url.openConnection();
            String basicAuth = "Bearer "  + token;
            Log.d(TAG, "basicAuth: "+basicAuth);

            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");


            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "responseCode :"+responseCode);
            if (responseCode >= 400 && responseCode <= 499) {
                throw new Exception("Errorcode: "+responseCode);
            }
            else {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                responseValue = readStream(in);
                Log.d(TAG,"responseValue:  "+responseValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    private void sendMessageToBot() {

        Log.d(TAG, "step3 sendMessageToBot");

        String UrlText = "https://directline.botframework.com/v3/directline/conversations/" + conversationId + "/activities";
        URL url = null;
        HttpURLConnection urlConnection = null;

        String inputText = "Hello";

        if (conversationId != null) {
            try {
                url = new URL(UrlText);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "message");
                jsonObject.put("from", (new JSONObject().put("id", "user1")));
                jsonObject.put("text", inputText);
                String postData = jsonObject.toString();
                Log.d(TAG, "sendMessage: "+postData);

                urlConnection = (HttpURLConnection) url.openConnection();
                String basicAuth = "Bearer " + secretKey;
                urlConnection.setRequestProperty("Authorization", basicAuth);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStream out = urlConnection.getOutputStream();
                out.write(postData.getBytes());
                out.flush();
                out.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "responseCode: "+responseCode);
                if (responseCode >= 400 && responseCode <= 499) {
                    throw new Exception("Errorcode: "+responseCode);
                } else {

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String responseValue = readStream(in);
                    Log.d(TAG, "response:" + responseValue);

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

        }
    }

    private void receiveMessageByBot(){
        Log.d(TAG, "step4 receiveMessageByBot");

        String UrlText = "https://directline.botframework.com/v3/directline/conversations/" + conversationId + "/activities";

        URL url = null;
        HttpURLConnection urlConnection = null;
        String responseValue = "";
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(500);
                url = new URL(UrlText);

                urlConnection = (HttpURLConnection) url.openConnection();
                String basicAuth = "Bearer " + secretKey;
                urlConnection.setRequestProperty("Authorization", basicAuth);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "responseCode :" + responseCode);
                if (responseCode >= 400 && responseCode <= 499) {
                    throw new Exception("Errorcode: " + responseCode);
                } else {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    responseValue = readStream(in);
                    Log.d(TAG, "responseValue:  " + responseValue);
                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private String readStream(InputStream in) {
        char[] buf = new char[2048];
        Reader r = null;
        try {
            r = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = 0;
            try {
                n = r.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (n < 0)
                break;
            s.append(buf, 0, n);
        }
        return s.toString();
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

}