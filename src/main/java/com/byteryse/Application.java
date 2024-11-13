package com.byteryse;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Application {

	public static void main(String[] args) throws Exception {
		JDA api = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).build();
		api.addEventListener(new InteractionRouting());
		api.addEventListener(new EventListeners());
	}
}
