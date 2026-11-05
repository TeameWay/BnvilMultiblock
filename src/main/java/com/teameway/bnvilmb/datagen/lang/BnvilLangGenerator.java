package com.teameway.bnvilmb.datagen.lang;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class BnvilLangGenerator {
    public static void accept(RegistrumLangProvider provider) {
        BnvilGuiLangProvider.init(provider);
    }
}
