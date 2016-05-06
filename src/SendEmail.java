// File Name SendEmail.java

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;



public class SendEmail
{
	 
   public static void sendEmail(String note)
   {    
      String to = "minbaev@gmail.com";
      
      String from = "minbaev@gmail.com";

      String host = "smtp.googlemail.com";

      Properties props = System.getProperties();

      props = new Properties();
      
      props.put("mail.smtp.user", "minbaev@gmail.com");
      props.put("mail.smtp.host", "smtp.googlemail.com");
      props.put("mail.smtp.port", "465");
      props.put("mail.smtp.starttls.enable","true");
      props.put("mail.smtp.debug", "true");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.socketFactory.port", "465");
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.socketFactory.fallback", "false");
      
     
      GMailAuthenticator auth = new GMailAuthenticator("minbaev@gmail.com", "nyashmyash88!");
      Session session = Session.getInstance(props, auth);

      try{
         MimeMessage message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         message.setSubject("Alert from your server");
         message.setText(note);

         // Send message
         
         Transport transport = session.getTransport("smtps");
         transport.connect("smtp.googlemail.com", 465, "minbaev@gmail.com", "nyashmyash88!");
         Transport.send(message);
         transport.close();
         System.out.println("Sent message successfully....");
      }catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }

}