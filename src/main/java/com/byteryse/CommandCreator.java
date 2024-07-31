package com.byteryse;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandCreator {
	public static void main(String[] args) throws Exception {
		JDA api = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).build();
		System.out.println("Activated for " + api.getSelfUser().getName());

		removeCommands(api);

		fullCommands(api);
	}

	private static void removeCommands(JDA api) {
		for (Command command : api.retrieveCommands().complete()) {
			api.deleteCommandById(command.getId());
		}
		System.out.println("Deleted Existing Commands");
	}

	private static void fullCommands(JDA api) {
		api.updateCommands().addCommands(
				Commands.slash("create-campaign", "Create new campaign.")
						.setDefaultPermissions(DefaultMemberPermissions.DISABLED));
		System.out.println("Added All Commands.");
	}
}
