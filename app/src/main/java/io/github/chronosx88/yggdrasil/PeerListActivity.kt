package io.github.chronosx88.yggdrasil

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CCPCountry
import io.github.chronosx88.yggdrasil.models.PeerInfo
import io.github.chronosx88.yggdrasil.models.Status
import io.github.chronosx88.yggdrasil.models.config.PeerInfoListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URI
import java.net.URL
import java.nio.charset.Charset


class PeerListActivity : AppCompatActivity() {

    companion object {
        const val PEER_LIST_URL = "https://publicpeers.neilalexander.dev/publicnodes.json"

        var allPeers = arrayListOf(
            PeerInfo(
                "tcp",
                Inet4Address.getByName("194.177.21.156"),
                5066,
                "RU"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("46.151.26.194"),
                60575,
                "RU"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("188.226.125.64"),
                54321,
                "RU"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("88.201.129.205"),
                8777,
                "RU"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("45.11.19.26"),
                5001,
                "DE"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("82.165.69.111"),
                61216,
                "DE"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("104.248.15.125"),
                31337,
                "US"
            ),
            PeerInfo(
                "tcp",
                Inet4Address.getByName("108.175.10.127"),
                61216,
                "US"
            )
        )
    }

    fun downloadJson(link: String): String {
        URL(link).openStream().use { input ->
            var outStream = ByteArrayOutputStream()
            outStream.use { output ->
                input.copyTo(output)
            }
            return String(outStream.toByteArray(), Charset.forName("UTF-8"))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peer_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        var extras = intent.extras
        var peerList = findViewById<ListView>(R.id.peerList)
        var instance = this
        GlobalScope.launch {
            var json = downloadJson(PEER_LIST_URL)
            var countries = CCPCountry.getLibraryMasterCountriesEnglish()
            val mapType: Type = object :
                TypeToken<Map<String?, Map<String, Status>>>() {}.type
            val peersMap: Map<String, Map<String, Status>> = Gson().fromJson(json, mapType)
            val allOnlinePeers = arrayListOf<PeerInfo>()
            for ((country, peers) in peersMap.entries) {
                println("$country:")
                for ((peer, status) in peers) {
                    if(status.up){
                        for (ccp in countries){
                            if(ccp.name.toLowerCase().contains(country.replace(".md",""))){
                                var url = URI(peer)
                                var peerInfo = PeerInfo(url.scheme, InetAddress.getByName(url.host), url.port, ccp.nameCode)
                                allOnlinePeers.add(peerInfo)
                            }
                        }
                    }
                }
            }
            if(allOnlinePeers.size>0){
                allPeers = allOnlinePeers
            }

            if (extras != null) {
                var cp = extras.getStringArrayList(MainActivity.PEER_LIST)!!
                var currentPeers = ArrayList(cp)
                for(peerInfo in allPeers){
                    if(currentPeers.contains(peerInfo.toString())){
                        currentPeers.remove(peerInfo.toString())
                    }
                }
                for(cp in currentPeers){
                    var url = URI(cp)
                    var peerInfo = PeerInfo(url.scheme, InetAddress.getByName(url.host), url.port, "RU")
                    allPeers.add(0, peerInfo)
                }
                var adapter = PeerInfoListAdapter(instance, allOnlinePeers, cp)
                withContext(Dispatchers.Main) {
                    peerList.adapter = adapter
                }
            } else {
                var adapter = PeerInfoListAdapter(instance, allOnlinePeers, ArrayList())
                withContext(Dispatchers.Main) {
                    peerList.adapter = adapter
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_peers, menu)
        val item = menu.findItem(R.id.saveItem) as MenuItem
        item.setActionView(R.layout.menu_save)
        val saveButton = item
            .actionView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val result = Intent(this, MainActivity::class.java)
            var adapter = findViewById<ListView>(R.id.peerList).adapter as PeerInfoListAdapter
            val selectedPeers = adapter.getSelectedPeers()
            if(selectedPeers.size>0) {
                result.putExtra(MainActivity.PEER_LIST, adapter.getSelectedPeers())
                setResult(Activity.RESULT_OK, result)
                finish()
            } else {
                val text = "Select at least one peer"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }

        }
        return true
    }
}