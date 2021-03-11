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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.world.LightType;


@Environment(EnvType.CLIENT)
public class AutotorchClient implements ClientModInitializer {
    private MinecraftClient client;

    @Override
    public void onInitializeClient() {
        this.client = MinecraftClient.getInstance();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

    }
    public void tick(MinecraftClient client) {
        if(client.player != null && client.world != null) {
            if (client.player.getOffHandStack().getItem() != Items.TORCH) return;
            BlockPos PlayerBlock = new BlockPos(client.player.getPos());
            if (client.world.getLightLevel(LightType.BLOCK, PlayerBlock) <= 7 && canPlaceTorch(PlayerBlock)) {
                offHandRightClickBlock(PlayerBlock);
            }
        }
    }
    private boolean offHandRightClickBlock(BlockPos pos) {
        Vec3d hitVec = Vec3d.ofBottomCenter(pos);
        ActionResult one = client.interactionManager.interactBlock(client.player, client.world, Hand.OFF_HAND,
                new BlockHitResult(hitVec, Direction.DOWN, pos, false));
        ActionResult two = client.interactionManager.interactItem(client.player, client.world, Hand.OFF_HAND);
        return (one.isAccepted() && two.isAccepted());
    }
    public boolean canPlaceTorch(BlockPos pos) {
        return (client.world.getBlockState(pos).getFluidState().isEmpty() &&
                Block.sideCoversSmallSquare(client.world, pos.down(), Direction.UP));
    }
}
