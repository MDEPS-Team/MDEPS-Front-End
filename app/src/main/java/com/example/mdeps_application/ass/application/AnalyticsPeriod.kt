package com.example.mdeps_application.ass.application

import com.example.mdeps_application.R
import java.util.Calendar

enum class AnalyticsPeriod(val labelRes: Int) {
    MONTH(R.string.ass_period_month),
    QUARTER(R.string.ass_period_quarter),
    YTD(R.string.ass_period_ytd),
    CUSTOM(R.string.ass_period_custom),
}

fun AnalyticsPeriod.rangeEndInclusive(nowMillis: Long = System.currentTimeMillis()): LongRange {
    val day = 86_400_000L
    return when (this) {
        AnalyticsPeriod.MONTH -> (nowMillis - 30L * day)..nowMillis
        AnalyticsPeriod.QUARTER -> (nowMillis - 90L * day)..nowMillis
        AnalyticsPeriod.YTD -> {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)
            cal.timeInMillis..nowMillis
        }
        AnalyticsPeriod.CUSTOM -> (nowMillis - 45L * day)..nowMillis
    }
}
