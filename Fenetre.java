// Swing
import javax.swing.text.*;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

// Awt
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import java.io.File;
import java.util.ArrayList;

/**
 * Fenêtre du tchat
 * 
 * @author Mario HOTAJ
 * @see JFrame
 */
public class Fenetre extends JFrame{
  private static final long serialVersionUID = 1L;


  private ClientConnexion client = null;
  private String nom = "?";

    // Éléments constants
  // Nom
  private final JPanel nomPanel = new JPanel();
  private final JLabel nomLabel = new JLabel("Nom");
  private final JTextField nomText = new JTextField();

  // IP
  private final JPanel ipPanel = new JPanel();
  private final JLabel ipLabel = new JLabel("IP");
  private final JTextField ipText = new JTextField("127.0.0.1");

  // Port
  private final JPanel portPanel = new JPanel();
  private final JLabel portLabel = new JLabel("Port");
  private final JTextField portText = new JTextField("2345");

  JButton connectButton = new JButton("Connection");
  JButton envoyerButton = new JButton("Envoyer");

  private final DefaultListModel<String> listModel = new DefaultListModel<>();
  private final JTextPane discuTextPane= new JTextPane();
  private final JTextPane aideTextPane= new JTextPane();
  private final JPanel chatPanel = new JPanel();
  private final JTextArea msgTextArea = new JTextArea();
  

  public Fenetre(){
    discuTextPane.setEditable(false);

    // Configuration de la fenêtre
    this.setTitle("MSN Remasterised");
    this.setLayout(new GridBagLayout());
    this.setMinimumSize(new Dimension(450, 600));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null); //On centre la fenêtre sur l'écran 
    this.addWindowListener(new DecoAvantDeFermer());


    // Nom
    nomLabel.setHorizontalAlignment(JLabel.CENTER);
    nomPanel.setLayout(new GridLayout(1, 1));
    nomPanel.add(nomLabel);
    nomPanel.add(nomText);

    // IP
    ipLabel.setHorizontalAlignment(JLabel.CENTER);
    ipPanel.setLayout(new GridLayout(1,1));
    ipPanel.add(ipLabel);
    ipPanel.add(ipText);

    // Port
    portLabel.setHorizontalAlignment(JLabel.CENTER);
    portPanel.setLayout(new GridLayout(1,1));
    portPanel.add(portLabel);
    portPanel.add(portText);

    /*
      (HAUT) Box de connection (nom + ip + port + btn connection)
    */
    JPanel connectionPanel = new JPanel();
    connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
    connectionPanel.setLayout(new GridLayout(2, 2, 10, 10));
    connectionPanel.setSize(new Dimension(640, 480));
      // Ajout des 4
    connectionPanel.add(nomPanel);
    
    connectButton.addActionListener(new BoutonConnexionListener());
    connectionPanel.add(connectButton);
    connectionPanel.add(ipPanel);
    connectionPanel.add(portPanel);

    //-------------------------------------------------------

    // (BAS GAUCHE) Personnes connectées
    final JPanel chatGauchePanel = new JPanel();
    chatGauchePanel.setLayout(new GridLayout(2, 1, 70, 10));

    final JPanel connecteePanel = new JPanel();
    connecteePanel.setBorder(BorderFactory.createTitledBorder("Connectés"));
    connecteePanel.setLayout(new GridLayout(1, 1));
    connecteePanel.add(new JScrollPane(new JList<String>(listModel)));

    final JPanel aidePanel = new JPanel();
    aidePanel.setBorder(BorderFactory.createTitledBorder("Aide"));
    aidePanel.setLayout(new GridLayout(1, 1));
    aidePanel.add(new JScrollPane(aideTextPane));
    this.initAide();

    chatGauchePanel.add(connecteePanel);
    chatGauchePanel.add(aidePanel);

    // (BAS DROITE) Partie de droite
    final JPanel chatDroitePanel = new JPanel();
    chatDroitePanel.setLayout(new GridLayout(2, 1, 70, 10));

    // Discussion
    final JPanel discuPanel = new JPanel();
    discuPanel.setLayout(new GridLayout(1, 1));
    discuPanel.setBorder(BorderFactory.createTitledBorder("Discussion"));


