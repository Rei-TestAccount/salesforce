package com.prodly.qa.utils;

import java.util.function.Supplier;

/** Centralized retry with exponential backoff. */
public class RetryUtil {
    public static <T> T retry(Supplier<T> action, int attempts, long baseDelayMs) {
        Exception last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                last = e;
                long sleep = (long) (baseDelayMs * Math.pow(2, i));
                try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Retry failed after " + attempts + " attempts", last);
    }
}
