package com.guillermonegrete.gallery

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FileDetailsFragment : Fragment() {

    private lateinit var bottomSheet: LinearLayout

    @SuppressLint("ClickableViewAccessibility")
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

        bottomSheet = root.findViewById(R.id.bottom_layout)
        // We need to return true so the bottom sheet handles the touch events.
        // We only do this because otherwise PhotoView consumes the events.
        bottomSheet.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onStop() {
        super.onStop()
        showStatusBar()
    }

    private fun openLink(item: String){
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(item)
        }
        startActivity(intent)
    }

    private fun hideStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.hide()

        val window = activity?.window
        if (Build.VERSION.SDK_INT < 16) {
            window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

    }

    private fun showStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.show()

        val window = activity?.window
        if (Build.VERSION.SDK_INT < 16) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window?.decorView?.systemUiVisibility = 0
        }
    }

    companion object{
        const val FILE_KEY = "folder"
    }
}
