package com.example.levelup.ui.catalog

import androidx.annotation.DrawableRes
import com.example.levelup.R

@DrawableRes
fun productImageResource(imageKey: String): Int = when (imageKey) {
    "catan" -> R.drawable.catan
    "carcasone" -> R.drawable.carcasone
    "xbosseries" -> R.drawable.xbosseries
    "hyperxcloud" -> R.drawable.hyperxcloud
    "pley5" -> R.drawable.pley5
    "pcgamer" -> R.drawable.pcgamer
    "sillagamer" -> R.drawable.sillagamer
    "logitchg502" -> R.drawable.logitchg502
    "mousepadrazer" -> R.drawable.mousepadrazer
    "polera_negra" -> R.drawable.polera_negra
    else -> R.drawable.ic_launcher_foreground
}