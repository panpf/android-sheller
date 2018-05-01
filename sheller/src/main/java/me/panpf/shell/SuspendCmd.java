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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 可检查前一个命令的执行结果，符合条件才执行当前命令，否则终止执行后续命令
 */
public abstract class SuspendCmd extends Cmd {
    @SuppressWarnings("WeakerAccess")
    public SuspendCmd(@NonNull String shell) {
        super(shell);
    }

    /**
     * 检查上一个命令的执行结果
     *
     * @param previousResult 上一个命令的执行结果
     * @return true：继续执行当前以及后续命令；false：终止执行当前以及后续命令
     */
    @SuppressWarnings("WeakerAccess")
    public abstract boolean checkLastResult(@Nullable CmdResult previousResult);

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SuspendCmd");
        builder.append("{");
        builder.append("shell=").append(getShell());
        if (getEnvpList() != null && !getEnvpList().isEmpty()) {
            builder.append(", envpList=").append(getEnvpList());
        }
        if (getDir() != null) {
            builder.append(", dir=").append(getDir());
        }
        builder.append('}');
        return builder.toString();
    }
}
