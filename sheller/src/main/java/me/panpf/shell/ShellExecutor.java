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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
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
        int code;
        String text = null;
        String errorText = null;
        Exception exception = null;

        Process process = null;
        BufferedReader textReader = null;
        BufferedReader errorTextReader = null;
        DataOutputStream outputStream = null;
        try {
            process = Runtime.getRuntime().exec("sh", command.getEnvpArray(), command.getDir());
            outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes(command.getShell());
            outputStream.writeBytes("\n");
            outputStream.flush();

            // 退出 sh
            outputStream.writeBytes("exit");
            outputStream.writeBytes("\n");
            outputStream.flush();

            // 退出 Process
            outputStream.writeBytes("exit");
            outputStream.writeBytes("\n");
            outputStream.flush();

            StringBuilder textBuilder = new StringBuilder();
            textReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String readLine;
            while ((readLine = textReader.readLine()) != null) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("\n");
                }
                textBuilder.append(readLine);
            }
            text = textBuilder.toString().trim();

            errorTextReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorTextBuilder = new StringBuilder();
            while ((readLine = errorTextReader.readLine()) != null) {
                if (errorTextBuilder.length() > 0) {
                    errorTextBuilder.append("\n");
                }
                errorTextBuilder.append(readLine);
            }
            errorText = errorTextBuilder.toString().trim();

            code = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            code = -1;
            exception = e;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (errorTextReader != null) {
                    errorTextReader.close();
                }
                if (textReader != null) {
                    textReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
        return new CommandResult(command, code, text, errorText, exception);
    }

//    /**
//     * 执行命令
//     *
//     * @return 执行结果
//     */
//    @NonNull
//    CommandResult execute() {
//        int code;
//        String text = null;
//        String errorText = null;
//        Exception exception = null;
//
//        Process process = null;
//        DataOutputStream outputStream = null;
//        StringBuilder fullBuilder = new StringBuilder();
//        StringBuilder textBuilder = new StringBuilder();
//        StringBuilder errorTextBuilder = new StringBuilder();
//        CountDownLatch countDownLatch = new CountDownLatch(3);
//        ReadThread readThread = null;
//        ReadThread errorReadThread = null;
//        try {
//            process = Runtime.getRuntime().exec("sh", command.getEnvpArray(), command.getDir());
//            outputStream = new DataOutputStream(process.getOutputStream());
//
//            readThread = new ReadThread(fullBuilder, textBuilder, countDownLatch, process.getInputStream());
//            errorReadThread = new ReadThread(fullBuilder, errorTextBuilder, countDownLatch, process.getErrorStream());
//
//            readThread.start();
//            errorReadThread.start();
//
//            outputStream.writeBytes(command.getShell());
//            outputStream.writeBytes("\n");
//            outputStream.flush();
//
//            // 退出 sh
//            outputStream.writeBytes("exit");
//            outputStream.writeBytes("\n");
//            outputStream.flush();
//
//            // 退出 Process
//            outputStream.writeBytes("exit");
//            outputStream.writeBytes("\n");
//            outputStream.flush();
//
//            Log.d("ShellExecutor", "ReadThread. waitFor before");
//
//            code = process.waitFor();
//
//            Log.d("ShellExecutor", "ReadThread. waitFor after");
//
//            readThread.destroy();
//            errorReadThread.destroy();
//            countDownLatch.countDown();
//
//            text = fullBuilder.toString().trim();
//            errorText = errorTextBuilder.toString().trim();
//
//            if (code == 1 && !TextUtils.isEmpty(text)) {
//                code = 0;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            code = -1;
//            exception = e;
//        } finally {
//            if (readThread != null) {
//                readThread.destroy();
//            }
//            if (errorReadThread != null) {
//                readThread.destroy();
//            }
//            try {
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (process != null) {
//                    process.destroy();
//                }
//            }
//        }
//        return new CommandResult(command, code, text, errorText, exception);
//    }
//
//    private static class ReadThread extends Thread {
//        @NonNull
//        private final StringBuilder builder;
//        @NonNull
//        private StringBuilder builder2;
//        @NonNull
//        private CountDownLatch downLatch;
//        @NonNull
//        private BufferedReader bufferedReader;
//        private boolean running = true;
//
//        ReadThread(@NonNull StringBuilder builder, @NonNull StringBuilder builder2, @NonNull CountDownLatch downLatch, @NonNull InputStream inputStream) {
//            this.builder = builder;
//            this.builder2 = builder2;
//            this.downLatch = downLatch;
//            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//        }
//
//        public void destroy() {
//            running = false;
//            notifyAll();
//        }
//
//        @Override
//        public void run() {
//            while (running) {
//                String readLine = null;
//                try {
//                    readLine = bufferedReader.readLine();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (readLine != null) {
//                    Log.d("ShellExecutor", "ReadThread. newLine: " + readLine);
//                    synchronized (builder) {
//                        if (builder.length() > 0) {
//                            builder.append("\n");
//                        }
//                        builder.append(readLine);
//
//                        if (builder2.length() > 0) {
//                            builder2.append("\n");
//                        }
//                        builder2.append(readLine);
//                    }
//                } else {
//                    Log.d("ShellExecutor", "ReadThread. wait 500 ms");
//                    try {
//                        wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            Log.w("ShellExecutor", "ReadThread. end");
//            try {
//                bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            downLatch.countDown();
//        }
//    }
}
