package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts

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
            status_text.visibility = View.VISIBLE
        }

    }
}