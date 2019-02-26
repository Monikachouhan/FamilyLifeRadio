package com.familyliferadio.myflrnew

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.dto.ProgramData
import kotlinx.android.synthetic.main.readmore_layout.*

/**
 * Created by ntf-19 on 7/9/18.
 */
class ReadMoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.readmore_layout)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "Read More"
        }

        val data = intent.getSerializableExtra("OBJECT") as ProgramData

        data.let {
            Glide.with(this@ReadMoreActivity)
                    .load(data.programe_logo)
                    .apply(RequestOptions()
                            .fitCenter()
                            .placeholder(R.mipmap.ic_launcher))
                    .into(logo)
            dec_tv.text = it.program_dec
            title_tv.text = it.programTitle

        }


    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}