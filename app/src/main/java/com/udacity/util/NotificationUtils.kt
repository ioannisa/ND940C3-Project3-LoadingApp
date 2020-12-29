package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.activities.DetailActivity
import com.udacity.util.DownloadJobSummary


// Notification ID.
private const val NOTIFICATION_ID = 0

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bitmap
}

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(downloadJobSummary: DownloadJobSummary, applicationContext: Context) {

    val messageBody =
            if (downloadJobSummary.outcome) { applicationContext.getString(R.string.successful_download_message) }
            else                            { applicationContext.getString(R.string.failed_download_message) }

    val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.cloud_download_18dp)
    val cloudImage = drawable?.let { drawableToBitmap(it) } // convert svg drawable to bitmap

    // Action: DETAILS - Takes you to the Details Activity
    val detailIntent = Intent(applicationContext, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(applicationContext.getString(R.string.download_job_summary_extra), downloadJobSummary)
    }
    val detailPendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)



    // Action: DOWNLOADS - Takes you to the downloads folder
    val downloadsIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
    val downloadsPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, downloadsIntent, 0)



    // Build the notification
    val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.download_channel_id))

    notificationBuilder
            .setSmallIcon(R.drawable.cloud_download_18dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)

            .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(cloudImage))

            // we have no content intent as we will only interact with notification actions
            //.setContentIntent(contentPendingIntent)

            .setAutoCancel(true)

            // set the action for opening the DetailActivity
            .addAction(
                    0,
                    applicationContext.getString(R.string.notification_action_details_description),
                    detailPendingIntent
            )

            // outside the project requirements, opening the downloads folder in another action would
            // add to the overall functionality and looks of the project
            .addAction(
                    0,
                    applicationContext.getString(R.string.notification_action_downloads_description),
                    downloadsPendingIntent
            )

            .priority = NotificationCompat.PRIORITY_HIGH


    notify(NOTIFICATION_ID, notificationBuilder.build())
}