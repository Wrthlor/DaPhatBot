package com.github.wrthlor.daphatbot.genshin;

// Damage calculation based off KeqingMains
// Check https://library.keqingmains.com/mechanics/combat/damage-formula
public class GenshinDamageCalculator {

    protected double totalAttack, damagePercent, critRate, critDamage;
    protected double resistanceMultiplier, defenseMultiplier;

    // Assumptions: enemy base RES = 10% (RES_multiplier = 0.9) and character level = enemy level (DEF_multiplier = 0.5)
    public GenshinDamageCalculator() {
        this(0, 0, 0, 0, 0.9, 0.5);
    }

    public GenshinDamageCalculator(double attack, double dmgBonus, double cRate, double cDmg) {
        this(attack, dmgBonus, cRate, cDmg, 0.9, 0.5);
    }

    // Only RES_multiplier is provided
    public GenshinDamageCalculator(double attack, double dmgBonus, double cRate, double cDmg, double resMult) {
        this(attack, dmgBonus, cRate, cDmg, resMult, 0.5);
    }

    // Both RES_multiplier and DEF_multiplier are provided
    public GenshinDamageCalculator(double attack, double dmgBonus, double cRate, double cDmg, double resMult, double defMult) {
        this.totalAttack = attack;
        this.damagePercent = dmgBonus;
        // If cRate is below zero, treat as 0%
        // If cRate is above hundred, treat as 100%
        this.critRate = cRate < 0 ? 0 : Math.min(cRate, 100);
        this.critDamage = cDmg;
        this.resistanceMultiplier = resMult;
        this.defenseMultiplier = defMult;
    }

    // Shows given stats
    @Override
    public String toString() {
        return "ATK: " + this.totalAttack +  "\nDMG%: " + this.damagePercent + "%\n" +
            "CRIT_Rate: " + this.critRate + "%\nCRIT_DMG: " + this.critDamage + "%\n" +
            "RES_Multiplier: " + this.resistanceMultiplier + "\nDEF_Multiplier: " + this.defenseMultiplier;
    }

    // Calculate base damage
    // Outgoing_Damage = Total_Attack * (1 + Dmg%) * RES_multiplier * DEF_multiplier
    public int calculateBase() {
        return (int) (this.totalAttack * (1 + this.damagePercent / 100) * this.resistanceMultiplier * this.defenseMultiplier);
    }

    // Calculate critical hits
    // Outgoing_Damage_Crit = Total_Attack * (1 + Dmg%) * (1 + CritDmg%) * RES_multiplier *  DEF_multiplier
    public int calculateCritical() {
        return (int) (this.calculateBase() * (1 + this.critDamage / 100));
    }

    // Calculate the average output damage (over infinite amount of time)
    // Outgoing_Damage_Average = Total_Attack * (1 + Dmg%) * CRIT_multiplier * RES_multiplier *  DEF_multiplier
    // where CRIT_multiplier = (1 + (min(Crit_Rate, 100%) * Crit_Damage))
    public int calculateAverageDamage() {
        double outgoingDamage = this.calculateBase();
        double critMultiplier = 1 + (this.critRate / 100) * (this.critDamage / 100);

        return (int) (outgoingDamage * critMultiplier);
    }
}