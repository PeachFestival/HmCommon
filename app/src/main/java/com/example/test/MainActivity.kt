package com.example.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.gyf.immersionbar.ktx.immersionBar
import com.tencent.mmkv.MMKV

class MainActivity : AppCompatActivity() {

    var title:LinearLayoutCompat? = null
    var img: ImageView ?=null
    var edit: ImageView ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide();
        title = findViewById(R.id.title_bar)
        img = findViewById(R.id.jump_add)
        edit = findViewById(R.id.jump_edit)
        immersionBar {
            statusBarColor(R.color.title_color)
            navigationBarColor(R.color.title_color)
            titleBar(title)
        }

        val rootDir: String = MMKV.initialize(this)
        println("mmkv root: $rootDir")

        img?.setOnClickListener { view ->
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent) }

        edit?.setOnClickListener { view ->
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent) }

    }
}