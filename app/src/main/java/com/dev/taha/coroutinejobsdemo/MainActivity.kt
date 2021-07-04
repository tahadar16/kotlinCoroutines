package com.dev.taha.coroutinejobsdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


class MainActivity : AppCompatActivity() {
    private val PROGRESS_MAX = 1000
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000L
    private lateinit var job : CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {
            if (!::job.isInitialized)
                initJob()
            job_progress_bar.startJobOrCancel(job)
        }
    }

    private fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            resetJob()
        } else {
            job_button.text = "Cancel Job"
            CoroutineScope(IO + job).launch {
                for (i in PROGRESS_START.. PROGRESS_MAX) {
                    delay(JOB_TIME/PROGRESS_MAX)
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteText("Job is completed")
            }
        }
    }

    private fun updateJobCompleteText(text : String) {
        CoroutineScope(Main).launch {
            job_complete_text.text = text
        }
    }

    private fun resetJob() {
        if (job.isActive || job.complete())
            job.cancel(CancellationException("Resetting Job"))
        initJob()
    }

    private fun initJob() {
        job_button.text = "Start Job#1"
        updateJobCompleteText("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank())
                    msg = "$job cancelled due to unknown error"
                showToast(msg)
            }
        }
        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    private fun showToast(msg: String) {
        CoroutineScope(Main).launch {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }
}