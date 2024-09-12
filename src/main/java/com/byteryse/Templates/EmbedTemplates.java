package com.byteryse.Templates;

import com.byteryse.DTOs.Campaign;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Basic Templates for Bot Embeds
 */
public class EmbedTemplates {
	/**
	 * Embed included in new campaign announcement message.
	 * Contains Campaign Name, Description and DM.
	 * 
	 * @param event               - Modal Event
	 * @param campaignName
	 * @param campaignDescription
	 * @return MessageEmbed
	 */
	public static MessageEmbed newCampaignAnnouncement(ModalInteractionEvent event, String campaignName,
			String campaignDescription) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaignName))
				.appendDescription(campaignDescription);

		return newEmbed.build();
	}

	/**
	 * Embed included in campaign opening announcement.
	 * Contains Campaign Name, Status, DM.
	 * 
	 * @param event    - Button Event
	 * @param campaign
	 * @return MessageEmbed
	 */
	public static MessageEmbed openCampaignAnnouncement(ButtonInteractionEvent event, Campaign campaign) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaign.getCampaign_name()))
				.appendDescription("*Now accepting join submissions.*");

		return newEmbed.build();
	}

	/**
	 * Embed included in campaign closing announcement.
	 * Contains Campaign Name, Status, DM.
	 * 
	 * @param event    - Button Event
	 * @param campaign
	 * @return MessageEmbed
	 */
	public static MessageEmbed closeCampaignAnnouncement(ButtonInteractionEvent event, Campaign campaign) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setFooter("DM: " + event.getUser().getEffectiveName(), event.getUser().getAvatarUrl())
				.setTitle(String.format("**%s**", campaign.getCampaign_name()))
				.appendDescription("*Submissions have been closed.*");

		return newEmbed.build();
	}

	/**
	 * Embed used for join requests in Applications thread channel.
	 * Contains Applicant Name, Provided Info
	 * 
	 * @param event - Modal Event
	 * @return MessageEmbed
	 */
	public static MessageEmbed joinRequest(ModalInteractionEvent event, String text) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setTitle(event.getUser().getEffectiveName())
				.setDescription(text);

		return newEmbed.build();
	}

	/**
	 * Embed that replaces the join request to indicate an accepted request.
	 * Contains Applicant Name, Provided Info, Request Status
	 * 
	 * @param event       - Button Event
	 * @param title       - Original Request Title
	 * @param description - Original Request Description
	 * @return
	 */
	public static MessageEmbed joinAccept(ButtonInteractionEvent event, String title, String description) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setColor(Color.GREEN)
				.setTitle(title)
				.setDescription(description)
				.setFooter("Accepted");

		return newEmbed.build();
	}

	/**
	 * Embed that replaces the join request to indicate a rejected request.
	 * Contains Applicant Name, Provided Info, Request Status
	 * 
	 * @param event       - Button Event
	 * @param title       - Original Request Title
	 * @param description - Original Request Description
	 * @return
	 */
	public static MessageEmbed joinReject(ButtonInteractionEvent event, String title, String description) {
		EmbedBuilder newEmbed = new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle(title)
				.setDescription(description)
				.setFooter("Rejected");

		return newEmbed.build();
	}
}
