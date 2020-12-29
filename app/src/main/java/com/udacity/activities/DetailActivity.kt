package com.udacity.activities

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.R
import com.udacity.util.DownloadJobSummary
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // initialize the notification manager
        notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        ) as NotificationManager

        // close the notification when we are taken to the detail activity
        // since we deal only with a single NotificationID, we can save ourselves some trouble
        // and simply cancelAll() instead of passing notification ID via bundle since it is always fixed
        notificationManager.cancelAll()

        // get the bundled DownloadJobSummary object, containing the DownloadManager's summary
        intent.getParcelableExtra<DownloadJobSummary>(getString(R.string.download_job_summary_extra))?.let { summary ->
            when (summary.outcome){
                true  -> {
                    textview_status_value.text = getString(R.string.success)
                    textview_status_value.setTextColor(getColor(R.color.colorSuccess))
                }
                false -> {
                    textview_status_value.text = getString(R.string.fail)
                    textview_status_value.setTextColor(getColor(R.color.colorFail))
                }
            }

            textview_filename_value.text = summary.description
        }

        /**
         * Pressing the "OK" button will act same as pressing "back"
         */
        button_ok.setOnClickListener {
            this.onBackPressed()
        }

    }

    override fun onBackPressed() {
        with (Intent(this, MainActivity::class.java)){
            // don't show MainActivity twice if already behind the Detail Activity
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
        }
        super.onBackPressed()
    }
}
