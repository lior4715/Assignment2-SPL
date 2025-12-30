package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        this.vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        this.vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        this.vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            this.vectors[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (vectors.length == 0) {
            return null;
        }

        if (this.getOrientation() == VectorOrientation.ROW_MAJOR) {
            double[][] result = new double[vectors.length][vectors[0].length()];
            acquireAllVectorReadLocks(vectors);
            try {
                for (int i = 0; i < vectors.length; i++) {
                    double[] row = new double[vectors[i].length()];
                    for (int j = 0; j < vectors[i].length(); j++) {
                        row[j] = vectors[i].get(j);
                    }
                    result[i] = row;
                }
            } finally {
                releaseAllVectorReadLocks(vectors);
            }
            return result;
        } else {
            // Convert column-major to row-major
            double[][] result = new double[vectors[0].length()][vectors.length];
            acquireAllVectorReadLocks(vectors);
            try {
                for (int j = 0; j < vectors.length; j++) {
                    double[] column = new double[vectors[j].length()];
                    for (int k = 0; k < vectors[j].length(); k++) {
                        column[k] = vectors[j].get(k);
                    }
                    for (int i = 0; i < vectors[0].length(); i++) {
                        result[i][j] = column[i];
                    }
                }
            } finally {
                releaseAllVectorReadLocks(vectors);
            }
            return result;
        }
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors.length == 0) {
            return null;
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (SharedVector vec : vecs) {
            vec.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (SharedVector vec : vecs) {
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (SharedVector vec : vecs) {
            vec.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (SharedVector vec : vecs) {
            vec.writeUnlock();
        }
    }
}
