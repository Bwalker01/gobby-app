package com.byteryse;

import java.util.List;

import com.byteryse.DTOs.Campaign;
import com.byteryse.Database.CampaignDAO;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter {
	private CampaignDAO campaignDAO;

	public EventListeners() {
		this.campaignDAO = new CampaignDAO();
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		List<Role> newRoles = event.getRoles();
		List<Campaign> campaigns = newRoles.stream().map(role -> campaignDAO.getCampaignByRole(role.getId())).toList();

		if (campaigns.isEmpty()) {
			return;
		}

		PlayerManagement.MakePlayerActive(event);
	}
}
