package me.panpf.shell.sample

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import me.panpf.adapter.AssemblyRecyclerItem
import me.panpf.adapter.AssemblyRecyclerItemFactory

class CommandHistoryItemFactory : AssemblyRecyclerItemFactory<CommandHistoryItem>() {
    override fun createAssemblyItem(parent: ViewGroup?): CommandHistoryItem {
        return CommandHistoryItem(R.layout.list_item_history, parent)
    }

    override fun isTarget(data: Any?): Boolean {
        return data is CommandHistory
    }
}

class CommandHistoryItem(itemLayoutId: Int, parent: ViewGroup?) : AssemblyRecyclerItem<CommandHistory>(itemLayoutId, parent) {
    private val shellTextView: TextView by bindView(R.id.text_historyItem_shell)
    private val textTextView: TextView by bindView(R.id.text_historyItem_text)

    override fun onConfigViews(context: Context?) {
    }

    override fun onSetData(position: Int, data: CommandHistory?) {
        if (data != null) {
            shellTextView.text = data.shell
            if (data.result != null) {
                when {
                    data.result!!.isSuccess -> {
                        textTextView.text = data.result!!.text
                        textTextView.setTextColor(Color.BLACK)
                    }
                    data.result!!.isException -> {
                        textTextView.text = data.result!!.exceptionMessage
                        textTextView.setTextColor(Color.RED)
                    }
                    else -> {
                        textTextView.text = data.result!!.errorText
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
