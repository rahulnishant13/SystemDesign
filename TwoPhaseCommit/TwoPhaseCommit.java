/**
implementing TwoPhaseCommit using ReentrantLock
tryLock helps in adding timeout values in case of deadlock
*/

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TwoPhaseCommit {
    static class Resource implements Runnable {
        TwoPhaseCommit twoPhaseCommit;
        Object resource1;
        Object resource2;

        Resource(TwoPhaseCommit twoPhaseCommits, Object resource11, Object resource12) {
            twoPhaseCommit = twoPhaseCommits;
            resource1 = resource11;
            resource2 = resource12;
        }

        @Override
        public void run() {
            try {
                twoPhaseCommit.aquireLock(resource1);
                System.out.println(Thread.currentThread().getName() + " aquired lock on " + resource1.toString());
                Thread.sleep(2000);
//                twoPhaseCommit.releaseLock(resource1);

                twoPhaseCommit.aquireLock(resource2);
                System.out.println(Thread.currentThread().getName() + " aquired lock on " + resource2.toString());
                Thread.sleep(2000);
//                twoPhaseCommit.releaseLock(resource2);
            } catch (Exception ex) {
                System.out.println("Excetion occured: "+ ex.getMessage());
            } finally {
                twoPhaseCommit.releaseLock(resource1);
                twoPhaseCommit.releaseLock(resource2);
            }
        }
    }

    static Map<Object, ReentrantLock> lockMap = new HashMap<>();

    public void aquireLock(Object resource) throws InterruptedException {
        Lock lock = lockMap.computeIfAbsent(resource, k -> new ReentrantLock());
//        lock.lock();
        lock.tryLock(1000, TimeUnit.MILLISECONDS);
    }

    public void releaseLock(Object resource) {
        try {
            lockMap.get(resource).unlock();
        } catch (Exception ex){
            System.out.println("releasing lock : " + ex.getMessage());
        }
    }


    public static void main(String[] args) throws InterruptedException {
        TwoPhaseCommit twoPhaseCommit = new TwoPhaseCommit();
        Object food = "food";
        Object delivery = "delivery";
        Runnable tranction1 = new Resource(twoPhaseCommit, food, delivery);
        Runnable tranction2 = new Resource(twoPhaseCommit, delivery, food);

        Thread t1 = new Thread(tranction1);
        Thread t2 = new Thread(tranction2);

        t1.start();
        t2.start();
//        t1.join();
//        t2.join();

        System.out.println("main stopped");
    }
}
