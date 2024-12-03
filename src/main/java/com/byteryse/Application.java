package com.byteryse;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Application {

	public static void main(String[] args) throws Exception {
		JDA api = JDABuilder
				.createDefault(System.getenv("DISCORD_TOKEN"))
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.build();
		api.addEventListener(new InteractionRouting());
		api.addEventListener(new EventListeners());
	}
}
