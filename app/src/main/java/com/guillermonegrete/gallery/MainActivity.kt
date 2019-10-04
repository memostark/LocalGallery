package com.guillermonegrete.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootFolderName: TextView = findViewById(R.id.textView_root_folder)
        rootFolderName.text = "Root folder name"

        val folderList: RecyclerView = findViewById(R.id.folders_list)
        val items = listOf("Person", "Another", "Stuff")

        val adapter = FolderAdapter(items)
        folderList.layoutManager = GridLayoutManager(this, 2)
        folderList.adapter = adapter
    }
}
