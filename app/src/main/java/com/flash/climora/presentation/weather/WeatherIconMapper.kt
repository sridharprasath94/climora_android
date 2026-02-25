package com.flash.climora.presentation.weather

import androidx.annotation.DrawableRes
import com.flash.climora.R

/**
 * UI mapper: Weather conditionCode + isDay -> drawable resource
 */
@DrawableRes
fun weatherIconRes(conditionCode: Int, isDay: Boolean): Int {
    return when (conditionCode) {
        1000 -> if (isDay) R.drawable.sun_max_fill else R.drawable.moon_stars_fill
        1003 -> if (isDay) R.drawable.cloud_sun else R.drawable.cloud_moon_fill

        1006, 1009 -> R.drawable.cloud_fill
        1030, 1135, 1147 -> R.drawable.cloud_fog_fill

        1063, 1150, 1153, 1180, 1183, 1240 -> R.drawable.cloud_drizzle_fill
        1186, 1189, 1243 -> R.drawable.cloud_rain_fill
        1192, 1195, 1246 -> R.drawable.cloud_heavy_rain_fill

        1066, 1210, 1213, 1255,
        1216, 1219, 1258 -> R.drawable.cloud_snow_fill

        1222, 1225 -> R.drawable.cloud_snow_flake_fill

        1087, 1273, 1276, 1279, 1282 -> R.drawable.cloud_bolt_rain_fill

        else -> R.drawable.cloud_fill
    }
}