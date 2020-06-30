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
 * <p><b>说明：WebSocket 的用户并不通过该类管理，但是其用户名会在此类保存。</b></p>
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

    /**
     * 清除全部注册用户信息。（不包含占位用户名信息）
     */
    public void clearAllProfiles() {
        userProfile.clear();
        names.clear();
    }

    /**
     * 查询注册用户表中是否存在这个用户名。
     * @param name 查询的用户名
     * @return 用户名存在，返回真。
     */
    public boolean containsName(String name) {
        return names.contains(name);
    }
    
    /**
     * 查询注册用户表中是否存在这个 UUID
     * @param name 查询的 UUID
     * @return UUID 存在，返回真。
     */
    public boolean containsUuid(String uuid) {
        return userProfile.containsKey(uuid);
    }

    /**
     * 删除注册用户数据。
     * @param uuid 用户 UUID
     * @return 是否删除成功
     */
    public User deleteUserProfile(String uuid) {
        names.remove(lookup(uuid).getName());
        return userProfile.remove(uuid);
    }

    /**
     * 获取 UUID 号对应用户名。
     * @param uuid
     * @return
     */
    public String getName(String uuid) {
        return lookup(uuid).getName();
    }

    /**
     * 判断用户信息是否空白。
     * @return 空白为真。
     */
    public boolean isEmptyUserProfile() {
        return userProfile.isEmpty();
    }

    /**
     * <p>获取 UDP 用户集合的只读迭代器。</p>
     * 
     * <p><b>此迭代器不可调用 remove 方法，否则产生异常。</b></p>
     */
    @Override
    public Iterator<User> iterator() {
        return Collections.unmodifiableCollection(userProfile.values()).iterator();
    }

    /**
     * 获取 UUID 对应的 User 对象。
     * @param uuid UUID
     * @return 对应 User 对象
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
