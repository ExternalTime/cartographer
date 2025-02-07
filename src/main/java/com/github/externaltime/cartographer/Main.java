package com.github.externaltime.cartographer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.jar.JarFile;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java cartographer [path to jar]");
            return;
        }
        var jarPath = args[0];
        var graph = new Graph<String>();
        try (JarFile jar = new JarFile(jarPath)) {
            new ClassReader(graph).addArchive(jar);
        } catch (Exception e) {
            throw new IOException("while reading \"%s\"".formatted(jarPath), e);
        }
        var scc = TransReductionCondensation.of(graph);
        try (var writer = new PrintWriter(System.out)) {
            Dot.write(writer, scc);
        } catch (Exception e) {
            throw new IOException("while writing to \"%s\"".formatted(jarPath), e);
        }
    }
}
