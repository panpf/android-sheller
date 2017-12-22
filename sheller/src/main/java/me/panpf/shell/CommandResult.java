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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 命令执行结果
 */
public class CommandResult {
    @NonNull
    private Command command;
    private int code;
    @Nullable
    private String text;
    @Nullable
    private String errorText;
    @Nullable
    private Exception exception;

    private transient String exceptionStackTrace;

    private transient String mixedText;

    @SuppressWarnings("WeakerAccess")
    public CommandResult(@NonNull Command command, int code, @Nullable String text, @Nullable String errorText, @Nullable Exception exception) {
        this.command = command;
        this.code = code;
        this.text = text;
        this.errorText = errorText;
        this.exception = exception;
    }

    /**
     * 是否成功，根据返回的状态判断，等于 0 即为成功
     */
    public boolean isSuccess() {
        return code == 0 || isMixedSuccess();
    }

    /**
     * 是否是因为异常导致的失败
     */
    @SuppressWarnings("unused")
    public boolean isException() {
        return code == -1 && exception != null;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isMixedSuccess() {
        return code > 0 && !TextUtils.isEmpty(text) && !TextUtils.isEmpty(errorText);
    }

    @NonNull
    public Command getCommand() {
        return command;
    }

    /**
     * 获取执行结果状态码
     *
     * @return 0：成功；1：失败；-1：过程异常导致失败
     */
    @SuppressWarnings("unused")
    public int getCode() {
        return code;
    }

    /**
     * 获取成功时返回的结果
     */
    @Nullable
    public String getText() {
        if (isMixedSuccess()) {
            if (mixedText == null) {
                mixedText = text + "\n" + errorText;
            }
            return mixedText;
        } else {
            return text;
        }
    }

    /**
     * 获取失败时返回的结果
     */
    @Nullable
    @SuppressWarnings("unused")
    public String getErrorText() {
        return errorText;
    }

    @SuppressWarnings("unused")
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

    /**
     * 获取完整的异常栈信息
     */
    @SuppressWarnings("unused")
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
        StringBuilder builder = new StringBuilder("CommandResult");
        builder.append("{");
        builder.append("command=").append(command);
        builder.append(", code=").append(code);
        if (isSuccess()) {
            if (isMixedSuccess()) {
                builder.append(", mixedText=").append(getText());
            } else {
                builder.append(", text=").append(text);
            }
        } else if (isException()) {
            builder.append(", exceptionMessage=").append(getExceptionMessage());
        } else {
            builder.append(", errorText=").append(errorText);
        }
        builder.append('}');
        return builder.toString();
    }
}
