package com.udacity.activities

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.udacity.R
import com.udacity.customviews.ButtonState
import com.udacity.customviews.RadioButtonExt
import com.udacity.sendNotification
import com.udacity.util.DownloadJobSummary
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // initialize the notification manager
        notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        ) as NotificationManager

        custom_button.setOnClickListener {
            // Not Loading, check for network availability and start to Load!
            if (custom_button.buttonState != ButtonState.Loading) {
                // if network available...
                if (isNetworkAvailable(this)){
                    // remove possible old download notifications which are not cleared
                    notificationManager.cancelAll()

                    // if a radio button is checked, download the equivalent repo
                    // otherwise show a fail toast message
                    // (note: the download() method will change the button state to "loading" when called so the button starts animating)
                    when {
                        radio_glide.isChecked    -> download(radio_glide.sourceUrl, radio_glide.targetFilename, radio_glide.text.toString())
                        radio_app.isChecked      -> download(radio_app.sourceUrl, radio_app.targetFilename, radio_app.text.toString())
                        radio_retrofit.isChecked -> download(radio_retrofit.sourceUrl, radio_retrofit.targetFilename, radio_retrofit.text.toString())
                        else  -> Toast.makeText(this, getString(R.string.no_option_selected), Toast.LENGTH_SHORT).show()
                    }
                }
                // no network available - display "no network error" toast
                else{
                    uncheckRadioButtons()
                    Toast.makeText(this, getString(R.string.no_network_available), Toast.LENGTH_SHORT).show()
                }
            }

            // Button Status is currently "Loading", so by pressing again cancels the ongoing download
            else{
                // cancel the download
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.remove(downloadID)
                downloadID = -1

                uncheckRadioButtons()

                // reset the button to its initial state (Completed)
                custom_button.buttonState = ButtonState.Completed

                // display toast message that the download was canceled by the user
                Toast.makeText(this, getString(R.string.download_canceled_by_user), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Receiver registered for ACTION_DOWNLOAD_COMPLETE intent filter (Download Manager observer)
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // if the completed download is the one we initiated...
            if (id == downloadID) {
                uncheckRadioButtons()

                // Reset the button to its default (non-animated) state
                custom_button.buttonState = ButtonState.Completed

                // check if the download was success or fail
                val downloadJobSummary =  getDownloadJobSummary(downloadID)

                notificationManager.sendNotification(downloadJobSummary, applicationContext)
            }
        }
    }

    /**
     * This method unchecks all the RadioButtons of the RadioGroup
     * Usually called when a download is complete as a nice ui addition
     */
    fun uncheckRadioButtons(){
        for (i in 0 until radioGroup.childCount){
            (radioGroup[i] as RadioButtonExt).isChecked = false
        }
    }

    /**
     * Check if download status and store result in data object
     *
     * Checking DownloadManager response status - Source:
     * https://stackoverflow.com/questions/8937817/downloadmanager-action-download-complete-broadcast-receiver-receiving-same-downl
     */
    fun getDownloadJobSummary(downloadID: Long): DownloadJobSummary {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val c: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

        var outcome = false
        var title = ""
        var description = ""

        if (c.moveToFirst()) {
            title       = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
            description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))

            val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                outcome = true //Download is valid, celebrate
            }
        }

        return DownloadJobSummary(downloadID, title, description, outcome)
    }

    /**
     * Check if network is available or not
     * Useful as we need to verify that network connection is available to initiate a download request
     *
     * Source:
     * https://stackoverflow.com/questions/57277759/getactivenetworkinfo-is-deprecated-in-api-29
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw      = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }


    /**
     * Since Android 10 (API 29) the new "Scoped Storage" limits access to the filesystem and we need
     * some extra steps for the DownloadManager to fetch our files
     *
     * Source:
     * https://knowledge.udacity.com/questions/392679
     *
     * Scoped Storage:
     * https://www.youtube.com/watch?v=UnJ3amzJM94
     */
    private fun download(url: String, filename: String, description: String) {
        // since we initiate download, change the button state to "loading" so it begins animating
        custom_button.buttonState = ButtonState.Loading

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription(description)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                    // following line required at api 29 and on
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/$filename")

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }
}
