package tallestegg.bigbrain.common.enchantments.entity;

//TODO convert this into a capability when i port to 1.20
public interface IOneCriticalAfterCharge {
    boolean isCritical();
    
    void setCritical(boolean critical);
}
