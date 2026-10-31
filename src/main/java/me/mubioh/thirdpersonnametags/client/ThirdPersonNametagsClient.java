package me.mubioh.thirdpersonnametags.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ThirdPersonNametagsClient implements ClientModInitializer {

    private static final String SCALE_ARGUMENT = "scale";

    @Override
    public void onInitializeClient() {
        NametagScaleConfig.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("nametagscale")
                        .executes(ThirdPersonNametagsClient::showScales)
                        .then(literal("self")
                                .then(argument(SCALE_ARGUMENT, FloatArgumentType.floatArg(NametagScaleConfig.MIN_SCALE, NametagScaleConfig.MAX_SCALE))
                                        .executes(context -> applyScale(context, "Own", NametagScaleConfig::setSelfScale))))
                        .then(literal("others")
                                .then(argument(SCALE_ARGUMENT, FloatArgumentType.floatArg(NametagScaleConfig.MIN_SCALE, NametagScaleConfig.MAX_SCALE))
                                        .executes(context -> applyScale(context, "Other players'", NametagScaleConfig::setOthersScale))))
                        .then(literal("reset")
                                .executes(ThirdPersonNametagsClient::resetScales))));
    }

    private static int showScales(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("Nametag scale - self: " + NametagScaleConfig.selfScale() + ", others: " + NametagScaleConfig.othersScale()));
        return 1;
    }

    private static int applyScale(CommandContext<FabricClientCommandSource> context, String target, Consumer<Float> setter) {
        final float scale = FloatArgumentType.getFloat(context, SCALE_ARGUMENT);
        setter.accept(scale);
        context.getSource().sendFeedback(Text.literal(target + " nametag scale set to " + scale));
        return 1;
    }

    private static int resetScales(CommandContext<FabricClientCommandSource> context) {
        NametagScaleConfig.setSelfScale(NametagScaleConfig.DEFAULT_SCALE);
        NametagScaleConfig.setOthersScale(NametagScaleConfig.DEFAULT_SCALE);
        return showScales(context);
    }
}
