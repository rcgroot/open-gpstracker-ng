package nl.sogeti.android.gpstracker.v2.sharedwear

import android.os.Parcelable
import com.google.android.gms.wearable.DataMap
import kotlinx.android.parcel.Parcelize

const val PATH_STATISTICS = "/ogt-recordings-statistics"

private const val SPEED = "SPEED"
private const val DISTANCE = "DISTANCE"
private const val DURATION = "DURATION"

@Parcelize
data class StatisticsMessage(val speed: String, val distance: String, val duration: String) : WearMessage(PATH_STATISTICS), Parcelable {

    constructor(datamap: DataMap) :
            this(datamap.getString(SPEED), datamap.getString(DISTANCE), datamap.getString(DURATION))

    override fun toDataMap(): DataMap {
        val dataMap = DataMap()
        dataMap.putString(SPEED, speed)
        dataMap.putString(DISTANCE, distance)
        dataMap.putString(DURATION, duration)
        return dataMap
    }
}
