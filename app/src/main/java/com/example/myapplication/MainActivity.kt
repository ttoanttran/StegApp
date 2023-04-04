package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.encrypt_button)
        button1.setOnClickListener {
            val intent = Intent(this, encrypt_page ::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<Button>(R.id.decrypt_button)
        button2.setOnClickListener {
            val intent = Intent(this, decrypt_page ::class.java)
            startActivity(intent)
        }
    }
}