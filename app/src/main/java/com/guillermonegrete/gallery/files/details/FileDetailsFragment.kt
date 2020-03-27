package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File

class FileDetailsFragment : Fragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_file_details, container, false)

        val file = arguments?.getString(FILE_KEY) ?: ""

        val viewPager: ViewPager2 = root.findViewById(R.id.file_details_viewpager)
        viewPager.adapter = FileDetailsAdapter(listOf(File(file)))

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
