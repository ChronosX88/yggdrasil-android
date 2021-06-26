package org.yggdrasil.app.crispa

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.URLUtil
import android.widget.Button
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.BuildConfig
import com.hbb20.CCPCountry
import com.vincentbrison.openlibraries.android.dualcache.Builder
import com.vincentbrison.openlibraries.android.dualcache.SizeOf
import com.vincentbrison.openlibraries.android.dualcache.JsonSerializer
import org.yggdrasil.app.crispa.models.PeerInfo
import org.yggdrasil.app.crispa.models.Status
import org.yggdrasil.app.crispa.models.config.DropDownAdapter
import org.yggdrasil.app.crispa.models.config.SelectPeerInfoListAdapter
import org.yggdrasil.app.crispa.models.config.Utils.Companion.deserializeStringList2PeerInfoSet
import org.yggdrasil.app.crispa.models.config.Utils.Companion.ping
import org.yggdrasil.app.crispa.models.config.Utils.Companion.serializePeerInfoSet2StringList
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.net.InetAddress
import java.net.URI
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.Charset


class PeerListActivity : AppCompatActivity() {

    companion object {
        const val PEER_LIST = "PEER_LIST"
        const val PEER_LIST_URL = "http://y4.rivchain.org/rest/peers.json"
        const val CACHE_NAME = "PEER_LIST_CACHE"
        const val ONLINE_PEERINFO_LIST = "online_peer_info_list"
        const val OFFLINE_PEERINFO_LIST = "offline_peer_info_list"
        const val TEST_APP_VERSION = BuildConfig.VERSION_CODE
        const val RAM_MAX_SIZE = 100000
        const val DISK_MAX_SIZE = 100000
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

    private var peerListUrl = PEER_LIST_URL
    private var peerListPing = true
    var popup: PopupWindow? = null
    var adapter: DropDownAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peer_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            addNewPeer()
        }
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(this.baseContext)
        var peerListUrl: String =
            preferences.getString(PEER_LIST, "")!!
        if(!peerListUrl.isNullOrBlank()){
            this@PeerListActivity.peerListUrl = peerListUrl
        }
        var extras = intent.extras
        var peerList = findViewById<ListView>(R.id.peerList)
        var adapter = SelectPeerInfoListAdapter(this, arrayListOf(), mutableSetOf())
        peerList.adapter = adapter
        var peerInfoListCache = Builder<List<PeerInfo>>(CACHE_NAME, TEST_APP_VERSION)
            .enableLog()
            .useReferenceInRam(RAM_MAX_SIZE, SizeOfPeerList())
            .useSerializerInDisk(
                DISK_MAX_SIZE, true,
                JsonSerializer(ArrayList<PeerInfo>().javaClass), baseContext
            ).build();

