package com.github.wrthlor.daphatbot.genshin;

// Damage calculation based off KeqingMains
// Check https://library.keqingmains.com/mechanics/combat/damage-formula
public class GenshinDamageCalculator {

    protected double totalAttack, damagePercent, critRate, critDamage;

    public GenshinDamageCalculator() {
        this.totalAttack = this.damagePercent = this.critRate = this.critDamage = 0.0;
    }

    public GenshinDamageCalculator(double attack, double dmgBonus, double cRate, double cDmg) {
        this.totalAttack = attack;
        this.damagePercent = dmgBonus;
        this.critRate = cRate;
        this.critDamage = cDmg;
    }

    // Shows given stats
    @Override
    public String toString() {
        return "ATK: " + this.totalAttack +  "\nDMG%: " + this.damagePercent + "%"
            + "\nCRIT_Rate: " + this.critRate + "%\nCRIT_DMG: " + this.critDamage + "%";
    }

    // Calculate base damage
    // Outgoing_Damage = Total_Attack * (1 + Dmg%)
    public int calculateBase() {
        return (int) (this.totalAttack * (1 + this.damagePercent / 100));
    }

    // Calculate critical hits
    // Outgoing_Damage_Crit = Total_Attack * (1 + Dmg%) * (1 + CritDmg%)
    public int calculateCritical() {
        return (int) (this.totalAttack * (1 + this.damagePercent / 100) * ( 1 + this.critDamage / 100));
    }

    // Calculate the average output damage (over infinite amount of time)
    // Outgoing_Damage_Average = Total_Attack * (1 + Dmg%) * (1 + (min(Crit_Rate, 100%) * Crit_Damage))
    public int calculateAverageDamage() {
        double outgoingDamage = (this.totalAttack * (1 + this.damagePercent / 100));
        double critFactor = 1 + Math.min(this.critRate / 100, 1) * (this.critDamage / 100);

        return (int)(outgoingDamage * critFactor);
    }
}