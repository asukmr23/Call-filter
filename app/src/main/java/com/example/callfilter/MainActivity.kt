package com.example.callfilter

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var roleButton: Button
    private lateinit var permissionButton: Button

    private val roleRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshStatus()
        }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannel(this)

        statusText = findViewById(R.id.statusText)
        roleButton = findViewById(R.id.roleButton)
        permissionButton = findViewById(R.id.permissionButton)

        permissionButton.setOnClickListener { requestNeededPermissions() }
        roleButton.setOnClickListener { requestCallScreeningRole() }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun requiredPermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return perms.toTypedArray()
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestNeededPermissions() {
        permissionRequestLauncher.launch(requiredPermissions())
    }

    private fun isCallScreeningRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            statusText.text = "This feature needs Android 10 or higher."
            return
        }
        val roleManager = getSystemService(RoleManager::class.java) ?: return
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            roleRequestLauncher.launch(intent)
        } else {
            statusText.text =
                "Your device doesn't expose the Call Screening role directly.\n" +
                "Go to Settings > Apps > Default apps > Caller ID & spam apps and pick this app manually."
        }
    }

    private fun refreshStatus() {
        val permsOk = hasAllPermissions()
        val roleOk = isCallScreeningRoleHeld()

        permissionButton.isEnabled = !permsOk
        roleButton.isEnabled = permsOk // ask for the role only after permissions are granted

        statusText.text = buildString {
            append(if (permsOk) "✅ Permissions granted\n" else "❌ Permissions needed\n")
            append(
                if (roleOk) "✅ Set as the Call Screening app — unknown calls will be silenced.\n"
                else "❌ Not yet set as the Call Screening app — calls will ring normally until you set it.\n"
            )
            if (permsOk && roleOk) {
                append("\nAll set. Calls from numbers not in your Contacts will no longer ring; " +
                        "you'll get a quiet notification instead.")
            }
        }
    }
}
