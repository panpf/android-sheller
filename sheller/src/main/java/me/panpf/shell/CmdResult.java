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

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * 命令执行结果
 */
public class CmdResult implements Serializable {
    @NonNull
    private Cmd cmd;
    private int code;
    @Nullable
    private String text;
    @Nullable
    private String errorText;
    @Nullable
    private Exception exception;

    private transient String exceptionStackTrace;

    public CmdResult(@NonNull Cmd cmd, int code, @Nullable String text, @Nullable String errorText, @Nullable Exception exception) {
        this.cmd = cmd;
        this.code = code;
        this.text = text;
        this.errorText = errorText;
        this.exception = exception;
    }

    /**
     * 是否成功，根据返回的状态判断，等于 0 即为成功
     */
    public boolean isSuccess() {
        if (code == 0) {
            // 虽然 code 为 0 ，但是只有错误信息，说明还是失败了
            return !TextUtils.isEmpty(text) || TextUtils.isEmpty(errorText);
        } else {
            return code > 0 && !TextUtils.isEmpty(text) && !TextUtils.isEmpty(errorText);
        }
    }

    /**
     * 是否是因为异常导致的失败
     */
    public boolean isException() {
        return code == -1 && exception != null;
    }

    /**
     * 是否超时
     *
     * @return 超时了
     */
    public boolean isTimeout() {
        return code == -2;
    }

    @NonNull
    public Cmd getCmd() {
        return cmd;
    }

    /**
     * 获取执行结果状态码
     *
     * @return 0：成功；1：失败；-1：过程异常导致失败
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取成功时返回的结果
     */
    @Nullable
    public String getText() {
        if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(errorText)) {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(text)) {
                builder.append(text);
            }
            if (!TextUtils.isEmpty(errorText)) {
                builder.append("\n");
                builder.append(errorText);
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    /**
     * 获取失败时返回的结果
     */
    @Nullable
    public String getErrorText() {
        return errorText;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    /**
     * 获取异常消息
     */
    public String getExceptionMessage() {
        return exception != null ? exception.getLocalizedMessage() : null;
    }

    public String getFinalErrorText() {
        if (isException()) {
            return getExceptionMessage();
        } else if (isTimeout()) {
            return "Time out of " + cmd.getTimeout() + " ms";
        } else if (!isSuccess()) {
            return errorText;
        } else {
            return null;
        }
    }

    /**
     * 获取完整的异常栈信息
     */
    public String getExceptionStackTrace() {
        if (exception == null) {
            return null;
        }
        if (exceptionStackTrace == null) {
            synchronized (this) {
                if (exceptionStackTrace == null) {
                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    exception.printStackTrace(new PrintStream(arrayOutputStream));
                    exceptionStackTrace = new String(arrayOutputStream.toByteArray());
                }
            }
        }
        return exceptionStackTrace;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CmdResult");
        builder.append("{");
        builder.append("cmd=").append(cmd);
        builder.append(", code=").append(code);
        if (isException()) {
            builder.append(", ex=").append(getExceptionMessage());
        } else {
            builder.append(", text=").append(text);
            builder.append(", error=").append(errorText);
        }
        builder.append('}');
        return builder.toString();
    }
}
