/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

/**
 * Deletes a world.
 *
 * Command Usage: /world delete [world] Permission: plugin.world.delete.base
 */
@Permissions(root = "world")
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WorldCommand.class)
public class DeleteWorldCommand extends io.github.nucleuspowered.nucleus.internal.command.AbstractCommand<CommandSource> {

    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.world(Text.of(world)))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldProperties = args.<WorldProperties>getOne(world).get();
        World altWorld = null;

        for (World w : Sponge.getServer().getWorlds()) {
            if (!w.getUniqueId().equals(worldProperties.getUniqueId())) {
                altWorld = w;
                break;
            }
        }

        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getWorld().getUniqueId().equals(worldProperties.getUniqueId()) && altWorld != null) {
                player.transferToWorld(altWorld.getName(), altWorld.getSpawnLocation().getPosition());
            }
        }

        worldProperties.setEnabled(false);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.delete"));
        return CommandResult.success();
    }
}
