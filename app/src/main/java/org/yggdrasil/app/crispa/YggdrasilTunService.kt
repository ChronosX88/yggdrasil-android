package org.yggdrasil.app.crispa

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.yggdrasil.app.crispa.models.DNSInfo
import org.yggdrasil.app.crispa.models.PeerInfo
import org.yggdrasil.app.crispa.models.config.Peer
import org.yggdrasil.app.crispa.models.config.Utils.Companion.convertPeer2PeerStringList
import org.yggdrasil.app.crispa.models.config.Utils.Companion.convertPeerInfoSet2PeerIdSet
import org.yggdrasil.app.crispa.models.config.Utils.Companion.deserializeStringList2DNSInfoSet
import org.yggdrasil.app.crispa.models.config.Utils.Companion.deserializeStringList2PeerInfoSet
import mobile.Mobile
import mobile.Yggdrasil
import org.acra.ACRA
import java.io.*
import java.net.Inet6Address
import kotlin.concurrent.thread

class YggdrasilTunService : VpnService() {

    private lateinit var ygg: Yggdrasil
    private lateinit var tunInputStream: InputStream
    private lateinit var tunOutputStream: OutputStream
    private lateinit var address: String
    private var isClosed = false

    /** Maximum packet size is constrained by the MTU, which is given as a signed short/2 */
    private val MAX_PACKET_SIZE = Short.MAX_VALUE/2
    private var tunInterface: ParcelFileDescriptor? = null

    companion object {
        private const val TAG = "Yggdrasil-service"
        public const val IS_VPN_SERVICE_STOPPED = "VPN_STATUS"
    }

    private val FOREGROUND_ID = 1338

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pi: PendingIntent? = intent?.getParcelableExtra(MainActivity.PARAM_PINTENT)
        when(intent?.getStringExtra(MainActivity.COMMAND)){
            MainActivity.STOP ->{
                stopVpn(pi)
                foregroundNotification(FOREGROUND_ID, "Yggdrasil service stopped")
            }
            MainActivity.START ->{
                val peers = deserializeStringList2PeerInfoSet(intent.getStringArrayListExtra(MainActivity.CURRENT_PEERS))
                val dns = deserializeStringList2DNSInfoSet(intent.getStringArrayListExtra(MainActivity.CURRENT_DNS))
                val staticIP: Boolean = intent.getBooleanExtra(MainActivity.STATIC_IP, false)
                ygg = Yggdrasil()
                setupTunInterface(pi, peers, dns, staticIP)
                foregroundNotification(FOREGROUND_ID, "Yggdrasil service started")
            }
            MainActivity.UPDATE_DNS ->{
                val dns = deserializeStringList2DNSInfoSet(intent.getStringArrayListExtra(MainActivity.CURRENT_DNS))
                setupIOStreams(dns)
            }
            MainActivity.UPDATE_PEERS ->{
                sendMeshPeerStatus(pi)
            }
        }

