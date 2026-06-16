package io.github.configstackmc.paper;

import io.github.configstackmc.common.*;
import org.bukkit.*;import org.bukkit.command.*;import org.bukkit.entity.*;import org.bukkit.event.*;import org.bukkit.event.world.LootGenerateEvent;import org.bukkit.event.block.BlockDropItemEvent;import org.bukkit.event.entity.*;import org.bukkit.event.inventory.*;import org.bukkit.event.player.*;import org.bukkit.inventory.*;import org.bukkit.inventory.meta.ItemMeta;import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;import java.nio.file.*;import java.util.*;

public final class ConfigStackMcPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private StackConfig config = new StackConfig(99, true, Map.of(), 0);
    @Override public void onEnable(){ saveDefaultConfigFile(); reloadRules(); Bukkit.getPluginManager().registerEvents(this,this); Objects.requireNonNull(getCommand("stackconfig")).setExecutor(this); Objects.requireNonNull(getCommand("stackconfig")).setTabCompleter(this); }
    private void saveDefaultConfigFile(){ try{ if(!getDataFolder().exists()) getDataFolder().mkdirs(); Path p=getDataFolder().toPath().resolve("config.yml"); if(Files.notExists(p)) Files.writeString(p, StackConfigParser.DEFAULT_CONFIG); }catch(IOException e){ getLogger().severe("Unable to write default config.yml: "+e.getMessage()); } }
    private void reloadRules(){ try{ config=StackConfigParser.parse(Files.readString(getDataFolder().toPath().resolve("config.yml")), logger()); getLogger().info("Loaded "+config.rules().size()+" valid stack rules; rejected "+config.invalidRuleCount()+" invalid rules."); }catch(IOException e){ getLogger().severe("Unable to read config.yml: "+e.getMessage()); } }
    private ConfigLogger logger(){ return new ConfigLogger(){ public void warn(String m){getLogger().warning(m);} public void info(String m){getLogger().info(m);} }; }
    public ItemStack applyRule(ItemStack stack){ if(stack==null||stack.getType().isAir()) return stack; String id=stack.getType().getKey().asString(); StackRule rule=config.rules().get(id); if(rule==null) return stack; ItemMeta meta=stack.getItemMeta(); if(meta==null) return stack; if(meta.hasMaxStackSize() && meta.getMaxStackSize()==rule.maxStackSize()) return stack; ItemStack copy=stack.clone(); ItemMeta copyMeta=copy.getItemMeta(); try{ copyMeta.setMaxStackSize(rule.maxStackSize()); copy.setItemMeta(copyMeta); return copy; }catch(IllegalArgumentException ex){ getLogger().warning("Paper rejected max_stack_size "+rule.maxStackSize()+" for "+id+": "+ex.getMessage()); return stack; } }
    public void normalizeInventory(Inventory inv){ if(inv==null) return; ItemStack[] contents=inv.getContents(); boolean changed=false; for(int i=0;i<contents.length;i++){ ItemStack after=applyRule(contents[i]); if(after!=contents[i]){ contents[i]=after; changed=true; } } if(changed) inv.setContents(contents); }
    private void normalizeLater(Inventory inv){ Bukkit.getScheduler().runTask(this,()->normalizeInventory(inv)); }
    @EventHandler public void join(PlayerJoinEvent e){ normalizeLater(e.getPlayer().getInventory()); }
    @EventHandler public void open(InventoryOpenEvent e){ normalizeLater(e.getInventory()); normalizeLater(e.getPlayer().getInventory()); }
    @EventHandler public void click(InventoryClickEvent e){ normalizeLater(e.getInventory()); normalizeLater(e.getWhoClicked().getInventory()); }
    @EventHandler public void drag(InventoryDragEvent e){ normalizeLater(e.getInventory()); }
    @EventHandler public void creative(InventoryCreativeEvent e){ e.setCursor(applyRule(e.getCursor())); normalizeLater(e.getWhoClicked().getInventory()); }
    @EventHandler public void craftPrepare(PrepareItemCraftEvent e){ e.getInventory().setResult(applyRule(e.getInventory().getResult())); }
    @EventHandler public void craft(CraftItemEvent e){ e.setCurrentItem(applyRule(e.getCurrentItem())); }
    @EventHandler public void anvil(PrepareAnvilEvent e){ e.setResult(applyRule(e.getResult())); }
    @EventHandler public void smith(PrepareSmithingEvent e){ e.setResult(applyRule(e.getResult())); }
    @EventHandler public void furnace(FurnaceExtractEvent e){ normalizeLater(e.getPlayer().getInventory()); }
    @EventHandler public void pickup(EntityPickupItemEvent e){ e.getItem().setItemStack(applyRule(e.getItem().getItemStack())); }
    @EventHandler public void attempt(PlayerAttemptPickupItemEvent e){ e.getItem().setItemStack(applyRule(e.getItem().getItemStack())); }
    @EventHandler public void drop(PlayerDropItemEvent e){ e.getItemDrop().setItemStack(applyRule(e.getItemDrop().getItemStack())); }
    @EventHandler public void entityDrop(EntityDropItemEvent e){ e.getItemDrop().setItemStack(applyRule(e.getItemDrop().getItemStack())); }
    @EventHandler public void blockDrop(BlockDropItemEvent e){ e.getItems().forEach(i->i.setItemStack(applyRule(i.getItemStack()))); }
    @EventHandler public void move(InventoryMoveItemEvent e){ e.setItem(applyRule(e.getItem())); }
    @EventHandler public void loot(LootGenerateEvent e){ e.getLoot().replaceAll(this::applyRule); }
    @EventHandler public void death(EntityDeathEvent e){ e.getDrops().replaceAll(this::applyRule); }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args){ if(args.length>0&&args[0].equalsIgnoreCase("reload")){ if(!sender.hasPermission("stackconfig.reload")) return noPerm(sender); reloadRules(); Bukkit.getOnlinePlayers().forEach(p->{normalizeInventory(p.getInventory()); if(p.getOpenInventory()!=null) normalizeInventory(p.getOpenInventory().getTopInventory());}); sender.sendMessage(ChatColor.GREEN+"ConfigStackMC reloaded: "+config.rules().size()+" rules."); return true;} if(args.length>1&&args[0].equalsIgnoreCase("info")){ if(!sender.hasPermission("stackconfig.info")) return noPerm(sender); info(sender,args[1]); return true;} return false; }
    private boolean noPerm(CommandSender s){ s.sendMessage(ChatColor.RED+"You do not have permission."); return true; }
    private void info(CommandSender s,String raw){ try{ String id=ItemIds.normalize(raw); Material mat=Registry.MATERIAL.get(NamespacedKey.fromString(id)); if(mat==null||!mat.isItem()){s.sendMessage(ChatColor.RED+"Unknown item: "+id);return;} StackRule r=config.rules().get(id); s.sendMessage(ChatColor.YELLOW+"Item: "+id); s.sendMessage("Configured stack size: "+(r==null?"not configured":r.maxStackSize())); s.sendMessage("Vanilla/default stack size: "+mat.getMaxStackSize()); s.sendMessage("Modified by ConfigStackMC: "+(r!=null && r.maxStackSize()!=mat.getMaxStackSize())); }catch(Exception e){s.sendMessage(ChatColor.RED+e.getMessage());} }
    @Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){ if(args.length==1) return List.of("reload","info"); return List.of(); }
}
