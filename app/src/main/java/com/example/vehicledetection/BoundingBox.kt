package com.example.vehicledetection

data class BoundingBox(

    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,

    val score: Float,

    val cls: Int,

    val label: String

)