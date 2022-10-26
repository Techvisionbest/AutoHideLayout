package com.vision.overhide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var tvRedraw: TextView
    lateinit var tvMain: TextView
    var text = "bilibili荣誉出品bilibili荣誉出品bilibili荣誉出品"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvRedraw = findViewById(R.id.tvRedraw)
//        tvMain = findViewById(R.id.mainText)
        tvRedraw.setOnClickListener {
            text += "品"
            tvMain.text = text
        }
    }
}