package nl.sogeti.android.gpstracker.ng.gpximport

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.JobIntentService
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.v2.R
import timber.log.Timber
import javax.inject.Inject

const val EXTRA_FILE = "GPX_FILE_URI"
const val EXTRA_DIRECTORY = "GPX_DIRECTORY_URI"
const val JOB_ID = R.menu.menu_import_export

class ImportService : JobIntentService() {

    @Inject
    lateinit var importController: GpxImportController

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    companion object {

        fun importFile(context: Context, uri: Uri) {
            val work = Intent()
            work.putExtra(EXTRA_FILE, uri)
            enqueueWork(context, ImportService::class.java, JOB_ID, work)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun importDirectory(context: Context, uri: Uri) {
            val work = Intent()
            work.putExtra(EXTRA_DIRECTORY, uri)
            enqueueWork(context, ImportService::class.java, JOB_ID, work)
        }
    }

    @SuppressLint("NewApi")
    override fun onHandleWork(intent: Intent) {
        when {
            intent.hasExtra(EXTRA_FILE) -> importController.import(this, intent.getParcelableExtra(EXTRA_FILE))
            intent.hasExtra(EXTRA_DIRECTORY) -> importController.importDirectory(this, intent.getParcelableExtra(EXTRA_DIRECTORY))
            else -> Timber.e("Failed to handle import work $intent")
        }
    }
}
