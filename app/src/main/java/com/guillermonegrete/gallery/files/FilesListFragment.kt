package com.guillermonegrete.gallery.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.R

class FilesListFragment: Fragment() {

    private lateinit var filesList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_files_list, container, false)

        val folderName: TextView = root.findViewById(R.id.folder_name_text)
        folderName.text = arguments?.getString(FOLDER_KEY) ?: ""

        filesList = root.findViewById(R.id.files_list)
        val adapter = FilesAdapter(listOf(
            "https://google.com",
            "https://twitter.com",
            "https://youtube.com",
            "https://github.com"
        ))
        filesList.adapter = adapter
        filesList.layoutManager = LinearLayoutManager(context)

        return root
    }

    companion object{
        const val FOLDER_KEY = "folder"
    }
}