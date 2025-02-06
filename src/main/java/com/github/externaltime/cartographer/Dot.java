package com.github.externaltime.cartographer;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

// Graph description language used by Graphviz
public class Dot {
    private final Writer out;
    private final Map<Graph<String>, String> names = new HashMap<>();

    private Dot(Writer out) {
        this.out = out;
    }

    private void writeSubgraph(Graph<String> subgraph) throws IOException {
        if (subgraph.vertices().size() <= 1) {
            names.put(subgraph, subgraph.vertices().iterator().next());
            return;
        }
        var name = "cluster_" + names.size();
        names.put(subgraph, name);
        out.write("\tsubgraph \"%s\" {\n".formatted(name));
        for (var from : subgraph.vertices()) {
            for (var to : subgraph.edgesFrom(from)) {
                if (!subgraph.edgesFrom(to).contains(from))
                    out.write("\t\t\"%s\" -> \"%s\";\n".formatted(from, to));
                    // only printing bidirectional arrows once
                else if (0 < from.compareTo(to))
                    out.write("\t\t\"%s\" -> \"%s\" [dir=both];\n".formatted(from, to));
            }
        }
        out.write("\t}\n");
    }

    private void write(Graph<Graph<String>> graph) throws IOException {
        out.write("strict digraph {\n");
        for (var subgraph : graph.vertices())
            writeSubgraph(subgraph);
        for (var from : graph.vertices())
            for (var to : graph.edgesFrom(from))
                out.write("\t\"%s\" -> \"%s\";\n".formatted(names.get(from), names.get(to)));
        out.write("}\n");
    }

    public static void write(Writer out, Graph<Graph<String>> graph) throws IOException {
        try {
            new Dot(out).write(graph);
        } catch (Exception e) {
            throw new IOException("while writing graph", e);
        }
    }
}
