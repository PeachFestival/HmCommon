package com.example.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.LinearLayoutCompat
import com.gyf.immersionbar.ktx.immersionBar

class ListActivity : AppCompatActivity() {

    var title: LinearLayoutCompat? = null
    var jump_details: LinearLayoutCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        supportActionBar?.hide();

        title = findViewById(R.id.title_bar)
        jump_details = findViewById(R.id.jump_details)
        immersionBar {
            statusBarColor(R.color.title_color)
            navigationBarColor(R.color.title_color)
            titleBar(title)
        }
        jump_details?.setOnClickListener {

            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent) }
        }


}