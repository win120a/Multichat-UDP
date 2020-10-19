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

package ac.adproj.mchat.handler;

import java.util.Map;

import static ac.adproj.mchat.model.ProtocolStrings.*;
import static ac.adproj.mchat.util.CollectionUtils.emptyMap;
import static ac.adproj.mchat.util.CollectionUtils.mapOf;

/**
 * Message type enumeration. Also provides methods to tokenize and generate protocol message.
 * 
 * @author Andy Cheung
 * @since 2020/5/19
 */
public enum MessageType {
    /**
     * User registration.
     */
    REGISTER {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            String message = CONNECTING_GREET_LEFT_HALF;
            message += elements.get(MessageTypeConstants.UUID);
            message += CONNECTING_GREET_MIDDLE_HALF;
            message += elements.get(MessageTypeConstants.USERNAME);
            message += CONNECTING_GREET_RIGHT_HALF;

            return message;
        }

        @Override
        public Map<String, String> tokenize(String message) {
            String[] data = message.replace(CONNECTING_GREET_LEFT_HALF, "")
                    .replace(CONNECTING_GREET_RIGHT_HALF, "").split(CONNECTING_GREET_MIDDLE_HALF);

            String uuid = data[0];
            String name = data[1];

            return mapOf(MessageTypeConstants.UUID, uuid, MessageTypeConstants.USERNAME, name);
        }
    },

    /**
     * Notification of user log off (or disconnect).
     */
    NOTIFY_LOGOFF {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            return NOTIFY_LOGOFF_HEADER + elements.get("uuid");
        }

        @Override
        public Map<String, String> tokenize(String message) {
            String targetUuid = message.replace(NOTIFY_LOGOFF_HEADER, "");

            return mapOf(MessageTypeConstants.UUID, targetUuid);
        }
    },

    /**
     * Incoming chat message.
     */
    INCOMING_MESSAGE {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            String message = MESSAGE_HEADER_LEFT_HALF;
            message += elements.get(MessageTypeConstants.UUID);
            message += MESSAGE_HEADER_MIDDLE_HALF;
            message += MESSAGE_HEADER_RIGHT_HALF + elements.get(MessageTypeConstants.MESSAGE_TEXT);

            return message;
        }

        @Override
        public Map<String, String> tokenize(String message) {
            String[] msgData = message.replace(MESSAGE_HEADER_LEFT_HALF, "")
                    .replace(MESSAGE_HEADER_RIGHT_HALF, "").split(MESSAGE_HEADER_MIDDLE_HALF);

            final int validDataArrayLength = 2;

            if (msgData.length < validDataArrayLength) {
                return emptyMap();
            }

            String fromUuid = msgData[0];
            String messageText = msgData[1];

            return mapOf(MessageTypeConstants.UUID, fromUuid, MessageTypeConstants.MESSAGE_TEXT, messageText);
        }
    },

    /**
     * Debugging mode.
     */
    DEBUG {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            return "";
        }

        @Override
        public Map<String, String> tokenize(String message) {
            return mapOf();
        }
    },

    /**
     * Request of User Name query service.
     */
    USERNAME_QUERY_REQUEST {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            return CHECK_DUPLICATE_REQUEST_HEADER + elements.get(MessageTypeConstants.USERNAME);
        }

        @Override
        public Map<String, String> tokenize(String message) {
            return mapOf(MessageTypeConstants.USERNAME, message.replace(CHECK_DUPLICATE_REQUEST_HEADER, ""));
        }
    },

    /**
     * Notification message of invalid encryption key.
     */
    INVALID_KEY {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            return INVALID_KEY_NOTIFYING_STRING_HEADER + elements.get(MessageTypeConstants.UUID);
        }

        @Override
        public Map<String, String> tokenize(String message) {
            return mapOf(MessageTypeConstants.UUID, message.replace(INVALID_KEY_NOTIFYING_STRING_HEADER, ""));
        }
    },
    
    /**
     * Unknown protocol message.
     */
    UNKNOWN {
        @Override
        public String generateProtocolMessage(Map<String, String> elements) {
            return "";
        }

        @Override
        public Map<String, String> tokenize(String message) {
            return mapOf();
        }
    };
    
    /**
     * Get the corresponding MessageType instance according to protocol string.
     * 
     * @param message Raw protocol message.
     * @return The corresponding Message Type object.
     */
    public static MessageType getMessageType(String message) {
        if (message.startsWith(CONNECTING_GREET_LEFT_HALF)) {
            return REGISTER;
        } else if (message.startsWith(DEBUG_MODE_STRING)) {
            return DEBUG;
        } else if (message.startsWith(NOTIFY_LOGOFF_HEADER)) {
            return NOTIFY_LOGOFF;
        } else if (message.startsWith(MESSAGE_HEADER_LEFT_HALF)) {
            return INCOMING_MESSAGE;
        } else if (message.startsWith(CHECK_DUPLICATE_REQUEST_HEADER)) {
            return USERNAME_QUERY_REQUEST;
        } else if (message.startsWith(INVALID_KEY_NOTIFYING_STRING_HEADER)) {
            return INVALID_KEY;
        }

        return MessageType.UNKNOWN;
    }

    /**
     * Generate protocol string according to "elements" map.
     *
     * @param elements A map which contains information to generate protocol message.
     * @return Generated protocol string.
     */
    public abstract String generateProtocolMessage(Map<String, String> elements);

    /**
     * Tokenize message, and put the tokenized elements into a map.
     * 
     * @param message Raw protocol message.
     * @return A map which contains protocol message elements.
     */
    public abstract Map<String, String> tokenize(String message);
}
