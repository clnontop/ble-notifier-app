package com.example.blerealtimenotifier

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.util.*

/**
 * Manages BLE advertising functionality for the app.
 * Handles Fast Pair and standard BLE advertising to trigger pairing notifications.
 */
class BleAdvertisingManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BleAdvertisingManager"
        
        // Fast Pair Service UUID (Google Fast Pair)
        private val FAST_PAIR_SERVICE_UUID = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
        
        // Standard BLE Service UUIDs that commonly trigger pairing dialogs
        private val AUDIO_SERVICE_UUID = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB")
        private val HID_SERVICE_UUID = ParcelUuid.fromString("00001812-0000-1000-8000-00805F9B34FB")
        
        // Fast Pair Model IDs for common devices (these are example IDs)
        private const val AIRPODS_PRO_MODEL_ID = 0x2B677D
        private const val AIRPODS_MODEL_ID = 0x2B677E
        private const val GENERIC_HEADPHONES_MODEL_ID = 0x2B677F
    }
    
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private var currentDeviceName: String = ""
    
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "Advertising started successfully")
            isAdvertising = true
            onAdvertisingStateChanged?.invoke(true, currentDeviceName)
        }
        
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertising failed with error code: $errorCode")
            isAdvertising = false
            val errorMessage = getAdvertiseErrorMessage(errorCode)
            onAdvertisingError?.invoke(errorMessage)
            onAdvertisingStateChanged?.invoke(false, "")
        }
    }
    
    var onAdvertisingStateChanged: ((Boolean, String) -> Unit)? = null
    var onAdvertisingError: ((String) -> Unit)? = null
    
    /**
     * Check if BLE advertising is supported on this device
     */
    fun isAdvertisingSupported(): Boolean {
        return bluetoothAdapter?.isMultipleAdvertisementSupported == true
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Start BLE advertising with the specified device name
     */
    fun startAdvertising(deviceName: String) {
        if (!isBluetoothEnabled()) {
            onAdvertisingError?.invoke("Bluetooth is not enabled")
            return
        }
        
        if (!isAdvertisingSupported()) {
            onAdvertisingError?.invoke("BLE advertising is not supported on this device")
            return
        }
        
        if (isAdvertising) {
            stopAdvertising()
        }
        
        currentDeviceName = deviceName
        bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        
        if (bluetoothLeAdvertiser == null) {
            onAdvertisingError?.invoke("Failed to get BLE advertiser")
            return
        }
        
        val settings = createAdvertiseSettings()
        val data = createAdvertiseData(deviceName)
        val scanResponse = createScanResponseData(deviceName)
        
        try {
            bluetoothLeAdvertiser?.startAdvertising(settings, data, scanResponse, advertiseCallback)
            Log.d(TAG, "Started advertising as: $deviceName")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when starting advertising", e)
            onAdvertisingError?.invoke("Permission denied for BLE advertising")
        } catch (e: Exception) {
            Log.e(TAG, "Exception when starting advertising", e)
            onAdvertisingError?.invoke("Failed to start advertising: ${e.message}")
        }
    }
    
    /**
     * Stop BLE advertising
     */
    fun stopAdvertising() {
        try {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
            currentDeviceName = ""
            onAdvertisingStateChanged?.invoke(false, "")
            Log.d(TAG, "Advertising stopped")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when stopping advertising", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception when stopping advertising", e)
        }
    }
    
    /**
     * Get current advertising state
     */
    fun isCurrentlyAdvertising(): Boolean = isAdvertising
    
    /**
     * Get current device name being advertised
     */
    fun getCurrentDeviceName(): String = currentDeviceName
    
    /**
     * Create advertising settings optimized for visibility
     */
    private fun createAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .setTimeout(0) // Advertise indefinitely
            .build()
    }
    
    /**
     * Create advertising data with Fast Pair information
     */
    private fun createAdvertiseData(deviceName: String): AdvertiseData {
        val builder = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
        
        // Add Fast Pair service data to trigger pairing notifications
        val fastPairData = createFastPairServiceData(deviceName)
        if (fastPairData.isNotEmpty()) {
            builder.addServiceData(FAST_PAIR_SERVICE_UUID, fastPairData)
        }
        
        // Add service UUIDs that commonly trigger pairing dialogs
        builder.addServiceUuid(FAST_PAIR_SERVICE_UUID)
        builder.addServiceUuid(AUDIO_SERVICE_UUID)
        
        return builder.build()
    }
    
    /**
     * Create scan response data with additional device information
     */
    private fun createScanResponseData(deviceName: String): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(HID_SERVICE_UUID)
            .build()
    }
    
    /**
     * Create Fast Pair service data based on device name
     */
    private fun createFastPairServiceData(deviceName: String): ByteArray {
        val modelId = when {
            deviceName.contains("AirPods Pro", ignoreCase = true) -> AIRPODS_PRO_MODEL_ID
            deviceName.contains("AirPods", ignoreCase = true) -> AIRPODS_MODEL_ID
            else -> GENERIC_HEADPHONES_MODEL_ID
        }
        
        // Create Fast Pair advertisement data
        // Format: [Model ID (3 bytes)] + [Additional data if needed]
        val buffer = ByteBuffer.allocate(3)
        buffer.put((modelId shr 16).toByte())
        buffer.put((modelId shr 8).toByte())
        buffer.put(modelId.toByte())
        
        return buffer.array()
    }
    
    /**
     * Convert advertising error codes to human-readable messages
     */
    private fun getAdvertiseErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> 
                "Advertising already started"
            AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> 
                "Advertising data too large"
            AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> 
                "BLE advertising not supported on this device"
            AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> 
                "Internal advertising error"
            AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> 
                "Too many advertisers running"
            else -> "Unknown advertising error (code: $errorCode)"
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopAdvertising()
        bluetoothLeAdvertiser = null
    }
}
