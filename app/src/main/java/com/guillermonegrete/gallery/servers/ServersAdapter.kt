package com.guillermonegrete.gallery.servers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.R

class ServersAdapter: RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    private val servers = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.server_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = servers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(servers[position])
    }

    fun addServer(server: String){
        if(!servers.contains(server)) {
            servers.add(server)
            notifyItemChanged(servers.size - 1)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        private val ipText: TextView = itemView.findViewById(R.id.server_ip)

        fun bind(text: String){
            ipText.text = text
        }
    }
}