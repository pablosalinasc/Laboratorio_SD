package laboratorio_1_sd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.math.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author psalinasc
 */
public class CacheService {

    
    int sizeDinamico;
    int sizeEstatico;
    LinkedHashMap<String, Result>[] cacheDinamico;//[particiones]
    LinkedHashMap<String, Result> cacheEstatico;
    static int HebraActual;
    Result[] resultadoTemp;
    static Thread[] t;
    int[] sizeParticiones;
    int nParticiones;
    int[] nResultsParticiones;//Guarda la cantidad de elementos dentro de cada particion
    int nHebras;
    static int total_hits = 0, total_miss = 0;
    static BufferedReader inFromIndex;
    static DataOutputStream outToIndex;
    
    public long Hash(String s){ 
        long result = 0;
        for (int i = 0; i < s.length(); i++)
           result += (long)Math.pow(27, Integer.MAX_VALUE - i - 1)*(1 + s.charAt(i) - 'a');
        return result;
    }
    
    public void refrescoCache(int numParticion, Query query, Result result){
            cacheDinamico[numParticion].remove(query.terminos);
            cacheDinamico[numParticion].put(query.terminos, result);
        nResultsParticiones[numParticion]++;
    }
    
    public Result getResult(Query query) throws InterruptedException {
        Lock l=new ReentrantLock();
        int pid;
        l.lock();
        try {
            pid=HebraActual;
            HebraActual=(HebraActual+1)%nHebras;
        } finally {
            l.unlock();
        }
        //circularmente se accede a la hebra para manejar la funcion de ingreso de datos al cache
        t[pid]=new Thread(){
            @Override
            public void run(){
                int encontrado=0;
                resultadoTemp[pid]=null;
                System.out.println("Ingresa a getResult con la hebra "+pid);
                if(cacheEstatico.containsKey(query.terminos)){
                    encontrado=1;
                    resultadoTemp[pid]=cacheEstatico.get(query.terminos);
                }
                int hashParticion=(int) ((java.lang.Math.abs(Hash(query.terminos)))%nParticiones);
                //busca en cada particion hasta que encuentra la correcta
                int numParticion=-1;
                if(encontrado==0){
                    if(cacheDinamico[hashParticion].containsKey(query.terminos)){
                        encontrado=1;
                        numParticion=hashParticion;
                        resultadoTemp[pid]=cacheDinamico[hashParticion].get(query.terminos);
                        refrescoCache(numParticion,query,resultadoTemp[pid]);
                    }
                }
                if(encontrado==1){
                    total_hits++;
                    print();
                }else{
                    total_miss++;
                }
                System.out.println("Encontrado: "+encontrado);
            }
        };
        t[pid].start();
        t[pid].join();
        return resultadoTemp[pid];

    }

