# LLM 내부 동작 원리: 백엔드 개발자 관점에서의 6단계 이해

## 1. 한 줄 정의
- LLM(대형 언어 모델)은 트랜스포머 아키텍처를 기반으로 토큰 단위로 텍스트를 처리하여 다음 단어를 예측하는 확률적 시스템이다. 백엔드 관점에서는 거대한 행렬 연산과 병렬 처리를 통해 패턴을 학습하고 추론하는 분산 시스템으로 이해할 수 있다.

---

## 2. LLM 동작 원리 6단계

### 2-1. 1단계: 토큰화(Tokenization)
- **개념**: 입력 텍스트를 모델이 이해할 수 있는 숫자 토큰으로 변환
- **백엔드 관점**: 문자열 파싱과 유사하며, 토큰화는 전처리 파이프라인의 첫 번째 단계
- **핵심 포인트**: 
  - BPE(Byte-Pair Encoding), WordPiece 등의 알고리즘 사용
  - 어휘 사전(vocabulary) 크기에 따라 메모리 사용량 결정
  - 토큰 길이 제한(context window)이 처리 가능한 입력 크기를 결정

```python
# 토큰화 예시 (개념적)
input_text = "백엔드 개발자입니다"
tokens = tokenizer.encode(input_text)
# tokens = [1234, 5678, 9012, 3456]
```

### 2-2. 2단계: 임베딩(Embedding)
- **개념**: 토큰을 고차원 벡터 공간으로 매핑하여 의미적 표현 생성
- **백엔드 관점**: 해시 테이블 룩업과 유사하지만, 각 토큰이 수백~수천 차원의 벡터로 변환
- **핵심 포인트**:
  - 임베딩 테이블은 모델 파라미터의 상당 부분을 차지
  - 위치 임베딩(positional embedding)을 통해 토큰 순서 정보 보존
  - 메모리 효율성을 위한 임베딩 압축 기법 존재

```python
# 임베딩 변환 예시 (개념적)
token_ids = [1234, 5678, 9012]
embeddings = embedding_layer(token_ids)
# embeddings.shape = [3, 768]  # 3개 토큰, 각각 768차원
```

### 2-3. 3단계: 어텐션 메커니즘(Attention Mechanism)
- **개념**: 입력 시퀀스의 각 위치가 다른 위치들과 얼마나 관련이 있는지 계산
- **백엔드 관점**: 가중치 기반 조인(weighted join) 연산과 유사
- **핵심 포인트**:
  - Self-Attention: 같은 시퀀스 내에서 토큰 간 관계 계산
  - Multi-Head Attention: 여러 관점에서 동시에 어텐션 계산
  - 계산 복잡도 O(n²)로 시퀀스 길이에 제곱 비례

```python
# 어텐션 계산 예시 (개념적)
def attention(query, key, value):
    # Q * K^T / sqrt(d_k)
    scores = torch.matmul(query, key.transpose(-2, -1)) / math.sqrt(key.size(-1))
    weights = torch.softmax(scores, dim=-1)
    output = torch.matmul(weights, value)
    return output
```

### 2-4. 4단계: 피드포워드 네트워크(Feed-Forward Network)
- **개념**: 어텐션 결과를 비선형 변환하여 복잡한 패턴 학습
- **백엔드 관점**: 2층 완전 연결 네트워크, ReLU 활성화 함수 사용
- **핵심 포인트**:
  - 중간 차원이 임베딩 차원의 4배 정도 (예: 768 → 3072 → 768)
  - 모델 파라미터의 약 2/3를 차지하는 가장 큰 구성요소
  - 병렬 처리에 최적화된 행렬 곱셈 연산

```python
# FFN 구조 예시
class FeedForward(nn.Module):
    def __init__(self, d_model, d_ff):
        self.linear1 = nn.Linear(d_model, d_ff)  # 768 -> 3072
        self.linear2 = nn.Linear(d_ff, d_model)  # 3072 -> 768
        self.relu = nn.ReLU()
    
    def forward(self, x):
        return self.linear2(self.relu(self.linear1(x)))
```

### 2-5. 5단계: 트랜스포머 블록 스택킹
- **개념**: 어텐션 + FFN을 하나의 블록으로 하여 여러 층 반복
- **백엔드 관점**: 레이어드 아키텍처와 유사, 각 층이 점진적으로 추상화 수행
- **핵심 포인트**:
  - GPT-3는 96개 층, GPT-4는 추정 120개 이상 층
  - 잔차 연결(residual connection)과 레이어 정규화로 안정성 확보
  - 깊이가 깊을수록 복잡한 추론 능력 향상

```python
# 트랜스포머 블록 예시
class TransformerBlock(nn.Module):
    def __init__(self, d_model, n_heads):
        self.attention = MultiHeadAttention(d_model, n_heads)
        self.ffn = FeedForward(d_model, d_model * 4)
        self.ln1 = nn.LayerNorm(d_model)
        self.ln2 = nn.LayerNorm(d_model)
    
    def forward(self, x):
        # 잔차 연결 + 레이어 정규화
        x = x + self.attention(self.ln1(x))
        x = x + self.ffn(self.ln2(x))
        return x
```

