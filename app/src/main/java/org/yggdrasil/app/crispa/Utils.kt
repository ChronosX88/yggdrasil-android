package org.yggdrasil.app.crispa

import android.content.Context
import com.google.gson.Gson
import config.NodeConfig
import org.yggdrasil.app.crispa.models.config.Config
import java.io.File

val gson = Gson()

fun createNativeYggConfig(config: Config): NodeConfig {
    val nativeConfig = NodeConfig()
    nativeConfig.adminListen = config.adminListen
    nativeConfig.privateKey = config.privateKey
    nativeConfig.publicKey = config.publicKey
    //nativeConfig.ifMTU = config.ifMTU
    nativeConfig.ifName = config.ifName
    //nativeConfig.ifTAPMode = config.ifTAPMode
    nativeConfig.nodeInfoPrivacy = config.nodeInfoPrivacy
    return nativeConfig
}

fun Context.saveYggConfig(config: Config) {
    val configJson = gson.toJson(config)
    val configFile = File(filesDir, "yggdrasil.conf")
    configFile.writeText(configJson)
}