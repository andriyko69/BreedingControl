package io.github.andriyko69.breedingcontrol.mixin;

import io.github.andriyko69.breedingcontrol.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSwingMixin {

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void breedingcontrol$cancelSwing(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof EntityHitResult ehr)) return;

        Entity target = ehr.getEntity();
        if (!(target instanceof Animal animal)) return;

        ItemStack stack = mc.player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        if (!animal.isFood(stack)) return;
        if (Config.isAllowed(target.getType())) return;

        if (animal.isBaby() && Config.isBabyFeedingAllowed) return;

        if (Config.isHealingWithBreedFoodAllowed && animal.getHealth() < animal.getMaxHealth()) return;

        ci.cancel();
    }
}