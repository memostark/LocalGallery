package com.guillermonegrete.gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FileDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_file_details, container, false)

        val file = arguments?.getString(FILE_KEY) ?: ""

        val nameText: TextView = root.findViewById(R.id.file_name_text)
        nameText.text = file

        val fileImage: ImageView = root.findViewById(R.id.file_image)
        Glide.with(this)
            .load(file)
            .into(fileImage)

        val linkButton: ImageButton = root.findViewById(R.id.open_link_button)
        linkButton.setOnClickListener { openLink(file) }

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    private fun openLink(item: String){
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(item)
        }
        startActivity(intent)
    }

    companion object{
        const val FILE_KEY = "folder"
    }
}
