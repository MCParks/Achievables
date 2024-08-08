package us.mcparks.achievables.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;
import lombok.Getter;
import us.mcparks.achievables.triggers.AchievableTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AchievableGsonManager {
    private static GsonBuilder defaultGsonBuilder = new GsonBuilder().registerTypeAdapter(Multimap.class,
                    (JsonSerializer<Multimap>) (multimap, type, jsonSerializationContext) -> jsonSerializationContext.serialize(multimap.asMap()))
            .registerTypeAdapter(Multimap.class,
                    (JsonDeserializer<Multimap>) (jsonElement, type, jsonDeserializationContext) -> {
                        final SetMultimap<AchievableTrigger.Type, String> map = Multimaps.newSetMultimap(new HashMap<>(),
                                () -> Sets.newHashSet());
                        for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
                            for (JsonElement element : (JsonArray) entry.getValue()) {
                                map.get(new AchievableTrigger.Type(entry.getKey()))
                                        .add(element.getAsString());
                            }
                        }
                        return map;
                    })
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);

    @Getter
    private static Gson gson = defaultGsonBuilder.create();

    public static void transformGson(Consumer<GsonBuilder> transformer) {
        transformer.accept(defaultGsonBuilder);
        gson = defaultGsonBuilder.create();
    }
}
