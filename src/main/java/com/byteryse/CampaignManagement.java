package com.byteryse;

import java.util.ArrayList;
import java.util.List;

import com.byteryse.DAO.CampaignDAO;
import com.byteryse.DTO.Campaign;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class CampaignManagement {
	private static final String CAMPAIGN_FORUM = System.getenv("CAMPAIGN_FORUM");
	private static final String GAME_ANNOUNCEMENTS = System.getenv("ANNOUNCEMENT_CHANNEL");
	private static final String GAME_MASTER_ROLE = System.getenv("DM_ROLE");

	public static void CampaignCreationModal(SlashCommandInteractionEvent event) {
		if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(GAME_MASTER_ROLE))) {
			event.reply(
					"You must be a Game Master to create campaigns here. Consider applying to become one if you're interested!")
					.setEphemeral(true).queue();
			return;
		}

		TextInput name = TextInput.create("name", "Name", TextInputStyle.SHORT)
				.setPlaceholder("The name of your campaign")
				.setMinLength(3)
				.setMaxLength(25)
				.setRequired(true)
				.build();

		TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
				.setPlaceholder("A short(ish) description.")
				.setMinLength(1)
				.setMaxLength(500)
				.build();

		TextInput tags = TextInput.create("tags", "Player Requirements", TextInputStyle.PARAGRAPH)
				.setPlaceholder(
						"Example:\n- New/Veteran/All Players\n- Serious/Fun/Novelty Campaign\n- RP/Combat Heavy")
				.setMinLength(1)
				.setMaxLength(100)
				.build();

		Modal modal = Modal.create("create-campaign", "Create Campaign")
				.addComponents(ActionRow.of(name), ActionRow.of(description), ActionRow.of(tags))
				.build();

		event.replyModal(modal).queue();
	}

	public static void CreateCampaign(ModalInteractionEvent event, CampaignDAO campaignDAO) {
		if (event.getGuild().getRoles().stream().map(role -> role.getName().toLowerCase()).toList()
				.contains(event.getValue("name").getAsString().toLowerCase())) {
			event.getHook().sendMessage("Couldn't create campaign. A role already exists with that name.")
					.setEphemeral(true).queue();
			return;
		}

		if (event.getGuild().getCategories().stream().map(category -> category.getName().toLowerCase()).toList()
				.contains(event.getValue("name").getAsString().toLowerCase())) {
			event.getHook().sendMessage("Couldn't create campaign. A category already exists with that name.")
					.setEphemeral(true).queue();
			return;
		}

		try {
			String campaignName = event.getValue("name").getAsString();
			String campaignDescription = event.getValue("description").getAsString();
			String campaignTags = event.getValue("tags").getAsString();
			Member DM = event.getMember();

			Guild guild = event.getGuild();
			Role player = guild.createRole().setName(campaignName).complete();
			Role gameMaster = guild.createRole().setName(campaignName + " DM").complete();
			guild.addRoleToMember(DM, gameMaster).queue();
			Category category = guild.createCategory(campaignName).complete();
			category.upsertPermissionOverride(guild.getPublicRole()).setDenied(
					Permission.VIEW_CHANNEL.getRawValue()).complete();
			category.upsertPermissionOverride(player).setAllowed(
					Permission.VIEW_CHANNEL).complete();
			category.upsertPermissionOverride(gameMaster).setAllowed(
					Permission.VIEW_CHANNEL,
					Permission.VOICE_DEAF_OTHERS,
					Permission.VOICE_MUTE_OTHERS,
					Permission.MANAGE_CHANNEL,
					Permission.MESSAGE_MANAGE).complete();

			category.createVoiceChannel("Game Room").queue();
			category.createTextChannel("game-chat").queue();

			TextChannel updateChannel = category.createTextChannel("updates").complete();
			updateChannel.upsertPermissionOverride(player).setDenied(Permission.MESSAGE_SEND).complete();
			updateChannel.upsertPermissionOverride(gameMaster).setDenied(Permission.MANAGE_CHANNEL).complete();

			TextChannel controlChannel = category.createTextChannel("DM Screen").complete();
			controlChannel.upsertPermissionOverride(player).setDenied(Permission.VIEW_CHANNEL).complete();
			controlChannel.upsertPermissionOverride(gameMaster).setDenied(
					Permission.MESSAGE_SEND,
					Permission.MESSAGE_MANAGE,
					Permission.MANAGE_CHANNEL).complete();

			MessageCreateData forumPost = new MessageCreateBuilder()
					.addContent(String.format("%s\n\nExpectations:\n%s\n",
							campaignDescription, campaignTags))
					.addActionRow(Button.success("join-campaign", "Apply To Join").asDisabled())
					.build();
			ForumChannel forum = guild.getForumChannelById(CAMPAIGN_FORUM);
			ForumPost post = forum.createForumPost(campaignName, forumPost)
					.setTags(forum.getAvailableTagsByName("PREPARING", false)).complete();
			Campaign campaign = new Campaign(campaignName, player.getId(), category.getId(),
					post.getThreadChannel().getId(), gameMaster.getId());
			campaignDAO.createCampaign(campaign);
			event.getHook().sendMessage("Campaign Created Successfully.").setEphemeral(true).queue();
			guild.getNewsChannelById(GAME_ANNOUNCEMENTS)
					.sendMessage(new MessageCreateBuilder()
							.addContent("**NEW CAMPAIGN**").setEmbeds(
									new EmbedBuilder()
											.setFooter("By " + event
													.getUser()
													.getEffectiveName(),
													event.getUser().getAvatarUrl())
											.setTitle(String.format(
													"**%s**",
													campaignName))
											.appendDescription(
													campaignDescription)
											.build())
							.addActionRow(
									Button.link(post.getMessage()
											.getJumpUrl(),
											"Go To Campaign"))
							.build())
					.queue();
			TextChannel textChannel = (TextChannel) category.getChannels().stream()
					.filter(channel -> channel.getName().equals("dm-screen")).toList().get(0);
			textChannel.sendMessage(new MessageCreateBuilder().setContent("**DUNGEON MASTER CONTROLS**")
					.addActionRow(
							Button.success("open", "Open"),
							Button.primary("schedule", "Schedule Session"),
							Button.secondary("availability", "Check Availability"),
							Button.danger("campaign-settings", "Settings"))
					.build()).queue();
		} catch (Exception e) {
			System.out.println(e);
			event.getHook().sendMessage("Hmmm. Something went wrong.").setEphemeral(true).queue();
		}
	}

	public static void RenameCampaignModal(ButtonInteractionEvent event) {
		TextInput newNameBox = TextInput
				.create("rename-text", "New Campaign Name",
						TextInputStyle.SHORT)
				.setMinLength(3).setMaxLength(25).setRequired(true).build();
		Modal modal = Modal
				.create("new-campaign-name", "Rename Campaign")
				.addComponents(ActionRow.of(newNameBox)).build();
		event.replyModal(modal).queue();
	}

	public static void RenameCampaign(ModalInteractionEvent event, CampaignDAO campaignDAO) {
		if (event.getGuild().getCategories().stream().map(role -> role.getName().toLowerCase()).toList()
				.contains(event.getValue("rename-text").getAsString().toLowerCase())) {
			event.getHook().sendMessage("Couldn't rename campaign. A role already exists with that name.")
					.setEphemeral(true).queue();
			return;
		}

		if (event.getGuild().getCategories().stream().map(category -> category.getName().toLowerCase()).toList()
				.contains(event.getValue("rename-text").getAsString().toLowerCase())) {
			event.getHook().sendMessage("Couldn't rename campaign. A category already exists with that name.")
					.setEphemeral(true).queue();
			return;
		}

		Category category = event.getChannel().asTextChannel()
				.getParentCategory();
		String newName = event.getValue("rename-text").getAsString();
		String oldName = category.getName();
		category.getManager().setName(newName).queue();
		Campaign campaign = campaignDAO.getCampaignByCategory(category.getId());
		event.getGuild().getRoleById(campaign.getRole_id()).getManager().setName(newName).queue();
		event.getGuild().getRoleById(campaign.getDm_role_id()).getManager().setName(newName + " DM").queue();
		ThreadChannel post = getPost(event, campaign);
		post.getManager().setName(newName).queue();
		post.sendMessage(String.format("***%s** has changed the campaign name from **%s** to **%s**.*",
				event.getUser().getAsMention(), oldName, newName)).queue();
		campaign.setName(newName);
		campaignDAO.updateCampaign(campaign);
	}

	public static void DeleteCampaignModal(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Campaign campaign = campaignDAO
				.getCampaignByCategory(event.getChannel().asTextChannel().getParentCategoryId());
		TextInput nameBox = TextInput
				.create("campaign-delete-name", "Enter the name of the campaign to confirm:",
						TextInputStyle.SHORT)
				.setRequired(true).setPlaceholder(campaign.getName()).build();
		Modal deleteModal = Modal
				.create("campaign-delete:" + campaign.getCategory_id(),
						String.format("Delete %s?", campaign.getName()))
				.addComponents(ActionRow.of(nameBox)).build();
		event.replyModal(deleteModal).queue();
	}

	public static void DeleteCampaign(ModalInteractionEvent event, CampaignDAO campaignDAO) {
		Campaign campaign = campaignDAO.getCampaignByCategory(event.getModalId().substring(16));
		if (event.getValue("campaign-delete-name").getAsString().equals(campaign.getName())) {
			Category categoryToDelete = event.getGuild().getCategoryById(campaign.getCategory_id());
			for (GuildChannel channel : categoryToDelete.getChannels()) {
				channel.delete().queue();
			}
			categoryToDelete.delete().queue();
			ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
			ThreadChannel post = getPost(event, campaign);
			post.getManager().setAppliedTags(forum.getAvailableTagsByName("ARCHIVED", true)).queue();
			List<Button> buttons = post.retrieveStartMessage().complete().getButtons();
			post.editMessageComponentsById(campaign.getPost_id(), ActionRow.of(buttons.get(0).asDisabled()))
					.queue();
			post.getManager().setLocked(true).queue();
			event.getGuild().getRoleById(campaign.getRole_id()).delete().queue();
			event.getGuild().getRoleById(campaign.getDm_role_id()).delete().queue();
			campaignDAO.deleteCampaign(campaign);
		}
	}

	public static void OpenCampaign(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Category campaignCategory = event.getChannel().asTextChannel().getParentCategory();
		Campaign campaign = campaignDAO.getCampaignByCategory(campaignCategory.getId());
		ArrayList<LayoutComponent> actionRows = new ArrayList<>(event.getMessage().getComponents());
		List<ItemComponent> firstRow = actionRows.get(0).getComponents();
		firstRow.set(0, Button.danger("close", "Close"));
		actionRows.set(0, ActionRow.of(firstRow));
		event.getMessage().editMessageComponents().setComponents(actionRows).queue();
		campaign.open();
		ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
		ThreadChannel post = getPost(event, campaign);
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
	}

	public static void CloseCampaign(ButtonInteractionEvent event, CampaignDAO campaignDAO) {
		Category campaignCategory = event.getChannel().asTextChannel().getParentCategory();
		Campaign campaign = campaignDAO.getCampaignByCategory(campaignCategory.getId());
		ArrayList<LayoutComponent> actionRows = new ArrayList<>(event.getMessage().getComponents());
		List<ItemComponent> firstRow = actionRows.get(0).getComponents();
		firstRow.set(0, Button.success("open", "Open"));
		actionRows.set(0, ActionRow.of(firstRow));
		event.getMessage().editMessageComponents().setComponents(actionRows).queue();
		campaign.close();
		ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
		ThreadChannel post = getPost(event, campaign);
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

	public static void OpenSettings(ButtonInteractionEvent event) {
		ArrayList<Button> buttons = new ArrayList<Button>(
				event.getMessage().getActionRows().get(0).getButtons());
		buttons.set(3, Button.success("close-settings", "Close Settings"));
		event.getHook()
				.editMessageComponentsById(event.getMessageId(), ActionRow.of(buttons),
						ActionRow.of(
								Button.secondary("rename-campaign", "Rename Campaign"),
								Button.primary("kick-player", "Kick Player"),
								Button.danger("archive-campaign", "End Campaign")))
				.queue();
	}

	public static void CloseSettings(ButtonInteractionEvent event) {
		ArrayList<Button> oldButtons = new ArrayList<Button>(
				event.getMessage().getActionRows().get(0).getButtons());
		oldButtons.set(3, Button.danger("campaign-settings", "Settings"));
		event.getHook().editMessageComponentsById(event.getMessageId(), ActionRow.of(oldButtons)).queue();
	}

	private static ThreadChannel getPost(GenericInteractionCreateEvent event, Campaign campaign) {
		ForumChannel forum = event.getGuild().getForumChannelById(CAMPAIGN_FORUM);
		ThreadChannel post = forum.getThreadChannels().stream()
				.filter(thisPost -> thisPost.getId().equals(campaign.getPost_id()))
				.toList().get(0);
		return post;
	}
}