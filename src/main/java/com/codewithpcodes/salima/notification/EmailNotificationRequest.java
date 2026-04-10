package com.codewithpcodes.salima.notification;

import lombok.Builder;

import java.util.Map;

@Builder
public record EmailNotificationRequest(
        String to,
        String subject,
        String templateName,
        Map<String, Object> templateModel
) {
}
