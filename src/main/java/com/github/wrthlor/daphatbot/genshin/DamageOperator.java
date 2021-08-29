package com.github.wrthlor.daphatbot.genshin;

public class DamageOperator {

    private String command;
    private String parameters;
    private String[] numbers;

    public DamageOperator(String cmd, String para) {
        this.command = cmd;
        this.parameters = para;

        // Splits given parameters string by whitespaces
        this.numbers = this.parameters.trim().split("\\s+");
    }

    public String[] getNumbers() {
        return this.numbers;
    }

    public String checkFormat() {

        // Checks for acceptable input format
        // Acceptable number format: X, X., X.X, .X
        String regex = "\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+";
        for (String nums : numbers) {
            if (!nums.matches(regex)) {
                if (this.command.equals("damage")) {
                    return "Please use format: \n`!damage ATK DMG% CRIT_Rate CRIT_DMG`";
                }
                else {
                    return "Please use format: \n`!damage ATK DMG% CRIT_Rate CRIT_DMG Talent_lvl`";
                }
            }
        }

        // Damage command
        if (this.command.equals("damage")) {
            if (numbers.length != 4) {
                return "Please use format: \n`!damage ATK DMG% CRIT_Rate CRIT_DMG`";
            }
        }

        // Beidou commands
        if (this.command.equals("parry") || this.command.equals("ult")) {
            // Checks for 5 inputs (5th = talent level)
            if (numbers.length != 5) {
                return "Please use format: \n`!damage ATK DMG% CRIT_Rate CRIT_DMG Talent_lvl`";
            }

            // Checks talent level input is within reachable values: 1-13 inclusive
            int value = (int) Double.parseDouble(this.numbers[4]);
            if (value < 1 || value > 13) {
                return "`Talent_lvl` must be between 1 and 13, inclusive";
            }
        }

        return "Success";
    }

    public GenshinDamageCalculator getDamage() {

        double totalAttack = Double.parseDouble(numbers[0]);
        double damageBonus = Double.parseDouble(numbers[1]);
        double critRate = Double.parseDouble(numbers[2]);
        double critDamage = Double.parseDouble(numbers[3]);

        return new GenshinDamageCalculator(totalAttack, damageBonus, critRate, critDamage);
    }

    public Beidou getBeidou() {

        double totalAttack = Double.parseDouble(numbers[0]);
        double damageBonus = Double.parseDouble(numbers[1]);
        double critRate = Double.parseDouble(numbers[2]);
        double critDamage = Double.parseDouble(numbers[3]);
        int talent = (int) Double.parseDouble(numbers[4]);

        return new Beidou(totalAttack, damageBonus, critRate, critDamage, talent);
    }
}