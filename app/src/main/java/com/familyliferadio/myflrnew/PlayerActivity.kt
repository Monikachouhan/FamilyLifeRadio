package com.familyliferadio.myflrnew

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.db.AlarmDatabase
import com.familyliferadio.myflrnew.player.PlaybackStatus
import com.familyliferadio.myflrnew.player.RadioManager
import com.familyliferadio.myflrnew.utils.MakeNetworkRequest
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.app_bar_player.*
import kotlinx.android.synthetic.main.content_player.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import saschpe.exoplayer2.ext.icy.IcyHttpDataSource


class PlayerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private var title: String = ""
    private var url: String = ""
    private var logo = ""
    private var index = 0
    private var audioList: List<ParseObject>? = null
    private var TAG: String = "PlayerActivity"
    private var audioTitle: String? = null
    public var isAlarm = false


    private val dialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
    }
    val manager by lazy {
        RadioManager.with(applicationContext)
    }

    @Subscribe
    fun onEvent(state: String) {
        when (state) {

            PlaybackStatus.PLAYING -> {
                Log.d("RequestQuery", " PLAYING ")
                progress.visibility = View.GONE
                btnPlayPause.setImageResource(R.drawable.ic_pause_30dp)
            }
            PlaybackStatus.PAUSED -> {
                //  Log.d("RequestQuery", " PAUSED ")
                progress.visibility = View.GONE
                btnPlayPause.setImageResource(R.drawable.ic_play)
            }
            PlaybackStatus.STOPPED -> {
                // Log.d("RequestQuery", " STOPPED ")
                progress.visibility = View.GONE
                btnPlayPause.setImageResource(R.drawable.ic_play)
            }
            PlaybackStatus.LOADING -> {
                Log.d("RequestQuery", " LOADING ")
                progress.visibility = View.VISIBLE
                btnPlayPause.setImageResource(R.drawable.ic_pause_30dp)

            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataEvent(icyHeaders: IcyHttpDataSource.IcyHeaders) {
        // txtTitle!!.text = icyHeaders.name
        Log.d("RequestQuery", " onHeaderEvent $icyHeaders" + icyHeaders.name)
        //   Log.d("RequestQuery", " onHeaderEvent $icyHeaders"+icyHeaders.url)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMetaDataEvent(icyMetaData: IcyHttpDataSource.IcyMetadata) {
        // Log.d("RequestQuery", " onMetaDataEvent $icyMetaData " + icyMetaData.streamUrl.trim())
        txtTitle!!.text = icyMetaData.streamTitle
        if (audioTitle == null) {
            this.audioTitle = icyMetaData.streamTitle
            MakeNetworkRequest(this, getAlbumArtUrl(icyMetaData.streamUrl.replace(" ", "+").replace("-", "+")))
                    .sendRequest(MakeNetworkRequest.getRequestQueue())
        } else if (!audioTitle.equals(icyMetaData.streamTitle, true)) {
            this.audioTitle = icyMetaData.streamTitle
            MakeNetworkRequest(this, getAlbumArtUrl(icyMetaData.streamUrl.replace(" ", "+").replace("-", "+")))
                    .sendRequest(MakeNetworkRequest.getRequestQueue())

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlbumReponseEvent(response: JSONObject) {
        val url = response.getJSONArray("results").getJSONObject(0).getString("artworkUrl100")
        manager.updateNotification(audioTitle, url)

        Glide.with(this@PlayerActivity)
                .load(url.replace("100x100bb","800x800bb"))
                .apply(RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL))

                .into(imgCover)
        Log.d("RequestQuery", " getQuery $url")

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReponseErrorEvent(error: VolleyError) {
        Log.d("RequestQuery", "onReponseErrorEvent $error")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        dialog.show()

        isAlarm = intent.getBooleanExtra("isAlarm", false)
       /* if (isAlarm)
            manager.setIsAlarmTrue()*/

        ParseQuery.getQuery<ParseObject>("Streaming_URL")
                .findInBackground { obj, e ->
                    try {
                        if (dialog != null && dialog.isShowing)
                            dialog.dismiss()
                    }catch (e :Exception){
                        e.printStackTrace()
                        Log.d("RequestQuery", " dialog error")
                    }
                  //  Log.d("RequestQuery", " findInBackground $e $obj")

                    if (e == null && obj.isNotEmpty()) {
                        this.audioList = obj
                        this.updateAudio()

                    }
                }

        //Listeners
        btnMenu.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }
        this.setPlayerBtnListener()
        this.setUpDrawer()
    }

    private fun getAlbumArtUrl(albumName: String): String {
        return StringBuilder("https://itunes.apple.com/search?term=")
                .append(albumName).append("&media=music&limit=1").toString()
    }

    private fun setPlayerBtnListener() {
        btnPlayPause.setOnClickListener(this)
        btnAudioToggle.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnAlarm.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnPlayPause -> {
                manager.playOrPause(url)
            }
            R.id.btnPrevious -> {
                this.setPreviousAudio()
            }
            R.id.btnNext -> {
                this.setNextAudioIndex()
            }
            R.id.btnAudioToggle -> {
                toggleVolume()
            }
            R.id.btnAlarm -> {
                startActivity(Intent(this@PlayerActivity, AlarmActivity::class.java))
            }
        }
    }

    private fun setUpDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun updateAudio() {
        audioList?.let {
            title = it[index].getString("title")
            this.logo = it[index].getParseFile("image_logo").url
            url = it[index].getString("streaming_url")
            txtTitle!!.text = this.title

            manager.playOrPause(url)
            audioTitle = null
            Glide.with(applicationContext)
                    .load(this.logo)
                    .apply(RequestOptions()
                            .fitCenter())
                    .into(imgCover)
            Log.d("RequestQuery", " getQuery $url")
        }
    }

    private fun setPreviousAudio() {
        audioList?.let {
            Log.d(TAG, "in next audio")
            if (index > 0) {
                index--
                this.updateAudio()
            }
        }
    }

    private fun setNextAudioIndex() {
        audioList?.let {
            Log.d(TAG, "in next audio")
            if (index < it.size - 1) {
                index++
                this.updateAudio()
            }
        }
    }

  /*  override fun onNewIntent(intent: Intent?) {
        this.onCreate(null)
        super.onNewIntent(intent)

    }*/
    private fun toggleVolume() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0)
            if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                btnAudioToggle.setImageResource(R.drawable.ic_volume_off)
            } else {
                btnAudioToggle.setImageResource(R.drawable.ic_volume_on)
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog.Builder(this)
                    .setMessage("Are you sure?")
                    .setPositiveButton("Exit") { dialog, which ->
                        dialog.dismiss()
                        this.finishAffinity()
                        this.finish()
                    }.setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }.show()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        manager.bind()
        val list = AlarmDatabase.getInstance(this).daoAccess().fetchAllAlarms()
        list?.let {
            if (list.isNotEmpty()) {
                btnAlarm!!.setImageResource(R.drawable.notification_active)
            } else {
                btnAlarm!!.setImageResource(R.drawable.ic_alarm)

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        manager.unbind()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_schedule -> {
                startActivity(Intent(this, ProgramScheduleActivity::class.java))
            }
            R.id.nav_contact -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.myflr.org/contact/")))
            }
            R.id.nav_donate -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://familylife.givingfuel.com/flr-main-donate")))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