        return START_NOT_STICKY
    }

    private fun setupIOStreams(dns: MutableSet<DNSInfo>){
        address = ygg.addressString

        var builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Builder()
                .addAddress(address, 7)
                .allowFamily(OsConstants.AF_INET)
                .allowBypass()
                .setBlocking(true)
                .setMtu(MAX_PACKET_SIZE)
        } else {
            Builder()
                .addAddress(address, 7)
                .setMtu(MAX_PACKET_SIZE)
        }
        if (dns.size > 0) {
            for (d in dns) {
                builder.addDnsServer(d.address)
            }
        }
        /*
        fix for DNS unavailability
         */
        if(!hasIpv6DefaultRoute()){
            builder.addRoute("2000::",3)
        }
        tunInterface = builder.establish()
        tunInputStream = FileInputStream(tunInterface!!.fileDescriptor)
        tunOutputStream = FileOutputStream(tunInterface!!.fileDescriptor)
    }

    private fun setupTunInterface(
        pi: PendingIntent?,
        peers: Set<PeerInfo>,
        dns: MutableSet<DNSInfo>,
        staticIP: Boolean
    ) {
        pi!!.send(MainActivity.STATUS_START)
        var configJson = Mobile.generateConfigJSON()
        val gson = Gson()
        var config = gson.fromJson(String(configJson), Map::class.java).toMutableMap()
        config = fixConfig(config, peers, staticIP)

        configJson = gson.toJson(config).toByteArray()

        ygg.startJSON(configJson)

        setupIOStreams(dns)

        thread(start = true) {
            val buffer = ByteArray(MAX_PACKET_SIZE)
            while (!isClosed) {
                readPacketsFromTun(buffer)
            }
        }
        thread(start = true) {
            while (!isClosed) {
                writePacketsToTun()
            }
        }
        val intent: Intent = Intent().putExtra(MainActivity.IPv6, address)
        pi.send(this, MainActivity.STATUS_FINISH, intent)
    }

    private fun sendMeshPeerStatus(pi: PendingIntent?){
        class Token : TypeToken<List<Peer>>()
        ygg.addressString
        ACRA.errorReporter.putCustomData("Peers JSON", ygg.peersJSON)
        var meshPeers: List<Peer> = gson.fromJson(ygg.peersJSON, Token().type)
        val intent: Intent = Intent().putStringArrayListExtra(
            MainActivity.MESH_PEERS,
            convertPeer2PeerStringList(meshPeers)
        );
        pi?.send(this, MainActivity.STATUS_PEERS_UPDATE, intent)
    }

    private fun fixConfig(
        config: MutableMap<Any?, Any?>,
        peers: Set<PeerInfo>,
        staticIP: Boolean
    ): MutableMap<Any?, Any?> {

        val whiteList = arrayListOf<String>()
        whiteList.add("")
        val blackList = arrayListOf<String>()
        blackList.add("")
        config["Peers"] = convertPeerInfoSet2PeerIdSet(peers)
        config["Listen"] = arrayListOf<String>()
        config["AdminListen"] = "tcp://localhost:9001"
        config["IfName"] = "tun0"
        if(staticIP) {
            val preferences =
                PreferenceManager.getDefaultSharedPreferences(this.baseContext)
            if(preferences.getString(MainActivity.STATIC_IP, null)==null) {
                val publicKey = config["PublicKey"].toString()
                val privateKey = config["PrivateKey"].toString()
                preferences.edit()
                    .putString(MainActivity.privateKey, privateKey)
                    .putString(MainActivity.publicKey, publicKey)
                    .putString(MainActivity.STATIC_IP, MainActivity.STATIC_IP).apply()
            } else {
                val privateKey = preferences.getString(MainActivity.privateKey, null)
                val publicKey = preferences.getString(MainActivity.publicKey, null)

                config["PrivateKey"] = privateKey
                config["PublicKey"] = publicKey
            }
        }
        //(config["SessionFirewall"] as MutableMap<Any, Any>)["AllowFromDirect"] = true
        //(config["SessionFirewall"] as MutableMap<Any, Any>)["AllowFromRemote"] = true
        //(config["SessionFirewall"] as MutableMap<Any, Any>)["AlwaysAllowOutbound"] = true
        //(config["SessionFirewall"] as MutableMap<Any, Any>)["WhitelistEncryptionPublicKeys"] = whiteList
        //(config["SessionFirewall"] as MutableMap<Any, Any>)["BlacklistEncryptionPublicKeys"] = blackList

        //(config["SwitchOptions"] as MutableMap<Any, Any>)["MaxTotalQueueSize"] = 4194304
        if (config["AutoStart"] == null) {
            val tmpMap = emptyMap<String, Boolean>().toMutableMap()
            tmpMap["WiFi"] = false
            tmpMap["Mobile"] = false
            config["AutoStart"] = tmpMap
        }
        return config
    }

    private fun readPacketsFromTun(buffer: ByteArray) {
        try {
            // Read the outgoing packet from the input stream.
            val length = tunInputStream.read(buffer)
            ygg.send(buffer.sliceArray(IntRange(0, length - 1)))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception){
            e.printStackTrace();
        }
    }

    private fun writePacketsToTun() {
        val buffer = ygg.recv()
        if(buffer!=null) {
            try {
                tunOutputStream.write(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception){
                e.printStackTrace();
            }
        }
    }

    private fun stopVpn(pi: PendingIntent?) {
        isClosed = true;
        tunInputStream.close()
        tunOutputStream.close()
        tunInterface!!.close()
        Log.d(TAG,"Stop is running from service")
        ygg.stop()
        val intent: Intent = Intent()
        pi!!.send(this, MainActivity.STATUS_STOP, intent)
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }

    private fun hasIpv6DefaultRoute(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networks = cm.allNetworks

            for (network in networks) {
                val linkProperties = cm.getLinkProperties(network)
                if(linkProperties!=null) {
                    val routes = linkProperties.routes
                    for (route in routes) {
                        if (route.isDefaultRoute && route.gateway is Inet6Address) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun foregroundNotification(FOREGROUND_ID: Int, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel(TAG, "Yggdrasil service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra(IS_VPN_SERVICE_STOPPED, isClosed);
            var stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntentWithParentStack(intent)
            var pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            val b = NotificationCompat.Builder(this, channelId)
            b.setOngoing(true)
                .setContentIntent(pi)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
            startForeground(FOREGROUND_ID, b.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = getColor(R.color.dark_10)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}
