import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Traitement du client par le serveur dans un thread
 * 
 * @author Mario HOTAJ
 * @see Runnable
 */
public class ClientProcessor implements Runnable{
   private Socket sock;
   private PrintWriter writer = null;
   private BufferedInputStream reader = null;
   private static HashMap<String, Socket> listeClient = new HashMap<>();
   
   private String nom = null;

   /**
    * Constructeur
    * @param pSock le socket
    */
   public ClientProcessor(Socket pSock){
      sock = pSock;
      try {
        writer = new PrintWriter(sock.getOutputStream(), true);
        reader = new BufferedInputStream(sock.getInputStream());
        
        //On attend de recevoir le nom du client
        String response = read();
        System.out.println("\t DEMANDE DE LA PART DE : " + response);
        
        //Envoie de la liste des personnes connecté si le nom du client n'est pas déjà pris, on envoie un message d'erreur sinon
        String retour = "";
        if(listeClient.containsKey(response)){
            retour = "ERREUR";
        }else{
            retour = this.listePersonneConnecte();
            nom = response;
            listeClient.put(nom, sock);
            envoyer("CONNECT;" + nom , nom);
        }

        writer.write(retour);
        writer.flush();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
   }
   
   /**
    * Envoie du message à tout le monde sauf à une personne exclu
    * @param msg Le message à envoyer
    * @param persExclu La Personne exclue
    */
   public void envoyer(String msg, String persExclu){
      try{
         for(Map.Entry<String, Socket> entry : listeClient.entrySet()){
               String p = entry.getKey();
               if(!p.equals(persExclu)){
                  Socket s = entry.getValue();
                  PrintWriter ecrire = new PrintWriter(s.getOutputStream());

                  ecrire.write(msg);
                  ecrire.flush();
               }
         }
      }catch(SocketException e){
         System.err.println("LA CONNEXION A ETE INTERROMPUE ! ");
      } catch (IOException e) {
         e.printStackTrace();
      }     
   }

   /**
    * @return La liste des personnes connectées concaténé par des ";"
    */
   public String listePersonneConnecte() {
      String res = "LISTE;";
      if(!listeClient.isEmpty()){
         for(Map.Entry<String, Socket> entry : listeClient.entrySet()){
            String p = entry.getKey();
            if(!p.equals(nom)){
               res += p + ";";
            }
         }
      }
      return res;
   }


   /**
    * Traitement serveur lancé dans un thread séparé
    * @see Runnable#run
    */
   public void run(){
      System.err.println("Lancement du traitement de la connexion cliente");

      //tant que la connexion est active, on traite les demandes
      boolean closeConnexion = false;
      while(!sock.isClosed()){
         
         try {
            
            //Ici, nous n'utilisons pas les mêmes objets que précédemment
            //Je vous expliquerai pourquoi ensuite
            writer = new PrintWriter(sock.getOutputStream());
            reader = new BufferedInputStream(sock.getInputStream());
            
            //On attend la demande du client
            String response = read();
            InetSocketAddress remote = (InetSocketAddress)sock.getRemoteSocketAddress();
            
            //On affiche quelques infos, pour le débuggage
            String debug = "";
            debug = "Thread : " + Thread.currentThread().getName() + ". ";
            debug += "Demande de l'adresse : " + remote.getAddress().getHostAddress() +".";
            debug += " Sur le port : " + remote.getPort() + ".\n";
            debug += "\t -> Commande reçue : " + response + "\n";
            System.err.println("\n" + debug);
            
            //On traite la demande du client en fonction de la commande envoyée
            String[] listeCommande = response.split(";",4);
            
            switch(listeCommande[0]){
               case "DECO":
                  listeClient.remove(listeCommande[1]);
                  envoyer(response, null);
                  closeConnexion = true;
                  break;
               case "MSG":
                  envoyer(response, listeCommande[1]);
                  
                  break;
               case "MP":
                  PrintWriter ecrire;
                  System.err.println("MP TRAITEMENT PAR LE SERVEUR");
                  if(listeClient.containsKey(listeCommande[1])){
                     System.err.println("MP CORRECT");
                     ecrire = new PrintWriter(listeClient.get(listeCommande[1]).getOutputStream());
                     ecrire.write(response);
                     ecrire.flush();
                  }else{
                     ecrire = new PrintWriter(listeClient.get(listeCommande[2]).getOutputStream());
                     ecrire.write("MPERROR;"+listeCommande[1]);
                     ecrire.flush();
                  }
                  break;
               default : 
                  System.out.println("Commande inconnue : " + response);               
                  break;
            }
            
            if(closeConnexion){
               System.err.println("COMMANDE CLOSE DETECTEE ! ");
               writer = null;
               reader = null;
               sock.close();
               break;
            }
         }catch(SocketException e){
            System.err.println("LA CONNEXION A ETE INTERROMPUE ! ");
            break;
         } catch (IOException e) {
            e.printStackTrace();
         }         
      }
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