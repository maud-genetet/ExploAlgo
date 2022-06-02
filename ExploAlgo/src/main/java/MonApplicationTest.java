import io.jbotsim.core.Color;
import io.jbotsim.core.Link;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static io.jbotsim.ui.icons.Icons.FLAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MonApplicationTest extends MonApplication {

    /**
     * Test de la reiniialisation de la topologie
     */
    @Test
    void testReinitialisation() {
        Topology tp = new Topology(); // Objet qui contient le graphe
        Node n1 = new Node();
        Node n2 = new Node();
        Node n3 = new Node();
        Link l1 = new Link(n1, n2);
        tp.addNode(100, 100, n1);
        tp.addNode(500, 100, n2);
        tp.addNode(500, 500, n3);
        tp.addLink(l1);

        n1.setColor(Color.RED);
        n2.setColor(Color.BLUE);
        n3.setIcon(FLAG);
        l1.setWidth(5);

        assertEquals(Color.RED, n1.getColor());
        assertEquals(Color.BLUE, n2.getColor());
        assertEquals(FLAG, n3.getIcon());
        assertEquals(5, l1.getWidth());

        reinitialisation(tp);

        assertNull(n1.getColor());
        assertNull(n2.getColor());
        assertNull(n3.getIcon());
        assertEquals(1, l1.getWidth());
    }

    @Test
    void testOnSelection() {
        Topology tp = new Topology();
        Node n1 = new Node();

        tp.addNode(100, 100, n1);

        onSelection(n1);

        assertEquals(Color.BLACK, n1.getColor());
    }

    /**
     *
     * Test du parcours en largeur
     *
     *     1-2-3-6
     *      \   /
     *       4-5
     */
    @Test
    void testParcoursEnLargeur() {

        Topology tp = new Topology();
        Node n1 = new Node();
        Node n2 = new Node();
        Node n3 = new Node();
        Node n4 = new Node();
        Node n5 = new Node();
        Node n6 = new Node();

        tp.addNode(100, 100, n1);
        tp.addNode(100, 200, n2);
        tp.addNode(100, 300, n3);
        tp.addNode(100, 400, n6);
        tp.addNode(200, 200, n4);
        tp.addNode(200, 300, n5);

        Link l1 = new Link(n1, n2);
        Link l2 = new Link(n2, n3);
        Link l3 = new Link(n3, n6);
        Link l4 = new Link(n1, n4);
        Link l5 = new Link(n4, n5);
        Link l6 = new Link(n5, n6);

        tp.addLink(l1);
        tp.addLink(l2);
        tp.addLink(l3);
        tp.addLink(l4);
        tp.addLink(l5);
        tp.addLink(l6);

        HashMap<Node, Node> chemin = ParcoursEnLargeur(n1, n6);

        assertEquals(n1, chemin.get(n1));
        assertEquals(n1, chemin.get(n2));
        assertEquals(n2, chemin.get(n3));
        assertEquals(n3, chemin.get(n6));
        assertEquals(n1, chemin.get(n4));
    }


    /**
     *
     * Test du calcul de la distance
     *
     *     1-2-3-6
     *      \   /
     *       4-5
     */
    @Test
    void testDistanceDestPointSource() {

        HashMap<Node, Node> chemin = new HashMap<>();

        Node n1 = new Node();
        Node n2 = new Node();
        Node n3 = new Node();
        Node n6 = new Node();

        chemin.put(n6, n3);
        chemin.put(n3, n2);
        chemin.put(n2, n1);
        chemin.put(n1, n1);

        //assertEquals( 300.00, DistanceDestPointSource(n1, chemin, n6));
    }


    /**
     * Test de la fonction qui extrait le bon chemin
     */
    @Test
    void testExtraireChemin() {

        HashMap<Node, Node> chemin = new HashMap<>();
        HashMap<Node, Node> chemin2 = new HashMap<>();

        Node n1 = new Node();
        Node n2 = new Node();
        Node n3 = new Node();
        Node n4 = new Node();
        Node n5 = new Node();
        Node n6 = new Node();

        chemin.put(n6, n3);
        chemin.put(n3, n2);
        chemin.put(n2, n1);
        chemin.put(n1, n1);
        chemin.put(n5, n4);
        chemin.put(n4, n1);

        chemin2.put(n6, n3);
        chemin2.put(n3, n2);
        chemin2.put(n2, n1);

        assertEquals(chemin2, ExtraireChemin(chemin, n1, n6));
    }
}