package com.zy.common.core.idcodec.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.zy.common.core.idcodec.annotation.IdEncode;

import java.util.List;

public class IdCodecBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            IdEncode annotation = writer.getAnnotation(IdEncode.class);
            if (annotation != null) {
                writer.assignSerializer(new IdEncodeJsonSerializer(annotation.gtZeroOnly()));
            }
        }
        return beanProperties;
    }
}
