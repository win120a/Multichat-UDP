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

import ac.adproj.mchat.model.User;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>User manager.</p>
 *
 * <p>Intended for sharing user registration information between WebSocket server and UDP server.</p>
 *
 * <p>This class is singleton.</p>
 *
 * <p><b>Note: Although the users of WebSocket server aren't managed by this class,
 * usernames of that are stored in this class.</b></p>
 * 
 * @author Andy Cheung
 * @since 2020/5/19
 */
public class UserManager implements Iterable<User> {
    private static final UserManager INSTANCE = new UserManager();
    
    /**
     * Obtain the only instance of UserManager.
     * 
     * @return The only instance.
     */
    public static UserManager getInstance() {
        return INSTANCE;
    }

    /**
     * User profile storage.
     */
    private Map<String, User> userProfile;

    /**
     * User Name storage (with copy of reserved names).
     */
    private Set<String> names;

    /**
     * Store the names that reserved by WebSocket users.
     */
    private Set<String> reservedNames;

    private UserManager() {
        userProfile = new ConcurrentHashMap<>(16);
        names = Collections.synchronizedSet(new HashSet<>());
        reservedNames = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Clears the UDP server's user profile.
     */
    public void clearAllProfiles() {
        userProfile.clear();
        names.clear();
        names.addAll(reservedNames);
    }

    /**
     * Query the "names" storage and determine the user name exists or not.
     *
     * @param name The username to query.
     * @return True if exists.
     */
    public boolean containsName(String name) {
        return names.contains(name);
    }
    
    /**
     * Query the "userProfile" storage and determine whether the UUID links to a user.
     *
     * @param uuid UUID to query.
     * @return True if the UUID links to a user.
     */
    public boolean containsUuid(String uuid) {
        return userProfile.containsKey(uuid);
    }

    /**
     * Delete the user profile that belongs to the UDP chatting server.
     *
     * @param uuid UUID of the user.
     * @return True if the profile is deleted successfully.
     */
    public User deleteUserProfile(String uuid) {
        names.remove(lookup(uuid).getName());
        return userProfile.remove(uuid);
    }

    /**
     * Obtain the corresponding nickname according to UUID.
     *
     * @param uuid UUID of the user.
     * @return The nickname of the user.
     */
    public String getName(String uuid) {
        return lookup(uuid).getName();
    }

    /**
     * Checks the user profile whether empty or not.
     *
     * @return True if the user profile storage is empty.
     */
    public boolean isEmptyUserProfile() {
        return userProfile.isEmpty();
    }

    /**
     * <p>Obtain the read-only iterator of the user profile.</p>
     * 
     * <p><b>Note: Removing is NOT supported in the iterator.</b></p>
     */
    @Override
    public Iterator<User> iterator() {
        return Collections.unmodifiableCollection(userProfile.values()).iterator();
    }

    /**
     * Obtain the corresponding User object according to the UUID.
     *
     * @param uuid UUID
     * @return The corresponding User object, or null if the UUID is not exists.
     */
    public User lookup(String uuid) {
        return userProfile.get(uuid);
    }

    /**
     * 注册用户到用户表。
     * @param uuid UUID 号
     * @param name 用户名
     * @param address 远端 IP 地址
     */
    public void register(String uuid, String name, SocketAddress address) {
        register(new User(uuid, address, name));
    }

    /**
     * 注册用户到用户表。（直接注册 User 对象）
     * @param u User 对象
     */
    public void register(User u) {
        userProfile.put(u.getUuid(), u);
        names.add(u.getName());
    }
    
    /**
     * 注册用户名，但不对应 User 对象。（占用用户名，主要为 WebSocket 服务器使用）
     * @param name 用户名。
     * @return 是否注册成功。
     */
    public boolean reserveName(String name) {
        if (!names.contains(name) && !reservedNames.contains(name)) {
            boolean fName = names.add(name);
            boolean rName = reservedNames.add(name);

            return fName && rName;
        }
        
        return false;
    }
    
    /**
     * 注销不对应 User 对象的用户名。（主要为 WebSocket 服务器使用）
     * @param name 用户名。
     * @return 是否注销成功。
     */
    public boolean undoReserveName(String name) {
        if (reservedNames.contains(name) && names.contains(name)) {
            boolean rName = reservedNames.remove(name);
            return names.remove(name) && rName;
        }
        
        return false;
    }

    /**
     * 获取用户表全部数据的 String 表示
     */
    @Override
    public String toString() {
        return userProfile.toString();
    }

    /**
     * 返回 UDP 用户地址表的只读视图。
     * @return 只读 UDP 用户地址表
     */
    public Collection<User> userProfileValueSet() {
        return Collections.unmodifiableCollection(userProfile.values());
    }
}
