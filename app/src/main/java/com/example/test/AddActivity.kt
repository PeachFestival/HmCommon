package com.example.test


import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.gyf.immersionbar.ktx.immersionBar
import com.tencent.mmkv.MMKV


class AddActivity : AppCompatActivity() {

    var title: LinearLayoutCompat? = null
    var text_count: TextView? = null
    var text_hint: TextView? = null

    var name1: TextView? = null
    var name2: TextView? = null
    var name3: TextView? = null

    var time1: TextView? = null
    var time2: TextView? = null
    var time3: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        text_count = findViewById(R.id.text_count)
        text_hint = findViewById(R.id.text_hint)

        name1 = findViewById(R.id.name)
        name2 = findViewById(R.id.name2)
        name3 = findViewById(R.id.name3)

        time1 = findViewById(R.id.time)
        time2 = findViewById(R.id.time2)
        time3 = findViewById(R.id.time3)

        supportActionBar?.hide();
        title = findViewById(R.id.title_bar)
        immersionBar {
            statusBarColor(R.color.title_color)
            navigationBarColor(R.color.title_color)
            titleBar(title)
        }

        val charSequence: CharSequence
        val charSequence2: CharSequence


        var editStr1 = MMKV.defaultMMKV().decodeString("edit1","&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本人<font color='#4295c9'>21电子商务5 (校企) 王海龙</font>因<font color='#4295c9'>感冒头疼</font>，需请<font color='#4295c9'>病假</font>，" +
                "从<font color='#4295c9'>2023-04-07 08:00 (周五) 早操</font>至<font color='#4295c9'>2023-04-08 12:30 (周五) 午休</font>，不能参加累计<font color='#4295c9'>1</font>节课程的学习。\n请假期间，本人将去往" +
                "<font color='#4295c9'>校外</font>,详细地址为<font color='#4295c9'>宿舍</font>，联系人: <font color='#4295c9'>王海龙</font>，联系电话<font color='#4295c9'>15315365307</font>。")
        var editStr2 = MMKV.defaultMMKV().decodeString("edit2","&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本人已签署<font color='#4295c9'>承诺书</font>书，承诺请假期间严格遵守国家法律法规和校规校及相关规定，不参加任何非法游行、集会活动，一切安全事故责任自负，按时返校销假，请准假!")


        var name = MMKV.defaultMMKV().decodeString("editName","大春")
        var teacher = MMKV.defaultMMKV().decodeString("editTeacher","老张")
        var timeStr1= MMKV.defaultMMKV().decodeString("editTime1","2023-03-24 08:00")
        var timeStr2= MMKV.defaultMMKV().decodeString("editTime2","2023-03-24 08:00")
        var timeStr3= MMKV.defaultMMKV().decodeString("editTime3","2023-03-24 08:00")

        name1?.text = name
        name2?.text = teacher
        name3?.text = name

        time1?.text = timeStr1
        time2?.text = timeStr2
        time3?.text = timeStr3


        val content = editStr1
        charSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(content)
        }

        val content2 = editStr2
        charSequence2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content2, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(content2)
        }

        text_count?.text = charSequence
        text_hint?.text = charSequence2



//        text_count?.setText(Html.fromHtml(""))

    }
}