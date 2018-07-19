package nl.sogeti.android.gpstracker.v2.sharedwear.util

import android.databinding.BaseObservable
import android.databinding.Observable

fun BaseObservable.observe(changed: (sender: Observable) -> Unit) =
        this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                changed(sender)
            }
        })
