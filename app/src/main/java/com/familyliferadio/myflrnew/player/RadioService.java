package com.familyliferadio.myflrnew.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.familyliferadio.myflrnew.R;
import com.familyliferadio.myflrnew.utils.MakeNetworkRequest;
import com.familyliferadio.myflrnew.utils.TimeUtility;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSourceFactory;

public class RadioService extends Service implements Player.EventListener, AudioManager.OnAudioFocusChangeListener, MakeNetworkRequest.GetVolleyRequestCallback {

    public static final String ACTION_PLAY = "com.mcakir.radio.player.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.mcakir.radio.player.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.mcakir.radio.player.ACTION_STOP";
    float volume = 0;
    float speed = 0.05f;
    public boolean isAlarm = false;
    private final IBinder iBinder = new LocalBinder();

    private Handler handler;
    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private boolean onGoingCall = false;
    private TelephonyManager telephonyManager;

    private WifiManager.WifiLock wifiLock;

    private AudioManager audioManager;

    private MediaNotificationManager notificationManager;
    private IcyHttpDataSource.IcyMetadata audioMetaData;
    private String status;

    private String strAppName;
    private String strLiveBroadcast;
    private String albumArt;
    private String streamUrl;
    private String audioTitle;

    @Override
    public void onGetResponse(JSONObject response) {
        String url = null;
        try {
            url = response.getJSONArray("results").getJSONObject(0).getString("artworkUrl100");
            notificationManager.updatNotification(audioTitle, url);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetError(VolleyError error) {

    }

    public class LocalBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            pause();
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (state == TelephonyManager.CALL_STATE_OFFHOOK
                    || state == TelephonyManager.CALL_STATE_RINGING) {

                if (!isPlaying()) return;

                onGoingCall = true;
                stop();

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                if (!onGoingCall) return;

                onGoingCall = false;
                resume();
            }
        }
    };

    private MediaSessionCompat.Callback mediasSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPause() {
            super.onPause();

            pause();
        }

        @Override
        public void onStop() {
            super.onStop();

            stop();
            notificationManager.cancelNotify();
        }

