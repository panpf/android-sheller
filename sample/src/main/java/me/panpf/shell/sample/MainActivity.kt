package me.panpf.shell.sample

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import me.panpf.adapter.AssemblyRecyclerAdapter
import me.panpf.shell.Command
import me.panpf.shell.Sheller

/**
 * 首页
 */
class MainActivity : AppCompatActivity() {

    private val inputEditText: EditText by bindView(R.id.editText)
    private val historyRecyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val button: Button by bindView(R.id.button)
    private val progress: ProgressBar by bindView(R.id.progress)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initViews()
        initData()
    }

    private fun initViews() {
        button.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE

        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = AssemblyRecyclerAdapter(null as Array<Any>?)
        adapter.addItemFactory(CommandHistoryItemFactory())

        historyRecyclerView.adapter = adapter

        button.setOnClickListener {
            val shell = inputEditText.editableText.toString().trim()

            if (TextUtils.isEmpty(shell)) {
                Toast.makeText(baseContext, "请输入命令", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            inputEditText.text = null

            val newCommandHistory = CommandHistory(shell, null)
            adapter.addAll(newCommandHistory)

            val insertIndex = adapter.itemCount - 1
            adapter.notifyItemInserted(insertIndex)
            historyRecyclerView.smoothScrollToPosition(insertIndex)

            button.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
            Sheller(Command(shell).dir(Environment.getExternalStorageDirectory())).asyncExecute(Handler(mainLooper)) { result ->
                newCommandHistory.result = result
                adapter.notifyItemChanged(insertIndex)
                historyRecyclerView.smoothScrollToPosition(insertIndex)

                button.visibility = View.VISIBLE
                progress.visibility = View.INVISIBLE
            }
        }

        inputEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                button.performClick()
                true
            }
            false
        }
    }

    private fun initData() {

    }
}
