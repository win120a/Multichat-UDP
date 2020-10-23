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

import java.net.SocketAddress;
import java.util.Objects;

/**
 * Wrapper of User properties.
 * 
 * @author Andy Cheung
 * @since 2020-4-27
 */
public final class User {
    /**
     * UUID of the user.
     */
    private String uuid;
    
    /**
     * Socket address.
     */
    private SocketAddress address;
    
    /**
     * Username.
     */
    private String name;

    public User(String uuid, SocketAddress address, String name) {
        super();
        this.uuid = uuid;
        this.address = address;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        User other = (User) obj;
        return Objects.equals(uuid, other.uuid);
    }
    
    @Override
    public String toString() {
        return String.format("{uuid=%s, address=%s, username=%s}", getUuid(), getAddress(), getName());
    }
}
