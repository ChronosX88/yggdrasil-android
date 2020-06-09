package io.github.chronosx88.yggdrasil

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.google.gson.Gson
import dummy.ConduitEndpoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mobile.Mobile
import mobile.Yggdrasil
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.or


class YggdrasilTunService : VpnService() {
    companion object {
        private var isRunning: Boolean = false
    }
    private var tunInterface: ParcelFileDescriptor? = null
    private lateinit var yggConduitEndpoint: ConduitEndpoint
    private var tunInputStream: InputStream? = null
    private var tunOutputStream: OutputStream? = null
    private lateinit var readCoroutine: CoroutineContext
    private lateinit var writeCoroutine: CoroutineContext

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        setupTunInterface()
    }

    private fun setupTunInterface() {
        val builder = Builder()
        val ygg = Yggdrasil()
        var configJson = Mobile.generateConfigJSON()
        val gson = Gson()

        var config = gson.fromJson(String(configJson), Map::class.java).toMutableMap()
        config = fixConfig(config)
        configJson = gson.toJson(config).toByteArray()

        yggConduitEndpoint = ygg.startJSON(configJson)
        val address = ygg.addressString // hack for getting generic ipv6 string from NodeID

        tunInterface = builder
            .addAddress(address, 7)
            .addRoute("0200::", 7)
            .establish()

        tunInputStream = FileInputStream(tunInterface!!.fileDescriptor)
        tunOutputStream = FileOutputStream(tunInterface!!.fileDescriptor)
        readCoroutine = GlobalScope.launch {
            // FIXME it will throw exception (bad file descriptor) when coroutine will be canceled
            while (true) {
                readPacketsFromTun()
            }
        }
        writeCoroutine = GlobalScope.launch {
            while (true) {
                writePacketsToTun()
            }
        }
    }

    private fun fixConfig(config: MutableMap<Any?, Any?>): MutableMap<Any?, Any?> {
        config["Listen"] = ""
        config["AdminListen"] = "tcp://localhost:9001"
        config["IfName"] = "dummy"
        (config["SessionFirewall"] as MutableMap<Any, Any>)["Enable"] = true
        (config["SwitchOptions"] as MutableMap<Any, Any>)["MaxTotalQueueSize"] = 1048576
        if (config["AutoStart"] == null) {
            val tmpMap = emptyMap<String, Boolean>().toMutableMap()
            tmpMap["WiFi"] = false
            tmpMap["Mobile"] = false
            config["AutoStart"] = tmpMap
        }
        return config
    }

    private fun readPacketsFromTun() {
        if(tunInputStream != null) {
            val buffer = ByteArray(1024)
            tunInputStream!!.read(buffer)
            if (!isBufferEmpty(buffer)) {
                yggConduitEndpoint.send(buffer)
            }
        }
    }

    private fun isBufferEmpty(buffer: ByteArray): Boolean {
        var sum: Byte = 0
        for (i in buffer) {
            sum.or(i)
        }
        return sum == 0.toByte()
    }

    private fun writePacketsToTun() {
        if(tunOutputStream != null) {
            val buffer = yggConduitEndpoint.recv()
            if (!isBufferEmpty(buffer)) {
                tunOutputStream!!.write(buffer)
            }
        }
    }

    override fun onRevoke() {
        super.onRevoke()
        isRunning = false
        readCoroutine.cancel()
        writeCoroutine.cancel()
        tunInterface!!.close()
        tunInterface = null
    }
}