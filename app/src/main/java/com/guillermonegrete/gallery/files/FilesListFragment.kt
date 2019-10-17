package com.guillermonegrete.gallery.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.guillermonegrete.gallery.R

class FilesListFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_files_list, container, false)

        val folderName: TextView = root.findViewById(R.id.folder_name_text)
        folderName.text = arguments?.getString(FOLDER_KEY) ?: ""

        return root
    }

    companion object{
        const val FOLDER_KEY = "folder"
    }
}