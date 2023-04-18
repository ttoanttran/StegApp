package com.example.myapplication

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import java.io.IOException
import java.math.BigInteger

class encrypt_page : AppCompatActivity() {

    private lateinit var imagebutton: Button
    private lateinit var imageView: ImageView
    private lateinit var status_text: TextView
    private lateinit var encrypt_button: Button
    private lateinit var download_button: Button

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_page)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imagebutton = findViewById(R.id.upload_button)
        imageView = findViewById(R.id.e_image)

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
                // hide encrypted messsage when user uploads new image
                status_text.visibility = View.INVISIBLE
            })

        imagebutton.setOnClickListener {
            galleryImage.launch("image/*")
        }

        // Status visibility
        status_text = findViewById(R.id.e_alert)
        encrypt_button = findViewById(R.id.e_button)

        encrypt_button.setOnClickListener {
            val messagebox: TextInputEditText = findViewById(R.id.e_messagebox)
            val epassbox: TextInputEditText = findViewById(R.id.e_passbox)

            val encodedImage = encodeImage(imageView, messagebox, epassbox)

            // set new bitmap to ImageView to be downloaded
            imageView.setImageBitmap(encodedImage)

            // Display the encrypted message if image was encoded

            status_text.visibility = View.VISIBLE
        }
        // initialize download button
        download_button = findViewById(R.id.download_button)

        download_button.setOnClickListener {
            // get the encoded bitmap from imageView to bitmap
            val encodedBitmap = (imageView.drawable as BitmapDrawable).bitmap
            downloadImageToGallery(encodedBitmap, this, "encoded_image")
        }

    }

    // download the image in PNG form and save to phone gallery
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadImageToGallery(bitmap: Bitmap, context: Context, title: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            }
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

    private fun encodeImage(
        image: ImageView,
        message: TextInputEditText,
        password: TextInputEditText
    ): Bitmap {
        // turn imageView into a bitmap
        val drawable = image.drawable
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap

        //turn message and password into a string first
        val messageString: String = message.text.toString()

        // combine password and message and then convert to binary
        val binaryMessage = convertToBinary(messageString)

        // get password in binary form
        // test to see if password is 8 or less characters
        try {
            val passwordString: String = password.text.toString()
            if (passwordString.length != 4) {
                throw IllegalArgumentException("Password has to be 4 characters")
            }

            // pad the password to 64 bits
            val binaryPassword = convertToBinary(passwordString)
            val paddedPassword = binaryPassword.padStart(32, '0')

            // get length of message in binary form
            val messagelength = binaryMessage.length
            val lengthBits = Integer.toBinaryString(messagelength).padStart(32, '0')

            val encodedMessage = lengthBits + paddedPassword + binaryMessage

            // check to see if the message can fit inside the bitmap
            try {
                val maxLength = bitmap.width * bitmap.height
                if (encodedMessage.length > maxLength) {
                    throw IllegalArgumentException("Message is too long")
                }

                //encode message
                var index = 0
                val newBitmap = bitmap.copy(bitmap.config, true)
                for (y in 0 until newBitmap.height) {
                    if (index >= encodedMessage.length) {
                        break
                    }
                    for (x in 0 until newBitmap.width) {
                        if (index >= encodedMessage.length) {
                            break
                        }
                        val pixel = newBitmap.getPixel(x, y)
                        val red = Color.red(pixel)
                        val newRed = (red and 0xFE) or encodedMessage[index].code
                        val newPixel = Color.rgb(newRed, Color.green(pixel), Color.blue(pixel))
                        newBitmap.setPixel(x, y, newPixel)
                        index += 1
                    }
                }
                return newBitmap
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Message is too long", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Password has to be 4 characters", Toast.LENGTH_SHORT).show()
        }
        status_text.setText("Encryption Failed")
        return bitmap
    }

    fun fromBinaryString(binaryString: String): String {
        val bytes = binaryString.chunked(8).map { Integer.parseInt(it, 2).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }
}

