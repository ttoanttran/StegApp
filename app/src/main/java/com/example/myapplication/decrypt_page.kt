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
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputEditText
import java.math.BigInteger

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

        val messagebox: TextInputEditText = findViewById(R.id.d_messagebox)

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
                // clear the text box when the user uploads a new image
                messagebox.setText("")
                // clear the decrypted message text
                status_text.visibility = View.INVISIBLE
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

            val passwordbox: TextInputEditText = findViewById(R.id.passbox)
            val passString: String = passwordbox.text.toString()

            try {
                // try but if a NumberFormatException occurs, due to the binary string of the
                // 32 bits not being a valid Int b/c it exceeds max value of an Int
                val lengthMessage = getLengthOfBitmap(bitmap)
                val lengthMessageString = lengthMessage.joinToString("")
                val lengthInt = lengthMessageString.toInt(2)

                try {
                    // if the message does not fit, then that means there is no message because
                    // the message cannot be encoded if it is too long
                    if (lengthInt + 64 > bitmap.width * bitmap.height) {
                        throw IllegalArgumentException("There is no hidden message in this image")
                    }

                    // get the length of the message as a integer to use getPixel() function
                    val mes = getPixel(bitmap, lengthInt + 64)

                    val secretMessageBinary = mes.drop(64).toList()

                    val secretMessageString = fromBinaryString(secretMessageBinary.joinToString(""))

                    // get the password binary form by dropping first 32 and taking next 32 bits
                    val droplength = mes.drop(32).toList()
                    val passconvert = droplength.take(32).toList()
                    val pass = fromBinaryString(passconvert.joinToString(""))

                    if (pass == passString) {
                        // display the secret message in the text box
                        val messagebox: TextInputEditText = findViewById(R.id.d_messagebox)
                        messagebox.setText(secretMessageString)

                        status_text.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Password is incorrect", Toast.LENGTH_SHORT).show()
                    }


                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this, "There is no hidden message in this image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "There is no hidden message in this image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // find the first LSB bits to find the length of the binary message
    fun getLengthOfBitmap (bitmap: Bitmap): List<Int> {
        var index = 0
        val messageLengthList = mutableListOf<Int>()
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