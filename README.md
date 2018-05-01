# Sheller

![Platform][platform_image]
[![API][api_image]][api_link]
[![Release][release_icon]][release_link]
[![License][license_image]][license_link]

Sheller 是 Android 上的一个 shell 库，可帮助开发者方便的执行 shell 命令

## 特性

* 支持同步和异步执行两种方式，异步执行还可通过 Handler 让异步回调在指定线程中（例如主线程）执行
* 支持批量执行命令
* 批量执行命令时还可以对每一步的结果进行检查，决定是否执行下一个命令或终止执行

## 使用指南

### 1. 从 JCenter 导入 Sheller

```groovy
dependencies {
    compile 'me.panpf:sheller:$lastVersionName'
}
```

`$lastVersionName`：[![Release][release_icon]][release_link]`（不带v）`

`最低兼容 API 10`

### 2. 执行命令

#### 2.1. 同步执行：

```java
CmdResult result = new Sheller("pwd").syncExecute();

if (result.isSuccess()) {
  // 命令执行成功，结果码为 0，打印返回结果
  Log.d("Sheller", "pwd: " + result.getTetxt());
} else if(result.isException()) {
  // 命令执行失败，发生了异常，一般是命令写错了，结果码为 -1，打印异常信息
  Log.w("Sheller", "pwd: " + result.getExceptionMessage());
} else {
  // 命令执行失败，结果码为 1，打印错误信息
  Log.w("Sheller", "pwd: " + result.getErrorText());
}
```

syncExecute() 方法万不可在主线程中执行

#### 2.2. 异步执行：

```java
new Sheller("pwd").asyncExecute(new ResultCallback(){
  @Override
  public void onCallbackResult(@NonNull CmdResult result){
    ...
  }
});
```

回调结果默认在异步线程中执行，若想要在主线程中执行回调，只需传入一个主线程的 Handler 即可：

```java
new Sheller("pwd").asyncExecute(new Handler(Looper.getMainLooper()), new ResultCallback(){
  @Override
  public void onCallbackResult(@NonNull CmdResult result){
    ...
  }
});
```

#### 2.3. 设置工作目录和环境变量

每一个命令都可以单独设置工作目录和环境变量，如下：

```java
Cmd pwdCmd = new Cmd("pwd");

// 设置工作目录
pwd.dir("/sdcard/Android/data/");

// 设置环境变量
pwd.addEnvp("WORK_HOME", "/sdcard/Android/data/me.panpf.shell");

CmdResult result = new Sheller(pwdCmd).syncExecute();
```

#### 2.4 设置超时时间

你可以通过如下代码设置超时时间：

```
Cmd pwdCmd = new Cmd("pwd");
pwdCmd.timeout(10 * 1000)
```

判断是否是超时：

```
CmdResult result = ...;
if (result.isTimeout()) {
    // 执行超时
}
```

### 3. 执行多个命令

#### 3.1. 无条件顺序执行

```java
CmdResult result = new Sheller("pwd", "ls", "id").syncExecute();
```

或

```java
CmdResult result = new Sheller("pwd").add("ls").add("id").syncExecute();
```

连续执行多个命令时只会返回最后一个命令的执行结果

#### 3.2. 条件顺序执行

有时候我们需要在连续执行命令的时候依赖前面一个命令的执行结果，如下通过 [ConditionalCmd] 可实现此需求：

```java
// 创建目录
Cmd createDirCmd = new Cmd("mkdir /sdcard/test/");

// 创建文件
Cmd createFileCmd = new ConditionalCmd("touch /sdcard/test/file.temp"){
  @Override
  public boolean checkLastResult(@Nullable CmdResult previousResult){
    return previousResult != null && previousResult.isSuccess();
  }
};

// 删除文件
Cmd removeFileCmd = new ConditionalCmd("rm -f /sdcard/test/file.temp")

CmdResult result = new Sheller(createDirCmd, createFileCmd, removeFileCmd).syncExecute();
```

上述示例执行流程简述：

1. 先执行创建目录命令
2. 检查创建目录命令的返回结果，通过则执行创建文件命令，否则跳过（执行结果为 null）
2. 检查创建文件命令的返回结果，通过则执行删除文件命令，否则跳过（执行结果为 null）

最终的返回结果，一句话概括为返回最后一个执行的命令（跳过的不算）的结果

#### 3.3. 条件终止顺序执行

有的时候需要一旦前面的命令执行失败，后面所有命令都不需要执行了，如下通过 [SuspendCmd] 可实现此需求：

```java
// su 命令，申请 root 权限
Cmd suCmd = new Cmd("su");

// 安装命令，静默安装 apk
Cmd installCmd = new SuspendCmd("pm install -r /sdcard/test.apk") {
  @Override
  public boolean checkLastResult(@Nullable CmdResult previousResult){
    return previousResult != null && previousResult.isSuccess();
  }
};

// 删除命令，安装成功后删除安装包
Cmd deleteApkCmd = new SuspendCmd("rm /sdcard/test.apk"){
  @Override
  public boolean checkLastResult(@Nullable CmdResult previousResult){
    return previousResult != null && previousResult.isSuccess();
  }
};

CmdResult result = new Sheller(suCmd).add(installCmd).syncExecute();
```

上述示例执行流程简述：

1. 先执行 su 命令
2. 检查 su 命令的返回结果，通过则执行安装命令，否则终止后续命令执行
3. 检查安装命令的返回结果，通过则执行删除命令，否则终止后续命令执行

最终的返回结果，一句话概括为返回最后一个执行的命令的结果

`更多示例请参考 sample 源码`

### License
    Copyright (C) 2017 Peng fei Pan <sky@panpf.me>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[platform_image]: https://img.shields.io/badge/Platform-Android-brightgreen.svg
[api_image]: https://img.shields.io/badge/API-14%2B-orange.svg
[api_link]: https://android-arsenal.com/api?level=14
[release_icon]: https://img.shields.io/github/release/panpf/sheller.svg
[release_link]: https://github.com/panpf/sheller/releases
[license_image]: https://img.shields.io/badge/License-Apache%202-blue.svg
[license_link]: https://www.apache.org/licenses/LICENSE-2.0
[SuspendCmd]: sheller/src/main/java/me/panpf/shell/SuspendCmd.java
[ConditionalCmd]: sheller/src/main/java/me/panpf/shell/ConditionalCmd.java
