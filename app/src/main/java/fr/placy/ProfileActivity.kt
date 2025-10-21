package fr.placy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private val fragments: Map<Int, Fragment> = mapOf(
        R.id.navigation_home to HomeFragment(),
        R.id.navigation_dashboard to DashboardFragment(),
        R.id.navigation_profile to ProfileFragment()
    )

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val fragment = fragments[item.itemId]
            if (fragment != null) {
                // If the selected fragment is not the currently displayed one, replace it
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment?.javaClass != fragment.javaClass) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
                return@OnNavigationItemSelectedListener true
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this points to the layout with the BottomNavigationView

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Set the default fragment to display when the activity is first created
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.navigation_profile // Set Profile as default
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragments[R.id.navigation_profile]!!)
                .commit()
        }
    }
}