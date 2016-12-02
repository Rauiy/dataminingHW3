import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve_000 on 2016-12-02.
 */

/* This class is used to make it easier to traverse through the graph */
public class Node{
    public Node(String id, int m){
        this.id = Integer.parseInt(id);
        nodes = new ArrayList<>();
        M = new int[m];
        for(int i = 0; i < M.length; i++)
            M[i] = Integer.MIN_VALUE;
    }

    public void add(Node n){
        nodes.add(n);
    }

    public int id;
    public int[] M;
    List<Node> nodes;
}
