package com.example.lab42;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;

public class MyAsyncTask {
    private final DatabaseHelper dbHelper;
    private String lastSong = "";
    private static final String TAG = "MyTask";
    private final MainActivity activity;
    private final Handler handler = new Handler();
    private final Runnable runnable;

    public MyAsyncTask(Context context, MainActivity activity) {
        dbHelper = new DatabaseHelper(context);
        this.activity = activity;

        runnable = new Runnable() {
            @Override
            public void run() {
                fetchCurrentSong();
                handler.postDelayed(this, 20000);
            }
        };
    }

    public void start() {
        handler.post(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
    }

    private void fetchCurrentSong() {
        new Thread(() -> {
            try {
                URL url = new URL("http://media.ifmo.ru/api_get_current_song.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "login=4707login&password=4707pass";
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("result").equals("success")) {
                    String info = jsonResponse.getString("info");
                    String[] parts = info.split(" – ");
                    String artist = parts[0];
                    String title = parts[1];

                    if (!lastSong.equals(title)) {
                        dbHelper.addSong(artist, title);
                        lastSong = title;
                        activity.runOnUiThread(activity::displaySongs);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in MyTask", e);
            }
        }).start();
    }
}