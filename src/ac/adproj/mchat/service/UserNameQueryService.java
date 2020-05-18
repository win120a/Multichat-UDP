package ac.adproj.mchat.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.protocol.UserManager;

/**
 * 用户名检查服务的线程执行体。
 * 
 * @author Andy Cheung
 */
public class UserNameQueryService implements Runnable {
    private DatagramChannel dc;
    private boolean stopSelf;
    private UserManager userManager;

    public UserNameQueryService() throws IOException {
        dc = DatagramChannel.open();
        dc.configureBlocking(true);
        dc.bind(new InetSocketAddress(Protocol.SERVER_CHECK_DUPLICATE_PORT));
        userManager = UserManager.getInstance();
    }

    private void reInit() throws IOException {
        dc = DatagramChannel.open();
        dc.configureBlocking(true);
        dc.bind(new InetSocketAddress(Protocol.SERVER_CHECK_DUPLICATE_PORT));
    }

    @Override
    public void run() {
        ByteBuffer bb = ByteBuffer.allocate(Protocol.BUFFER_SIZE);
        StringBuffer buffer = new StringBuffer();

        while (!stopSelf) {

            try {

                if (!dc.isOpen()) {
                    reInit();
                }

                SocketAddress address = dc.receive(bb);

                bb.flip();

                while (bb.hasRemaining()) {
                    buffer.append(StandardCharsets.UTF_8.decode(bb));
                }

                String message = buffer.toString();

                buffer.delete(0, buffer.length());
                bb.clear();

                if (message.startsWith(Protocol.CHECK_DUPLICATE_REQUEST_HEADER)) {
                    String name = message.replace(Protocol.CHECK_DUPLICATE_REQUEST_HEADER, "");
                    String result = userManager.containsName(name) ? Protocol.USER_NAME_DUPLICATED
                            : Protocol.USER_NAME_NOT_EXIST;

                    bb.put(result.getBytes(StandardCharsets.UTF_8));

                    bb.flip();

                    dc.send(bb, address);

                    bb.clear();
                }
            } catch (IOException e) {
                String name = e.getClass().getName();
                if (name.contains("ClosedByInterruptException") || name.contains("AsynchronousCloseException")) {
                    // ignore
                    return;
                }

                e.printStackTrace();
            }
        }

        if (stopSelf) {
            try {
                dc.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void stopSelf() {
        this.stopSelf = true;
    }
}