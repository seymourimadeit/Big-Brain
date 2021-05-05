package tallestegg.bigbrain.entity;

//TODO convert this into a capability when i port to 1.17
public interface IBucklerUser {
    int getCooldown();
    
    void setCooldown(int cooldown);
    
    void setBucklerDashing(boolean charging);
    
    boolean isBucklerDashing();
    
    int getBucklerUseTimer();
    
    void setBucklerUseTimer(int useTimer);
}
