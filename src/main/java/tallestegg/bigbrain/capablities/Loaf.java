package tallestegg.bigbrain.capablities;

public class Loaf implements ILoaf {
    private boolean loaf;

    @Override
    public boolean isLoafing() {
        return loaf;
    }

    @Override
    public void setLoafing(boolean loaf) {
        this.loaf = loaf;
    }
}
