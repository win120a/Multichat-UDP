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

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ac.adproj.mchat.model.User;

/**
 * <p>用户管理器。</p>
 * 
 * <p>此类为单例类，主要目的是在 WebSocket 服务器和 UDP 服务器中间共享用户注册的数据。</p>
 * 
 * @author Andy Cheung
 * @since 2020/5/19
 */
public class UserManager implements Iterable<User> {
    private static UserManager instance;
    
    /**
     * 获得此类的唯一实例。
     * 
     * @return 实例
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    private Map<String, User> userProfile;

    private Set<String> names;
    
    // Store the reserved names.
    private Set<String> reservedNames;

    private UserManager() {
        userProfile = new ConcurrentHashMap<>(16);
        names = Collections.synchronizedSet(new HashSet<>());
        reservedNames = Collections.synchronizedSet(new HashSet<>());
    }

    public void clearAllProfiles() {
        userProfile.clear();
        names.clear();
    }

    public boolean containsName(String name) {
        return names.contains(name);
    }
    
    public boolean containsUuid(String uuid) {
        return userProfile.containsKey(uuid);
    }

    public User deleteUserProfile(String uuid) {
        names.remove(lookup(uuid).getName());
        return userProfile.remove(uuid);
    }

    public String getName(String uuid) {
        return lookup(uuid).getName();
    }

    public boolean isEmptyUserProfile() {
        return userProfile.isEmpty();
    }

    @Override
    public Iterator<User> iterator() {
        return userProfile.values().iterator();
    }

    public User lookup(String uuid) {
        return userProfile.get(uuid);
    }

    public void register(String uuid, String name, SocketAddress address) {
        register(new User(uuid, address, name));
    }

    public void register(User u) {
        userProfile.put(u.getUuid(), u);
        names.add(u.getName());
    }
    
    public boolean reserveName(String name) {
        if (!names.contains(name) && !names.contains(name)) {
            return names.add(name) & reservedNames.add(name);
        }
        
        return false;
    }
    
    public boolean undoReserveName(String name) {
        if (reservedNames.contains(name) && names.contains(name)) {
            return reservedNames.remove(name) & names.remove(name);
        }
        
        return false;
    }

    @Override
    public String toString() {
        return userProfile.toString();
    }

    public Collection<User> userProfileValueSet() {
        return userProfile.values();
    }
}
