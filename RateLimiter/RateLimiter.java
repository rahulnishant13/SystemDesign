package RateLimiter;

public interface RateLimiter {
    boolean grantAccess(String clientId);
}
