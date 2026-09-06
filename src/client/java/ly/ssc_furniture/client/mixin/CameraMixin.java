package ly.ssc_furniture.client.mixin;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.WaterbedBlock;
import ly.ssc_furniture.client.render.bathtub.FormCheck;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Accessor("entity")
    abstract Entity ssc_furniture$getEntity();

    @Accessor("position")
    abstract Vec3 ssc_furniture$getPosition();

    @Invoker("setPosition")
    abstract void ssc_furniture$invokeSetPosition(Vec3 pos);

    @Invoker("setRotation")
    abstract void ssc_furniture$invokeSetRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void ssc_furniture$adjustBathtubSleepCamera(BlockGetter level, Entity entity, boolean detached,
                                                        boolean thirdPersonReverse, float partialTicks, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        if (!player.isSleeping()) return;
        if (!FormCheck.isAnyAxolotlSleepPoseEnabled(player)) return;
        Optional<BlockPos> posOpt = player.getSleepingPos();
        if (posOpt.isEmpty()) return;
        BlockPos sleepPos = posOpt.get();
        BlockState bs = player.level().getBlockState(sleepPos);
        boolean isBathtub = bs.getBlock() instanceof BathtubBedBlock;
        boolean isWaterbed = bs.getBlock() instanceof WaterbedBlock;
        if (!isBathtub && !isWaterbed) return;
        // tier 4 睡姿只在浴缸床上生效; tier 3 睡姿在浴缸床和水床上都生效
        if (!isBathtub && !FormCheck.isAxolotl2SleepPoseEnabled(player)) return;

        Direction facing = bs.getValue(BedBlock.FACING);
        BlockPos footPos;
        if (bs.getValue(BedBlock.PART) == BedPart.FOOT) {
            footPos = sleepPos;
        } else {
            footPos = sleepPos.relative(facing.getOpposite());
        }

        // 浴缸床两格几何中心 (FOOT 和 HEAD 中间)
        double centerX = footPos.getX() + 0.5 + 0.5 * facing.getStepX();
        double centerY = footPos.getY() + 0.5;
        double centerZ = footPos.getZ() + 0.5 + 0.5 * facing.getStepZ();

        if (!detached) {
            // 第一人称: 在原有 +0.3 基础上再 +0.2, 共 +0.5
            Vec3 cur = ssc_furniture$getPosition();
            ssc_furniture$invokeSetPosition(cur.add(0.0, 0.5, 0.0));
            return;
        }

        if (thirdPersonReverse) {
            // "第二人称" (F5 前视): 浴缸床正上方 3 格, 俯视对着模型
            double camX = centerX;
            double camY = centerY + 3.0;
            double camZ = centerZ;
            ssc_furniture$invokeSetPosition(new Vec3(camX, camY, camZ));
            // 俯视: pitch=90 (向下), yaw 让美西螈头部朝屏幕上方 (yaw 对齐 facing 相反方向, 使屏幕上=床头方向)
            float yaw = facing.getOpposite().toYRot();
            ssc_furniture$invokeSetRotation(yaw, 90.0f);
            return;
        }

        // 第三人称 (F5 背视): 浴缸床"后上方" 3 格
        // "后" = 床头方向后侧 (即从美西螈头顶朝床头方向再往外), 摄像机在 HEAD 一侧再往外, 俯视回看
        double dx = facing.getStepX() * 3.0;
        double dz = facing.getStepZ() * 3.0;
        double camX = centerX + dx;
        double camY = centerY + 2.5;
        double camZ = centerZ + dz;
        ssc_furniture$invokeSetPosition(new Vec3(camX, camY, camZ));

        // 看向浴缸中心
        double lookDx = centerX - camX;
        double lookDy = centerY - camY;
        double lookDz = centerZ - camZ;
        double horiz = Math.sqrt(lookDx * lookDx + lookDz * lookDz);
        float yaw = (float) (Mth.atan2(lookDz, lookDx) * (180.0 / Math.PI) - 90.0);
        float pitch = (float) (-Mth.atan2(lookDy, horiz) * (180.0 / Math.PI));
        ssc_furniture$invokeSetRotation(Mth.wrapDegrees(yaw), Mth.wrapDegrees(pitch));
    }
}
