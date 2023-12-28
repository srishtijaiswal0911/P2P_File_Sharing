import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Bitfield {
    // Locks for concurrent read and write operations
    private final ReadWriteLock readWrite = new ReentrantReadWriteLock();
    private final Lock read = readWrite.readLock();
    private final Lock write = readWrite.writeLock();

    // BitSet to represent pieces
    private final BitSet bitfield;

    // Configuration variables
    private final CommonCfg commonConfigurationOfVariables;

    // Total number of pieces
    private final int numPieces;

    // Set to store indices of requested pieces
    private final Set<Integer> reqPieces = ConcurrentHashMap.newKeySet();

    // DelayQueue to manage timeouts for requested pieces
    private final DelayQueue<PieceIndex> reqPiecesQueue = new DelayQueue<>();

    // Constructor to initialize the Bitfield with given BitSet and configuration
    public Bitfield(BitSet bitfield, CommonCfg commonConfigurationOfVariables) {
        this.bitfield = bitfield;
        this.commonConfigurationOfVariables = commonConfigurationOfVariables;
        this.numPieces = this.commonConfigurationOfVariables.getNumberOfPieces();
    }

    // Getter for the bitfield
    public BitSet getFieldForBit() {
        return this.bitfield;
    }

    // Check if the current bitfield is interested in the provided peer's bitfield
    public boolean isInterested(BitSet peerBitField) {
        try {
            this.read.lock();
            return getNxtInterestedPieceIdx(peerBitField) != -1;
        } finally {
            this.read.unlock();
        }
    }

    // Get the index of the next piece in the peer's bitfield that the current bitfield is interested in
    public int getNxtInterestedPieceIdx(BitSet peerBitField) {
        for (int i = peerBitField.nextSetBit(0); i != -1; i = peerBitField.nextSetBit(i + 1)) {
            if (reqPieces.contains(i)) {
                continue;
            }if (!bitfield.get(i)) {
                return i;
            }}
        return -1;
    }

    // Add a piece index to the set of requested pieces and to the DelayQueue
    public void addToReqPieces(int pieceIdx) {
        this.reqPieces.add(pieceIdx);
        this.reqPiecesQueue.add(new PieceIndex(pieceIdx));
    }

    // Mark a piece as received in the bitfield and remove it from the set of requested pieces
    public void setReceivedPieceIdx(int pieceIdx) {
        try {
            this.write.lock();
            bitfield.set(pieceIdx);
            reqPieces.remove(pieceIdx);
        } finally {
            this.write.unlock();
        }
    }

    // Check if all pieces have been received
    public boolean receivedAllPieces() {
        int nextClearIndex = bitfield.nextClearBit(0);
        return nextClearIndex == -1 || nextClearIndex >= numPieces;
    }

    // Returns the Delay Queue for Required Pieces
    public DelayQueue<PieceIndex> getDelayQueue() {
        return this.reqPiecesQueue;
    }

    // Removes Timed out Piece from Queue
    public void removeTimedOutPieceIndex(int pieceIdx) {
        this.reqPieces.remove(pieceIdx);
    }

    // Acquires Read Lock
    public void readLock() {
        this.read.lock();
    }

    // Releases Read Lock
    public void readUnlock() {
        this.read.unlock();
    }
}