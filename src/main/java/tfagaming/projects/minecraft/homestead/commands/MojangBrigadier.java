package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.PluginCommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.lucko.commodore.Commodore;
import tfagaming.projects.minecraft.homestead.Homestead;

public class MojangBrigadier {
	private Homestead plugin;
	private Commodore commodore;

	public MojangBrigadier(Homestead plugin, Commodore commodore) {
		this.plugin = plugin;
		this.commodore = commodore;

		setRegionCommand();
		setOtherCommands();
	}

	public void setRegionCommand() {
		LiteralCommandNode<?> regionCommandNode = LiteralArgumentBuilder.literal("region")
				.then(LiteralArgumentBuilder.literal("accept")
						.then(RequiredArgumentBuilder.argument("region",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("auto"))
				.then(LiteralArgumentBuilder.literal("banlist"))
				.then(LiteralArgumentBuilder.literal("ban")
						.then(RequiredArgumentBuilder.argument("player",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("claimlist"))
				.then(LiteralArgumentBuilder.literal("create")
						.then(RequiredArgumentBuilder.argument("name",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("delete")
						.then(RequiredArgumentBuilder.argument("confirm",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("deny")
						.then(RequiredArgumentBuilder.argument("region",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("deposit")
						.then(RequiredArgumentBuilder.argument("amount",
								IntegerArgumentType.integer(0, 1000000000))))
				.then(LiteralArgumentBuilder.literal("flags")
						.then(LiteralArgumentBuilder.literal("global")
								.then(RequiredArgumentBuilder
										.argument("flag", StringArgumentType
												.string())
										.then(RequiredArgumentBuilder.argument(
												"state",
												StringArgumentType
														.string()))))
						.then(LiteralArgumentBuilder.literal("member")
								.then(RequiredArgumentBuilder
										.argument("player", StringArgumentType
												.string())
										.then(RequiredArgumentBuilder
												.argument("flag",
														StringArgumentType
																.string())
												.then(RequiredArgumentBuilder
														.argument(
																"state",
																StringArgumentType
																		.string())))))
						.then(LiteralArgumentBuilder.literal("world")
								.then(RequiredArgumentBuilder
										.argument("flag", StringArgumentType
												.string())
										.then(RequiredArgumentBuilder.argument(
												"state",
												StringArgumentType
														.string())))))
				.then(LiteralArgumentBuilder.literal("help"))
				.then(LiteralArgumentBuilder.literal("home"))
				.then(LiteralArgumentBuilder.literal("logs"))
				.then(LiteralArgumentBuilder.literal("members"))
				.then(LiteralArgumentBuilder.literal("menu"))
				.then(LiteralArgumentBuilder.literal("player")
						.then(RequiredArgumentBuilder.argument("player",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("borders")
						.then(LiteralArgumentBuilder.literal("stop")))
				.then(LiteralArgumentBuilder.literal("info")
						.then(RequiredArgumentBuilder.argument("region",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("rate")
						.then(RequiredArgumentBuilder.argument("region",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("top"))
				.then(LiteralArgumentBuilder.literal("rename")
						.then(RequiredArgumentBuilder.argument("newname",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("set")
						.then(LiteralArgumentBuilder.literal("description").then(
								RequiredArgumentBuilder.argument("description",
										StringArgumentType.greedyString())))
						.then(LiteralArgumentBuilder.literal("displayname").then(
								RequiredArgumentBuilder.argument("displayname",
										StringArgumentType.greedyString())))
						.then(LiteralArgumentBuilder.literal("icon")
								.then(RequiredArgumentBuilder.argument("icon",
										StringArgumentType.string())))
						.then(LiteralArgumentBuilder.literal("mapcolor")
								.then(RequiredArgumentBuilder.argument("color",
										StringArgumentType.string())))
						.then(LiteralArgumentBuilder.literal("spawn"))
						.then(LiteralArgumentBuilder.literal("target")
								.then(RequiredArgumentBuilder.argument("region",
										StringArgumentType.string())))
						.then(LiteralArgumentBuilder.literal("tax").then(
								RequiredArgumentBuilder.argument("amount",
										IntegerArgumentType.integer(0,
												1000000000)))))
				.then(LiteralArgumentBuilder.literal("subareas")
						.then(LiteralArgumentBuilder.literal("create")
								.then(RequiredArgumentBuilder.argument("name",
										StringArgumentType.string())))
						.then(LiteralArgumentBuilder.literal("rename")
								.then(RequiredArgumentBuilder.argument("subarea",
										StringArgumentType.string())
										.then(RequiredArgumentBuilder.argument(
												"newname",
												StringArgumentType
														.string()))))
						.then(LiteralArgumentBuilder.literal("delete")
								.then(RequiredArgumentBuilder.argument("subarea",
										StringArgumentType.string())))
						.then(LiteralArgumentBuilder.literal("flags")
								.then(RequiredArgumentBuilder
										.argument("subarea", StringArgumentType
												.string())
										.then(RequiredArgumentBuilder.argument(
												"flag",
												StringArgumentType
														.string())
												.then(RequiredArgumentBuilder
														.argument("state",
																StringArgumentType
																		.string()))))))
				.then(LiteralArgumentBuilder.literal("trust")
						.then(RequiredArgumentBuilder.argument("player",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("unban")
						.then(RequiredArgumentBuilder.argument("player",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("untrust")
						.then(RequiredArgumentBuilder.argument("player",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("visit")
						.then(RequiredArgumentBuilder.argument("name",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("withdraw")
						.then(RequiredArgumentBuilder.argument("amount",
								IntegerArgumentType.integer(0, 1000000000))))
				.build();

		PluginCommand command = plugin.getCommand("region");

		commodore.register(command, regionCommandNode);
	}

	public void setOtherCommands() {
		// /claim
		LiteralCommandNode<?> claimCommandNode = LiteralArgumentBuilder.literal("claim")
				.build();

		PluginCommand claimCommand = plugin.getCommand("claim");

		commodore.register(claimCommand, claimCommandNode);

		// /unclaim
		LiteralCommandNode<?> unclaimCommandNode = LiteralArgumentBuilder.literal("unclaim")
				.build();

		PluginCommand unclaimCommand = plugin.getCommand("unclaim");

		commodore.register(unclaimCommand, unclaimCommandNode);

		// /homesteadadmin
		LiteralCommandNode<?> homesteadAdminCommandNode = LiteralArgumentBuilder.literal("homesteadadmin")
				.then(LiteralArgumentBuilder.literal("importdata")
						.then(RequiredArgumentBuilder.argument("plugin",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("migratedata")
						.then(RequiredArgumentBuilder.argument("provider",
								StringArgumentType.string())))
				.then(LiteralArgumentBuilder.literal("plugin"))
				.then(LiteralArgumentBuilder.literal("reload"))
				.then(LiteralArgumentBuilder.literal("updates"))
				.build();

		PluginCommand homesteadAdminCommand = plugin.getCommand("homesteadadmin");

		commodore.register(homesteadAdminCommand, homesteadAdminCommandNode);
	}
}
