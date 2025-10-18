package com.example.blerealtimenotifier

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blerealtimenotifier.databinding.ActivityConsentBinding

/**
 * Activity for obtaining user consent before allowing BLE advertising
 */
class ConsentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConsentBinding
    private lateinit var consentManager: ConsentManager
    private lateinit var loggingManager: LoggingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        consentManager = ConsentManager(this)
        loggingManager = LoggingManager(this)
        
        setupClickListeners()
        
        // If consent is already given and valid, go directly to MainActivity
        if (consentManager.hasValidConsent()) {
            navigateToMainActivity()
            return
        }
    }
    
    private fun setupClickListeners() {
        binding.agreeButton.setOnClickListener {
            handleConsentGiven()
        }
        
        binding.declineButton.setOnClickListener {
            handleConsentDeclined()
        }
    }
    
    private fun handleConsentGiven() {
        consentManager.giveConsent()
        loggingManager.logConsentGiven()
        
        Toast.makeText(
            this,
            "Consent recorded. Please use this app responsibly.",
            Toast.LENGTH_LONG
        ).show()
        
        navigateToMainActivity()
    }
    
    private fun handleConsentDeclined() {
        consentManager.revokeConsent()
        loggingManager.logConsentDenied()
        
        Toast.makeText(
            this,
            getString(R.string.consent_required),
            Toast.LENGTH_LONG
        ).show()
        
        finish()
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        // Prevent back navigation without consent
        handleConsentDeclined()
    }
}
