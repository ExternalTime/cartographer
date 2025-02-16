package com.github.externaltime.cartographer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarFile;

public final class ClassReader {
    private static final int MAGIC = 0xCAFEBABE;

    private final Predicate<String> filter;
    private final Graph<String> graph;

    public ClassReader(Predicate<String> filter, Graph<String> graph) {
        this.filter = filter;
        this.graph = graph;
    }

    // We don't care whether a type is an element of an array.
    // We also merge nested classes for readability.
    private static String normalizeType(String field) {
        while (true) {
            int tmp;
            // Array
            if (field.startsWith("["))
                field = field.substring(1);
            // Pointer
            else if (field.startsWith("L") && field.endsWith(";"))
                field = field.substring(1, field.length() - 1);
            // Nested classes
            else if ((tmp = field.lastIndexOf('$')) != -1)
                field = field.substring(0, tmp);
            else break;
        }
        return field;
    }

    public void addClass(InputStream stream) throws IOException {
        var in = new DataInputStream(stream);
        if (in.readInt() != MAGIC)
            throw new ClassFormatError("invalid magic");
        in.readNBytes(4);
        var strings = new HashMap<Integer, String>();
        var classIds = new HashMap<Integer, Integer>();
        var constants = in.readUnsignedShort();
        for (var i = 1; i < constants; i++) {
            var tag = in.readUnsignedByte();
            switch (tag) {
                case 1 -> strings.put(i, new String(in.readNBytes(in.readUnsignedShort()), StandardCharsets.UTF_8));
                case 7 -> classIds.put(i, in.readUnsignedShort());
                case 8, 16, 19, 20 -> in.readNBytes(2);
                case 15 -> in.readNBytes(3);
                case 3, 4, 9, 10, 11, 12, 13, 14, 17, 18 -> in.readNBytes(4);
                // Both `Long` and `Double` take up 2 spaces in constant pool.
                case 5, 6 -> {
                    i += 1;
                    in.readNBytes(8);
                }
                default -> throw new ClassFormatError("Unknown constant tag " + tag);
            }
        }
        if (in.readUnsignedShort() == 0x8000)
            return;
        var thisClass = normalizeType(Objects.requireNonNull(strings.get(classIds.get(in.readUnsignedShort()))));
        if (!filter.test(thisClass))
            return;
        var otherClasses = classIds
                .values()
                .stream()
                .map(strings::get)
                .map(Objects::requireNonNull)
                .map(ClassReader::normalizeType)
                .filter(filter);
        graph.addEdges(thisClass, otherClasses);
    }

    public void addArchive(JarFile archive) throws IOException {
        for (var entries = archive.entries(); entries.hasMoreElements(); ) {
            var entry = entries.nextElement();
            var name = entry.getName();
            if (entry.isDirectory() || !name.endsWith(".class"))
                continue;
            try (var in = archive.getInputStream(entry)) {
                addClass(in);
            } catch (Exception e) {
                throw new IOException("encountered an error while trying to read a file \"%s\" from an archive".formatted(name), e);
            }
        }
    }
}
