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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 可检查前一个命令的执行结果，符合条件才执行当前命令，不会终止后续命令的执行
 */
public abstract class ConditionalCmd extends Cmd {

    public ConditionalCmd(@NonNull String shell) {
        super(shell);
    }

    /**
     * 检查前一个命令的执行结果
     *
     * @param previousResult 上一个命令的执行结果
     * @return true：执行当前命令；false：跳过当前命令
     */
    public abstract boolean checkLastResult(@Nullable CmdResult previousResult);

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ConditionalCmd");
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
