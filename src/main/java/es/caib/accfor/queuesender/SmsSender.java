/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.caib.accfor.queuesender;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SmsSender extends TimerTask {
    public static final Integer CONST_SMS_MAX_LENGTH = 160;//Sin caracteres extendidos
    
    private Integer contador;
    private String smsResponse;    
    
    public SmsSender() {
        contador = 1;
        smsResponse="";
    }

    @Override
    public void run() {
        String phoneNumberOK="653 296 136";
        String phoneNumberKO="653 296 136";
        
        //TODO: Consultamos la lista de la BBDD y vamos enviando uno a uno
        String message="Enviado SMS número "+contador+++" a las ("+Date.from(Instant.now())+")...";
        smsResponse=enviarSMSAltiria(phoneNumberOK, message);
        //TODO: Controlar la respuesta y marcar los errores para reenvío
        System.out.println("Mensaje enviado: ["+message+"] --> ["+smsResponse+"]");

    }
    
   private static String enviarSMSAltiria(String phoneNumber, String message) {

        boolean hayError = false;
        // Formateamos el teléfono y comprobamos si es válido
        String smsNumber=normalizaSMSPhone(phoneNumber);
        if(smsNumber.trim().length()==0) return "Ko";
        
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(60000)
                .build();

        //Se inicia el objeto HTTP
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(config);
        CloseableHttpClient httpClient = builder.build();

        //Se fija la URL sobre la que enviar la petición POST
        HttpPost post = new HttpPost("http://www.altiria.net/api/http");

        //Se crea la lista de parámetros a enviar en la petición POST
        List parametersList = new ArrayList();
        
        parametersList.add(new BasicNameValuePair("cmd", "sendsms"));
        parametersList.add(new BasicNameValuePair("domainId", "inno"));
        parametersList.add(new BasicNameValuePair("login", "IFORMALIA"));
        parametersList.add(new BasicNameValuePair("passwd", "IFORMALIA"));
        parametersList.add(new BasicNameValuePair("dest", smsNumber));
        // Enviamos sólo los 160 caracteres que tiene de máximo el API. SI no devolvería un Error 013
        parametersList.add(new BasicNameValuePair("msg", (message.length()>160)?message.substring(0,CONST_SMS_MAX_LENGTH):message));
        parametersList.add(new BasicNameValuePair("senderId", "IFORMALIA"));

        try {
            //Se fija la codificacion de caracteres de la peticion POST
            post.setEntity(new UrlEncodedFormEntity(parametersList, "UTF-8"));
        } catch (Exception uex) {
            System.out.println("ERROR: codificación de caracteres no soportada");
        }

        CloseableHttpResponse response = null;

        try {
            System.out.println("Enviando petición");
            //Se envía la petición
            response = httpClient.execute(post);
            //Se consigue la respuesta
            String resp = EntityUtils.toString(response.getEntity());

            //Error en la respuesta del servidor
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("ERROR: Código de error HTTP:  " + response.getStatusLine().getStatusCode());
                System.out.println("Compruebe que ha configurado correctamente la direccion/url ");
                System.out.println("suministrada por Altiria");
                hayError=true;
            } else {
                //Se procesa la respuesta capturada en la cadena 'response'
                if (resp.startsWith("ERROR")) {
                    System.out.println(resp);
                    System.out.println("Código de error de Altiria. Compruebe las especificaciones");
                   hayError=true;
                } else {
                    System.out.println(resp);
                }
            }
        } catch (Exception e) {
            System.out.println("Excepción");
            e.printStackTrace();
            return "Ko";
        } finally {
            //En cualquier caso se cierra la conexión
            post.releaseConnection();
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ioe) {
                    System.out.println("ERROR cerrando recursos");
                }
            }
            return (hayError)?"Ko":"Ok";
        }
    }
   
    private static String normalizaSMSPhone(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String defaultCountry = "ES"; 
        String smsPhone="";
        try {
             Phonenumber.PhoneNumber number = phoneUtil.parseAndKeepRawInput(phoneNumber, defaultCountry);
             boolean isPossible = phoneUtil.isPossibleNumberForType(number, PhoneNumberUtil.PhoneNumberType.MOBILE);
             boolean isNumberValid = phoneUtil.isValidNumber(number);
             if(isPossible && isNumberValid)
                 smsPhone=phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
           return "";
        }   
        
        return smsPhone;       
    }
}
