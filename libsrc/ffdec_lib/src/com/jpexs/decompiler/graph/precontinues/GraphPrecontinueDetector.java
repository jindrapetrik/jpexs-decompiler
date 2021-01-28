/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph.precontinues;

import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class GraphPrecontinueDetector {
    public void detectPrecontinues(List<GraphPart> heads, Set<GraphPart> allParts, List<Loop> loops) {
        boolean isSomethingTodo = false;
        for (Loop el : loops) {
            if (el.backEdges.size() == 1) {
                isSomethingTodo = true;
            }
        }
        if (!isSomethingTodo) {
            return;
        }

        Map<GraphPart, Node> partToNode = new HashMap<>();
        for (GraphPart part : allParts) {
            Node node = new Node();
            node.graphPart = part;
            partToNode.put(part, node);
        }
        for (GraphPart part : allParts) {
            Node node = partToNode.get(part);
            for (GraphPart prev : part.refs) {
                if (prev.start < 0) {
                    continue;
                }
                Node prevNode = partToNode.get(prev);
                node.prev.add(prevNode);
            }
            for (GraphPart next : part.nextParts) {
                Node nextNode = partToNode.get(next);
                node.next.add(nextNode);
            }
        }
        List<Node> headNodes = new ArrayList<>();
        for (GraphPart head : heads) {
            headNodes.add(partToNode.get(head));
        }


        boolean changed = true;
        while (changed) {
            changed = false;
            if (joinNodes(headNodes)) {
                changed = true;
            }
            if (checkIfs(headNodes)) {
                changed = true;
            }
            if (handleWhile(headNodes)) {
                changed = true;
            }
        }

        for (Loop el : loops) {
            if (el.backEdges.size() == 1) {
                //System.err.println("loop " + el.loopContinue);
                GraphPart backEdgePart = el.backEdges.iterator().next();
                Node node = partToNode.get(backEdgePart);
                //System.err.println("backedge:" + backEdgePart);
                boolean wholeLoop = false;
                while (node.parentNode != null) {
                    node = node.parentNode;
                    //System.err.println("- parent " + node.graphPart);
                    if (node.graphPart.equals(el.loopContinue)) {
                        wholeLoop = true;
                        break;
                    }
                }
                if (!wholeLoop) {
                    el.loopPreContinue = node.graphPart;
                }
            }
        }
        //printGraph(headNodes);
    }

    private void printGraph(List<Node> headNodes) {
        Set<Node> allNodes = new LinkedHashSet<>();
        for (Node headNode : headNodes) {
            populateNodes(headNode, allNodes);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("digraph mygraph {\r\n");
        for (Node node : allNodes) {
            sb.append("node" + node.getId() + "[label=\"" + node.toString() + "\"];\r\n");
            for (Node n : node.next) {
                sb.append("node" + node.getId() + "->node" + n.getId() + ";\r\n");
            }
        }
        sb.append("}\r\n");
        System.err.println(sb.toString());
    }

    private void populateNodes(Node node, Set<Node> populated) {
        if (populated.contains(node)) {
            return;
        }
        populated.add(node);
        for (Node n : node.next) {
            populateNodes(n, populated);
        }
    }

    private boolean checkIfs(List<Node> headNodes) {
        Set<Node> visited = new HashSet<>();
        Reference<Integer> numChanged = new Reference<>(0);
        for (int h = 0; h < headNodes.size(); h++) {
            Node newHeadNode;
            newHeadNode = checkIfs(headNodes.get(h), visited, numChanged);
            headNodes.set(h, newHeadNode);
        }
        return numChanged.getVal() > 0;
    }

    private boolean joinNodes(List<Node> headNodes) {
        Set<Node> visited = new HashSet<>();
        Reference<Integer> numChanged = new Reference<>(0);
        for (int h = 0; h < headNodes.size(); h++) {
            Node newHeadNode;
            newHeadNode = joinNodes(headNodes.get(h), visited, numChanged);
            headNodes.set(h, newHeadNode);
        }
        return numChanged.getVal() > 0;
    }

    private boolean handleWhile(List<Node> headNodes) {
        Set<Node> visited = new HashSet<>();
        Reference<Integer> numChanged = new Reference<>(0);
        for (int h = 0; h < headNodes.size(); h++) {
            Node newHeadNode;
            newHeadNode = handleWhile(headNodes.get(h), visited, numChanged);
            headNodes.set(h, newHeadNode);
        }
        return numChanged.getVal() > 0;
    }

    private Node handleWhile(Node node, Set<Node> visited, Reference<Integer> numWhile) {
        if (visited.contains(node)) {
            return node;
        }
        visited.add(node);

        Node result = node;

        Node bodyNode = null;
        Node breakNode = null;
        if (node.next.size() == 2
                && node.next.get(0).next.size() == 1
                && node.next.get(0).next.get(0) == node) {
            breakNode = node.next.get(1);
            bodyNode = node.next.get(0);
        } else if (node.next.size() == 2
                && node.next.get(1).next.size() == 1
                && node.next.get(1).next.get(0) == node) {
            breakNode = node.next.get(0);
            bodyNode = node.next.get(1);
        }

        if (bodyNode != null) {

            WhileNode whileNode = new WhileNode();
            bodyNode.parentNode = whileNode;
            whileNode.graphPart = node.graphPart;
            whileNode.body = bodyNode;
            whileNode.body.removeFromGraph();
            whileNode.prev = new NonNullList<>(node.prev);
            node.replacePrevs(whileNode);
            node.replaceNexts(whileNode);
            whileNode.next.add(breakNode);
            result = whileNode;
            numWhile.setVal(numWhile.getVal() + 1);
        }

        for (Node n : result.next) {
            handleWhile(n, visited, numWhile);
        }
        return result;
    }

    private Node joinNodes(Node node, Set<Node> visited, Reference<Integer> numJoined) {
        if (visited.contains(node)) {
            return node;
        }
        visited.add(node);
        Node currentNode = node;
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(currentNode);
        while (currentNode.next.size() == 1
                && currentNode.next.get(0).prev.size() == 1
                && !visited.contains(currentNode.next.get(0))) {
            currentNode = currentNode.next.get(0);
            visited.add(currentNode);
            nodeList.add(currentNode);
        }

        Node result = node;
        if (nodeList.size() > 1) {
            JoinedNode joinedNode = new JoinedNode();
            joinedNode.graphPart = node.graphPart;
            joinedNode.nodes = nodeList;
            joinedNode.next = new NonNullList<>(currentNode.next);
            joinedNode.prev = new NonNullList<>(node.prev);
            node.replacePrevs(joinedNode);
            currentNode.replaceNexts(joinedNode);
            for (Node n : nodeList) {
                n.parentNode = joinedNode;
                n.removeFromGraph();
            }
            result = joinedNode;
            numJoined.setVal(numJoined.getVal() + 1);
        }

        for (Node n : result.next) {
            joinNodes(n, visited, numJoined);
        }

        return result;
    }

    private Node checkIfs(Node node, Set<Node> visited, Reference<Integer> numIfs) {
        if (visited.contains(node)) {
            return node;
        }
        visited.add(node);
        /*
        if(a)
        {
            onTrue
        }        
         */
        if (node.next.size() == 2
                && node.next.get(0).next.size() == 1
                && node.next.get(0).next.get(0) == node.next.get(1)) {
            IfNode ifNode = new IfNode();
            ifNode.onTrue = node.next.get(0);
            ifNode.onTrue.parentNode = ifNode;
            ifNode.onTrue.removeFromGraph();
            ifNode.onFalse = null;
            ifNode.graphPart = node.graphPart;
            ifNode.prev = new NonNullList<>(node.prev);
            node.replacePrevs(ifNode);
            Node after = node.next.get(1);
            node.removeFromGraph();
            ifNode.next.add(after);
            after.prev.add(ifNode);
            numIfs.setVal(numIfs.getVal() + 1);
            checkIfs(after, visited, numIfs);
            return ifNode;
        }

        /*
        if(a)
        {
            
        }   
        else
        {
            onFalse
        }
         */
        if (node.next.size() == 2
                && node.next.get(1).next.size() == 1
                && node.next.get(1).next.get(0) == node.next.get(0)) {
            IfNode ifNode = new IfNode();
            ifNode.onTrue = null;
            ifNode.onFalse = node.next.get(1);
            ifNode.onFalse.parentNode = ifNode;
            ifNode.onFalse.removeFromGraph();
            ifNode.graphPart = node.graphPart;
            ifNode.prev = new NonNullList<>(node.prev);
            node.replacePrevs(ifNode);
            Node after = node.next.get(0);
            node.removeFromGraph();
            ifNode.next.add(after);
            after.prev.add(ifNode);
            numIfs.setVal(numIfs.getVal() + 1);
            checkIfs(after, visited, numIfs);
            return ifNode;
        }
        /*
        if(a)
        {
            onTrue
        }   
        else
        {
            onFalse
        }
         */
        if (node.next.size() == 2
                && node.next.get(0).next.size() == 1
                && node.next.get(1).next.size() == 1
                && node.next.get(0).next.get(0) == node.next.get(1).next.get(0)) {
            IfNode ifNode = new IfNode();
            ifNode.onTrue = node.next.get(0);
            ifNode.onTrue.parentNode = ifNode;
            ifNode.onFalse = node.next.get(1);
            ifNode.onFalse.parentNode = ifNode;
            ifNode.onFalse.removeFromGraph();
            ifNode.graphPart = node.graphPart;
            ifNode.prev = new NonNullList<>(node.prev);
            node.replacePrevs(ifNode);
            Node after = node.next.get(0).next.get(0);
            node.removeFromGraph();
            ifNode.next.add(after);
            after.prev.add(ifNode);
            numIfs.setVal(numIfs.getVal() + 1);
            checkIfs(after, visited, numIfs);
            return ifNode;
        }

        for (Node n : node.next) {
            checkIfs(n, visited, numIfs);
        }
        return node;
    }
}
