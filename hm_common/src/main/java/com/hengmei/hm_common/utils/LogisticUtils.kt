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


        /**
         * 公式"y = a+bx"
         * @param x x值
         * @param a a值
         * @param b b值
         * @param  num 保留的小数位数
         * @return 返回计算后的结果
         * */
        fun getLogisticValueAB(
            x: Double,
            a: Double,
            b: Double,
            num: Int
        ): Double {
            val multiplicationCalculation =    NumberUtils.multiplicationStr(x.toString(), b.toString(), 9)
            val additionCalculation = NumberUtils.additionStr(a.toString(), multiplicationCalculation)
            return NumberUtils.divisionStr(additionCalculation, "1", num).toDouble()
        }
        /**
         * 公式 "y = a+bx+cx^2"
         * @param x x值
         * @param a a值
         * @param b b值
         * @param c c值
         * @param  num 保留的小数位数
         * @return 返回计算后的结果
         *
         */
        fun getLogisticValueABC(
            x: Double,
            a: Double,
            b: Double,
            c: Double,
            num: Int
        ): Double {
            val multiplicationCalculation =    NumberUtils.multiplicationStr(x.toString(), b.toString(), 9)
            val x2 =    NumberUtils.multiplicationStr(x.toString(),x.toString(), 9)
            val multiplicationCalculation2 =    NumberUtils.multiplicationStr(x2, c.toString(), 9)
            val additionCalculation = NumberUtils.additionStr(a.toString(), multiplicationCalculation)
            val additionCalculation2 =NumberUtils.additionStr(additionCalculation, multiplicationCalculation2)
            return NumberUtils.divisionStr(additionCalculation2, "1", num).toDouble()
        }
        /**
         * 公式 "y = a + bx + cx^2 + dx^3"
         * @param x x值
         * @param a a值
         * @param b b值
         * @param c c值
         * @param d d值
         * @param num 保留的小数位数
         * @return 返回计算后的结果
         */
        fun getLogisticValueABCD(
            x: Double,
            a: Double,
            b: Double,
            c: Double,
            d: Double,
            num: Int
        ): Double {
            // 计算 bx
            val bx = NumberUtils.multiplicationStr(x.toString(), b.toString(), 9)

            // 计算 x^2
            val x2 = NumberUtils.multiplicationStr(x.toString(), x.toString(), 9)

            // 计算 cx^2
            val cx2 = NumberUtils.multiplicationStr(x2, c.toString(), 9)

            // 计算 x^3
            val x3 = NumberUtils.multiplicationStr(x2, x.toString(), 9)

            // 计算 dx^3
            val dx3 = NumberUtils.multiplicationStr(x3, d.toString(), 9)

            // 计算 a + bx + cx^2 + dx^3
            val addition1 = NumberUtils.additionStr(a.toString(), bx)
            val addition2 = NumberUtils.additionStr(addition1, cx2)
            val addition3 = NumberUtils.additionStr(addition2, dx3)

            // 返回结果，保留指定小数位数
            return NumberUtils.divisionStr(addition3, "1", num).toDouble()
        }
    }

}