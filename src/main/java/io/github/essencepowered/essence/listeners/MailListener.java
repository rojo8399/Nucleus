/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.listeners;

import com.google.inject.Inject;
import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.ListenerBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.services.MailHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.concurrent.TimeUnit;

@Modules(PluginModule.MAILS)
public class MailListener extends ListenerBase {

    @Inject private MailHandler handler;
    @Inject private Game game;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        game.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            int mailCount = handler.getMail(event.getTargetEntity()).size();
            if (mailCount > 0) {
                event.getTargetEntity().sendMessage(Util.getTextMessageWithFormat("mail.login", String.valueOf(mailCount)));
                event.getTargetEntity().sendMessage(Text.builder()
                        .append(Text.builder("/mail").color(TextColors.AQUA).style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("/mail"))
                                .onHover(TextActions.showText(Text.of("Click here to read your mail."))).build())
                        .append(Text.builder().append(Text.of(TextColors.YELLOW, " ")).append(Util.getTextMessageWithFormat("mail.toread"))
                                .append(Text.of(" ")).build())
                        .append(Text.builder("/mail clear").color(TextColors.AQUA).style(TextStyles.UNDERLINE)
                                .onClick(TextActions.runCommand("/mail clear"))
                                .onHover(TextActions.showText(Text.of("Click here to delete your mail."))).build())
                        .append(Text.builder().append(Text.of(TextColors.YELLOW, " ")).append(Util.getTextMessageWithFormat("mail.toclear")).build())
                        .build());
            }
        } , 1, TimeUnit.SECONDS);
    }
}