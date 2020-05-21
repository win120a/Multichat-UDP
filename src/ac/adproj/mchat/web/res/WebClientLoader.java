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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class WebClientLoader {
    public static String getWebappWarPath() {
        FileOutputStream fos = null;

        try (BufferedInputStream is = new BufferedInputStream(
                WebClientLoader.class.getResourceAsStream("webClient.war"))) {
            
            File f = File.createTempFile("acmc-webC-", ".war");
            fos = new FileOutputStream(f);

            int dataBit;
            while ((dataBit = is.read()) != -1) {
                fos.write(dataBit);
            }

            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            
            return null;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
