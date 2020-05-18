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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ac.adproj.mchat.handler.Handler;
import ac.adproj.mchat.model.Listener;
import ac.adproj.mchat.model.Protocol;

/**
 * 客户端监听器。
 * 
 * @author Andy Cheung
 */
public class ClientListener implements Listener {
    private DatagramChannel socketChannel;
    private String uuid;
    private String name;

    /**
     * 构造客户端监听器类。
     * 
     * @param shell    服务器 UI 窗口
     * @param uiActions 包装由服务器 UI 指定的行为，其在接受、处理完消息后执行。
     * @param address 服务器地址
     * @param port 客户端端口
     * @param username 用户名
     */
    public ClientListener(Shell shell, Consumer<String> uiActions, byte[] address, int port, String username)
            throws IOException {
        this.name = username;
        init(shell, uiActions, address, port, username);
    }

    /**
     * 业务逻辑初始化方法。
     * 
     * @param shell    服务器 UI 窗口
     * @param uiActions 包装由服务器 UI 指定的行为，其在接受、处理完消息后执行。
     * @param address 服务器地址
     * @param port 客户端端口
     * @param username 用户名
     * @throws IOException 如果读写出错
     */
    private void init(Shell shell, Consumer<String> uiActions, byte[] address, int port, String username)
            throws IOException {
        socketChannel = DatagramChannel.open();

        socketChannel.bind(new InetSocketAddress(port));

        InetAddress ia = InetAddress.getByAddress(address);

        uuid = UUID.randomUUID().toString();

        initNioSocketConnection(shell, uiActions, ia, username);
    }

    /**
     * 向服务器查询用户名是否重复。
     * 
     * @param serverAddress 服务器地址
     * @param name          待查用户名
     * @return 是否重复
     * @throws IOException 如果IO出现异常
     */
    public static boolean checkNameDuplicates(byte[] serverAddress, String name) throws IOException {
        DatagramChannel dc = DatagramChannel.open();

        ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);

        bb.put((Protocol.CHECK_DUPLICATE_REQUEST_HEADER + name).getBytes(StandardCharsets.UTF_8));
        bb.flip();

        StringBuffer buffer = new StringBuffer();

        try {

            dc.configureBlocking(true);
            dc.send(bb, new InetSocketAddress(InetAddress.getByAddress(serverAddress),
                    Protocol.SERVER_CHECK_DUPLICATE_PORT));

            bb.clear();

            dc.receive(bb);

            bb.flip();

            while (bb.hasRemaining()) {
                buffer.append(StandardCharsets.UTF_8.decode(bb));
            }

            if (buffer.toString().startsWith(Protocol.USER_NAME_NOT_EXIST)) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            dc.close();
        }
    }

    /**
     * 初始化 NIO UDP 连接。
     * 
     * @param shell     客户端 UI 窗体
     * @param uiActions 由客户端指定的 UI 行为，在处理完消息后执行
     * @param ia        客户端地址
     * @param username  用户名
     */
    private void initNioSocketConnection(Shell shell, Consumer<String> uiActions, InetAddress ia, String username) {
        ClientMessageHandler handler = new ClientMessageHandler();

        try {
            socketChannel.connect(new InetSocketAddress(ia, SERVER_PORT));

            String greetMessage = CONNECTING_GREET_LEFT_HALF + uuid + CONNECTING_GREET_MIDDLE_HALF + username;
            final ByteBuffer greetBuffer = ByteBuffer.wrap(greetMessage.getBytes());

            socketChannel.write(greetBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        shell.getDisplay().syncExec(() -> {
            uiActions.accept("Connected to Server, UserName: " + username + ", UUID: " + uuid);
        });

        new Thread(() -> {

            while (true) {
                final ByteBuffer buffer = ByteBuffer.allocate(Protocol.BUFFER_SIZE);

                Display display = shell.getDisplay();

                try {
                    int result = socketChannel.read(buffer);

                    if (result != -1) {
                        buffer.flip();

                        StringBuffer sbuffer = new StringBuffer();

                        while (buffer.hasRemaining()) {
                            sbuffer.append(StandardCharsets.UTF_8.decode(buffer));
                        }

                        display.syncExec(() -> {
                            try {
                                uiActions.accept(
                                        handler.handleMessage(sbuffer.toString(), socketChannel.getRemoteAddress()));
                            } catch (IOException exc) {
                                exc.printStackTrace();

                                display.syncExec(() -> {
                                    MessageDialog.openError(shell, "出错", "客户机读取出错：" + exc.getMessage());
                                });
                            }
                        });

                        buffer.clear();
                    }
                } catch (Exception exc) {

                    if (exc.getClass().getName().contains("AsynchronousCloseException")) {
                        return;
                    }

                    exc.printStackTrace();

                    display.syncExec(() -> MessageDialog.openError(shell, "出错", "客户机读取出错：" + exc.getMessage()));
                }
            }
        }).start();
    }

    /**
     * 客户端消息处理类。
     * 
     * @author Andy Cheung
     */
    private class ClientMessageHandler implements Handler {
        @Override
        public String handleMessage(String message, SocketAddress address) {
            if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
                message = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_MIDDLE_HALF, ": ");

            } else if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
                return "";
            } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
                try {
                    onForceLogoff();
                } catch (IOException e) {
                    // Ignore.
                }

                return "Server closed the connection.";
            }

            return message;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getUserName() {
        return name;
    }

    @Override
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isOpen();
    }

    @Override
    public void sendCommunicationData(String text, String uuid) {
        final ByteBuffer bb = ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));

        try {
            socketChannel.write(bb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送聊天信息。
     * 
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        if (message.equals(Protocol.DEBUG_MODE_STRING)) {
            sendCommunicationData(Protocol.DEBUG_MODE_STRING, uuid);
            return;
        }
        
        sendMessage(message, uuid);
    }

    @Override
    public void sendMessage(String message, String uuidValue) {
        sendCommunicationData(
                MESSAGE_HEADER_LEFT_HALF + uuidValue + MESSAGE_HEADER_MIDDLE_HALF + MESSAGE_HEADER_RIGHT_HALF + message,
                uuidValue);
    }

    /**
     * 向服务器申请注销。
     * 
     * @throws IOException 出现IO错误
     */
    public void logoff() throws IOException {
        if (isConnected()) {
            sendCommunicationData(NOTIFY_LOGOFF_HEADER + uuid, uuid);
            socketChannel.close();
            socketChannel = null;
        }
    }

    /**
     * 当服务器强行下线时的回调方法。
     * 
     * @throws IOException 出现IO错误
     */
    private void onForceLogoff() throws IOException {
        if (isConnected()) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    /**
     * 关闭连接，回收资源。
     */
    @Override
    public void close() throws Exception {
        logoff();
    }
}
