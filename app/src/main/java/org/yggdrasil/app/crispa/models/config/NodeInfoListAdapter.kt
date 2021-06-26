package org.yggdrasil.app.crispa.models.config

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.yggdrasil.app.crispa.R
import org.yggdrasil.app.crispa.models.NodeInfo

class NodeInfoListAdapter(private val context: Context, private val infoSet: Array<NodeInfo>) :
    RecyclerView.Adapter<NodeInfoListAdapter.ViewHolder>() {

    class ViewHolder(private val context: Context, view: View) : RecyclerView.ViewHolder(view) {
        val key: TextView
        val value: TextView

        init {
            key = view.findViewById(R.id.node_info_key)
            value = view.findViewById(R.id.node_info_value)

            value.setOnClickListener {
                val clipboard: ClipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText(key.text, value.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, key.text.toString() + " " + context.getString(R.string.node_info_copied), Toast.LENGTH_LONG).show()
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.node_info_row, viewGroup, false)

        return ViewHolder(
            context,
            view
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.key.text = infoSet[position].key
        viewHolder.value.text = infoSet[position].value
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = infoSet.size

}