    JScrollPane jsp = new JScrollPane(discuTextPane);
    jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
      public void adjustmentValueChanged(AdjustmentEvent e) {  
          e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
      }
    });

    discuPanel.add(jsp);

    // Message
    final JPanel msgPanel = new JPanel();
    msgPanel.setBorder(BorderFactory.createTitledBorder("Message"));
    msgPanel.setLayout(new GridLayout(2, 1, 10, 10));
    msgPanel.add(msgTextArea);
    
    envoyerButton.addActionListener(new BoutonEnvoiListener());
    msgPanel.add(envoyerButton);

    chatDroitePanel.add(discuPanel);
    chatDroitePanel.add(msgPanel);


    chatPanel.setLayout(new GridLayout(1, 2, 10, 10));
    chatPanel.add(chatGauchePanel);
    chatPanel.add(chatDroitePanel);
    chatPanel.setVisible(false);

    // ---------------------------------------------------------

    // Placement des 2 conteneurs (connectionPanel et chatPanel)
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.weightx =1;
    gbc.weighty =1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(connectionPanel, gbc);
    
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy = 1;
    gbc.weighty = 100;
    this.add(chatPanel, gbc);

    this.pack();
    this.setVisible(true);
  }


  /**
   * Initialisation du champs de texte <b>aideTextPane</b>
   */
  public void initAide(){
    try {
      // Configuration du style
      Style basic = aideTextPane.getStyle("default");
      Style titreStyle = aideTextPane.addStyle("titre", basic);


      StyleConstants.setForeground(titreStyle, Color.red);
      StyleConstants.setBold(titreStyle, true);
      StyleConstants.setItalic(titreStyle, true);
      StyleConstants.setUnderline(titreStyle, true);
      StyleConstants.setFontSize(titreStyle, 15);

      Document doc = aideTextPane.getDocument();

      doc.insertString(doc.getLength(), "Envoie de message privé :\n", titreStyle);
      doc.insertString(doc.getLength(), "/mp destinataire message\n\n", basic);

      doc.insertString(doc.getLength(), "Envoie de smiley :\n", titreStyle);
      doc.insertString(doc.getLength(), "il suffit d'insérer :nomdusmiley: dans votre message et celui sera changé automatiquement par un smiley lors de l'envoi\nVoici la liste de tout les smiley disponible :\n", basic);
      
      File folder = new File("emoji");
      File[] listOfFiles = folder.listFiles();

      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile())
          if(!listOfFiles[i].getName().equals(".DS_Store") && !listOfFiles[i].getName().equals("")){ // retirer fichier temporaire du mac un peu gênant
            String nomEmoji = listOfFiles[i].getName().replaceFirst("[.][^.]+$", "");
            StyledDocument docStyle = (StyledDocument) aideTextPane.getDocument();
            Style style = docStyle.addStyle(nomEmoji, null);
            StyleConstants.setIcon(style, new ImageIcon ( "emoji/" + nomEmoji + ".gif" ));
      
            docStyle.insertString(docStyle.getLength(), "invisible text", style);
            doc.insertString(doc.getLength(), ":" + nomEmoji + ":\n", basic); 
          }
      }
    } catch (BadLocationException e) {
      System.out.println("Problème lors de l'ajout de : " + e);
    }
  }


  /**
   * MÉTHODES DE GESTION DE CONNEXION
   */
  

  /**
   * Ajout de la nouvelle personne connectée dans la liste <b>listModel</b>
   * @param pers La personne connectée
   */
  public void connecter(String pers){
    listModel.addElement(pers);
  }

  /**
   * Indiquer au serveur que l'on se déconnecte et retour à la page initial.
   */
  private void seDeconnecter(){
    client.envoyer("DECO;" + nom);
    client.stop();
    this.dispose();
    System.out.println("On se déconnecte ");
    new Fenetre();
  }

  /**
   * Retirer de la liste <b>listModel</b> la personne déconnectée
   * @param pers La personne déconnectée
   */
  public void estDeconnecte(String pers){
    listModel.removeElement(pers);
  }

  /*
    TRAITEMENT DES CONTENEURS
  */

  /**
   * Ajout du message dans la zone de texte <b>discuTextPane</b>
   * @param enTete En-tête du message
   * @param msg Le message
   * @param estMP Vrai si c'est un booléan, faux sinon
   * @param color Couleur du texte
   */

  Style defaut = discuTextPane.getStyle("default");
  Style nouveauStyle = discuTextPane.addStyle("nouveauStyle", defaut);
  
  StyleConstants.setForeground(nouveauStyle, Color.red);
  StyleConstants.setFontSize(nouveauStyle, 11);

  Document doc = discuTextPane.getDocument();
  doc.insertString(doc.getLength(), "Le message", nouveauStyle);

  public void ajouterMessage(String enTete, String msg, Boolean estMP, Color color){
    try {
      // Configuration du style
      Style defaut = discuTextPane.getStyle("default");
      Style style2 = discuTextPane.addStyle("style2", defaut);
      StyleConstants.setForeground(style2, color);
      StyleConstants.setFontSize(style2, 11);

      Document doc = discuTextPane.getDocument();
      String[] separeMsg = msg.split(":");
      ArrayList<String> listeEmoji = new ArrayList<>();

      if(estMP) doc.insertString(doc.getLength(), "Message privé de ", style2);

      doc.insertString(doc.getLength(), enTete + " :\n", style2);
      
      // Lister les emoji dans le message
      for(String s : separeMsg){
        if(new File("emoji/"+ s +".gif").exists()){
          listeEmoji.add(s);
        }
      }

      if(!listeEmoji.isEmpty()){
        // Affichage du message avec les balise remplacé par les émojis
        String traitementMsg = msg;
        for(String s : listeEmoji){

          separeMsg = traitementMsg.split(":"+s+":",2);

          if(separeMsg[0].equals("")){
            traitementMsg = separeMsg[1];
          }else{
            doc.insertString(doc.getLength(), separeMsg[0], style2);
            traitementMsg = separeMsg[0];
          }

          StyledDocument docStyle = (StyledDocument) discuTextPane.getDocument();
          Style style = docStyle.addStyle(s, null);
          StyleConstants.setIcon(style, new ImageIcon ( "emoji/" + s + ".gif" ));
          docStyle.insertString(docStyle.getLength(), "invisible text", style);
        }
        doc.insertString(doc.getLength(), traitementMsg, style2);
      }else{
        doc.insertString(doc.getLength(), msg, style2);
      }

      if(estMP){
        doc.insertString(doc.getLength(), "\n\tPour répondre à son MP faites /mp "+ enTete +" message", style2);
      }
      doc.insertString(doc.getLength(), "\n\n", style2);

    } catch (BadLocationException e) {
      System.out.println("Problème lors de l'ajout de : " + e);
    }
  }


  /**
   * LES LISTENERS
   */
  
  /**
   * Listener du bouton d'envoi de message
   * @see ActionListener
   */
  class BoutonEnvoiListener implements ActionListener{
    /**
     * Envoie du message si un clic au bouton a été lu
     * @see ActionListener#actionPerformed
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      //Check si c'est un msg vide
      if(msgTextArea.getText().equals("")) return;

      //Check si c'est un message privé
      String[] checkMp = msgTextArea.getText().split(" ",3);
      if(checkMp[0].equals("/mp")){
        //On s'assure qu'il ne trafique pas notre envoie
        String pers = checkMp[1].replaceAll("[^A-Za-z0-9]", "");
        if(!checkMp[2].equals("")){
          ajouterMessage("MP de Vous à " + pers, checkMp[2], false, Color.orange);
          client.envoyer("MP;"+ pers + ";" + nom + ";" + checkMp[2]);
        }else{
          ajouterMessage("Erreur d'envoi: " + pers, "Impossible d'envoyer un message privé vide", false, Color.red);
        }
        
      }else{
        ajouterMessage("Vous", msgTextArea.getText(), false, Color.blue);
        client.envoyer("MSG;"+ nom + ";" + msgTextArea.getText());
      }
      msgTextArea.setText("");
    }
  }

  /**
   * Listener du bouton de connexion
   * @see ActionListener
   */
  class BoutonConnexionListener implements ActionListener{
    /**
     * Connexion du serveur si un clic au bouton a été lu
     * @see ActionListener#actionPerformed
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      try{
        if(nomText.getText().equals("")) return;
        //On fait en sorte d'avoir un nom contenant que des caratères alphanumériques
        nomText.setText(nomText.getText().replaceAll("[^A-Za-z0-9]", ""));

        System.out.println(ipText.getText() + Integer.parseInt(portText.getText()) + nomText.getText());
        client = new ClientConnexion(ipText.getText(), Integer.parseInt(portText.getText()), nomText.getText(), Fenetre.this);
        Thread t = new Thread(client);
        System.out.println(client);
        t.start();
        System.out.println(this);
        chatPanel.setVisible(true);
        nom = nomText.getText();

        // Textfield plus éditable
        nomText.setEditable(false);
        ipText.setEditable(false);
        portText.setEditable(false);

        // Fond gris pour bien comprendre que c'est plus éditable
        nomText.setBackground(Color.gray);
        ipText.setBackground(Color.gray);
        portText.setBackground(Color.gray);

        // Transformation en bouton de deconnexion
        connectButton.setText("Deconnexion");
        connectButton.removeActionListener(this);
        connectButton.addActionListener(new BoutonDeconnexionListener());

      }catch(NomIndisponibleException e){
        System.out.println("Erreur : " + e);
        JOptionPane.showMessageDialog(null, "Nom '" + nomText.getText() + "' indisponible", "Erreur", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * Listener du bouton de deconnexion
   * @see ActionListener
   */
  class BoutonDeconnexionListener implements ActionListener{
    /**
     * Deconnexion du serveur si un clic au bouton a été lu
     * @see ActionListener#actionPerformed
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      seDeconnecter();
    }
  }


  /**
   * Listener de la fenêtre
   * @see WindowAdapter
   */
  class DecoAvantDeFermer extends WindowAdapter{
    /**
     * Deconnexion avant la fermeture
     * @see WindowAdapter#windowClosing
     */
    @Override
    public void windowClosing(WindowEvent we) {
      if(client != null){
        client.envoyer("DECO;" + nom);
        client.stop();
      }
      
    }
  }

  public static void main(final String[] args){
    new Fenetre();
  }
}
