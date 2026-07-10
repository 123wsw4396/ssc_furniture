package ly.textmod.net;

import net.fabricmc.api.ModInitializer;

import ly.textmod.net.block.ModBlocks;
import ly.textmod.net.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextMod implements ModInitializer {
	public static final String MOD_ID = "textmod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		LOGGER.info("Hello Fabric world!");
	}
}