package com.github.wrthlor.daphatbot.genshin;

// Beidou info gathered from wiki
// https://genshin-impact.fandom.com/wiki/Tidecaller
// https://genshin-impact.fandom.com/wiki/Stormbreaker
public class Beidou extends GenshinDamageCalculator {

    private int talent;
    // Tidecaller ratios
    final private double[] base = {122, 131, 140, 152, 161, 170, 182, 195, 207, 219, 231, 243, 258};
    final private double[] dmgBonus = {160, 172, 184, 200, 212, 224, 240, 256, 272, 288, 304, 320, 340};
    // Stormbreaker ratios
    final private double[] lightning = {96, 103, 110, 120, 127, 134, 144, 154, 163, 173, 182, 192, 203};

    public Beidou() {
        this(0, 0, 0, 0, 1, 0.9, 0.5);
    }

    public Beidou(double attack, double dmgBonus, double cRate, double cDmg, double level) {
        this(attack, dmgBonus, cRate, cDmg, level, 0.9, 0.5);
    }

    // Only RES_multiplier is provided
    public Beidou(double attack, double dmgBonus, double cRate, double cDmg, double level, double resMult) {
        this(attack, dmgBonus, cRate, cDmg, level, resMult, 0.5);
    }

    // Both RES_multiplier and DEF_multiplier are provided
    public Beidou(double attack, double dmgBonus, double cRate, double cDmg, double level, double resMult, double defMult) {
        this.totalAttack = attack;
        this.damagePercent = dmgBonus;
        this.critRate = cRate;
        this.critDamage = cDmg;
        this.talent = (int) level;
        this.resistanceMultiplier = resMult;
        this.defenseMultiplier = defMult;
    }

    // Shows given stats
    @Override
    public String toString() {
        return "ATK: " + this.totalAttack +  "\nDMG%: " + this.damagePercent + "%\n"
            + "CRIT_Rate: " + this.critRate + "%\nCRIT_DMG: " + this.critDamage + "%\n"
            + "Talent_lvl: " + this.talent;
    }

    // Calculates Tidecaller damage with respective number of hits absorbed
    // Stored into array to hold base, crits, and average
    public int[] calculateParry(int hits) {
        int[] results = new int[3];
        results[0] = (int) (this.calculateBase() * (base[talent - 1] + hits * dmgBonus[talent - 1]) / 100);
        results[1] = (int) (this.calculateCritical() * (base[talent- 1] + hits * dmgBonus[talent - 1]) / 100);
        results[2] = (int) (this.calculateAverageDamage() * (base[talent - 1] + hits * dmgBonus[talent - 1]) / 100);
        return results;
    }

    // Calculates Stormbreaker damage
    // Stored into array to hold base, crits, and average
    public int[] calculateUlt() {
        int[] results = new int[3];
        results[0] = (int) (this.calculateBase() * lightning[talent - 1] / 100);
        results[1] = (int) (this.calculateCritical() * lightning[talent - 1] / 100);
        results[2] = (int) (this.calculateAverageDamage() * lightning[talent - 1] / 100);
        return results;
    }
}
