/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
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
import java.util.HashMap;
import java.util.Map;

import static io.github.nucleuspowered.nucleus.modules.teleport.commands.TeleportAskHereCommand.playerKey;

@Permissions(root = "teleport", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoWarmup(generateConfigEntry = true)
@RegisterCommand({"tpahere", "tpaskhere", "teleportaskhere"})
@NotifyIfAFK(playerKey)
public class TeleportAskHereCommand extends CommandBase<Player> {

    @Inject private TeleportHandler tpHandler;
    @Inject private TeleportConfigAdapter tca;

    private static final String tristateResult = "tristate";
    static final String playerKey = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("force", new PermissionInformation(Util.getMessageWithFormat("permission.teleport.force"), SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new NicknameArgument(Text.of(playerKey), plugin.getUserDataManager(), NicknameArgument.UnderlyingType.PLAYER)),
            GenericArguments.flags().permissionFlag(permissions.getPermissionWithSuffix("force"), "f").buildWith(GenericArguments.none())
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

        TeleportHandler.TeleportBuilder tb = tpHandler.getBuilder().setFrom(target).setTo(src).setSafe(isSafe);
        int warmup = getWarmup(target);
        if (warmup > 0) {
            tb.setWarmupTime(warmup);
        }

        double cost = getCost(src, args);
        if (cost > 0.) {
            tb.setCharge(src).setCost(cost);
        }

        // The question needs to be asked of the target
        tpHandler.addAskQuestion(target.getUniqueId(), new TeleportHandler.TeleportPrep(Instant.now().plus(30, ChronoUnit.SECONDS), src, cost, tb));
        target.sendMessage(Util.getTextMessageWithFormat("command.tpahere.question", src.getName()));
        target.sendMessage(tpHandler.getAcceptDenyMessage());

        src.sendMessage(Util.getTextMessageWithFormat("command.tpask.sent", target.getName()));
        return CommandResult.success();
    }
}
