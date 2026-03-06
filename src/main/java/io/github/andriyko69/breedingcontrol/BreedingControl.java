package io.github.andriyko69.breedingcontrol;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(BreedingControl.MOD_ID)
public class BreedingControl {
    public static final String MOD_ID = "breedingcontrol";

    public BreedingControl(IEventBus ignoredModEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @EventBusSubscriber(modid = BreedingControl.MOD_ID)
    private static final class Events {
        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            handle(event.getTarget(), event.getEntity(), event.getItemStack(), event);
        }

        @SubscribeEvent
        public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
            handle(event.getTarget(), event.getEntity(), event.getItemStack(), event);
        }

        private static void handle(Entity target, Player player, ItemStack stack, ICancellableEvent event) {
            if (!(target instanceof Animal animal)) return;
            if (stack.isEmpty()) return;
            if (!animal.isFood(stack)) return;
            if (Config.isAllowed(target.getType())) return;

            boolean client = player.level().isClientSide();

            if (Config.isHealingWithBreedFoodAllowed && animal.getHealth() < animal.getMaxHealth()) {
                if (client) {
                    cancel(event, true, true);
                    return;
                }

                cancel(event, false, true);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                animal.heal(Config.healAmount);
                return;
            }

            if (animal.isBaby() && Config.isBabyFeedingAllowed) return;

            cancel(event, client, false);
        }

        private static void cancel(ICancellableEvent event, boolean client, boolean allowAnimation) {
            event.setCanceled(true);

            InteractionResult result;
            if (allowAnimation) {
                result = InteractionResult.SUCCESS;
            } else {
                result = client ? InteractionResult.FAIL : InteractionResult.SUCCESS;
            }

            if (event instanceof PlayerInteractEvent.EntityInteract e) {
                e.setCancellationResult(result);
            } else if (event instanceof PlayerInteractEvent.EntityInteractSpecific e) {
                e.setCancellationResult(result);
            }
        }
    }
}
