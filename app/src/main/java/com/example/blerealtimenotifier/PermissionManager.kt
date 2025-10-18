package com.example.blerealtimenotifier

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Manages Bluetooth and location permissions required for BLE advertising
 */
class PermissionManager(private val context: Context) {
    
    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
        
        // Permissions required for different Android versions
        private val BLUETOOTH_PERMISSIONS_LEGACY = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        private val BLUETOOTH_PERMISSIONS_NEW = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    /**
     * Get the required permissions based on Android version
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BLUETOOTH_PERMISSIONS_NEW
        } else {
            BLUETOOTH_PERMISSIONS_LEGACY
        }
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(): List<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request missing permissions
     */
    fun requestPermissions(activity: Activity) {
        val missingPermissions = getMissingPermissions()
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Check if we should show rationale for any permission
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return getMissingPermissions().any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
    
    /**
     * Handle permission request results
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): PermissionResult {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return PermissionResult.UNKNOWN
        }
        
        val deniedPermissions = mutableListOf<String>()
        val permanentlyDeniedPermissions = mutableListOf<String>()
        
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i])
            }
        }
        
        return when {
            deniedPermissions.isEmpty() -> PermissionResult.ALL_GRANTED
            deniedPermissions.isNotEmpty() -> PermissionResult.SOME_DENIED
            else -> PermissionResult.UNKNOWN
        }
    }
    
    /**
     * Get user-friendly permission names
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.BLUETOOTH -> "Bluetooth"
            Manifest.permission.BLUETOOTH_ADMIN -> "Bluetooth Admin"
            Manifest.permission.BLUETOOTH_ADVERTISE -> "Bluetooth Advertise"
            Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth Scan"
            Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth Connect"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Location Access"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
            else -> permission.substringAfterLast(".")
        }
    }
    
    /**
     * Get explanation for why each permission is needed
     */
    fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN -> 
                "Required to access Bluetooth functionality"
            Manifest.permission.BLUETOOTH_ADVERTISE -> 
                "Required to broadcast BLE advertisements"
            Manifest.permission.BLUETOOTH_SCAN -> 
                "Required to scan for nearby BLE devices"
            Manifest.permission.BLUETOOTH_CONNECT -> 
                "Required to connect to BLE devices"
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "Required for BLE operations on Android (privacy protection)"
            else -> "Required for app functionality"
        }
    }
}

/**
 * Result of permission request
 */
enum class PermissionResult {
    ALL_GRANTED,
    SOME_DENIED,
    UNKNOWN
}
