package com.guillermonegrete.gallery.servers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.databinding.ServerItemBinding

class ServersAdapter: RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    private val servers = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ServerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
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

    class ViewHolder(private val binding: ServerItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(text: String){
            binding.serverIp.text = text
        }
    }
}