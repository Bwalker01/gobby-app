package com.byteryse;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.byteryse.DTOs.Campaign;
import com.byteryse.Database.CampaignDAO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class PlayerManagement {

	public static void JoinRequestModal(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Campaign campaign = campaignDAO.getCampaignByPost(event.getMessageId());
		if (campaign.isOpen()) {
			TextChannel dmScreen = getDmScreen(event, campaign.getCategory_id());
			ThreadChannel applicationThread = dmScreen.getHistoryFromBeginning(1).complete()
					.getRetrievedHistory().get(0).getStartedThread();
			List<Message> applications = applicationThread.getHistoryFromBeginning(100).complete()
					.getRetrievedHistory();
			ArrayList<Message> usersApplications = new ArrayList<>();
			for (Message message : applications) {
				if (message.getMentions().getUsers().contains(event.getUser())) {
					usersApplications.add(message);
				}
			}
			if (event.getMember().getRoles()
					.contains(event.getGuild().getRoleById(campaign.getRole_id()))) {
				event.reply("You're already in this campaign!").setEphemeral(true).queue();
				return;
			}
			if (event.getMember().getRoles().contains(event.getGuild().getRoleById(campaign.getDm_role_id()))) {
				event.reply("You're DMing this campaign!").setEphemeral(true).queue();
				return;
			}

			if (usersApplications.size() == 0) {
				TextInput subject = TextInput
						.create("request-text", "Your Experience, Preferences, etc",
								TextInputStyle.PARAGRAPH)
						.setMaxLength(500).setRequired(false).build();
				Modal modal = Modal
						.create("join-request:" + campaign.getCategory_id(), "Tell The DM A Bit About You")
						.addComponents(ActionRow.of(subject)).build();
				event.replyModal(modal).queue();
			} else {
				event.reply("You're already applied!").setEphemeral(true).queue();
			}
		} else {
			event.reply("This campaign is closed.").setEphemeral(true).queue();
		}
	}

	public static void SendJoinRequest(ModalInteractionEvent event) {
		TextChannel dmScreen = getDmScreen(event, event.getModalId().substring(13));
		dmScreen.getHistoryFromBeginning(1).queue(history -> {
			ThreadChannel applicationThreads = history.getRetrievedHistory().get(0).getStartedThread();
			applicationThreads.sendMessage(
					new MessageCreateBuilder()
							.setContent(
									String.format("**%s** has applied to join:", event.getUser().getAsMention()))
							.addEmbeds(
									new EmbedBuilder().setTitle(event.getUser().getEffectiveName())
											.setDescription(event.getValue("request-text").getAsString()).build())
							.addActionRow(Button.success("join-accept:" + event.getUser().getId(), "Accept"),
									Button.danger("join-reject:" + event.getUser().getId(), "Reject"))
							.build())
					.queue();
			event.getHook().sendMessage(event.getUser().getAsMention() + " has requested to join!").queue();
		});
	}

	public static void KickPlayerOptions(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Campaign campaign = campaignDAO
				.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
		ArrayList<ActionRow> players = new ArrayList<>();
		event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(campaign.getRole_id()))
				.forEach(member -> players.add(ActionRow.of(Button.danger("remove-player:" + member.getId(),
						"Kick " + member.getUser().getEffectiveName()))));
		if (players.isEmpty()) {
			event.getHook().sendMessage("There are no players in your campaign yet!").setEphemeral(true).queue();
		} else {
			event.getHook().sendMessage("").addComponents(players).setEphemeral(true).queue();
		}
	}

	public static void KickPlayer(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Campaign campaign = campaignDAO
				.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
		event.getGuild().removeRoleFromMember(event.getGuild().getMemberById(event.getButton().getId().substring(14)),
				event.getGuild().getRoleById(campaign.getRole_id())).queue();
		List<ActionRow> rows = event.getMessage().getActionRows().stream()
				.map(row -> row.getButtons().get(0).equals(event.getButton()) ? row.asDisabled() : row)
				.toList();
		event.getMessage().editMessageComponents(rows).queue();
	}

	public static void AcceptPlayer(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		String[] eventArgs = event.getComponentId().split(":");
		Campaign campaign = campaignDAO
				.getCampaignByCategory(
						event.getChannel().asThreadChannel().getParentChannel().asTextChannel()
								.getParentCategory().getId());
		Member user = event.getGuild().retrieveMemberById(eventArgs[1]).complete();
		event.getGuild()
				.addRoleToMember(user, event.getGuild().getRoleById(campaign.getRole_id()))
				.queue();
		event.getMessage()
				.editMessageComponents(ActionRow.of(Button.secondary("dismiss-request", "Dismiss")))
				.queue();
		MessageEmbed acceptOldEmbed = event.getMessage().getEmbeds().get(0);
		EmbedBuilder acceptNewEmbed = new EmbedBuilder().setColor(Color.GREEN)
				.setTitle(acceptOldEmbed.getTitle()).setDescription(acceptOldEmbed.getDescription())
				.setFooter("Accepted");
		event.getMessage().editMessageEmbeds(acceptNewEmbed.build()).queue();
		user.getUser().openPrivateChannel().complete()
				.sendMessage(
						String.format(
								"You have been accepted into the campaign **%s**. You'll now be able to find it in the server categories!",
								campaign.getName()))
				.queue();
	}

	public static void RejectPlayer(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		event.getMessage()
				.editMessageComponents(ActionRow.of(Button.secondary("dismiss-request", "Dismiss")))
				.queue();
		MessageEmbed rejectOldEmbed = event.getMessage().getEmbeds().get(0);
		EmbedBuilder rejectNewEmbed = new EmbedBuilder().setColor(Color.RED)
				.setTitle(rejectOldEmbed.getTitle()).setDescription(rejectOldEmbed.getDescription())
				.setFooter("Rejected");
		event.getMessage().editMessageEmbeds(rejectNewEmbed.build()).queue();
	}

	private static TextChannel getDmScreen(GenericInteractionCreateEvent event, String categoryId) {
		Category campaignCategory = event.getGuild().getCategoryById(categoryId);
		TextChannel dmScreen = campaignCategory
				.getTextChannels()
				.stream().filter(channel -> channel.getName().equals("dm-screen")).toList().get(0);
		return dmScreen;
	}
}
