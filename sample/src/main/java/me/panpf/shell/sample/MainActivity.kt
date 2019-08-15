package me.panpf.shell.sample

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import me.panpf.adapter.AssemblyRecyclerAdapter
import me.panpf.shell.Cmd
import me.panpf.shell.Sheller

class MainActivity : AppCompatActivity() {

    private var timeout = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        button.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = AssemblyRecyclerAdapter(null as Array<Any>?)
        adapter.addItemFactory(CmdHistoryItem.Factory())
        recyclerView.adapter = adapter

        editText.setText("su")

        button.setOnClickListener {
            val shell = editText.editableText.toString().trim()

            if (TextUtils.isEmpty(shell)) {
                Toast.makeText(baseContext, "请输入命令", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editText.text = null

            val newCmdHistory = CmdHistory(shell, null)
            adapter.addAll(newCmdHistory)

            val insertIndex = adapter.itemCount - 1
            adapter.notifyItemInserted(insertIndex)
            recyclerView.smoothScrollToPosition(insertIndex)

            button.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
            Sheller(Cmd(shell).dir(Environment.getExternalStorageDirectory()).printLog().timeout(timeout)).asyncExecute(Handler(mainLooper)) { result ->
                newCmdHistory.result = result
                adapter.notifyItemChanged(insertIndex)
                recyclerView.smoothScrollToPosition(insertIndex)

                button.visibility = View.VISIBLE
                progress.visibility = View.INVISIBLE
            }
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                button.performClick()
                true
            }
            false
        }
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
