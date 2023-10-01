package com.guillermonegrete.gallery.folders

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guillermonegrete.gallery.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.main_bottom_nav)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.isGone = when (destination.id) {
                R.id.fileDetailsFragment, R.id.addTagFragment -> true
                else -> false
            }

            if(destination.id == R.id.files_fragment_dest)
                bottomNavigationView.menu.findItem(R.id.folders_fragment_dest).isChecked = true
        }
    }
}
