/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.shell;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * 执行 shell 命令，可以同步也可以异步，还可以批量顺序执行
 */
// TODO: 2017/12/20 支持事务，打开一个进程 持续输入命令返回结果
public class Sheller {
    public static final String TAG = "Sheller";

    private List<Cmd> cmdList = new LinkedList<>();

    public Sheller(@NonNull Cmd... cmds) {
        addAll(cmds);
    }

    @SuppressWarnings("unused")
    public Sheller(@NonNull Cmd cmd) {
        add(cmd);
    }

    public Sheller(@NonNull String... shells) {
        addAll(shells);
    }

    public Sheller(@NonNull String shell) {
        add(shell);
    }


    @NonNull
    @SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
    public Sheller add(@NonNull Cmd cmd) {
        //noinspection ConstantConditions
        if (cmd == null) {
            throw new IllegalArgumentException("param cmd is null");
        }
        this.cmdList.add(cmd);
        return this;
    }

    @NonNull
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
    public Sheller add(@NonNull String shell) {
        //noinspection ConstantConditions
        if (shell == null || "".equals(shell)) {
            throw new IllegalArgumentException("param shell is null or empty");
        }
        this.cmdList.add(new Cmd(shell));
        return this;
    }


    @NonNull
    @SuppressWarnings("unused")
    public Sheller add(int index, @NonNull Cmd cmd) {
        if (index < 0) {
            throw new IllegalArgumentException("param index invalid. " + index);
        }
        //noinspection ConstantConditions
        if (cmd == null) {
            throw new IllegalArgumentException("param cmd is null");
        }
        this.cmdList.add(index, cmd);
        return this;
    }

    @NonNull
    @SuppressWarnings("unused")
    public Sheller add(int index, @NonNull String shell) {
        if (index < 0) {
            throw new IllegalArgumentException("param index invalid. " + index);
        }
        //noinspection ConstantConditions
        if (shell == null || "".equals(shell)) {
            throw new IllegalArgumentException("param shell is null or empty");
        }
        this.cmdList.add(index, new Cmd(shell));
        return this;
    }


    @NonNull
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Sheller addAll(@NonNull Cmd... cmds) {
        //noinspection ConstantConditions
        if (cmds == null || cmds.length <= 0) {
            throw new IllegalArgumentException("param cmd is empty");
        }
        int index = 0;
        for (Cmd cmd : cmds) {
            if (cmd == null) {
                throw new IllegalArgumentException("element at index " + index + " is null");
            }
            this.cmdList.add(cmd);
            index++;
        }
        return this;
    }

    @NonNull
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Sheller addAll(@NonNull String... shells) {
        //noinspection ConstantConditions
        if (shells == null || shells.length <= 0) {
            throw new IllegalArgumentException("param shells is empty");
        }
        int index = 0;
        for (String cmd : shells) {
            if (cmd == null || "".equals(cmd)) {
                throw new IllegalArgumentException("shell at index " + index + " is null or empty");
            }
            this.cmdList.add(new Cmd(cmd));
        }
        return this;
    }

    /**
     * 同步执行，只要最后一个结果
     */
    @NonNull
    @SuppressWarnings("WeakerAccess")
    public CmdResult syncExecute() {
        if (cmdList.isEmpty()) {
            throw new IllegalArgumentException("cmd list is empty");
        }

        CmdResult previousResult = null;
        CmdResult lastResult = null;
        for (Cmd cmd : cmdList) {
            if (cmd == null) {
                continue;
            }

            if (lastResult != null && cmd instanceof SuspendCmd) {
                if (((SuspendCmd) cmd).checkLastResult(previousResult)) {
                    previousResult = ShellExecutor.syncExecute(cmd);
                    lastResult = previousResult;
                } else {
                    break;
                }
            } else if (lastResult != null && cmd instanceof ConditionalCmd) {
                if (((ConditionalCmd) cmd).checkLastResult(previousResult)) {
                    previousResult = ShellExecutor.syncExecute(cmd);
                    lastResult = previousResult;
                } else {
                    previousResult = null;
                }
            } else {
                previousResult = ShellExecutor.syncExecute(cmd);
                lastResult = previousResult;
            }
        }

        if (lastResult == null) {
            throw new IllegalArgumentException("cmd list elements is empty");
        }

        return lastResult;
    }

    /**
     * 异步执行
     *
     * @param callback 结果回调
     * @param handler  结果回调在指定 Handler 中执行
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void asyncExecute(@SuppressWarnings("SameParameterValue") final Handler handler, @Nullable final ResultCallback callback) {
        new Thread() {
            @Override
            public void run() {
                final CmdResult result = syncExecute();
                if (callback != null) {
                    if (handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCallbackResult(result);
                            }
                        });
                    } else {
                        callback.onCallbackResult(result);
                    }
                }
            }
        }.start();
    }

    /**
     * 异步执行
     *
     * @param callback 结果回调
     */
    @SuppressWarnings("unused")
    public void asyncExecute(@Nullable final ResultCallback callback) {
        asyncExecute(null, callback);
    }
}
