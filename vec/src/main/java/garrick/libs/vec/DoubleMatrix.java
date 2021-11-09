package garrick.libs.vec;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DoubleMatrix extends AbstractDoubleVec {
    private final int nCols;
    private final AbstractDoubleVec[] rows;

    public DoubleMatrix(DoubleMatrix other) {
        this.rows = Arrays.stream(other.rows).map(row -> row.stream().toArray()).map(DoubleVec::new).toArray(AbstractDoubleVec[]::new);
        this.nCols = other.nCols;
        this.length = other.length;
    }

    public DoubleMatrix(AbstractDoubleVec... rows) {
        if (Arrays.stream(rows).mapToInt(AbstractDoubleVec::length).max().orElse(0)
                != Arrays.stream(rows).mapToInt(AbstractDoubleVec::length).min().orElse(0)) {
            throw new IllegalArgumentException();
        }

        this.rows = rows;
        nCols = rows.length == 0 ? 0 : rows[0].length();
        this.length = rows.length * nCols;
    }

    public DoubleMatrix(int nRows, int nCols) {
        this.rows = Arrays.stream(new double[nRows][nCols]).map(DoubleVec::new).toArray(AbstractDoubleVec[]::new);
        this.nCols = nCols;
        this.length = nRows * nCols;
    }

    public DoubleMatrix transpose() {
        return new DoubleMatrix(IntStream.range(0, nCols).mapToObj(i -> new DoubleWeave(i, rows)).toArray(AbstractDoubleVec[]::new));
    }

    public DoubleMatrix mult(DoubleMatrix other) {
        var t = other.transpose();

        int m = rows.length;
        int p = t.rows.length;
        var result = new DoubleMatrix(m, p);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                result.set2(i, j, rows[i].dotProd(t.rows[j]));
            }
        }

        return result;
    }

    private final int length;

    @Override
    public int length() {
        return length;
    }

    @Override
    public double get(int i) {
        return rows[i / nCols].get(i % nCols);
    }

    public double get2(int row, int col) {
        return rows[row].get(col);
    }

    @Override
    public void set(int i, double v) {
        rows[i / nCols].set(i % nCols, v);
    }

    public void set2(int row, int col, double v) {
        rows[row].set(col, v);
    }

    @Override
    protected DoubleMatrix target() {
        return new DoubleMatrix(this);
    }

    @Override
    public String toString() {
        return "\n\t" + Arrays.stream(rows).map(Object::toString).collect(Collectors.joining("\n\t"));
    }

    public static class Mut extends DoubleMatrix {
        public Mut(AbstractDoubleVec... rows) {
            super(rows);
        }

        public Mut(int nRows, int nCols) {
            super(nRows, nCols);
        }

        @Override
        protected DoubleMatrix target() {
            return this;
        }
    }
}
