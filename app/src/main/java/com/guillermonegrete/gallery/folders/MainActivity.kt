package com.guillermonegrete.gallery.folders

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.guillermonegrete.gallery.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main){

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.main_bottom_nav)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.isGone = when (destination.id) {
                R.id.file_details_dest, R.id.addTagFragment -> true
                // Keep the bar hidden when navigating to the files list from details
                R.id.files_fragment_dest -> navController.previousBackStackEntry?.destination?.id == R.id.file_details_dest
                else -> false
            }

            if(destination.id == R.id.files_fragment_dest)
                bottomNavigationView.menu.findItem(R.id.folders_fragment_dest).isChecked = true
        }
    }

    fun showSnackBar(message: String) {
        val navBar = findViewById<View>(R.id.main_bottom_nav)
        val snackBar = Snackbar.make(navBar, message, Snackbar.LENGTH_SHORT)
        snackBar.setAnchorView(navBar)
        snackBar.show()
    }
}
