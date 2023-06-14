package app.bicast.finma.utils

import java.util.Calendar

class DateUtils {
    companion object {
        fun startTime(dateTime: Long): Long {
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = dateTime
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            return tempCal.timeInMillis
        }
        fun endTime(dateTime: Long): Long {
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = dateTime
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            tempCal.add(Calendar.DATE,1)
            tempCal.add(Calendar.SECOND,-1)
            return tempCal.timeInMillis
        }
        fun monthStartTime() : Long{
            val tempCal = Calendar.getInstance()
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            tempCal.set(Calendar.DAY_OF_MONTH, 1)
            return tempCal.timeInMillis
        }
        fun monthEndTime() : Long{
            val tempCal = Calendar.getInstance()
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
            tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            tempCal.add(Calendar.DAY_OF_MONTH,1)
            tempCal.add(Calendar.SECOND,-1)
            return tempCal.timeInMillis
        }
    }
}