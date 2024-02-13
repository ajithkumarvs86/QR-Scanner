package com.ak.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ak.qrscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        when (it) {
            true -> {
                launchScanner()
            }
            false -> {
                showPermissionDialog("Camera")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.crdScanner.setOnClickListener {
            checkCameraPermission()
        }

    }

    private fun checkCameraPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(Manifest.permission.CAMERA)
        } else {
            launchScanner()
        }
    }

    private fun launchScanner() {
        startActivity(Intent(this,CameraActivity::class.java))
    }

    private fun showPermissionDialog(permissionType:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission required")
        builder.setMessage("$permissionType permissions are needed to be allowed to use this app without any problems.")
        builder.setPositiveButton("Grant") { dialog, _ ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package",packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}