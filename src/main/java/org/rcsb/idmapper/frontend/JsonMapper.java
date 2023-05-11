package org.rcsb.idmapper.frontend;

import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created on 4/25/23.
 *
 * @author Yana Rose
 */
public class JsonMapper {

    public Gson create() {
        return new GsonBuilder()
                //.serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Multimap.class, new MultiMapAdapter())
                .create();
    }

    private static final class MultiMapAdapter implements JsonSerializer<Multimap<String,String>> {
        private static final Type asMapReturnType;
        static {
            try {
                asMapReturnType = Multimap.class.getDeclaredMethod("asMap").getGenericReturnType();
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public JsonElement serialize(Multimap<String, String> src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.asMap(), asMapType(typeOfSrc));
        }


        private static Type asMapType(Type multimapType) {
            return TypeToken.of(multimapType).resolveType(asMapReturnType).getType();
        }
    }
}
