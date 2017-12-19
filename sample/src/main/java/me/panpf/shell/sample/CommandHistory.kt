package me.panpf.shell.sample

import me.panpf.shell.CommandResult

data class CommandHistory(val shell: String, var result: CommandResult?)