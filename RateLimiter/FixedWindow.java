package RateLimiter;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindow implements RateLimiter{
    private static volatile FixedWindow fixedWindow;
    private final long timeWindowInMilis;
    private final int bucketToken;
    private final ConcurrentHashMap<String, Window> requestLog;

    private FixedWindow( int token, long timeWindow) {
        this.timeWindowInMilis = timeWindow;
        this.bucketToken = token;
        this.requestLog = new ConcurrentHashMap<>();
    }

    public static synchronized FixedWindow getInstance(int bucketToken, int timeWindowInMilis) {
        if(fixedWindow == null) {
            fixedWindow = new FixedWindow(bucketToken, timeWindowInMilis);
        } else if(fixedWindow.bucketToken != bucketToken || fixedWindow.timeWindowInMilis != timeWindowInMilis) {
//            check if need to re-initialize or throw an error
        }

        return fixedWindow;
    }

//    public static FixedWindow getFixedWindowInstance(int maxRequest, long windowSizeMillis){
//        if(fixedWindow == null) {
//            synchronized (fixedWindow) {
//                if(fixedWindow == null) {
//                    fixedWindow = new FixedWindow(maxRequest, windowSizeMillis);
//                }
//            }
//        }
//        return fixedWindow;
//    }

    @Override
    public boolean grantAccess(String clientID) {
        long currentTimeMillis = System.currentTimeMillis();
        Window window = requestLog.computeIfAbsent(clientID, k -> new Window(currentTimeMillis));

        synchronized (window) {
            if(currentTimeMillis - window.startTime > this.timeWindowInMilis) {
                window.startTime = currentTimeMillis;
                window.requestCount.set(1);
                return true;
            }
            if(window.requestCount.incrementAndGet() <= this.bucketToken){
                return true;
            } else {
                return false;
            }
        }
    }

    private static class Window{
        long startTime;
        AtomicInteger requestCount;

        Window(long startTime) {
            this.startTime = startTime;
            this.requestCount = new AtomicInteger(1);
        }
    }
}
