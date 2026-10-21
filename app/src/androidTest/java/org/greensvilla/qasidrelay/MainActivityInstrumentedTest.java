package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Test public void homeRendersCoreOperatorSectionsWithoutGateway() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        try {
            View root = activity.getWindow().getDecorView();
            assertTrue(findText(root, "RELAY OVERVIEW"));
            assertTrue(findText(root, "Recent activity"));
            assertTrue(findText(root, "Support & diagnostics"));
            assertTrue(findText(root, "Guided device test"));
            assertTrue(findText(root, "Operator lock"));
            assertTrue(findText(root, "App version"));
            assertTrue(findText(root, "Policy not reported by server"));
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(activity::finish);
        }
    }

    private boolean findText(View view, String text) {
        if (view instanceof TextView) {
            CharSequence value = ((TextView) view).getText();
            if (value != null && value.toString().contains(text)) {
                return true;
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (findText(parent.getChildAt(i), text)) {
                    return true;
                }
            }
        }
        return false;
    }
}
