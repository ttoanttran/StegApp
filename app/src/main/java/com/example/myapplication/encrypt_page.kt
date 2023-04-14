package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
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

        encrypt_button.setOnClickListener {
            val messagebox: TextInputEditText = findViewById(R.id.e_messagebox)
            val passbox: TextInputEditText = findViewById(R.id.passbox)
            val encodedImage = encodeImage(imageView, messagebox, passbox)
            status_text.visibility = View.VISIBLE


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

    private fun encodeBit(color: Int, bit: Char): Int {
        return if (bit == '0') {
            color and 0xfe
        } else {
            color or 0x01
        }
    }

    private fun encodeImage(image: ImageView, message: TextInputEditText, password: TextInputEditText): Bitmap {
        //turn message and password into a string first
        val messageString: String = message.text.toString()
        val passwordString: String = password.text.toString()

        // combine password and message and then convert to binary
        val messageAndPassword = messageString + passwordString
        val binaryMessage = convertToBinary(messageAndPassword)

        println(binaryMessage)

        // turn imageView into a bitmap
        val drawable = image.drawable
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        image.draw(canvas)

        print("Width: ")
        println(bitmap.width)
        print("height: ")
        println(bitmap.height)

        val maxLength = bitmap.width * bitmap.height * 3
        if (binaryMessage.length > maxLength) {
            throw IllegalArgumentException("Message and password are too long")
        }

        println("testing")

        var length = messageString.length + passwordString.length
        val lengthBits = Integer.toBinaryString(length).padStart(16, '0')
        var index = 0

        println("testing2")

        //encode message
        val encodedBitmap = bitmap.copy(bitmap.config, true)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                // check if we have encoded the length of message and password
                if (index < lengthBits.length) {
                    val pixel = bitmap.getPixel(x, y)
                    val red = encodeBit(Color.red(pixel), lengthBits[index])
                    val green = encodeBit(Color.green(pixel), lengthBits[index + 1])
                    val blue = encodeBit(Color.blue(pixel), '0')
                    val encodePixel = Color.rgb(red, green, blue)
                    encodedBitmap.setPixel(x, y, encodePixel)
                    index += 2
                } else {
                    val pixel = bitmap.getPixel(x, y)
                    if (index < binaryMessage.length) {
                        val red = encodeBit(Color.red(pixel), binaryMessage[index])
                        val green = encodeBit(Color.green(pixel), binaryMessage.getOrElse(index + 1) { '0' })
                        val blue = encodeBit(Color.blue(pixel), binaryMessage.getOrElse(index + 2) { '0' })
                        val encodePixel = Color.rgb(red, green, blue)
                        encodedBitmap.setPixel(x, y, encodePixel)
                        index += 3
                    } else {
                        encodedBitmap.setPixel(x, y, bitmap.getPixel(x, y))
                    }
                }
            }
        }
        return encodedBitmap
    }
}
