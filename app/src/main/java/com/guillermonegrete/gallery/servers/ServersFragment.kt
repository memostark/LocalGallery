package com.guillermonegrete.gallery.servers

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.DialogSetServerAddressBinding
import com.guillermonegrete.gallery.folders.ServerScanner
import kotlinx.coroutines.launch

class ServersFragment: DialogFragment(){

    private var _binding: DialogSetServerAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val view = it.layoutInflater.inflate(R.layout.dialog_set_server_address, null)

            _binding = DialogSetServerAddressBinding.bind(view)
            bindLayout()

            builder.setView(view)
                .setMessage("Set server address") // TODO use string resource
                .setPositiveButton(R.string.ok) { _, _ ->
                    val serverIp = binding.serverAddressEdit.text.toString()
                    setFragmentResult(REQUEST_KEY, bundleOf(IP_KEY to serverIp))
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->  dialog.cancel() }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun bindLayout(){
        val serversAdapter = ServersAdapter()
        binding.serversList.apply {
            adapter = serversAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.serverAddressEdit.setText(arguments?.getString(IP_KEY))

        binding.searchServersBtn.setOnClickListener {
            lifecycleScope.launch {
                val scanner = ServerScanner()
                val ip = scanner.search()

                if(ip != null) serversAdapter.addServer(ip)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val IP_KEY = "preset-ip"
        const val REQUEST_KEY = "server_frag_request"
    }
}