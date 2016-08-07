/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusGenericArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import javax.inject.Inject;

@Permissions(root = "teleport")
@NoWarmup
@NoCost
@NoCooldown
@RegisterCommand({"tpall", "tpallhere"})
public class TeleportAllHereCommand extends CommandBase<Player> {

    @Inject private TeleportHandler handler;
    @Inject private TeleportConfigAdapter tca;

    private final String tristateResult = "tristate";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags()
                    .valueFlag(GenericArguments.optionalWeak(NucleusGenericArgument.tristateChoice(Text.of(tristateResult))), "s", "-safe")
                    .buildWith(GenericArguments.none())
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        boolean isSafe;
        Tristate t = args.<Tristate>getOne(tristateResult).orElse(Tristate.UNDEFINED);
        if (t == Tristate.UNDEFINED) {
            isSafe = args.hasAny("s") || tca.getNodeOrDefault().isDefaultSafeTeleport();
        } else {
            isSafe = t.asBoolean();
        }

        MessageChannel.TO_ALL.send(Util.getTextMessageWithFormat("command.tpall.broadcast", src.getName()));
        Sponge.getServer().getOnlinePlayers().forEach(x -> {
            if (!x.equals(src)) {
                try {
                    handler.getBuilder().setFrom(x).setTo(src).setSafe(isSafe).setSilentSource(true)
                            .setBypassToggle(true).startTeleport();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return CommandResult.success();
    }
}
