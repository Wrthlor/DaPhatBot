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
import java.util.HashMap;
import java.util.Map;

public class DaPhatBot {

    public static void main(String[] args) {

        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
            .login()
            .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
            // subscribe is like block, in that it will *request* for action
            // to be done, but instead of blocking the thread, waiting for it
            // to finish, it will just execute the results asynchronously.
            .subscribe(event -> {
                // 3.1 Message.getContent() is a String
                final String content = event.getMessage().getContent();

                for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                    // We will be using ! as our "prefix" to any command in the system.
                    if (content.startsWith('!' + entry.getKey())) {
                        entry.getValue().execute(event);
                        break;
                    }
                }
            });

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
        commands.put("ping", event -> event.getMessage()
            .getChannel().block()
            // 0xe6e6fa = Lavender
            .createEmbed(spec -> spec.setColor(Color.of(0xE6E6FA))
                .setTitle("Ping Pong")
                .setDescription("Pong!")
                .setFooter("Bot by DaPhatWan#5333", "")
                .setTimestamp(Instant.now())
            ).block());

        // Testing ability to get caller's username
        commands.put("helloBot", event -> {
            User user = event.getMessage().getAuthor().get();
            String username = user.getUsername() + "#" + user.getDiscriminator();
            event.getMessage()
                .getChannel().block()
                .createMessage("What do you want @" + username + "?").block();
        });

        commands.put("damage", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(1, 7);
            String parameters = input.substring(7);

            DamageOperator checkInput = new DamageOperator(command, parameters);
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
                        .setDescription("Calculated results are for ***raw*** damage output. \n" +
                            "Enemy DEF/RES values and level differences are not taken into account. \n" +
                            "Click link to see in-depth calculations. ")
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

        commands.put("parry", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(1, 6);
            String parameters = input.substring(6);

            DamageOperator checkInput = new DamageOperator(command, parameters);
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
                        .setDescription("• Zero hits = *Tap*\n" +
                            "• Two (or more) hits = *Full* = *\"Perfect\"* (A1 Talent)")
                        .addField("\u200B", "*Base dmg:*\n*Crit dmg:*\n*Avg dmg:*", true)   // Using Unicode "zero width space" as empty "String name" filler
                        .addField("*Tap*", tap[0] + "\n" + tap[1] + "\n" + tap[2], true)
                        .addField("*Full*", perfect[0] + "\n" + perfect[1] + "\n" + perfect[2], true)
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

        commands.put("ult", event -> {
            String input = event.getMessage().getContent().trim();
            String command = input.substring(1, 4);
            String parameters = input.substring(4);

            DamageOperator checkInput = new DamageOperator(command, parameters);
            String status = checkInput.checkFormat();
            if (status.equals("Success")) {
                event.getMessage()
                    .getChannel().block()
                    .createMessage("Work in progress! Check back later!").block();
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
