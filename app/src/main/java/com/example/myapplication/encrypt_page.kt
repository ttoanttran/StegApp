package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class encrypt_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_page)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}