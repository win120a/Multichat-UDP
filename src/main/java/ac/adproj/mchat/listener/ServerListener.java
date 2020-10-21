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

import ac.adproj.mchat.crypto.AESCryptoServiceImpl;
import ac.adproj.mchat.crypto.ParamUtil;
import ac.adproj.mchat.crypto.SymmetricCryptoService;
import ac.adproj.mchat.handler.Handler;
import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.MessageTypeConstants;
import ac.adproj.mchat.handler.ServerMessageHandler;
import ac.adproj.mchat.model.Listener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.model.User;
import ac.adproj.mchat.service.CommonThreadPool;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.service.UserManager;
import ac.adproj.mchat.service.UserNameQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ac.adproj.mchat.model.ProtocolStrings.*;
import static ac.adproj.mchat.util.CollectionUtils.mapOf;

/**
 * UDP Server Listener.
 * 
 * @author Andy Cheung
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
    private Key key;

    private AtomicInteger threadNumber = new AtomicInteger();

    private static volatile ServerListener instance;

    private static final Logger LOG = LoggerFactory.getLogger(ServerListener.class);

    /**
     * Obtain the only instance of ServerListener. If the instance does not exist, initialize the server listener.
     * 
     * @return The instance.
     * @throws IOException If I/O error occurs.
     */
    public static ServerListener getInstance() throws IOException {
        if (instance == null) {
            synchronized (ServerListener.class) {
                if (instance == null) {
                    instance = new ServerListener();
                }
            }
        }

        return instance;
    }

    /**
     * Sets encryption key. This method can be invoked only once.
     *
     * @param key The encryption key.
     */
    public void setKey(Key key) {
        if (this.key == null) {
            this.key = key;
        }
    }

    /**
     * Constructor.
     *
     * @throws IOException If I/O error occurs.
     */
    private ServerListener() throws IOException {
        init();
    }

    /**
     * Read message.
     * 
     * @param bb      Destination byte buffer.
     * @param handler Protocol message handler.
     * @param address Client address.
     */
    private void readMessage(ByteBuffer bb, Handler handler, SocketAddress address) {

        bb.flip();

        StringBuilder sbuffer = new StringBuilder();

        while (bb.hasRemaining()) {
            sbuffer.append(StandardCharsets.UTF_8.decode(bb));
        }

        String rawMessage = sbuffer.toString();

        if (key != null && MessageType.getMessageType(rawMessage) == MessageType.INCOMING_MESSAGE) {
            Map<String, String> tokenizeResult = MessageType.INCOMING_MESSAGE.tokenize(rawMessage);
            String uuid = tokenizeResult.get(MessageTypeConstants.UUID);
            String encryptedText = tokenizeResult.get(MessageTypeConstants.MESSAGE_TEXT);

            try {
                byte[] ivBytes = ParamUtil.getIVFromString(uuid, 16);
                
                // << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)
                
                String decryptedMessage = new AESCryptoServiceImpl(key, ivBytes).decryptMessageFromBase64String(encryptedText);

                Map<String, String> info = mapOf(MessageTypeConstants.UUID,
                                                uuid,
                                                MessageTypeConstants.MESSAGE_TEXT,
                                                decryptedMessage);

                rawMessage = MessageType.INCOMING_MESSAGE.generateProtocolMessage(info);
                
            } catch (InvalidKeyException e) {
                LOG.warn("Invalid Key! ");
                String ps = MessageType.INVALID_KEY.generateProtocolMessage(mapOf(MessageTypeConstants.UUID, uuid));
                sendCommunicationData(ps, uuid);
            } catch (BadPaddingException e) {
                bb.clear();
                LOG.warn(String.format("Incorrect Key: [UUID = %s]", uuid), e);
                sendCommunicationData(ProtocolStrings.INVALID_KEY_NOTIFYING_STRING_HEADER + uuid, uuid);
                return;
            }
        }

        String message = handler.handleMessage(rawMessage, address);

        try {
            MessageDistributor.getInstance().sendUiMessage(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Sending message, but the process was interrupted by other thread.", e);
        }

        bb.clear();
    }

    /**
     * Method to initialize.
     *
     * @throws IOException If I/O error occurs.
     */
    private void init() throws IOException {
        ServerMessageHandler handler = new ServerMessageHandler(this);

        BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>(16);

        ThreadFactory threadFactory = r -> new Thread(r, "服务器 UDP 监听线程 - #" + threadNumber.incrementAndGet());

        threadPool = new ThreadPoolExecutor(4, 16, 2, TimeUnit.MINUTES, bq, threadFactory);

        userNameQueryService = new UserNameQueryService();
        threadPool.submit(userNameQueryService);

        serverDatagramChannel = DatagramChannel.open();
        serverDatagramChannel.bind(new InetSocketAddress(ProtocolStrings.SERVER_PORT));

        // Runnable to accept UDP connection.
        Runnable connectionReceivingRunnable = () -> receiveConnection(handler);

        threadPool.execute(connectionReceivingRunnable);
    }

    /**
     * Receives UDP Connection, and dispatches the connection to message receiving method.
     *
     * @param handler Server message handler.
     */
    private void receiveConnection(ServerMessageHandler handler) {
        // In order to make the the socket address mutable.
        List<SocketAddress> ll = Collections.synchronizedList(new LinkedList<>());

        Thread currentThread = Thread.currentThread();

        final ByteBuffer bb = ByteBuffer.allocate(ProtocolStrings.BUFFER_SIZE);

        while (!currentThread.isInterrupted()) {
            try {
                SocketAddress address = serverDatagramChannel.receive(bb);

                threadPool.execute(() -> {
                    ll.add(address);

                    readMessage(bb, handler, ll.get(0));

                    ll.clear();
                });

            } catch (Exception exc) {
                if (exc.getClass() == ClosedByInterruptException.class) {
                    // Occurs when program is going to exit, ignore.
                    return;
                }

                SoftReference<User> sr = null;

                for (User user : userManager.userProfileValueSet()) {
                    if (user.getAddress().equals(ll.get(0))) {
                        sr = new SoftReference<>(user);
                    }
                }

                if (sr != null && sr.get() != null) {
                    userManager.deleteUserProfile(sr.get().getUuid());

                    LOG.warn(String.format("Got exception when receiving message. UUID: %s", sr.get().getUuid()), exc);

                    sr.clear();

                    // Clear the reference for garbage collection.
                    sr = null;
                }

                ll.clear();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return !userManager.isEmptyUserProfile();
    }

    @Override
    public void sendCommunicationData(String text, String uuid) {
        if (uuid.equals(ProtocolStrings.BROADCAST_MESSAGE_UUID)) {
            // Broadcast from chatting server.

            sendServerBroadcast(text, uuid);
        } else {
            if (key != null) {
                byte[] ivBytes = ParamUtil.getIVFromString(uuid, 16);

                SymmetricCryptoService scs = new AESCryptoServiceImpl(key, ivBytes);

                String rawMessage = MessageType.INCOMING_MESSAGE.tokenize(text).get(MessageTypeConstants.MESSAGE_TEXT);
                String nickname = MessageType.INCOMING_MESSAGE.tokenize(text).get(MessageTypeConstants.UUID);

                try {
                    String message = scs.encryptMessageToBase64String(rawMessage);
                    text = MESSAGE_HEADER_LEFT_HALF + nickname + MESSAGE_HEADER_MIDDLE_HALF
                            + MESSAGE_HEADER_RIGHT_HALF + message;

                } catch (InvalidKeyException e) {
                    LOG.error("Invalid key!", e);
                }
            }
            
            final ByteBuffer bb = ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));

            try {
                serverDatagramChannel.send(bb, userManager.lookup(uuid).getAddress());
            } catch (IOException e) {
                LOG.error("Error in sending message.", e);
            }
        }
    }

    private void sendServerBroadcast(String text, String uuid) {
        if (MessageType.INCOMING_MESSAGE.tokenize(text).get(MessageTypeConstants.UUID).equals(ProtocolStrings.BROADCAST_MESSAGE_UUID)) {
            try {
                // Update UI and WebSocket simultaneously.

                MessageDistributor.getInstance().sendRawProtocolMessage(text);
            } catch (InterruptedException e1) {
                LOG.warn("Sending message to other UI components, but the process was interrupted by other thread.", e1);
                Thread.currentThread().interrupt();
            }
        }

        String encryptedText = text;

        for (User u : userManager.userProfileValueSet()) {
            if (key != null) {
                byte[] ivBytes = ParamUtil.getIVFromString(u.getUuid(), 16);

                SymmetricCryptoService scs = new AESCryptoServiceImpl(key, ivBytes);

                String rawMessage = MessageType.INCOMING_MESSAGE.tokenize(text).get(MessageTypeConstants.MESSAGE_TEXT);

                try {
                    String message = scs.encryptMessageToBase64String(rawMessage);
                    encryptedText = MESSAGE_HEADER_LEFT_HALF + uuid + MESSAGE_HEADER_MIDDLE_HALF
                            + MESSAGE_HEADER_RIGHT_HALF + message;

                } catch (InvalidKeyException e) {
                    LOG.error("Invalid key!", e);
                }
            }

            final ByteBuffer bb = ByteBuffer.wrap(encryptedText.getBytes(StandardCharsets.UTF_8));

            try {
                bb.rewind();

                while (bb.hasRemaining()) {
                    serverDatagramChannel.send(bb, u.getAddress());
                }
            } catch (IOException e) {
                LOG.error("Error in sending message.", e);
            }
        }
    }


    @Override
    public void sendMessage(String message, String uuid) {
        sendCommunicationData(MESSAGE_HEADER_LEFT_HALF + ProtocolStrings.BROADCAST_MESSAGE_UUID + MESSAGE_HEADER_MIDDLE_HALF
                + MESSAGE_HEADER_RIGHT_HALF + message, uuid);
    }

    /**
     * Callback when server got the logoff request from client.
     * 
     * @param uuid The UUID of the user.
     * @throws IOException If I/O error occurs.
     */
    public void logoff(String uuid) throws IOException {
        userManager.deleteUserProfile(uuid);
    }

    /**
     * Let all clients connected to server logoff.
     * 
     * @throws IOException If I/O error occurs.
     */
    public void logoffAll() throws IOException {
        userManager.userProfileValueSet().forEach(v -> {
            try {
                final ByteBuffer bb = ByteBuffer
                        .wrap((ProtocolStrings.NOTIFY_LOGOFF_HEADER + "SERVER").getBytes(StandardCharsets.UTF_8));

                serverDatagramChannel.send(bb, v.getAddress());
            } catch (IOException e) {
                // Ignore
            }
        });

        userManager.clearAllProfiles();
    }

    /**
     * Close the listener and release resources.
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
