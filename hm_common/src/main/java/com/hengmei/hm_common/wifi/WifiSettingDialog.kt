package com.hengmei.hm_common.wifi

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hengmei.hm_common.R
import com.iwdael.wifimanager.IWifi
import com.iwdael.wifimanager.IWifiManager
import com.iwdael.wifimanager.OnWifiChangeListener
import com.iwdael.wifimanager.OnWifiConnectListener
import com.iwdael.wifimanager.OnWifiStateChangeListener
import com.iwdael.wifimanager.State

/**
 * WiFi 设置弹窗（支持多语言国际化，支持外部状态回调与配置）
 */
class WifiSettingDialog : DialogFragment() {

    private var wifiManager: IWifiManager? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var adapter: WifiAdapter? = null
    private val wifiList = mutableListOf<IWifi>()

    private var switchWifi: Switch? = null
    private var ivRefresh: ImageView? = null
    private var ivClose: ImageView? = null
    private var pbScanning: ProgressBar? = null
    private var tvEmptyTip: TextView? = null
    private var rvWifiList: RecyclerView? = null

    private var isScanning = false
    /** 标记是否有正在进行中的连接请求（防止 DISCONNECTED 广播误触 Toast） */
    private var isConnecting = false

    /** 外部事件监听回调 */
    var onWifiConnectedListener: ((wifi: IWifi?) -> Unit)? = null
    var onWifiDisconnectedListener: (() -> Unit)? = null
    var onWifiConnectTimeoutListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_wifi_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        checkPermissions()
        initWifiManager()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val dm = resources.displayMetrics
            val density = dm.density
            val screenWidth = dm.widthPixels
            val screenHeight = dm.heightPixels
            val isLandscape = screenWidth > screenHeight
            val targetWidth = if (isLandscape) {
                (screenWidth * 0.72f).toInt().coerceIn(
                    (450 * density).toInt().coerceAtMost(screenWidth),
                    (720 * density).toInt().coerceAtMost(screenWidth)
                )
            } else {
                (screenWidth * 0.92f).toInt().coerceIn(
                    (320 * density).toInt().coerceAtMost(screenWidth),
                    (540 * density).toInt().coerceAtMost(screenWidth)
                )
            }
            window.setLayout(targetWidth, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun initViews(root: View) {
        switchWifi = root.findViewById(R.id.switch_wifi)
        ivRefresh = root.findViewById(R.id.iv_refresh)
        ivClose = root.findViewById(R.id.iv_close)
        pbScanning = root.findViewById(R.id.pb_scanning)
        tvEmptyTip = root.findViewById(R.id.tv_empty_tip)
        rvWifiList = root.findViewById(R.id.rv_wifi_list)

        adapter = WifiAdapter(wifiList) { wifi ->
            onWifiItemClick(wifi)
        }
        rvWifiList?.layoutManager = LinearLayoutManager(context)
        rvWifiList?.adapter = adapter

        ivClose?.setOnClickListener {
            dismissAllowingStateLoss()
        }

        ivRefresh?.setOnClickListener {
            startScan()
        }
    }

    private fun checkPermissions() {
        val act = activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasLocation = ContextCompat.checkSelfPermission(
                act,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasLocation) {
                ActivityCompat.requestPermissions(
                    act,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun initWifiManager() {
        val ctx = context ?: return
        wifiManager = IWifiManager.create(ctx)

        val isOpened = wifiManager?.isOpened == true
        switchWifi?.setOnCheckedChangeListener(null)
        switchWifi?.isChecked = isOpened
        updateWifiState(isOpened)

        switchWifi?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (wifiManager?.isOpened == false) {
                    wifiManager?.openWifi()
                    startScan()
                }
            } else {
                if (wifiManager?.isOpened == true) {
                    wifiManager?.closeWifi()
                }
                updateWifiState(false)
            }
        }

        // 根据连接状态将已连接置顶
        wifiManager?.setOnWifiChangeListener(object : OnWifiChangeListener {
            override fun onWifiChanged(wifis: MutableList<IWifi>?) {
                activity?.runOnUiThread {
                    stopScanAnim()
                    val connectList = mutableListOf<IWifi>()
                    val enableList = mutableListOf<IWifi>()

                    wifis?.forEach {
                        if (it.isConnected) {
                            connectList.add(it)
                        } else {
                            enableList.add(it)
                        }
                    }
                    wifiList.clear()
                    wifiList.addAll(connectList)
                    wifiList.addAll(enableList)
                    adapter?.notifyDataSetChanged()
                    updateEmptyView()
                }
            }
        })

        wifiManager?.setOnWifiStateChangeListener(object : OnWifiStateChangeListener {
            override fun onStateChanged(state: State?) {
                activity?.runOnUiThread {
                    when (state) {
                        State.ENABLED -> {
                            switchWifi?.isChecked = true
                            updateWifiState(true)
                            startScan()
                        }
                        State.DISABLED -> {
                            switchWifi?.isChecked = false
                            updateWifiState(false)
                        }
                        State.ENABLING -> {
                            switchWifi?.isChecked = true
                        }
                        State.DISABLING -> {
                            switchWifi?.isChecked = false
                        }
                        else -> {}
                    }
                }
            }
        })

        wifiManager?.setOnWifiConnectListener(object : OnWifiConnectListener {
            override fun onConnectChanged(status: Boolean) {
                activity?.runOnUiThread {
                    stopScanAnim()
                    adapter?.notifyDataSetChanged()
                    if (status) {
                        isConnecting = false
                        val currentConnected = wifiList.find { it.isConnected }
                        onWifiConnectedListener?.invoke(currentConnected)
                    } else {
                        onWifiDisconnectedListener?.invoke()
                    }
                }
            }

            override fun onConnectTimeout() {
                activity?.runOnUiThread {
                    stopScanAnim()
                    if (isConnecting) {
                        isConnecting = false
                        val toastCtx = context ?: return@runOnUiThread
                        Toast.makeText(
                            toastCtx,
                            getString(R.string.hm_wifi_timeout_tip),
                            Toast.LENGTH_LONG
                        ).show()
                        onWifiConnectTimeoutListener?.invoke()
                    }
                }
            }
        })

        // 注册全局 ConnectivityManager 监听网络连接状态
        try {
            connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d("WifiSettingDialog", "网络已连接")
                    activity?.runOnUiThread {
                        stopScanAnim()
                        adapter?.notifyDataSetChanged()
                    }
                }

                override fun onUnavailable() {
                    Log.d("WifiSettingDialog", "网络不可用")
                    super.onUnavailable()
                    activity?.runOnUiThread {
                        stopScanAnim()
                    }
                }

                override fun onLost(network: Network) {
                    Log.d("WifiSettingDialog", "网络断开")
                    activity?.runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isOpened) {
            startScan()
        }
    }

    private fun startScan() {
        if (wifiManager?.isOpened == true) {
            startScanAnim()
            wifiManager?.scanWifi()
        }
    }

    private fun startScanAnim() {
        if (isScanning) return
        isScanning = true
        pbScanning?.visibility = View.VISIBLE
        ivRefresh?.let { iv ->
            val rotate = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 1000
                repeatCount = Animation.INFINITE
            }
            iv.startAnimation(rotate)
        }
    }

    private fun stopScanAnim() {
        isScanning = false
        pbScanning?.visibility = View.GONE
        ivRefresh?.clearAnimation()
    }

    private fun updateWifiState(isEnabled: Boolean) {
        if (isEnabled) {
            tvEmptyTip?.visibility = if (wifiList.isEmpty()) View.VISIBLE else View.GONE
            tvEmptyTip?.text = getString(R.string.hm_wifi_empty_searching)
            rvWifiList?.visibility = View.VISIBLE
        } else {
            stopScanAnim()
            wifiList.clear()
            adapter?.notifyDataSetChanged()
            tvEmptyTip?.visibility = View.VISIBLE
            tvEmptyTip?.text = getString(R.string.hm_wifi_empty_disabled)
            rvWifiList?.visibility = View.GONE
        }
    }

    private fun updateEmptyView() {
        if (wifiManager?.isOpened == true) {
            if (wifiList.isEmpty()) {
                tvEmptyTip?.visibility = View.VISIBLE
                tvEmptyTip?.text = getString(R.string.hm_wifi_empty_none)
            } else {
                tvEmptyTip?.visibility = View.GONE
            }
        }
    }

    private fun onWifiItemClick(wifi: IWifi) {
        val ctx = context ?: return
        if (wifi.isConnected) {
            // 已连接网络操作
            val ipInfo = if (wifi.ip().isNotEmpty()) " (${wifi.ip()})" else ""
            AlertDialog.Builder(ctx)
                .setTitle(wifi.name())
                .setMessage("${getString(R.string.hm_wifi_connected_tip)}$ipInfo")
                .setPositiveButton(getString(R.string.hm_wifi_disconnect)) { _, _ ->
                    wifiManager?.disConnectWifi()
                    Toast.makeText(ctx, getString(R.string.hm_wifi_disconnecting), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.hm_wifi_forget)) { _, _ ->
                    wifiManager?.removeWifi(wifi)
                    Toast.makeText(ctx, getString(R.string.hm_wifi_forget_success), Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton(getString(R.string.hm_wifi_cancel), null)
                .show()
        } else if (wifi.isSaved) {
            // 已保存网络
            AlertDialog.Builder(ctx)
                .setTitle(wifi.name())
                .setMessage(getString(R.string.hm_wifi_saved_tip))
                .setPositiveButton(getString(R.string.hm_wifi_connect)) { _, _ ->
                    connectWifi(wifi, "")
                }
                .setNegativeButton(getString(R.string.hm_wifi_forget)) { _, _ ->
                    wifiManager?.removeWifi(wifi)
                    Toast.makeText(ctx, getString(R.string.hm_wifi_forget_success), Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton(getString(R.string.hm_wifi_cancel), null)
                .show()
        } else if (!wifi.isEncrypt) {
            // 开放无密码网络
            connectWifi(wifi, "")
        } else {
            // 加密网络，弹出密码输入框
            showPasswordDialog(wifi)
        }
    }

    private fun showPasswordDialog(wifi: IWifi) {
        val ctx = context ?: return
        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wifi_password)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvSsid = dialog.findViewById<TextView>(R.id.tv_pwd_ssid)
        val etPassword = dialog.findViewById<EditText>(R.id.et_wifi_password)
        val cbShowPassword = dialog.findViewById<CheckBox>(R.id.cb_show_password)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnConnect = dialog.findViewById<Button>(R.id.btn_connect)

        tvSsid.text = getString(R.string.hm_wifi_pwd_ssid_prefix, wifi.name())

        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConnect.setOnClickListener {
            val password = etPassword.text.toString().trim()
            if (password.length < 8) {
                Toast.makeText(ctx, getString(R.string.hm_wifi_pwd_length_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            connectWifi(wifi, password)
        }

        dialog.show()

        // 动态适配横竖屏与大屏尺寸，防止密码框和按钮挤压折行
        dialog.window?.let { win ->
            val dm = ctx.resources.displayMetrics
            val density = dm.density
            val screenWidth = dm.widthPixels
            val screenHeight = dm.heightPixels
            val isLandscape = screenWidth > screenHeight
            val targetWidth = if (isLandscape) {
                (screenWidth * 0.55f).toInt().coerceIn(
                    (420 * density).toInt().coerceAtMost(screenWidth),
                    (600 * density).toInt().coerceAtMost(screenWidth)
                )
            } else {
                (screenWidth * 0.90f).toInt().coerceIn(
                    (320 * density).toInt().coerceAtMost(screenWidth),
                    (480 * density).toInt().coerceAtMost(screenWidth)
                )
            }
            win.setLayout(targetWidth, WindowManager.LayoutParams.WRAP_CONTENT)
            win.setGravity(Gravity.CENTER)
            win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun connectWifi(wifi: IWifi, pwd: String = "") {
        val ctx = context ?: return
        isConnecting = true
        Toast.makeText(ctx, getString(R.string.hm_wifi_connecting), Toast.LENGTH_SHORT).show()
        startScanAnim()
        if (wifi.isEncrypt) {
            wifiManager?.connectEncryptWifi(wifi, pwd)
        } else if (wifi.isSaved) {
            wifiManager?.connectSavedWifi(wifi)
        } else {
            wifiManager?.connectOpenWifi(wifi)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            val callback = networkCallback
            if (connectivityManager != null && callback != null) {
                connectivityManager?.unregisterNetworkCallback(callback)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        networkCallback = null
        connectivityManager = null
        wifiManager?.destroy()
        wifiManager = null
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001

        @JvmStatic
        fun show(activity: FragmentActivity): WifiSettingDialog {
            val dialog = WifiSettingDialog()
            dialog.show(activity.supportFragmentManager, "WifiSettingDialog")
            return dialog
        }

        @JvmStatic
        fun show(
            activity: FragmentActivity,
            onConnected: ((wifi: IWifi?) -> Unit)? = null,
            onDisconnected: (() -> Unit)? = null,
            onTimeout: (() -> Unit)? = null
        ): WifiSettingDialog {
            val dialog = WifiSettingDialog().apply {
                this.onWifiConnectedListener = onConnected
                this.onWifiDisconnectedListener = onDisconnected
                this.onWifiConnectTimeoutListener = onTimeout
            }
            dialog.show(activity.supportFragmentManager, "WifiSettingDialog")
            return dialog
        }
    }

    // 内部 RecyclerView 适配器
    private class WifiAdapter(
        private val list: List<IWifi>,
        private val onItemClick: (IWifi) -> Unit
    ) : RecyclerView.Adapter<WifiViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi_list, parent, false)
            return WifiViewHolder(view)
        }

        override fun onBindViewHolder(holder: WifiViewHolder, position: Int) {
            val wifi = list[position]
            val context = holder.itemView.context
            holder.tvName.text = wifi.name()

            // 多语言状态描述信息
            val rawDesc = wifi.description2()
            val localizedDesc = getLocalizedDescription(context, rawDesc, wifi.isConnected)
            if (localizedDesc.isNullOrEmpty()) {
                holder.tvStatus.visibility = View.GONE
            } else {
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvStatus.text = localizedDesc
                if (wifi.isConnected) {
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    holder.tvStatus.setTextColor(Color.parseColor("#888888"))
                }
            }

            // 是否已连接
            holder.ivConnected.visibility = if (wifi.isConnected) View.VISIBLE else View.GONE

            // 是否加密
            holder.ivLock.visibility = if (wifi.isEncrypt) View.VISIBLE else View.GONE

            // 信号强度图标计算 (0~4)
            val signalLevel = getSignalLevel(wifi.level())
            val signalRes = when (signalLevel) {
                4 -> R.drawable.hm_ic_wifi_4
                3 -> R.drawable.hm_ic_wifi_3
                2 -> R.drawable.hm_ic_wifi_2
                1 -> R.drawable.hm_ic_wifi_1
                else -> R.drawable.hm_ic_wifi_0
            }
            holder.ivSignal.setImageResource(signalRes)

            holder.itemView.setOnClickListener {
                onItemClick(wifi)
            }
        }

        override fun getItemCount(): Int = list.size

        private fun getLocalizedDescription(context: Context, raw: String?, isConnected: Boolean): String? {
            if (raw.isNullOrEmpty()) return null
            return when {
                isConnected || raw.contains("已连接") || raw.contains("Connected") ->
                    context.getString(R.string.hm_wifi_status_connected)
                raw.contains("开始连接") || raw.contains("Connecting") ->
                    context.getString(R.string.hm_wifi_status_connecting)
                raw.contains("验证") || raw.contains("Authenticating") ->
                    context.getString(R.string.hm_wifi_status_authenticating)
                raw.contains("IP") || raw.contains("Obtaining") ->
                    context.getString(R.string.hm_wifi_status_obtaining_ip)
                raw.contains("密码错误") || raw.contains("Incorrect password") ->
                    context.getString(R.string.hm_wifi_status_pwd_error)
                raw.contains("已保存") || raw.contains("Saved") ->
                    context.getString(R.string.hm_wifi_status_saved)
                raw.contains("失败") || raw.contains("fail") ->
                    context.getString(R.string.hm_wifi_status_fail)
                else -> raw
            }
        }

        private fun getSignalLevel(rssi: Int): Int {
            return when {
                rssi >= -55 -> 4
                rssi >= -65 -> 3
                rssi >= -75 -> 2
                rssi >= -85 -> 1
                rssi > 0 -> rssi.coerceIn(0, 4)
                else -> 0
            }
        }
    }

    private class WifiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSignal: ImageView = itemView.findViewById(R.id.iv_wifi_signal)
        val ivLock: ImageView = itemView.findViewById(R.id.iv_wifi_lock)
        val tvName: TextView = itemView.findViewById(R.id.tv_wifi_name)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_wifi_status)
        val ivConnected: ImageView = itemView.findViewById(R.id.iv_wifi_connected)
    }
}
