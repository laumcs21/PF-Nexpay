package proyecto.nexpay.web.util;

import proyecto.nexpay.web.datastructures.DirectedGraph;
import proyecto.nexpay.web.datastructures.GraphNode;
import proyecto.nexpay.web.datastructures.SimpleList;

public class GraphStats {

    public String mostOutgoingNode;
    public String mostIncomingNode;
    public int totalNodes;
    public int totalEdges;

    public static GraphStats calculate(DirectedGraph<String> graph) {
        GraphStats stats = new GraphStats();
        stats.totalNodes = graph.getAllNodes().size();
        stats.totalEdges = 0;

        String maxOutNode = null;
        int maxOutDegree = 0;

        String maxInNode = null;
        int maxInDegree = 0;

        SimpleList<GraphNode<String>> all = graph.getAllNodes();
        for (int i = 0; i < all.size(); i++) {
            GraphNode<String> node = all.get(i);

            // Outgoing
            int out = node.getAdjacency().size();
            stats.totalEdges += out;
            if (out > maxOutDegree) {
                maxOutDegree = out;
                maxOutNode = node.getValue();
            }

            // Incoming
            int in = 0;
            for (int j = 0; j < all.size(); j++) {
                if (all.get(j).getAdjacency().contains(node.getValue())) {
                    in++;
                }
            }
            if (in > maxInDegree) {
                maxInDegree = in;
                maxInNode = node.getValue();
            }
        }

        stats.mostOutgoingNode = maxOutNode + " (" + maxOutDegree + ")";
        stats.mostIncomingNode = maxInNode + " (" + maxInDegree + ")";
        return stats;
    }
}
