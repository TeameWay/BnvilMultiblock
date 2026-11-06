package com.teameway.bnvilmb.datagen.lang;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

/**
 * 语言数据生成器
 *
 * <p>由 {@code ProviderType.LANG} 调用，条目内容交给 {@link BnvilGuiLangProvider} 写入。</p>
 */
public class BnvilLangGenerator {
    /**
     * 写入全部语言条目
     *
     * @param provider Registrum 语言 provider
     */
    public static void accept(RegistrumLangProvider provider) {
        BnvilGuiLangProvider.init(provider);
    }
}
