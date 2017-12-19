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
import android.text.TextUtils;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 单个可执行的命令，可配置环境变量和工作目录
 */
@SuppressWarnings("WeakerAccess")
public class Command {
    private String shell;
    private List<String> envpList;
    private File dir;

    public Command(@NonNull String shell) {
        //noinspection ConstantConditions
        if (shell == null || "".equals(shell)) {
            throw new IllegalArgumentException("param shell is null or empty");
        }
        this.shell = shell;
    }

    /**
     * 设置环境变量，会清空旧的
     *
     * @param envps 环境变量数据，每个元素的格式为 name=value
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command envp(@Nullable String[] envps) {
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        envpList.clear();

        //noinspection ConstantConditions
        if (envps != null && envps.length > 0) {
            Collections.addAll(envpList, envps);
        }
        return this;
    }

    /**
     * 设置环境变量，会清空旧的
     *
     * @param envpList 环境变量列表，每个元素的格式为 name=value
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command envp(@Nullable List<String> envpList) {
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.clear();

        //noinspection ConstantConditions
        if (envpList != null && !envpList.isEmpty()) {
            this.envpList.addAll(envpList);
        }
        return this;
    }

    /**
     * 添加环境变量
     *
     * @param envp 环境变量，格式为 name=value
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command addEnvp(@NonNull String envp) {
        if (TextUtils.isEmpty(envp)) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.add(envp);
        return this;
    }

    /**
     * 添加环境变量
     *
     * @param envpKey   环境变量 KEY
     * @param envpValue 环境变量 VALUE
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command addEnvp(@NonNull String envpKey, @NonNull String envpValue) {
        if (TextUtils.isEmpty(envpKey) || TextUtils.isEmpty(envpValue)) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.add(envpKey + "=" + envpValue);
        return this;
    }

    /**
     * 批量添加环境变量，每个元素的格式为 name=value
     *
     * @param envps 环境变量数据，每个元素的格式为 name=value
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command addEnvpAll(@NonNull String[] envps) {
        //noinspection ConstantConditions
        if (envps == null || envps.length <= 0) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        Collections.addAll(envpList, envps);
        return this;
    }

    /**
     * 添加环境变量，每个元素的格式为 name=value
     *
     * @param envpList 环境变量列表，每个元素的格式为 name=value
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command addEnvpAll(@NonNull List<String> envpList) {
        //noinspection ConstantConditions
        if (envpList == null || envpList.isEmpty()) {
            return this;
        }
        if (this.envpList == null) {
            this.envpList = new LinkedList<>();
        }
        this.envpList.addAll(envpList);
        return this;
    }

    /**
     * 设置当前命令的工作目录
     *
     * @param dir 工作目录
     * @return Command
     */
    @NonNull
    @SuppressWarnings("unused")
    public Command dir(@Nullable File dir) {
        this.dir = dir;
        return this;
    }

    @NonNull
    public String getShell() {
        return shell;
    }

    @Nullable
    @SuppressWarnings("unused")
    public List<String> getEnvpList() {
        return envpList;
    }

    @Nullable
    public String[] getEnvpArray() {
        return envpList != null && !envpList.isEmpty() ? envpList.toArray(new String[envpList.size()]) : null;
    }

    @Nullable
    public File getDir() {
        return dir;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Command");
        builder.append("{");
        builder.append("shell=").append(shell);
        if (envpList != null && !envpList.isEmpty()) {
            builder.append(", envpList=").append(envpList);
        }
        if (dir != null) {
            builder.append(", dir=").append(dir);
        }
        builder.append('}');
        return builder.toString();
    }
}
