package org.greensvilla.qasidrelay;

import java.util.regex.Pattern;

final class PendingMessage {
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    final String id;
    final String to;
    final String body;

    PendingMessage(String id, String to, String body) {
        this.id = id == null ? "" : id.trim();
        this.to = to == null ? "" : to.trim();
        this.body = body == null ? "" : body;
    }

    static boolean hasValidId(String id) {
        return id != null && ID_PATTERN.matcher(id).matches();
    }

    String validationError() {
        if (!hasValidId(id)) {
            return "invalid_message_id";
        }
        if (to.isEmpty()) {
            return "empty_destination";
        }
        if (body.isEmpty()) {
            return "empty_body";
        }
        return null;
    }
}
