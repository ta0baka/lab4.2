package com.example.lab42;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView tv;
    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        dbHelper = new DatabaseHelper(this);

        if (!isInternetAvailable()) {
            Toast.makeText(this, "Запуск в автономном режиме. Доступен только просмотр внесенных ранее записей.", Toast.LENGTH_LONG).show();
        } else {
            myAsyncTask = new MyAsyncTask(this, this);
            myAsyncTask.start();
        }
        displaySongs();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void displaySongs() {
        Cursor cursor = dbHelper.getAllSongs();
        StringBuilder songsList = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                int artistIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ARTIST);
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
                int timestampIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP);

                String artist = cursor.getString(artistIndex);
                String title = cursor.getString(titleIndex);
                String timestamp = cursor.getString(timestampIndex);
                songsList.append(artist).append(" - ").append(title).append(" (").append(timestamp).append(")\n");
            } while (cursor.moveToNext());
        }
        tv.setText(songsList.toString());
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myAsyncTask != null) {
            myAsyncTask.stop();
        }
    }
}