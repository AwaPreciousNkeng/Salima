package com.codewithpcodes.salima.notification;

import lombok.Getter;

public enum EmailTemplate {
    RESET_PASSWORD("password-reset.html", "Reset Your Password");

    @Getter
    private final String template;

    @Getter
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
