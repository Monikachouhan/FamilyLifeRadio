package com.familyliferadio.myflrnew.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.ReadMoreActivity
import com.familyliferadio.myflrnew.dto.ProgramData
import kotlinx.android.synthetic.main.itemlist_layout.view.*

/**
 * Created by ntf-19 on 6/9/18.
 */
class ProgScheduleAdapter(private val items: ArrayList<ProgramData>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val contentItemView = LayoutInflater.from(parent.context).inflate(
                R.layout.itemlist_layout, parent, false)
        return ViewHolder(contentItemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemsData = items[position]
        val viewHolder = holder as ViewHolder
        viewHolder.pro_title.text = itemsData.programTitle
        viewHolder.pro_time.text = itemsData.time;
        viewHolder.pro_dec.text = itemsData.program_dec

        Glide.with(context)
                .load(itemsData.programe_logo)
                .apply(RequestOptions()
                        .fitCenter()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(viewHolder.pro_logo)

        viewHolder.readmore.setOnClickListener {
            context.startActivity(Intent(context, ReadMoreActivity::class.java)
                    .putExtra("OBJECT", itemsData))
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val pro_title = view.program_title
        val pro_time = view.programe_time
        val pro_logo = view.image
        val pro_dec = view.programe_dec
        val readmore = view.show


    }

}