        GlobalScope.launch() {
            try {
                var cp = deserializeStringList2PeerInfoSet(
                    extras!!.getStringArrayList(MainActivity.PEER_LIST)!!
                )
                for (pi in cp) {
                    var ping = ping(pi.hostName, pi.port)
                    pi.ping = ping
                }
                try {
                    var peerInfoCache = peerInfoListCache.get(ONLINE_PEERINFO_LIST)
                    if (peerInfoCache != null && peerInfoCache.isNotEmpty()) {
                        for (peerInfo in peerInfoCache) {
                            var ping = ping(peerInfo.hostName, peerInfo.port)
                            peerInfo.ping = ping
                            if (cp.contains(peerInfo)) {
                                continue
                            }
                            withContext(Dispatchers.Main) {
                                adapter.addItem(peerInfo)
                                if (adapter.count % 5 == 0) {
                                    adapter.sort()
                                }
                            }
                        }
                    }
                    var json = downloadJson(this@PeerListActivity.peerListUrl)
                    var countries = CCPCountry.getLibraryMasterCountriesEnglish()
                    val mapType: Type = object :
                        TypeToken<Map<String?, Map<String, Status>>>() {}.type
                    val peersMap: Map<String, Map<String, Status>> = Gson().fromJson(json, mapType)
                    var cachePeerInfoList = mutableListOf<PeerInfo>()
                    for ((country, peers) in peersMap.entries) {
                        for ((peer, status) in peers) {
                            if (status.up) {
                                for (ccp in countries) {
                                    if (ccp.name.toLowerCase()
                                            .contains(country.replace(".md", "").replace("-", " "))
                                    ) {
                                        if(!peerListPing){
                                            return@launch
                                        }
                                        var url = URI(peer)
                                        try {
                                            var address = InetAddress.getByName(url.host)
                                            var peerInfo =
                                                PeerInfo(
                                                    url.scheme,
                                                    address,
                                                    url.port,
                                                    ccp.nameCode
                                                )
                                            var ping = ping(url.host, url.port)
                                            peerInfo.ping = ping
                                            if (cp.contains(peerInfo)) {
                                                continue
                                            }
                                            if (peerInfo.ping < Int.MAX_VALUE) {
                                                cachePeerInfoList.add(peerInfo)
                                            }
                                            withContext(Dispatchers.Main) {
                                                adapter.addItem(peerInfo)
                                                if (adapter.count % 5 == 0) {
                                                    adapter.sort()
                                                    if (cachePeerInfoList.size > 0) {
                                                        peerInfoListCache.put(
                                                            ONLINE_PEERINFO_LIST,
                                                            cachePeerInfoList.toList()
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: Throwable) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is FileNotFoundException, is UnknownHostException -> {
                            var onlinePeerInfoList = peerInfoListCache.get(ONLINE_PEERINFO_LIST)
                            if (onlinePeerInfoList != null) {
                                for (peerInfo in onlinePeerInfoList) {
                                    var ping = ping(peerInfo.hostName, peerInfo.port)
                                    peerInfo.ping = ping
                                    if (cp.contains(peerInfo)) {
                                        continue
                                    }
                                    withContext(Dispatchers.Main) {
                                        adapter.addItem(peerInfo)
                                        if (adapter.count % 5 == 0) {
                                            adapter.sort()
                                        }
                                    }
                                }
                            }
                            e.printStackTrace()
                        }
                        else -> e.printStackTrace()
                    }
                }
                var currentPeers = ArrayList(cp.sortedWith(compareBy { it.ping }))
                withContext(Dispatchers.Main) {
                    adapter.addAll(0, currentPeers)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun editPeerListUrl() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.edit_peer_list_url_dialog, null)
        val ab: AlertDialog.Builder = AlertDialog.Builder(this)
        ab.setCancelable(true).setView(view)
        var ad = ab.show()
        var saveButton = view.findViewById<Button>(R.id.save)
        var urlInput = view.findViewById<TextView>(R.id.urlInput)
        urlInput.text = peerListUrl
        saveButton.setOnClickListener{

            var url = urlInput.text.toString()
            if(!URLUtil.isValidUrl(url)){
                urlInput.error = "The URL is invalid!"
                return@setOnClickListener;
            }
            peerListUrl = url
            val preferences =
                PreferenceManager.getDefaultSharedPreferences(this.baseContext)
            preferences.edit().putString(PEER_LIST, peerListUrl).apply()
            ad.dismiss()
        }
    }

    private fun addNewPeer() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.new_peer_dialog, null)
        val countryCode: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.resources.configuration.locales[0].country
        } else {
            this.resources.configuration.locale.country
        }
        var schemaInput = view.findViewById<TextView>(R.id.schemaInput)
        var ipInput = view.findViewById<TextView>(R.id.ipInput)
        ipInput.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            schemaInput.showSoftInputOnFocus = false
        }
        schemaInput.setOnFocusChangeListener { v, _ ->
            if(schemaInput.isFocused) {
                onClickSchemaList(v)
            }
        }
        schemaInput.setOnClickListener { v->
            onClickSchemaList(v)
        }
        getPopupWindow(
            R.layout.spinner_item,
            resources.getStringArray(R.array.schemas),
            schemaInput
        );
        view.findViewById<com.hbb20.CountryCodePicker>(R.id.ccp).setCountryForNameCode(countryCode)
        val ab: AlertDialog.Builder = AlertDialog.Builder(this)
        ab.setCancelable(true).setView(view)
        var ad = ab.show()
        var addButton = view.findViewById<Button>(R.id.add)
        addButton.setOnClickListener{
            var portInput = view.findViewById<TextView>(R.id.portInput)
            var ccpInput = view.findViewById<com.hbb20.CountryCodePicker>(R.id.ccp)
            var schema = schemaInput.text.toString().toLowerCase()
            if(schema.isEmpty()){
                schemaInput.error = "Schema is required"
            }
            var ip = ipInput.text.toString().toLowerCase()
            if(ip.isEmpty()){
                ipInput.error = "IP address is required"
            }
            var port = portInput.text.toString().toInt()
            if(port<=0){
                portInput.error = "Port should be > 0"
            }
            if(port>=Short.MAX_VALUE){
                portInput.error = "Port should be < "+Short.MAX_VALUE
            }
            var ccp = ccpInput.selectedCountryNameCode
            GlobalScope.launch {
                var pi = PeerInfo(schema, InetAddress.getByName(ip), port, ccp)
                try {
                    var ping = ping(pi.hostName, pi.port)
                    pi.ping = ping
                } catch (e: Throwable){
                    pi.ping = Int.MAX_VALUE
                }
                withContext(Dispatchers.Main) {
                    var selectAdapter = (findViewById<ListView>(R.id.peerList).adapter as SelectPeerInfoListAdapter)
                    selectAdapter.addItem(0, pi)
                    selectAdapter.notifyDataSetChanged()
                    ad.dismiss()
                }
            }
        }
    }

    private fun onClickSchemaList(v: View) {
        val height = -1 * v.height +30
        getAddressListPopup()?.showAsDropDown(v, -5, height)
    }

    private fun getAddressListPopup(): PopupWindow? {
        return popup
    }

    private fun getPopupWindow(
        textViewResourceId: Int,
        objects: Array<String>,
        editText: TextView
    ): PopupWindow? {
        // initialize a pop up window type
        val popupWindow = PopupWindow(this)
        // the drop down list is a list view
        val listView = ListView(this)
        listView.dividerHeight = 0
        // set our adapter and pass our pop up window contents
        adapter = DropDownAdapter(this, textViewResourceId, objects, popupWindow, editText)
        listView.adapter = adapter
        // set the item click listener
        listView.onItemClickListener = adapter
        // some other visual settings
        popupWindow.isFocusable = true
        popupWindow.width = 320
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        // set the list view as pop up window content
        popupWindow.contentView = listView
        popup = popupWindow
        return popupWindow
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_peers, menu)
        val item = menu.findItem(R.id.saveItem) as MenuItem
        item.setActionView(R.layout.menu_save)
        val saveButton = item
            .actionView.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveButton.isClickable = false
            cancelPeerListPing()
            val result = Intent(this, MainActivity::class.java)
            var adapter = findViewById<ListView>(R.id.peerList).adapter as SelectPeerInfoListAdapter
            val selectedPeers = adapter.getSelectedPeers()
            result.putExtra(MainActivity.PEER_LIST, serializePeerInfoSet2StringList(selectedPeers))
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        val editUrl = menu.findItem(R.id.editUrlItem) as MenuItem
        editUrl.setActionView(R.layout.menu_edit_url)
        val editUrlButton = editUrl
            .actionView.findViewById<Button>(R.id.editUrlButton)
        editUrlButton.setOnClickListener {
            editPeerListUrl()
        }
        return true
    }

    private fun cancelPeerListPing() {
        peerListPing = false
    }

    override fun onStop() {
        super.onStop()
        cancelPeerListPing()
    }
}

class SizeOfPeerList: SizeOf<List<PeerInfo>> {

    override fun sizeOf(obj: List<PeerInfo>): Int{
        var size = 0
        for (o in obj) {
            if (o.hostName != null) {
                size += o.hostName.length * 2
            }
            if (o.schema != null) {
                size += o.schema.length * 2
            }
            if (o.countryCode != null) {
                size += o.countryCode!!.length * 2
            }
            size += 4
            size += 4
            size += 1
        }
        return size
    }
}
