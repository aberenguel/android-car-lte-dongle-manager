package org.berenguel.carheadunitconfigurer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
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
        Toast.makeText(this, getString(R.string.toast_exception, throwable), Toast.LENGTH_SHORT).show()
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
                    successToast()
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
                    successToast()
                } finally {
                    binding.usbModeswitchBtn.isEnabled = true
                }
            }
        }

        binding.wifiScanBtn.setOnClickListener {
            EthernetWifiManager.startWifiScan(this)
            successToast()
        }

        binding.huaweiSwitchDebugModeBtn.setOnClickListener {
            job = coroutineScope.launch {
                try {
                    binding.huaweiSwitchDebugModeBtn.isEnabled = false
                    HuaweiSwitchDebugModeManager.changeDeviceModeSet(this@MainActivity)
                    successToast()
                } finally {
                    binding.huaweiSwitchDebugModeBtn.isEnabled = true
                }
            }

        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.gridLayout.columnCount =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestLocationPermissionAndroidQ()
        } else {
            requestLocationPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissionAndroidQ() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] != true) {
                    showLocationPermissionDialog()
                }
            }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun requestLocationPermission() {

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    showLocationPermissionDialog()
                }
            }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_required_title)
            .setMessage(R.string.location_permission_required_message)
            .setPositiveButton(R.string.location_permission_required_button) { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
        binding.usbResetBtn.isEnabled = true
        binding.usbModeswitchBtn.isEnabled = true
        binding.huaweiSwitchDebugModeBtn.isEnabled = true
    }

    private fun successToast() {
        Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show()
    }
}