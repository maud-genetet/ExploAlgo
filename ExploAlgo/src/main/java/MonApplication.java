/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import io.jbotsim.core.Color;
import io.jbotsim.core.Link;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.core.event.SelectionListener;
import io.jbotsim.ui.JTopology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import static io.jbotsim.ui.icons.Icons.FLAG;

/**
 *
 * @author maud
 */
public class MonApplication implements ActionListener, SelectionListener {
    Topology tp; // Objet qui contient le graphe
    JTopology jtp; // Composant graphique qui affiche le graphe
    Node source = null; // noeud source
    Node destination = null; // noeud destination
    HashSet<Node> pointAEviter = new HashSet<>(); // ensemble des noeuds a eviter
    Boolean estCherche = false; // vrai si on cherche un chemin, faux sinon

    /**
     * Constructeur
     *
     * @author maud
     */
    public MonApplication() {
        // Creation du graphe
        tp = new Topology();
        // Creation de l'interface graphique (ci-dessous)
        creerInterfaceGraphique();
    }

    /**
     * Main
     *
     * @param args the command line arguments
     * @author maud
     */
    public static void main(String[] args) {
        new MonApplication();
    } 
    
    /**
     * Cree une Inteface Graphique
     *
     * @author maud
     */
    private void creerInterfaceGraphique() {
        // Creation d'une fenêtre
        JFrame window = new JFrame("Mon application");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creation du composant graphique qui affiche le graphe
        jtp = new JTopology(tp);
        window.add(jtp);

        // Creation d'un bouton test haut
        JButton button = new JButton("Reinitialisation");
        window.add(button,BorderLayout.NORTH);

        // Creation d'un bouton test est
        JButton button2 = new JButton("Generer grilles");
        window.add(button2,BorderLayout.EAST);

        // Creation d'un bouton test bas
        JButton button3 = new JButton("Chercher chemin");
        window.add(button3,BorderLayout.SOUTH);
        
        // Abonnement aux evenements des boutons (clic, etc.)
        button.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        tp.addSelectionListener(this);

        // Finalisation
        window.pack();
        window.setVisible(true);
    }
    
    /**
     * Se declanche quand un bouton est clique
     * 
     * @param e Evenement
     * @author maud
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Reinitialisation")) {
            reinitialisation(tp);
        }
        else if (e.getActionCommand().equals("Chercher chemin")) {
          if ( this.source!=null && this.destination!=null &&  tp.getNodes().contains(this.source) 
                && tp.getNodes().contains(this.destination)) {
              chercherChemin(this.source, this.destination);
          } else {
              JOptionPane.showMessageDialog(null, "Veuillez selectionner une source et une destination");
          }
        }
        else if (e.getActionCommand().equals("Generer grilles")) {
            genererGrille(tp,6);
        }
    }

    /**
     * Reinitialise le graphe et les noeuds
     *
     * @param tp Topologie
     * @author maud
     */
    public void reinitialisation(Topology tp){
        this.source = null;
        this.destination = null;
        this.pointAEviter.clear();
        for (Link l : tp.getLinks()){
            l.setWidth(1);
        }
        for (Node n : tp.getNodes()){
            n.setColor(null);
            n.setIcon(null);
        }
        estCherche = false;
    }

