package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import org.junit.Test;
public class SmokeTestProgressTest {
    @Test public void requiresNewSendAndAcknowledgementWithinSession() {
        assertTrue(SmokeTestProgress.canPass(true,true,true,true,true,true,true,true,0));
        assertFalse(SmokeTestProgress.canPass(true,true,true,true,false,true,true,true,0));
        assertFalse(SmokeTestProgress.canPass(true,true,true,true,true,false,true,true,0));
        assertFalse(SmokeTestProgress.canPass(true,true,true,true,true,true,false,true,0));
        assertFalse(SmokeTestProgress.canPass(true,true,true,true,true,true,true,true,2));
    }
    @Test public void neverClaimsCarrierDelivery() {
        assertFalse(SmokeTestProgress.summary(true,true).contains("message delivered"));
        assertTrue(SmokeTestProgress.summary(true,true).contains("NOT verified"));
    }
}
