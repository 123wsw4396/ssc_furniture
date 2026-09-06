package ly.ssc_furniture.util;

public final class NightSkipState {
	public static final ThreadLocal<Boolean> SKIPPING_NIGHT = ThreadLocal.withInitial(() -> Boolean.FALSE);
	private NightSkipState() {}
}
