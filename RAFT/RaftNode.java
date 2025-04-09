import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RaftNode {

    private final int electionTimeoutBase = 150; // Base for randomized timeout in milliseconds
    private final int electionTimeoutRandomBound = 150; // Randomness bound
    private final int nodeId;
    private final List<Integer> otherNodes;
    private State state;
    private int currentTerm;
    private Integer votedFor;
    private final AtomicInteger votesReceived;
    private Timer electionTimer;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public RaftNode(int nodeId, List<Integer> otherNodes) {
        this.nodeId = nodeId;
        this.otherNodes = new ArrayList<>(otherNodes);
        this.state = State.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.votesReceived = new AtomicInteger(0);
        startElectionTimer();
    }

    public static void main(String[] args) throws InterruptedException {
        List<Integer> nodes = List.of(1, 2, 3, 4, 5);
        List<Integer> othersFor1 = List.of(2, 3, 4, 5);
        List<Integer> othersFor2 = List.of(1, 3, 4, 5);
        List<Integer> othersFor3 = List.of(1, 2, 4, 5);
        List<Integer> othersFor4 = List.of(1, 2, 3, 5);
        List<Integer> othersFor5 = List.of(1, 2, 3, 4);

        RaftNode node1 = new RaftNode(1, othersFor1);
        RaftNode node2 = new RaftNode(2, othersFor2);
        RaftNode node3 = new RaftNode(3, othersFor3);
        RaftNode node4 = new RaftNode(4, othersFor4);
        RaftNode node5 = new RaftNode(5, othersFor5);

        List<RaftNode> allNodes = List.of(node1, node2, node3, node4, node5);

        // Simulate some network interactions (very basic)
        Thread.sleep(2000); // Let nodes start and potentially elect a leader

        for (RaftNode node : allNodes) {
            System.out.println("Node " + node.getNodeId() + " is in state: " + node.getState() + " in term: " + node.getCurrentTerm() + ", votedFor: " + node.votedFor);
        }

        // Simulate a leader failure (stop the leader)
        for (RaftNode node : allNodes) {
            if (node.getState() == State.LEADER) {
                System.out.println("Simulating failure of leader: Node " + node.getNodeId());
                node.stop();
                break;
            }
        }

        Thread.sleep(5000); // Allow for a new election

        for (RaftNode node : allNodes) {
            System.out.println("Node " + node.getNodeId() + " is in state: " + node.getState() + " in term: " + node.getCurrentTerm() + ", votedFor: " + node.votedFor);
        }

        // Stop all nodes
        for (RaftNode node : allNodes) {
            node.stop();
        }
    }

    public int getNodeId() {
        return nodeId;
    }

    public State getState() {
        return state;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    // Simulate receiving a RequestVote RPC
    public synchronized VoteResponse handleRequestVote(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        if (!running.get()) return new VoteResponse(currentTerm, false);

        if (term < currentTerm) {
            return new VoteResponse(currentTerm, false); // Reject vote if candidate's term is older
        }

        if (term > currentTerm) {
            currentTerm = term;
            state = State.FOLLOWER;
            votedFor = null;
            resetElectionTimer(); // Reset timer as we've seen a higher term
        }

        boolean voteGranted = false;
        // In a real implementation, you'd check the candidate's log for up-to-dateness
        boolean logOk = true; // Simplified for this example

        if (votedFor == null && logOk) {
            votedFor = candidateId;
            voteGranted = true;
            resetElectionTimer(); // Vote granted, reset timer
        }

        System.out.println("Node " + nodeId + " received RequestVote from " + candidateId + " in term " + term + ". Granted: " + voteGranted);
        return new VoteResponse(currentTerm, voteGranted);
    }

    // Simulate sending a RequestVote RPC
    private void sendRequestVote(int candidateId, int term) {
        System.out.println("Node " + nodeId + " (Candidate) sending RequestVote to Node " + candidateId + " for term " + term);
        // In a real implementation, you would send an RPC to the other node
        // and process the VoteResponse asynchronously.
        // For this example, we'll simulate a response.
        int randomDelay = new Random().nextInt(50) + 50;
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (running.get() && state == State.CANDIDATE && currentTerm == term) {
                            // Simulate a response (replace with actual RPC handling)
                            boolean voteGranted = new Random().nextBoolean(); // Simulate the other node's decision
                            handleVoteResponse(term, candidateId, voteGranted);
                        }
                    }
                },
                randomDelay
        );
    }

    // Handle the response to a RequestVote RPC
    private synchronized void handleVoteResponse(int term, int voterId, boolean voteGranted) {
        if (!running.get() || state != State.CANDIDATE || term != currentTerm) {
            return;
        }

        if (voteGranted) {
            votesReceived.incrementAndGet();
            System.out.println("Node " + nodeId + " (Candidate) received a vote from " + voterId + " in term " + term + ". Total votes: " + votesReceived.get());
            if (votesReceived.get() > (otherNodes.size() + 1) / 2) {
                becomeLeader();
            }
        } else {
            System.out.println("Node " + nodeId + " (Candidate) did not receive a vote from " + voterId + " in term " + term);
        }
    }

    private synchronized void startElection() {
        if (!running.get()) return;

        state = State.CANDIDATE;
        currentTerm++;
        votedFor = nodeId; // Vote for self
        votesReceived.set(1);
        System.out.println("Node " + nodeId + " started an election for term " + currentTerm);

        resetElectionTimer(); // Reset the timer as we are now a candidate

        for (int otherNodeId : otherNodes) {
            sendRequestVote(otherNodeId, currentTerm);
        }
    }

    private void startElectionTimer() {
        electionTimer = new Timer();
        scheduleNewElectionTimeout();
    }

    private void resetElectionTimer() {
        if (electionTimer != null) {
            electionTimer.cancel();
        }
        electionTimer = new Timer();
        scheduleNewElectionTimeout();
    }

    private void scheduleNewElectionTimeout() {
        Random random = new Random();
        int timeout = electionTimeoutBase + random.nextInt(electionTimeoutRandomBound);
        electionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (running.get() && state != State.LEADER) {
                    System.out.println("Node " + nodeId + " election timeout reached in term " + currentTerm);
                    startElection();
                }
            }
        }, timeout);
    }

    private synchronized void becomeLeader() {
        if (!running.get()) return;

        state = State.LEADER;
        System.out.println("Node " + nodeId + " became the leader for term " + currentTerm);
        electionTimer.cancel(); // Leader doesn't need the election timer

        // In a real implementation, the leader would start sending heartbeats
        startHeartbeats();
    }

    // Simulate sending heartbeats (AppendEntries with no entries)
    private void startHeartbeats() {
        Timer heartbeatTimer = new Timer();
        int heartbeatInterval = 50; // Milliseconds
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running.get() && state == State.LEADER) {
                    for (int followerId : otherNodes) {
                        System.out.println("Node " + nodeId + " (Leader) sending heartbeat to Node " + followerId + " in term " + currentTerm);
                        // In a real implementation, send AppendEntries RPC
                    }
                } else {
                    heartbeatTimer.cancel();
                }
            }
        }, 0, heartbeatInterval);
    }

    // Simulate receiving AppendEntries (heartbeat)
    public synchronized void handleAppendEntries(int term, int leaderId) {
        if (!running.get()) return;

        if (term < currentTerm) {
            return; // Ignore older term
        }

        if (term > currentTerm) {
            currentTerm = term;
            state = State.FOLLOWER;
            votedFor = null;
        } else if (state == State.CANDIDATE) {
            state = State.FOLLOWER; // Step down if we see a valid leader
        }

        resetElectionTimer(); // Reset timer upon receiving a valid heartbeat
        System.out.println("Node " + nodeId + " (Follower) received heartbeat from Leader " + leaderId + " in term " + term);
    }

    public void stop() {
        running.set(false);
        if (electionTimer != null) {
            electionTimer.cancel();
        }
    }

    enum State {FOLLOWER, CANDIDATE, LEADER}

    public static class VoteResponse {
        public int term;
        public boolean voteGranted;

        public VoteResponse(int term, boolean voteGranted) {
            this.term = term;
            this.voteGranted = voteGranted;
        }
    }
}
