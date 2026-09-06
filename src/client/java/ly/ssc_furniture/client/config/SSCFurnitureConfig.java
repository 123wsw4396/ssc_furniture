package ly.ssc_furniture.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SSCFurnitureConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("ssc_furniture.json");

    public static SSCFurnitureConfig INSTANCE = new SSCFurnitureConfig();

    public boolean hangCameraInverted = true;
    public boolean showShapingHookInExtraHand = false;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            SSCFurnitureConfig loaded = GSON.fromJson(json, SSCFurnitureConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
