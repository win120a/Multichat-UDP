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

package ac.adproj.mchat.listener;

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

import ac.adproj.mchat.handler.Handler;
import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.ServerMessageHandler;
import ac.adproj.mchat.model.Listener;
import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.model.User;
import ac.adproj.mchat.service.CommonThreadPool;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.service.UserManager;
import ac.adproj.mchat.service.UserNameQueryService;
import ac.adproj.mchat.ui.CommonDialogs;

/**
 * 聊天服务器 UDP 协议通信类。
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
    private UserNameQueryService userNameQueryService;

    private int threadNumber = 0;
    
    private static ServerListener instance;
    
    /**
     * 获得此类的唯一实例。
     * 
     * @return 实例
     * @throws IOException 如果读写出错
     */
    public static ServerListener getInstance() throws IOException {
        if (instance == null) {
            instance = new ServerListener();
        }

        return instance;
    }

    /**
     * 构造服务端监听器类。
     * 
     * @throws IOException 如果读写出错
     */
    private ServerListener() throws IOException {
        init();
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

            try {
                MessageDistributor.getInstance().sendUiMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
    private void init() throws IOException {
        ServerMessageHandler handler = new ServerMessageHandler(this);

        BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>(16);

        ThreadFactory threadFactory = r -> {
            threadNumber++;
            return new Thread(r, "服务器 UDP 监听线程 - #" + threadNumber);
        };

        threadPool = new ThreadPoolExecutor(4, 16, 2, TimeUnit.MINUTES, bq, threadFactory);

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
            // 服务器发出的消息。
            if (MessageType.INCOMING_MESSAGE.tokenize(text).get("uuid").equals(Protocol.BROADCAST_MESSAGE_UUID)) {
                try {
                    // 同时更新 UI 和 WebSocket
                    MessageDistributor.getInstance().sendRawProtocolMessage(text);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            
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
    public void logoff(String uuid) throws IOException {
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
        
        CommonThreadPool.shutdown();
    }
}
