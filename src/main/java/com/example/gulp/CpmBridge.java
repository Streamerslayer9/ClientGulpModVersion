/*
 * Decompiled with CFR 0.152.
 */
package com.example.gulp;

public final class CpmBridge {
    private static volatile Client client;

    private CpmBridge() {
    }

    static void setClient(Client c) {
        client = c;
    }

    public static boolean available() {
        return client != null;
    }

    public static int maxValue(String animation) {
        Client c = client;
        if (c == null) {
            return -1;
        }
        try {
            return c.maxValue(animation);
        }
        catch (Throwable t) {
            return -1;
        }
    }

    public static boolean play(String animation, int value) {
        Client c = client;
        if (c == null) {
            return false;
        }
        try {
            return c.play(animation, value);
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static interface Client {
        public boolean play(String var1, int var2);

        public int maxValue(String var1);
    }
}

