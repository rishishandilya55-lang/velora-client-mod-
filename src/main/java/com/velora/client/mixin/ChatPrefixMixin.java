package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatPrefixMixin {

    private static boolean velora_reentrant = false;

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void velora_addChatPrefix(Text message, CallbackInfo ci) {
        if (!ModConfig.showChatPrefix || velora_reentrant) return;

        try {
            String fullText = message.getString();
            int colonIndex = fullText.indexOf(':');
            if (colonIndex <= 0) return;

            String playerName = fullText.substring(0, colonIndex);
            RankSystem.Rank rank = RankSystem.getRankForPlayer(playerName);
            if (rank == RankSystem.Rank.DEFAULT) return;

            String rest = fullText.substring(colonIndex);
            MutableText rankedPrefix = Text.literal("[" + rank.getDisplayName() + "] ").withColor(rank.getColor());
            MutableText nameText = Text.literal(playerName).withColor(0xFFFFFFFF).styled(s -> s.withBold(false));
            MutableText chatRest = Text.literal(rest);

            ci.cancel();
            velora_reentrant = true;
            ((ChatHud)(Object)this).addMessage(rankedPrefix.append(nameText).append(chatRest));
        } catch (Exception e) {
            // ignore
        } finally {
            velora_reentrant = false;
        }
    }
}
