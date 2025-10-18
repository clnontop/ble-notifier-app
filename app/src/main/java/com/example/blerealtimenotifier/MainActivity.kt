package com.example.blerealtimenotifier

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.blerealtimenotifier.databinding.ActivityMainBinding

/**
 * Main activity for the BLE Real-Time Notifier app
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var bleAdvertisingManager: BleAdvertisingManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var consentManager: ConsentManager
    private lateinit var loggingManager: LoggingManager
    
    private val bluetoothEnableRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupClickListeners()
        checkInitialRequirements()
        
        loggingManager.logAppStarted()
    }
    
    private fun initializeManagers() {
        bleAdvertisingManager = BleAdvertisingManager(this)
        permissionManager = PermissionManager(this)
        consentManager = ConsentManager(this)
        loggingManager = LoggingManager(this)
        
        // Set up BLE advertising callbacks
        bleAdvertisingManager.onAdvertisingStateChanged = { isAdvertising, deviceName ->
            runOnUiThread {
                updateAdvertisingStatus(isAdvertising, deviceName)
            }
        }
        
        bleAdvertisingManager.onAdvertisingError = { error ->
            runOnUiThread {
                showError(error)
                loggingManager.logAdvertisingError(error)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.startButton.setOnClickListener {
            startAdvertising()
        }
        
        binding.stopButton.setOnClickListener {
            stopAdvertising()
        }
        
        binding.viewLogsButton.setOnClickListener {
            showLogsDialog()
        }
        
        binding.settingsButton.setOnClickListener {
            showSettingsDialog()
        }
        
        // Preset chip listeners
        binding.chipAirPodsPro.setOnClickListener {
            binding.deviceNameInput.setText(getString(R.string.preset_airpods_pro))
        }
        
        binding.chipAirPods.setOnClickListener {
            binding.deviceNameInput.setText(getString(R.string.preset_airpods))
        }
        
        binding.chipCarAudio.setOnClickListener {
            binding.deviceNameInput.setText(getString(R.string.preset_car_audio))
        }
        
        binding.chipHeadphones.setOnClickListener {
            binding.deviceNameInput.setText(getString(R.string.preset_headphones))
        }
    }
    
    private fun checkInitialRequirements() {
        // Check consent first
        if (!consentManager.hasValidConsent()) {
            navigateToConsentActivity()
            return
        }
        
        // Check if BLE is supported
        if (!packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE)) {
            showError(getString(R.string.ble_not_supported))
            return
        }
        
        // Check if advertising is supported
        if (!bleAdvertisingManager.isAdvertisingSupported()) {
            showError(getString(R.string.advertising_not_supported))
            return
        }
        
        // Check permissions
        if (!permissionManager.hasAllPermissions()) {
            requestPermissions()
            return
        }
        
        // Check if Bluetooth is enabled
        if (!bleAdvertisingManager.isBluetoothEnabled()) {
            requestBluetoothEnable()
            return
        }
        
        // All requirements met
        updateUI()
    }
    
    private fun startAdvertising() {
        val deviceName = binding.deviceNameInput.text?.toString()?.trim()
        
        if (deviceName.isNullOrEmpty()) {
            showError(getString(R.string.device_name_empty))
            return
        }
        
        if (!permissionManager.hasAllPermissions()) {
            requestPermissions()
            return
        }
        
        if (!bleAdvertisingManager.isBluetoothEnabled()) {
            requestBluetoothEnable()
            return
        }
        
        bleAdvertisingManager.startAdvertising(deviceName)
        loggingManager.logAdvertisingStarted(deviceName)
    }
    
    private fun stopAdvertising() {
        val currentDeviceName = bleAdvertisingManager.getCurrentDeviceName()
        bleAdvertisingManager.stopAdvertising()
        loggingManager.logAdvertisingStopped(currentDeviceName)
    }
    
    private fun updateAdvertisingStatus(isAdvertising: Boolean, deviceName: String) {
        if (isAdvertising) {
            binding.statusText.text = getString(R.string.status_advertising, deviceName)
            binding.statusIndicator.backgroundTintList = 
                ContextCompat.getColorStateList(this, R.color.status_active)
            binding.startButton.isEnabled = false
            binding.stopButton.isEnabled = true
        } else {
            binding.statusText.text = getString(R.string.status_stopped)
            binding.statusIndicator.backgroundTintList = 
                ContextCompat.getColorStateList(this, R.color.status_inactive)
            binding.startButton.isEnabled = true
            binding.stopButton.isEnabled = false
        }
    }
    
    private fun updateUI() {
        val isAdvertising = bleAdvertisingManager.isCurrentlyAdvertising()
        val deviceName = bleAdvertisingManager.getCurrentDeviceName()
        updateAdvertisingStatus(isAdvertising, deviceName)
    }
    
    private fun requestPermissions() {
        if (permissionManager.shouldShowRationale(this)) {
            showPermissionRationaleDialog()
        } else {
            permissionManager.requestPermissions(this)
        }
    }
    
    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        bluetoothEnableRequest.launch(enableBtIntent)
    }
    
    private fun showPermissionRationaleDialog() {
        val missingPermissions = permissionManager.getMissingPermissions()
        val permissionList = missingPermissions.joinToString("\n") { permission ->
            "• ${permissionManager.getPermissionDisplayName(permission)}: ${permissionManager.getPermissionExplanation(permission)}"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app requires the following permissions to function:\n\n$permissionList")
            .setPositiveButton("Grant Permissions") { _, _ ->
                permissionManager.requestPermissions(this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                showError(getString(R.string.permission_required))
            }
            .show()
    }
    
    private fun showLogsDialog() {
        val logs = loggingManager.getFormattedLogs()
        val statistics = loggingManager.getLogStatistics()
        
        val message = if (logs.isEmpty()) {
            getString(R.string.no_logs)
        } else {
            val statsText = "Sessions: ${statistics.advertisingSessions} | Errors: ${statistics.errors}\n\n"
            statsText + logs.takeLast(20).joinToString("\n")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Activity Logs")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Clear Logs") { _, _ ->
                loggingManager.clearLogs()
                Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showSettingsDialog() {
        val consentStatus = consentManager.getConsentStatus()
        val message = """
            Consent Status: ${if (consentStatus.hasConsent) "Valid" else "Invalid"}
            Days until renewal: ${consentStatus.daysUntilExpiry}
            
            BLE Advertising: ${if (bleAdvertisingManager.isAdvertisingSupported()) "Supported" else "Not Supported"}
            Bluetooth: ${if (bleAdvertisingManager.isBluetoothEnabled()) "Enabled" else "Disabled"}
            Permissions: ${if (permissionManager.hasAllPermissions()) "Granted" else "Missing"}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Settings & Status")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Revoke Consent") { _, _ ->
                showRevokeConsentDialog()
            }
            .show()
    }
    
    private fun showRevokeConsentDialog() {
        AlertDialog.Builder(this)
            .setTitle("Revoke Consent")
            .setMessage("Are you sure you want to revoke consent? This will stop all advertising and require consent again to use the app.")
            .setPositiveButton("Revoke") { _, _ ->
                stopAdvertising()
                consentManager.revokeConsent()
                navigateToConsentActivity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun navigateToConsentActivity() {
        val intent = Intent(this, ConsentActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        val result = permissionManager.handlePermissionResult(requestCode, permissions, grantResults)
        when (result) {
            PermissionResult.ALL_GRANTED -> {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                checkInitialRequirements()
            }
            PermissionResult.SOME_DENIED -> {
                showError(getString(R.string.permission_denied))
            }
            PermissionResult.UNKNOWN -> {
                // Handle unknown result
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
        
        // Check if consent needs renewal
        if (consentManager.needsConsentRenewal()) {
            Toast.makeText(
                this,
                "Your consent will expire in ${consentManager.getDaysUntilConsentExpires()} days. Please renew in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bleAdvertisingManager.cleanup()
    }
}
