package com.example.myapplication

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputEditText

class encrypt_page : AppCompatActivity() {

    private lateinit var imagebutton: Button
    private lateinit var imageView: ImageView
    private lateinit var status_text: TextView
    private lateinit var encrypt_button: Button
    private lateinit var download_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_page)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imagebutton = findViewById(R.id.upload_button)
        imageView = findViewById(R.id.e_image)

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
            })

        imagebutton.setOnClickListener {
            galleryImage.launch("image/*")
        }

        // Status visibility
        status_text = findViewById(R.id.e_alert)
        encrypt_button = findViewById(R.id.e_button)

        // val encodedImage: Bitmap? = null

        encrypt_button.setOnClickListener {
            val messagebox: TextInputEditText = findViewById(R.id.e_messagebox)

            val encodedImage = encodeImage(imageView, messagebox)

            val m = getPixel(encodedImage)
            println(m.joinToString(""))



            status_text.visibility = View.VISIBLE


        }

        download_button = findViewById(R.id.download_button)

        download_button.setOnClickListener {

        }

    }


    private fun convertToBinary(messageInput: String): String {
        val bytes = messageInput.toByteArray()
        val binaryString = StringBuilder()

        for (byte in bytes) {
            var binary = Integer.toBinaryString(byte.toInt())
            while (binary.length < 8) {
                binary = "0$binary"
            }
            binaryString.append(binary)
        }
        return binaryString.toString()
    }

    // Function to encode one bit of the message in the least significant bit of a color channel
    fun encodeBit(color: Int, bit: Char): Int {
        return if (bit == '0') {
            color and 0xfe
        } else {
            color or 0x01
        }
    }

    fun getPixel(bitmap: Bitmap): List<Int> {
        var length = 10
        var index = 0
        val pixelLSBs = mutableListOf<Int>()
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel) and 1
                val g = Color.red(pixel) and 1
                val b = Color.red(pixel) and 1
                pixelLSBs.add(r)
                pixelLSBs.add(g)
                pixelLSBs.add(b)
                index += 1
                if (index == length) {
                    break
                }
            }
            if (index >= length) {
                break
            }
        }
        return pixelLSBs
    }



    private fun encodeImage(
        image: ImageView,
        message: TextInputEditText
    ): Bitmap {
        //turn message and password into a string first
        val messageString: String = message.text.toString()

        // combine password and message and then convert to binary
        val binaryMessage = convertToBinary(messageString)

        println(binaryMessage)

        // turn imageView into a bitmap
        val drawable = image.drawable
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap

        //val bitmap = Bitmap.createBitmap(oldbitmap.width, oldbitmap.height, oldbitmap.config)
        //val canvas = Canvas(bitmap)
        //canvas.drawBitmap(oldbitmap, 0f, 0f, null)

        print("Width: ")
        println(bitmap.width)
        print("height: ")
        println(bitmap.height)

        val m = getPixel(bitmap)
        println(m.joinToString(""))

        var messagelength = binaryMessage.length
        val lengthBits = Integer.toBinaryString(messagelength).padStart(32, '0')

        val encodedMessage = lengthBits + binaryMessage

        println(messagelength)
        println(lengthBits)

        val maxLength = bitmap.width * bitmap.height * 3
        if (encodedMessage.length > maxLength) {
            throw IllegalArgumentException("Message and password are too long")
        }

        //encode message
        var newLsb = Integer.parseInt(encodedMessage, 2)
        val encodedBitmap = bitmap.copy(bitmap.config, true)
        var bitIndex = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitIndex >= encodedMessage.length) {
                    break
                }
                val pixel = bitmap.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                val newRed = (red and 0xFE) or ((newLsb shr 7) and 0x01)
                val newGreen = (green and 0xFE) or ((newLsb shr 6) and 0x01)
                val newBlue = (blue and 0xFE) or ((newLsb shr 5) and 0x01)
                val newPixel = Color.rgb(newRed, newGreen, newBlue)
                encodedBitmap.setPixel(x, y, newPixel)
                newLsb = newLsb shl 3
                bitIndex += 3
            }
            if (bitIndex >= encodedMessage.length) {
                break
            }
        }

        return encodedBitmap
    }




    // Function to convert a binary string to a plain text string
    fun fromBinaryString(binaryString: String): String {
        val bytes = binaryString.chunked(8).map { Integer.parseInt(it, 2).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }


}

