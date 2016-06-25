package com.luacraft.meta;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdata;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;

public class LuaBiomeGenBase {
    public static JavaFunction __tostring = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushString(biomeGenBase.biomeName);
            return 1;
        }
    };

    public static JavaFunction HasWeather = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushBoolean(biomeGenBase.canSpawnLightningBolt() || biomeGenBase.func_150559_j());
            return 1;
        }
    };

    public static JavaFunction IsRainy = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushBoolean(biomeGenBase.canSpawnLightningBolt());
            return 1;
        }
    };

    public static JavaFunction IsSnowy = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushBoolean(biomeGenBase.func_150559_j());
            return 1;
        }
    };

    public static JavaFunction IsHighHumidity = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushBoolean(biomeGenBase.isHighHumidity());
            return 1;
        }
    };

    public static JavaFunction IsMutation = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushBoolean(biomeGenBase instanceof BiomeGenMutated);
            return 1;
        }
    };

    public static JavaFunction RainfallLevel = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushNumber(biomeGenBase.rainfall);
            return 1;
        }
    };

    public static JavaFunction Temperature = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushNumber(biomeGenBase.temperature);
            return 1;
        }
    };

    public static JavaFunction HeightVariation = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushNumber(biomeGenBase.heightVariation);
            return 1;
        }
    };

    public static JavaFunction SpawningChance = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushNumber(biomeGenBase.getSpawningChance());
            return 1;
        }
    };

    public static JavaFunction Name = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushString(biomeGenBase.biomeName);
            return 1;
        }
    };

    public static JavaFunction BaseHeight = new JavaFunction() {
        public int invoke(LuaState l) {
            BiomeGenBase biomeGenBase = (BiomeGenBase)(l.checkUserdata(-1, BiomeGenBase.class, "BiomeGenBase"));
            l.pushNumber(biomeGenBase.rootHeight);
            return 1;
        }
    };

    public static void Init(final LuaCraftState l) {
        l.newMetatable("BiomeGenBase");
        {
            l.pushJavaFunction(__tostring);
            l.setField(-2, "__tostring");

            LuaUserdata.SetupBasicMeta(l);
            LuaUserdata.SetupMeta(l, true);

            l.newMetatable("Object");
            l.setField(-2, "__basemeta");

            l.pushJavaFunction(HasWeather);
            l.setField(-2, "HasWeather");
            l.pushJavaFunction(IsRainy);
            l.setField(-2, "IsRainy");
            l.pushJavaFunction(IsSnowy);
            l.setField(-2, "IsSnowy");
            l.pushJavaFunction(IsHighHumidity);
            l.setField(-2, "IsHighHumidity");
            l.pushJavaFunction(IsMutation);
            l.setField(-2, "IsMutation");
            l.pushJavaFunction(RainfallLevel);
            l.setField(-2, "RainfallLevel");
            l.pushJavaFunction(Temperature);
            l.setField(-2, "Temperature");
            l.pushJavaFunction(HeightVariation);
            l.setField(-2, "HeightVariation");
            l.pushJavaFunction(SpawningChance);
            l.setField(-2, "SpawningChance");
            l.pushJavaFunction(Name);
            l.setField(-2, "Name");
            l.pushJavaFunction(BaseHeight);
            l.setField(-2, "BaseHeight");
        }
        l.pop(1);
    }
}
