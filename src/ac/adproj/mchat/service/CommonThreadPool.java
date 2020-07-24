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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 零散线程的线程池。
 * 
 * @author Andy Cheung
 * @since 2020/5/24
 */
public class CommonThreadPool {

    private CommonThreadPool() { throw new UnsupportedOperationException("No instance for you! "); }

    private static int threadNumber = 0;

    private static BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>(16);
    private static String comment = "";
    
    private static final byte[] MUTEX = new byte[1];
    private static final byte[] MUTEX_EXECUTE = new byte[1];

    private static ThreadFactory threadFactory = r -> {
        synchronized (MUTEX) {
            threadNumber++;
            
            String threadName = "零散线程池线程 - #" + threadNumber + (comment.isEmpty() ? "" : " - " + comment);
            
            Thread t = new Thread(r, threadName);
            
            comment = "";
            
            return t;
        }
    };

    private static ExecutorService threadPool = new ThreadPoolExecutor(3, 8, 1, TimeUnit.MINUTES, bq, threadFactory);
    
    /**
     * 向线程池提交线程。
     * 
     * @param r 线程执行体
     */
    public static synchronized void execute(Runnable r) {
        synchronized (MUTEX_EXECUTE) {
            threadPool.execute(r);
        }
    }
    
    /**
     * 向线程池提交线程，并根据说明文字命名。
     * 
     * @param r 线程执行体
     * @param stmt 说明文字
     */
    public static synchronized void execute(Runnable r, String stmt) {
        synchronized (MUTEX_EXECUTE) {
            synchronized (MUTEX) {
                comment = stmt;
            }

            threadPool.execute(r);
        }
    }
    
    public static void shutdown() {
        threadPool.shutdownNow();
    }
}
