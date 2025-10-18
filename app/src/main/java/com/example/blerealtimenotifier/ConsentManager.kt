package com.example.blerealtimenotifier

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user consent for BLE advertising functionality
 */
class ConsentManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "ble_notifier_consent"
        private const val CONSENT_GIVEN_KEY = "consent_given"
        private const val CONSENT_TIMESTAMP_KEY = "consent_timestamp"
        private const val CONSENT_VERSION_KEY = "consent_version"
        
        // Current consent version - increment when consent terms change
        private const val CURRENT_CONSENT_VERSION = 1
        
        // Consent expires after 30 days (in milliseconds)
        private const val CONSENT_EXPIRY_DURATION = 30L * 24L * 60L * 60L * 1000L
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if user has given valid consent
     */
    fun hasValidConsent(): Boolean {
        val consentGiven = sharedPrefs.getBoolean(CONSENT_GIVEN_KEY, false)
        val consentTimestamp = sharedPrefs.getLong(CONSENT_TIMESTAMP_KEY, 0L)
        val consentVersion = sharedPrefs.getInt(CONSENT_VERSION_KEY, 0)
        
        if (!consentGiven) return false
        if (consentVersion < CURRENT_CONSENT_VERSION) return false
        
        // Check if consent has expired
        val currentTime = System.currentTimeMillis()
        if (currentTime - consentTimestamp > CONSENT_EXPIRY_DURATION) {
            return false
        }
        
        return true
    }
    
    /**
     * Record user consent
     */
    fun giveConsent() {
        sharedPrefs.edit()
            .putBoolean(CONSENT_GIVEN_KEY, true)
            .putLong(CONSENT_TIMESTAMP_KEY, System.currentTimeMillis())
            .putInt(CONSENT_VERSION_KEY, CURRENT_CONSENT_VERSION)
            .apply()
    }
    
    /**
     * Revoke user consent
     */
    fun revokeConsent() {
        sharedPrefs.edit()
            .putBoolean(CONSENT_GIVEN_KEY, false)
            .remove(CONSENT_TIMESTAMP_KEY)
            .remove(CONSENT_VERSION_KEY)
            .apply()
    }
    
    /**
     * Get consent timestamp
     */
    fun getConsentTimestamp(): Long {
        return sharedPrefs.getLong(CONSENT_TIMESTAMP_KEY, 0L)
    }
    
    /**
     * Get days remaining until consent expires
     */
    fun getDaysUntilConsentExpires(): Int {
        if (!hasValidConsent()) return 0
        
        val consentTimestamp = getConsentTimestamp()
        val currentTime = System.currentTimeMillis()
        val timeSinceConsent = currentTime - consentTimestamp
        val timeUntilExpiry = CONSENT_EXPIRY_DURATION - timeSinceConsent
        
        return if (timeUntilExpiry > 0) {
            (timeUntilExpiry / (24L * 60L * 60L * 1000L)).toInt()
        } else {
            0
        }
    }
    
    /**
     * Check if consent needs renewal (within 7 days of expiry)
     */
    fun needsConsentRenewal(): Boolean {
        return hasValidConsent() && getDaysUntilConsentExpires() <= 7
    }
    
    /**
     * Get consent status information
     */
    fun getConsentStatus(): ConsentStatus {
        return ConsentStatus(
            hasConsent = hasValidConsent(),
            consentTimestamp = getConsentTimestamp(),
            daysUntilExpiry = getDaysUntilConsentExpires(),
            needsRenewal = needsConsentRenewal(),
            consentVersion = sharedPrefs.getInt(CONSENT_VERSION_KEY, 0)
        )
    }
    
    /**
     * Clear all consent data (for testing or reset)
     */
    fun clearConsentData() {
        sharedPrefs.edit().clear().apply()
    }
}

/**
 * Represents the current consent status
 */
data class ConsentStatus(
    val hasConsent: Boolean,
    val consentTimestamp: Long,
    val daysUntilExpiry: Int,
    val needsRenewal: Boolean,
    val consentVersion: Int
)
