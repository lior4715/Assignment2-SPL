package memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    private SharedMatrix matrix;
    private double[][] testData;

    @BeforeEach
    public void setUp() {
        testData = new double[][] {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };
        matrix = new SharedMatrix();
    }

    @Test
    public void testEmptyMatrix() {
        assertEquals(0, matrix.length(), "Empty matrix should have length 0");
    }

    @Test
    public void testLoadRowMajor() {
        matrix.loadRowMajor(testData);
        assertEquals(2, matrix.length(), "Matrix should have 2 rows");
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
    }

    @Test
    public void testLoadColumnMajor() {
        matrix.loadColumnMajor(testData);
        assertEquals(2, matrix.length(), "Matrix should have 2 columns stored");
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }

    @Test
    public void testReadRowMajorFromRowMajor() {
        matrix.loadRowMajor(testData);
        double[][] result = matrix.readRowMajor();
        assertNotNull(result);
        assertEquals(2, result.length, "Should have 2 rows");
        assertEquals(3, result[0].length, "Should have 3 columns");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(testData[i][j], result[i][j],
                        "Element [" + i + "][" + j + "] should match");
            }
        }
    }

    @Test
    public void testReadRowMajorFromColumnMajor() {
        matrix.loadColumnMajor(testData);
        double[][] result = matrix.readRowMajor();
        assertNotNull(result);
        assertEquals(3, result.length, "Should have 3 rows");
        assertEquals(2, result[0].length, "Should have 2 columns");
        assertEquals(1, result[0][0]);
        assertEquals(4, result[0][1]);
        assertEquals(2, result[1][0]);
        assertEquals(5, result[1][1]);
        assertEquals(3, result[2][0]);
        assertEquals(6, result[2][1]);
    }

    @Test
    public void testGetVector() {
        matrix.loadRowMajor(testData);
        SharedVector row0 = matrix.get(0);
        assertNotNull(row0);
        assertEquals(3, row0.length());
        assertEquals(1, row0.get(0));
        assertEquals(2, row0.get(1));
        assertEquals(3, row0.get(2));
    }

    @Test
    public void testConstructorWithData() {
        SharedMatrix matrixWithData = new SharedMatrix(testData);
        assertEquals(2, matrixWithData.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrixWithData.getOrientation());
        double[][] result = matrixWithData.readRowMajor();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(testData[i][j], result[i][j],
                        "Element [" + i + "][" + j + "] should match");
            }
        }
    }

    @Test
    public void testSingleElementMatrix() {
        double[][] singleElement = { { 42 } };
        matrix.loadRowMajor(singleElement);
        assertEquals(1, matrix.length());
        double[][] result = matrix.readRowMajor();
        assertEquals(42, result[0][0]);
    }

    @Test
    public void testRectangularMatrix() {
        double[][] rectData = {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 }
        };
        matrix.loadRowMajor(rectData);
        assertEquals(2, matrix.length(), "Should have 2 rows");
        SharedVector row0 = matrix.get(0);
        assertEquals(4, row0.length(), "Row should have 4 elements");
    }

    @Test
    public void testTransposeCorrectness() {
        double[][] original = { { 1, 2, 3 }, { 4, 5, 6 } };
        matrix.loadRowMajor(original);
        double[][] data = matrix.readRowMajor();
        int rows = data.length;
        int cols = data[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = data[i][j];
            }
        }
        matrix.loadRowMajor(transposed);
        double[][] result = matrix.readRowMajor();
        assertEquals(3, result.length, "Transposed: 3 rows");
        assertEquals(2, result[0].length, "Transposed: 2 columns");
        assertEquals(1, result[0][0]);
        assertEquals(4, result[0][1]);
        assertEquals(2, result[1][0]);
        assertEquals(5, result[1][1]);
        assertEquals(3, result[2][0]);
        assertEquals(6, result[2][1]);
    }
}