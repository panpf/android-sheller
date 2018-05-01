package me.panpf.shell.sample

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import me.panpf.adapter.AssemblyRecyclerItem
import me.panpf.adapter.AssemblyRecyclerItemFactory

class CmdHistoryItemFactory : AssemblyRecyclerItemFactory<CmdHistoryItem>() {
    override fun createAssemblyItem(parent: ViewGroup?): CmdHistoryItem {
        return CmdHistoryItem(R.layout.list_item_history, parent)
    }

    override fun isTarget(data: Any?): Boolean {
        return data is CmdHistory
    }
}

class CmdHistoryItem(itemLayoutId: Int, parent: ViewGroup?) : AssemblyRecyclerItem<CmdHistory>(itemLayoutId, parent) {
    private val shellTextView: TextView by bindView(R.id.text_historyItem_shell)
    private val textTextView: TextView by bindView(R.id.text_historyItem_text)

    override fun onConfigViews(context: Context?) {
    }

    override fun onSetData(position: Int, data: CmdHistory?) {
        if (data != null) {
            shellTextView.text = data.shell
            if (data.result != null) {
                when {
                    data.result!!.isSuccess -> {
                        textTextView.text = data.result!!.text
                        textTextView.setTextColor(Color.BLACK)
                    }
                    else -> {
                        textTextView.text = data.result!!.finalErrorText
                        textTextView.setTextColor(Color.RED)
                    }
                }
            } else {
                textTextView.text = null
            }
        } else {
            shellTextView.text = null
            textTextView.text = null
        }
    }
}
