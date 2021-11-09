package garrick.libs.vec;

public final class MatrixOps {
    private MatrixOps() {}

    public static DoubleVec vec(double... vec) {
        return new DoubleVec(vec);
    }

    public static DoubleVec vecm(double... vec) {
        return new DoubleVec.Mut(vec);
    }

    public static DoubleMatrix mat(DoubleVec... vecs) {
        return new DoubleMatrix(vecs);
    }

    public static DoubleMatrix mat(int rows, int cols) {
        return new DoubleMatrix(rows, cols);
    }

    public static DoubleMatrix matm(DoubleVec... vecs) {
        return new DoubleMatrix.Mut(vecs);
    }

    public static DoubleMatrix matm(int rows, int cols) {
        return new DoubleMatrix.Mut(rows, cols);
    }
}
