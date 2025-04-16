package RateLimiter;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindow implements RateLimiter {
    private static SlidingWindow slidingWindow;
    private long timeWindowInMilis;
    private int bucketToken;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestLog;

    private SlidingWindow(int token, long timeWindowInMilis){
        this.timeWindowInMilis = timeWindowInMilis;
        this.bucketToken = token;
        this.requestLog = new ConcurrentHashMap<>();
    }

    public static synchronized SlidingWindow getInstance(int token, long timeWindowInMilis) {
        if(slidingWindow == null) {
            slidingWindow = new SlidingWindow(token, timeWindowInMilis);
        } else if(slidingWindow.bucketToken != token || slidingWindow.timeWindowInMilis != timeWindowInMilis) {
//            check to throw error or reset it
        }

        return slidingWindow;
    }

    @Override
    public boolean grantAccess(String clientId) {
        long currTime = System.currentTimeMillis();
        ConcurrentLinkedQueue<Long> queue = requestLog.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());

        while (!queue.isEmpty()) {
            Long head = queue.peek();
            if(head != null && currTime - head > this.timeWindowInMilis) {
                queue.poll();
            } else {
                break;
            }
        }

        synchronized (queue) {
            if(queue.size() < this.bucketToken) {
                queue.offer(currTime);
                return true;
            } else {
                return false;
            }
        }
    }
}
