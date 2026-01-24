package com.example.myradioapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;import java.util.List;

import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;

import android.os.Handler;
import android.os.Looper;


public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private RecyclerView recyclerView; // 추가된 리스트 뷰
    private StationAdapter adapter;
    private boolean playbackConfirmed = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    // 1. 앱 화면이 생성될 때 호출 (main() 함수와 비슷)
    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML과 자바 연결
        // XML에 있는 뷰 찾기

        playerView = findViewById(R.id.player_view);
        recyclerView = findViewById(R.id.recycler_view);

        // 1. 더미 데이터 생성 (나중에는 서버에서 받아올 부분)
        List<RadioStation> stations = new ArrayList<>();
        stations.add(new RadioStation("BBC World Service", "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service"));
        stations.add(new RadioStation("Classic FM", "http://media-the.musicradio.com/ClassicFM"));
        stations.add(new RadioStation("Energy 98 (Dance)", "https://edge.audioxi.com/ENERGY98"));
        stations.add(new RadioStation("Energy 98 (Dance) Backup", "https://streaming.radio.co/s98a1c2f3e/listen"));
        stations.add(new RadioStation("K-Pop Way", "http://stream.kpopway.com:8000/kpopway"));
        stations.add(new RadioStation("K-Pop Radio", "https://kpopradio.stream.laut.fm/kpopradio"));
        stations.add(new RadioStation("Big B Radio – K-Pop", "https://antares.dribbcast.com/proxy/kpop?mp=/stream"));
        stations.add(new RadioStation("Korea FM – K-Pop", "https://listen.radioking.com/radio/245658/stream/289258"));
        stations.add(new RadioStation("K-Pop Hits Radio", "https://stream.rcast.net/251405"));
        stations.add(new RadioStation("Arirang Radio", "https://amdlive-ch03.ctnd.com.edgesuite.net/arirangradio_720p/chunklist.m3u8"));
        // 예시 URL

        // 2. 어댑터 설정 및 클릭 이벤트 구현 (람다식 사용)
        adapter = new StationAdapter(stations, station -> {
            // 리스트 아이템 클릭 시 실행될 코드:
            // "클릭한 station 객체의 URL로 플레이어를 교체한다"

            playRadio(station.getStreamUrl());
        });

        // 3. 리사이클러뷰에 연결
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 세로 스크롤 설정
        recyclerView.setAdapter(adapter);
    }


    // 플레이어 재생 로직을 별도 함수로 분리하면 깔끔합니다
    private String currentUrl;

    private void playRadio(String url) {
        if (url.equals(currentUrl)) return;
        currentUrl = url;

        playbackConfirmed = false;

        if (player == null) initializePlayer();
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();

        startPlaybackTimeout();
    }

    private void startPlaybackTimeout() {
        handler.removeCallbacks(playbackTimeoutRunnable);
        handler.postDelayed(playbackTimeoutRunnable, 3000);
    }

    // 2. 화면이 눈에 보이기 시작할 때 (초기화)
    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    // 3. 화면이 사라지거나 앱이 꺼질 때 (자원 해제 - 매우 중요!)
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
    private void initializePlayer() {
        if (player == null) {
            // ExoPlayer 객체 생성 (빌더 패턴)
            player = new ExoPlayer.Builder(this).build();


            // PlayerView에 플레이어 연결
            playerView.setPlayer(player);
            // 🔥 여기서 리스너 등록 (중요!)
            player.addListener(new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY && player.getPlayWhenReady()) {
                        playbackConfirmed = true;
                        handler.removeCallbacks(playbackTimeoutRunnable);
                        Log.d("RADIO", "🎉 재생 가능 상태 확인 (READY)");
                    }
                }



                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e("RADIO", "❌ 치명적 오류: " + error.getErrorCodeName());

                    Toast.makeText(
                            MainActivity.this,
                            "이 방송은 재생할 수 없습니다",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) {
                        playbackConfirmed = true;
                        handler.removeCallbacks(playbackTimeoutRunnable);
                        Log.d("RADIO", "🔊 실제 오디오 출력 시작");
                    }
                }
            });

        }
    }
    private void releasePlayer() {
        if (player != null) {
            player.release(); // 메모리 누수 방지를 위해 꼭 해제해야 함
            player = null;
        }
    }
    private Runnable playbackTimeoutRunnable = () -> {
        if (isFinishing() || isDestroyed()) return;

        if (!playbackConfirmed && player != null) {
            player.stop();
            Toast.makeText(
                    getApplicationContext(),
                    "스트림 응답 없음",
                    Toast.LENGTH_SHORT
            ).show();
        }
    };
}