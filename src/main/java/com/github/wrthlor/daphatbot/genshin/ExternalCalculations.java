package com.github.wrthlor.daphatbot.genshin;

public class ExternalCalculations {

    // Calculates RES_multiplier
    public double resistanceMultiplier(double resistance) {
        resistance = resistance / 100;
        if (resistance < 0) {
            return 1 - resistance / 2;
        }
        else if (resistance > 0.75) {
            return 1 / (4 * resistance + 1);
        }
        else {
            return 1 - resistance;
        }
    }

    // Calculates DEF_multiplier
    public double defenseMultiplier(double characterLevel, double enemyLevel) {
        return defenseMultiplier(characterLevel, enemyLevel, 0.0);
    }

    // Defense reduction provided
    // Defense shred is hard capped at 90%
    public double defenseMultiplier(double characterLevel, double enemyLevel, double defReduction) {
        return (characterLevel + 100) / ((characterLevel + 100) + (enemyLevel + 100) * ( 1 - Math.min(defReduction, 90) / 100));
    }

    // Calculate common external ATK% buffs
    // Total_ATK = [Base_ATK * (1 + ATK%)] + Flat_ATK = Base_ATK + (Base_ATK * ATK%) + Flat_ATK
    // (Base_ATK * ATK%) is what's being calculated
    // Common cases: 4NO (20%), Pyro Resonance (25%), and TToDS [(18+6R)% where R=refinement levels]
    public double commonExtraAttack(double baseAttack, int noblesse, int pyro,  int refinement) {
        if (refinement == 0) {
            return baseAttack * ((noblesse * .2) + (pyro * .25));
        }
        return baseAttack * ((noblesse * .2) + (pyro * .25) + (refinement * 0.06 + 0.18));
    }
}
