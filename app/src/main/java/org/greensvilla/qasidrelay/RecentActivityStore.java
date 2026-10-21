package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Bounded, redacted UI-only activity. Never used for delivery or deduplication. */
final class RecentActivityStore {
    private static final String KEY = "ui_recent_activity_v1";
    private RecentActivityStore() { }

    static synchronized void record(Context context, String messageId, String state, String cause) {
        if (!PendingMessage.hasValidId(messageId)) {
            return;
        }
        String safeState = RecentActivityPolicy.state(state);
        if ("unknown".equals(safeState)) {
            return;
        }
        List<Entry> entries = read(context);
        entries.add(new Entry(System.currentTimeMillis(),
            RecentActivityPolicy.fingerprint(messageId), safeState,
            RecentActivityPolicy.cause(cause)));
        RecentActivityPolicy.keepLast20(entries);
        JSONArray array = new JSONArray();
        for (Entry entry : entries) {
            JSONObject object = new JSONObject();
            try {
                object.put("at", entry.timestamp);
                object.put("ref", entry.reference);
                object.put("state", entry.state);
                object.put("cause", entry.cause);
                array.put(object);
            } catch (Exception ignored) {
                // JSON is constructed from safe primitives and known values.
            }
        }
        prefs(context).edit().putString(KEY, array.toString()).apply();
    }

    static synchronized List<Entry> recent(Context context) {
        List<Entry> result = read(context);
        Collections.reverse(result);
        return result;
    }

    static synchronized void clear(Context context) {
        // Never touch the SMS duplicate ledger, in-flight attempts, or report queue.
        prefs(context).edit().remove(KEY).apply();
    }

    private static List<Entry> read(Context context) {
        ArrayList<Entry> items = new ArrayList<>();
        JSONArray stored;
        try {
            stored = new JSONArray(prefs(context).getString(KEY, "[]"));
        } catch (Exception ignored) {
            return items;
        }
        for (int i = 0; i < stored.length(); i++) {
            JSONObject item = stored.optJSONObject(i);
            if (item == null) {
                continue;
            }
            String ref = item.optString("ref", "");
            if (!ref.matches("job-[0-9a-f]{10}")) {
                continue;
            }
            String state = RecentActivityPolicy.state(item.optString("state", ""));
            if ("unknown".equals(state)) {
                continue;
            }
            items.add(new Entry(Math.max(0L, item.optLong("at", 0L)),
                ref, state, RecentActivityPolicy.cause(item.optString("cause", ""))));
        }
        RecentActivityPolicy.keepLast20(items);
        return items;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
    }

    static final class Entry {
        final long timestamp;
        final String reference;
        final String state;
        final String cause;

        Entry(long timestamp, String reference, String state, String cause) {
            this.timestamp = timestamp;
            this.reference = reference;
            this.state = state;
            this.cause = cause;
        }
    }
}
