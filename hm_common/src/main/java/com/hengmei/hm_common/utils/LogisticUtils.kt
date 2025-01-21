package com.hengmei.hm_common.utils

//曲线操作工具类
class LogisticUtils {

    companion object {
        fun logisticAB(xData: DoubleArray, yData: DoubleArray): Map<String, Double> {
            val curveFitter = Logistic(xData, yData)
            curveFitter.doFit(0)
            var r2Str: String = curveFitter.getResultString()[0]
            var logisticStr: String = curveFitter.getResultString()[1]
            if (logisticStr.startsWith("\n")) {
//            判断起始位是否是换行符，是的话，删除起始位的换行符
                logisticStr = logisticStr.replaceFirst("\n".toRegex(), "")
            }
            log("拟合值 r2Str：" + r2Str + " logisticStr " + logisticStr)
            var allLogic: Array<String> = arrayOf()
            allLogic =
                logisticStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in allLogic.indices) {
                allLogic[i] =
                    allLogic[i].replace("a=", "").replace("b=", "")
                        .replace("R2=", "")
            }
            var lineValueA = allLogic[0]
            var lineValueB = allLogic[1]

            var r2Value = allLogic[2]
            return mapOf(
                "valueA" to lineValueA.toDouble(),
                "valueB" to lineValueB.toDouble(),
                "valueR" to r2Value.toDouble()
            )
        }
        fun logisticABC(xData: DoubleArray, yData: DoubleArray): Map<String, Double> {
            val curveFitter = Logistic(xData, yData)
            curveFitter.doFit(1)
            var r2Str: String = curveFitter.getResultString()[0]
            var logisticStr: String = curveFitter.getResultString()[1]
            if (logisticStr.startsWith("\n")) {
//            判断起始位是否是换行符，是的话，删除起始位的换行符
                logisticStr = logisticStr.replaceFirst("\n".toRegex(), "")
            }
            log("拟合值 r2Str：" + r2Str + " logisticStr " + logisticStr)
            var allLogic: Array<String> = arrayOf()
            allLogic =
                logisticStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in allLogic.indices) {
                allLogic[i] =
                    allLogic[i].replace("a=", "").replace("b=", "").replace("c=", "")
                        .replace("R2=", "")
            }
            var lineValueA = allLogic[0]
            var lineValueB = allLogic[1]
            var lineValueC = allLogic[2]
            var r2Value = allLogic[3]
            return mapOf(
                "valueA" to lineValueA.toDouble(),
                "valueB" to lineValueB.toDouble(),
                "valueC" to lineValueC.toDouble(),
                "valueR" to r2Value.toDouble()
            )
        }

        fun logisticABCD(xData: DoubleArray, yData: DoubleArray): Map<String, Double> {
            val curveFitter = Logistic(xData, yData)
            curveFitter.doFit(2)
            var r2Str: String = curveFitter.getResultString()[0]
            var logisticStr: String = curveFitter.getResultString()[1]
            if (logisticStr.startsWith("\n")) {
//            判断起始位是否是换行符，是的话，删除起始位的换行符
                logisticStr = logisticStr.replaceFirst("\n".toRegex(), "")
            }
            log("拟合值 r2Str：" + r2Str + " logisticStr " + logisticStr)
            var allLogic: Array<String> = arrayOf()
            allLogic =
                logisticStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in allLogic.indices) {
                allLogic[i] =
                    allLogic[i].replace("a=", "").replace("b=", "").replace("c=", "").replace("d=", "")
                        .replace("R2=", "")
            }
            var lineValueA = allLogic[0]
            var lineValueB = allLogic[1]
            var lineValueC = allLogic[2]
            var lineValueD = allLogic[3]
            var r2Value = allLogic[4]
            return mapOf(
                "valueA" to lineValueA.toDouble(),
                "valueB" to lineValueB.toDouble(),
                "valueC" to lineValueC.toDouble(),
                "valueD" to lineValueD.toDouble(),
                "valueR" to r2Value.toDouble()
            )
        }
    }

}