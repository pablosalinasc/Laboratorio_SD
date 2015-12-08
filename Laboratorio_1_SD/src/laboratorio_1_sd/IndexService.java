package laboratorio_1_sd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author psalinasc
 */
public class IndexService {
    
    static Query[] queries;
    static Result[] answers;
    
    public IndexService(int size) {
        queries=new Query[size];
        answers=new Result[size];
        for(int i=0;i<size;i++){
            String terminos="query_"+(i+1);
            queries[i]=new Query(terminos);
            String url="answer"+(i+1)+".com";
            String nombreSitio="answer_"+(i+1);
            answers[i]=new Result(nombreSitio,url);
        }
    }
    
    
    public static Result getResult(String query) {
        for (int i = 0; i < queries.length; i++) {
            if (queries[i].terminos.equals(query)) {
                return answers[i];
            }
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception{
        //Variables
        String fromFront="";
        
        
        try{
            //Socket (server)INDEX-FRONT en el puerto 3500
            ServerSocket acceptSocket1 = new ServerSocket(3500);
            //Socket (server)INDEX-CACHE en el puerto 3200
            ServerSocket acceptSocket2 = new ServerSocket(3200);

            System.out.println("Index service is running...\n");

            //Socket listo para recibir 
            Socket connectionSocket1 = acceptSocket1.accept();
            Socket connectionSocket2 = acceptSocket2.accept();

            //Buffer para recibir desde el FrontService
            BufferedReader inFromFront = new BufferedReader(new InputStreamReader(connectionSocket1.getInputStream()));
            //Buffer para recibir desde el CacheService
            BufferedReader inFromCache = new BufferedReader(new InputStreamReader(connectionSocket2.getInputStream()));
            //Buffer para enviar al FrontService
            DataOutputStream outToFront = new DataOutputStream(connectionSocket1.getOutputStream());
            //Buffer para enviar al CacheService
            DataOutputStream outToCache = new DataOutputStream(connectionSocket2.getOutputStream());            
            //Abrir archivo de configuracion
            File archivo = new File ("config.ini");
            FileReader fr = new FileReader (archivo);
            BufferedReader br = new BufferedReader(fr);
            String linea = br.readLine();
            String[] parametros=linea.split(" ");
            IndexService indice=new IndexService(Integer.parseInt(parametros[1]));
            //Envia datos estáticos al cache
            System.out.println("Esperando recibir desde cache");
            String temp=inFromCache.readLine();
            System.out.println("Recibe de Cache: tamaño cache estatico "+temp);
            int sizeCacheEstatico=Integer.parseInt(temp);
            for(int i=0;i<sizeCacheEstatico;i++){
                outToCache.writeBytes(queries[i].terminos+"\n");
                outToCache.writeBytes(answers[i].nombreSitio+"\n");
                outToCache.writeBytes(answers[i].url+"\n");
                System.out.println("Envia a Cache: '"+queries[i].terminos+"' '"+answers[i].nombreSitio+"' '"+answers[i].url+"' '");
            }
            
            while(true){
                //Recibimos el dato del cliente y lo mostramos en el server
                fromFront =inFromFront.readLine();
                System.out.println("Received: " + fromFront);

                String[] tokens = fromFront.split(" ");
                String parametrosREST = tokens[1];

                String http_method = tokens[0];

                String[] tokens_parametros = parametrosREST.split("/");

                String terminos = tokens_parametros.length > 2 ? tokens_parametros[2] : "";
                
                if(http_method.equals("GET")){
                    String terminosNorm=terminos.replace("+", " ");
                    Query query= new Query(terminosNorm);
                    Result resultado=indice.getResult(query.terminos);
                    //envía el resultado al FrontService
                    if(resultado==null){
                        outToFront.writeBytes("0\n");
                        System.out.println("Envia a Front: '0'");
                    }else{
                        outToFront.writeBytes("1\n");
                        outToFront.writeBytes(resultado.nombreSitio+"\n");
                        outToFront.writeBytes(resultado.url+"\n");
                        System.out.println("Envia a Front: '1' '"+resultado.nombreSitio+"' '"+resultado.url+"'");
                    }
                }
            }
        }catch(IOException ex){
            System.out.println("\n---------------------------\nSe cerro la conexión con el Cache\n-----------------------------\n");
        }
    }
    
}
