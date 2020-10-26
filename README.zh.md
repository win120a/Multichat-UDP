# 介绍
这是以 UDP 和 WebSocket 为传输协议的跨平台多端聊天程序的服务端和 Java (SWT) 客户端。整个项目以 Maven 的模块组织。

# 对应模块
## 消息加解密
crypto_api - 加解密 API

aes_encryption_impl - 加解密 API 的 AES 实现。

二者间以 SPI 机制松耦合。

## WebSocket 模块
websocket_mod - WebSocket 的服务端。

客户端：https://github.com/win120a/Multichat-WebSocket-Client

## 用户界面
mchat_ui - 基于 SWT / 命令行实现的界面。

## UDP 服务器
udp_socket_mod - 基于 UDP 的传输

## 通用模块
common_part - 提供一些诸如用户管理、消息分发器等公用的服务。

# 入口类
ac.adproj.mchat.ui.ClientUI - Java (SWT) 客户端的启动类

ac.adproj.mchat.ui.ServerUI - Java (SWT) 服务端的启动类

ac.adproj.mchat.ui.HeadlessServer - 命令行界面的服务端启动类
