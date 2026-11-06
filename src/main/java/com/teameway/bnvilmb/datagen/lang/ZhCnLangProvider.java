package com.teameway.bnvilmb.datagen.lang;

import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 中文语言生成器
 *
 * <p>依照 {@code RegistrumLangProvider} 的结构实现：注册一个独立的 provider 类型，
 * 在 {@link #addTranslations()} 里回调本模组的翻译表，输出 {@code zh_cn.json}。</p>
 */
public final class ZhCnLangProvider extends LanguageProvider implements RegistrumProvider {
    /**
     * 中文 provider 类型，注册进 Registrum 的 provider 表
     */
    public static final ProviderType<ZhCnLangProvider> TYPE = ProviderType.registerProvider(
        "zh_cn_lang",
        context -> new ZhCnLangProvider(context.parent(), context.output())
    );

    private final AbstractRegistrum<?> owner;

    public ZhCnLangProvider(AbstractRegistrum<?> owner, PackOutput output) {
        super(output, owner.getModid(), "zh_cn");
        this.owner = owner;
    }

    @Override
    protected void addTranslations() {
        this.owner.genData(TYPE, this);
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
