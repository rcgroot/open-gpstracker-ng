package nl.sogeti.android.gpstracker.v2.sharedwear

import android.os.Parcelable
import com.google.android.gms.wearable.DataMap
import kotlinx.android.parcel.Parcelize

const val PATH_STATUS = "/ogt-recordings-status"

const val STATE_UNKNOWN = -1
const val STATE_START = 1
const val STATE_PAUSE = 2
const val STATE_RESUME = 3
const val STATE_STOP = 4

private const val STATUS = "STATUS"

@Parcelize
data class StatusMessage(val status: Int): WearMessage(PATH_STATUS), Parcelable {
    constructor(datamap: DataMap) : this(datamap.getInt(STATUS))

    override fun toDataMap(): DataMap {
        val dataMap = DataMap()
        dataMap.putInt(STATUS, status)
        return dataMap
    }
}
