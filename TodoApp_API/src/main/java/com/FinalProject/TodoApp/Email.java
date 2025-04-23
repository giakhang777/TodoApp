package com.FinalProject.TodoApp;

import com.FinalProject.TodoApp.entity.User;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Random;

@Component
public class Email {

    public String getRandom() {
        Random rnd = new Random();
        int number = rnd.nextInt(99999);
        return String.format("%04d", number);
    }

    // send email to the user email
    public boolean sendEmail(String email,String otp) {
        boolean test = false;
        String toEmail = email;
        String fromEmail = "nvh11103@gmail.com";
        String password = "qnua licf bppg emwm";
        try {

            Properties pr = configEmail(new Properties());
            Session session = Session.getInstance(pr, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });
            Message mess = new MimeMessage(session);
            mess.setHeader("Content-Type", "text/plain; charset=UTF-8");
            mess.setFrom(new InternetAddress(fromEmail));
            mess.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            mess.setSubject("Tài khoản tham gia của quý khách đã được đăng ký thành công.");
            mess.setText("Cảm ơn bạn đã tham gia! Đây là mã kích hoạt tài khoản của bạn : " + otp);
            Transport.send(mess);
            test = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return test;






    }
    public Properties configEmail(Properties pr) {
        pr.setProperty("mail.smtp.host", "smtp.gmail.com");
        pr.setProperty("mail.smtp.port", "587");
        pr.setProperty("mail.smtp.auth", "true");
        pr.setProperty("mail.smtp.starttls.enable", "true");
        pr.put("mail.smtp.socketFactory.port", "587");
        pr.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        return pr;
    }

}

