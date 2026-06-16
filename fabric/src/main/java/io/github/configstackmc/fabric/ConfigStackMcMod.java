package io.github.configstackmc.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.configstackmc.common.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import java.io.IOException;import java.nio.file.*;import java.util.Map;

public final class ConfigStackMcMod implements ModInitializer {
    public static final String MOD_ID="configstackmc"; public static final Logger LOGGER=LoggerFactory.getLogger(MOD_ID);
    private static volatile StackConfig config = new StackConfig(99,true, Map.of(),0);
    @Override public void onInitialize(){ load(); CommandRegistrationCallback.EVENT.register((dispatcher, registry, selection) -> dispatcher.register(Commands.literal("stackconfig")
        .then(Commands.literal("reload").executes(ctx->{ load(); ctx.getSource().sendSuccess(()->Component.literal("ConfigStackMC reloaded: "+config.rules().size()+" rules."), true); return 1; }))
        .then(Commands.literal("info").then(Commands.argument("item", StringArgumentType.word()).executes(ctx->{ sendInfo(ctx.getSource(), StringArgumentType.getString(ctx,"item")); return 1; }))))); }
    public static StackConfig config(){ return config; }
    public static void load(){ Path path=FabricLoader.getInstance().getConfigDir().resolve(MOD_ID+".yml"); try{ if(Files.notExists(path)) Files.writeString(path, StackConfigParser.DEFAULT_CONFIG); config=StackConfigParser.parse(Files.readString(path), logger()); LOGGER.info("Loaded {} valid stack rules; rejected {} invalid rules.", config.rules().size(), config.invalidRuleCount()); }catch(IOException e){ LOGGER.error("Unable to load {}", path, e); } }
    private static ConfigLogger logger(){ return new ConfigLogger(){ public void warn(String m){LOGGER.warn(m);} public void info(String m){LOGGER.info(m);} }; }
    private static void sendInfo(net.minecraft.commands.CommandSourceStack source, String raw){ try{ String id=ItemIds.normalize(raw); Identifier loc=Identifier.parse(id); Item item=BuiltInRegistries.ITEM.getValue(loc); if(item==null){ source.sendFailure(Component.literal("Unknown item: "+id)); return; } StackRule rule=config.rules().get(id); source.sendSuccess(()->Component.literal("Item: "+id).withStyle(ChatFormatting.YELLOW), false); source.sendSuccess(()->Component.literal("Configured stack size: "+(rule==null?"not configured":rule.maxStackSize())), false); source.sendSuccess(()->Component.literal("Vanilla/default stack size: "+item.getDefaultMaxStackSize()), false); source.sendSuccess(()->Component.literal("Modified by ConfigStackMC: "+(rule!=null && rule.maxStackSize()!=item.getDefaultMaxStackSize())), false); }catch(Exception e){ source.sendFailure(Component.literal(e.getMessage())); } }
}
