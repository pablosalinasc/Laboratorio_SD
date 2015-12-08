/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laboratorio_1_sd;

import java.io.*;
import java.net.*;
/**
 *
 * @author ñuño
 */
public class Cliente {
    
    public static void main(String args[]) throws Exception{
        //Variables
        String sentence="";
        String fromServer;
        
        try{
            //Buffer para recibir desde el usuario
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            //Socket para el cliente (host, puerto)
            Socket clientSocket = new Socket("localhost", 5000);

            //Buffer para enviar el dato al server
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

            //Buffer para recibir dato del servidor
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while(!sentence.equals("exit")){
                //Leemos del cliente y lo mandamos al servidor
                System.out.print("Ingrese su consulta:");
                sentence = inFromUser.readLine();
                outToServer.writeBytes(sentence + '\n');

                //Recibimos del servidor
                fromServer = inFromServer.readLine();
                String[] partes=fromServer.split(",");
                System.out.println("Server response: ");
                for(int i=0;i<partes.length;i++){
                    System.out.println("   "+partes[i]);
                }
            }

            //Cerramos el socket
            clientSocket.close();
        }catch(IOException ex){
            System.out.println("\nALERTA: Servidor no disponible!!");
        }
    }
}
