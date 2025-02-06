package com.github.externaltime.cartographer;

import java.util.*;

public class Tarjan<T> {
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

    private Graph<T> graph;
    private final List<T> stack = new ArrayList<>();
    private final Map<T, NodeInfo> nodeInfo = new HashMap<>();
    private final List<List<T>> components = new ArrayList<>();

    private Tarjan(Graph<T> graph) {
        this.graph = graph;
    }

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
            components.add(new ArrayList<>(component));
            component.clear();
        }
        return info;
    }

    public static <T> List<List<T>> stronglyConnectedComponents(Graph<T> graph) {
        var tarjan = new Tarjan<>(graph);
        graph.vertices().forEach(tarjan::recurse);
        return tarjan.components;
    }
}
