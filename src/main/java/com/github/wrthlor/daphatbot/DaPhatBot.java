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
import java.time.Instant;
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
            String dpsMessages = "`p!damage` - Gets expected output damage, ignoring talents \n" +
                "⤷ Format: `p!damage ATK DMG% CRate CDmg [RES_Mult [DEF_Mult]]` \n"+
                "`p!calcRes` - Gets enemy resistance *multiplier* \n" +
                "⤷ Format: `p!calcRes Enemy_RES` \n"+
                "`p!calcDef` - Gets enemy defense *multiplier* \n" +
                "⤷ Format: `p!calcDef Char_lvl Enemy_lvl [DEF_Reduction]` ";
            String beidouMessages = "`p!parry` - Gets expected Tidecaller damage \n" +
                "⤷ Format: `p!parry ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` \n" +
                "`p!ult` - Gets expected Stormbreaker damage \n" +
                "⤷ Format: `p!ult ATK DMG% CRate CDmg Talent_lvl [RES_Mult [DEF_Mult]]` ";

            event.getMessage()
                .getChannel().block()
                // 0xe6e6fa = Lavender
                .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                    .setTitle("DPS calculator commands")
                    .addField("General DPS command", dpsMessages, false)
                    .addField("Beidou specific commands", beidouMessages, false)
                    .setFooter("Bot by DaPhatWan#5333", "")
                    .setTimestamp(Instant.now())
                ).block();
        });

        // p!damage command - calculates expected damage output (ignores talent)
        commands.put("damage", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 8);
            String parameters = input.substring(8);

            DiscordBotCommands checkInput = new DiscordBotCommands(command, parameters);
            String status = checkInput.checkFormat();
            if (status.equals("Success")) {

                GenshinDamageCalculator results = checkInput.getDamage();
                String base = String.valueOf(results.calculateBase());
                String crit = String.valueOf(results.calculateCritical());
                String avg = String.valueOf(results.calculateAverageDamage());

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Damage Calculator")
                        .setUrl("https://library.keqingmains.com/mechanics/combat/damage-formula")
                        .setDescription("Assumptions: \n" +
                            "• Character level = Enemy level → `DEF_multiplier = 50%` \n" +
                            "• Enemy has `RES = 10%` \n" +
                            "• Talents are **NOT** factored. Please check wiki/in-game to get Talent% multiplier \n" +
                            "• Click link for more details")
                        .addField("*Base damage* ", base, true)
                        .addField("*Critical hit damage* ", crit, true)
                        .addField("*Average damage output* ", avg, true)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Damage Calculator")
                        .setUrl("https://library.keqingmains.com/mechanics/combat/damage-formula")
                        .setDescription(status)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!calcRes command - calculates RES multiplier for damage equation
        commands.put("calcRes", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 9);
            String parameters = input.substring(9);

            // p!calcRes Enemy_RES
            DiscordBotCommands checkInput = new DiscordBotCommands(command, parameters);
            String status = checkInput.checkFormat();

            if (status.equals("Success")) {
                GenshinDamageCalculator resistance = new GenshinDamageCalculator();

                String paramValue = checkInput.getNumbers()[0];
                double output = resistance.calculateResistanceMultiplier(Double.parseDouble(paramValue));

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Resistance Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Resistance#RES_Percentage")
                        .setDescription("Enemy resistance: " + paramValue + "%\n" +
                            "\n**RES multiplier:** " + String.format("%.4f", output))
                        .setFooter("Bot by DaPhatWan#5333", ""))
                    .block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Resistance Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Resistance#RES_Percentage")
                        .setDescription(status)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }

        });

        // p!calcDef command - calculates DEF multiplier for damage equation
        commands.put("calcDef", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 9);
            String parameters = input.substring(9);

            // p!calcDef charLvl defLvl [defShred]
            DiscordBotCommands checkInput = new DiscordBotCommands(command, parameters);
            String status = checkInput.checkFormat();

            if (status.equals("Success")) {
                GenshinDamageCalculator defense = new GenshinDamageCalculator();

                ArrayList<Double> paramValues = new ArrayList<>();
                for (String nums : checkInput.getNumbers()) {
                    paramValues.add(Double.valueOf(nums));
                }

                double output;
                String extra = "";
                if (paramValues.size() == 3) {
                    output = defense.calculateDefenseMultiplier(paramValues.get(0), paramValues.get(1), paramValues.get(2));
                    extra = "\nDefense reduction: " + paramValues.get(2) + "%";
                }
                else {
                    output = defense.calculateDefenseMultiplier(paramValues.get(0), paramValues.get(1));
                    extra = "";
                }

                String description = "Character level: " + paramValues.get(0) +
                    "\nEnemy level: " + paramValues.get(1) + extra +
                    "\n\n**DEF multiplier:** " + String.format("%.4f", output);

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Defense Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Defense#Enemy_Defense")
                        .setDescription(description)
                        .setFooter("Bot by DaPhatWan#5333", ""))
                    .block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Defense Multiplier")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Defense#Enemy_Defense")
                        .setDescription(status)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!parry command - calculates expected Beidou Tidecaller skill damage
        commands.put("parry", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 7);
            String parameters = input.substring(7);

            DiscordBotCommands checkInput = new DiscordBotCommands(command, parameters);
            String status = checkInput.checkFormat();

            if (status.equals("Success")) {

                Beidou parry = checkInput.getBeidou();
                int[] tap = parry.calculateParry(0);
                int[] perfect = parry.calculateParry(2);

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.VIVID_VIOLET)
                        .setTitle("Tidecaller Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Tidecaller")
                        .setDescription("Assumptions: \n" +
                            "• Character level = Enemy level → `DEF_multiplier = 50%` \n" +
                            "• Enemy has `RES = 10%` \n" +
                            "How **Tidecaller** works: \n" +
                            "• Zero hits = *Tap*\n" +
                            "• Two (or more) hits = *Full* = *\"Perfect\"* (A1 Talent)")
                        .addField("\u200B", "*Base damage:*\n" +
                            "*Critical hit damage:*\n" +
                            "*Average damage:*", true)   // Using Unicode "zero width space" as empty "String name" filler
                        .addField("*Tap*", tap[0] + "\n" + tap[1] + "\n" + tap[2], true)
                        .addField("*Full / Perfect*", perfect[0] + "\n" + perfect[1] + "\n" + perfect[2], true)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Tidecaller Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Tidecaller")
                        .setDescription(status)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
        });

        // p!ult command - calculates expected Beidou Stormcaller burst damage
        commands.put("ult", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(0, 5);
            String parameters = input.substring(5);

            DiscordBotCommands checkInput = new DiscordBotCommands(command, parameters);
            String status = checkInput.checkFormat();
            if (status.equals("Success")) {
                Beidou parry = checkInput.getBeidou();
                int[] ult = parry.calculateUlt();

                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.VIVID_VIOLET)
                        .setTitle("Stormbreaker Damage")
                        .setUrl("https://genshin-impact.fandom.com/wiki/Stormbreaker")
                        .setDescription("Assumptions: \n" +
                            "• Character level = Enemy level → `DEF_multiplier = 50%` \n" +
                            "• Enemy has `RES = 10%` \n" +
                            "How **Stormbreaker** works: \n" +
                            "• Damage calculated *per* discharge \n" +
                            "• Multiply by number of jumps and discharges for total damage")
                        .addField("\u200B", "*Base damage:*\n" +
                            "*Critical hit damage:*\n" +
                            "*Average damage:*", true)   // Using Unicode "zero width space" as empty "String name" filler
                        .addField("*Lightning Damage*", ult[0] + "\n" + ult[1] + "\n" + ult[2], true)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
            else {
                event.getMessage()
                    .getChannel().block()
                    .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                        .setTitle("Stormbreaker Damage")
                        .setDescription(status)
                        .setFooter("Bot by DaPhatWan#5333", "")
                    ).block();
            }
        });
    }
}
