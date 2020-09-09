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

package ac.adproj.mchat.model;

/**
 * Interface of network connection listeners.
 * 
 * @author Andy Cheung
 * @since 2020/4/26
 * @see ProtocolStrings
 * @see AutoCloseable
 */
public interface Listener extends AutoCloseable {
    /**
     * Sends chatting message to a specified machine. (according to UUID)
     * 
     * @param message Message content.
     * @param uuid Client's UUID.
     */
    void sendMessage(String message, String uuid);

    /**
     * Sends raw protocol message to a specified machine. (according to UUID)
     * 
     * @param text Raw protocol message.
     * @param uuid Client's UUID.
     */
    void sendCommunicationData(String text, String uuid);
    
    /**
     * Determines whether the listener has established a connection.
     * 
     * @return If a connection exists between server and client.
     */
    boolean isConnected();
}
