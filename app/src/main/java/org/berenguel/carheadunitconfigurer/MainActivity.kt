package org.berenguel.carheadunitconfigurer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.berenguel.carheadunitconfigurer.databinding.ActivityMainBinding
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager
import org.berenguel.carheadunitconfigurer.managers.HuaweiSwitchDebugModeManager
import org.berenguel.carheadunitconfigurer.managers.UsbModeSwitchManager
import org.berenguel.carheadunitconfigurer.managers.UsbResetManager

class MainActivity : AppCompatActivity() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Toast.makeText(this, "Exception: $throwable", Toast.LENGTH_SHORT).show()
        Log.e("MainActivity", "Exception", throwable)
    }
    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + coroutineExceptionHandler)

    private var job: Job? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usbResetBtn.setOnClickListener {
            job = coroutineScope.launch {
                try {
                    binding.usbResetBtn.isEnabled = false
                    UsbResetManager.execute()
                } finally {
                    binding.usbResetBtn.isEnabled = true
                }
            }
        }

        binding.usbModeswitchBtn.setOnClickListener {
            job = coroutineScope.launch {
                try {
                    binding.usbModeswitchBtn.isEnabled = false
                    UsbModeSwitchManager.execute(this@MainActivity)
                } finally {
                    binding.usbModeswitchBtn.isEnabled = true
                }
            }
        }

        binding.checkWifiEthernetBtn.setOnClickListener {
            EthernetWifiManager.startWifiScan(this)
        }

        binding.huaweiSwitchDebugModeBtn.setOnClickListener {
            HuaweiSwitchDebugModeManager.changeDeviceModeSet(this)
        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }


        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // ok.. just continue
                } else {
                    Toast.makeText(
                        this,
                        R.string.location_permission_required_alert_txt,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )) {
                PackageManager.PERMISSION_GRANTED -> {
                    // ok.. just continue
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
            }
        } else {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )) {
                PackageManager.PERMISSION_GRANTED -> {
                    // ok.. just continue
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
        binding.usbResetBtn.isEnabled = true
        binding.usbModeswitchBtn.isEnabled = true
    }
}