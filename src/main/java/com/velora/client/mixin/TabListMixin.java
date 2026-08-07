package com.velora.client.mixin;

import com.velora.client.client.RankSystem;
import com.velora.client.config.ModConfig;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class TabListMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void velora_addTabPrefix(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (!ModConfig.showTabListPrefix) return;

        try {
            String playerName = entry.getProfile().getName();
            RankSystem.Rank rank = RankSystem.getRankForPlayer(playerName);
            if (rank == RankSystem.Rank.DEFAULT) return;

            MutableText prefix = Text.literal("[" + rank.getDisplayName() + "] ").withColor(rank.getColor());
            Text original = cir.getReturnValue();
            cir.setReturnValue(prefix.append(original.copy()));
        } catch (Exception e) {
            // ignore
        }
    }
}
