package com.github.wrthlor.daphatbot.genshin;

// Damage calculation based off KeqingMains
// Check https://library.keqingmains.com/mechanics/combat/damage-formula
public class GenshinDamageCalculator {

    protected double totalAttack, damagePercent, critRate, critDamage;
    // Assumptions: character level = enemy level (DEF multiplier = 50%) and enemy base RES = 10%
    protected double defenseMultiplier = 0.5;
    protected double resistance = 0.1;

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

    // Can be used for future implementation if enemy RES values are a given argument
    // Currently should be static and return 0.9
    // Calculates RES_multiplier
    public double calculateRes() {
        if (this.resistance < 0) {
            return 1 - this.resistance / 2;
        }
        else if (this.resistance > 0.75) {
            return 1 / (4 * this.resistance + 1);
        }
        else {
            return 1 - this.resistance;
        }
    }

    // Calculate base damage
    // Outgoing_Damage = Total_Attack * (1 + Dmg%) *  DEF_multiplier * RES_multiplier
    public int calculateBase() {
        return (int) (this.totalAttack * (1 + this.damagePercent / 100) * this.defenseMultiplier * this.calculateRes());
    }

    // Calculate critical hits
    // Outgoing_Damage_Crit = Total_Attack * (1 + Dmg%) * (1 + CritDmg%) *  DEF_multiplier * RES_multiplier
    public int calculateCritical() {
        return (int) (this.calculateBase() * (1 + this.critDamage / 100));
    }

    // Calculate the average output damage (over infinite amount of time)
    // Outgoing_Damage_Average = Total_Attack * (1 + Dmg%) * CRIT_multiplier *  DEF_multiplier * RES_multiplier
    // where CRIT_multiplier = (1 + (min(Crit_Rate, 100%) * Crit_Damage))
    public int calculateAverageDamage() {
        double outgoingDamage = this.calculateBase();
        double critMultiplier = 1 + Math.min(this.critRate / 100, 1) * (this.critDamage / 100);

        return (int) (outgoingDamage * critMultiplier);
    }
}