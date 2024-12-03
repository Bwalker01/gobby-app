package com.byteryse;

import java.util.List;

import com.byteryse.Database.CampaignDAO;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter {
	private CampaignDAO campaignDAO;

	public EventListeners() {
		this.campaignDAO = new CampaignDAO();
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		List<Role> playerRoles = event.getMember().getRoles();
		playerRoles = playerRoles.stream().filter(role -> campaignDAO.getCampaignByRole(role.getId()) != null).toList();

		if (!playerRoles.isEmpty()) {
			PlayerManagement.MakePlayerActive(event);
		}
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		List<Role> playerRoles = event.getMember().getRoles();
		playerRoles = playerRoles.stream().filter(role -> campaignDAO.getCampaignByRole(role.getId()) != null).toList();

		if (playerRoles.isEmpty()) {
			PlayerManagement.MakePlayerInactive(event);
		}
	}
}
