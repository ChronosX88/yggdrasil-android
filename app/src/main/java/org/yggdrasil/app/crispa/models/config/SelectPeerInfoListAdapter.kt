package org.yggdrasil.app.crispa.models.config

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.yggdrasil.app.crispa.R
import org.yggdrasil.app.crispa.models.PeerInfo

class SelectPeerInfoListAdapter(
    context: Context,
    allPeers: List<PeerInfo>,
    currentPeers: MutableSet<PeerInfo>
) : ArrayAdapter<PeerInfo?> (context, 0, allPeers) {

    private val mContext: Context = context
    private var allPeers: MutableList<PeerInfo> = allPeers as MutableList<PeerInfo>
    private var currentPeers: MutableSet<PeerInfo> = currentPeers

    override fun getItem(position: Int): PeerInfo? {
        return allPeers.get(position)
    }

    override fun getCount(): Int {
        return allPeers.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var peerInfoHolder = PeerInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item_edit, parent, false)
            peerInfoHolder.checkbox = listItem.findViewById(R.id.checkbox) as CheckBox
            peerInfoHolder.countryFlag = listItem.findViewById(R.id.countryFlag) as ImageView
            peerInfoHolder.peerInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            peerInfoHolder.ping = listItem.findViewById(R.id.ping) as TextView
            listItem.tag = peerInfoHolder
        } else {
            peerInfoHolder = listItem.tag as PeerInfoHolder
        }
        val currentPeer = allPeers[position]
        peerInfoHolder.countryFlag.setImageResource(currentPeer.getCountry(mContext)!!.flagID)
        val peerId = currentPeer.toString()
        peerInfoHolder.peerInfoText.text = peerId
        if(currentPeer.ping == Int.MAX_VALUE){
            peerInfoHolder.ping.text=""
            peerInfoHolder.peerInfoText.setTextColor(Color.GRAY)
        } else {
            peerInfoHolder.ping.text = currentPeer.ping.toString() + " ms"
            peerInfoHolder.peerInfoText.setTextColor(Color.WHITE)
        }
        peerInfoHolder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!currentPeers.contains(currentPeer)) {
                    currentPeers.add(currentPeer)
                }
            } else {
                if (currentPeers.contains(currentPeer)) {
                    currentPeers.remove(currentPeer)
                }
            }
        }
        peerInfoHolder.peerInfoText.setOnClickListener {
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                ClipData.newPlainText("Peer info", peerId)
            clipboard.setPrimaryClip(clip)
            showToast(peerId + " " + context.getString(R.string.node_info_copied))
        }
        peerInfoHolder.checkbox.isChecked = this.currentPeers.contains(currentPeer)
        return listItem!!
    }

    fun getSelectedPeers(): Set<PeerInfo> {
        return currentPeers
    }

    fun getAllPeers(): List<PeerInfo> {
        return allPeers
    }

    fun addItem(peerInfo: PeerInfo){
        if(!allPeers.contains(peerInfo)){
            allPeers.add(peerInfo)
        }
    }

    fun addItem(index: Int, peerInfo: PeerInfo){
        allPeers.add(index, peerInfo)
    }

    fun addAll(index: Int, peerInfo: ArrayList<PeerInfo>){
        currentPeers.addAll(peerInfo)
        allPeers.removeAll(peerInfo)
        allPeers.addAll(index, peerInfo)
        this.notifyDataSetChanged()
    }

    fun sort(){
        allPeers = ArrayList(allPeers.sortedWith(compareBy { it.ping }))
        this.notifyDataSetChanged()
    }

    class PeerInfoHolder {
        lateinit var checkbox: CheckBox
        lateinit var countryFlag: ImageView
        lateinit var peerInfoText: TextView
        lateinit var ping: TextView
    }

    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, text, duration)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}