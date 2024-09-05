package com.byteryse.Templates;

import com.byteryse.DTOs.Campaign;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Basic Templates for Bot Embeds
 */
public class EmbedTemplates {
	public static MessageEmbed newCampaignAnnouncement(ModalInteractionEvent event, String campaignName,
			String campaignDescription) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaignName))
				.appendDescription(campaignDescription);

		return newEmbed.build();
	}

	public static MessageEmbed openCampaignAnnouncement(ButtonInteractionEvent event, Campaign campaign) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaign.getCampaign_name()))
				.appendDescription("*Now accepting join submissions.*");

		return newEmbed.build();
	}

	public static MessageEmbed closeCampaignAnnouncement(ButtonInteractionEvent event, Campaign campaign) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaign.getCampaign_name()))
				.appendDescription("*Submissions have been closed.*");

		return newEmbed.build();
	}
}
