package com.dhruvnagarajan.bugreport

import android.graphics.Bitmap
import android.os.Build

/**
 * @author Dhruvaraj Nagarajan
 */
data class BugReport(
    var screenshot: Bitmap,
    var appVersion: Int,
    var buildType: String,
    var buildFlavor: String,
    var osVersion: Int
) {
    constructor(screenshot: Bitmap) : this(
        screenshot,
        BuildConfig.VERSION_CODE,
        BuildConfig.BUILD_TYPE,
        BuildConfig.FLAVOR,
        Build.VERSION.SDK_INT
    )
}