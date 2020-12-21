package tallestegg.bigbrain.entity;

public interface IBucklerUser {
    int getCooldown();
    
    void setCooldown(int cooldown);
    
    void setCharging(boolean charging);
    
    boolean isCharging();
}
