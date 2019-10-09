package com.guillermonegrete.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository

class MainActivity : AppCompatActivity() {

    private lateinit var loadingIcon: ProgressBar
    private lateinit var folderListContainer: View
    private lateinit var messageContainer: View
    private lateinit var folderList: RecyclerView

    private lateinit var adapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootFolderName: TextView = findViewById(R.id.textView_root_folder)
        rootFolderName.text = "Root folder name"

        folderList = findViewById(R.id.folders_list)
        folderList.layoutManager = GridLayoutManager(this, 2)

        loadingIcon = findViewById(R.id.folders_progress_bar)
        folderListContainer = findViewById(R.id.folders_linear_layout)
        messageContainer = findViewById(R.id.folders_message_container)

        setViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.set_server_menu_item -> {
                showServerDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showServerDialog() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_set_server_address, null)
        val addressText: EditText = dialogLayout.findViewById(R.id.server_address_edit)

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Set server address")
            .setView(dialogLayout)
            .setPositiveButton(R.string.ok) { _, _ ->
                Toast.makeText(this, "Address set ${addressText.text}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->}

        builder.create().show()

    }

    private fun setViewModel() {
        val factory = ViewModelFactory(DefaultSettingsRepository(this), DefaultFilesRepository())
        val viewModel = ViewModelProvider(this, factory).get(FoldersViewModel::class.java).apply {

            dataLoading.observe(this@MainActivity, Observer {
                loadingIcon.visibility = if(it) View.VISIBLE else View.GONE
            })

            hasUrl.observe(this@MainActivity, Observer {
                folderListContainer.visibility = if(it) View.VISIBLE else View.GONE
                messageContainer.visibility = if(it) View.GONE else View.VISIBLE
            })

            folders.observe(this@MainActivity, Observer {
                adapter = FolderAdapter(it)
                folderList.adapter = adapter
            })
            loadFolders()
        }
    }
}
