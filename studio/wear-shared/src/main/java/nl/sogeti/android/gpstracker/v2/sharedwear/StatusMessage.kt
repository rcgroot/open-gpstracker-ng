package nl.sogeti.android.gpstracker.v2.sharedwear

import android.os.Parcelable
import com.google.android.gms.wearable.DataMap
import kotlinx.android.parcel.Parcelize

const val PATH_STATUS = "/ogt-recordings-status"

private const val STATUS = "STATUS"

@Parcelize
data class StatusMessage(val status: Status) : WearMessage(PATH_STATUS), Parcelable {
    constructor(dataMap: DataMap) : this(Status.valueOf(dataMap.getInt(STATUS)))

    override fun toDataMap(): DataMap {
        val dataMap = DataMap()
        dataMap.putInt(STATUS, status.code)
        return dataMap
    }

    enum class Status(val code: Int) {
        UNKNOWN(-1),
        START(1),
        PAUSE(2),
        RESUME(3),
        STOP(4);

        companion object {
            @JvmStatic
            fun valueOf(code: Int): Status =
                    when (code) {
                        1 -> START
                        2 -> PAUSE
                        3 -> RESUME
                        4 -> STOP
                        else -> UNKNOWN
                    }
        }
    }
}
