package graph.sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class Topological {

    // Number of vertices
    private int V;

    // Adjacency Matrix
    private ArrayList<ArrayList<Integer>> adj;

    public Topological(int v) {
        V = v;
        adj = new ArrayList<ArrayList<Integer>>(v);
        for (int i=0; i<v; ++i){
            adj.add(new ArrayList<Integer>());
        }
    }

    public void addEdge(int v,int w) {
        adj.get(v).add(w);
    }

    // A recursive function used by topologicalSort
    public void topologicalSortUtil(int v, boolean visited[], Stack<Integer> stack) {

        // Mark the current node as visited.
        visited[v] = true;
        Integer i;

        // Recur for all the vertices adjacent to this vertix
        Iterator<Integer> it = adj.get(v).iterator();
        while (it.hasNext()) {
            i = it.next();
            if (!visited[i]){
                topologicalSortUtil(i, visited, stack);
            }
        }

        // Push current vertex to stack which stores result
        stack.push(new Integer(v));
    }

    public Stack<Integer> topologicalSort()
    {
        Stack<Integer> stack = new Stack<Integer>();

        // Mark all the vertices as not visited
        boolean visited[] = new boolean[V];
        for (int i = 0; i < V; i++){
            visited[i] = false;
        }

        // Call the recursive helper function to store Topological Sort starting from all vertices one by one
        for (int i = 0; i < V; i++){
            if (visited[i] == false){
                topologicalSortUtil(i, visited, stack);
            }
        }

        return stack;
    }


}