### 2-6. 6단계: 출력 및 다음 토큰 예측
- **개념**: 최종 은닉 상태를 어휘 사전 크기의 확률 분포로 변환
- **백엔드 관점**: 분류(classification) 문제로, 소프트맥스를 통한 확률 계산
- **핵심 포인트**:
  - 어휘 사전 크기만큼의 로짓(logit) 생성 (예: 50,000개)
  - 온도(temperature) 파라미터로 확률 분포 조절
  - Top-k, Top-p 샘플링으로 다양성과 품질 균형

```python
# 출력 계산 예시
def generate_next_token(hidden_states, vocab_size, temperature=1.0):
    # 선형 변환으로 어휘 사전 크기만큼 로짓 생성
    logits = linear_projection(hidden_states[-1])  # [vocab_size]
    
    # 온도 적용
    logits = logits / temperature
    
    # 확률 분포 계산
    probabilities = torch.softmax(logits, dim=-1)
    
    # 샘플링
    next_token = torch.multinomial(probabilities, num_samples=1)
    return next_token
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 분산 처리 및 확장성
- **모델 병렬화**: 거대한 모델을 여러 GPU/서버에 분산 배치
- **데이터 병렬화**: 배치 단위로 여러 디바이스에서 동시 처리
- **파이프라인 병렬화**: 각 트랜스포머 층을 다른 디바이스에 배치

### 3-2. 메모리 관리
- **모델 크기**: GPT-3는 175B 파라미터 ≈ 350GB (FP16 기준)
- **추론 메모리**: KV 캐시, 중간 활성화값 저장을 위한 추가 메모리 필요
- **메모리 최적화**: 그래디언트 체크포인팅, 모델 양자화 등

### 3-3. 추론 최적화
- **KV 캐시**: 이전 토큰들의 Key-Value 쌍을 캐시하여 중복 계산 방지
- **동적 배칭**: 여러 요청을 배치로 묶어 GPU 활용률 향상
- **추론 가속**: TensorRT, ONNX 등을 활용한 최적화

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 레이턴시 및 처리량
- **콜드 스타트**: 모델 로딩 시간 (수십 초)
- **토큰당 생성 시간**: 20-100ms (모델 크기에 따라)
- **동시 처리**: 배치 크기와 메모리 용량의 트레이드오프

### 4-2. 비용 최적화
- **GPU 인스턴스**: A100, H100 등 고성능 GPU 필수
- **스케일링 전략**: 오토스케일링과 로드밸런싱 설계
- **캐싱 전략**: 자주 사용되는 응답 캐싱으로 비용 절약

### 4-3. 모니터링 및 로깅
- **메트릭 수집**: GPU 사용률, 메모리 사용량, 토큰/초 처리율
- **품질 관리**: 응답 품질 모니터링, A/B 테스트
- **오류 처리**: 메모리 부족, 타임아웃 등 예외 상황 대응

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. LLM의 어텐션 메커니즘이 기존 RNN과 비교해 어떤 장점이 있나요?
2. 트랜스포머의 시간 복잡도가 O(n²)인 이유와 이를 해결하는 방법은?
3. KV 캐시의 동작 원리와 메모리 사용량 계산 방법을 설명해주세요.

### 5-2. 시스템 설계 질문
1. 수백만 사용자를 위한 LLM 서비스의 아키텍처를 설계해보세요.
2. LLM 추론 서버의 오토스케일링 전략을 어떻게 구성하시겠나요?
3. 여러 모델 버전을 동시에 서빙하는 시스템을 어떻게 설계하시겠나요?

### 5-3. 최적화 질문
1. GPU 메모리가 부족할 때 사용할 수 있는 최적화 기법들은?
2. 배치 처리를 통한 처리량 향상 방법과 주의사항은?
3. 모델 양자화(quantization)의 장단점과 구현 방법은?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **순차 처리**: 토큰을 하나씩 생성하는 자동회귀 모델
- **병렬 학습**: 트레이닝 시에는 병렬 처리 가능
- **확률적 생성**: 다음 토큰을 확률 분포에서 샘플링

### 6-2. 백엔드 개발자의 핵심 이해사항
- LLM은 거대한 확률 모델로, 패턴 매칭과 통계적 추론을 수행한다
- 트랜스포머 아키텍처는 병렬 처리와 장거리 의존성 학습에 최적화되어 있다
- 실제 서비스에서는 메모리, 레이턴시, 비용의 균형이 핵심이다

### 6-3. 실무 적용 포인트
- GPU 리소스 관리와 배치 처리 최적화가 성능의 핵심
- 캐싱 전략과 모델 최적화로 비용 효율성 확보
- 모니터링과 로그 분석을 통한 지속적인 성능 개선
