package ly.ssc_furniture.compat;

import com.google.gson.Gson;
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
 * 加载 data/&lt;ns&gt;/ssc_furniture/compat/leg_types/*.json, 每个文件一个 JsonObject:
 * <pre>{
 *   "hex_mod:hex_fox_2": "digitigrade",
 *   "hex_mod:hex_fox_3": "quadruped"
 * }</pre>
 * 每次 reload 会先清空 dataRegistry 再重灌. apiRegistry / builtinRegistry 不受影响.
 */
public final class LegTypeDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final String FOLDER = "ssc_furniture/compat/leg_types";
    private static final ResourceLocation ID = new ResourceLocation("ssc_furniture", "leg_types");
    private static final Gson GSON = new Gson();

    @Override
    public ResourceLocation getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager mgr) {
        SscFormCompat.clearDataRegistry();
        int count = 0;
        for (Map.Entry<ResourceLocation, Resource> entry :
                mgr.listResources(FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation file = entry.getKey();
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject()) {
                    SSCFurniture.LOGGER.warn("[SscFormCompat] leg_types file not a JSON object: {}", file);
                    continue;
                }
                JsonObject obj = root.getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    ResourceLocation formId = ResourceLocation.tryParse(e.getKey());
                    if (formId == null) {
                        SSCFurniture.LOGGER.warn("[SscFormCompat] invalid formId '{}' in {}", e.getKey(), file);
                        continue;
                    }
                    if (!e.getValue().isJsonPrimitive()) {
                        SSCFurniture.LOGGER.warn("[SscFormCompat] value for '{}' in {} must be a string", e.getKey(), file);
                        continue;
                    }
                    SscFormCompat.LegType type = SscFormCompat.LegType.parse(e.getValue().getAsString());
                    SscFormCompat.putDataRegistry(formId, type);
                    count++;
                }
            } catch (Exception ex) {
                SSCFurniture.LOGGER.error("[SscFormCompat] failed to load leg_types file {}: {}", file, ex.toString());
            }
        }
        SSCFurniture.LOGGER.info("[SscFormCompat] leg_types data entries loaded: {}", count);
    }
}
