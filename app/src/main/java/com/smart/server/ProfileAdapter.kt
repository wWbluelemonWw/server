package com.smart.server

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//class ProfileAdapter(val profileList: ArrayList<Profiles>) : RecyclerView.Adapter<ProfileAdapter.CustomViewHolder>(){
class ProfileAdapter(
    val profileList: ArrayList<Profiles>,
    private val context: Context
) : RecyclerView.Adapter<ProfileAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return CustomViewHolder(view).apply {
            itemView.setOnClickListener {

            }
        }
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titlename = itemView.findViewById<TextView>(R.id.titlename)
        val regionname = itemView.findViewById<TextView>(R.id.regionname)
        val cityname = itemView.findViewById<TextView>(R.id.cityname)
        val personname = itemView.findViewById<TextView>(R.id.personname)
        val goButton = itemView.findViewById<Button>(R.id.Go)

        init {
            goButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val profile = profileList[position]
                    val intent = Intent(itemView.context, Map::class.java)
                    val myCode : String = MakeRoom().generateRandomString(8)
                    intent.putExtra("region", profile.regionname)
                    intent.putExtra("city", profile.cityname)
                    intent.putExtra("person", profile.personname)
                    intent.putExtra("myCode", myCode)
                    intent.putExtra("roomCode", profile.roomCode)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.titlename.text = profileList.get(position).titlename
        holder.regionname.text = profileList.get(position).regionname
        holder.cityname.text = profileList.get(position).cityname
        holder.personname.text = profileList.get(position).personname

    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}