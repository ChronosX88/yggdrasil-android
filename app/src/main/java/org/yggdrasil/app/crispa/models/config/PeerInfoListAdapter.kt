package org.yggdrasil.app.crispa.models.config

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import org.yggdrasil.app.crispa.R
import org.yggdrasil.app.crispa.models.PeerInfo


class PeerInfoListAdapter(
    context: Context,
    allPeers: List<PeerInfo>
) : ArrayAdapter<PeerInfo?> (context, 0, allPeers) {

    private val mContext: Context = context
    private var allPeers: List<PeerInfo> = allPeers

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var peerInfoHolder = PeerInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item, parent, false)
            peerInfoHolder.countryFlag = listItem.findViewById(R.id.countryFlag) as ImageView
            peerInfoHolder.peerInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            listItem.tag = peerInfoHolder
        } else {
            peerInfoHolder = listItem.tag as PeerInfoHolder
        }
        val currentPeer = allPeers[position]
        if(currentPeer.isMeshPeer){
            //TODO set mesh icon
        } else {
            peerInfoHolder.countryFlag.setImageResource(currentPeer.getCountry(mContext)!!.flagID)
        }
        peerInfoHolder.peerInfoText.text = currentPeer.toString()
        return listItem!!
    }

    class PeerInfoHolder {
        lateinit var countryFlag: ImageView
        lateinit var peerInfoText: TextView
    }

}