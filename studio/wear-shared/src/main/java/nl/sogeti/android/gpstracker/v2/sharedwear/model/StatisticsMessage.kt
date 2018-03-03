package nl.sogeti.android.gpstracker.v2.sharedwear.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.android.gms.wearable.DataMap
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class StatisticsMessage(val speed: Float, val distance: Float, val duration: Long) : WearMessage(PATH_STATISTICS), Parcelable {

    constructor(dataMap: DataMap) :
            this(dataMap.getFloat(SPEED), dataMap.getFloat(DISTANCE), dataMap.getLong(DURATION))

    override fun toDataMap(): DataMap {
        val dataMap = DataMap()
        dataMap.putFloat(SPEED, speed)
        dataMap.putFloat(DISTANCE, distance)
        dataMap.putLong(DURATION, duration)
        return dataMap
    }

    companion object {
        const val PATH_STATISTICS = "/ogt-recordings-statistics"

        private const val SPEED = "SPEED"
        private const val DISTANCE = "DISTANCE"
        private const val DURATION = "DURATION"
    }
}
