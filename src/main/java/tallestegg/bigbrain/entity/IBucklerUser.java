package tallestegg.bigbrain.entity;

public interface IBucklerUser {
    int getCooldown();
    
    void setCooldown(int cooldown);
    
    void setBucklerDashing(boolean charging);
    
    boolean isBucklerDashing();
    
    int getBucklerUseTimer();
    
    void setBucklerUseTimer(int useTimer);
}
