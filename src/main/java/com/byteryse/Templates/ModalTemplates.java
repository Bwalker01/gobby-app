package com.byteryse.Templates;

import com.byteryse.DTOs.Campaign;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

/**
 * Basic Templates for Bot Modals
 */
public class ModalTemplates {
	/**
	 * Modal allowing for input of data required in the creation of a campaign -
	 * name, description and requirements.
	 * 
	 * @return 3-ActionRow modal.
	 */
	public static Modal createCampaign() {

		/*
		 * Campaign Name - required
		 * Standard for referring to campaign across system.
		 * Used to name the Category, Roles and Post associated with the campaign.
		 * Saved in the database.
		 */
		TextInput name = TextInput
				.create("name", "Name", TextInputStyle.SHORT)
				.setPlaceholder("The name of your campaign")
				.setMinLength(3)
				.setMaxLength(25)
				.setRequired(true)
				.build();

		/*
		 * Campaign Description - optional
		 * Quick synopsis of the campaign.
		 * Only shown within the initial announcement and the forum post description.
		 * Not saved elsewhere.
		 */
		TextInput description = TextInput
				.create("description", "Description", TextInputStyle.PARAGRAPH)
				.setPlaceholder("A short(ish) description.")
				.setMinLength(1)
				.setMaxLength(500)
				.build();

		/*
		 * Campaign Requirements - optional
		 * Difficulty, scheduling and theming awareness.
		 * Only shown within the forum post description.
		 * Not saved elsewhere.
		 */
		TextInput tags = TextInput
				.create("tags", "Player Requirements", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Example:\n- New/Veteran/All Players\n- Serious/Novelty Campaign\n- RP/Combat Heavy")
				.setMinLength(1)
				.setMaxLength(100)
				.build();

		Modal modal = Modal
				.create("create-campaign", "Create Campaign")
				.addComponents(ActionRow.of(name), ActionRow.of(description), ActionRow.of(tags))
				.build();

		return modal;
	}

	/**
	 * Modal allowing for the renaming of campaigns, only changing the basic name.
	 * Changes the name across the database, category, role and post.
	 * 
	 * @return 1-ActionRow modal
	 */
	public static Modal renameCampaign() {
		/*
		 * New Campaign name - required
		 * The new name for the campaign to be renamed to. Will be checked across the
		 * roles and categories in this server to prevent overlap with any of them.
		 */
		TextInput newName = TextInput
				.create("rename-text", "New Campaign Name", TextInputStyle.SHORT)
				.setMinLength(3).setMaxLength(25).setRequired(true)
				.build();

		Modal modal = Modal
				.create("new-campaign-name", "Rename Campaign")
				.addComponents(ActionRow.of(newName))
				.build();

		return modal;
	}

	/**
	 * Confirmation modal before deleting a campaign. Requires the name of the
	 * campaign be typed in exactly for authentication.
	 * 
	 * @return 1-ActionRow modal
	 */
	public static Modal deleteCampaign(Campaign campaign) {
		/*
		 * Campaign's name - required
		 * Last verification step before deleting to confirm that the deletion is
		 * intentional and that the user is deleting the right one.
		 */
		TextInput name = TextInput
				.create("campaign-delete-name", "Enter the name of the campaign to confirm:", TextInputStyle.SHORT)
				.setRequired(true).setPlaceholder(campaign.getCampaign_name())
				.build();

		Modal modal = Modal
				.create("campaign-delete:" + campaign.getCategory_id(),
						String.format("Delete %s?", campaign.getCampaign_name()))
				.addComponents(ActionRow.of(name))
				.build();

		return modal;
	}

	/**
	 * Info modal for players to give their prior experience in other campaigns
	 * and/or their own expectations for the campaign they're joining to help the DM
	 * to decide on which players are the best fit.
	 * 
	 * @return 1-ActionRow modal
	 */
	public static Modal joinCampaign(Campaign campaign) {
		/**
		 * Player input - optional
		 * Allows the player to enter how experienced they are with DnD and what they
		 * might be looking for in the campaign they're entering.
		 */
		TextInput subject = TextInput
				.create("request-text", "Any Experience and/or Expectations?", TextInputStyle.PARAGRAPH)
				.setMaxLength(500).setRequired(false)
				.build();

		Modal modal = Modal
				.create("join-request:" + campaign.getCategory_id(), "Tell the DM a bit about you")
				.addComponents(ActionRow.of(subject))
				.build();

		return modal;
	}
}
