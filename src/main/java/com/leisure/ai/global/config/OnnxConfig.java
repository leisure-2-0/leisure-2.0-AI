package com.leisure.ai.global.config;

// TODO
// - ONNX Runtime의 OrtEnvironment는 프로세스당 하나만 두는 게 권장되므로,
//   공유 OrtEnvironment 빈을 여기서 등록 (embedding.BgeM3EmbeddingModel, embedding.OnnxRerankerModel이 재사용)
// - 모델 파일 경로(application.yml의 onnx.models.*)를 읽어 각 컴포넌트에 전달
