package com.hengmei.hm_common.utils

/**
 * 版本比较工具类
 * 提供多种版本号比较方法
 */
object VersionCompareUtils {
    
    /**
     * 比较两个版本号
     * @param currentVersion 当前版本号，如 "1.0.0" 或 "100" (对应 versionCode)
     * @param remoteVersion 远程版本号，如 "1.0.1" 或 "101" (对应 versionCode)
     * @return -1 如果当前版本小于远程版本，0 如果相等，1 如果当前版本大于远程版本
     */
    fun compareVersions(currentVersion: String, remoteVersion: String): Int {
        // 尝试直接转换为数字比较（适用于 versionCode）
        val currentNum = currentVersion.toIntOrNull()
        val remoteNum = remoteVersion.toIntOrNull()
        
        if (currentNum != null && remoteNum != null) {
            return when {
                currentNum < remoteNum -> -1
                currentNum > remoteNum -> 1
                else -> 0
            }
        }
        
        // 如果不是纯数字，则按语义化版本号比较
        val currentParts = currentVersion.split('.')
        val remoteParts = remoteVersion.split('.')
        
        val maxLength = maxOf(currentParts.size, remoteParts.size)
        
        for (i in 0 until maxLength) {
            val currentPart = if (i < currentParts.size) currentParts[i].toIntOrNull() ?: 0 else 0
            val remotePart = if (i < remoteParts.size) remoteParts[i].toIntOrNull() ?: 0 else 0
            
            when {
                currentPart < remotePart -> return -1
                currentPart > remotePart -> return 1
            }
        }
        
        return 0
    }
    
    /**
     * 检查远程版本是否更新
     * @return true 如果远程版本比当前版本新
     */
    fun isRemoteVersionNewer(currentVersion: String, remoteVersion: String): Boolean {
        return compareVersions(currentVersion, remoteVersion) < 0
    }
    
    /**
     * 检查远程版本是否更新 (整数版本号)
     * @return true 如果远程版本比当前版本新
     */
    fun isRemoteVersionNewer(currentVersionCode: Int, remoteVersionCode: Int): Boolean {
        return currentVersionCode < remoteVersionCode
    }

}