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

import ac.adproj.mchat.crypto.ParamUtil;
import ac.adproj.mchat.crypto.SymmetricCryptoService;
import ac.adproj.mchat.crypto.key.SymmetricKeyService;
import ac.adproj.mchat.handler.ClientMessageHandler;
import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.MessageTypeConstants;
import ac.adproj.mchat.model.Listener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.service.CommonThreadPool;
import ac.adproj.mchat.service.MessageDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Map;
import java.util.UUID;

import static ac.adproj.mchat.handler.MessageType.INCOMING_MESSAGE;
import static ac.adproj.mchat.handler.MessageType.NOTIFY_LOGOFF;
import static ac.adproj.mchat.model.ProtocolStrings.*;
import static ac.adproj.mchat.util.CollectionUtils.mapOf;

/**
 * UDP Client Listener.
 *
 * @author Andy Cheung
 */
public class ClientListener implements Listener {
    private static final Logger LOG = LoggerFactory.getLogger(ClientListener.class);
    private final String name;
    private final Key key;
    private DatagramChannel socketChannel;
    private String uuid;

    private static final int TIMEOUT = 5000;

    /**
     * Constructs Client Listener with arguments.
     *
     * @param address   Server address.
     * @param port      Client port.
     * @param username  The user name.
     * @param keyFile   Path to the key file.
     * @throws IOException If I/O Error occurs.
     */
    public ClientListener(byte[] address, int port, String username,
                          String keyFile) throws IOException {
        this.name = username;
        this.key = keyFile.isEmpty() ? null : SymmetricKeyService.getInstance().readKeyFromFile(keyFile);

        init(address, port, username);
    }


    /**
     * Contacts server to check the user name whether duplicate or not.
     *
     * @param serverAddress The server address.
     * @param name          The user name to query.
     * @return True if the username is duplicate with others.
     * @throws IOException If I/O Error occurs.
     */
    public static boolean checkNameDuplicates(byte[] serverAddress, String name) throws IOException {
        DatagramChannel dc = DatagramChannel.open();

        ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);

        bb.put((ProtocolStrings.CHECK_DUPLICATE_REQUEST_HEADER + name).getBytes(StandardCharsets.UTF_8));
        bb.flip();

        StringBuilder buffer = new StringBuilder();