    /**
     * Gere les evenements de selection des noeuds
     *
     * @param selectedNode Noeud selectionne
     * @author maud
     */
    @Override
    public void onSelection(Node selectedNode) {
        if ( (this.source == null || !tp.getNodes().contains(this.source))
                && (this.source!=this.destination || this.destination==null)){
                this.source = selectedNode;
                this.source.setColor(Color.BLACK);
                estCherche = false;
        } else if ( (this.destination == null || !tp.getNodes().contains(this.destination))
                && (this.destination!=this.source || this.source==null)) {
                this.destination = selectedNode;
                this.destination.setIcon(FLAG);
                estCherche = false;
        }
        else if ( !estCherche && this.destination!=null && this.source!=null) {
            if (!pointAEviter.contains(selectedNode) 
                    && selectedNode!=this.destination && selectedNode!=source){
                pointAEviter.add(selectedNode);
                selectedNode.setColor(Color.RED);
            } else if ( selectedNode!=this.destination && selectedNode!=source ){
                pointAEviter.remove(selectedNode);
                selectedNode.setColor(null); 
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Veuillez Reinitialiser");
        }
    }

    /**
     * Cherche le chemin le plus court entre la source et la destination
     *
     * @param source Noeud source
     * @param destination  Noeud destination
     * @author maud
     */
    private void chercherChemin(Node source, Node destination) {
            HashMap<Node,Node> allChemin = ParcoursEnLargeur(source, destination);
            if (allChemin.containsKey(destination)) {
                HashMap<Node,Node> chemin = ExtraireChemin(allChemin, source, destination);
                for (Node n : tp.getNodes()){
                    if (chemin.containsKey(n) && chemin.get(n)!=n){
                        n.getCommonLinkWith(chemin.get(n)).setWidth(4);
                    }
                }
                estCherche = true;
            } else {
                JOptionPane.showMessageDialog(null, "Il n'y a pas de chemin possible");
            }
    }

    /**
     * Retourne la distance entre un noeud et la destination
     *
     * @param n Noeud
     * @param path Chemin
     * @return Distance entre le noeud et la destination
     * @author maud
     */
    public double DistanceDestPointSource(Node n, HashMap<Node, Node> path, Node destination){
        double dist = 0;
        Node n1 = destination;
        Node a = n;
        while(n1!=source){
            dist += n1.distance(a);
            n1 = a;
            a = path.get(n1);
        }
        return dist;
    }

    /**
     * Retoune la liste des noeuds et leurs parents du parcours en largeur
     * jusqu'a ce qu'on arrive a la destination
     *
     * @param source Noeud source
     * @param destination Noeud destination
     * @return Liste des noeuds et leurs parents du parcours en largeur
     * @author maud
     */
    public HashMap<Node,Node> ParcoursEnLargeur(Node source, Node destination){
        HashMap<Node, Node> path = new HashMap<>();
        PriorityQueue<Node> file = new PriorityQueue<>(new Comparator<>() {
            @Override
            public int compare(Node n1, Node n2) {
                return Double.compare (DistanceDestPointSource(n1, path, destination),  DistanceDestPointSource(n2, path, destination));
            }
        });
        file.add(source);
        path.put(source,source);
        Node n = null;
        while (!file.isEmpty() && n!=destination){
            n = file.remove();
            for (Node Neighbor : n.getNeighbors()) {
                if (!path.containsKey(Neighbor) && !pointAEviter.contains(Neighbor)){
                    path.put(Neighbor, n);
                    file.add(Neighbor);
                    
                }
            }
        }
        return path;
    }

    /**
     * Retourne le chemin le plus court entre la source et la destination
     * On retrouve le chemin en partant de la destination et en remontant
     * jusqu'a la source
     *
     * @param allChemin Liste des noeuds et leurs parents du parcours en largeur
     * @param source Noeud source
     * @param destination Noeud destination
     * @return Chemin le plus court entre la source et la destination
     * @author maud
     */
    public HashMap<Node,Node> ExtraireChemin(HashMap<Node, Node> allChemin, Node source, Node destination){
        HashMap<Node,Node> cheminVersDest = new HashMap<>();
        if (allChemin.containsKey(destination)){
            Node dest = allChemin.get(destination);
            cheminVersDest.put(destination, dest);
            while (dest!=source){
                cheminVersDest.put(dest, allChemin.get(dest));
                dest = allChemin.get(dest);
            }
        }
        return cheminVersDest;
    }

    /**
     * Genere une topologie avec des noeuds et des liens sous forme de grille
     *
     * @param tp Topologie
     * @param nbRows Nombre de lignes
     * @author maud
     */
    public static void genererGrille(Topology tp, int nbRows) {
        int stepX = (tp.getWidth() - 100) / (nbRows - 1);
        int stepY = (tp.getHeight() - 100) / (nbRows - 1);
        if (Math.max(stepX, stepY) >= 2 * Math.min(stepX, stepY)) {
            String s = "les proportions de la topologie sont inadaptees";
            JOptionPane.showMessageDialog(null, s);
            return;
        }
        tp.setCommunicationRange(Math.max(stepX, stepY) + 1);
        for (int i = 50; i <= tp.getWidth() - 50; i += stepX) {
            for (int j = 50; j <= tp.getHeight() - 50; j += stepY) {
                tp.addNode(i, j, new Node());
            }
        }
    }
}


