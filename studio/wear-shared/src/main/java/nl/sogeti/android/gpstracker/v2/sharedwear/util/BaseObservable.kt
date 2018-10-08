package nl.sogeti.android.gpstracker.v2.sharedwear.util

import androidx.databinding.BaseObservable
import androidx.databinding.Observable

fun BaseObservable.observe(changed: (sender: Observable) -> Unit) =
        this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                changed(sender)
            }
        })
