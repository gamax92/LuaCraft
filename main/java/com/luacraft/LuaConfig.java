package com.luacraft;

import com.luacraft.console.ConsoleManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class LuaConfig
{
    public Configuration config;

    public ConfigCategory general;

    public Property developerConsole;
    public Property scriptEnforcer;

    public LuaConfig(File file)
    {
        config = new Configuration(file);
        config.load();

        // General
        general = config.getCategory(Configuration.CATEGORY_GENERAL);

        developerConsole = config.get(general.getName(), "developer-console", true, "Developer console for LuaCraft");
        scriptEnforcer = config.get(general.getName(), "script-enforcer", true, "Prevent clients from running their own Lua scripts");

        save();
    }

    public void save()
    {
        if(config.hasChanged()) config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.modID.equals(LuaCraft.MODID)) {
            save();
            ConsoleManager.onConfigChange();
        }
    }
}
