package com.smart.server

import android.app.Activity
import okhttp3.*
import java.io.IOException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressAdapter(
    val profileList: ArrayList<Addressfiles>,
    private val context: Context
) : RecyclerView.Adapter<AddressAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_address, parent, false)
        return CustomViewHolder(view).apply {
            itemView.setOnClickListener {

            }
        }
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Building = itemView.findViewById<TextView>(R.id.Building)
        val Province = itemView.findViewById<TextView>(R.id.Province)
        val mark_button = itemView.findViewById<Button>(R.id.mark_button)

        init {
            mark_button.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val profile = profileList[position]
                    Log.d("위도/경도 확인",profile.Point.toString())
                    val lat = profile.Point?.latitude ?: 0.0 // 기본값은 0.0
                    val lon = profile.Point?.longitude ?: 0.0 // 기본값은 0.0
                    (context as Map).Map_center(lon, lat)

                    // Get the parent activity's root view
                    val rootView = (itemView.context as Activity).findViewById<View>(android.R.id.content)
                    // Find the address_layout within the activity's root view
                    val address_Layout = rootView.findViewById<View>(R.id.address_layout)
                    // Set the visibility of address_layout to View.INVISIBLE
                    address_Layout.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.Building.text = profileList.get(position).Building
        holder.Province.text = profileList.get(position).Province

    }

    override fun getItemCount(): Int {
        return profileList.size
    }

}