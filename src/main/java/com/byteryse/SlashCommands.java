package com.byteryse;

import com.byteryse.DAO.CampaignDAO;
import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommands extends ListenerAdapter {
	private CampaignDAO campaignDAO;

	public SlashCommands(DatabaseController dbCon) {
		this.campaignDAO = new CampaignDAO(dbCon);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String command = event.getName();
		switch (command) {
			case "create-campaign":
				CampaignManagement.CampaignCreationModal(event);
				break;
			default:
				event.reply("Something went wrong.").setEphemeral(true).queue();
				break;
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		event.deferEdit().queue();
		String modalId = event.getModalId();
		if (modalId.equals("create-campaign")) {
			CampaignManagement.CreateCampaign(event, campaignDAO);
		} else if (modalId.substring(0, 13).equals("join-request-")) {
			PlayerManagement.SendJoinRequest(event);
		} else if (modalId.equals("new-campaign-name")) {
			CampaignManagement.RenameCampaign(event, campaignDAO);
		} else if (modalId.substring(0, 16).equals("campaign-delete-")) {
			CampaignManagement.DeleteCampaign(event, campaignDAO);
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		String[] interactionArgs = event.getComponentId().split(":");
		String buttonId = interactionArgs[0];
		switch (buttonId) {
			case "open":
				event.deferEdit().queue();
				CampaignManagement.OpenCampaign(event, campaignDAO);
				return;
			case "close":
				event.deferEdit().queue();
				CampaignManagement.CloseCampaign(event, campaignDAO);
				return;
			case "join-campaign":
				PlayerManagement.JoinRequestModal(event, campaignDAO);
				return;
			case "campaign-settings":
				event.deferEdit().queue();
				CampaignManagement.OpenSettings(event);
				return;
			case "close-settings":
				event.deferEdit().queue();
				CampaignManagement.CloseSettings(event);
				return;
			case "dismiss-request":
				event.deferEdit().queue();
				event.getMessage().delete().queue();
				return;
			case "rename-campaign":
				CampaignManagement.RenameCampaignModal(event);
				return;
			case "kick-player":
				event.deferEdit().queue();
				PlayerManagement.KickPlayerOptions(event, campaignDAO);
				return;
			case "archive-campaign":
				CampaignManagement.DeleteCampaignModal(event, campaignDAO);
				return;
			case "join-accept":
				event.deferEdit().queue();
				PlayerManagement.AcceptPlayer(event, campaignDAO);
				return;
			case "join-reject":
				event.deferEdit().queue();
				PlayerManagement.RejectPlayer(event, campaignDAO);
				return;
			case "remove-player":
				event.deferEdit().queue();
				PlayerManagement.KickPlayer(event, campaignDAO);
				return;
			default:
				event.reply("Something went wrong.").setEphemeral(true).queue();
				System.out.println("Error; button not found\nButton ID: " + buttonId);
				return;
		}
	}
}
