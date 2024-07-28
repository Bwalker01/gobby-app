package com.byteryse;

import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Application {

    public static void main(String[] args) throws Exception {
        JDA api = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).build();
        DatabaseController dbCon = new DatabaseController();
        api.addEventListener(new SlashCommands(dbCon));
        api.addEventListener(new EventListeners(dbCon));
    }
}
