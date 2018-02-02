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
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class ShellExecutor {
    @NonNull
    private Command command;

    ShellExecutor(@NonNull Command command) {
        this.command = command;
    }

    /**
     * 执行命令
     *
     * @return 执行结果
     */
    @NonNull
    CommandResult execute() {
        if (command.isPrintLog()) {
            Log.d(Sheller.TAG, "ShellExecutor. execute");
        }

        int code;
        String text = null;
        String errorText = null;
        Exception exception = null;

        Process process = null;
        DataOutputStream outputStream = null;
        try {
            process = Runtime.getRuntime().exec("sh", command.getEnvpArray(), command.getDir());

            outputStream = new DataOutputStream(process.getOutputStream());

            StringBuilder textBuilder = new StringBuilder();
            new ReadThread(command, false, textBuilder, process.getInputStream()).start();

            StringBuilder errorTextBuilder = new StringBuilder();
            new ReadThread(command, true, errorTextBuilder, process.getErrorStream()).start();

            final String shell = command.getShell();
            if (command.isPrintLog()) {
                Log.d(Sheller.TAG, String.format("ShellExecutor. write command: %s", shell));
            }

            outputStream.writeBytes(shell);
            outputStream.writeBytes("\n");
            outputStream.flush();

            if (command.isPrintLog()) {
                Log.d(Sheller.TAG, "ShellExecutor. exit sh");
            }

            // exit sh
            outputStream.writeBytes("exit 0");
            outputStream.writeBytes("\n");
            outputStream.flush();

            if (command.isPrintLog()) {
                Log.d(Sheller.TAG, "ShellExecutor. exit process");
            }

            // exit Process
            outputStream.writeBytes("exit 0");
            outputStream.writeBytes("\n");
            outputStream.flush();

            // 关闭输出流，输入流才会不再等待
            outputStream.close();

            if (command.isPrintLog()) {
                Log.d(Sheller.TAG, "ShellExecutor. wait");
            }

            code = process.waitFor();

            text = textBuilder.toString().trim();
            errorText = errorTextBuilder.toString().trim();
        } catch (Exception e) {
            if (command.isPrintLog()) {
                Log.w(Sheller.TAG, String.format("ShellExecutor. exception: %s", e.toString()));
            }

            e.printStackTrace();
            code = -1;
            exception = e;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }

        final CommandResult result = new CommandResult(command, code, text, errorText, exception);
        if (command.isPrintLog()) {
            Log.w(Sheller.TAG, String.format("ShellExecutor. return: %s", result.toString()));
        }

        return result;
    }

    private static class ReadThread extends Thread {
        @NonNull
        private Command command;
        private boolean error;
        @NonNull
        private StringBuilder textBuilder;
        @NonNull
        private InputStream inputStream;

        ReadThread(@NonNull Command command, boolean error, @NonNull StringBuilder textBuilder, @NonNull InputStream inputStream) {
            this.command = command;
            this.error = error;
            this.textBuilder = textBuilder;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            if (command.isPrintLog()) {
                Log.i(Sheller.TAG, String.format("ShellExecutor. ReadThread. %s. start", error ? "error" : "text"));
            }

            BufferedReader bufferedReader = null;
            try {
                String readLine;
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while ((readLine = bufferedReader.readLine()) != null) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(readLine);

                    if (command.isPrintLog()) {
                        if (error) {
                            Log.e(Sheller.TAG, String.format("ShellExecutor. ReadThread. %s. read text: %s", error ? "error" : "text", readLine));
                        } else {
                            Log.d(Sheller.TAG, String.format("ShellExecutor. ReadThread. %s. read text: %s", error ? "error" : "text", readLine));
                        }
                    }
                }
            } catch (IOException e) {
                if (command.isPrintLog()) {
                    Log.w(Sheller.TAG, String.format("ShellExecutor. ReadThread. %s. exception: %s", error ? "error" : "text", e.toString()));
                }
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (command.isPrintLog()) {
                Log.w(Sheller.TAG, String.format("ShellExecutor. ReadThread. %s. end", error ? "error" : "text"));
            }
        }
    }
}
