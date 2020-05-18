/*
    Copyright (C) 2011-2020 Andy Cheung

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package ac.adproj.mchat.protocol;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ac.adproj.mchat.model.Handler;
import ac.adproj.mchat.model.Listener;
import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.model.User;
import ac.adproj.mchat.service.UserNameQueryService;
import ac.adproj.mchat.ui.CommonDialogs;

/**
 * 聊天服务器核心类。
 * 
 * @author Andy Cheung
 * @implNote 本服务器主要使用 DatagramChannel 来接受用户的 UDP 连接。其使用 ServerMessageHandler
 *           来进行信息处理， UserNameQueryService 来接受客户端对用户名是否使用的询问（用户名查询服务）。其使用
 *           ThreadPoolExecutor 作为线程池实现，以管理线程。
 * 
 * @see java.nio.channels.DatagramChannel
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see ServerMessageHandler
 * @see UserNameQueryService
 */
public class ServerListener implements Listener {

    private DatagramChannel serverDatagramChannel;

    private ExecutorService threadPool;

    private UserManager userManager = UserManager.getInstance();

    private Shell shell;
    private Consumer<String> uiAction;
    private UserNameQueryService userNameQueryService;

    private int threadNumber = 0;

    /**
     * 构造服务端监听器类。
     * 
     * @param shell    服务器 UI 窗口
     * @param uiAction 包装由服务器 UI 指定的行为，其在接受、处理完消息后执行。
     * @throws IOException 如果读写出错
     */
    public ServerListener(Shell shell, Consumer<String> uiAction) throws IOException {
        this.shell = shell;
        this.uiAction = uiAction;

        init(shell, uiAction);
    }

    /**
     * 读取消息
     * 
     * @param bb      写入到的字节缓冲区
     * @param handler 协议消息处理器
     * @param result  读取的字节数，-1 为结束
     * @param address 客户端地址
     */
    private void readMessage(ByteBuffer bb, Handler handler, Integer result, SocketAddress address) {

        if (result != -1) {

            bb.flip();

            StringBuffer sbuffer = new StringBuffer();

            while (bb.hasRemaining()) {
                sbuffer.append(StandardCharsets.UTF_8.decode(bb));
            }

            String message = handler.handleMessage(sbuffer.toString(), address);

            shell.getDisplay().syncExec(() -> {
                uiAction.accept(message);
            });

            bb.clear();
        }
    }

    /**
     * 业务逻辑初始化方法。
     * 
     * @param shell    服务器 UI 窗口
     * @param uiAction 包装由服务器 UI 指定的行为，其在接受、处理完消息后执行。
     * @throws IOException 如果读写出错
     */
    private void init(Shell shell, Consumer<String> uiAction) throws IOException {
        ServerMessageHandler handler = new ServerMessageHandler();

        BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>(16);

        ThreadFactory threadFactory = r -> {
            threadNumber++;
            return new Thread(r, "服务器 UDP 监听线程 - #" + threadNumber);
        };

        threadPool = new ThreadPoolExecutor(4, 16, 3000, TimeUnit.MILLISECONDS, bq, threadFactory);

        userNameQueryService = new UserNameQueryService();
        threadPool.submit(userNameQueryService);

        serverDatagramChannel = DatagramChannel.open();
        serverDatagramChannel.bind(new InetSocketAddress(Protocol.SERVER_PORT));

        // 接受 UDP 连接的线程执行体
        Runnable connectionReceivingRunnable = () -> {
            // 用于规避内部类变量 final 限制的List （可变类）
            List<SocketAddress> ll = Collections.synchronizedList(new LinkedList<>());

            while (true) {
                try {
                    final ByteBuffer bb = ByteBuffer.allocate(Protocol.BUFFER_SIZE);
                    SocketAddress address = serverDatagramChannel.receive(bb);

                    threadPool.execute(() -> {
                        ll.add(address);

                        readMessage(bb, handler, 0, ll.get(0));

                        ll.clear();
                    });

                } catch (Exception exc) {
                    if (exc.getClass() == ClosedByInterruptException.class) {
                        // 程序退出了会发生这个异常，忽略。
                        return;
                    }

                    exc.printStackTrace();

                    SoftReference<User> sr = null;

                    for (User user : userManager.userProfileValueSet()) {
                        if (user.getAddress().equals(ll.get(0))) {
                            sr = new SoftReference<>(user);
                        }
                    }

                    userManager.deleteUserProfile(sr.get().getUuid());

                    sr.clear();
                    sr = null;

                    ll.clear();
                }
            }
        };

        threadPool.execute(connectionReceivingRunnable);
    }

