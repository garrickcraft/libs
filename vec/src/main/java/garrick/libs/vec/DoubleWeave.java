package garrick.libs.vec;

public class DoubleWeave extends AbstractDoubleVec {
    private final int index;
    private final AbstractDoubleVec[] vecs;

    public DoubleWeave(int index, AbstractDoubleVec[] vecs) {
        this.index = index;
        this.vecs = vecs;
    }

    @Override
    public int length() {
        return vecs.length;
    }

    @Override
    public double get(int i) {
        return vecs[i].get(index);
    }

    @Override
    public void set(int i, double v) {
        vecs[i].set(index, v);
    }

    @Override
    protected AbstractDoubleVec target() {
        return this;
    }
}
