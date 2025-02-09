package com.github.externaltime.cartographer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.jar.JarFile;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("usage: java cartographer [one or more jar files]");
            return;
        }
        var graph = new Graph<String>();
        for (var path : args) {
            try (JarFile jar = new JarFile(path)) {
                new ClassReader(graph).addArchive(jar);
            } catch (Exception e) {
                throw new IOException("while reading \"%s\"".formatted(path), e);
            }
        }
        var scc = TransReductionCondensation.of(graph);
        try (var writer = new PrintWriter(System.out)) {
            Dot.write(writer, scc);
        } catch (Exception e) {
            throw new IOException("while while printing graph description", e);
        }
    }
}
