package com.github.externaltime.cartographer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Main {
    public static Predicate<String> getFilter(String path) throws IOException {
        try (var filters = new FileReader(path)) {
            var list = new BufferedReader(filters)
                    .lines()
                    .map(String::trim)
                    // Allow for comments and empty lines for formatting
                    .filter(line -> !line.startsWith("#") && !line.isEmpty())
                    .map(Pattern::compile)
                    .toList();
            return str -> list.stream().noneMatch(pattern -> pattern.matcher(str).matches());
        } catch (IOException e) {
            throw new IOException("while reading the filters file: \"%s\"".formatted(path), e);
        }
    }

    public static Graph<String> getGraph(Predicate<String> filter, List<String> jars) throws IOException {
        var graph = new Graph<String>();
        var classReader = new ClassReader(filter, graph);
        for (var path : jars) {
            try (JarFile jar = new JarFile(path)) {
                classReader.addArchive(jar);
            } catch (IOException e) {
                throw new IOException("while reading jar \"%s\"".formatted(path), e);
            }
        }
        return graph;
    }

    public static void print(Graph<Graph<String>> transRedCondensation) throws IOException {
        try (var writer = new PrintWriter(System.out)) {
            Dot.write(writer, transRedCondensation);
        } catch (Exception e) {
            throw new IOException("while while printing graph description", e);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("usage: java cartographer [path to file with filters] [one or more jar files to include in graph]");
            return;
        }
        var filter = getFilter(args[0]);
        var jars = Arrays.asList(args).subList(1, args.length);
        var graph = getGraph(filter, jars);
        print(TransReductionCondensation.of(graph));
    }
}
