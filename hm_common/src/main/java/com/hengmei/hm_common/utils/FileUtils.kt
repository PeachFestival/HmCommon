package com.hengmei.hm_common.utils

import android.os.Environment
import java.io.File

class FileUtils {

    companion object{
        fun getSDPath(): String {
            var sdDir: File? = null
            val sdCardExist = (Environment.getExternalStorageState()
                    == Environment.MEDIA_MOUNTED) //判断sd卡是否存在
            if (sdCardExist) {
                sdDir = Environment.getExternalStorageDirectory() //获取跟目录
            }
            return sdDir.toString()
        }

        fun getFileName(fileAbsolutePath: String): ArrayList<File> {
            val vecFile = ArrayList<File>()
            val file = File(fileAbsolutePath)
            val subFile = file.listFiles()
            if (subFile != null) {
                for (iFileLength in subFile.indices) {
                    // 判断是否为文件夹
                    if (!subFile[iFileLength].isDirectory) {
                        val filename = subFile[iFileLength].name
                        println("文件名 ： $filename")

                        if (isVideo(subFile[iFileLength])) {
                            vecFile.add(subFile[iFileLength])
                        }
                    }
                }
            }
            return vecFile
        }

        private fun isVideo(file: File): Boolean {
            var isVideo = false
            val fileName = file.name

            if (fileName.contains(".")) {
                val str1 = fileName.substring(0, fileName.indexOf("."))
                val str2 = fileName.substring(str1.length + 1)
                println("文件扩展名：$str2")

                val formatList = listOf(
                    "avi", "flv", "mpg", "mpeg", "mpe", "mp4", "3gp", "3gpp", "mov", "amr", "rm", "rmvb"
                )


                if (formatList.contains(str2)) {
                    isVideo = true
                }
            }
            return isVideo
        }
    }

}