package com.luacraft.meta;

import com.luacraft.LuaCraftState;
import com.luacraft.LuaUserdata;
import com.luacraft.classes.Vector;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;

import java.util.Map;

public class LuaExplosion {
    public static JavaFunction __tostring = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            l.pushString(String.format("Explosion [%.2f, %.2f, %.2f]",
                    self.explosionX,
                    self.explosionZ,
                    self.explosionY));
            return 1;
        }
    };

    public static JavaFunction Explode = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            boolean spawnParticles = l.checkBoolean(2, true);
            self.doExplosionA();
            self.doExplosionB(spawnParticles);
            return 0;
        }
    };

    public static JavaFunction GetEntityPlacedBy = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            LuaUserdata.PushUserdata(l, self.getExplosivePlacedBy());
            return 1;
        }
    };

    public static JavaFunction GetKnockedBackedPlayers = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            l.newTable();
            {
                for(Map.Entry<EntityPlayer, Vec3> entry : ((Map<EntityPlayer, Vec3>) self.func_77277_b()).entrySet()) {
                    LuaUserdata.PushUserdata(l, entry.getKey());
                    Vector vec = new Vector(entry.getValue().xCoord, entry.getValue().zCoord, entry.getValue().yCoord);
                    vec.push(l);
                    l.setTable(-3);
                }
            }
            return 1;
        }
    };

    public static JavaFunction GetAffectedBlocks = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            l.newTable();
            {
                for(int i = 0; i < self.affectedBlockPositions.size(); i++) {
                    l.pushInteger(i + 1);
                    ChunkPosition pos = (ChunkPosition) self.affectedBlockPositions.get(i);
                    Vector vec = new Vector(pos.chunkPosX, pos.chunkPosZ, pos.chunkPosY);
                    vec.push(l);
                    l.setTable(-3);
                }
            }
            return 1;
        }
    };

    public static JavaFunction GetPos = new JavaFunction() {
        public int invoke(LuaState l) {
            Explosion self = (Explosion) l.checkUserdata(1, Explosion.class, "Explosion");
            Vector vec = new Vector(self.explosionX, self.explosionZ, self.explosionY);
            vec.push(l);
            return 1;
        }
    };

    public static void Init(final LuaCraftState l) {
        l.newMetatable("Explosion");
        {
            l.pushJavaFunction(__tostring);
            l.setField(-2, "__tostring");

            LuaUserdata.SetupBasicMeta(l);
            LuaUserdata.SetupMeta(l, true);

            l.newMetatable("Object");
            l.setField(-2, "__basemeta");

            l.pushJavaFunction(Explode);
            l.setField(-2, "Explode");
            l.pushJavaFunction(GetEntityPlacedBy);
            l.setField(-2, "GetEntityPlacedBy");
            l.pushJavaFunction(GetKnockedBackedPlayers);
            l.setField(-2, "GetKnockedBackedPlayers");
            l.pushJavaFunction(GetAffectedBlocks);
            l.setField(-2, "GetAffectedBlocks");
            l.pushJavaFunction(GetPos);
            l.setField(-2, "GetPos");
        }
        l.pop(1);
    }
}
