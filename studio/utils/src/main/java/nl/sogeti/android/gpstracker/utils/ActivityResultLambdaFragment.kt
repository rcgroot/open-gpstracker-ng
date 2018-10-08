package nl.sogeti.android.gpstracker.utils

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import android.util.SparseArray
import timber.log.Timber

/**
 * Default implementation for ActivityResultLambda on a V4 Fragment super class
 */
abstract class ActivityResultLambdaFragment : Fragment(), ActivityResultLambda {

    //region Activity results

    private var requests = 1
    private val resultHandlers = SparseArray<(Intent?) -> Unit>()

    override fun startActivityForResult(intent: Intent, resultHandler: (Intent?) -> Unit) {
        val requestCode = ++requests
        resultHandlers.put(requestCode, resultHandler)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        val resultHandler = resultHandlers.get(requestCode)
        resultHandlers.remove(requestCode)
        if (resultCode == Activity.RESULT_OK && resultHandler != null) {
            resultHandler(result)
        } else {
            Timber.e("Received $result without an way to handle")
            super.onActivityResult(requestCode, resultCode, result)
        }
    }

    //endregion
}
