package com.altloc.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmailWithToken(String to, String subject, String token) throws MessagingException {
        String url = "http://localhost:3000/auth/email-verification?activation_token=" + token;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@yourdomain.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText("Please activate your account by clicking on the following link: \n" + url, true);

        javaMailSender.send(message);
    }
}
