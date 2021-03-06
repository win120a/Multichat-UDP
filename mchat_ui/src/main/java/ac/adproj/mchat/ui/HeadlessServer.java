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

package ac.adproj.mchat.ui;

import ac.adproj.mchat.crypto.key.AESKeyServiceImpl;
import ac.adproj.mchat.crypto.key.SymmetricKeyService;
import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.web.WebServerStarter;

import java.io.IOException;

/**
 * 无图形界面启动类。
 * 
 * @author Andy Cheung
 */
public class HeadlessServer {
    public static void main(String[] args) {
        System.out.println("AC Multichat Server (UDP, Headless)");
        System.out.println(String.format(
                            "Accepting UDP Connection on port %d, WebSocket connection on %d."
                                , ProtocolStrings.SERVER_PORT, 8090));
        System.out.println("Key File: " + (args.length == 0 ? "<No key file>" : args[0]));
        System.out.println();
        
        try (ServerListener listener = ServerListener.getInstance(); WebServerStarter starter = new WebServerStarter()) {
            if (args.length != 0) {
                listener.setKey(SymmetricKeyService.getInstance().readKeyFromFile(args[0]));
            }

            MessageDistributor.getInstance().registerSubscriber(System.out::println);
            
            starter.start(8090);
            
            Thread.currentThread().join();
        } catch (Exception e1) {
            e1.printStackTrace();
            System.exit(-1);
        }
    }
}
