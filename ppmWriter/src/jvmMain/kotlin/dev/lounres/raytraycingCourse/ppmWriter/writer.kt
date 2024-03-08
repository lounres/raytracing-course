package dev.lounres.raytraycingCourse.ppmWriter

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.bufferedWriter


public fun Path.writePpmImage(imageWidth: UInt, imageHeight: UInt, bytesToWrite: ByteArray) {
    val outputWriter = this.bufferedWriter()

    outputWriter.appendLine("P6")
    outputWriter.appendLine("$imageWidth $imageHeight")
    outputWriter.appendLine("255")

    outputWriter.close()

    Files.write(this, bytesToWrite, StandardOpenOption.APPEND)
}