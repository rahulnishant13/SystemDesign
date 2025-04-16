package RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucket implements RateLimiter {
    private static TokenBucket tokenBucket;
    private final int maxRequests;
    private final long refillRateMillis;
    private final ConcurrentHashMap<String, Bucket> buckets;

    private TokenBucket(int maxRequests, long refillRateMillis) {
        this.maxRequests = maxRequests;
        this.refillRateMillis = refillRateMillis;
        this.buckets = new ConcurrentHashMap<>();
    }

    public static synchronized TokenBucket getInstance(int maxRequests, long refillRateMillis) {
        if(tokenBucket == null) {
            tokenBucket = new TokenBucket(maxRequests, refillRateMillis);
        } else if (tokenBucket.maxRequests != maxRequests || tokenBucket.refillRateMillis != refillRateMillis) {
//            check and throw errror or set new param
        }

        return tokenBucket;
    }


    @Override
    public boolean grantAccess(String clientId) {
        long currTime = System.currentTimeMillis();
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> new Bucket(this.maxRequests, currTime));

        synchronized (bucket) {
            long timePassed = currTime - bucket.lastRefillTime;
            int tokensToBeAdded = (int) (timePassed/this.refillRateMillis);
            if(tokensToBeAdded > 0) {
                bucket.tokens = Math.min(this.maxRequests, bucket.tokens+tokensToBeAdded);
                bucket.lastRefillTime = currTime;
            }

            if(bucket.tokens >= 1) {
                bucket.tokens--;
                return true;
            } else {
                return false;
            }
        }
    }

    private static class Bucket {
        long lastRefillTime;
        int tokens;

        Bucket(int tokens, long lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}
