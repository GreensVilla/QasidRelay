package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

final class RelayMessageLedger {
    private static final String KEY_RECENT_MESSAGE_IDS = "recent_message_ids";
    private static final int MAX_RECENT_IDS = 200;

    private RelayMessageLedger() {
    }

    static synchronized boolean hasSeen(Context context, String messageId) {
        if (!PendingMessage.hasValidId(messageId)) {
            return true;
        }
        return load(context).contains(messageId);
    }

    static synchronized void remember(Context context, String messageId) {
        if (!PendingMessage.hasValidId(messageId)) {
            return;
        }

        List<String> ids = load(context);
        ids.remove(messageId);
        ids.add(messageId);
        while (ids.size() > MAX_RECENT_IDS) {
            ids.remove(0);
        }
        save(context, ids);
    }

    private static List<String> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
        List<String> ids = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(prefs.getString(KEY_RECENT_MESSAGE_IDS, "[]"));
        } catch (Exception error) {
            return ids;
        }
        for (int index = 0; index < array.length(); index++) {
            String id = array.optString(index, "");
            if (PendingMessage.hasValidId(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void save(Context context, List<String> ids) {
        JSONArray array = new JSONArray();
        for (String id : ids) {
            array.put(id);
        }
        context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENT_MESSAGE_IDS, array.toString())
            .apply();
    }
}
