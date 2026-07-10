package ly.ssc_furniture.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrapplingHookEntity extends ThrowableProjectile {

    private double maxDistance = 30.0;
    private double pullSpeed = 0.5;
    private static final double STOP_DISTANCE = 1.2;

    public void setMaxDistance(double v) { this.maxDistance = v; }
    public void setPullSpeed(double v) { this.pullSpeed = v; }

    private Vec3 stuckTarget;
    private int stuckEntityId;

    public GrapplingHookEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public GrapplingHookEntity(Level level, LivingEntity owner) {
        super(ModEntities.GRAPPLING_HOOK, owner, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        if (isStuck()) {
            if (!level().isClientSide) {
                handleStuck();
            }
            return;
        }

        super.tick();

        if (!level().isClientSide && distanceToOwner() > maxDistance) {
            discard();
        }
    }

    private boolean isStuck() {
        return isNoGravity();
    }

    private void handleStuck() {
        if (!(getOwner() instanceof Player owner) || !owner.isAlive()) {
            discard();
            return;
        }

        if (owner.isShiftKeyDown()) {
            discard();
            return;
        }

        Vec3 target;
        if (stuckEntityId != 0) {
            Entity targetEntity = level().getEntity(stuckEntityId);
            if (targetEntity == null || !targetEntity.isAlive()) {
                discard();
                return;
            }
            target = targetEntity.position().add(0, targetEntity.getBbHeight() / 2, 0);
            stuckTarget = target;
        } else if (stuckTarget != null) {
            target = stuckTarget;
        } else {
            discard();
            return;
        }

        if (owner.position().distanceTo(target) > maxDistance) {
            discard();
            return;
        }

        pullOwnerToward(owner, target);
        setPos(target);
    }

    private void pullOwnerToward(Player owner, Vec3 target) {
        Vec3 ownerPos = owner.position();
        double dist = ownerPos.distanceTo(target);

        if (dist <= STOP_DISTANCE) {
            owner.setDeltaMovement(0, 0, 0);
            owner.fallDistance = 0;
            owner.hurtMarked = true;
        } else {
            Vec3 dir = target.subtract(ownerPos).normalize();
            double speed = pullSpeed;
            if (dir.y > 0) {
                speed *= 1.0 + dir.y * 1.2;
            }
            Vec3 pull = dir.scale(speed);
            owner.setDeltaMovement(pull);
            owner.fallDistance = 0;
            owner.hurtMarked = true;
        }
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
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 4096.0;
    }
}
