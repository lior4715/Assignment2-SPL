package spl.lae;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import parser.*;

public class SystemTest {

    private LinearAlgebraEngine engine;
    private InputParser parser;

    @BeforeEach
    public void setUp() {
        engine = new LinearAlgebraEngine(4);
        parser = new InputParser();
    }

    @Test
    public void testCompleteSystemWithExample() throws Exception {
        System.out.println("\n==============================================");
        System.out.println("System Test - Example.json");
        System.out.println("==============================================\n");
        System.out.println("Step 1: Creating LinearAlgebraEngine with 4 threads...");
        assertNotNull(engine);

        System.out.println("Step 2: Parsing example.json...");
        ComputationNode computationRoot = parser.parse("example.json");
        assertNotNull(computationRoot);
        System.out.println("Parsing successful");

        System.out.println("Step 3: Running computation...");
        System.out.println("This computes: ((A + T(B)) * C * -D), Where A, B, C, D are 10x10 matrices");
        long startTime = System.currentTimeMillis();
        ComputationNode result = engine.run(computationRoot);
        long endTime = System.currentTimeMillis();
        assertNotNull(result);
        System.out.println("Computation successful");
        System.out.println("Time taken: " + (endTime - startTime) + " ms");

        System.out.println("Step 4: Verifying result...");
        double[][] resultMatrix = result.getMatrix();
        assertNotNull(resultMatrix);
        assertEquals(10, resultMatrix.length);
        assertEquals(10, resultMatrix[0].length);
        System.out.println("Result dimensions correct: 10x10");
        double[][] expectedMatrix = {
                { -2.46812775E8, -2.4781405E8, -2.48815325E8, -2.498166E8, -2.50817875E8, -2.5181915E8, -2.52820425E8,
                        -2.538217E8, -2.54822975E8, -2.5582425E8 },
                { -2.80207725E8, -2.8134445E8, -2.82481175E8, -2.836179E8, -2.84754625E8, -2.8589135E8, -2.87028075E8,
                        -2.881648E8, -2.89301525E8, -2.9043825E8 },
                { -3.13602675E8, -3.1487485E8, -3.16147025E8, -3.174192E8, -3.18691375E8, -3.1996355E8, -3.21235725E8,
                        -3.225079E8, -3.23780075E8, -3.2505225E8 },
                { -3.46997625E8, -3.4840525E8, -3.49812875E8, -3.512205E8, -3.52628125E8, -3.5403575E8, -3.55443375E8,
                        -3.56851E8, -3.58258625E8, -3.5966625E8 },
                { -3.80392575E8, -3.8193565E8, -3.83478725E8, -3.850218E8, -3.86564875E8, -3.8810795E8, -3.89651025E8,
                        -3.911941E8, -3.92737175E8, -3.9428025E8 },
                { -4.13787525E8, -4.1546605E8, -4.17144575E8, -4.188231E8, -4.20501625E8, -4.2218015E8, -4.23858675E8,
                        -4.255372E8, -4.27215725E8, -4.2889425E8 },
                { -4.47182475E8, -4.4899645E8, -4.50810425E8, -4.526244E8, -4.54438375E8, -4.5625235E8, -4.58066325E8,
                        -4.598803E8, -4.61694275E8, -4.6350825E8 },
                { -4.80577425E8, -4.8252685E8, -4.84476275E8, -4.864257E8, -4.88375125E8, -4.9032455E8, -4.92273975E8,
                        -4.942234E8, -4.96172825E8, -4.9812225E8 },
                { -5.13972375E8, -5.1605725E8, -5.18142125E8, -5.20227E8, -5.22311875E8, -5.2439675E8, -5.26481625E8,
                        -5.285665E8, -5.30651375E8, -5.3273625E8 },
                { -5.47367325E8, -5.4958765E8, -5.51807975E8, -5.540283E8, -5.56248625E8, -5.5846895E8, -5.60689275E8,
                        -5.629096E8, -5.65129925E8, -5.6735025E8 } };
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(expectedMatrix[i][j], resultMatrix[i][j],
                        String.format("Result[%d][%d] mismatch: expected %.2e, got %.2e", i, j, expectedMatrix[i][j],
                                resultMatrix[i][j]));
            }
        }

        System.out.println("Step 5: Writing output to test_output.json...");
        OutputWriter.write(resultMatrix, "test_output.json");
        System.out.println("Output written successfully");

        System.out.println("Step 6: Checking worker statistics...");
        String workerReport = engine.getWorkerReport();
        assertNotNull(workerReport);
        System.out.println("Worker Report:");
        System.out.println("-----------------------------------");
        System.out.println(workerReport);
        System.out.println("-----------------------------------");
        String[] lines = workerReport.split("\n");
        for (String line : lines) {
            if (line.contains("Worker")) {
                assertFalse(line.contains("Time Used = 0 ns"), "Worker should have done work: " + line);
            }
        }
        System.out.println("All workers participated in computation");

        double[] fatigues = new double[4];
        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            int fatigueIdx = line.indexOf("Fatigue = ") + 10;
            int commaIdx = line.indexOf(",", fatigueIdx);
            String fatigueStr = line.substring(fatigueIdx, commaIdx);
            fatigues[i] = Double.parseDouble(fatigueStr);
        }
        double avgFatigue = 0;
        for (double f : fatigues)
            avgFatigue += f;
        avgFatigue /= 4;
        double maxDeviation = 0;
        for (double f : fatigues) {
            double deviation = Math.abs(f - avgFatigue) / avgFatigue;
            if (deviation > maxDeviation)
                maxDeviation = deviation;
        }

        System.out.println("Fatigue Statistics:");
        System.out.println("Average Fatigue: " + String.format("%.2e", avgFatigue));
        System.out.println("Max Deviation: " + String.format("%.1f%%", maxDeviation * 100));

        assertTrue(maxDeviation < 1.0, "Fatigue deviation should be reasonable");
        System.out.println("Fatigue well-balanced across workers");

        System.out.println("\n==============================================");
        System.out.println("SYSTEM TEST PASSED");
        System.out.println("==============================================");
    }

    @Test
    public void testSimpleAddition() {
        System.out.println("Testing simple addition...");
        double[][] a = { { 1, 2 }, { 3, 4 } };
        double[][] b = { { 5, 6 }, { 7, 8 } };
        ComputationNode aNode = new ComputationNode(a);
        ComputationNode bNode = new ComputationNode(b);
        ComputationNode addNode = new ComputationNode("+", java.util.List.of(aNode, bNode));

        LinearAlgebraEngine simpleEngine = new LinearAlgebraEngine(2);
        ComputationNode addResult = simpleEngine.run(addNode);
        double[][] addMatrix = addResult.getMatrix();

        assertEquals(6, addMatrix[0][0]);
        assertEquals(8, addMatrix[0][1]);
        assertEquals(10, addMatrix[1][0]);
        assertEquals(12, addMatrix[1][1]);
        System.out.println("Addition operation correct");
    }

    @Test
    public void testSimpleMultiplication() {
        System.out.println("Testing simple multiplication...");
        double[][] c = { { 1, 2 }, { 3, 4 } };
        double[][] d = { { 2, 0 }, { 1, 2 } };
        ComputationNode cNode = new ComputationNode(c);
        ComputationNode dNode = new ComputationNode(d);
        ComputationNode multNode = new ComputationNode("*", java.util.List.of(cNode, dNode));

        LinearAlgebraEngine simpleEngine = new LinearAlgebraEngine(2);
        ComputationNode multResult = simpleEngine.run(multNode);
        double[][] multMatrix = multResult.getMatrix();

        assertEquals(4, multMatrix[0][0]);
        assertEquals(4, multMatrix[0][1]);
        assertEquals(10, multMatrix[1][0]);
        assertEquals(8, multMatrix[1][1]);
        System.out.println("Multiplication operation correct");
    }

    @Test
    public void testNegateOperation() {
        System.out.println("Testing negate operation...");
        double[][] e = { { 1, 2 }, { 3, 4 } };
        ComputationNode eNode = new ComputationNode(e);
        ComputationNode negNode = new ComputationNode("-", java.util.List.of(eNode));

        LinearAlgebraEngine simpleEngine = new LinearAlgebraEngine(2);
        ComputationNode negResult = simpleEngine.run(negNode);
        double[][] negMatrix = negResult.getMatrix();

        assertEquals(-1, negMatrix[0][0]);
        assertEquals(-2, negMatrix[0][1]);
        assertEquals(-3, negMatrix[1][0]);
        assertEquals(-4, negMatrix[1][1]);
        System.out.println("Negate operation correct");
    }

    @Test
    public void testTransposeOperation() {
        System.out.println("Testing transpose operation...");
        double[][] f = { { 1, 2, 3 }, { 4, 5, 6 } };
        ComputationNode fNode = new ComputationNode(f);
        ComputationNode transNode = new ComputationNode("T", java.util.List.of(fNode));

        LinearAlgebraEngine simpleEngine = new LinearAlgebraEngine(2);
        ComputationNode transResult = simpleEngine.run(transNode);
        double[][] transMatrix = transResult.getMatrix();

        assertEquals(3, transMatrix.length);
        assertEquals(2, transMatrix[0].length);
        assertEquals(1, transMatrix[0][0]);
        assertEquals(4, transMatrix[0][1]);
        assertEquals(2, transMatrix[1][0]);
        assertEquals(5, transMatrix[1][1]);
        assertEquals(3, transMatrix[2][0]);
        assertEquals(6, transMatrix[2][1]);
        System.out.println("Transpose operation correct");
    }
}