    /**
     * 服务端消息处理类。
     * 
     * @author Andy Cheung
     * @since 2020/4/26
     */
    private class ServerMessageHandler implements Handler {
        @Override
        public String handleMessage(String message, SocketAddress address) {
            if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
                // 用户注册
                String[] data = message.replace(Protocol.CONNECTING_GREET_LEFT_HALF, "")
                        .replace(Protocol.CONNECTING_GREET_RIGHT_HALF, "").split(Protocol.CONNECTING_GREET_MIDDLE_HALF);

                String uuid = data[0];
                String name = data[1];

                User userObject = new User(uuid, address, name);

                userManager.register(userObject);

                return "Client: " + uuid + " (" + name + ") Connected.";
            } else if (message.startsWith(Protocol.DEBUG_MODE_STRING)) {
                // 调试模式
                System.out.println(userManager.toString());
                return "";

            } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
                // 客户端请求注销
                SoftReference<String> uuid = new SoftReference<String>(
                        message.replace(Protocol.NOTIFY_LOGOFF_HEADER, ""));

                try {
                    System.out.println("Disconnecting: " + uuid.get());
                    logoff(uuid.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "Client: " + message.replace(Protocol.NOTIFY_LOGOFF_HEADER, "") + " Disconnected.";
            } else if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
                // 收到消息
                String[] data = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "").split(Protocol.MESSAGE_HEADER_MIDDLE_HALF);

                final int vaildDataArrayLength = 2;

                if (data.length < vaildDataArrayLength) {
                    return "";
                }

                String uuid = data[0];
                String messageText = data[1];

                if (!userManager.containsUuid(uuid)) {
                    // 不接收没有注册机器的任何信息
                    return "";
                }

                String nameOnlyProtocolMessage = message.replace(uuid, userManager.getName(uuid));

                ByteBuffer bb = ByteBuffer.wrap(nameOnlyProtocolMessage.getBytes(StandardCharsets.UTF_8));

                for (User u : userManager.userProfileValueSet()) {
                    try {
                        if (!uuid.equals(u.getUuid())) {
                            bb.rewind();

                            int bytes = 0;

                            while (bb.hasRemaining()) {
                                bytes = serverDatagramChannel.send(bb, u.getAddress());
                            }

                            System.out.println("Forwarding message to " + u.getUuid() + ", size: " + bytes
                                    + " message: " + messageText);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        shell.getDisplay().syncExec(() -> {
                            MessageDialog.openError(shell, "出错", "转发出错：" + e.getMessage());
                        });
                    }
                }

                message = userManager.getName(uuid) + ": " + messageText;
            }

            return message;
        }
    }

    @Override
    public boolean isConnected() {
        if (userManager.isEmptyUserProfile()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void sendCommunicationData(String text, String uuid) {
        final ByteBuffer bb = ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));

        if (uuid.equals(Protocol.BROADCAST_MESSAGE_UUID)) {
            for (User u : userManager.userProfileValueSet()) {
                try {
                    bb.rewind();

                    while (bb.hasRemaining()) {
                        serverDatagramChannel.send(bb, u.getAddress());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    CommonDialogs.errorDialog("发送信息出错: " + e.getMessage());
                }
            }
        } else {
            try {
                serverDatagramChannel.send(bb, userManager.lookup(uuid).getAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessage(String message, String uuid) {
        sendCommunicationData(MESSAGE_HEADER_LEFT_HALF + Protocol.BROADCAST_MESSAGE_UUID + MESSAGE_HEADER_MIDDLE_HALF
                + MESSAGE_HEADER_RIGHT_HALF + message, uuid);
    }

    /**
     * 被动注销指定客户端（UUID）的回调方法。
     * 
     * @param uuid 用户标识
     * @throws IOException 如果IO出错
     */
    private void logoff(String uuid) throws IOException {
        userManager.deleteUserProfile(uuid);
    }

    /**
     * 主动注销全部客户端。
     * 
     * @throws IOException 如果IO出错
     */
    public void logoffAll() throws IOException {
        userManager.userProfileValueSet().forEach((v) -> {
            try {
                final ByteBuffer bb = ByteBuffer
                        .wrap((Protocol.NOTIFY_LOGOFF_HEADER + "SERVER").getBytes(StandardCharsets.UTF_8));

                serverDatagramChannel.send(bb, v.getAddress());
            } catch (IOException e) {
                // Ignore
            }
        });
        
        userManager.clearAllProfiles();
    }

    /**
     * 关闭监听器，回收资源。
     */
    @Override
    public void close() throws Exception {
        userNameQueryService.stopSelf();
        threadPool.shutdownNow();
        userManager.clearAllProfiles();
        serverDatagramChannel.close();
    }
}
