package garrick.libs.vec;

import java.util.PrimitiveIterator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public abstract class AbstractDoubleVec {
    public abstract int length();
    public abstract double get(int i);
    public abstract void set(int i, double v);

    public PrimitiveIterator.OfDouble iterator() {
        return new PrimitiveIterator.OfDouble() {
            private int i = 0;
            private final int length = length();

            @Override
            public double nextDouble() {
                return get(i++);
            }

            @Override
            public boolean hasNext() {
                return i < length;
            }
        };
    }
    
    // ---------------- Element-wise Operations ----------------
    
    protected abstract AbstractDoubleVec target();
    
    public AbstractDoubleVec unaryOp(DoubleUnaryOperator unaryOperator) {
        var vec = target();
        var length = vec.length();

        for (int i = 0; i < length; i++) {
            vec.set(i, unaryOperator.applyAsDouble(vec.get(i)));
        }
        
        return vec;
    }
    
    private static final DoubleUnaryOperator NEGATE = d -> -d;
    
    public AbstractDoubleVec negate() {
        return unaryOp(NEGATE);
    }
    
    public AbstractDoubleVec fill(double value) {
        return unaryOp(ignore -> value);
    }
    
    public AbstractDoubleVec binaryOp(DoubleBinaryOperator binaryOperator, DoubleSupplier doubleSupplier) {
        var vec = target();
        var length = vec.length();

        for (int i = 0; i < length; i++) {
            vec.set(i, binaryOperator.applyAsDouble(vec.get(i), doubleSupplier.getAsDouble()));
        }
        
        return vec;
    }
    
    public AbstractDoubleVec binaryOp(DoubleBinaryOperator binaryOperator, double value) {
        return binaryOp(binaryOperator, () -> value);
    }
    
    private static final DoubleBinaryOperator ADD = Double::sum;
    
    public AbstractDoubleVec add(DoubleSupplier doubleSupplier) {
        return binaryOp(ADD, doubleSupplier);
    }
    
    public AbstractDoubleVec add(double value) {
        return binaryOp(ADD, value);
    }
    
    private static final DoubleBinaryOperator SCALE = (l, r) -> l * r;
    
    public AbstractDoubleVec scale(DoubleSupplier doubleSupplier) {
        return binaryOp(SCALE, doubleSupplier);
    }
    
    public AbstractDoubleVec scale(double value) {
        return binaryOp(SCALE, value);
    }

    public AbstractDoubleVec normalize() {
        return scale(1/l2Norm());
    }
    
    private static final DoubleBinaryOperator MAX = Math::max;
    
    public AbstractDoubleVec max(DoubleSupplier doubleSupplier) {
        return binaryOp(MAX, doubleSupplier);
    }
    
    public AbstractDoubleVec max(double value) {
        return binaryOp(MAX, value);
    }
    
    private static final DoubleBinaryOperator MIN = Math::min;
    
    public AbstractDoubleVec min(DoubleSupplier doubleSupplier) {
        return binaryOp(MIN, doubleSupplier);
    }
    
    public AbstractDoubleVec min(double value) {
        return binaryOp(MIN, value);
    }

    // ---------------- Reduction Operations ----------------
    
    public double reduce(double acc, DoubleBinaryOperator binaryOperator) {
        var length = length();

        for (int i = 0; i < length; i++) {
            acc = binaryOperator.applyAsDouble(acc, get(i));
        }
        
        return acc;
    }
    
    public double sum() {
        return reduce(0, ADD);
    }
    
    public double prod() {
        return reduce(1, SCALE);
    }
    
    public double max() {
        return reduce(Double.NEGATIVE_INFINITY, MAX);
    }
    
    public double min() {
        return reduce(Double.POSITIVE_INFINITY, MIN);
    }
    
    public double mean() {
        return sum() / length();
    }
    
    public DoubleStream stream() {
        return DoubleStream.generate(iterator()::nextDouble).limit(length());
    }
    
    public double l2Norm() {
        return stream().map(v -> v * v).sum() / (length() - 1);
    }

    public static final String DELIMITER = "\t";
    
    @Override
    public String toString() {
        var iterator = iterator();
        var sb = new StringBuilder();
        if (iterator.hasNext()) {
            sb.append("%.3f".formatted(iterator.nextDouble()));
        }
        while (iterator.hasNext()) {
            sb.append(DELIMITER).append("%.3f".formatted(iterator.nextDouble()));
        }
        return sb.toString();
    }

    public class Range extends AbstractDoubleVec {
        private final int offset, length;

        public Range(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public Range(int offset) {
            this(offset, AbstractDoubleVec.this.length() - offset);
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public double get(int i) {
            return AbstractDoubleVec.this.get(offset + i);
        }

        @Override
        public void set(int i, double v) {
            AbstractDoubleVec.this.set(offset + i, v);
        }

        @Override
        protected AbstractDoubleVec target() {
            return AbstractDoubleVec.this.target().new Range(offset, length);
        }
    }

    public DoubleMatrix reshape(int rows) {
        int nCols = length() / rows;

        if (rows * nCols != length()) {
            throw new IllegalArgumentException();
        }

        return new DoubleMatrix(IntStream.range(0, rows).mapToObj(i -> new Range(i * nCols, nCols)).toArray(AbstractDoubleVec[]::new));
    }

    public double dotProd(AbstractDoubleVec other) {
        var iterator = other.iterator();
        return reduce(0, (l, r) -> l + r * iterator.nextDouble());
    }

    public double innerProd() {
        return dotProd(this);
    }
}
