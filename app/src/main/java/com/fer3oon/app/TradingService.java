package com.fer3oon.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TradingService extends Service {

    private static final String TAG = "Fer3oon";
    private static final String BASE =
        "https://demo-api-capital.backend-capital.com/api/v1";

    private volatile boolean running = false;
    private Thread engineThread;

    private String apiKey;
    private String cst;
    private String securityToken;
    private String identifier;
    private String password;
    private double lotSize = 0.01;

    private static final int NOTIFICATION_ID = 7001;
    private static final String CHANNEL_ID = "fer3oon_trading";



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fer3oon DEMO Trading",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("حالة محرك التداول التجريبي");
            NotificationManager manager =
                getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Fer3oon - DEMO")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
        }
        return new Notification.Builder(this)
            .setContentTitle("Fer3oon - DEMO")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build();
    }

    private void clearCredentials() {
        identifier = null;
        apiKey = null;
        password = null;
        cst = null;
        securityToken = null;
    }

    private void saveSessionHeaders(HttpURLConnection conn) {
        String newCst = conn.getHeaderField("CST");
        String newToken = conn.getHeaderField("X-SECURITY-TOKEN");

        if (newCst != null) cst = newCst;
        if (newToken != null) securityToken = newToken;
    }

    private JSONObject request(String method, String path, JSONObject body)
            throws Exception {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("X-CAP-API-KEY", apiKey);
        conn.setRequestProperty("Accept", "application/json");

        if (cst != null) {
            conn.setRequestProperty("CST", cst);
            conn.setRequestProperty("X-SECURITY-TOKEN", securityToken);
        }

        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.toString().getBytes("UTF-8"));
            }
        }

        int code = conn.getResponseCode();
        saveSessionHeaders(conn);
        java.io.InputStream stream =
            (code >= 200 && code < 400)
            ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder result = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
        }
        conn.disconnect();

        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + result);
        }
        if (result.length() == 0) return new JSONObject();
        return new JSONObject(result.toString());
    }






    private double[] extractCloses(JSONObject response) {
        JSONArray prices = response.optJSONArray("prices");
        if (prices == null) return new double[0];

        double[] closes = new double[prices.length()];
        for (int i = 0; i < prices.length(); i++) {
            JSONObject candle = prices.optJSONObject(i);
            if (candle == null) continue;

            JSONObject close = candle.optJSONObject("closePrice");
            if (close == null) continue;

            double bid = close.optDouble("bid", Double.NaN);
            double ask = close.optDouble("ask", Double.NaN);

            if (!Double.isNaN(bid) && !Double.isNaN(ask)) {
                closes[i] = (bid + ask) / 2.0;
            } else {
                closes[i] = Double.NaN;
            }
        }
        return closes;
    }

    private double sma(double[] values, int period) {
        if (values == null || values.length < period) {
            return Double.NaN;
        }

        double sum = 0;
        for (int i = values.length - period; i < values.length; i++) {
            sum += values[i];
        }
        return sum / period;
    }

    private String signal(double[] candles) {
        if (candles == null || candles.length < 31) {
            return null;
        }

        double[] closed = new double[candles.length - 1];
        System.arraycopy(candles, 0, closed, 0, closed.length);

        int n = closed.length;
        double fastPrevious = sma(
            java.util.Arrays.copyOfRange(closed, 0, n - 1), 10
        );
        double slowPrevious = sma(
            java.util.Arrays.copyOfRange(closed, 0, n - 1), 30
        );
        double fastNow = sma(closed, 10);
        double slowNow = sma(closed, 30);

        if (fastPrevious <= slowPrevious && fastNow > slowNow) {
            return "BUY";
        }
        if (fastPrevious >= slowPrevious && fastNow < slowNow) {
            return "SELL";
        }
        return null;
    }

    private JSONObject getPrices(String epic) throws Exception {
        return request(
            "GET",
            "/prices/" + epic + "?resolution=MINUTE&max=50",
            null
        );
    }

    private JSONArray getOpenPositions() throws Exception {
        JSONObject response = request("GET", "/positions", null);
        JSONArray positions = response.optJSONArray("positions");
        return positions != null ? positions : new JSONArray();
    }

    private boolean login() {
        try {
            JSONObject body = new JSONObject();
            body.put("identifier", identifier);
            body.put("password", password);
            body.put("encryptedPassword", false);

            request("POST", "/session", body);

            boolean valid = cst != null && securityToken != null;
            Log.i(TAG, valid ? "DEMO LOGIN OK" : "DEMO LOGIN FAILED");
            return valid;
        } catch (Exception e) {
            Log.e(TAG, "DEMO LOGIN ERROR: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "TradingService created - DEMO ONLY");
    }


    public static final String ACTION_START = "com.fer3oon.app.START";
    public static final String ACTION_STOP = "com.fer3oon.app.STOP";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_STOP.equals(action)) {
            running = false;
            if (engineThread != null) engineThread.interrupt();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {
            identifier = intent.getStringExtra("email");
            apiKey = intent.getStringExtra("key");
            password = intent.getStringExtra("password");

            String selectedLot = intent.getStringExtra("lot");
            if (selectedLot != null) {
                try {
                    lotSize = Double.parseDouble(selectedLot);
                } catch (NumberFormatException e) {
                    lotSize = 0.01;
                }
            }

            Log.i(TAG, "DEMO credentials received; trading engine remains disabled");
            clearCredentials();
            stopSelf();
            return START_NOT_STICKY;
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
