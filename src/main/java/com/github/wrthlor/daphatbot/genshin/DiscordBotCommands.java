package com.github.wrthlor.daphatbot.genshin;

public class DiscordBotCommands {

    private String command;
    private String parameters;
    private String[] numbers;

    public DiscordBotCommands(String cmd, String para) {
        this.command = cmd;
        this.parameters = para;
        // Splits given parameters string by whitespaces
        this.numbers = this.parameters.trim().split("\\s+");
    }

    public String[] getNumbers() {
        return this.numbers;
    }

    public String checkFormat() {

        // Checks for negative number input
        // Specifically for `p!calcRes` command
        String negRegex = "\\-?(\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+)";
        if (this.command.equals("p!calcRes")) {
            if (numbers.length != 1) {
                return "Please use format: \n`" + this.command + " Enemy_RES`";
            }

            if (numbers[0].matches(negRegex)) {
                return "Success";
            }
        }

        // calcRes command
        if (this.command.equals("p!calcRes")) {
            if (numbers.length != 1) {
                return "Please use format: \n`" + this.command + " Enemy_RES`";
            }
        }

        // Checks for acceptable input format
        // Acceptable number format: X, X., X.X, .X
        String regex = "\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+";
        for (String nums : numbers) {
            if (!nums.matches(regex)) {
                if (this.command.equals("p!damage")) {
                    return "Please use format: \n`" + this.command + " ATK DMG% CRIT_Rate CRIT_DMG`";
                }
                else if (this.command.equals("p!parry") || this.command.equals("p!ult")) {
                    return "Please use format: \n`" + this.command + " ATK DMG% CRIT_Rate CRIT_DMG Talent_lvl`";
                }
                else if (this.command.equals("p!calcRes")) {
                    return "Please use format: \n`" + this.command + " Enemy_RES`";
                }
                else if (this.command.equals("p!calcDef")) {
                    return "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
                }
            }
        }

        // Damage command
        if (this.command.equals("p!damage")) {
            if (numbers.length != 4) {
                return "Please use format: \n`" + this.command + " ATK DMG% CRIT_Rate CRIT_DMG`";
            }
        }

//        // calcRes command
//        if (this.command.equals("p!calcRes")) {
//            if (numbers.length != 1) {
//                return "Please use format: \n`" + this.command + " Enemy_RES`";
//            }
//        }

        // calcDef command
        if (this.command.equals("p!calcDef")) {
            if (numbers.length < 2 || numbers.length > 3) {
                return "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
            }
        }

        // Beidou commands
        if (this.command.equals("p!parry") || this.command.equals("p!ult")) {
            // Checks for 5 inputs (5th = talent level)
            if (numbers.length != 5) {
                return "Please use format: \n`" + this.command + " ATK DMG% CRIT_Rate CRIT_DMG Talent_lvl`";
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