package com.luacraft;

import com.luacraft.classes.Vector;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.luacraft.classes.LuaJavaBlock;
import com.naef.jnlua.LuaRuntimeException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class LuaEventManager {
	public final LuaCraftState l;

	public LuaEventManager(LuaCraftState state) {
		l = state;
	}

	private static boolean isValidResult(int pos) {
		return pos > -1 && pos < Result.values().length;
	}

	// FML Bus

	// Command Events

	/**
	 * @author Jake
	 * @function command.run
	 * @info Calls whenever a command is ran
	 * @arguments [[Player]]:player, [[String]]:command, [[Table]]:arguments
	 * @return nil
	 */

	@SubscribeEvent
	public void onCommand(CommandEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("command.run");

				if (event.sender instanceof EntityPlayer)
					LuaUserdata.PushUserdata(l, event.sender);
				else
					l.pushNil();

				l.pushString(event.command.getCommandName());
				l.newTable();

				for (int i = 0; i < event.parameters.length; i++) {
					l.pushNumber(i + 1);
					l.pushString(event.parameters[i]);
					l.setTable(-3);
				}

				l.call(4, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	// Input Events

	/**
	 * @author Jake
	 * @function input.keypress
	 * @info Calls whenever a key is pressed
	 * @arguments [[Number]]:key, [[Boolean]]:repeat
	 * @return nil
	 */

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("input.keypress");
				l.pushNumber(Keyboard.getEventKey());
				l.pushBoolean(Keyboard.isRepeatEvent());
				l.call(3, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function input.mousepress
	 * @info Calls whenever a mouse button is pressed
	 * @arguments [[Number]]:key
	 * @return nil
	 */

	@SubscribeEvent
	public void onMouseInput(MouseInputEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("input.mousepress");
				l.pushNumber(Mouse.getEventButton());
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	// Player Events (ItemCraftedEvent, ItemPickupEvent, ItemSmeltedEvent,
	// PlayerChangedDimensionEvent, PlayerLoggedInEvent, PlayerLoggedOutEvent,
	// PlayerRespawnEvent)

	/**
	 * @author Jake
	 * @function player.craftitem
	 * @info Called whenever a player crafts a new item
	 * @arguments [[Player]]:player, [[ItemStack]]:stack
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.craftitem");
				LuaUserdata.PushUserdata(l, event.player);
				l.pushUserdataWithMeta(event.crafting, "ItemStack");
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

				l.setTop(0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.pickupitem
	 * @info Called whenever a player picks up an item
	 * @arguments [[Player]]:player, [[EntityItem]]:item
	 * @return [[RESULT]]:result
	 */

	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.pickupitem");
				LuaUserdata.PushUserdata(l, event.player);
				LuaUserdata.PushUserdata(l, event.pickedUp);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setResult(Result.values()[l.checkInteger(-1, Result.DEFAULT.ordinal())]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.smeltitem
	 * @info Called whenever a player smelts an item in a furnace
	 * @arguments [[Player]]:player, [[ItemStack]]:stack
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onItemSmelted(ItemSmeltedEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.smeltitem");
				LuaUserdata.PushUserdata(l, event.player);
				l.pushUserdataWithMeta(event.smelting, "ItemStack");
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

				l.setTop(0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.changedimension
	 * @info Called whenever a player attempts to go to a new dimension
	 * @arguments [[Player]]:player, [[Number]]:fromID, [[Number]]:toID
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.changedimension");
				LuaUserdata.PushUserdata(l, event.player);
				l.pushNumber(event.fromDim);
				l.pushNumber(event.toDim);
				l.call(4, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.connect
	 * @info Called when a player connects to the server
	 * @arguments [[Player]]:player
	 * @return nil
	 */

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.connect");
				LuaUserdata.PushUserdata(l, event.player);
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.disconnect
	 * @info Called when a player disconnects from the server
	 * @arguments [[Player]]:player
	 * @return nil
	 */

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.disconnect");
				LuaUserdata.PushUserdata(l, event.player);
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.spawned
	 * @info Called when a player spawns for the first time
	 * @arguments [[Player]]:player
	 * @return nil
	 */

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.spawned");
				LuaUserdata.PushUserdata(l, event.player);
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	// Tick Events (ClientTickEvent, PlayerTickEvent, RenderTickEvent,
	// ServerTickEvent, WorldTickEvent)

	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;

		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("game.tick");
				l.call(1, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;

		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("game.tick");
				l.call(1, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.START)
			return;

		synchronized (l) {
			if (!l.isOpen())
				return;

			if (l.getMinecraft().thePlayer == null)
				return;

			try {
				l.pushHookCall();
				l.pushString("render.tick");
				l.pushNumber(event.renderTickTime);
				l.pushNumber(event.phase.ordinal());
				l.call(3, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	// Minecraft Bus

	/**
	 * @author Jake
	 * @function player.say
	 * @info Called when a player types something in chat
	 * @arguments [[Player]]:player, [[String]]:message
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onServerChat(ServerChatEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.say");
				LuaUserdata.PushUserdata(l, event.player);
				l.pushString(event.message);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	// Brewing Events

	// TODO: PotionBrewedEvent

	// Item Events

	/**
	 * @author Jake
	 * @function item.expired
	 * @info Called when an item gets cleaned up from the world, after being on the ground for too long
	 * @arguments [[EntityItem]]:item
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onItemExpire(ItemExpireEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("item.expired");
				LuaUserdata.PushUserdata(l, event.entityItem);
				l.call(2, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.dropitem
	 * @info Called when a player drops an item on the ground
	 * @arguments [[Player]]:player, [[EntityItem]]:item
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.dropitem");
				LuaUserdata.PushUserdata(l, event.player);
				LuaUserdata.PushUserdata(l, event.entityItem);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	// Entity Events

	/**
	 * @author Jake
	 * @function entity.lightning
	 * @info Called when an entity is struck by lightning
	 * @arguments [[Entity]]:target, [[Entity]]:lightning
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.lightning");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.lightning);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.joinworld");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.world);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	// Living Events

	/**
	 * @author Jake
	 * @function entity.spawned
	 * @info Called when an entity attempts to spawn into the world
	 * @arguments [[Entity]]:entity
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingSpawned(LivingSpawnEvent.CheckSpawn event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.spawned");
				LuaUserdata.PushUserdata(l, event.entity);
				l.call(2, 1);

				if (!l.isNil(-1))
					event.setResult(Result.values()[l.checkInteger(-1, Result.DEFAULT.ordinal())]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.removed
	 * @info Called when an entity gets removed from the world
	 * @arguments [[Entity]]:entity
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingRemoved(LivingSpawnEvent.AllowDespawn event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.removed");
				LuaUserdata.PushUserdata(l, event.entity);
				l.call(2, 1);

				if (!l.isNil(-1))
					event.setResult(Result.values()[l.checkInteger(-1, Result.DEFAULT.ordinal())]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.attacked
	 * @info Called when an entity attacks another entity
	 * @arguments [[Entity]]:target, [[DamageSource]]:source, [[Number]]:damage
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.attacked");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushUserdataWithMeta(event.source, "DamageSource");
				l.pushNumber(event.ammount);
				l.call(4, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	// TODO: LivingHurtEvent (same as LivingAttackEvent)

	/**
	 * @author Jake
	 * @function entity.death
	 * @info Called when an entity is killed
	 * @arguments [[Entity]]:target, [[DamageSource]]:source, [[Number]]:damage
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.death");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushUserdataWithMeta(event.source, "DamageSource");
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.dropall
	 * @info Called when a entity dies and drops all their loot
	 * @arguments [[Entity]]:entity, [[Table]]:drops, [[Number]]:lootLevel, [[Boolean]]:hitRecent
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onEntityDrops(LivingDropsEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.dropall");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushUserdataWithMeta(event.source, "DamageSource");
				l.newTable();
				for (int i = 0; i < event.drops.size(); i++) {
					l.pushNumber(i + 1);
					LuaUserdata.PushUserdata(l, event.drops.get(i));
					l.setTable(-3);
				}
				l.pushNumber(event.lootingLevel);
				l.pushBoolean(event.recentlyHit);
				l.call(6, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.fall
	 * @info Called when an entity falls to the ground
	 * @arguments [[Entity]]:entity, [[Number]]:distance
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.fall");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushNumber(event.distance);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.jump
	 * @info Called when an entity falls to the ground
	 * @arguments [[Entity]]:entity, [[Number]]:distance
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.jump");
				LuaUserdata.PushUserdata(l, event.entity);
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function entity.update
	 * @info Called when an entity is updated
	 * @arguments [[Entity]]:entity
	 * @return nil
	 */

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.update");
				LuaUserdata.PushUserdata(l, event.entity);
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	// Player Events

	@SubscribeEvent
	public void onPlayerBonemeal(BonemealEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.bonemeal");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.world);
				LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.entityPlayer.worldObj, event.x, event.y, event.z));
				l.call(4, 1);

				if (!l.isNil(-1))
					event.setResult(Result.values()[l.checkInteger(-1, Result.DEFAULT.ordinal())]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.interact
	 * @info Called when a player attempts to interact with another entity
	 * @arguments [[Player]]:player, [[Entity]]:entity
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.interact");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.target);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.destroyitem
	 * @info Called when a player destroys an item in their inventory, normally when completing an action
	 * @arguments [[Player]]:player, [[ItemStack]]:original
	 * @return [[Boolean]]:cancel
	 */

	public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.destroyitem");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushUserdataWithMeta(event.original, "ItemStack");
				l.call(3, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.dropall
	 * @info Called when a player dies and drops all their loot
	 * @arguments [[Player]]:player, [[Table]]:drops, [[Number]]:lootLevel, [[Boolean]]:hitRecent
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onPlayerDrops(PlayerDropsEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.dropall");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushUserdataWithMeta(event.source, "DamageSource");
				l.newTable();
				for (int i = 0; i < event.drops.size(); i++) {
					l.pushNumber(i + 1);
					LuaUserdata.PushUserdata(l, event.drops.get(i));
					l.setTable(-3);
				}
				l.pushNumber(event.lootingLevel);
				l.pushBoolean(event.recentlyHit);
				l.call(6, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	// TODO: PlayerFlyableFallEvent

	/**
	 * @author Jake
	 * @function player.leftclick
	 * @info Called when a player hits a block
	 * @arguments [[Player]]:player, [[Block]]:block, [[Vector]]:normal
	 * @return [[Boolean]]:cancel
	 */

	/**
	 * @author Jake
	 * @function player.rightclick
	 * @info Called when a player attempts to interact with a block
	 * @arguments [[Player]]:player, [[Block]]:block, [[Vector]]:normal
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();

				switch (event.action) {
				case LEFT_CLICK_BLOCK:
					l.pushString("player.leftclick");
					break;
				case RIGHT_CLICK_AIR:
					l.pushString("player.rightclick");
					break;
				case RIGHT_CLICK_BLOCK:
					l.pushString("player.rightclick");
					break;
				}

				LuaUserdata.PushUserdata(l, event.entityPlayer);
				if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
					l.pushNil();
				else
					LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.entityPlayer.worldObj, event.x, event.y, event.z));
				l.pushFace(event.face);
				l.call(4, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.mineblock
	 * @info Called when a player breaks a block
	 * @arguments [[Player]]:player, [[Block]]:block, [[Number]]:exp
	 * @return [[Boolean]]:cancel or [[Number]]:exp
	 */

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.mineblock");
				LuaUserdata.PushUserdata(l, event.getPlayer());
				LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.getPlayer().worldObj, event.x, event.y, event.z));
				l.pushNumber(event.getExpToDrop());
				l.call(4, 1);

				if (l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.setExpToDrop(l.toInteger(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.placeblock
	 * @info Called when a player places a block
	 * @arguments [[Player]]:player, [[Block]]:block
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onBlockPlace(PlaceEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.placeblock");
				LuaUserdata.PushUserdata(l, event.player);
				LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.player.worldObj, event.x, event.y, event.z));
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.harvestblock
	 * @info Called when a player breaks a block and it is about to drop items
	 * @arguments [[Player]]:harvester, [[Block]]:harvested, [[number]]:drop chance, [[bool]]:silk touch, [[number]]:fortune level, [[table]]:drops
	 * @return [[number]]:set drop amount
	 */
	@SubscribeEvent
	public void onBlockHarvestDrop(BlockEvent.HarvestDropsEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.harvestblock");
				LuaUserdata.PushUserdata(l, event.harvester);
				LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.world, event.x, event.y, event.z));
				l.pushNumber(event.dropChance);
				l.pushBoolean(event.isSilkTouching);
				l.pushInteger(event.fortuneLevel);
				l.newTable();
				for(int i = 0; i < event.drops.size(); i++) {
					l.pushInteger(i + 1);
					LuaUserdata.PushUserdata(l, event.drops.get(i));
					l.setTable(-3);
				}
				l.call(7, 1);

				if (l.isNumber(-1))
					event.dropChance = (float) l.toNumber(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.opencontainer
	 * @info Called when a player attempts to open a container such as a chest
	 * @arguments [[Player]]:player, [[Boolean]]:interact
	 * @return [[RESULT]]:result
	 */

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerOpenContainerEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.opencontainer");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushBoolean(event.canInteractWith);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setResult(Result.values()[l.checkInteger(-1, Result.DEFAULT.ordinal())]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author Jake
	 * @function player.pickupxp
	 * @info Called when a player attempts to pickup XP orbs
	 * @arguments [[Player]]:player, [[Entity]]:orb
	 * @return [[Boolean]]:cancel
	 */

	@SubscribeEvent
	public void onPlayerPickupXP(PlayerPickupXpEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.pickupxp");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.orb);
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.onsleep
	 * @info Called when a player attempts to sleep in a bed
	 * @arguments [[Player]]:player, [[number]]:status, [[Vector]]:bedpos
	 * @return
	 */
	@SubscribeEvent
	public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.onsleep");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushInteger(event.result.ordinal());
				Vector vec = new Vector(event.x, event.y, event.z);
				vec.push(l);
				l.call(4, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.onusehoe
	 * @info Called when a player uses a hoe
	 * @arguments [[Player]]:player, [[World]]:world, [[ItemStack]]:stack, [[Vector]]:blockpos
	 * @return [[Boolean]]:cancel
	 */
	@SubscribeEvent
	public void onUseHoe(UseHoeEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.onusehoe");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.world);
				LuaUserdata.PushUserdata(l, event.current);
				Vector vec = new Vector(event.x, event.y, event.z);
				vec.push(l);
				l.call(5, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.achievment
	 * @info Called when a player earns an achievement
	 * @arguments [[Player]]:player, [[string]]:description
	 * @return [[Boolean]]:cancel
	 */
	@SubscribeEvent
	public void onAchievement(AchievementEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.achievment");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushString(event.achievement.getDescription());
				l.call(3, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.anvilrepair
	 * @info Called when a player repairs an anvil
	 * @arguments [[Player]]:player, [[ItemStack]]:left, [[ItemStack]]:right, [[ItemStack]]:output, [[number]]:break chance
	 * @return [[number]]:new break chance
	 */
	@SubscribeEvent
	public void onAnvilRepair(AnvilRepairEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.anvilrepair");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.left);
				LuaUserdata.PushUserdata(l, event.right);
				LuaUserdata.PushUserdata(l, event.output);
				l.pushNumber(event.breakChance);
				l.call(6, 1);

				if(l.isNumber(-1))
					event.breakChance = (float) l.toNumber(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.arrowloose
	 * @info Called when a player stops using a bow
	 * @arguments [[Player]]:player, [[World]]:world, [[ItemStack]]:bow, [[number]]:charge, [[bool]]:has ammo
	 * @return [[Boolean]]:cancel or [[number]]:new charge
	 */
	@SubscribeEvent
	public void onArrowLoose(ArrowLooseEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.arrowloose");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.entityPlayer.worldObj);
				LuaUserdata.PushUserdata(l, event.bow);
				l.pushInteger(event.charge);
				l.call(5, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.charge = l.toInteger(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.attackentity
	 * @info Called when a player attacks an entity
	 * @arguments [[Player]]:player, [[Entity]]:target
	 * @return [[bool]]:cancel
	 */
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.attackentity");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.target);
				l.call(3, 1);

				if(l.toBoolean(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.itempickup
	 * @info Called when a player picks up an item on the ground
	 * @arguments [[Player]]:player, [[ItemStack]]:item
	 * @return [[bool]]:cancel or [[number]]:result
	 */
	@SubscribeEvent
	public void onEntitiyItemPickup(EntityItemPickupEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.itempickup");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.item);
				l.call(3, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1) && isValidResult(l.toInteger(-1)))
					event.setResult(Result.values()[l.toInteger(-1)]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.fillbucket
	 * @info Called when a player picks up an item on the ground
	 * @arguments [[Player]]:player, [[World]]:world, [[ItemStack]]:emptybucket, [[ItemStack]]:filledbucket, [[table]]:trace
	 * @return [[bool]]:cancel or [[number]]:result
	 */
	@SubscribeEvent
	public void onFillBucket(FillBucketEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.fillbucket");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.world);
				LuaUserdata.PushUserdata(l, event.current);
				LuaUserdata.PushUserdata(l, event.result);
				l.newTable();
				{
					Vector vec = new Vector(event.target.hitVec);
					vec.push(l);
					l.setField(-2, "HitPos");

					LuaUserdata.PushUserdata(l, event.target.entityHit);
					l.setField(-2, "HitEntity");

					l.pushInteger(event.target.sideHit);
					l.setField(-2, "SideHit");

					LuaUserdata.PushUserdata(l, new LuaJavaBlock(event.world, event.target.blockX, event.target.blockY, event.target.blockZ));
					l.setField(-2, "HitBlock");
				}
				l.call(6, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1) && isValidResult(l.toInteger(-1)))
					event.setResult(Result.values()[l.toInteger(-1)]);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.tooltip
	 * @info Called when the items tool tip is requested
	 * @arguments [[Player]]:player, [[ItemStack]]:item, [[bool]]:advancedtip, [[table]]:tip
	 * @return
	 */
	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.tooltip");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				LuaUserdata.PushUserdata(l, event.itemStack);
				l.pushBoolean(event.showAdvancedItemTooltips);
				l.newTable();
				{
					for(int i = 0; i < event.toolTip.size(); i++) {
						l.pushInteger(i + 1);
						l.pushString(event.toolTip.get(i));
						l.setTable(-3);
					}
				}
				l.call(5, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.flyablefall
	 * @info Called when a player falls but can fly
	 * @arguments [[Player]]:player, [[number]]:distance, [[number]]:multiplier
	 * @return [[table]]:{distance=[[number]]:new distance, multiplier=[[number]]:new multiplier}
	 */
	@SubscribeEvent
	public void onPlayerFlyableFall(PlayerFlyableFallEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.flyablefall");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushNumber(event.distance);
				l.call(3, 1);

				if(l.isTable(-1)) {
					l.getField(-1, "distance");
					if(l.isNumber(-1))
						event.distance = (float)l.toNumber(-1);
					l.pop(1);
				}

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function player.wakeup
	 * @info Called when a player wakes up
	 * @arguments [[Player]]:player, [[bool]]:immediate wakeup, [[bool]]:update world, [[bool]]:should set spawn
	 * @return [[bool]]:cancel
	 */
	@SubscribeEvent
	public void onPlayerWakeUp(PlayerWakeUpEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("player.wakeup");
				LuaUserdata.PushUserdata(l, event.entityPlayer);
				l.pushBoolean(event.wakeImmediatly);
				l.pushBoolean(event.updateWorld);
				l.pushBoolean(event.setSpawn);
				l.call(5, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function chunk.load
	 * @info Called when a chunk is loaded
	 * @arguments [[Chunk]]:chunk
	 * @return
	 */
	@SubscribeEvent
	public void onChunkEventLoad(ChunkEvent.Load event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("chunk.load");
				l.pushUserdataWithMeta(event.getChunk(), "Chunk");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function chunk.unload
	 * @info Called when a chunk is unloaded
	 * @arguments [[Chunk]]:chunk
	 * @return
	 */
	@SubscribeEvent
	public void onChunkEventUnload(ChunkEvent.Unload event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("chunk.unload");
				l.pushUserdataWithMeta(event.getChunk(), "Chunk");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function chunk.loadfromdisk
	 * @info Called when a chunk is loaded
	 * @arguments [[Chunk]]:chunk
	 * @return
	 */
	@SubscribeEvent
	public void onChunkDataEventLoad(ChunkDataEvent.Load event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("chunk.loadfromdisk");
				l.pushUserdataWithMeta(event.getChunk(), "Chunk");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function chunk.savetodisk
	 * @info Called when a chunk is saved
	 * @arguments [[Chunk]]:chunk
	 * @return
	 */
	@SubscribeEvent
	public void onChunkDataEventSave(ChunkDataEvent.Save event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("chunk.savetodisk");
				l.pushUserdataWithMeta(event.getChunk(), "Chunk");
				l.call(2, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function block.preexplode
	 * @info Called when a block explodes
	 * @arguments [[World]]:world, [[Explosion]]:explosion
	 * @return [[boolean]]:cancel explosion
	 */
	@SubscribeEvent
	public void onExplosionEventStart(ExplosionEvent.Start event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("block.preexplode");
				LuaUserdata.PushUserdata(l, event.world);
				l.pushUserdataWithMeta(event.explosion, "Explosion");
				l.call(3, 1);

				if (!l.isNil(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function block.postexplode
	 * @info Called when a block explodes
	 * @arguments [[World]]:world, [[Explosion]]:explosion, [[table]]:affected entities
	 * @return
	 */
	@SubscribeEvent
	public void onExplosionEventDetnoate(ExplosionEvent.Detonate event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("block.postexplode");
				LuaUserdata.PushUserdata(l, event.world);
				l.pushUserdataWithMeta(event.explosion, "Explosion");
				l.newTable();
				{
					for(int i = 0; i < event.getAffectedEntities().size(); i++) {
						l.pushInteger(i + 1);
						LuaUserdata.PushUserdata(l, event.getAffectedEntities().get(i));
						l.setTable(-3);
					}
				}
				l.call(4, 0);
			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function item.prebrew
	 * @info Called before brewing takes place
	 * @arguments [[table]]:brew items
	 * @return [[bool]]:cancel
	 */
	@SubscribeEvent
	public void onPotionBrewPre(PotionBrewEvent.Pre event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("item.prebrew");
				l.newTable();
				{
					for(int i = 0; i < event.getLength(); i++) {
						l.pushInteger(i + 1);
						LuaUserdata.PushUserdata(l, event.getItem(i));
						l.setTable(-3);
					}
				}
				l.call(2, 1);

				if (l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function item.postbrew
	 * @info Called after brewing takes place
	 * @arguments [[table]]:brew items
	 * @return
	 */
	@SubscribeEvent
	public void onPotionBrewPost(PotionBrewEvent.Post event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("item.postbrew");
				l.newTable();
				{
					for(int i = 0; i < event.getLength(); i++) {
						l.pushInteger(i + 1);
						LuaUserdata.PushUserdata(l, event.getItem(i));
						l.setTable(-3);
					}
				}
				l.call(2, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.enderteleport
	 * @info Called when ender teleport is used
	 * @arguments [[Entity]]:entity, [[Vector]]:targetpos, [[number]]:attack dmg
	 * @return [[bool]]:cancel or [[table]]:{newpos=[[Vector]]:new tp pos, damage=[[number]]:attack dmg}
	 */
	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.enderteleport");
				LuaUserdata.PushUserdata(l, event.entity);
				Vector vec = new Vector(event.targetX, event.targetY, event.targetZ);
				vec.push(l);
				l.pushNumber(event.attackDamage);
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isTable(-1)) {
					l.getField(-1, "newpos");
					if(l.isUserdata(-1, Vector.class)) {
						Vector newPos = (Vector)l.toUserdata(-1);
						event.targetX = newPos.x;
						event.targetY = newPos.z;
						event.targetZ = newPos.y;
					}
					l.pop(1);
					l.getField(-1, "damage");
					if(l.isNumber(-1))
						event.attackDamage = (float)l.toNumber(-1);
					l.pop(1);
				}

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.startuseitem
	 * @info Called when entity starts using an item
	 * @arguments [[Entity]]:entity, [[ItemStack]]:item using, [[number]]:duration
	 * @return [[bool]]:cancel or [[number]]:new duration
	 */
	@SubscribeEvent
	public void onPlayerUseItemStart(PlayerUseItemEvent.Start event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.startuseitem");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.item);
				l.pushInteger(event.duration);
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.duration = l.toInteger(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.useitemtick
	 * @info Called while entity is using an item
	 * @arguments [[Entity]]:entity, [[ItemStack]]:item using, [[number]]:duration
	 * @return [[bool]]:cancel or [[number]]:new duration
	 */
	@SubscribeEvent
	public void onPlayerUseItemTick(PlayerUseItemEvent.Tick event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.useitemtick");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.item);
				l.pushInteger(event.duration);
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.duration = l.toInteger(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.stopuseitem
	 * @info Called when a entity stops using an item before duration is up
	 * @arguments [[Entity]]:entity, [[ItemStack]]:item using, [[number]]:duration
	 * @return [[bool]]:cancel or [[number]]:new duration
	 */
	@SubscribeEvent
	public void onPlayerUseItemStop(PlayerUseItemEvent.Stop event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.stopuseitem");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.item);
				l.pushInteger(event.duration);
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.duration = l.toInteger(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.finisheduseitem
	 * @info Called when a entity uses an item
	 * @arguments [[Entity]]:entity, [[ItemStack]]:item using, [[number]]:duration, [[ItemStack]]:result stack
	 * @return [[ItemStack]]:new result stack
	 */
	@SubscribeEvent
	public void onPlayerUseItemFinish(PlayerUseItemEvent.Finish event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.finisheduseitem");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.item);
				l.pushInteger(event.duration);
				LuaUserdata.PushUserdata(l, event.result);
				l.call(5, 1);

				if(l.isUserdata(-1, ItemStack.class))
					event.result = (ItemStack) l.toUserdata(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.heal
	 * @info Called when a entity heals
	 * @arguments [[Entity]]:entity, [[number]]:amount
	 * @return [[bool]]:cancel or [[number]]:new amount
	 */
	@SubscribeEvent
	public void onLivingHealEvent(LivingHealEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.heal");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushNumber(event.amount);
				l.call(3, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.amount = (float)l.toNumber(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.hurt
	 * @info Called when a entity hurts
	 * @arguments [[Entity]]:entity, [[number]]:amount, [[DamageSource]]:source
	 * @return [[bool]]:cancel or [[number]]:new amount
	 */
	@SubscribeEvent
	 public void onLivingHurtEvent(LivingHurtEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.hurt");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushNumber(event.ammount);
				l.pushUserdataWithMeta(event.source, "DamageSource");
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));
				else if(l.isNumber(-1))
					event.ammount = (float) l.toNumber(-1);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.packsize
	 * @info Called when spawner determines the max amount of entities to spawn
	 * @arguments [[Entity]]:entity, [[number]]:max size
	 * @return [[table]]:{maxsize=[[number]]:new max size, result=[[number]]:result}
	 */
	@SubscribeEvent
	public void onLivingPackSize(LivingPackSizeEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.packsize");
				LuaUserdata.PushUserdata(l, event.entity);
				l.pushInteger(event.maxPackSize);
				l.call(3, 1);

				if(l.isTable(-1)) {
					l.getField(-1, "maxsize");
					if(l.isNumber(-1))
						event.maxPackSize = l.toInteger(-1);
					l.pop(1);
					l.getField(-1, "result");
					if(l.isNumber(-1) && isValidResult(l.toInteger(-1)))
						event.setResult(Result.values()[l.toInteger(-1)]);
					l.pop(1);
				}

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.settarget
	 * @info Called when a entity sets its attack target
	 * @arguments [[Entity]]:entity, [[Entity]]:target
	 * @return
	 */
	@SubscribeEvent
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.settarget");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.target);
				l.call(3, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function entity.specialspawn
	 * @info Called when a entity spawns from a mob spawner
	 * @arguments [[Entity]]:entity, [[World]]:world, [[Vector]]:spawn pos
	 * @return [[bool]]:cancel
	 */
	@SubscribeEvent
	public void onLivingSpawnSpecial(LivingSpawnEvent.SpecialSpawn event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.specialspawn");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.world);
				Vector vec = new Vector(event.x, event.z, event.y);
				vec.push(l);
				l.call(4, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * TODO: test this
	 * @author fr1kin
	 * @function entity.summonaid
	 * @info Called when a zombie attempts to summon other zombies into the world
	 * @arguments [[Entity]]:entity, [[World]]:world, [[Entity]]:summoner, [[Entity]]:custom summoner, [[Entity]]:attacker, [[Vector]]:summoning pos
	 * @return [[table]]:{custom_summoner=[[Entity]]:new summoner, result=[[number]]:result}
	 */
	@SubscribeEvent
	public void onZombieSummonAid(ZombieEvent.SummonAidEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("entity.summonaid");
				LuaUserdata.PushUserdata(l, event.entity);
				LuaUserdata.PushUserdata(l, event.world);
				LuaUserdata.PushUserdata(l, event.getSummoner());
				LuaUserdata.PushUserdata(l, event.customSummonedAid);
				LuaUserdata.PushUserdata(l, event.attacker);
				Vector vec = new Vector(event.x, event.z, event.y);
				vec.push(l);
				l.pushNumber(event.summonChance);
				l.call(8, 1);

				if(l.isTable(-1)) {
					l.getField(-1, "custom_summoner");
					if(l.isUserdata(-1, EntityLiving.class))
						event.customSummonedAid = (EntityZombie)l.toUserdata(-1);
					l.pop(1);
					l.getField(-1, "result");
					if(l.isNumber(-1) && isValidResult(l.toInteger(-1)))
						event.setResult(Result.values()[l.toInteger(-1)]);
					l.pop(1);
				}

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function minecart.collision
	 * @info Called when a minecart collides with a entity
	 * @arguments [[Entity]]:minecart, [[Entity]]:collider
	 * @return
	 */
	@SubscribeEvent
	public void onMinecartCollision(MinecartCollisionEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("minecart.collision");
				LuaUserdata.PushUserdata(l, event.minecart);
				LuaUserdata.PushUserdata(l, event.collider);
				l.call(3, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function minecart.interact
	 * @info Called when a player interacts with a minecart
	 * @arguments [[Entity]]:minecart, [[Player]]:player, [[ItemStack]]:item, [[number]]:hand
	 * @return [[bool]]:cancel
	 */
	@SubscribeEvent
	public void onMinecartInteract(MinecartInteractEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("minecart.interact");
				LuaUserdata.PushUserdata(l, event.minecart);
				LuaUserdata.PushUserdata(l, event.player);
				l.call(3, 1);

				if(l.isBoolean(-1))
					event.setCanceled(l.toBoolean(-1));

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function minecart.update
	 * @info Called when a minecart is updated
	 * @arguments [[Entity]]:minecart, [[Vector]]:minecart pos
	 * @return
	 */
	@SubscribeEvent
	public void onMinecartUpdate(MinecartUpdateEvent event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("minecart.update");
				LuaUserdata.PushUserdata(l, event.minecart);
				Vector vec = new Vector(event.x, event.y, event.z);
				vec.push(l);
				l.call(3, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function world.load
	 * @info Called when a world is loaded
	 * @arguments [[World]]:world
	 * @return
	 */
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("world.load");
				LuaUserdata.PushUserdata(l, event.world);
				l.call(2, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function world.unload
	 * @info Called when a world is unloaded
	 * @arguments [[World]]:world
	 * @return
	 */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("world.unload");
				LuaUserdata.PushUserdata(l, event.world);
				l.call(2, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}

	/**
	 * @author fr1kin
	 * @function world.save
	 * @info Called when a world is saved
	 * @arguments [[World]]:world
	 * @return
	 */
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		synchronized (l) {
			if (!l.isOpen())
				return;

			try {
				l.pushHookCall();
				l.pushString("world.save");
				LuaUserdata.PushUserdata(l, event.world);
				l.call(2, 0);

			} catch (LuaRuntimeException e) {
				l.handleLuaError(e);
			} finally {
				l.setTop(0);
			}
		}
	}
}
