package io.github.chronosx88.yggdrasil

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.chronosx88.yggdrasil.models.config.NodeInfoListAdapter

class CopyLocalNodeInfoActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copy_local_node_info)
        setSupportActionBar(findViewById(R.id.toolbar))
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(this.baseContext)
        val ipv6Address = intent.extras!!.getString(MainActivity.IPv6, "")
        val publicKey = preferences.getString(MainActivity.publicKey, "")
        var nodeInfoListView = findViewById<ListView>(R.id.nodeInfoList)
        val nodeInfoList = listOf<NodeInfo>(NodeInfo("IP address", ipv6Address!!), NodeInfo("Public Key", publicKey!!));
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