        @Override
        public void onPlay() {
            super.onPlay();

            resume();
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        strAppName = getResources().getString(R.string.app_name);
        strLiveBroadcast = getResources().getString(R.string.live_broadcast);

        onGoingCall = false;

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        notificationManager = new MediaNotificationManager(this);

        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mcScPAmpLock");

        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "...")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, strLiveBroadcast)
                .build());
        mediaSession.setCallback(mediasSessionCallback);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        handler = new Handler();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addListener(this);

        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        status = PlaybackStatus.IDLE;


    }


  /*  public void fadeIn(final int duration) {
        Log.d("Streaming", " fadeIn " + duration);
        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {

                // can call h again after work!
                time += 100;
                volume = (deviceVolume * time) / duration;
                exoPlayer.setVolume(volume);
                if (time < duration)
                    Log.d("Streaming", " time is letss fadeIn " + duration);

                h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)

    }*/

    public float getDeviceVolume() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return (float) volumeLevel / maxVolume;
    }

    /* public void startFadeIn() {
         Log.d("mPlayer", "startFadeIn called");
         final int FADE_DURATION = 3000; //The duration of the fade
         //The amount of time between volume changes. The smaller this is, the smoother the fade
         final int FADE_INTERVAL = 250;
         int MAX_VOLUME = 1; //The volume will increase from 0 to 1
         AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        *//*  int maxVolume =  am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
         if (maxVolume>0)
             MAX_VOLUME=maxVolume;*//*
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MAX_VOLUME / (float) numberOfSteps;
        Log.d("mPlayer", "startFadeIn numberOfSteps called  " + MAX_VOLUME + "  " + numberOfSteps);
        final float value = MAX_VOLUME;
        //Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                fadeInStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                Log.d("mPlayer", "TimerTask run called " + volume);

                if (volume >= 1f) {
                    isAlarm = false;
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, 0, FADE_DURATION);
    }

    private void fadeInStep(float deltaVolume) {
        Log.d("mPlayer", "startFadeIn run called " + deltaVolume + " " + volume);
        exoPlayer.setVolume(volume);
        volume += deltaVolume;

    }
*/
    public void setOnZeroVolume() {
        //  exoPlayer.setVolume(0f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (TextUtils.isEmpty(action))
            return START_NOT_STICKY;

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            stop();

            return START_NOT_STICKY;
        }

        if (action.equalsIgnoreCase(ACTION_PLAY)) {

            transportControls.play();

        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {

            transportControls.pause();

        } else if (action.equalsIgnoreCase(ACTION_STOP)) {

            transportControls.stop();

        }

        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (status.equals(PlaybackStatus.IDLE))
            stopSelf();


        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(final Intent intent) {

    }

    @Override
    public void onDestroy() {

        pause();
        exoPlayer.release();
        exoPlayer.removeListener(this);

        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        notificationManager.cancelNotify();

        mediaSession.release();

        unregisterReceiver(becomingNoisyReceiver);

        super.onDestroy();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:

                exoPlayer.setVolume(0.8f);

                resume();

                break;

            case AudioManager.AUDIOFOCUS_LOSS:

                stop();

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                if (isPlaying()) pause();

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                if (isPlaying())
                    exoPlayer.setVolume(0.1f);

                break;
        }

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                status = PlaybackStatus.LOADING;
                //     Log.d("Streaming", "onPlayerStateChanged STATE_BUFFERING " + exoPlayer.getBufferedPercentage());
                break;
            case Player.STATE_ENDED:
                //   Log.d("Streaming", "onPlayerStateChanged STATE_ENDED");
                status = PlaybackStatus.STOPPED;
                break;
            case Player.STATE_IDLE:
                // Log.d("Streaming", "onPlayerStateChanged STATE_IDLE");
                status = PlaybackStatus.IDLE;
                break;
            case Player.STATE_READY:
                Log.d("Streaming", "onPlayerStateChanged STATE_READY " + isAlarm);
                Log.d("RequestQuery", " serviec STATE_READY");
                status = playWhenReady ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
              /*  if (isAlarm && status.equalsIgnoreCase(PlaybackStatus.PLAYING)) {
                    startFadeIn();
                }*/
                break;
            default:
                // Log.d("Streaming", "onPlayerStateChanged IDLE");
                status = PlaybackStatus.IDLE;
                break;
        }

        if (!status.equals(PlaybackStatus.IDLE)) {
            notificationManager.startNotify(status, audioMetaData);
        }
        EventBus.getDefault().post(status);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.d("Streaming", "trackSelections " + trackSelections.length);

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d("Streaming", "onLoadingChanged is Loding and is Alarm " + "  " + exoPlayer.isLoading() + " " + isLoading + " " + isAlarm);

        /*if (!isLoading && isAlarm) {
            fadeIn(9000);*/

    }

    ;

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d("Streaming", "play onPlayerError " + error.getCause().getMessage().toString());

        EventBus.getDefault().post(PlaybackStatus.ERROR);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.d("Streaming", "onPositionDiscontinuity " + reason);

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public void play(String streamUrl) {

        this.streamUrl = streamUrl;

        if (wifiLock != null && !wifiLock.isHeld()) {
            wifiLock.acquire();

        }
        Log.d("onIcyHeaders", " play  " + streamUrl);

  /*      Log.d("Streaming", "play called");
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(streamUrl);
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
        mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
        Bitmap b = mmr.getFrameAtTime(2000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
        byte [] artwork = mmr.getEmbeddedPicture();
        Log.d("onIcyStreaming", "play called "+mmr.getMetadata().getAll());
*/
        IcyHttpDataSourceFactory icyHttpDataSourceFactory = new IcyHttpDataSourceFactory.Builder(new OkHttpClient.Builder().build())
                .setIcyHeadersListener(icyHeaders -> {
                      Log.d("onIcyHeaders", "onIcyHeaders: %s"+icyHeaders);
                    EventBus.getDefault().post(icyHeaders);
                }).setIcyMetadataChangeListener(icyMetadata -> {
                       Log.d("onIcyMetaData", "onIcyMetaData: %s"+icyMetadata);
                    audioMetaData = icyMetadata;
                    EventBus.getDefault().post(icyMetadata);
                    if (audioTitle == null) {
                        audioTitle = icyMetadata.getStreamTitle();
                        new MakeNetworkRequest(getApplicationContext(), getAlbumArtUrl(icyMetadata.getStreamUrl().replace(" ", "+").replace("-", "+")), RadioService.this)
                                .sendRequest(MakeNetworkRequest.getRequestQueue());
                       // updateMetaData();
                    } else if (!audioTitle.equalsIgnoreCase(icyMetadata.getStreamTitle())) {
                        audioTitle = icyMetadata.getStreamTitle();
                        new MakeNetworkRequest(getApplicationContext(), getAlbumArtUrl(icyMetadata.getStreamUrl().replace(" ", "+").replace("-", "+")), RadioService.this)
                                .sendRequest(MakeNetworkRequest.getRequestQueue());


                    }
                    updateMetaData();
                })
                .build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, null, icyHttpDataSourceFactory);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, getUserAgent(), BANDWIDTH_METER);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(streamUrl));


        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

    }

    void updateMetaData() {
        if (mediaSession != null) {
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "...")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioTitle)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, audioTitle)
                    .build());
        }


    }

    private String getAlbumArtUrl(String albumName) {
        return "https://itunes.apple.com/search?term=" +
                albumName + "&media=music&limit=1";
    }

    public void updateNotifications(String title, String url) {
        if (!status.equals(PlaybackStatus.IDLE))
            notificationManager.updatNotification(title, url);
    }

    public void getAudioLength() {
        String duration = TimeUtility.formateLength(exoPlayer.getDuration());
        exoPlayer.getDuration();
        Log.d("Streaming", "audio length " + duration);
    }

    public void resume() {
        if (streamUrl != null) {
            play(streamUrl);
            Log.d("Streaming", "resume");

        }
    }

    public void pause() {

        exoPlayer.setPlayWhenReady(false);

        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }

    public void stop() {

        exoPlayer.stop();

        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }

    public void playOrPause(String url) {

        if (streamUrl != null && streamUrl.equals(url)) {
            Log.d("RequestQuery", " servic playOrPause " + streamUrl);

            if (!isPlaying()) {

                play(streamUrl);

            } else {

                pause();
            }

        } else {

            if (isPlaying()) {

                pause();

            }

            play(url);
        }
    }

    public String getStatus() {

        return status;
    }

    public MediaSessionCompat getMediaSession() {

        return mediaSession;
    }

    public boolean isPlaying() {

        return this.status.equals(PlaybackStatus.PLAYING);
    }

    private void wifiLockRelease() {

        if (wifiLock != null && wifiLock.isHeld()) {

            wifiLock.release();
        }
    }

    private String getUserAgent() {

        return Util.getUserAgent(this, getClass().getSimpleName());
    }
}
