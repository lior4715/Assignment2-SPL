package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        try{
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            computationRoot.associativeNesting();
            ComputationNode resolvableNode = computationRoot.findResolvable();
            loadAndCompute(resolvableNode);
            double[][] resultData = leftMatrix.readRowMajor();
            if (resolvableNode == computationRoot) {
                resolvableNode.resolve(resultData);
                return computationRoot;
            } else {
                resolvableNode.resolve(resultData);
            }
        }
        return computationRoot;
    }
    finally {
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        if (node.getChildren() == null || node.getChildren().size() < 1) {
            throw new IllegalArgumentException("Node has no children: " + node.getNodeType());
        }

        double[][] leftData = node.getChildren().get(0).getMatrix();
        leftMatrix.loadRowMajor(leftData);

        if (node.getNodeType() == ComputationNodeType.ADD ||
                node.getNodeType() == ComputationNodeType.MULTIPLY) {

            if (node.getChildren().size() < 2) {
                throw new IllegalArgumentException("Binary operation requires two operands: " + node.getNodeType());
            }

            double[][] rightData = node.getChildren().get(1).getMatrix();
            rightMatrix.loadRowMajor(rightData);
        }

        if (node.getNodeType() == ComputationNodeType.NEGATE ||
                node.getNodeType() == ComputationNodeType.TRANSPOSE) {

            if (node.getChildren().size() != 1) {
                throw new IllegalArgumentException(
                        "Unary operation requires exactly one operand: " + node.getNodeType());
            }
        }

        List<Runnable> tasks = null;
        switch (node.getNodeType()) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + node.getNodeType());
        }
        executor.submitAll(tasks);
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        List<Runnable> tasks = new java.util.ArrayList<>();
        if (leftMatrix.length() != rightMatrix.length() || leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
            throw new IllegalArgumentException("Matrices must have the same number of rows to add.");
        }
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                leftMatrix.get(rowIndex).add(rightMatrix.get(rowIndex));
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new java.util.ArrayList<>();
        if (leftMatrix.get(0).length() != rightMatrix.length()) {
            throw new IllegalArgumentException(
                    "Number of columns in left matrix must equal number of rows in right matrix for multiplication.");
        }
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                leftMatrix.get(rowIndex).vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                leftMatrix.get(rowIndex).negate();
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> tasks = new java.util.ArrayList<>();
        tasks.add(() -> {
            double[][] data = leftMatrix.readRowMajor();
            int rows = data.length;
            int cols = data[0].length;

            double[][] transposed = new double[cols][rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    transposed[j][i] = data[i][j];
                }
            }

            leftMatrix.loadRowMajor(transposed);
        });

        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}
