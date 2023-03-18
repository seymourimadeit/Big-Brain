package tallestegg.bigbrain.common.entity;

//TODO convert this into a capability when i port to 1.20
public interface IBucklerUser {
    int getCooldown();
    
    void setCooldown(int cooldown);
    
    void setBucklerDashing(boolean charging);
    
    boolean isBucklerDashing();
    
    int getBucklerUseTimer();
    
    void setBucklerUseTimer(int useTimer);
}
