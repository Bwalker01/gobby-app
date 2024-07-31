package com.byteryse;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import com.byteryse.DAO.CampaignDAO;
import com.byteryse.DTO.Campaign;
import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class SlashCommands extends ListenerAdapter {
	private CampaignDAO campaignDAO;

	private static final String CAMPAIGN_FORUM = "1263135869645094993";
	private static final String GAME_ANNOUNCEMENTS = "1252710143838261399";

	public SlashCommands(DatabaseController dbCon) {
		this.campaignDAO = new CampaignDAO(dbCon);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String command = event.getName();

		switch (command) {
			case "create-campaign":
				CampaignManagement.campaignCreationModal(event);
				break;
			default:
				event.reply("Something went wrong.");
				break;
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (event.getModalId().equals("create-campaign")) {
			CampaignManagement.createCampaign(event, campaignDAO);
		} else if (event.getModalId().substring(0, 13).equals("join-request-")) {
			TextChannel dmScreen = event.getGuild().getCategoryById(event.getModalId().substring(13))
					.getTextChannels()
					.stream().filter(channel -> channel.getName().equals("dm-screen")).toList().get(0);
			dmScreen.getHistoryFromBeginning(1).queue(history -> {
				ThreadChannel applicationThreads = history.getRetrievedHistory().get(0).getStartedThread();
				applicationThreads.sendMessage(
						new MessageCreateBuilder()
								.setContent(
										String.format("**%s** has applied to join:", event.getUser().getAsMention()))
								.addEmbeds(
										new EmbedBuilder().setTitle(event.getUser().getEffectiveName())
												.setDescription(event.getValue("request-text").getAsString()).build())
								.addActionRow(Button.success("join-accept-" + event.getUser().getId(), "Accept"),
										Button.danger("join-reject-" + event.getUser().getId(), "Reject"))
								.build())
						.queue();
				event.reply(event.getUser().getAsMention() + " has requested to join!").queue();
			});
		} else if (event.getModalId().equals("new-campaign-name")) {
			event.deferEdit().queue();
			Category category = event.getChannel().asTextChannel()
					.getParentCategory();
			String newName = event.getValue("rename-text").getAsString();
			String oldName = category.getName();
			category.getManager().setName(newName).queue();
			Campaign campaign = campaignDAO.getCampaignByCategory(category.getId());
			ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
			ThreadChannel post = forum.getThreadChannels().stream()
					.filter(thisPost -> thisPost.getId().equals(campaign.getPost_id()))
					.toList().get(0);
			post.getManager().setName(newName).queue();
			post.sendMessage(String.format("***%s** has changed the campaign name from **%s** to **%s**.*",
					event.getUser().getAsMention(), oldName, newName)).queue();
			campaign.setName(newName);
			campaignDAO.updateCampaign(campaign);
		} else if (event.getModalId().substring(0, 16).equals("campaign-delete-")) {
			event.deferEdit().queue();
			Campaign campaignToDelete = campaignDAO.getCampaignByCategory(event.getModalId().substring(16));
			if (event.getValue("campaign-delete-name").getAsString().equals(campaignToDelete.getName())) {
				Category categoryToDelete = event.getGuild().getCategoryById(campaignToDelete.getCategory_id());
				for (GuildChannel channel : categoryToDelete.getChannels()) {
					channel.delete().queue();
				}
				categoryToDelete.delete().queue();
				ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
				ThreadChannel post = forum.getThreadChannels().stream()
						.filter(thisPost -> thisPost.getId().equals(campaignToDelete.getPost_id()))
						.toList().get(0);
				post.getManager().setAppliedTags(forum.getAvailableTagsByName("ARCHIVED", true)).queue();
				List<Button> buttons = post.retrieveStartMessage().complete().getButtons();
				post.editMessageComponentsById(campaignToDelete.getPost_id(), ActionRow.of(buttons.get(0).asDisabled()))
						.queue();
				post.getManager().setLocked(true).queue();
				event.getGuild().getRoleById(campaignToDelete.getRole_id()).delete().queue();
				campaignDAO.deleteCampaign(campaignToDelete);
			}
			return;
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		String buttonId = event.getComponentId();

		switch (buttonId) {
			case "open-close":
				event.deferEdit().queue();
				Category campaignCategory = event.getChannel().asTextChannel().getParentCategory();
				Campaign campaign = campaignDAO.getCampaignByCategory(campaignCategory.getId());

				if (event.getComponent().getLabel().equals("Open")) {
					List<ItemComponent> row = event.getMessage().getActionRows().get(0).getComponents();
					row.set(0, Button.danger("open-close", "Close"));
					event.getMessage().editMessageComponents().setActionRow(row).queue();
					campaign.open();
					ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
					ThreadChannel post = forum.getThreadChannels().stream()
							.filter(thisPost -> thisPost.getId().equals(campaign.getPost_id()))
							.toList().get(0);
					post.getManager()
							.setAppliedTags(forum.getAvailableTagsByName("OPEN", true)).queue();
					List<Button> buttons = post.retrieveStartMessage().complete().getButtons();
					post.editMessageComponentsById(campaign.getPost_id(), ActionRow.of(buttons.get(0).asEnabled()))
							.queue();
					post.sendMessage(
							"======================================\n:green_circle:  Join submissions have been opened  :green_circle:\n======================================")
							.queue();
					event.getMessage().createThreadChannel("Join Applications").complete();
					event.getChannel().getHistoryFromBeginning(50).queue(
							history -> history.getRetrievedHistory().stream()
									.filter(message -> message.getContentRaw().equals("Join Applications"))
									.forEach(message -> message.delete().queue()));
					event.getGuild().getNewsChannelById(GAME_ANNOUNCEMENTS)
							.sendMessage(new MessageCreateBuilder()
									.addContent("**NEW UPDATE**").setEmbeds(
											new EmbedBuilder()
													.setFooter("DM: " + event
															.getUser()
															.getEffectiveName(),
															event.getUser().getAvatarUrl())
													.setTitle(String.format(
															"**%s**",
															campaign.getName()))
													.appendDescription("*Now accepting join submissions.*")
													.build())
									.addActionRow(
											Button.link(post.retrieveStartMessage().complete()
													.getJumpUrl(),
													"Go To Campaign"))
									.build())
							.queue();
					campaignDAO.updateCampaign(campaign);
				} else if (event.getComponent().getLabel().equals("Close")) {
					List<ItemComponent> row = event.getMessage().getActionRows().get(0).getComponents();
					row.set(0, Button.success("open-close", "Open"));
					event.getMessage().editMessageComponents().setActionRow(row).queue();
					campaign.close();
					ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
					ThreadChannel post = forum.getThreadChannels().stream()
							.filter(thisPost -> thisPost.getId().equals(campaign.getPost_id()))
							.toList().get(0);
					post.getManager()
							.setAppliedTags(forum.getAvailableTagsByName("CLOSED", true)).queue();
					List<Button> buttons = post.retrieveStartMessage().complete().getButtons();
					post.editMessageComponentsById(campaign.getPost_id(), ActionRow.of(buttons.get(0).asDisabled()))
							.queue();
					post.sendMessage(
							"======================================\n:red_circle:  Join submissions have been closed  :red_circle:\n======================================")
							.queue();
					event.getMessage().getStartedThread().delete().queue();
					event.getGuild().getNewsChannelById(GAME_ANNOUNCEMENTS)
							.sendMessage(new MessageCreateBuilder()
									.addContent("**NEW UPDATE**").setEmbeds(
											new EmbedBuilder()
													.setFooter("DM: " + event
															.getUser()
															.getEffectiveName(),
															event.getUser().getAvatarUrl())
													.setTitle(String.format(
															"**%s**",
															campaign.getName()))
													.appendDescription("*Submissions now closed.*")
													.build())
									.addActionRow(
											Button.link(post.retrieveStartMessage().complete()
													.getJumpUrl(),
													"Go To Campaign"))
									.build())
							.queue();
					campaignDAO.updateCampaign(campaign);
				}
				return;
			case "join-campaign":
				Campaign thisCampaign = campaignDAO.getCampaignByPost(event.getMessageId());
				if (thisCampaign.isOpen()) {
					TextChannel dmScreen = event.getGuild().getCategoryById(thisCampaign.getCategory_id())
							.getTextChannels()
							.stream().filter(channel -> channel.getName().equals("dm-screen")).toList().get(0);
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
					if (usersApplications.size() == 0 && !event.getMember().getRoles()
							.contains(event.getGuild().getRoleById(thisCampaign.getRole_id()))) {
						TextInput subject = TextInput
								.create("request-text", "Your Experience, Preferences, etc",
										TextInputStyle.PARAGRAPH)
								.setMaxLength(500).setRequired(false).build();
						Modal modal = Modal
								.create("join-request-" + thisCampaign.getCategory_id(), "Tell The DM A Bit About You")
								.addComponents(ActionRow.of(subject)).build();
						event.replyModal(modal).queue();
					} else {
						event.reply("You're already applied!").setEphemeral(true).queue();
					}
				} else {
					event.reply("This campaign is closed.").setEphemeral(true).queue();
				}
				return;
			case "campaign-settings":
				event.deferEdit().queue();
				ArrayList<Button> buttons = new ArrayList<Button>(
						event.getMessage().getActionRows().get(0).getButtons());
				buttons.set(3, Button.success("close-settings", "Close Settings"));
				event.getHook()
						.editMessageComponentsById(event.getMessageId(), ActionRow.of(buttons),
								ActionRow.of(
										Button.secondary("rename-campaign", "Rename Campaign"),
										Button.primary("kick-player", "Kick Player"),
										Button.danger("archive-campaign", "End Campaign") // TODO
								))
						.queue();
				return;
			case "close-settings":
				event.deferEdit().queue();
				ArrayList<Button> oldButtons = new ArrayList<Button>(
						event.getMessage().getActionRows().get(0).getButtons());
				oldButtons.set(3, Button.danger("campaign-settings", "Settings"));
				event.getHook().editMessageComponentsById(event.getMessageId(), ActionRow.of(oldButtons)).queue();
				return;
			case "dismiss-request":
				event.deferEdit().queue();
				event.getMessage().delete().queue();
				return;
			case "rename-campaign":
				TextInput newNameBox = TextInput
						.create("rename-text", "New Campaign Name",
								TextInputStyle.SHORT)
						.setMinLength(3).setMaxLength(25).setRequired(true).build();
				Modal modal = Modal
						.create("new-campaign-name", "Rename Campaign")
						.addComponents(ActionRow.of(newNameBox)).build();
				event.replyModal(modal).queue();
				return;
			case "kick-player":
				Campaign kickFromCampaign = campaignDAO
						.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
				ArrayList<ActionRow> players = new ArrayList<>();
				event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(kickFromCampaign.getRole_id()))
						.forEach(member -> players.add(ActionRow.of(Button.danger("remove-player-" + member.getId(),
								"Kick " + member.getUser().getEffectiveName()))));
				if (players.isEmpty()) {
					event.reply("There are no players in your campaign yet!").setEphemeral(true).queue();
				} else {
					event.reply("").addComponents(players).setEphemeral(true).queue();
				}
				return;
			case "archive-campaign":
				Campaign deleteCampaign = campaignDAO
						.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
				TextInput nameBox = TextInput
						.create("campaign-delete-name", "Enter the name of the campaign to confirm:",
								TextInputStyle.SHORT)
						.setRequired(true).setPlaceholder(deleteCampaign.getName()).build();
				Modal deleteModal = Modal
						.create("campaign-delete-" + deleteCampaign.getCategory_id(),
								String.format("Delete %s?", deleteCampaign.getName()))
						.addComponents(ActionRow.of(nameBox)).build();
				event.replyModal(deleteModal).queue();
				return;
			default:
				try {
					if (buttonId.substring(0, 12).equals("join-accept-")) {
						event.deferEdit().queue();
						Campaign joiningCampaign = campaignDAO
								.getCampaignByCategory(
										event.getChannel().asThreadChannel().getParentChannel().asTextChannel()
												.getParentCategory().getId());
						Member user = event.getGuild().retrieveMemberById(buttonId.substring(12)).complete();
						event.getGuild()
								.addRoleToMember(user, event.getGuild().getRoleById(joiningCampaign.getRole_id()))
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
												joiningCampaign.getName()))
								.queue();
						return;
					}

					if (buttonId.substring(0, 12).equals("join-reject-")) {
						event.deferEdit().queue();
						event.getMessage()
								.editMessageComponents(ActionRow.of(Button.secondary("dismiss-request", "Dismiss")))
								.queue();
						MessageEmbed rejectOldEmbed = event.getMessage().getEmbeds().get(0);
						EmbedBuilder rejectNewEmbed = new EmbedBuilder().setColor(Color.RED)
								.setTitle(rejectOldEmbed.getTitle()).setDescription(rejectOldEmbed.getDescription())
								.setFooter("Rejected");
						event.getMessage().editMessageEmbeds(rejectNewEmbed.build()).queue();
						return;
					}

					if (buttonId.substring(0, 14).equals("remove-player-")) {
						Campaign campaign2 = campaignDAO
								.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
						event.deferEdit().queue();
						event.getGuild()
								.removeRoleFromMember(
										event.getGuild().retrieveMemberById(buttonId.substring(14)).complete(),
										event.getGuild().getRoleById(campaign2.getRole_id()))
								.queue();
						List<ActionRow> rows = event.getMessage().getActionRows().stream()
								.map(row -> row.getButtons().get(0).equals(event.getButton()) ? row.asDisabled() : row)
								.toList();
						event.getMessage().editMessageComponents(rows).queue();
						return;
					}
					event.reply("Something went wrong.").setEphemeral(true).queue();
					return;
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
					System.out.println("Button ID: " + buttonId);
				}
		}
	}
}
