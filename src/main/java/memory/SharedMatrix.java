package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
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
        this.vectors = new SharedVector[matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            double[] column = new double[matrix.length];
            for (int j = 0; j < matrix.length; j++) {
                column[j] = matrix[j][i];
            }
            this.vectors[i] = new SharedVector(column, VectorOrientation.COLUMN_MAJOR);
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if(vectors.length == 0) {
            return null;
        }
        return null;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return null;
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
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
    }
}
