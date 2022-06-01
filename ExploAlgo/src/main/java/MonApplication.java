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
    Node source = null;
    Node destination = null;
    HashSet<Node> pointAEviter = new HashSet<Node>();
    Boolean estCherche = false;

    /**
     * Constructeur
     */
    public MonApplication() {
        // Création du graphe
        tp = new Topology();
        // Création de l'interface graphique (ci-dessous)
        creerInterfaceGraphique();
    }

    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args) {
        new MonApplication();
    } 
    
    /**
     * Crée une Inteface Graphique
     */
    private void creerInterfaceGraphique() {
        // Création d'une fenêtre
        JFrame window = new JFrame("Mon application");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Création du composant graphique qui affiche le graphe
        jtp = new JTopology(tp);
        window.add(jtp);

        // Création d'un bouton test haut
        JButton button = new JButton("Réinitialisation");
        window.add(button,BorderLayout.NORTH);

        // Création d'un bouton test est
        JButton button2 = new JButton("Générer grilles");
        window.add(button2,BorderLayout.EAST);

        // Création d'un bouton test bas
        JButton button3 = new JButton("Chercher chemin");
        window.add(button3,BorderLayout.SOUTH);
        
        // Abonnement aux évènements des boutons (clic, etc.)
        button.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        tp.addSelectionListener(this);

        // Finalisation
        window.pack();
        window.setVisible(true);
    }
    
    /**
     * Se déclanche quand un bouton est cliqué
     * 
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Réinitialisation")) {
            reinitialisation(tp);
        }
        else if (e.getActionCommand().equals("Chercher chemin")) {
          if ( this.source!=null && this.destination!=null &&  tp.getNodes().contains(this.source) 
                && tp.getNodes().contains(this.destination)) {
              chercherChemin(this.source, this.destination);
          } else {
              JOptionPane.showMessageDialog(null, "Veuillez sélectionner une source et une destination");
          }
        }
        else if (e.getActionCommand().equals("Générer grilles")) {
            genererGrille(tp,6);
        }
    }

    /**
     * Réinitialise le graphe et les noeuds
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
     * Gère les évènements de selection des noeuds
     * @param selectedNode
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
            JOptionPane.showMessageDialog(null, "Veuillez Réinitialiser");
        }
    }

    /**
     * Cherche le chemin le plus court entre la source et la destination
     *
     * @param source
     * @param destination
     */
    private void chercherChemin(Node source, Node destination){
            HashMap<Node,Node> allChemin = ParcoursEnLargeur(tp, source, destination);
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
     * @param n
     * @param path
     * @return
     */
    private double DistanceDestPointSource(Node n, HashMap<Node, Node> path){
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
     * jusqu'à ce qu'on arrive à la destination
     * @param tp
     * @param source
     * @param destination
     * @return
     */
    private HashMap<Node,Node> ParcoursEnLargeur(Topology tp, Node source, Node destination){
        HashMap<Node, Node> path = new HashMap<Node, Node>();
        PriorityQueue<Node> file = new PriorityQueue<Node>(new Comparator<>() {
            @Override
            public int compare(Node n1, Node n2) {
                return Double.compare (DistanceDestPointSource(n1, path),  DistanceDestPointSource(n2, path));
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
     * jusqu'à la source
     *
     * @param allChemin
     * @param source
     * @param destination
     * @return
     */
    public HashMap<Node,Node> ExtraireChemin(HashMap<Node, Node> allChemin, Node source, Node destination){
        HashMap<Node,Node> cheminVersDest = new HashMap<Node,Node>();
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
     * @param tp
     * @param nbRows
     */
    public static void genererGrille(Topology tp, int nbRows){
        int stepX = (tp.getWidth() - 100) / (nbRows - 1);
        int stepY = (tp.getHeight() - 100) / (nbRows - 1);
        if (Math.max(stepX, stepY) >= 2 * Math.min(stepX, stepY)){
            String s = "les proportions de la topologie sont inadaptées";
            JOptionPane.showMessageDialog(null, s);
            return;
        }
        tp.setCommunicationRange(Math.max(stepX, stepY)+1);
        for (int i = 50; i <= tp.getWidth() - 50; i += stepX){
            for (int j = 50; j <= tp.getHeight() - 50; j += stepY) {
                tp.addNode(i, j, new Node());
            }
        }
    }
}


