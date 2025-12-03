package com.example.levelup.ui.catalog

import androidx.annotation.DrawableRes
import com.example.levelup.R

@DrawableRes
fun productImageResource(key: String): Int {
    return when (key.lowercase()) {

        "pley5" -> R.drawable.pley5
        "xbosseries" -> R.drawable.xbosseries
        "zelda" -> R.drawable.zelda
        "dualsense" -> R.drawable.dualsense
        "rtx4070" -> R.drawable.rtx4070
        "switcholed" -> R.drawable.switcholed
        "hyperxcloud" -> R.drawable.hyperxcloud
        "eldenring" -> R.drawable.eldenring

        // fallback obligatorio
        else -> R.drawable.setup
    }
}