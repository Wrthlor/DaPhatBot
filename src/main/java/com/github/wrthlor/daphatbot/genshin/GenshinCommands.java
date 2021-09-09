package com.github.wrthlor.daphatbot.genshin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenshinCommands {

    private String command;
    private String parameters;
    private String[] inputs;
    private boolean acceptableFormat;
    private String message;

    public GenshinCommands(String cmd, String para) {
        this.command = cmd;
        this.parameters = para.trim();

        // Splits given parameters string by whitespaces
        this.inputs = this.parameters.split("\\s+");

        this.acceptableFormat = false;
        this.message = "";
    }

    public String[] getInputs() {
        return this.inputs;
    }

    public boolean getAcceptableFormat() {
        return this.acceptableFormat;
    }

    public String getMessage() {
        return this.message;
    }

    public void checkFormat() {

        // Checks for acceptable inputs
        // Acceptable number format: X, X., X.X, .X
        String regex = "\\-?(\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+)";
        Pattern pattern = Pattern.compile(regex);
        for (String nums : inputs) {

            // Checks for specific possible string input
            // Specifically for `p!extraATK` command
            if (this.command.equals("p!extraATK")) {
                String extraAttackRegex = "\\-?(\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+)\\s+[yYnN]\\s+[yYnN]\\s+([yYnN]|\\d+)";
                if (!this.parameters.matches(extraAttackRegex)) {
                    this.message = "Please use format: \n`" + this.command + " baseATK 4NO pyro TToDS` \n" +
                        "└ Example: `" + this.command + " 735 y n 3` calculates with 4NO and R3 TToDS \n" +
                        "*Notes:* \n" +
                        "• Use **left, white ATK** stat on characters stats for baseATK \n" +
                        "• Include/exclude calculation with y/n \n" +
                        "• TToDS: `1-5` for refinements (integers only), `y = R5`, `n` excludes TToDS";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // For all other commands (don't take negative inputs)
            Matcher matcher = pattern.matcher(nums);
            if (!matcher.matches()) {
                switch(this.command) {
                    // p!damage
                    case "p!damage": {
                        this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]` \n" +
                            "└ Example: `" + this.command + " 1935 130.6 87.0 175.1` \n" +
                            "*Notes:* \n" +
                            "• Bracketed inputs are optional: `RES_Mult`, `DEF_Mult`";
                        this.acceptableFormat = false;
                        return;
                    }

                    // p!calcRES
                    case "p!calcRES": {
                        this.message = "Please use format: \n`" + this.command + " Enemy_RES` \n" +
                            "└ Example: `" + this.command + " -10` \n";
                        this.acceptableFormat = false;
                        return;
                    }

                    // p!calcDef
                    case "p!calcDEF": {
                        this.message = "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]` \n" +
                            "└ Example: `" + this.command + " 90 100` \n" +
                            "*Notes:* \n" +
                            "• Bracketed inputs are optional: `DEF_Reduction`";
                        this.acceptableFormat = false;
                        return;
                    }

                    // p!parry or p!ult
                    default: {
                        this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` \n" +
                            "└ Example: `" + this.command + " 1935 130.6 87.0 175.1 13 1.05` \n" +
                            "*Notes:* \n" +
                            "• Bracketed inputs are optional: `RES_Mult`, `DEF_Mult`";
                        this.acceptableFormat = false;
                        return;
                    }
                }
            }
        }

        switch (this.command) {
            // p!damage
            case "p!damage": {
                if (inputs.length < 4 || inputs.length > 6) {
                    this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]` \n" +
                        "└ Example: `" + this.command + " 1935 130.6 87.0 175.1` \n" +
                        "*Notes:* \n" +
                        "• Bracketed inputs are optional: `RES_Mult`, `DEF_Mult`";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!extraATK
            case "p!extraATK": {
                if (inputs.length != 4) {
                    this.message = "Please use format: \n`" + this.command + " baseATK 4NO pyro TToDS` \n" +
                        "└ Example: `" + this.command + " 735 y n 3` calculates with 4NO and R3 TToDS \n" +
                        "*Notes:* \n" +
                        "• Use **left, white ATK** stat on characters stats for baseATK \n" +
                        "• Include/exclude calculation with y/n \n" +
                        "• TToDS: `1-5` for refinements (integers only), `y = R5`, `n` excludes TToDS";
                    this.acceptableFormat = false;
                    return;
                }

                // Checks if TToDS input is yYnN
                // yY = R5, nN = do not calculate
                if (this.inputs[3].equalsIgnoreCase("y") || this.inputs[3].equalsIgnoreCase("n")) {
                    break;
                }

                // Checks talent level input is within reachable values: 1-5 inclusive
                double value = Double.parseDouble(this.inputs[3]);
                if (value < 1 || value > 5) {
                    this.message = "TToDS refinements must be between 1 and 5, inclusive";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!calcRES
            case "p!calcRES": {
                if (inputs.length != 1) {
                    this.message = "Please use format: \n`" + this.command + " Enemy_RES` \n" +
                        "└ Example: `" + this.command + " -10` \n";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!calcDEF
            case "p!calcDEF": {
                if (inputs.length < 2 || inputs.length > 3) {
                    this.message = "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]` \n" +
                        "└ Example: `" + this.command + " 90 100` \n" +
                        "*Notes:* \n" +
                        "• Bracketed inputs are optional: `DEF_Reduction`";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!parry or p!ult
            default: {
                if (inputs.length < 5 || inputs.length > 7) {
                    this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` \n" +
                        "└ Example: `" + this.command + " 1935 130.6 87.0 175.1 13 1.05` \n" +
                        "*Notes:* \n" +
                        "• Bracketed inputs are optional: `RES_Mult`, `DEF_Mult`";
                    this.acceptableFormat = false;
                    return;
                }

                // Checks talent level input is within reachable values: 1-13 inclusive
                double value = Double.parseDouble(this.inputs[4]);
                if (value < 1 || value > 13) {
                    this.message = "`Talent_lvl` must be between 1 and 13, inclusive";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }
        }

        this.acceptableFormat = true;
    }
}