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

/**
 * Constants in Message Type Enumeration.
 * Used to set the key of map of the tokenized message.
 *
 * @author Andy Cheung
 */
public class MessageTypeConstants {
    private MessageTypeConstants() {
        throw new UnsupportedOperationException("NO MessageTypeConstants instance for you! ");
    }

    public static final String UUID = "uuid";
    public static final String MESSAGE_TEXT = "messageText";
    public static final String USERNAME = "username";
}
