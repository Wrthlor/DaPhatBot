package com.github.wrthlor.daphatbot.genshin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordBotCommands {

    private String command;
    private String parameters;
    private String[] inputs;
    private boolean acceptableFormat;
    private String message;

    public DiscordBotCommands(String cmd, String para) {
        this.command = cmd;
        this.parameters = para;

        // Splits given parameters string by whitespaces
        this.inputs = this.parameters.trim().split("\\s+");

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
        String regex = "\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+";
        Pattern pattern = Pattern.compile(regex);

        String negRegex = "\\-?(\\d+|\\d+\\.|\\d+\\.\\d+|\\.\\d+)";
        for (String nums : inputs) {

            // Checks for negative number input
            // Specifically for `p!calcRes` command
            if (this.command.equals("p!calcRes")) {
                if (!nums.matches(negRegex)) {
                    this.message = "Please use format: \n`" + this.command + " Enemy_RES`";
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
                        this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]`";
                        this.acceptableFormat = false;
                        return;
                    }

                    // p!calcDef
                    case "p!calcDef": {
                        this.message = "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
                        this.acceptableFormat = false;
                        return;
                    }

                    // p!parry or p!ult
                    default: {
                        this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]`";
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
                    this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]`";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!calcRes
            case "p!calcRes": {
                if (inputs.length != 1) {
                    this.message = "Please use format: \n`" + this.command + " Enemy_RES`";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!calcDef
            case "p!calcDef": {
                if (inputs.length < 2 || inputs.length > 3) {
                    this.message = "Please use format: \n`" + this.command + " Char_lvl Enemy_lvl [DEF_Reduction]`";
                    this.acceptableFormat = false;
                    return;
                }
                break;
            }

            // p!parry or p!ult
            default: {
                if (inputs.length < 5 || inputs.length > 7) {
                    this.message = "Please use format: \n`" + this.command + " ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]`";
                    this.acceptableFormat = false;
                    return;
                }

                // Checks talent level input is within reachable values: 1-13 inclusive
                int value = (int) Double.parseDouble(this.inputs[4]);
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