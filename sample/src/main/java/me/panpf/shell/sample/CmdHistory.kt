package me.panpf.shell.sample

import me.panpf.shell.CmdResult

data class CmdHistory(val shell: String, var result: CmdResult?)