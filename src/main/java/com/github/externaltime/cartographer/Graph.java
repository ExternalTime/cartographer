package com.github.externaltime.cartographer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph<T> {
    private final Map<T, List<T>> graph = new HashMap<>();

    public void addEdges(T from, Stream<T> to) {
        var edgesTo = to.filter(item -> !Objects.equals(from, item)).toList();
        graph.put(from, edgesTo);
        for (var edgeTo : edgesTo)
            graph.putIfAbsent(edgeTo, Collections.emptyList());
    }

    public Collection<T> vertices() {
        return graph.keySet();
    }

    public Collection<T> edgesFrom(T vertex) {
        return graph.get(vertex);
    }

    private Graph<T> subgraph(Collection<T> vertices) {
        var res = new Graph<T>();
        for (var vertex : vertices)
            res.addEdges(vertex, this.graph.get(vertex).stream().filter(vertices::contains));
        return res;
    }

    public Graph<Graph<T>> concentrate(List<List<T>> groups) {
        var inSubgraph = new HashMap<T, Integer>();
        var subgraphs = new ArrayList<Graph<T>>();
        groups.forEach(group -> {
            for (var vertex : group)
                inSubgraph.put(vertex, subgraphs.size());
            subgraphs.add(this.subgraph(group));
        });
        var res = new Graph<Graph<T>>();
        for (var subgraph : subgraphs)
            res.addEdges(subgraph, subgraph.vertices()
                    .stream()
                    .map(this::edgesFrom)
                    .flatMap(Collection::stream)
                    .map(inSubgraph::get)
                    .map(subgraphs::get)
                    .collect(Collectors.toUnmodifiableSet())
                    .stream());
        return res;
    }
}
