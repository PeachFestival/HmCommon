package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.tencent.mmkv.MMKV

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var editStr1 =MMKV.defaultMMKV().decodeString("edit1","&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本人<font color='#4295c9'>21电子商务5 (校企) 王海龙</font>因<font color='#4295c9'>感冒头疼</font>，需请<font color='#4295c9'>病假</font>，" +
                "从<font color='#4295c9'>2023-04-07 08:00 (周五) 早操</font>至<font color='#4295c9'>2023-04-08 12:30 (周五) 午休</font>，不能参加累计<font color='#4295c9'>1</font>节课程的学习。\n请假期间，本人将去往" +
                "<font color='#4295c9'>校外</font>,详细地址为<font color='#4295c9'>宿舍</font>，联系人: <font color='#4295c9'>王海龙</font>，联系电话<font color='#4295c9'>15315365307</font>。")
        var editStr2 = MMKV.defaultMMKV().decodeString("edit2","&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本人已签署<font color='#4295c9'>承诺书</font>书，承诺请假期间严格遵守国家法律法规和校规校及相关规定，不参加任何非法游行、集会活动，一切安全事故责任自负，按时返校销假，请准假!")

        var name = MMKV.defaultMMKV().decodeString("editName","大春")
        var teacher = MMKV.defaultMMKV().decodeString("editTeacher","老张")
        var time1= MMKV.defaultMMKV().decodeString("editTime1","2023-03-24 08:00")
        var time2= MMKV.defaultMMKV().decodeString("editTime2","2023-03-24 08:00")
        var time3= MMKV.defaultMMKV().decodeString("editTime3","2023-03-24 08:00")

        var edit1:EditText = findViewById(R.id.edit_1)
        var edit2:EditText = findViewById(R.id.edit_2)

        var editName:EditText = findViewById(R.id.edit_name)
        var editTeacher:EditText = findViewById(R.id.edit_teacher)

        var editTime1:EditText = findViewById(R.id.time)
        var editTime2:EditText = findViewById(R.id.time2)
        var editTime3:EditText = findViewById(R.id.time3)

        edit1.setText(editStr1)
        edit2.setText(editStr2)

        editName.setText(name)
        editTeacher.setText(teacher)
        editTime1.setText(time1)
        editTime2.setText(time2)
        editTime3.setText(time3)

        var button = findViewById<Button>(R.id.save)
        button.setOnClickListener {
            MMKV.defaultMMKV().encode("edit1",edit1.text.toString())
            MMKV.defaultMMKV().encode("edit2",edit2.text.toString())
            MMKV.defaultMMKV().encode("editName",editName.text.toString())
            MMKV.defaultMMKV().encode("editTeacher",editTeacher.text.toString())
            MMKV.defaultMMKV().encode("editTime1",editTime1.text.toString())
            MMKV.defaultMMKV().encode("editTime2",editTime2.text.toString())
            MMKV.defaultMMKV().encode("editTime3",editTime3.text.toString())

            finish()
        }
    }
}