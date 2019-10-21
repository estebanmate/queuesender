/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.caib.accfor.queuesender;

import java.util.Timer;

/**
 *
 * @author portatil2
 */
public class QueueSender {
    
    // Intervalo en segundos entre cada ejecución de la tarea (20 seg. para pruebas)
    public static Integer CRON_INTERVAL = 20;
    
    public static void main(String[] args) {
    
        //Creamos la tarea de ir leyendo la BBDD para enviar o no los SMS
        SmsSender smsSend = new SmsSender();
        Timer taskExecutor = new Timer();

        taskExecutor.scheduleAtFixedRate(smsSend, 0, 1000*CRON_INTERVAL);

    }
}
