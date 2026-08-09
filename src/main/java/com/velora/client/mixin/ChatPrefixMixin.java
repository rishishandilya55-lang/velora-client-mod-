package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public abstract class ChatPrefixMixin {

    private static boolean velora_reentrant = false;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V")
    )
    private void redirectChatBackgroundFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (ModConfig.customChatBackground) {
            int userAlpha = Math.max(0, Math.min(255, ModConfig.chatBackgroundOpacity));
            int bgRgb = ModConfig.chatBackgroundColor & 0x00FFFFFF;
            context.fill(x1, y1, x2, y2, (userAlpha << 24) | bgRgb);
        } else {
            context.fill(x1, y1, x2, y2, color);
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void velora_processChatMessage(Text message, CallbackInfo ci) {
        if (velora_reentrant) return;

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return;

            String fullText = message.getString();
            String localPlayerName = (mc.getSession() != null) ? mc.getSession().getUsername() : null;

            String senderName = null;
            int delimiterEndIndex = -1;

            if (fullText.contains(" » ")) {
                int idx = fullText.indexOf(" » ");
                senderName = fullText.substring(0, idx).trim();
                delimiterEndIndex = idx + 3;
            } else if (fullText.startsWith("<") && fullText.contains(">")) {
                int closeIdx = fullText.indexOf('>');
                senderName = fullText.substring(1, closeIdx).trim();
                delimiterEndIndex = closeIdx + 1;
                if (delimiterEndIndex < fullText.length() && fullText.charAt(delimiterEndIndex) == ' ') {
                    delimiterEndIndex++;
                }
            } else if (fullText.contains(": ")) {
                int idx = fullText.indexOf(": ");
                senderName = fullText.substring(0, idx).trim();
                delimiterEndIndex = idx + 2;
            }

            if (senderName != null && senderName.contains("] ")) {
                senderName = senderName.substring(senderName.lastIndexOf("] ") + 2).trim();
            }

            boolean isOwnMessage = (senderName != null && localPlayerName != null && senderName.equalsIgnoreCase(localPlayerName));
            RankSystem.Rank rank = (senderName != null) ? RankSystem.getRankForPlayer(senderName) : RankSystem.Rank.DEFAULT;
            boolean hasRankPrefix = (ModConfig.showChatPrefix && rank != RankSystem.Rank.DEFAULT);

            String messageBody = (delimiterEndIndex != -1 && delimiterEndIndex <= fullText.length())
                ? fullText.substring(delimiterEndIndex)
                : fullText;

            boolean hasMention = false;
            if (!isOwnMessage && ModConfig.chatHighlightMentions && localPlayerName != null && !localPlayerName.isEmpty()) {
                Pattern mentionPattern = Pattern.compile("(?i)\\b" + Pattern.quote(localPlayerName) + "\\b");
                if (mentionPattern.matcher(messageBody).find()) {
                    hasMention = true;
                }
            }

            if (!hasRankPrefix && !hasMention && !ModConfig.chatShowTimestamp) {
                return;
            }

            ci.cancel();
            velora_reentrant = true;

            MutableText processed = Text.empty();

            // 1. Timestamp Prefix
            if (ModConfig.chatShowTimestamp) {
                String timeStr = "[" + LocalTime.now().format(TIME_FORMATTER) + "] ";
                processed.append(Text.literal(timeStr).formatted(Formatting.DARK_GRAY));
            }

            // 2. Rank Prefix
            if (hasRankPrefix) {
                processed.append(Text.literal("[" + rank.getDisplayName() + "] ").withColor(rank.getColor()));
            }

            // 3. Sender and Body handling
            if (hasMention && localPlayerName != null) {
                if (ModConfig.chatMentionSound && mc.getSoundManager() != null) {
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f, 0.8f));
                }

                // Append sender part unmodified
                if (delimiterEndIndex != -1 && delimiterEndIndex <= fullText.length()) {
                    processed.append(Text.literal(fullText.substring(0, delimiterEndIndex)));
                }

                // Highlight player name only in the body
                Pattern mentionPattern = Pattern.compile("(?i)\\b(" + Pattern.quote(localPlayerName) + ")\\b");
                Matcher matcher = mentionPattern.matcher(messageBody);
                int lastEnd = 0;
                int mentionRgb = ModConfig.chatMentionColor & 0x00FFFFFF;

                while (matcher.find()) {
                    if (matcher.start() > lastEnd) {
                        processed.append(Text.literal(messageBody.substring(lastEnd, matcher.start())));
                    }
                    String matchedName = matcher.group(1);
                    processed.append(Text.literal("@" + matchedName).styled(s -> s
                        .withColor(TextColor.fromRgb(mentionRgb))
                        .withBold(true)
                        .withUnderline(true)
                    ));
                    lastEnd = matcher.end();
                }
                if (lastEnd < messageBody.length()) {
                    processed.append(Text.literal(messageBody.substring(lastEnd)));
                }
            } else {
                processed.append(message);
            }

            ((ChatHud) (Object) this).addMessage(processed);

        } catch (Exception e) {
            // fallback
        } finally {
            velora_reentrant = false;
        }
    }
}
