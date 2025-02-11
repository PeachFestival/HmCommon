package com.hengmei.hm_common.utils
import android.os.SystemClock
import android.util.Log
import tp.xmaihh.serialport.stick.AbsStickPackageHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder

class UartBag(
    private val byteOrder: ByteOrder,
    private val lenSize: Int,
    private val lenIndex: Int,
    private val offset: Int
) : AbsStickPackageHelper {

    private val mBytes = mutableListOf<Byte>()
    private val lenStartIndex = lenIndex
    private val lenEndIndex = lenIndex + lenSize - 1

    init {
        require(lenStartIndex <= lenEndIndex) { "lenStartIndex > lenEndIndex" }
    }

    private fun getLen( src:ByteArray): Int {



        return byteToInt(src[0],src[1])
    }
    override fun execute(inStr: InputStream): ByteArray? {
        val buffer = ByteArray(1024) // 固定大小的缓冲区
        val outputStream = ByteArrayOutputStream() // 用于拼接分片数据包

        try {
            while (true) {
                val bytesRead = inStr.read(buffer) // 读取数据
                if (bytesRead == -1) {
                    break // 输入流结束
                }

                outputStream.write(buffer, 0, bytesRead) // 将读取的数据写入输出流

                // 检查是否有完整的数据包
                val data = outputStream.toByteArray()
                if (data.size >= 4 && data[0] == 0xAA.toByte()&& data[1] == 0x55.toByte()) {

                    val packetSize = 4104 // 计算完整数据包的大小

                    if (data.size >= packetSize) {
                        // 提取完整的数据包
                        val packet = data.copyOfRange(0, packetSize)
                        Log.d("uartBag", "完整数据包: ${packet.toPrintString()}")

                        // 处理剩余数据
                        val remainingData = data.copyOfRange(packetSize, data.size)
                        outputStream.reset()
                        if (remainingData.isNotEmpty()) {
                            outputStream.write(remainingData) // 将剩余数据写入输出流
                        }

                        return packet
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream.close()
        }

        return null
    }
}
