package me.panpf.shell.sample

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import me.panpf.adapter.AssemblyItem
import me.panpf.adapter.AssemblyItemFactory
import me.panpf.adapter.ktx.bindView

class CmdHistoryItem(parent: ViewGroup) : AssemblyItem<CmdHistory>(R.layout.list_item_history, parent) {

    private val shellTextView: TextView by bindView(R.id.text_historyItem_shell)
    private val textTextView: TextView by bindView(R.id.text_historyItem_text)

    override fun onConfigViews(context: Context) {
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

    class Factory : AssemblyItemFactory<CmdHistory>() {
        override fun createAssemblyItem(parent: ViewGroup) = CmdHistoryItem(parent)
        override fun match(data: Any?): Boolean = data is CmdHistory
    }
}
