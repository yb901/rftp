package com.zy.common.core.idcodec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.common.core.idcodec.annotation.IdDecode;
import com.zy.common.core.idcodec.annotation.IdEncode;
import com.zy.common.core.idcodec.jackson.IdCodecJacksonModule;
import com.zy.common.core.idcodec.web.IdDecodeRequestBodyConverter;
import com.zy.common.utils.IdEncodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdCodecJacksonModuleTest {

    @Test
    void shouldEncodeAnnotatedLongFields() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new IdCodecJacksonModule());

        EncodeVo vo = new EncodeVo();
        vo.id = 123L;
        vo.ids = List.of(123L, 456L);
        vo.count = 123L;

        String json = objectMapper.writeValueAsString(vo);

        assertThat(json).contains("\"id\":\"" + IdEncodeUtil.encodeId(123L) + "\"");
        assertThat(json).contains("\"ids\":[\"" + IdEncodeUtil.encodeId(123L) + "\",\"" + IdEncodeUtil.encodeId(456L) + "\"]");
        assertThat(json).contains("\"count\":123");
    }

    @Test
    void shouldDecodeAnnotatedRequestBodyFields() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        IdDecodeRequestBodyConverter converter = new IdDecodeRequestBodyConverter(objectMapper);
        String body = """
                {"id":"%s","name":"demo","child":{"id":"%s"},"ids":["%s","%s"]}
                """.formatted(
                IdEncodeUtil.encodeId(123L),
                IdEncodeUtil.encodeId(456L),
                IdEncodeUtil.encodeId(1L),
                IdEncodeUtil.encodeId(2L));

        DecodeParam param = (DecodeParam) converter.read(DecodeParam.class, new StringHttpInputMessage(body));

        assertThat(param.id).isEqualTo(123L);
        assertThat(param.child.id).isEqualTo(456L);
        assertThat(param.ids).containsExactly(1L, 2L);
        assertThat(param.name).isEqualTo("demo");
    }

    static class EncodeVo {
        @IdEncode
        public Long id;

        @IdEncode
        public List<Long> ids;

        public Long count;
    }

    @IdDecode
    static class DecodeParam {
        @IdDecode
        public Long id;

        public String name;

        @IdDecode
        public ChildParam child;

        @IdDecode(type = Long.class)
        public List<Long> ids;
    }

    static class ChildParam {
        @IdDecode
        public Long id;
    }

    static class StringHttpInputMessage implements HttpInputMessage {
        private final String body;

        StringHttpInputMessage(String body) {
            this.body = body;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            return headers;
        }
    }
}
