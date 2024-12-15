package http;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration value) throws IOException {
        if (value == null) {
            jsonWriter.nullValue();
        } else {
            // сохраняем как кол-во минут
            long minutes = value.toMinutes();
            jsonWriter.value(minutes);
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        // Читаем число минут из JSON
        long minutes = jsonReader.nextLong();
        return Duration.ofMinutes(minutes);
    }
}
