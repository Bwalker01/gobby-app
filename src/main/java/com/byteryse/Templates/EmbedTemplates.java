package com.byteryse.Templates;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class EmbedTemplates {
	public static MessageEmbed newCampaignAnnouncement(ModalInteractionEvent event, String campaignName,
			String campaignDescription) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("By " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaignName))
				.appendDescription(campaignDescription);

		return newEmbed.build();
	}
}
