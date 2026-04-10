package com.codewithpcodes.salima.notification;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_RELATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(EmailNotificationRequest request) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_RELATED, UTF_8.name());
            helper.setFrom("awaprecious3t@gmail.com");

            final String templateName = request.templateName();

            Context context = new Context();
            context.setVariables(request.templateModel());

            String htmlTemplate = templateEngine.process(templateName, context);

            helper.setSubject(request.subject());
            helper.setText(htmlTemplate, true);
            helper.setTo(request.to());

            mailSender.send(mimeMessage);
            log.info("Email successfully sent to {} with template {}", request.to(), templateName);
        } catch (MessagingException e) {
            log.error("Error - Cannot send email to {}: {}", request.to(), e.getMessage());
        }
    }
}
