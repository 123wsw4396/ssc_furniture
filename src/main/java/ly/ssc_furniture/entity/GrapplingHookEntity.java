package ly.ssc_furniture.entity;

import ly.ssc_furniture.server.HookRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrapplingHookEntity extends ThrowableProjectile {

    public static final double STOP_DISTANCE = 1.2;
    public static final double GRAVITY = 0.08;
    public static final double TANGENT_DAMPING = 0.99;
    public static final double MAX_TANGENT_SPEED = 1.5;

    private static final EntityDataAccessor<Boolean> DATA_MODE_B =
            SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_HIT_FACE =
            SynchedEntityData.defineId(GrapplingHookEntity.class, EntityDataSerializers.BYTE);

    private double maxDistance = 30.0;
    private double pullSpeed = 0.5;
    private double bModeSpeed = 0.01;

    private Vec3 stuckTarget;
    private int stuckEntityId;
    private double currentLength = -1;
    private double initialLength = -1;
    private boolean sneakReleasedOnce = false;
    private byte grappleInput = 0;
    private boolean novice = false;
    private boolean needsNoviceKick = false;

    public GrapplingHookEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public GrapplingHookEntity(Level level, LivingEntity owner) {
        super(ModEntities.GRAPPLING_HOOK, owner, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_MODE_B, false);
        this.entityData.define(DATA_HIT_FACE, (byte) -1);
    }

    public void setMaxDistance(double v) { this.maxDistance = v; }
    public void setPullSpeed(double v) { this.pullSpeed = v; }
    public double getPullSpeed() { return this.pullSpeed; }
    public void setBModeSpeed(double v) { this.bModeSpeed = v; }
    public double getBModeSpeed() { return this.bModeSpeed; }

    public void setModeB(boolean modeB) { this.entityData.set(DATA_MODE_B, modeB); }
    public boolean isModeB() { return this.entityData.get(DATA_MODE_B); }

    public void setHitFace(Direction dir) {
        this.entityData.set(DATA_HIT_FACE, dir == null ? (byte) -1 : (byte) dir.get3DDataValue());
    }
    public Direction getHitFace() {
        byte b = this.entityData.get(DATA_HIT_FACE);
        return b < 0 ? null : Direction.from3DDataValue(b);
    }

    public void setGrappleInput(byte state) { this.grappleInput = state; }
    public byte getGrappleInput() { return this.grappleInput; }

    public double getCurrentLength() { return this.currentLength; }
    public void setCurrentLength(double v) { this.currentLength = v; }
    public double getInitialLength() { return this.initialLength; }
    public void setInitialLength(double v) { this.initialLength = v; }

    public boolean isStuck() { return isNoGravity(); }

    public void setNovice(boolean b) { this.novice = b; }
    public boolean isNovice() { return this.novice; }

    public boolean consumeNeedsNoviceKick() {
        if (needsNoviceKick) { needsNoviceKick = false; return true; }
        return false;
    }

    /** 返回当前 pivot: 若钩到实体则跟随实体, 否则用 stuckTarget. null = 无效. */
    public Vec3 getPivot() {
        if (stuckEntityId != 0) {
            Entity e = level().getEntity(stuckEntityId);
            if (e == null || !e.isAlive()) return null;
            Vec3 p = e.position().add(0, e.getBbHeight() / 2, 0);
            stuckTarget = p;
            return p;
        }
        return stuckTarget;
    }

    public int getStuckEntityId() { return stuckEntityId; }

    @Override
    public void tick() {
        if (isStuck()) {
            // 生命周期检查; 物理由 HookController 统一处理
            if (!level().isClientSide) {
                handleStuckLifecycle();
            }
            return;
        }
        super.tick();
        if (!level().isClientSide && distanceToOwner() > maxDistance) {
            discard();
        }
    }

    private void handleStuckLifecycle() {
        if (!(getOwner() instanceof Player owner) || !owner.isAlive()) {
            discard();
            return;
        }

        // shift 断线: 命中时按着 shift 不算按下, 必须先松开一次
        if (!owner.isShiftKeyDown()) {
            sneakReleasedOnce = true;
        } else if (sneakReleasedOnce) {
            discard();
            return;
        }

        Vec3 pivot = getPivot();
        if (pivot == null) {
            discard();
            return;
        }

        if (owner.position().distanceTo(pivot) > maxDistance) {
            discard();
            return;
        }

        // 每 tick 把 hook 视觉位置贴到 pivot (跟随实体)
        setPos(pivot);
    }

    private double distanceToOwner() {
        Entity owner = getOwner();
        return owner != null ? distanceTo(owner) : Double.MAX_VALUE;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (level().isClientSide) return;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            stuckTarget = blockHit.getLocation();
            setHitFace(blockHit.getDirection());
            setStuck();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (level().isClientSide) return;

        Entity target = entityHitResult.getEntity();
        if (target instanceof LivingEntity && target != getOwner()) {
            stuckEntityId = target.getId();
            stuckTarget = target.position().add(0, target.getBbHeight() / 2, 0);
            setStuck();
        }
    }

    private void setStuck() {
        setNoGravity(true);
        setDeltaMovement(0, 0, 0);
        currentLength = -1;
        initialLength = -1;

        if (getOwner() instanceof Player owner) {
            sneakReleasedOnce = !owner.isShiftKeyDown();
            if (novice && isModeB()) needsNoviceKick = true;
            HookRegistry.addStuck(owner, this);
        } else {
            sneakReleasedOnce = true;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && getOwner() instanceof Player owner) {
            HookRegistry.remove(owner, this.getId());
        }
        super.remove(reason);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 4096.0;
    }
}
