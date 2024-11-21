package com.byteryse;

import javax.annotation.Nonnull;

import com.byteryse.Database.CampaignDAO;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionRouting extends ListenerAdapter {
	private CampaignDAO campaignDAO;

	public InteractionRouting() {
		this.campaignDAO = new CampaignDAO();
	}

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
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
	public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
		event.deferEdit().queue();
		String[] modalArgs = event.getModalId().split(":");
		String modalId = modalArgs[0];
		switch (modalId) {
			case "create-campaign":
				CampaignManagement.CreateCampaign(event, campaignDAO);
				return;
			case "join-request":
				PlayerManagement.SendJoinRequest(event);
				return;
			case "new-campaign-name":
				CampaignManagement.RenameCampaign(event, campaignDAO);
				return;
			case "campaign-delete":
				CampaignManagement.DeleteCampaign(event, campaignDAO);
				return;
			default:
				event.getHook().sendMessage("Something went wrong.").setEphemeral(true).queue();
				System.out.println("Error; button not found\nModal ID: " + modalId);
		}
	}

	@Override
	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		String[] interactionArgs = event.getComponentId().split(":");
		String buttonId = interactionArgs[0];
		switch (buttonId) {
			case "open-close":
				event.deferEdit().queue();
				CampaignManagement.OpenCampaign(event, campaignDAO);
				return;
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
			case "schedule":
				CampaignManagement.ScheduleSessionDateSelect(event, campaignDAO);
				return;
			default:
				event.reply("Something went wrong.").setEphemeral(true).queue();
				System.out.println("Error; button not found\nButton ID: " + buttonId);
				return;
		}
	}

	@Override
	public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
		String[] interactionArgs = event.getComponentId().split(":");
		String menuId = interactionArgs[0];
		switch (menuId) {
			case "schedule-session":
				CampaignManagement.ScheduleSessionTimeModal(event, campaignDAO);
				return;
		}
	}
}
