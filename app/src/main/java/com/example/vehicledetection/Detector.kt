package com.example.vehicledetection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class Detector(
    private val context: Context
) {

    companion object {
        private const val INPUT_SIZE = 640
        private const val NUM_BOXES = 8400
        private const val NUM_CLASSES = 12
        private const val NUM_CHANNELS = 16
        private const val CONFIDENCE_THRESHOLD = 0.90f
        private const val IOU_THRESHOLD = 0.45f
    }

    private val labels = listOf(
        "Mobil Avanza",
        "Mobil Ayla",
        "Mobil Hilux",
        "Mobil Innova",
        "Mobil Kijang",
        "Motor CRF",
        "Motor Scoopy",
        "Motor Supra",
        "Motor WR",
        "Motor Fino",
        "Truk Hino",
        "Truk Tronton"
    )

    private val interpreter: Interpreter

    init {

        interpreter = Interpreter(loadModelFile())

        val inputTensor = interpreter.getInputTensor(0)
        val outputTensor = interpreter.getOutputTensor(0)

        Log.d(
            "MODEL",
            "Input Shape = ${inputTensor.shape().contentToString()}"
        )

        Log.d(
            "MODEL",
            "Output Shape = ${outputTensor.shape().contentToString()}"
        )
    }

    private fun loadModelFile(): ByteBuffer {

        val fileDescriptor = context.assets.openFd("best.tflite")

        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)

        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {

        val resized =
            Bitmap.createScaledBitmap(
                bitmap.copy(Bitmap.Config.ARGB_8888, false),
                INPUT_SIZE,
                INPUT_SIZE,
                true
            )

        val buffer =
            ByteBuffer.allocateDirect(
                4 * 3 * INPUT_SIZE * INPUT_SIZE
            )

        buffer.order(ByteOrder.nativeOrder())

        val pixels =
            IntArray(INPUT_SIZE * INPUT_SIZE)

        resized.getPixels(
            pixels,
            0,
            INPUT_SIZE,
            0,
            0,
            INPUT_SIZE,
            INPUT_SIZE
        )

        for (pixel in pixels)
            buffer.putFloat(
                ((pixel shr 16) and 0xff) / 255f
            )

        for (pixel in pixels)
            buffer.putFloat(
                ((pixel shr 8) and 0xff) / 255f
            )

        for (pixel in pixels)
            buffer.putFloat(
                (pixel and 0xff) / 255f
            )

        buffer.rewind()

        return buffer
    }
    fun detect(bitmap: Bitmap): List<BoundingBox> {

        val input = bitmapToByteBuffer(bitmap)

        val output = Array(1) {
            Array(NUM_CHANNELS) {
                FloatArray(NUM_BOXES)
            }
        }

        interpreter.run(input, output)

        val boxes = mutableListOf<BoundingBox>()

        for (i in 0 until NUM_BOXES) {

            // YOLOv8/v12 typically outputs normalized coordinates [0, 1] 
            // but the detect function here treats them as pixel values [0, 640].
            // If the model outputs normalized values, we must scale them.
            val cx = output[0][0][i] * INPUT_SIZE
            val cy = output[0][1][i] * INPUT_SIZE
            val w = output[0][2][i] * INPUT_SIZE
            val h = output[0][3][i] * INPUT_SIZE

            var bestScore = 0f
            var bestClass = -1

            for (c in 0 until NUM_CLASSES) {

                val score = output[0][4 + c][i]

                if (score > bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }

            if (bestClass == -1) continue
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            val x1 = max(0f, cx - w / 2f)
            val y1 = max(0f, cy - h / 2f)
            val x2 = min(INPUT_SIZE.toFloat(), cx + w / 2f)
            val y2 = min(INPUT_SIZE.toFloat(), cy + h / 2f)

            boxes.add(
                BoundingBox(
                    x1 = x1,
                    y1 = y1,
                    x2 = x2,
                    y2 = y2,
                    score = bestScore,
                    cls = bestClass,
                    label = labels[bestClass]
                )
            )
        }

        return applyNMS(boxes)
    }
    private fun calculateIoU(
        a: BoundingBox,
        b: BoundingBox
    ): Float {

        val x1 = max(a.x1, b.x1)
        val y1 = max(a.y1, b.y1)
        val x2 = min(a.x2, b.x2)
        val y2 = min(a.y2, b.y2)

        val intersection =
            max(0f, x2 - x1) *
                    max(0f, y2 - y1)

        val areaA =
            (a.x2 - a.x1) *
                    (a.y2 - a.y1)

        val areaB =
            (b.x2 - b.x1) *
                    (b.y2 - b.y1)

        val union = areaA + areaB - intersection

        return if (union <= 0f) 0f else intersection / union
    }

    private fun applyNMS(
        boxes: List<BoundingBox>
    ): List<BoundingBox> {

        if (boxes.isEmpty())
            return emptyList()

        val selected = mutableListOf<BoundingBox>()

        val sorted =
            boxes.sortedByDescending {
                it.score
            }.toMutableList()

        while (sorted.isNotEmpty()) {

            val first = sorted.removeAt(0)

            selected.add(first)

            val iterator = sorted.iterator()

            while (iterator.hasNext()) {

                val next = iterator.next()

                if (
                    first.cls == next.cls &&
                    calculateIoU(first, next) > IOU_THRESHOLD
                ) {
                    iterator.remove()
                }
            }
        }

        return selected
    }

}