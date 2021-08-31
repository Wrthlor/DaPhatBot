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

        // Checks for acceptable input format
        // Acceptable number format: X, X., X.X, .X
        String regex = "\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+";
        for (String nums : numbers) {

            // Checks for negative number input
            // Specifically for `p!calcRes` command
            if (this.command.equals("p!calcRes")) {
                if (!nums.matches("\\-?"+regex)) {
                    return "Please use format: \n`" + this.command + " Enemy_RES`";
                }
            }
            else {
                if (!nums.matches(regex)) {
                    if (this.command.equals("p!damage")) {
                        return "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]`";
                    }
                    else if (this.command.equals("p!parry") || this.command.equals("p!ult")) {
                        return "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]`";
                    }
                    else if (this.command.equals("p!calcDef")) {
                        return "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
                    }
                }
            }
        }

        // Damage command
        if (this.command.equals("p!damage")) {
            if (numbers.length < 4 || numbers.length > 6) {
                return "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]`";
            }
        }

        // calcRes command
        if (this.command.equals("p!calcRes")) {
            if (numbers.length != 1) {
                return "Please use format: \n`" + this.command + " Enemy_RES`";
            }
        }

        // calcDef command
        if (this.command.equals("p!calcDef")) {
            if (numbers.length < 2 || numbers.length > 3) {
                return "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
            }
        }

        // Beidou commands
        if (this.command.equals("p!parry") || this.command.equals("p!ult")) {
            if (numbers.length < 5 || numbers.length > 7) {
                return "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]`";
            }

            // Checks talent level input is within reachable values: 1-13 inclusive
            int value = (int) Double.parseDouble(this.numbers[4]);
            if (value < 1 || value > 13) {
                return "`Talent_lvl` must be between 1 and 13, inclusive";
            }
        }

        return "Success";
    }
}