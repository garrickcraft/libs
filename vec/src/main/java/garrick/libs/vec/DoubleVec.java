package garrick.libs.vec;

public class DoubleVec extends AbstractDoubleVec {
    private final double[] vec;

    public DoubleVec(double... vec) {
        this.vec = vec;
    }

    @Override
    public int length() {
        return vec.length;
    }

    @Override
    public double get(int i) {
        return vec[i];
    }

    @Override
    public void set(int i, double v) {
        vec[i] = v;
    }

    public static class Mut extends DoubleVec {
        public Mut(double... vec) {
            super(vec);
        }

        @Override
        protected DoubleVec target() {
            return this;
        }
    }

    @Override
    protected DoubleVec target() {
        return new DoubleVec(vec.clone());
    }
}
