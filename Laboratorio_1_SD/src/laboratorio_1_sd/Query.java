/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laboratorio_1_sd;
import java.util.Date;
/**
 *
 * @author ñuño
 */
public class Query {
    String terminos;
    Date fecha;

    public Query(String terminos) {
        this.terminos = terminos;
        this.fecha = new Date();//retorna la fecha actual
    }
    
}
