package com.example.myradio2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RadioAdapter adapter;
    private List<RadioStation> radioList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission(); // ⭐ 반드시 여기
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        radioList = new ArrayList<>();
        adapter = new RadioAdapter(radioList, station ->
                playRadio(station.getStreamUrl())
        );

        recyclerView.setAdapter(adapter);

        loadRadioStations();
    }

    private void playRadio(String url) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                // 권한 없으면 재요청
                requestNotificationPermission();
                return;
            }
        }

        Intent intent = new Intent(this, RadioService.class);
        intent.setAction(RadioService.ACTION_PLAY);
        intent.putExtra(RadioService.EXTRA_URL, url);
        ContextCompat.startForegroundService(this, intent);
    }
    private void loadRadioStations() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://de1.api.radio-browser.info/json/stations/bycountry/korea")
                .header("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) return;

                try {
                    String body = response.body().string();
                    JSONArray jsonArray = new JSONArray(body);

                    runOnUiThread(() -> {
                        radioList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.optJSONObject(i);
                            if (obj == null) continue;

                            String name = obj.optString("name");
                            String country = obj.optString("country");
                            String streamUrl = obj.optString("url_resolved");

                            if (streamUrl == null || streamUrl.isEmpty()) continue;

                            radioList.add(
                                    new RadioStation(name, country, streamUrl)
                            );
                        }

                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }
}