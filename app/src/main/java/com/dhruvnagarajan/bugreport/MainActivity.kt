package com.dhruvnagarajan.bugreport

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhruvnagarajan.bugreportsdk.BugManager
import com.dhruvnagarajan.bugreportsdk.BugReport
import com.dhruvnagarajan.bugreportsdk.BuildConfig
import com.dhruvnagarajan.bugreportsdk.ReportInteractionListener

/**
 * @author Dhruvaraj Nagarajan
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bugManager: BugManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bugManager = BugManager.Builder()
            .setWatermark(
                """
                    |OS_VERSION = ${Build.VERSION.SDK_INT}
                    |APP_VERSION = ${BuildConfig.VERSION_NAME}
                    |BUILD_ TYPE = ${BuildConfig.BUILD_TYPE}
                """.trimMargin()
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                captureBugs()
            } else requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else captureBugs()
    }

    private fun captureBugs() {
        bugManager.listen(this, findViewById(android.R.id.content), object : ReportInteractionListener {
            override fun onInteraction(bugReport: BugReport, submit: Boolean) {
                Toast.makeText(
                    this@MainActivity,
                    if (submit) "Submit requested" else "Closed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            var allPermissionsAcquired = true
            for (grantResult in grantResults) {
                allPermissionsAcquired = allPermissionsAcquired && grantResult == PackageManager.PERMISSION_GRANTED
            }
            if (allPermissionsAcquired) captureBugs()
        }
    }

    override fun onDestroy() {
        bugManager.onDestroy()
        super.onDestroy()
    }
}
