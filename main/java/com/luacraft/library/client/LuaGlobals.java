package com.luacraft.library.client;

import net.minecraft.client.Minecraft;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdataManager;
import com.luacraft.classes.LuaJavaBlock;
import com.luacraft.classes.Vector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

public class LuaGlobals
{
	private static Minecraft client = null;

	/**
	 * @author Gregor
	 * @function Block
	 * Get the Block at specified coordinates
	 * @arguments nil
	 * @return [[Block]]:block
	 */

	public static JavaFunction Block = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			int x,y,z;
			
			if (l.isUserdata(1, Vector.class))
			{
				Vector thisVec = (Vector) l.checkUserdata(1, Vector.class, "Vector");
				x = (int) Math.floor(thisVec.x);
				y = (int) Math.floor(thisVec.y);
				z = (int) Math.floor(thisVec.z);
			}
			else
			{
				x = (int) Math.floor(l.checkNumber(1, 0));
				y = (int) Math.floor(l.checkNumber(2, 0));
				z = (int) Math.floor(l.checkNumber(3, 0));
			}
			
			LuaJavaBlock thisBlock = new LuaJavaBlock(client.theWorld, x, z, y);
			LuaUserdataManager.PushUserdata(l, thisBlock);
			return 1;
		}
	};

	/**
	 * @author Jake
	 * @function World
	 * Get the world that the local player is currently in
	 * @arguments nil
	 * @return [[World]]:world
	 */

	public static JavaFunction World = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			LuaUserdataManager.PushUserdata(l, client.theWorld);
			return 1;
		}
	};

	/**
	 * @author Matt
	 * @function ScrW
	 * Get the screen width
	 * @arguments nil
	 * @return [[Number]]:width
	 */

	public static JavaFunction ScrW = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			l.pushNumber(client.displayWidth);
			return 1;
		}
	};

	/**
	 * @author Matt
	 * @function ScrH
	 * Get the screen height
	 * @arguments nil
	 * @return [[Number]]:height
	 */

	public static JavaFunction ScrH = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			l.pushNumber(client.displayHeight);
			return 1;
		}
	};

	/**
	 * @author Matt
	 * @function LocalPlayer
	 * Get your local player object
	 * @arguments nil
	 * @return [[Player]]:localplayer
	 */

	public static JavaFunction LocalPlayer = new JavaFunction()
	{
		public int invoke(LuaState l)
		{
			LuaUserdataManager.PushUserdata(l, client.thePlayer);
			return 1;
		}
	};

	public static void Init(final LuaCraftState l)
	{
		client = l.getMinecraft();

		l.pushJavaFunction(Block);
		l.setGlobal("Block");

		l.pushJavaFunction(World);
		l.setGlobal("World");

		l.pushJavaFunction(ScrW);
		l.setGlobal("ScrW");

		l.pushJavaFunction(ScrH);
		l.setGlobal("ScrH");

		l.pushJavaFunction(LocalPlayer);
		l.setGlobal("LocalPlayer");
	}
}