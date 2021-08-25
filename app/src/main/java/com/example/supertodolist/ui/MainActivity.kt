package com.example.supertodolist.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.supertodolist.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val name : String = "Ali"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }
}