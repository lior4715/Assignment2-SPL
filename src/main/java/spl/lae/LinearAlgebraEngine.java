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
        while(computationRoot.getNodeType() != ComputationNodeType.MATRIX){
            computationRoot.associativeNesting();
            ComputationNode resolvableNode = computationRoot.findResolvable();
            loadAndCompute(resolvableNode);
            double[][] resultData = resolvableNode.getChildren().get(0).getMatrix();
            if(resolvableNode == computationRoot){
                return computationRoot;
            } else {
                resolvableNode.resolve(resultData);
            }
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        if (node.getChildren().size() < 1) {
            throw new IllegalArgumentException("Left child is null for node: " + node.getNodeType());
        }
        
        double[][] leftData = node.getChildren().get(0).getMatrix();
        leftMatrix.loadRowMajor(leftData);

        if (node.getChildren().size() < 2 && 
            (node.getNodeType() == ComputationNodeType.ADD || 
             node.getNodeType() == ComputationNodeType.MULTIPLY)) {
            throw new IllegalArgumentException("Right child is null for binary operation node: " + node.getNodeType());  
        }

        if(node.getChildren().size() > 1 && (node.getNodeType() == ComputationNodeType.NEGATE ||
           node.getNodeType() == ComputationNodeType.TRANSPOSE)) {
            throw new IllegalArgumentException("Unary operation node has two children: " + node.getNodeType());
        }

        double[][] rightData = node.getChildren().get(1).getMatrix();
        rightMatrix.loadRowMajor(rightData);

        if(node.getChildren().size() > 2){
            throw new IllegalArgumentException("Node has more than two children: " + node.getNodeType());
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
        if(leftMatrix.length() != rightMatrix.length() || leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
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
        if(leftMatrix.get(0).length() != rightMatrix.length()) {
            throw new IllegalArgumentException("Number of columns in left matrix must equal number of rows in right matrix for multiplication.");
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
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                leftMatrix.get(rowIndex).transpose();
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}
