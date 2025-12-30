package memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    private SharedVector rowVector1;
    private SharedVector rowVector2;
    private SharedVector colVector1;
    private SharedVector colVector2;

    @BeforeEach
    public void setUp() {
        rowVector1 = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.ROW_MAJOR);
        rowVector2 = new SharedVector(new double[] { 4, 5, 6 }, VectorOrientation.ROW_MAJOR);
        colVector1 = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.COLUMN_MAJOR);
        colVector2 = new SharedVector(new double[] { 4, 5, 6 }, VectorOrientation.COLUMN_MAJOR);
    }

    @Test
    public void testVectorLength() {
        assertEquals(3, rowVector1.length(), "Vector length should be 3");
    }

    @Test
    public void testVectorGet() {
        assertEquals(1, rowVector1.get(0));
        assertEquals(2, rowVector1.get(1));
        assertEquals(3, rowVector1.get(2));
    }

    @Test
    public void testVectorOrientation() {
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector1.getOrientation());
        assertEquals(VectorOrientation.COLUMN_MAJOR, colVector1.getOrientation());
    }

    @Test
    public void testAddSameOrientation() {
        rowVector1.add(rowVector2);
        assertEquals(5, rowVector1.get(0));
        assertEquals(7, rowVector1.get(1));
        assertEquals(9, rowVector1.get(2));
    }

    @Test
    public void testAddDifferentOrientationsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            rowVector1.add(colVector1);
        }, "Adding vectors with different orientations should throw exception");
    }

    @Test
    public void testAddDifferentLengthsThrowsException() {
        SharedVector shortVector = new SharedVector(new double[] { 1, 2 }, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> {
            rowVector1.add(shortVector);
        }, "Adding vectors with different lengths should throw exception");
    }

    @Test
    public void testNegate() {
        rowVector1.negate();
        assertEquals(-1, rowVector1.get(0));
        assertEquals(-2, rowVector1.get(1));
        assertEquals(-3, rowVector1.get(2));
    }

    @Test
    public void testNegateZeroVector() {
        SharedVector zeroVector = new SharedVector(new double[] { 0, 0, 0 }, VectorOrientation.ROW_MAJOR);
        zeroVector.negate();
        assertEquals(0, zeroVector.get(0), 0.000001);
        assertEquals(0, zeroVector.get(1), 0.000001);
        assertEquals(0, zeroVector.get(2), 0.000001);
    }

    @Test
    public void testTranspose() {
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector1.getOrientation());
        rowVector1.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, rowVector1.getOrientation());
        rowVector1.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, rowVector1.getOrientation());
    }

    @Test
    public void testDotProductRowColumn() {
        double result = rowVector1.dot(colVector2);
        assertEquals(32, result);
    }

    @Test
    public void testDotProductSameOrientationThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            rowVector1.dot(rowVector2);
        }, "Dot product of vectors with same orientation should throw exception");
    }

    @Test
    public void testDotProductDifferentLengthsThrowsException() {
        SharedVector shortColVector = new SharedVector(new double[] { 1, 2 }, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> {
            rowVector1.dot(shortColVector);
        }, "Dot product of vectors with different lengths should throw exception");
    }

    @Test
    public void testVecMatMul() {
        double[][] matrixData = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };
        SharedMatrix matrix = new SharedMatrix(matrixData);
        rowVector1.vecMatMul(matrix);

        assertEquals(2, rowVector1.length());
        assertEquals(22, rowVector1.get(0));
        assertEquals(28, rowVector1.get(1));
    }

    @Test
    public void testVecMatMulDimensionMismatch() {
        double[][] matrixData = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        SharedMatrix matrix = new SharedMatrix(matrixData);
        assertThrows(IllegalArgumentException.class, () -> {
            rowVector1.vecMatMul(matrix);
        }, "vecMatMul with dimension mismatch should throw exception");
    }
}