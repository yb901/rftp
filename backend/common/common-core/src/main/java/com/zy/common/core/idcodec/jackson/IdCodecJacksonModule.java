package com.zy.common.core.idcodec.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class IdCodecJacksonModule extends SimpleModule {

    public IdCodecJacksonModule() {
        super("zy-id-codec");
        setSerializerModifier(new IdCodecBeanSerializerModifier());
    }
}
