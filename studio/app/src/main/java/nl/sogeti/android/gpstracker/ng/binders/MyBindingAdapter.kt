package nl.sogeti.android.gpstracker.ng.binders

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.widget.ImageView


class MyBindingAdapter {

    @BindingAdapter("bind:bitmap")
    fun setBitmap(view: ImageView, bitmap: Bitmap?) {
        view.setImageBitmap(bitmap)
    }

    @BindingAdapter("bind:srcCompat")
    fun setSrcCompat(view: ImageView, resource: Int?) {
        if (resource != null) {
            view.setImageResource(resource)
        }
    }
}