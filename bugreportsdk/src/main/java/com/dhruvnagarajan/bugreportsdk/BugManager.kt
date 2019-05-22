package com.dhruvnagarajan.bugreportsdk

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_bug_report.view.*

/**
 * @author Dhruvaraj Nagarajan
 */
class BugManager private constructor(private val watermark: String?) {
    private lateinit var context: Context
    private lateinit var reportInteractionListener: ReportInteractionListener
    private lateinit var rootView: ViewGroup
    private var view: View? = null
    private lateinit var contentObserver: ContentObserver

    /**
     * Requires READ_EXTERNAL_STORAGE permission.
     */
    fun listen(context: Context, rootView: ViewGroup, reportInteractionListener: ReportInteractionListener) {
        this.context = context
        this.rootView = rootView
        this.reportInteractionListener = reportInteractionListener

        contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri != null) {
                    val screenshotUri = validateMediaUri(context, uri)
                    renderScreenshot(screenshotUri)
                }
                super.onChange(selfChange, uri)
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true, contentObserver
        )
    }

    fun onDestroy() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }

    private fun validateMediaUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        var filePath: String? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return filePath
    }

    private fun renderScreenshot(filePath: String?) {
        filePath ?: return
        var bitmap = BitmapFactory.decodeFile(filePath)
        watermark?.let { bitmap = putWatermark(bitmap) }
        showView()
        view?.iv_screenshot?.setImageBitmap(bitmap)
        val bugReport = BugReport(bitmap)
        view?.b_submit_issue?.setOnClickListener {
            reportInteractionListener.onInteraction(bugReport, true)
            ridView()
        }
        view?.iv_close?.setOnClickListener {
            reportInteractionListener.onInteraction(bugReport, false)
            ridView()
        }
    }

    private fun putWatermark(screenshot: Bitmap?): Bitmap? {
        screenshot ?: return null
        watermark ?: return null

        val paint = Paint()
        paint.color = Color.parseColor("#000000")
        paint.alpha = 200
        paint.isAntiAlias = true
        paint.textSize = 40f

        val bitmap = Bitmap.createBitmap(screenshot.width, screenshot.height, screenshot.config)

        val canvas = Canvas(bitmap)
        canvas.drawBitmap(screenshot, 0f, 0f, null)

        var y = screenshot.height - 200f
        for (str in watermark.split('\n')) {
            canvas.drawText(str, 10f, y, paint)
            y += 50f
        }

        return bitmap
    }

    private fun showView() {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_bug_report, rootView, true)
            return
        }
        view?.iv_close?.visibility = View.VISIBLE
        view?.iv_screenshot?.visibility = View.VISIBLE
        view?.b_submit_issue?.visibility = View.VISIBLE
    }

    private fun ridView() {
        view?.iv_close?.visibility = View.GONE
        view?.iv_screenshot?.visibility = View.GONE
        view?.b_submit_issue?.visibility = View.GONE
    }

    class Builder {

        private var watermark: String? = null

        /**
         * put a watermark at the bottom of the screenshot of bug with:
         * os version,
         * app build version,
         * app flavor
         */
        fun setWatermark(watermark: String): Builder {
            this.watermark = watermark
            return this
        }

        fun build(): BugManager {
            return BugManager(watermark)
        }
    }
}

interface ReportInteractionListener {

    fun onInteraction(bugReport: BugReport, submit: Boolean)
}