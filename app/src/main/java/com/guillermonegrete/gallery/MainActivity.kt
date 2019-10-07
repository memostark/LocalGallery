package com.guillermonegrete.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var loadingIcon: ProgressBar
    private lateinit var folderListContainer: View
    private lateinit var messageContainer: View

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

        loadingIcon = findViewById(R.id.folders_progress_bar)
        folderListContainer = findViewById(R.id.folders_linear_layout)
        messageContainer = findViewById(R.id.folders_message_container)

        setViewModel()
    }

    private fun setViewModel() {
        val factory = ViewModelFactory(SettingsRepository())
        val viewModel = ViewModelProvider(this, factory).get(FoldersViewModel::class.java).apply {

            dataLoading.observe(this@MainActivity, Observer {
                loadingIcon.visibility = if(it) View.VISIBLE else View.GONE
            })

            hasUrl.observe(this@MainActivity, Observer {
                folderListContainer.visibility = if(it) View.VISIBLE else View.GONE
                messageContainer.visibility = if(it) View.GONE else View.VISIBLE
            })
            loadFolders()
        }
    }
}
