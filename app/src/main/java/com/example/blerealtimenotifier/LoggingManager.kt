package com.example.blerealtimenotifier

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages logging of advertising events for the app
 * Stores logs locally without collecting any personal data
 */
class LoggingManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "ble_notifier_logs"
        private const val LOGS_KEY = "advertising_logs"
        private const val MAX_LOGS = 100 // Limit number of stored logs
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Log an advertising start event
     */
    fun logAdvertisingStarted(deviceName: String) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.ADVERTISING_STARTED,
            deviceName = deviceName,
            details = "Started advertising as '$deviceName'"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Log an advertising stop event
     */
    fun logAdvertisingStopped(deviceName: String) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.ADVERTISING_STOPPED,
            deviceName = deviceName,
            details = "Stopped advertising as '$deviceName'"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Log an advertising error event
     */
    fun logAdvertisingError(error: String) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.ADVERTISING_ERROR,
            deviceName = "",
            details = "Advertising error: $error"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Log app startup
     */
    fun logAppStarted() {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.APP_STARTED,
            deviceName = "",
            details = "App started"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Log consent given
     */
    fun logConsentGiven() {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.CONSENT_GIVEN,
            deviceName = "",
            details = "User consent granted for BLE advertising"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Log consent denied
     */
    fun logConsentDenied() {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = LogEvent.CONSENT_DENIED,
            deviceName = "",
            details = "User consent denied for BLE advertising"
        )
        addLogEntry(logEntry)
    }
    
    /**
     * Get all log entries
     */
    fun getAllLogs(): List<LogEntry> {
        val logsJson = sharedPrefs.getString(LOGS_KEY, "[]")
        val type = object : TypeToken<List<LogEntry>>() {}.type
        return try {
            gson.fromJson(logsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get formatted log entries for display
     */
    fun getFormattedLogs(): List<String> {
        return getAllLogs().map { entry ->
            val timestamp = dateFormat.format(Date(entry.timestamp))
            val eventIcon = when (entry.event) {
                LogEvent.ADVERTISING_STARTED -> "▶️"
                LogEvent.ADVERTISING_STOPPED -> "⏹️"
                LogEvent.ADVERTISING_ERROR -> "❌"
                LogEvent.APP_STARTED -> "🚀"
                LogEvent.CONSENT_GIVEN -> "✅"
                LogEvent.CONSENT_DENIED -> "❌"
            }
            "$eventIcon $timestamp - ${entry.details}"
        }
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        sharedPrefs.edit().remove(LOGS_KEY).apply()
    }
    
    /**
     * Get log statistics
     */
    fun getLogStatistics(): LogStatistics {
        val logs = getAllLogs()
        val advertisingStartedCount = logs.count { it.event == LogEvent.ADVERTISING_STARTED }
        val advertisingStoppedCount = logs.count { it.event == LogEvent.ADVERTISING_STOPPED }
        val errorCount = logs.count { it.event == LogEvent.ADVERTISING_ERROR }
        val totalSessions = advertisingStartedCount
        
        return LogStatistics(
            totalLogs = logs.size,
            advertisingSessions = totalSessions,
            errors = errorCount,
            lastActivity = logs.maxByOrNull { it.timestamp }?.timestamp
        )
    }
    
    /**
     * Add a log entry to storage
     */
    private fun addLogEntry(entry: LogEntry) {
        val currentLogs = getAllLogs().toMutableList()
        currentLogs.add(entry)
        
        // Keep only the most recent logs
        if (currentLogs.size > MAX_LOGS) {
            currentLogs.sortBy { it.timestamp }
            while (currentLogs.size > MAX_LOGS) {
                currentLogs.removeAt(0)
            }
        }
        
        val logsJson = gson.toJson(currentLogs)
        sharedPrefs.edit().putString(LOGS_KEY, logsJson).apply()
    }
}

/**
 * Represents a log entry
 */
data class LogEntry(
    val timestamp: Long,
    val event: LogEvent,
    val deviceName: String,
    val details: String
)

/**
 * Types of events that can be logged
 */
enum class LogEvent {
    ADVERTISING_STARTED,
    ADVERTISING_STOPPED,
    ADVERTISING_ERROR,
    APP_STARTED,
    CONSENT_GIVEN,
    CONSENT_DENIED
}

/**
 * Statistics about logged events
 */
data class LogStatistics(
    val totalLogs: Int,
    val advertisingSessions: Int,
    val errors: Int,
    val lastActivity: Long?
)
