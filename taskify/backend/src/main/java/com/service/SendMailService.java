
package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class SendMailService {

	@Autowired
	private JavaMailSender mailsender;

	public void sendResetPasswordEmail(String name, String email, String resetLink) {
		try {
			MimeMessage message = mailsender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			String userName = name;
			String companyName = "Taskify";
			String htmlContent = """
					    <!DOCTYPE html>
					    <html>
					    <head>
					        <meta charset="UTF-8" />
					        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
					        <style>
					            body {
					                margin: 0;
					                padding: 0;
					                background-color: #f4f4f4;
					                font-family: Arial, sans-serif;
					            }
					            .container {
					                max-width: 600px;
					                margin: 40px auto;
					                background-color: #ffffff;
					                padding: 30px;
					                border-radius: 8px;
					                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
					            }
					            h2 {
					                color: #333333;
					            }
					            p {
					                color: #555555;
					                line-height: 1.6;
					            }
					            .button {
					                display: inline-block;
					                padding: 12px 20px;
					                background-color: #007bff;
					                color: #ffffff !important;
					                text-decoration: none;
					                border-radius: 5px;
					                margin-top: 20px;
					            }
					            .footer {
					                text-align: center;
					                font-size: 12px;
					                color: #999999;
					                margin-top: 30px;
					            }
					        </style>
					    </head>
					    <body>
					        <div class="container">
					            <h2>Password Reset Request</h2>
					            <p>Hi %s,</p>
					            <p>We received a request to reset your password. If you didn't make this request, you can safely ignore this email.</p>
					            <p>Click the button below to reset your password:</p>
					            <a href="%s" class="button" target="_blank">Reset Password</a>
					            <p>This link will expire in 10 minutes for your security.</p>
					            <p>Thanks,<br />The %s Team</p>
					            <div class="footer">
					                If you’re having trouble, copy and paste this link into your browser:<br />
					                <a href="%s" style="color: #007bff;">%s</a>
					            </div>
					        </div>
					    </body>
					    </html>
					"""
					.formatted(userName, resetLink, companyName, resetLink, resetLink);

			helper.setFrom("noreply@taskify.com", "Taskify Support");
			helper.setTo(email);
			helper.setSubject("Reset Your Password");
			helper.setText(htmlContent, true); 
			mailsender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
