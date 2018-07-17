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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责具体执行 shell 命令
 */
class ShellExecutor {

    @NonNull
    private Cmd cmd;
    @NonNull
    private Callback callback;

    @Nullable
    private TimeoutTask timeoutTask;
    @NonNull
    private AtomicBoolean timeout;

    @Nullable
    private ShellTask shellTask;
    @NonNull
    private AtomicBoolean running;

    private ShellExecutor(@NonNull Cmd cmd, @NonNull Callback callback) {
        this.cmd = cmd;
        this.callback = callback;
        this.timeout = new AtomicBoolean(false);
        this.running = new AtomicBoolean(false);
    }

    public static CmdResult syncExecute(@NonNull Cmd cmd) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final CmdResult[] results = new CmdResult[1];
        new ShellExecutor(cmd, new Callback() {
            @Override
            public void onFinished(@NonNull CmdResult result) {
                results[0] = result;
                countDownLatch.countDown();
            }
        }).execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            results[0] = new CmdResult(cmd, -1, null, null, e);
        }
        return results[0];
    }

    private void execute() {
        if (running.get()) {
            return;
        }
        running.set(true);

        if (cmd.getTimeout() > 0) {
            new Timer().schedule(timeoutTask = new TimeoutTask(this), cmd.getTimeout());
        }

        new Thread(shellTask = new ShellTask(cmd, this)).start();
    }

    private void timeout() {
        running.set(false);

        timeout.set(true);

        if (shellTask != null) {
            shellTask.cancel();
        }

        if (cmd.isPrintLog()) {
            SHLog.w("Timeout. cmd: %s", cmd.toString());
        }

        try {
            callback.onFinished(new CmdResult(cmd, -2, null, null, null));
        } catch (Exception e) {
            // 捕获回调异常
            e.printStackTrace();
        }
    }

    private synchronized void done(@NonNull CmdResult result) {
        running.set(false);

        if (timeout.get()) {
            return;
        }

        if (timeoutTask != null) {
            timeoutTask.cancel();
        }

        if (cmd.isPrintLog()) {
            SHLog.d("Done. cmd: %s, return: %s", cmd.toString(), result.toString());
        }

        try {
            callback.onFinished(result);
        } catch (Exception e) {
            // 捕获回调异常
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onFinished(@NonNull CmdResult result);
    }

    private static class TimeoutTask extends TimerTask {
        @NonNull
        private ShellExecutor executor;

        TimeoutTask(@NonNull ShellExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void run() {
            executor.timeout();
        }
    }

    private static class ShellTask implements Runnable {
        @NonNull
        private Cmd cmd;
        @NonNull
        private ShellExecutor executor;
        @NonNull
        private AtomicBoolean canceled;

        @Nullable
        private Process process = null;
        @Nullable
        private DataOutputStream outputStream = null;

        ShellTask(@NonNull Cmd cmd, @NonNull ShellExecutor executor) {
            this.cmd = cmd;
            this.executor = executor;
            this.canceled = new AtomicBoolean(false);
        }

        private void cancel() {
            canceled.set(true);

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (process != null) {
                process.destroy();
            }
        }

        @Override
        public void run() {
            if (cmd.isPrintLog()) {
                SHLog.d("execute. %s", cmd.toString());
            }

            int code;
            String text = null;
            String errorText = null;
            Exception exception = null;

            try {
                process = Runtime.getRuntime().exec("sh", cmd.getEnvpArray(), cmd.getDir());

                outputStream = new DataOutputStream(process.getOutputStream());

                StringBuilder textBuilder = new StringBuilder();
                new ReadThread(cmd, false, textBuilder, process.getInputStream()).start();

                StringBuilder errorTextBuilder = new StringBuilder();
                new ReadThread(cmd, true, errorTextBuilder, process.getErrorStream()).start();

                final String shell = cmd.getShell();
                if (cmd.isPrintLog()) {
                    SHLog.d("write cmd: %s", shell);
                }

                outputStream.writeBytes(shell);
                outputStream.writeBytes("\n");
                outputStream.flush();

                if (cmd.isPrintLog()) {
                    SHLog.d("exit sh. %s", cmd.toString());
                }

                // exit sh
                outputStream.writeBytes("exit 0");
                outputStream.writeBytes("\n");
                outputStream.flush();

                if (cmd.isPrintLog()) {
                    SHLog.d("exit process. %s", cmd.toString());
                }

                // exit Process
                outputStream.writeBytes("exit 0");
                outputStream.writeBytes("\n");
                outputStream.flush();

                // 关闭输出流，输入流才会不再等待
                outputStream.close();

                if (cmd.isPrintLog()) {
                    SHLog.d("wait. %s", cmd.toString());
                }

                code = process.waitFor();

                text = textBuilder.toString().trim();
                errorText = errorTextBuilder.toString().trim();
            } catch (Exception e) {
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

            if (!canceled.get()) {
                executor.done(new CmdResult(cmd, code, text, errorText, exception));
            }
        }
    }

    private static class ReadThread extends Thread {
        @NonNull
        private Cmd cmd;
        private boolean error;
        @NonNull
        private StringBuilder textBuilder;
        @NonNull
        private InputStream inputStream;

        ReadThread(@NonNull Cmd cmd, boolean error, @NonNull StringBuilder textBuilder, @NonNull InputStream inputStream) {
            this.cmd = cmd;
            this.error = error;
            this.textBuilder = textBuilder;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            if (cmd.isPrintLog()) {
                SHLog.i("Read thread. %s. start", error ? "error" : "text");
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

                    if (cmd.isPrintLog()) {
                        if (error) {
                            SHLog.e("Read thread. %s. read text: %s", error ? "error" : "text", readLine);
                        } else {
                            SHLog.d("Read thread. %s. read text: %s", error ? "error" : "text", readLine);
                        }
                    }
                }
            } catch (IOException e) {
                if ("Stream closed".equalsIgnoreCase(e.getMessage())) {
                    // Normal closed
                } else {
                    if (cmd.isPrintLog()) {
                        SHLog.w("Read thread. %s. exception: %s", error ? "error" : "text", e.toString());
                    }
                    e.printStackTrace();
                }
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (cmd.isPrintLog()) {
                SHLog.w("Read thread. %s. end", error ? "error" : "text");
            }
        }
    }
}
