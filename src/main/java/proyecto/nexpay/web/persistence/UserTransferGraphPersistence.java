package proyecto.nexpay.web.persistence;

import proyecto.nexpay.web.datastructures.DirectedGraph;
import proyecto.nexpay.web.datastructures.GraphNode;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.fileUtil.FileUtil;

import java.io.File;
import java.io.IOException;

public class UserTransferGraphPersistence {

    private static final String FILE_PATH = "src/main/resources/persistence/files/UserTransferGraph.txt";

    public void saveGraph(DirectedGraph<String> graph) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < graph.getAllNodes().size(); i++) {
            GraphNode<String> node = graph.getAllNodes().get(i);
            for (int j = 0; j < node.getAdjacency().size(); j++) {
                builder.append(node.getValue())
                        .append("@@")
                        .append(node.getAdjacency().get(j))
                        .append("\n");
            }
        }

        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) file.getParentFile().mkdirs();
            FileUtil.saveFile(FILE_PATH, builder.toString(), false);
        } catch (IOException e) {
            System.err.println("Error saving category graph: " + e.getMessage());
        }
    }

    public DirectedGraph<String> loadGraph() {
        DirectedGraph<String> graph = new DirectedGraph<>();

        try {
            for (String line : FileUtil.readFile(FILE_PATH)) {
                if (line.trim().isEmpty()) continue;
                String[] split = line.split("@@");
                if (split.length == 2) {
                    graph.addEdge(split[0], split[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading category graph: " + e.getMessage());
        }

        return graph;
    }
}