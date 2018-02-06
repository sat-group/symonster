import java.util.*;
public class mst {
    List<Node> nodes = new ArrayList<>();

    public class Node {
        int number;
        Node parent = this;
        long rank = 0;

        Node find() {
            if (parent == this) return this;
            else {
                Node result = parent.find();
                parent = result;
                return result;
            }
        }

        long depth() {
            if (parent == this) return 0;
            else return 1 + parent.depth();
        }
    }

    public class Edge {
        int node1;
        int node2;
        int weight;

        Edge(int node1, int node2, int value) {
            this.node1 = node1;
            this.node2 = node2;
            this.weight = value;
        }

        @Override
        public String toString() {
            return node1 + "," + node2 + "," + weight;
        }
    }

    public void link(Node r1, Node r2) {
        if (r1.rank > r2.rank) {
            r2.parent = r1;
        } else if (r2.rank > r1.rank) {
            r1.parent = r2;
        } else {
            if (r1.number > r2.number) {
                r1.parent = r2;
                r2.rank += 1;
            } else {
                r2.parent = r1;
                r1.rank += 1;
            }
        }
    }

    public void union(Node n1, Node n2) {
        link(n1.find(), n2.find());
    }

    public static void main(String[] args) {
        new mst().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        for (int i = 0; i < n; i += 1) {
            Node node = new Node();
            node.number = i;
            nodes.add(node);
        }

        int m = scanner.nextInt();
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < m; i += 1) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            int w = scanner.nextInt();
            edges.add(new Edge(u, v, w));
        }

        edges.sort(new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                if (o1.weight > o2.weight) return 1;
                else if (o1.weight < o2.weight) return -1;
                else return 0;
            }
        });
        int edgeCount = 0;
        long mstWeight = 0;
        long maxWeight = 0;
        long maxWeightInd = 0;
        int index = 1;
        for (Edge edge : edges) {
            if (nodes.get(edge.node1).find() != nodes.get(edge.node2).find()) {
                mstWeight += edge.weight;
                if (edge.weight > maxWeight) {
                    maxWeightInd = index;
                    maxWeight = edge.weight;
                }
                union(nodes.get(edge.node1), nodes.get(edge.node2));
                edgeCount += 1;
            }
            if (edgeCount == n-1) break;
            index += 1;
        }
        System.out.println(mstWeight);
        System.out.println(maxWeight);
        System.out.println(maxWeightInd);
        System.out.println(nodes.get(n / 2).depth());


    }
}