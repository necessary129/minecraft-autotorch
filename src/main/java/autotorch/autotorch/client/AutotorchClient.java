//Autotorch: A fabric mod to automatically place torches in offhand
//Copyright (C) 2021 Shamil K

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Lesser General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.

//You should have received a copy of the GNU Lesser General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.

package autotorch.autotorch.client;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.Block;
import net.minecraft.world.LightType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;



@Environment(EnvType.CLIENT)
public class AutotorchClient implements ClientModInitializer {
    private MinecraftClient client;
    public ConfigHolder<ModConfig> CONFIG;
    private ModConfig CDATA;
    static final ImmutableSet<Item> TorchSet = ImmutableSet.of(Items.TORCH, Items.SOUL_TORCH);

    @Override
    public void onInitializeClient() {
        this.client = MinecraftClient.getInstance();
        CONFIG = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        CDATA = CONFIG.getConfig();
        CONFIG.registerLoadListener((manager, data) -> {
            CDATA = data;
            return ActionResult.SUCCESS;
        });
    }
    public void tick(MinecraftClient client) {
        if (!CDATA.enabled) return;
        if(client.player != null && client.world != null) {
            if (!TorchSet.contains(client.player.getOffHandStack().getItem())) return;
            BlockPos PlayerBlock = new BlockPos(client.player.getPos());
            if (client.world.getLightLevel(LightType.BLOCK, PlayerBlock) < CDATA.lightLevel && canPlaceTorch(PlayerBlock)) {
                offHandRightClickBlock(PlayerBlock);
            }
        }
    }
    private boolean offHandRightClickBlock(BlockPos pos) {
        Vec3d hitVec = Vec3d.ofBottomCenter(pos);
        if (CDATA.accuratePlacement) {
            PlayerMoveC2SPacket.LookAndOnGround packet = new PlayerMoveC2SPacket.LookAndOnGround(client.player.getYaw(), 90.0F, true);
            client.player.networkHandler.sendPacket(packet);
        }
        ActionResult one = client.interactionManager.interactBlock(client.player,Hand.OFF_HAND,
                new BlockHitResult(hitVec, Direction.DOWN, pos, false));
        ActionResult two = client.interactionManager.interactItem(client.player,Hand.OFF_HAND);
        return (one.isAccepted() && two.isAccepted());
    }
    public boolean canPlaceTorch(BlockPos pos) {
        return (client.world.getBlockState(pos).getFluidState().isEmpty() &&
                Block.sideCoversSmallSquare(client.world, pos.down(), Direction.UP));
    }
}
