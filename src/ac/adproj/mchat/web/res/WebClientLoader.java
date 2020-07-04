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

package ac.adproj.mchat.web.res;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * WebSocket 客户端应用包的加载程序。
 * 
 * @author Andy Cheung
 * @since 2020/5/22
 */
public final class WebClientLoader {
    private WebClientLoader() {
        throw new UnsupportedOperationException("No instance! ");
    }

    private static final Logger LOG = LoggerFactory.getLogger(WebClientLoader.class);

    public static final String WAR_NAME_IN_RESOURCE_PATH = "webClient.war"; 
    /**
     * 加载应用包。（退出时会删除）
     * @return 应用包释放到的路径
     */
    public static String getWebappWarPath() {
        FileOutputStream fos = null;

        try (BufferedInputStream is = new BufferedInputStream(
                WebClientLoader.class.getResourceAsStream(WAR_NAME_IN_RESOURCE_PATH))) {
            
            File f = File.createTempFile("acmc-webC-", ".war");
            f.deleteOnExit();
            fos = new FileOutputStream(f);

            int dataBit;
            while ((dataBit = is.read()) != -1) {
                fos.write(dataBit);
            }

            return f.getAbsolutePath();
        } catch (IOException e) {
            LOG.error("Error in loading web app file.", e);
            
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