        try {

            dc.configureBlocking(true);

            InetSocketAddress ia = new InetSocketAddress(InetAddress.getByAddress(serverAddress),
                    ProtocolStrings.SERVER_CHECK_DUPLICATE_PORT);

            dc.send(bb, ia);

            bb.clear();

            dc.receive(bb);

            bb.flip();

            while (bb.hasRemaining()) {
                buffer.append(StandardCharsets.UTF_8.decode(bb));
            }

            return !buffer.toString().startsWith(ProtocolStrings.USER_NAME_NOT_EXIST);
        } finally {
            dc.close();
        }
    }

    /**
     * Initialization method.
     *
     * @param address   Server address.
     * @param port      Client UDP port.
     * @param username  The username.
     * @throws IOException If I/O error occurs.
     */
    private void init(byte[] address, int port, String username)
            throws IOException {
        socketChannel = DatagramChannel.open();

        socketChannel.bind(new InetSocketAddress(port));

        InetAddress ia = InetAddress.getByAddress(address);

        uuid = UUID.randomUUID().toString();

        initNioSocketConnection(ia, username);
    }

    /**
     * Initializes the UDP connection.
     *
     * @param ia        The address of client.
     * @param username  The username.
     */
    private void initNioSocketConnection(InetAddress ia, String username) {
        ClientMessageHandler handler = new ClientMessageHandler(force -> {
            if (Boolean.TRUE.equals(force)) {
                try {
                    onForceLogoff();
                } catch (IOException e) {
                    LOG.error("Logoff failed.", e);
                }
            } else {
                try {
                    close();
                } catch (Exception e) {
                    LOG.error("Logoff failed.", e);
                }
            }
        });

        try {
            socketChannel.connect(new InetSocketAddress(ia, SERVER_PORT));

            Map<String, String> info = mapOf(MessageTypeConstants.UUID, uuid, MessageTypeConstants.USERNAME, username);

            String greetMessage = MessageType.REGISTER.generateProtocolMessage(info);
            final ByteBuffer greetBuffer = ByteBuffer.wrap(greetMessage.getBytes());

            socketChannel.write(greetBuffer);
        } catch (IOException e) {
            LOG.error("Failed to connect.", e);
        }

        try {
            MessageDistributor.getInstance().sendUiMessage("Connected to Server, UserName: " + username + ", UUID: " + uuid);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            e.printStackTrace();
        }

        CommonThreadPool.execute(() -> readMessage(handler));
    }

    private String encryptMessage(String message) {
        byte[] ivBytes = ParamUtil.getIVFromString(uuid, 16);

        SymmetricCryptoService scs = SymmetricCryptoService.getInstance(key, ivBytes);

        try {
            return scs.encryptMessageToBase64String(message);
        } catch (InvalidKeyException e) {
            LOG.error("Invalid key!", e);
        }

        return "";
    }

    /**
     * Decrypts encrypted message.
     *
     * @param rawMessage Raw encrypted protocol message.
     * @return Decrypted message.
     */
    private String decryptMessage(String rawMessage) {
        try {
            byte[] ivBytes = ParamUtil.getIVFromString(uuid, 16);

            if (key != null && MessageType.getMessageType(rawMessage) == MessageType.INCOMING_MESSAGE) {
                Map<String, String> tokenizeResult = MessageType.INCOMING_MESSAGE.tokenize(rawMessage);
                String messageUuid = tokenizeResult.get(MessageTypeConstants.UUID);
                String encryptedText = tokenizeResult.get(MessageTypeConstants.MESSAGE_TEXT);
                String decryptedMessage = SymmetricCryptoService.getInstance(key, ivBytes).decryptMessageFromBase64String(encryptedText);

                Map<String, String> infoMap = mapOf(MessageTypeConstants.UUID, messageUuid,
                        MessageTypeConstants.MESSAGE_TEXT, decryptedMessage);

                rawMessage = INCOMING_MESSAGE.generateProtocolMessage(infoMap);
            }

            return rawMessage;
        } catch (InvalidKeyException | BadPaddingException e) {
            LOG.warn("Invalid Key! ", e);

            try {
                close();
            } catch (Exception ignored) {
                // ignored.
            }
        }

        return "";
    }

    private void readMessage(ClientMessageHandler handler) {
        while (socketChannel.isOpen()) {
            final ByteBuffer buffer = ByteBuffer.allocate(ProtocolStrings.BUFFER_SIZE);

            try {
                if (socketChannel.read(buffer) == -1) {
                    return;
                }

                buffer.flip();

                StringBuilder sbuffer = new StringBuilder();

                while (buffer.hasRemaining()) {
                    sbuffer.append(StandardCharsets.UTF_8.decode(buffer));
                }

                try {
                    String rawMessage = decryptMessage(sbuffer.toString());

                    MessageDistributor.getInstance().sendUiMessage(handler.handleMessage(rawMessage, socketChannel.getRemoteAddress()));
                } catch (IOException exc) {
                    LOG.error("Failed to get remote address.", exc);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();

                    LOG.error("Interrupted by other thread. ");
                }

                buffer.clear();
            } catch (Exception exc) {

                if (exc.getClass().getName().contains("AsynchronousCloseException")) {
                    return;
                }

                if (exc.getClass().getName().contains("InterruptedException")) {
                    break;
                }

                LOG.error("Failed to read message from server.", exc);
            }
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
            LOG.error("Failed to send message to server.", e);
        }
    }

    /**
     * Sends chatting message.
     *
     * @param message The message content.
     */
    public void sendMessage(String message) {
        if (message.equals(ProtocolStrings.DEBUG_MODE_STRING)) {
            sendCommunicationData(ProtocolStrings.DEBUG_MODE_STRING, uuid);
            return;
        }

        if (key != null) {
            sendMessage(encryptMessage(message), uuid);
        } else {
            sendMessage(message, uuid);
        }
    }

    @Override
    public void sendMessage(String message, String uuidValue) {
        sendCommunicationData(
                MESSAGE_HEADER_LEFT_HALF + uuidValue + MESSAGE_HEADER_MIDDLE_HALF + MESSAGE_HEADER_RIGHT_HALF + message,
                uuidValue);
    }

    /**
     * Notifies server the client is going to logoff.
     *
     * @throws IOException If I/O Error occurs.
     */
    public void logoff() throws IOException {
        if (isConnected()) {
            sendCommunicationData(NOTIFY_LOGOFF.generateProtocolMessage(mapOf("uuid", uuid)), uuid);
            socketChannel.close();
            socketChannel = null;
        }
    }

    /**
     * Callback method when the server kicks the connection.
     *
     * @throws IOException If I/O Error occurs.
     */
    private void onForceLogoff() throws IOException {
        if (isConnected()) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    /**
     * Closes connection & resources.
     */
    @Override
    public void close() throws Exception {
        logoff();

        CommonThreadPool.shutdown();
    }
}