    public CacheService(int size, int nHebras,int nParticiones) {
        this.sizeDinamico =(int) Math.rint(0.8*size);
        this.sizeEstatico =(int) Math.rint(0.2*size);
        System.out.println("Size Dinamino: "+sizeDinamico+"\nSize Estatico: "+sizeEstatico);
        this.cacheEstatico= new LinkedHashMap<>();
        this.cacheDinamico = new LinkedHashMap[nParticiones];
        for(int j=0;j<nParticiones;j++){
            this.cacheDinamico[j]=new LinkedHashMap<>();
        }
        this.sizeParticiones=new int[nParticiones];
        //calcula tamano de particiones
        System.out.print("Size particiones: ");
        for(int i=0;i<nParticiones;i++){
            if(i<(nParticiones-1)){
                this.sizeParticiones[i]=sizeDinamico/nParticiones;
                System.out.print(this.sizeParticiones[i]+" ");
            }else if(i==nParticiones-1){
                this.sizeParticiones[i]=sizeDinamico-sizeDinamico/nParticiones*(nParticiones-1);
                System.out.print(this.sizeParticiones[i]+" ");
            }
        }
        System.out.println("\n");
        this.t=new Thread[nHebras];
        this.nHebras=nHebras;
        this.HebraActual=0;
        this.nParticiones=nParticiones;
        this.resultadoTemp=new Result[nHebras];
        this.nResultsParticiones=new int[nParticiones];
        for(int i=0;i<nParticiones;i++){
            this.nResultsParticiones[i]=0;
        }
        try {
            //llenado de cache estatico
            System.out.println("Envia a Index: sizeEstatico "+this.sizeEstatico);
            outToIndex.writeBytes(this.sizeEstatico+"\n");
            for(int i=0;i<this.sizeEstatico;i++){
                String query=inFromIndex.readLine();
                String nombreSitio=inFromIndex.readLine();
                String url=inFromIndex.readLine();
                Result result=new Result(nombreSitio,url);
                cacheEstatico.put(query, result);
                System.out.println("Recibe de Index: '"+query+"' '"+nombreSitio+"' '"+url+"'");
            }
        } catch (IOException ex) {
            Logger.getLogger(CacheService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addResult(Query query, Result result) {
        
        Lock l=new ReentrantLock();
        int pid;
        l.lock();
        try {
            pid=HebraActual;
            HebraActual=(HebraActual+1)%nHebras;
        } finally {
            l.unlock();
        }
        t[pid]=new Thread(){
            @Override
            public void run(){
                System.out.println("Ingresa a addResult con la hebra "+pid);
                int hashParticion=(int) ((java.lang.Math.abs(Hash(query.terminos)))%nParticiones);
                int encontrado=0;
                if (cacheDinamico[hashParticion].containsKey(query.terminos)) { // HIT particion
                    // Bring to front
                    System.out.println("Hit de la particion "+hashParticion);
                    refrescoCache(hashParticion, query, result);
                    encontrado=1;
                }
                
                if(encontrado==0){ //MISS particion

                    System.out.println("Miss de la particion "+hashParticion);
                    if(cacheDinamico[hashParticion].size() == sizeParticiones[hashParticion]) {//particion llena
                        System.out.println("Particion llena");
                        String first_element = cacheDinamico[hashParticion].entrySet().iterator().next().getKey();
                        System.out.println("Removiendo: '" + first_element + "'");
                        cacheDinamico[hashParticion].remove(first_element);
                        cacheDinamico[hashParticion].put(query.terminos, result);
                    }else{//particion no llena
                        System.out.println("Particion no llena");
                        cacheDinamico[hashParticion].put(query.terminos, result);
                        nResultsParticiones[hashParticion]++;
                        
                    }
                    
                }
                print();
            }
            
        };
        
        t[pid].start();
    }

    public void print() {
        System.out.println("\n========= Cache ==========");
        System.out.println("------Cache Estatico------");
        System.out.print("|");
        Object queriesEst[]=cacheEstatico.keySet().toArray();
        for(int i=0;i<sizeEstatico;i++){
            String temp= (String) queriesEst[i];
            System.out.print("'"+queriesEst[i]+"'|");
        }
        System.out.println("\n------Cache Dinamico------");
        System.out.print("|");
        for(int j=0;j<nParticiones;j++){
            Object[] queriesDin=cacheDinamico[j].keySet().toArray();
            for(int i=0;i<queriesDin.length;i++){
                String temp= (String) queriesDin[i];
                System.out.print("P"+j+" '"+temp+"'|");
            }
        }
        System.out.println("\n==========================");
        System.out.println("Total Hits: "+total_hits+"  Total Miss: "+total_miss+"\n");
    }

    public static void main(String[] args) throws Exception{
        //Variables
        String fromFront="";
        String processedData;    

        try{
            //Socket (server)CACHE-FRONT en el puerto 4000
            ServerSocket acceptSocket = new ServerSocket(4000);
            //Socket (server)INDEX-CACHE en el puerto 3200
            Socket clientSocket = new Socket("localhost",3200);
            
            System.out.println("Cache service is running...\n");

            //Socket listo para recibir 
            Socket connectionSocket1 = acceptSocket.accept();

            //Buffer para recibir desde el cliente
            BufferedReader inFromFront = new BufferedReader(new InputStreamReader(connectionSocket1.getInputStream()));
            //Buffer para recibir del cache
            inFromIndex = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Buffer para enviar al cliente
            DataOutputStream outToFront = new DataOutputStream(connectionSocket1.getOutputStream());
            //Buffer para consultar al cache
            outToIndex = new DataOutputStream(clientSocket.getOutputStream());
            
            //Abrir archivo de configuracion
            File archivo = new File ("config.ini");
            FileReader fr = new FileReader (archivo);
            BufferedReader br = new BufferedReader(fr);
            String linea = br.readLine();
            String[] parametros=linea.split(" ");
            CacheService cacheService=new CacheService(Integer.parseInt(parametros[0]),
                    Integer.parseInt(parametros[2]),
                    Integer.parseInt(parametros[3]));
            
            
            while(true){

                //Recibimos el dato del front service y lo mostramos en el server
                fromFront =inFromFront.readLine();
                System.out.println("Recibe de Front: " + fromFront);

                String[] tokens = fromFront.split(" ");
                String parametrosREST = tokens[1];

                String http_method = tokens[0];

                String[] tokens_parametros = parametrosREST.split("/");

                String terminos = tokens_parametros.length > 2 ? tokens_parametros[2] : "";
                
                if(http_method.equals("GET")){
                    String terminosNorm=terminos.replace("+", " ");
                    System.out.println("Se reconoce un GET con terminos: '"+terminosNorm+"'");
                    Query query= new Query(terminosNorm);
                    Result resultado=null;
                    resultado=cacheService.getResult(query);
                    if(resultado==null){
                        outToFront.writeBytes("0\n");
                        System.out.println("Envia a cache: 0");
                        //recibe tag desde front para ver si agrega una nueva entrada
                        String tagNuevaEntrada=inFromFront.readLine();
                        if(tagNuevaEntrada.equals("1")){
                            //recibe nueva entrada desde el index
                            fromFront=inFromFront.readLine();
                            System.out.println("Recibe de Front: " + fromFront);

                            tokens = fromFront.split(" ");
                            parametrosREST = tokens[1];

                            http_method = tokens[0];

                            tokens_parametros = parametrosREST.split("/");

                            terminos = tokens_parametros.length > 2 ? tokens_parametros[2] : "";
                            
                            if(http_method.equals("POST")){
                                String[] tokenTerminos = terminos.split("\\+");
                                System.out.println("Reconoce POST con tokens: '"+tokenTerminos[0]+"' '"+tokenTerminos[1]+"' '"+tokenTerminos[2]+"'");
                                query= new Query(tokenTerminos[0]);
                                resultado=new Result(tokenTerminos[1],tokenTerminos[2]);
                                cacheService.addResult(query,resultado);
                            }
                        }
                    }else{
                        //enviar query al frontService
                        outToFront.writeBytes("1\n");
                        outToFront.writeBytes(resultado.nombreSitio+"\n");
                        outToFront.writeBytes(resultado.url+"\n");
                    }
                    
                }
            }
        }catch(IOException ex){
            System.out.println("\n---------------------------\nSe cerro la conexión con el Front Service\n-----------------------------\n");
        }
    }
}
