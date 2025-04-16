package RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucket implements RateLimiter {
    private static LeakyBucket leakyBucket;
    private final int capacity;
    private final long leakRateMillis;
    private final ConcurrentHashMap<String, Bucket> logRequest;

    private LeakyBucket(int capacity, long leakRateMillis) {
        this.capacity = capacity;
        this.leakRateMillis = leakRateMillis;
        this.logRequest = new ConcurrentHashMap<>();
    }

    public static synchronized LeakyBucket getInstance(int capacity, long leakRateMillis) {
        if(leakyBucket == null) {
            leakyBucket = new LeakyBucket(capacity, leakRateMillis);
        } else if (leakyBucket.capacity != capacity || leakyBucket.leakRateMillis != leakRateMillis) {
//            check and throw error
        }

        return leakyBucket;
    }

    @Override
    public boolean grantAccess(String clientId) {
        long currTime = System.currentTimeMillis();
        Bucket bucket = logRequest.computeIfAbsent(clientId, k -> new Bucket(0, currTime));

        synchronized (bucket) {
            long timePassed = currTime - bucket.lastUpdatedTime;
            int leakedRequest = (int) (timePassed/this.leakRateMillis);
            bucket.currSize = Math.max(0, bucket.currSize - leakedRequest);
            if (leakedRequest > 0 || bucket.currSize < capacity) {
                bucket.lastUpdatedTime = currTime;
            }


            if(bucket.currSize < capacity) {
                bucket.currSize++;
                return true;
            } else {
                return false;
            }
        }
    }

    private static class Bucket{
        long lastUpdatedTime;
        int currSize;

        Bucket(int currSize, long lastUpdatedTime) {
            this.currSize = currSize;
            this.lastUpdatedTime = lastUpdatedTime;
        }
    }
}
