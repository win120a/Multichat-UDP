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
    private static int threadNumber = 0;

    private static BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>(16);

    private static ThreadFactory threadFactory = r -> {
        threadNumber++;
        Thread t = new Thread(r, "零散线程池线程 - #" + threadNumber);
        return t;
    };

    private static ExecutorService threadPool = new ThreadPoolExecutor(2, 8, 1, TimeUnit.MINUTES, bq, threadFactory);
    
    public static void execute(Runnable r) {
        threadPool.execute(r);
    }
    
    public static void shutdown() {
        threadPool.shutdownNow();
    }
}
