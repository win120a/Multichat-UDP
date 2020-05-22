package ac.adproj.mchat.listener;

import ac.adproj.mchat.service.UserManager;

public class WebSocketBridge {
    private static ServerListener listener;
    
    public static boolean reserveName(String name) {
        return UserManager.getInstance().reserveName(name);
    }
}