package com.github.wrthlor.daphatbot;

import com.github.wrthlor.daphatbot.genshin.*;

// Discord4J packages
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;

// Java packages
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DaPhatBot {

    public static void main(String[] args) {

        // Use Heroku's environment file to store Discord bot token
        String token = System.getenv("TOKEN");
        // If argument for token is provided, use that instead 
		// Primarily for local testing
        if (args.length > 0) {
            token = args[0];
        }

        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build()
            .login()
            .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
            .subscribe(event -> {
                final String content = event.getMessage().getContent();

                for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                    // Using p! as our "prefix" to any command in the system
                    if (content.startsWith("p!" + entry.getKey())) {
                        entry.getValue().execute(event);
                        break;
                    }
                }
            });

		// Console output for clarity and tracking
        client.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(event -> {
                final User self = event.getSelf();
                System.out.println(String.format(
                    "Logged in as %s#%s", self.getUsername(), self.getDiscriminator()
                ));
            });

        client.onDisconnect().block();
    }

    // HashMap for list of Discord bot commands
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        // p!help command - Displays all available commands
        commands.put("help", event -> {
            String dpsMessages = "✅ `p!damage ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]` \n" +
                "└ Gets expected output damage, ignoring talents \n"+
                "✅ `p!extraATK baseATK 4NO pyro TToDS` \n" +
                "└ Example: `p!extraATK 735 y n 3` calculates with 4NO and R3 TToDS \n" +
                "└ Gets common, *external* ATK% buffs (4NO, pyro resonance, TToDS) \n" +
                "└ Use **left, white ATK** stat on Attributes menu for baseATK \n" +
                "└ Include/exclude calculation with y/n \n" +
                "└ TToDS: 1-5 for refinements (integers only), y = R5, n excludes TToDS \n" +
                "✅ `p!calcRES Enemy_RES` \n" +
                "└ Gets enemy resistance *multiplier* → RES_Mult \n"+
                "└ Enemy_RES is *additive* (subtract 40 when using 4VV) \n"+
                "✅ `p!calcDEF Char_lvl Enemy_lvl [DEF_Reduction]` \n" +
                "└ Gets enemy defense *multiplier* → DEF_Mult";
            String beidouMessages = "✅ `p!parry ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` \n" +
                "└ Gets expected Tidecaller damage \n" +
                "✅ `p!ult ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` \n" +
                "└ Gets expected Stormbreaker damage ";
            String notes = "Bracketed inputs are optional \n" +
                "└ Example: `p!calcDEF 80 95` and `p!calcDEF 80 95 10` are both valid \n" +
                "└ Negative numeric values are accepted. Use at your own risk";

            event.getMessage()
                .getChannel().block()
                // 0xe6e6fa = Lavender
                .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                    .setTitle("Genshin Calculator Commands")
                    .setUrl("https://library.keqingmains.com/mechanics/combat/damage-formula")
                    .addField("*=== General DPS commands ===*", dpsMessages, false)
                    .addField("*=== Beidou specific commands ===*", beidouMessages, false)
                    .addField("*=== Notes ===*", notes, false)
                    .setFooter("Bot by DaPhatWan#5333", "")
                ).block();
        });

        // p!damage command - calculates expected damage output (ignores talent)
        commands.put("damage", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 8);
            String parameters = input.substring(8);

            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();
            if (variables.getAcceptableFormat()) {

                ArrayList<Double> paramValues = new ArrayList<>();
                for (String nums : variables.getInputs()) {
                    paramValues.add(Double.parseDouble(nums));
                }

                String description;
                String notes = "Notes: \n" +
                    "• Talents are **NOT** factored. Please check wiki/in-game to get `Talent%` multiplier \n" +
                    "• Click link for more details \n";

                GenshinDamageCalculator results;
                // User provides RES_multiplier and DEF_multiplier
                if (paramValues.size() == 6) {
                    results = new GenshinDamageCalculator(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4), paramValues.get(5));

                    description = "Assumptions: \n" +
                        "• `DEF_Mult = " + paramValues.get(5) + "` \n" +
                        "• `RES_Mult = " + paramValues.get(4) + "` \n";
                }
                // User provides only RES_multiplier
                else if (paramValues.size() == 5) {
                    results = new GenshinDamageCalculator(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• `RES_Mult = " + paramValues.get(4) + "` \n";
                }
                // User provides no additional multipliers
                else {
                    results = new GenshinDamageCalculator(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• Enemy has `RES = 10%` → `RES_Mult = 0.9` \n";
                }
                String finalDescription = description + notes;

                String base = String.valueOf(results.calculateBase());
                String crit = String.valueOf(results.calculateCritical());
                String avg = String.valueOf(results.calculateAverageDamage());

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Damage Calculator")
                        .setUrl("https://library.keqingmains.com/mechanics/combat/damage-formula#general-formula-for-damage")
                        .setDescription(finalDescription)
                        .addField("*Base damage* ", base, true)
                        .addField("*Critical hit damage* ", crit, true)
                        .addField("*Average damage output* ", avg, true)
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Damage Calculator")
                        .setUrl("https://library.keqingmains.com/mechanics/combat/damage-formula#general-formula-for-damage")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!extraAtk command - calculates common external ATK% buffs (4NO, Pyro Resonance, TToDS)
        commands.put("extraATK", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 10);
            String parameters = input.substring(10);

            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();

            if (variables.getAcceptableFormat()) {
                ExternalCalculations attack = new ExternalCalculations();

                // Guaranteed 4 inputs
                String[] paramValues = variables.getInputs();
                double baseAttack = Double.parseDouble(paramValues[0]);
                int noblesse = (paramValues[1].equals("y") || paramValues[1].equals("Y")) ? 1 : 0;
                int pyro = (paramValues[2].equals("y") || paramValues[2].equals("Y")) ? 1 : 0;
                int refines;
                boolean letter;
                if (paramValues[3].equalsIgnoreCase("n")) {
                    refines = 0;
                    letter = true;
                }
                else if (paramValues[3].equalsIgnoreCase("y")) {
                    refines = 5;
                    letter = true;
                }
                else {
                    refines = Integer.parseInt(paramValues[3]);
                    letter = false;
                }

                int output = (int) attack.commonExtraAttack(baseAttack, noblesse, pyro, refines);
                // If not letter, output refinements
                // If is letter and y, output R5, else output n
                String ttods = !letter ? "R" + paramValues[3] :
                    paramValues[3].equalsIgnoreCase("y") ? "R5" : "n";

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Common External ATK% Buffs")
                        .setUrl("https://github.com/Wrthlor/DaPhatBot/blob/master/src/main/java/com/github/wrthlor/daphatbot/genshin/Images/CalculatingAdditionalAttack.png")
                        .setDescription("Base attack: " + baseAttack + " \n" + "4 Noblesse: " + paramValues[1] + " \n" +
                            "pyro resonance: " + paramValues[2] + " \n" + "TToDS: " + ttods + " \n\n" +
                            "Note: \n" +
                            "• Click link to find out how to calculate additional ATK% buffs \n\n" +
                            "***Extra ATK%:*** " + output)
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();

            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Common External ATK% Buffs")
                        .setUrl("https://github.com/Wrthlor/DaPhatBot/blob/master/src/main/java/com/github/wrthlor/daphatbot/genshin/Images/CalculatingAdditionalAttack.png")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!calcRes command - calculates RES multiplier for damage equation
        commands.put("calcRES", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 9);
            String parameters = input.substring(9);

            // p!calcRes Enemy_RES
            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();

            if (variables.getAcceptableFormat()) {
                ExternalCalculations resistance = new ExternalCalculations();

                String paramValue = variables.getInputs()[0];
                Double output = resistance.resistanceMultiplier(Double.parseDouble(paramValue));

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Resistance Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Resistance#RES_Percentage")
                        .setDescription("Enemy resistance: " + paramValue + "%\n" +
                            "\n***RES multiplier:*** " + String.format("%.4f", output))
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", ""))
                    .block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Resistance Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Resistance#RES_Percentage")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!calcDef command - calculates DEF multiplier for damage equation
        commands.put("calcDEF", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 9);
            String parameters = input.substring(9);

            // p!calcDef charLvl defLvl [defShred]
            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();

            if (variables.getAcceptableFormat()) {
                ExternalCalculations defense = new ExternalCalculations();

                ArrayList<Double> paramValues = new ArrayList<>();
                for (String nums : variables.getInputs()) {
                    paramValues.add(Double.valueOf(nums));
                }

                double output;
                String extra;
                if (paramValues.size() == 3) {
                    output = defense.defenseMultiplier(paramValues.get(0), paramValues.get(1), paramValues.get(2));
                    extra = "\nDefense reduction: " + paramValues.get(2) + "%";
                }
                else {
                    output = defense.defenseMultiplier(paramValues.get(0), paramValues.get(1));
                    extra = "";
                }

                String description = "Character level: " + paramValues.get(0) +
                    "\nEnemy level: " + paramValues.get(1) + extra +
                    "\n\n***DEF multiplier:*** " + String.format("%.4f", output);

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Defense Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Defense#Enemy_Defense")
                        .setDescription(description)
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", ""))
                    .block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Defense Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Defense#Enemy_Defense")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!parry command - calculates expected Beidou Tidecaller skill damage
        commands.put("parry", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 7);
            String parameters = input.substring(7);

            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();

            if (variables.getAcceptableFormat()) {

                ArrayList<Double> paramValues = new ArrayList<>();
                for (String nums : variables.getInputs()) {
                    paramValues.add(Double.parseDouble(nums));
                }

                String description;
                String notes = "How **Tidecaller** works: \n" +
                    "• Zero hits = *Tap*\n" +
                    "• Two (or more) hits = *Full* = *\"Perfect\"* (A1 Talent)\n";

                Beidou parry;
                // User provides RES_multiplier and DEF_multiplier
                if (paramValues.size() == 7) {
                    parry = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4),
                        paramValues.get(5), paramValues.get(6));

                    description = "Assumptions: \n" +
                        "• `DEF_Mult = " + paramValues.get(6) + "` \n" +
                        "• `RES_Mult = " + paramValues.get(5) + "` \n";
                }
                // User provides only RES_multiplier
                else if (paramValues.size() == 6) {
                    parry = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4),
                        paramValues.get(5));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• `RES_Mult = " + paramValues.get(5) + "` \n";
                }
                // User provides no additional multipliers
                else {
                    parry = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• Enemy has `RES = 10%` → `RES_Mult = 0.9` \n";
                }
                String finalDescription = description + notes;

                int[] tap = parry.calculateParry(0);
                int[] perfect = parry.calculateParry(2);

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.VIVID_VIOLET)
                        .setTitle("Tidecaller Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Tidecaller")
                        .setDescription(finalDescription)
                        .addField("\u200B", "*Base damage:*\n" +
                            "*Critical hit damage:*\n" +
                            "*Average damage:*", true)   // Using Unicode "zero width space" as empty "String name" filler
                        .addField("*Tap*", tap[0] + "\n" + tap[1] + "\n" + tap[2], true)
                        .addField("*Full / Perfect*", perfect[0] + "\n" + perfect[1] + "\n" + perfect[2], true)
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Tidecaller Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Tidecaller")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!ult command - calculates expected Beidou Stormcaller burst damage
        commands.put("ult", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 5);
            String parameters = input.substring(5);

            GenshinCommands variables = new GenshinCommands(command, parameters);
            variables.checkFormat();
            if (variables.getAcceptableFormat()) {

                ArrayList<Double> paramValues = new ArrayList<>();
                for (String nums : variables.getInputs()) {
                    paramValues.add(Double.parseDouble(nums));
                }

                String description;
                String notes = "How **Stormbreaker** works: \n" +
                    "• Damage calculated *per* discharge \n" +
                    "• Multiply by number of jumps and discharges for total damage";

                Beidou burst;
                // User provides RES_multiplier and DEF_multiplier
                if (paramValues.size() == 7) {
                    burst = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4),
                        paramValues.get(5), paramValues.get(6));

                    description = "Assumptions: \n" +
                        "• `DEF_Mult = " + paramValues.get(6) + "` \n" +
                        "• `RES_Mult = " + paramValues.get(5) + "` \n";
                }
                // User provides only RES_multiplier
                else if (paramValues.size() == 6) {
                    burst = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4),
                        paramValues.get(5));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• `RES_Mult = " + paramValues.get(5) + "` \n";
                }
                // User provides no additional multipliers
                else {
                    burst = new Beidou(paramValues.get(0), paramValues.get(1),
                        paramValues.get(2), paramValues.get(3), paramValues.get(4));

                    description = "Assumptions: \n" +
                        "• Character level = Enemy level → `DEF_Mult = 0.5` \n" +
                        "• Enemy has `RES = 10%` → `RES_Mult = 0.9` \n";
                }
                String finalDescription = description + notes;

                int[] ult = burst.calculateUlt();

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.VIVID_VIOLET)
                        .setTitle("Stormbreaker Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Stormbreaker")
                        .setDescription(finalDescription)
                        .addField("\u200B", "*Base damage:*\n" +
                            "*Critical hit damage:*\n" +
                            "*Average damage:*", true)   // Using Unicode "zero width space" as empty "String name" filler
                        .addField("*Lightning Damage*", ult[0] + "\n" + ult[1] + "\n" + ult[2], true)
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Stormbreaker Damage")
                        .setDescription(variables.getMessage())
                        .setFooter("\"p!help\" for more commands, bot by DaPhatWan#5333", "")
                    ).block();
            }
        });
    }
}
