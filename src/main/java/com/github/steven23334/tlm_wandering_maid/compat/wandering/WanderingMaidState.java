package com.github.steven23334.tlm_wandering_maid.compat.wandering;

public enum WanderingMaidState {
    APPROACHING,
    RETRYING,
    WAITING,
    LEAVING,
    REJECTED;

    public static WanderingMaidState parse(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return REJECTED;
        }
    }
}