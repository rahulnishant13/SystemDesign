package RateLimiter;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        FixedWindow rateLimiter =  FixedWindow.getInstance(3, 1000); // 3 requests per 1 second

        String clientId = "user123";

        for (int i = 0; i < 5; i++) {
            if (rateLimiter.grantAccess(clientId)) {
                System.out.println("Request " + (i + 1) + " allowed.");
            } else {
                System.out.println("Request " + (i + 1) + " blocked.");
            }
//            Thread.sleep(300); // Simulate requests coming in at different times
        }

        Thread.sleep(1000); // Wait for the window to reset

        // New window, should allow more requests
        for (int i = 5; i < 9; i++) {
            if (rateLimiter.grantAccess(clientId)) {
                System.out.println("Request " + (i + 1) + " allowed.");
            } else {
                System.out.println("Request " + (i + 1) + " blocked.");
            }
//            Thread.sleep(300);
        }

        System.out.println("\n");

        // =========================================================================================

        SlidingWindow slidingRateLimiter = SlidingWindow.getInstance(5, 1000); // 5 requests per 1 second
        String clientIdS = "user456";

        for (int i = 0; i < 10; i++) {
            if (slidingRateLimiter.grantAccess(clientIdS)) {
                System.out.println("Request " + (i + 1) + " allowed at " + System.currentTimeMillis());
            } else {
                System.out.println("Request " + (i + 1) + " blocked at " + System.currentTimeMillis());
            }
//            Thread.sleep(200); // Simulate requests every 200ms
        }
        Thread.sleep(1000);
        System.out.println("Starting new window");
        for (int i = 10; i < 18; i++) {
            if (slidingRateLimiter.grantAccess(clientIdS)) {
                System.out.println("Request " + (i + 1) + " allowed at " + System.currentTimeMillis());
            } else {
                System.out.println("Request " + (i + 1) + " blocked at " + System.currentTimeMillis());
            }
//            Thread.sleep(100);
        }

        System.out.println("\n");

        // =========================================================================================

        TokenBucket tokenRateLimiter = TokenBucket.getInstance(5, 200);
        String clientIdT = "user123";

        for (int i = 0; i < 15; i++) {
            if (tokenRateLimiter.grantAccess(clientIdT)) {
                System.out.println("Request " + (i + 1) + " allowed at " + System.currentTimeMillis());
            } else {
                System.out.println("Request " + (i + 1) + " blocked at " + System.currentTimeMillis());
            }
            Thread.sleep(100);
        }

        System.out.println("\nTesting with a different client, should use the same rate limiter instance:");
        String clientId2 = "user456";
        for (int i = 0; i < 10; i++) { //show that the rate limiter works for another client
            if (tokenRateLimiter.grantAccess(clientId2)) {
                System.out.println("Request " + (i + 1) + " allowed for " + clientId2 + " at " + System.currentTimeMillis());
            } else {
                System.out.println("Request " + (i + 1) + " blocked for " + clientId2 + " at " + System.currentTimeMillis());
            }
//            Thread.sleep(500);
        }

        System.out.println("\n leaky bucket");

        // =========================================================================================

        LeakyBucket leakyRateLimiter = LeakyBucket.getInstance(10, 500); // leakRateMillis = 1000/2 = 500
        String clientIdL = "user123";

        for (int i = 0; i < 30; i++) {
            if (leakyRateLimiter.grantAccess(clientIdL)) {
                System.out.println("Request " + (i + 1) + " allowed at " + System.currentTimeMillis());
            } else {
                System.out.println("Request " + (i + 1) + " blocked at " + System.currentTimeMillis());
            }
            Thread.sleep(200); // Simulate requests
        }
    }
}
