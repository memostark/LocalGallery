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

    private lateinit var viewModel: FoldersViewModel

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
                viewModel.loadDialogData()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showServerDialog(presetData: String) {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_set_server_address, null)
        val addressText: EditText = dialogLayout.findViewById(R.id.server_address_edit)
        addressText.setText(presetData)
        addressText.setSelection(presetData.length)

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Set server address")
            .setView(dialogLayout)
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.updateUrl(addressText.text.toString())
            }
            .setNegativeButton(R.string.cancel) { _, _ ->}

        builder.create().show()

    }

    private fun setViewModel() {
        val factory = ViewModelFactory(DefaultSettingsRepository(this), DefaultFilesRepository())
        viewModel = ViewModelProvider(this, factory).get(FoldersViewModel::class.java).apply {

            dataLoading.observe(this@MainActivity, Observer {
                loadingIcon.visibility = if(it) View.VISIBLE else View.GONE
            })

            hasUrl.observe(this@MainActivity, Observer {
                folderListContainer.visibility = if(it) View.VISIBLE else View.GONE
                messageContainer.visibility = if(it) View.GONE else View.VISIBLE
            })

            openDialog.observe(this@MainActivity, Observer {
                showServerDialog(it)
            })

            folders.observe(this@MainActivity, Observer {
                adapter = FolderAdapter(it)
                folderList.adapter = adapter
            })
            loadFolders()
        }
    }
}
