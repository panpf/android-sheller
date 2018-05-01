package me.panpf.shell.sample

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import me.panpf.adapter.AssemblyRecyclerAdapter
import me.panpf.shell.Cmd
import me.panpf.shell.Sheller

/**
 * 首页
 */
class MainActivity : AppCompatActivity() {

    private val inputEditText: EditText by bindView(R.id.editText)
    private val historyRecyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val button: Button by bindView(R.id.button)
    private val progress: ProgressBar by bindView(R.id.progress)

    private var timeout = 0

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
        adapter.addItemFactory(CmdHistoryItemFactory())

        historyRecyclerView.adapter = adapter

        inputEditText.setText("su")

        button.setOnClickListener {
            val shell = inputEditText.editableText.toString().trim()

            if (TextUtils.isEmpty(shell)) {
                Toast.makeText(baseContext, "请输入命令", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            inputEditText.text = null

            val newCmdHistory = CmdHistory(shell, null)
            adapter.addAll(newCmdHistory)

            val insertIndex = adapter.itemCount - 1
            adapter.notifyItemInserted(insertIndex)
            historyRecyclerView.smoothScrollToPosition(insertIndex)

            button.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
            Sheller(Cmd(shell).dir(Environment.getExternalStorageDirectory()).printLog().timeout(timeout)).asyncExecute(Handler(mainLooper)) { result ->
                newCmdHistory.result = result
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.addSubMenu(0, 1, 0, "Closed Time out")
        menuItem?.item?.setShowAsAction(SHOW_AS_ACTION_ALWAYS)
        menuItem?.add(0, 11, 0, "Closed")?.setOnMenuItemClickListener {
            timeout = 0
            supportInvalidateOptionsMenu()
            true
        }
        menuItem?.add(0, 12, 1, "10 秒超时")?.setOnMenuItemClickListener {
            timeout = 10 * 1000
            supportInvalidateOptionsMenu()
            true
        }
        menuItem?.add(0, 13, 2, "20 秒超时")?.setOnMenuItemClickListener {
            timeout = 20 * 1000
            supportInvalidateOptionsMenu()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(1)?.apply {
            when (timeout) {
                0 -> title = "Closed Time out"
                10 * 1000 -> title = "10 S Time out"
                20 * 1000 -> title = "20 S Time out"
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
