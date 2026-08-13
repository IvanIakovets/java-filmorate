package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Arrays;

public class GenreDeserializer extends JsonDeserializer<Genre> {

    @Override
    public Genre deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Проверяем наличие поля "id" (может быть и "name", но мы ищем по id)
        if (node.has("id")) {
            int id = node.get("id").asInt();

            return Arrays.stream(Genre.values())
                    .filter(genre -> genre.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown genre id: " + id));
        }

        throw new IllegalArgumentException("Genre object must have 'id' field");
    }
}