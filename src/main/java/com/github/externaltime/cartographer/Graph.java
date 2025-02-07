package com.github.externaltime.cartographer;

import java.util.*;
import java.util.stream.Stream;

public class Graph<T> {
    private final Map<T, Set<T>> graph = new LinkedHashMap<>();

    public void addEdges(T from, Stream<T> to) {
        graph.putIfAbsent(from, new HashSet<>());
        var edges = graph.get(from);
        to.forEach(edges::add);
        edges.remove(from);
        for (var edgeTo : edges)
            graph.putIfAbsent(edgeTo, new HashSet<>());
    }

    public Collection<T> vertices() {
        return graph.keySet();
    }

    public Collection<T> edgesFrom(T vertex) {
        return graph.get(vertex);
    }

    public Graph<T> subgraph(Collection<T> vertices) {
        var res = new Graph<T>();
        for (var vertex : vertices)
            res.addEdges(vertex, this.graph.get(vertex).stream().filter(vertices::contains));
        return res;
    }

    public boolean isReachable(T from, T to, Set<T> visited) {
        if (visited.contains(from)) return false;
        if (Objects.equals(from, to)) return true;
        visited.add(from);
        for (var child : this.edgesFrom(from))
            if (this.isReachable(child, to, visited))
                return true;
        return false;
    }
}
