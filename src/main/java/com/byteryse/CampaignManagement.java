package com.byteryse;

import com.byteryse.DAO.CampaignDAO;
import com.byteryse.DTO.Campaign;
import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class CampaignManagement {
	private static final String CAMPAIGN_FORUM = "1263135869645094993";
	private static final String GAME_ANNOUNCEMENTS = "1252710143838261399";

	public static void campaignCreationModal(SlashCommandInteractionEvent event) {
		if (!event.getMember().getRoles().contains(event.getGuild().getRoleById("1252378924961370182"))) {
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
						"Example:\n- New/Veteran/All Players\n- Serious/Fun/Novelty Campaign\n- RP/Combat Heavy\n- Saturday/Sunday Availability")
				.setMinLength(1)
				.setMaxLength(100)
				.build();

		Modal modal = Modal.create("create-campaign", "Create Campaign")
				.addComponents(ActionRow.of(name), ActionRow.of(description), ActionRow.of(tags))
				.build();

		event.replyModal(modal).queue();
	}

	public static void createCampaign(ModalInteractionEvent event, CampaignDAO campaignDAO) {
		if (event.getGuild().getRoles().stream().map(role -> role.getName().toLowerCase()).toList()
				.contains(event.getValue("name").getAsString().toLowerCase())) {
			event.reply("Couldn't create campaign. A role likely already exists with that name.")
					.setEphemeral(true)
					.queue();
			return;
		}
		event.deferReply().setEphemeral(true).queue();
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
					post.getThreadChannel().getId());
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
							Button.success("open-close", "Open"),
							Button.primary("schedule", "Schedule Session"),
							Button.secondary("availability", "Check Availability"),
							Button.danger("campaign-settings", "Settings"))
					.build()).queue();
		} catch (Exception e) {
			System.out.println(e);
			event.getHook().sendMessage("Hmmm. Something went wrong.").setEphemeral(true).queue();
		}
	}
}
