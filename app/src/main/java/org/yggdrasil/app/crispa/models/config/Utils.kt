package org.yggdrasil.app.crispa.models.config

import com.google.gson.Gson
import org.yggdrasil.app.crispa.models.DNSInfo
import org.yggdrasil.app.crispa.models.PeerInfo
import org.acra.ACRA
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI

class Utils {

    companion object {

        @JvmStatic
        fun deserializeStringList2PeerInfoSet(list: List<String>?): MutableSet<PeerInfo> {
            var gson = Gson()
            var out = mutableSetOf<PeerInfo>()
            if (list != null) {
                for(s in list) {
                    out.add(gson.fromJson(s, PeerInfo::class.java))
                }
            }
            return out
        }

        @JvmStatic
        fun deserializeStringList2DNSInfoSet(list: List<String>?): MutableSet<DNSInfo> {
            var gson = Gson()
            var out = mutableSetOf<DNSInfo>()
            if (list != null) {
                for(s in list) {
                    out.add(gson.fromJson(s, DNSInfo::class.java))
                }
            }
            return out
        }

        @JvmStatic
        fun deserializeStringSet2PeerInfoSet(list: Set<String>): MutableSet<PeerInfo> {
            var gson = Gson()
            var out = mutableSetOf<PeerInfo>()
            for(s in list) {
                out.add(gson.fromJson(s, PeerInfo::class.java))
            }
            return out
        }

        @JvmStatic
        fun deserializeStringSet2DNSInfoSet(list: Set<String>): MutableSet<DNSInfo> {
            var gson = Gson()
            var out = mutableSetOf<DNSInfo>()
            for(s in list) {
                out.add(gson.fromJson(s, DNSInfo::class.java))
            }
            return out
        }

        @JvmStatic
        fun serializePeerInfoSet2StringList(list: Set<PeerInfo>): ArrayList<String> {
            var gson = Gson()
            var out = ArrayList<String>()
            for(p in list) {
                out.add(gson.toJson(p))
            }
            return out
        }

        @JvmStatic
        fun serializeDNSInfoSet2StringList(list: Set<DNSInfo>): ArrayList<String> {
            var gson = Gson()
            var out = ArrayList<String>()
            for(p in list) {
                out.add(gson.toJson(p))
            }
            return out
        }

        @JvmStatic
        fun ping(hostname: String, port:Int): Int {
            val start = System.currentTimeMillis()
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(hostname, port), 5000)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                print(hostname)
                return Int.MAX_VALUE
            }
            return (System.currentTimeMillis() - start).toInt()
        }

        @JvmStatic
        fun convertPeerInfoSet2PeerIdSet(list: Set<PeerInfo>): Set<String> {
            var out = mutableSetOf<String>()
            for(p in list) {
                out.add(p.toString())
            }
            return out
        }

        @JvmStatic
        fun convertPeer2PeerStringList(list: List<Peer>): ArrayList<String> {
            var out = ArrayList<String>()
            var gson = Gson()
            for(p in list) {
                out.add(gson.toJson(p))
            }
            return out
        }

        @JvmStatic
        fun deserializePeerStringList2PeerInfoSet(list: List<String>?): MutableSet<PeerInfo> {
            var gson = Gson()
            ACRA.errorReporter.putCustomData("Peer list", gson.toJson(list))
            var out = mutableSetOf<PeerInfo>()
            if (list != null) {
                for(s in list) {
                    var p = gson.fromJson(s, Peer::class.java)
                    var fixWlanPart = p.remote.substring(p.remote.indexOf('%'), p.remote.indexOf(']'))
                    var fixedUrlString = p.remote.replace(fixWlanPart, "")
                    var url = URI(fixedUrlString)
                    out.add(
                        PeerInfo(
                            url.scheme,
                            InetAddress.getByName(url.host),
                            url.port,
                            null,
                            true
                        )
                    )
                }
            }
            return out
        }
    }
}