package org.yggdrasil.app.crispa.models.config

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import org.yggdrasil.app.crispa.R
import org.yggdrasil.app.crispa.models.DNSInfo


class DNSInfoListAdapter(
    context: Context,
    allDNS: List<DNSInfo>
) : ArrayAdapter<DNSInfo?> (context, 0, allDNS) {

    private val mContext: Context = context
    private var allDNS: List<DNSInfo> = allDNS

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var dnsInfoHolder = DNSInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item, parent, false)
            dnsInfoHolder.countryFlag = listItem.findViewById(R.id.countryFlag) as ImageView
            dnsInfoHolder.dnsInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            listItem.tag = dnsInfoHolder
        } else {
            dnsInfoHolder = listItem.tag as DNSInfoHolder
        }
        val currentDNS = allDNS[position]
        dnsInfoHolder.countryFlag.setImageResource(currentDNS.getCountry(mContext)!!.flagID)
        dnsInfoHolder.dnsInfoText.text = currentDNS.toString()
        return listItem!!
    }

    class DNSInfoHolder {
        lateinit var countryFlag: ImageView
        lateinit var dnsInfoText: TextView
    }

}