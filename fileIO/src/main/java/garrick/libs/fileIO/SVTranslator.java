package garrick.libs.fileIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SVTranslator<Row extends Record> {
    @SuppressWarnings("unchecked")
    public SVTranslator(Class<Row> rowClass, String delimiter) {
        this.delimiter = delimiter;

        this.getters = Arrays.stream(rowClass.getDeclaredFields())
                .map(Field::getName).map(name -> {
                    try {
                        return rowClass.getMethod(name);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).toArray(Method[]::new);

        this.parsers = Arrays.stream(getters)
                .map(Method::getReturnType)
                .map(type -> {
                    try {
                        return type.getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).<Function<String, ?>>map(constructor -> string -> {
                    try {
                        return constructor.newInstance(string);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                }).toArray(Function[]::new);

        try {
            this.rowConstructor = rowClass.getConstructor(
                    Arrays.stream(getters).map(Method::getReturnType).toArray(Class[]::new)
            );
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final String delimiter;

    private final Method[] getters;
    private final Function<String, ?>[] parsers;
    private final Constructor<Row> rowConstructor;

    public Row toRow(String line) {
        var tokens = line.split(delimiter);

        if (tokens.length < parsers.length) {
            throw new IllegalArgumentException();
        }

        var params = new Object[parsers.length];

        for (int i = 0; i < parsers.length; i++) {
            params[i] = parsers[i].apply(tokens[i]);
        }

        try {
            return rowConstructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public String toLine(Row row) {
        return Arrays.stream(getters)
                .map(getter -> {
                    try {
                        return getter.invoke(row).toString();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }).collect(Collectors.joining(delimiter));
    }

    public static <Row extends Record> SVTranslator<Row> csv(Class<Row> rowClass) {
        return new SVTranslator<>(rowClass, ",");
    }

    public static <Row extends Record> SVTranslator<Row> tsv(Class<Row> rowClass) {
        return new SVTranslator<>(rowClass, "\t");
    }
    
    public Stream<Row> load(Path path) throws IOException {
        return Files.lines(path).map(this::toRow);
    }

    public void save(Path path, Stream<Row> rowStream) throws IOException {
        var data = rowStream.map(this::toLine).collect(Collectors.joining("\n"));

        try (var fileOutputStream = new FileOutputStream(path.toFile(), true)) {
            fileOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    @SafeVarargs
    public final void save(Path path, Row... rows) throws IOException {
        save(path, Arrays.stream(rows));
    }
}
