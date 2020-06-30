import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.Color;

/**
 * Traitement du client par le serveur dans un thread
 * 
 * @author Mario HOTAJ
 * @see Runnable
 */
public class ClientConnexion implements Runnable{

   private Socket connexion = null;
   private PrintWriter writer = null;
   private BufferedInputStream reader = null;
   
   //Notre liste de commandes. Le serveur nous répondra différemment selon la commande utilisée.
   private String nom;
   private Fenetre fenetre;

   private Boolean isRunning = true;
   
   /**
    * Constructeur de la connexion client
    * @param host L'hôte
    * @param port Le port
    * @param nom Le nom du client
    * @param fenetre La fênetre dans laquel on fera du traitement plus tard
    * @throws NomIndisponibleException Le nom est déjà pris
    */
   public ClientConnexion(String host, int port, String nom, Fenetre fenetre) throws NomIndisponibleException{
        this.nom = nom;
        this.fenetre = fenetre;
        try {
            connexion = new Socket(host, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            writer = new PrintWriter(connexion.getOutputStream(), true);
            reader = new BufferedInputStream(connexion.getInputStream());
            
            //On envoie le nom du client au serveur
            writer.write(nom);
            writer.flush();  
            
            System.out.println("INITIALISATION : Nom '" + nom + "' envoyée au serveur");
            
            //On attend la réponse
            String response = read();
            System.out.println("\t INITIALISATION : REPONSE : '" + response +"'\n");
            if(response.equals("ERREUR")) throw new NomIndisponibleException("Nom indisponible :(");
            System.out.println("LISTE DES PERONNES CONNECTEES : " + response);
            
            String[] liste = response.split(";");
            for(int i = 1; i < liste.length; i++){
               fenetre.connecter(liste[i]);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
   }
   
   /**
    * Envoie du message au serveur
    * @param msg Le message à envoyer
    */
   public void envoyer(String msg){
      try{
         PrintWriter writer = new PrintWriter(connexion.getOutputStream(), true);
         writer.write(msg);
         writer.flush();
      }catch(SocketException e){
         System.err.println("LA CONNEXION A ETE INTERROMPUE ! ");
      }catch(IOException e){
         e.printStackTrace();
      }     
   }

   /**
    * Traitement client lancé dans un thread séparé
    * @see Runnable#run
    */
   public void run(){
      while(isRunning == true){
         //Thread.currentThread();
         try {
               reader = new BufferedInputStream(connexion.getInputStream());
               
               //On attend la réponse
               String response = read();
               System.out.println("\t * " + nom + " : Réponse reçue " + response);
               String[] listeCommande = response.split(";",4);

               switch(listeCommande[0]){
                  case "DECO":
                     fenetre.estDeconnecte(listeCommande[1]);
                     break;
                  
                  case "CONNECT":
                     fenetre.connecter(listeCommande[1]);
                     break;

                  case "MSG":
                     fenetre.ajouterMessage(listeCommande[1], listeCommande[2], false, Color.black);
                     break;

                  case "MP":
                     fenetre.ajouterMessage(listeCommande[2], listeCommande[3], true, Color.green);
                     break;

                  case "MPERROR":
                     System.err.println(response);
                     fenetre.ajouterMessage("Message serveur", "Le correspondant " + listeCommande[1] + " n'existe pas", false, Color.red);
                     break;
                  
                  
                  default :
                     System.err.println("Commande inconnue : " + listeCommande[0]);
                     break;
               }
         } catch (IOException e1) {
               e1.printStackTrace();
         }
      }
   }
   
   /**
    * Arrêt du thread
    */
   public void stop(){
      isRunning = false;
      writer.close();
   }
   
   /**
    * Lecture de la réponse
    * @return le message reçu
    * @throws IOException
    */
   private String read() throws IOException{      
      String response = "";
      int stream;
      byte[] b = new byte[4096];
      stream = reader.read(b);
      response = new String(b, 0, stream);      
      return response;
   }
}