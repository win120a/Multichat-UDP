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

package ac.adproj.mchat.service;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.MessageTypeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ac.adproj.mchat.handler.MessageTypeConstants.*;

/**
 * Message Distributor (like MQ).
 *
 * @author Andy Cheung
 * @since 2020/5/24
 */
public class MessageDistributor {
    private static MessageDistributor instance;
    
    static {
        instance = new MessageDistributor();
    }

    private MessageDistributor() {
        uiMessages = new LinkedBlockingQueue<>();
        callbacks = new LinkedList<>();
        CommonThreadPool.execute(new MessageDistributingService(), "Message Distributing Service");
    }

    /**
     * Obtain the only instance of MessageDistributor.
     *
     * @return The only instance.
     */
    public static MessageDistributor getInstance() {
        return instance;
    }

    /**
     * UI messages that are going to send.
     */
    private BlockingQueue<String> uiMessages;

    /**
     * Subscriber callbacks.
     */
    private LinkedList<SubscriberCallback> callbacks;

    /**
     * Logger of Message Distributing service.
     */
    private static final Logger MDS_LOG = LoggerFactory.getLogger(MessageDistributingService.class);

    /**
     * Message Distributing service, which aims to send message to subscribers.
     *
     * @implNote It's a runnable that will be executed by separate thread.
     */
    private class MessageDistributingService implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = uiMessages.take();
                    
                    for (SubscriberCallback cb : callbacks) {
                        cb.onMessageReceived(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    break;
                } catch (Exception e) {
                    MDS_LOG.error(String.format("MDS - Other exception occurred. [ThreadName: %s]",
                            Thread.currentThread().getName()), e);
                }
            }
        }
    }
    
    /**
     * Represents the callback method when MessageDistributingService receives message.
     * 
     * @author Andy Cheung
     * @since 2020.5.24
     */
    public interface SubscriberCallback {
        /**
         * Callback method when receives message.
         * @param uiMessage The UI message that shows to user.
         */
        void onMessageReceived(String uiMessage);
    }

    /**
     * Directly sends UI message to subscribers.
     * @param message The UI message.
     * @throws InterruptedException If the process of putting message into queue is interrupted.
     */
    public void sendUiMessage(String message) throws InterruptedException {
        uiMessages.put(message);
    }
    
    /**
     * Shortcut of converting the "INCOMING_MESSAGE" to UI message, and send the message to subscribers.
     *
     * @param message Raw protocol message whose type is "INCOMING_MESSAGE".
     * @throws InterruptedException If the process of putting message into queue is interrupted.
     */
    public void sendRawProtocolMessage(String message) throws InterruptedException {
        Map<String, String> tresult = MessageType.INCOMING_MESSAGE.tokenize(message);
        uiMessages.put(tresult.get(UUID) + ": " + tresult.get(MESSAGE_TEXT));
    }

    /**
     * Register the subscriber callback to this Message Distributor.
     *
     * @param callback The callback method when receives message.
     */
    public void registerSubscriber(SubscriberCallback callback) {
        callbacks.add(callback);
    }
}
