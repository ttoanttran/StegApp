package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputEditText

class decrypt_page : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var status_text: TextView
    private lateinit var decrypt_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt_page)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        button = findViewById(R.id.upload_button)
        imageView = findViewById(R.id.d_image)

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
            })

        button.setOnClickListener {
            galleryImage.launch("image/*")
        }

        status_text = findViewById(R.id.d_alert)
        decrypt_button = findViewById(R.id.d_button)

        decrypt_button.setOnClickListener {
            // turn imageView into a bitmap to be able to use the function
            val drawable = imageView.drawable
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap

            // get the length of the message as a integer to use getPixel() function
            val lengthMessage = getLengthOfBitmap(bitmap)
            val lengthMessageString = lengthMessage.joinToString("")
            val lengthInt = lengthMessageString.toInt(2)

            val mes = getPixel(bitmap, lengthInt + 32)

            val secret_message = mes.drop(32).toList()
            val test = fromBinaryString(secret_message.joinToString(""))
            println(test)

            status_text.visibility = View.VISIBLE
        }
    }

    fun getLengthOfBitmap (bitmap: Bitmap): List<Int> {
        var index = 0
        var messageLengthList = mutableListOf<Int>()
        for (y in 0 until bitmap.height) {
            if (index >= 32) {
                break
            }
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                // get only the red color channel
                val r = Color.red(pixel) and 1
                messageLengthList.add(r)
                index += 1
                if (index >= 32) {
                    break
                }
            }
        }
        return messageLengthList
    }

    fun getPixel(bitmap: Bitmap, maxlength: Int): List<Int> {
        var index = 0
        val pixelLSBs = mutableListOf<Int>()
        for (y in 0 until bitmap.height) {
            if (index >= maxlength) {
                break
            }
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                // get only the red color channel
                val r = Color.red(pixel) and 1
                pixelLSBs.add(r)
                index += 1
                if (index >= maxlength) {
                    break
                }
            }
        }
        return pixelLSBs
    }

    // Function to convert a binary string to a plain text string
    fun fromBinaryString(binaryString: String): String {
        val bytes = binaryString.chunked(8).map { Integer.parseInt(it, 2).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }

}