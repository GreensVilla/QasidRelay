package org.greensvilla.qasidrelay;

final class PendingMessage {
    final String id;
    final String to;
    final String body;

    PendingMessage(String id, String to, String body) {
        this.id = id;
        this.to = to;
        this.body = body;
    }
}
