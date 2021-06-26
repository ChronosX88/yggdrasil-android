package org.yggdrasil.app.crispa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.yggdrasil.app.crispa.models.NodeInfo
import org.yggdrasil.app.crispa.models.config.NodeInfoListAdapter

class CopyLocalNodeInfoActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copy_local_node_info)
        setSupportActionBar(findViewById(R.id.toolbar))
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(this.baseContext)
        val ipv6Address = intent.extras!!.getString(MainActivity.IPv6, "")
        val publicKey = preferences.getString(MainActivity.publicKey, "")
        var nodeInfoListView = findViewById<RecyclerView>(R.id.node_info_list)
        val nodeInfoList = listOf(NodeInfo("IP address", ipv6Address!!), NodeInfo("Public Key", publicKey!!));
        val adapter =
            NodeInfoListAdapter(
                this,
                nodeInfoList.toTypedArray()
            )

        nodeInfoListView.adapter = adapter
        nodeInfoListView.layoutManager = LinearLayoutManager(this)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}