package nl.sogeti.android.gpstracker.ng.utils

import android.content.Intent

interface ActivityResultLambda {

    /**
     * Implementing Activity or Fragment should call the resultHandler when the called
     * Activity returns an RESULT_OK with its Intent value
     */
    fun startActivityForResult(intent: Intent, resultHandler: (Intent?) -> Unit)
}
