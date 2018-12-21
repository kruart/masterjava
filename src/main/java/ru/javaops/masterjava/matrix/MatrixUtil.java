package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        class MatrixColumn {
            private final int column;
            private final int[] columnByRow;

             private MatrixColumn(int column, int[] columnByRow) {
                this.column = column;
                this.columnByRow = columnByRow;
            }
        }

        List<Future<MatrixColumn>> futures = new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            final int col = i;
            final int[] thatColumn = new int[matrixSize];
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][i];
            }

            futures.add(executor.submit(() -> {
                final int[] columnC = new int[matrixSize];

                for (int row = 0; row < matrixSize; row++) {
                    final int[] thisRow = matrixA[row];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += thisRow[k] * thatColumn[k];
                    }
                    columnC[row] = sum;
                }
                return new MatrixColumn(col, columnC);
            }));
        }

        for (int i = 0; i < matrixSize; i++) {
            MatrixColumn columnFuture = futures.get(i).get();
            for (int j = 0; j < matrixSize; j++) {
                matrixC[j][columnFuture.column] = columnFuture.columnByRow[j];
            }
        }
        return matrixC;
    }

    // optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        try {
            for (int i = 0; ; i++) {
                final int[] thatColumn = new int[matrixSize];
                for (int k = 0; k < matrixSize; k++) {
                    thatColumn[k] = matrixB[k][i];
                }

                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    final int[] thisRow = matrixA[j];
                    for (int k = 0; k < matrixSize; k++) {
                        sum += thisRow[k] * thatColumn[k];
                    }
                    matrixC[j][i] = sum;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {/*NOP*/}
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
