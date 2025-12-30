package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    }

    /**
     * Uses ReadLock to allow multiple threads to read simultaneously while preventing writes. 
     * This is safe because we're only reading a single element without modifying the vector.
     */

    public double get(int index) {
        // TODO: return element at index (read-locked)
        this.readLock();
        try {
            double value = vector[index];
            return value;
        } finally {
            this.readUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        return this.orientation;
    }

    public void setOrientation(VectorOrientation orientation) {
        this.orientation = orientation;
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    /*     
     * Uses WriteLock for exclusive access because we're modifying the orientation metadata.
     * Even though we're not changing the data of the array, we need exclusive access to ensure
     * the orientation change is "atomic" and visible to all threads.
     */
    public void transpose() {
        // TODO: transpose vector
        this.writeLock();
        try {
            if (this.getOrientation() == VectorOrientation.ROW_MAJOR) {
                this.setOrientation(VectorOrientation.COLUMN_MAJOR);
            } else {
                this.setOrientation(VectorOrientation.ROW_MAJOR);
            }
        } finally {
            this.writeUnlock();
        }
    }

    /*
     * Prevent race conditions. We acquire ReadLock on 'other' vector (we only read from it),
     * then we acquire WriteLock on 'this' vector (we modify it).
     * 
     * 'other' is locked first, then 'this'. This consistent ordering prevents deadlock when multiple threads
     * perform add operations. Locks are released in finally blocks to ensure cleanup.
     */
    public void add(SharedVector other) {
        // TODO: add two vectors
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Vectors must be of the same length to add.");
        }
        if (this.orientation != other.orientation) {
            throw new IllegalArgumentException("Vectors must have the same orientation to add.");
        }
        other.readLock();
        this.writeLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.vector[i];
            }
        } finally {
            other.readUnlock();
            this.writeUnlock();
        }
    }

    /*
     * Uses WriteLock for exclusive access because we're modifying every element in the vector.
     * No other thread should read or write while negation is in progress to ensure "atomicity" of the operation.
     */
    public void negate() {
        // TODO: negate vector
        this.writeLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = -this.vector[i];
            }
        } finally {
            this.writeUnlock();
        }
    }


    /**
     * Allow concurrent dot product calculations while preventing modifications.
     * We acquire ReadLock on 'this' vector and then we acquire ReadLock on 'other' vector
     * 
     * Both locks are read locks because we're only reading from both vectors, not modifying
     * them. This allows multiple threads to compute different dot products simultaneously,
     * improving parallelism. Lock ordering ('this' then 'other') prevents deadlock.
     */
    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Vectors must be of the same length to compute dot product.");
        }
        if(this.orientation == other.orientation) {
            throw new IllegalArgumentException("Vectors must have different orientations to compute dot product.");
        }
        double result = 0;
        this.readLock();
        other.readLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                result += this.vector[i] * other.vector[i];
            }
        } finally {
            this.readUnlock();
            other.readUnlock();
        }
        return result;
    }


    /*
     * Uses WriteLock on 'this' vector because we're replacing its contents with the multiplication result.
     * 
     * We acquire the write lock after reading the matrix to minimize lock holding time
     * and avoid potential deadlock with matrix operations.
     */
    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        if (this.getOrientation() != VectorOrientation.ROW_MAJOR) {
            throw new IllegalArgumentException("Vector must be row-major for vector-matrix multiplication.");
        }
        double[][] rowMatrix = matrix.readRowMajor();
        if (this.length() != rowMatrix.length) {
            throw new IllegalArgumentException("Vector length must match number of rows in matrix for multiplication.");
        }
        double[] result = new double[rowMatrix[0].length];
        this.writeLock();
        try {
            for (int j = 0; j < rowMatrix[0].length; j++) {
                for (int i = 0; i < rowMatrix.length; i++) {
                    result[j] += this.vector[i] * rowMatrix[i][j];
                }
            }
        } finally {
            this.writeUnlock();
        }
        this.vector = result;
    }
}
