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

/**
 * 消息分派器。
 * 
 * @author Andy Cheung
 * @since 2020/5/24
 */
public class MessageDistributor {
    private static MessageDistributor instance;
    
    static {
        instance = new MessageDistributor();
    }

    public static MessageDistributor getInstance() {
        return instance;
    }

    private BlockingQueue<String> uiMessages;
    private LinkedList<SubscriberCallback> callbacks;
    
    private class MessageDistributingService implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    String message = uiMessages.take();
                    
                    for (SubscriberCallback cb : callbacks) {
                        cb.onMessageReceived(message);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    
    /**
     * 定义收到信息后的回调方法。
     * 
     * @author Andy Cheung
     * @since 2020.5.24
     */
    public static interface SubscriberCallback {
        /**
         * 收到信息后的回调方法。
         * @param uiMessage 处理后的，对用户友好的信息。
         */
        void onMessageReceived(String uiMessage);
    }

    public MessageDistributor() {
        uiMessages = new LinkedBlockingQueue<>();
        callbacks = new LinkedList<>();
        CommonThreadPool.execute(new MessageDistributingService());
    }

    public void sendUIMessage(String message) throws InterruptedException {
        uiMessages.put(message);
    }
    
    /**
     * 直接将协议消息（只限 INCOMING_MESSAGE 类型的）转化为 UI 消息的快捷方法。
     * @param message “消息”类协议消息
     * @throws InterruptedException 如果操作被迫中断
     */
    public void sendRawProtocolMessage(String message) throws InterruptedException {
        Map<String, String> tresult = MessageType.INCOMING_MESSAGE.tokenize(message);
        uiMessages.put(tresult.get("uuid") + ": " + tresult.get("messageText"));
    }
    
    public void registerSubscriber(SubscriberCallback callback) {
        callbacks.add(callback);
    }
}
