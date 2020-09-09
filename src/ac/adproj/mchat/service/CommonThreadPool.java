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
 * Thread pool for scattered tasks.
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
            
            String threadName = "ScatteredTaskWorkerThread - #" + threadNumber + (comment.isEmpty() ? "" : " - " + comment);
            
            Thread t = new Thread(r, threadName);
            
            comment = "";
            
            return t;
        }
    };

    private static ExecutorService threadPool = new ThreadPoolExecutor(3, 8, 1, TimeUnit.MINUTES, bq, threadFactory);
    
    /**
     * Commit a task to the thread pool.
     * 
     * @param r Runnable of the task.
     */
    public static synchronized void execute(Runnable r) {
        synchronized (MUTEX_EXECUTE) {
            threadPool.execute(r);
        }
    }
    
    /**
     * Commit a task to the thread pool, and name it with a statement.
     * 
     * @param r Runnable of the task.
     * @param stmt Statement of the worker thread.
     */
    public static synchronized void execute(Runnable r, String stmt) {
        synchronized (MUTEX_EXECUTE) {
            synchronized (MUTEX) {
                comment = stmt;
            }

            threadPool.execute(r);
        }
    }

    /**
     * Shutdown the thread pool.
     */
    public static void shutdown() {
        threadPool.shutdownNow();
    }
}
