package org.berenguel.carheadunitconfigurer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        val settingsFragment = SettingsFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, settingsFragment)
            .commit();
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}