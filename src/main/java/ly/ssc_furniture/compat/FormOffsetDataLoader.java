package ly.ssc_furniture.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ly.ssc_furniture.SSCFurniture;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 加载 data/&lt;ns&gt;/ssc_furniture/compat/form_offsets/*.json.
 * Schema:
 * <pre>{
 *   "ssc_furniture:gust_cloth": {
 *     "ssc:anubis_wolf_3":  { "dz": 3.0, "scale": 1.10 },
 *     "hex_mod:hex_wolf_3": { "dx": 0.0, "dy": 0.0, "dz": 4.0, "scale": 1.05 }
 *   }
 * }</pre>
 * 缺省字段: dx=dy=dz=0, scale=1. Reload 时 dataRegistry 会被清空重灌.
 */
public final class FormOffsetDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final String FOLDER = "ssc_furniture/compat/form_offsets";
    private static final ResourceLocation ID = new ResourceLocation("ssc_furniture", "form_offsets");

    @Override
    public ResourceLocation getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager mgr) {
        FormOffsetRegistry.clearData();
        int count = 0;
        for (Map.Entry<ResourceLocation, Resource> entry :
                mgr.listResources(FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation file = entry.getKey();
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject()) {
                    SSCFurniture.LOGGER.warn("[FormOffset] file not a JSON object: {}", file);
                    continue;
                }
                for (Map.Entry<String, JsonElement> eqEntry : root.getAsJsonObject().entrySet()) {
                    ResourceLocation eqId = ResourceLocation.tryParse(eqEntry.getKey());
                    if (eqId == null) {
                        SSCFurniture.LOGGER.warn("[FormOffset] invalid equipmentId '{}' in {}", eqEntry.getKey(), file);
                        continue;
                    }
                    if (!eqEntry.getValue().isJsonObject()) {
                        SSCFurniture.LOGGER.warn("[FormOffset] value for '{}' in {} must be object", eqEntry.getKey(), file);
                        continue;
                    }
                    for (Map.Entry<String, JsonElement> formEntry : eqEntry.getValue().getAsJsonObject().entrySet()) {
                        String key = formEntry.getKey();
                        FormAdjustment adj = parseAdjustment(formEntry.getValue(), file, key);
                        if (adj == null) continue;
                        FormOffsetRegistry.putData(eqId, key, adj);
                        count++;
                    }
                }
            } catch (Exception ex) {
                SSCFurniture.LOGGER.error("[FormOffset] failed to load {}: {}", file, ex.toString());
            }
        }
        SSCFurniture.LOGGER.info("[FormOffset] form_offsets entries loaded: {}", count);
    }

    private static FormAdjustment parseAdjustment(JsonElement el, ResourceLocation file, String key) {
        if (!el.isJsonObject()) {
            SSCFurniture.LOGGER.warn("[FormOffset] adjustment for '{}' in {} must be object", key, file);
            return null;
        }
        JsonObject o = el.getAsJsonObject();
        float dx    = getFloat(o, "dx",    0f);
        float dy    = getFloat(o, "dy",    0f);
        float dz    = getFloat(o, "dz",    0f);
        float scale = getFloat(o, "scale", 1f);
        return new FormAdjustment(dx, dy, dz, scale);
    }

    private static float getFloat(JsonObject o, String field, float def) {
        JsonElement el = o.get(field);
        if (el == null || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isNumber()) return def;
        return el.getAsFloat();
    }
}
