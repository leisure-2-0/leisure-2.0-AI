package com.leisure.ai.global.config;

import ai.onnxruntime.OrtEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OnnxConfig {
    // OrtEnvironment.getEnvironment()는 ONNX Runtime이 내부적으로 관리하는 프로세스 전역 싱글턴.
    // 여기서는 다른 컴포넌트에 DI로 주입해서 쓰기 위해 빈으로만 노출.
    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }
}
