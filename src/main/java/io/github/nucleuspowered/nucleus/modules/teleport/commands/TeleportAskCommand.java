/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusGenericArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Sends a request to a player to teleport to them, using click handlers.
 */
@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.USER)
@NoWarmup(generateConfigEntry = true)
@RegisterCommand({"tpa", "teleportask"})
@RunAsync
@NotifyIfAFK(TeleportAskCommand.playerKey)
public class TeleportAskCommand extends CommandBase<Player> {

    @Inject private TeleportHandler tpHandler;
    @Inject private TeleportConfigAdapter tca;

    static final String playerKey = "player";
    private static final String tristateResult = "tristate";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(new NicknameArgument(Text.of(playerKey), plugin.getUserDataManager(), NicknameArgument.UnderlyingType.PLAYER)),
                GenericArguments.flags().valueFlag(GenericArguments.optionalWeak(NucleusGenericArgument.tristateChoice(Text.of(tristateResult))), "s", "-safe").buildWith(GenericArguments.none())
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(playerKey).get();
        if (src.equals(target)) {
            src.sendMessage(Util.getTextMessageWithFormat("command.teleport.self"));
            return CommandResult.empty();
        }

        boolean isSafe;
        Tristate t = args.<Tristate>getOne(tristateResult).orElse(Tristate.UNDEFINED);
        if (t == Tristate.UNDEFINED) {
            isSafe = args.hasAny("s") || tca.getNodeOrDefault().isDefaultSafeTeleport();
        } else {
            isSafe = t.asBoolean();
        }

        TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(src).setTo(target).setSafe(isSafe);
        int warmup = getWarmup(src);
        if (warmup > 0) {
            tb.setWarmupTime(warmup);
        }

        double cost = getCost(src, args);
        if (cost > 0.) {
            tb.setCharge(src).setCost(cost);
        }

        tpHandler.addAskQuestion(target.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), src, cost, tb));
        target.sendMessage(Util.getTextMessageWithFormat("command.tpa.question", src.getName()));
        target.sendMessage(tpHandler.getAcceptDenyMessage());

        src.sendMessage(Util.getTextMessageWithFormat("command.tpask.sent", target.getName()));
        return CommandResult.success();
    }
}
