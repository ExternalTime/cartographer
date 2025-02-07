package com.github.externaltime.cartographer;

import java.util.*;
import java.util.stream.Collectors;

// Transitive reduction of a condensation of a graph
public class TransReductionCondensation<T> {
    private final Graph<T> graph;
    private final Graph<Graph<T>> res = new Graph<>();
    private final Map<T, Graph<T>> inComponent = new HashMap<>();

    private TransReductionCondensation(Graph<T> graph) {
        this.graph = graph;
    }

    private void addComponent(Graph<T> component) {
        var candidates = component
                .vertices()
                .stream()
                .map(graph::edgesFrom)
                .flatMap(Collection::stream)
                .map(inComponent::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        res.addEdges(component, candidates.stream().filter(candidate -> {
            var visited = new HashSet<Graph<T>>();
            for (var other : candidates)
                if (other != candidate && res.isReachable(other, candidate, visited))
                    return false;
            return true;
        }));
        for (var vertex : component.vertices()) {
            inComponent.put(vertex, component);
        }
    }

    private static class NodeInfo {
        final int index;
        int lowlink;
        boolean inStack;

        public NodeInfo(int index) {
            this.index = index;
            this.lowlink = index;
            this.inStack = true;
        }

        public void fixup(NodeInfo child) {
            if (child.inStack)
                this.lowlink = Math.min(this.lowlink, child.lowlink);
        }

        public boolean isRoot() {
            return this.index == this.lowlink;
        }
    }

    private final List<T> stack = new ArrayList<>();
    private final Map<T, NodeInfo> nodeInfo = new HashMap<>();

    private NodeInfo recurse(T vertex) {
        if (nodeInfo.containsKey(vertex))
            return nodeInfo.get(vertex);
        stack.add(vertex);
        var info = new NodeInfo(nodeInfo.size());
        nodeInfo.put(vertex, info);
        for (var other : graph.edgesFrom(vertex))
            info.fixup(recurse(other));
        if (info.isRoot()) {
            var component = stack.subList(stack.lastIndexOf(vertex), stack.size());
            for (var element : component)
                nodeInfo.get(element).inStack = false;
            this.addComponent(graph.subgraph(component));
            component.clear();
        }
        return info;
    }

    public static <T> Graph<Graph<T>> of(Graph<T> graph) {
        var tarjan = new TransReductionCondensation<>(graph);
        graph.vertices().forEach(tarjan::recurse);
        return tarjan.res;
    }
}
