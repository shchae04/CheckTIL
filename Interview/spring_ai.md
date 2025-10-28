# Spring AI란?

## 1. 한 줄 정의
Spring AI는 AI 애플리케이션 개발을 위한 Spring 생태계 기반의 프레임워크로, 다양한 AI 모델과의 통합을 추상화하여 생산성과 이식성을 제공한다.

---

## 2. Spring AI의 핵심 개념

### 2-1. 목적(Purpose)
- **AI 통합 추상화**: 다양한 AI 서비스(OpenAI, Azure OpenAI, Hugging Face 등)를 일관된 API로 사용
- **Spring 생태계 통합**: Spring Boot, Spring Cloud 등 기존 Spring 기술과 자연스럽게 통합
- **생산성 향상**: Boilerplate 코드 최소화 및 빠른 AI 기능 구현

```java
// Spring AI를 사용한 간단한 예시
@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        return chatClient.call(message);
    }
}
```

### 2-2. 주요 기능(Key Features)
- **채팅 모델(Chat Models)**: 대화형 AI 모델 통합
- **임베딩(Embeddings)**: 텍스트를 벡터로 변환
- **벡터 저장소(Vector Stores)**: 임베딩 데이터 저장 및 검색
- **프롬프트 템플릿(Prompt Templates)**: 재사용 가능한 프롬프트 관리
- **출력 파싱(Output Parsers)**: AI 응답을 구조화된 데이터로 변환

```java
// 프롬프트 템플릿 예시
@Service
public class PromptService {
    private final ChatClient chatClient;

    public String generateResponse(String topic) {
        PromptTemplate template = new PromptTemplate(
            "다음 주제에 대해 3문장으로 설명해주세요: {topic}"
        );

        Prompt prompt = template.create(Map.of("topic", topic));
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
```

### 2-3. 지원 AI 모델(Supported AI Models)
- **OpenAI**: GPT-3.5, GPT-4, DALL-E
- **Azure OpenAI**: Microsoft Azure 기반 OpenAI 서비스
- **Hugging Face**: 오픈소스 모델들
- **Anthropic**: Claude 모델
- **Google Vertex AI**: Google Cloud AI 서비스
- **Ollama**: 로컬 LLM 실행

```yaml
# application.yml 설정 예시
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
        temperature: 0.7
```

### 2-4. 벡터 데이터베이스 통합
- **지원 벡터 스토어**: Pinecone, Chroma, Weaviate, Milvus, Redis
- **RAG(Retrieval-Augmented Generation)**: 외부 지식 기반 검색 및 생성
- **시맨틱 검색**: 의미 기반 데이터 검색

```java
// 벡터 스토어를 활용한 RAG 예시
@Service
public class DocumentSearchService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public String searchAndAnswer(String question) {
        // 1. 질문을 임베딩으로 변환하여 유사 문서 검색
        List<Document> similarDocs = vectorStore.similaritySearch(question);

        // 2. 검색된 문서를 컨텍스트로 포함하여 답변 생성
        String context = similarDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));

        String prompt = String.format(
            "다음 문서를 참고하여 질문에 답하세요:\n%s\n\n질문: %s",
            context, question
        );

        return chatClient.call(prompt);
    }
}
```

### 2-5. 함수 호출(Function Calling)
- **도구 통합**: AI가 외부 함수/API를 호출할 수 있도록 지원
- **구조화된 응답**: AI 응답을 메서드 호출로 변환

```java
// 함수 호출 예시
@Configuration
public class FunctionConfig {

    @Bean
    @Description("현재 날씨 정보를 가져옵니다")
    public Function<WeatherRequest, WeatherResponse> getWeather() {
        return request -> {
            // 실제 날씨 API 호출 로직
            return new WeatherResponse(request.location(), "맑음", 25);
        };
    }
}

@Service
public class WeatherChatService {
    private final ChatClient chatClient;

    public String chat(String message) {
        return chatClient.call(message);
        // AI가 필요하면 자동으로 getWeather 함수 호출
    }
}
```

---

## 3. Spring AI의 아키텍처

### 3-1. 계층 구조
```
┌─────────────────────────────────────┐
│   Application Layer (Your Code)    │
├─────────────────────────────────────┤
│     Spring AI Abstraction Layer    │
│  (ChatClient, EmbeddingClient 등)   │
├─────────────────────────────────────┤
│      AI Provider Adapters           │
│  (OpenAI, Azure, Hugging Face 등)   │
├─────────────────────────────────────┤
│         AI Model Services           │
│   (GPT-4, Claude, Gemini 등)        │
└─────────────────────────────────────┘
```

### 3-2. 주요 컴포넌트

**ChatClient 인터페이스**
```java
public interface ChatClient {
    ChatResponse call(Prompt prompt);
    String call(String message);
}
```

**EmbeddingClient 인터페이스**
```java
public interface EmbeddingClient {
    List<Double> embed(String text);
    List<List<Double>> embed(List<String> texts);
}
```

**VectorStore 인터페이스**
```java
public interface VectorStore {
    void add(List<Document> documents);
    List<Document> similaritySearch(String query);
}
```

---

## 4. 사용 사례

### 4-1. 챗봇 구현
- **고객 지원 봇**: 실시간 고객 문의 응답
- **도메인 특화 어시스턴트**: 특정 분야 전문 지식 제공
- **대화형 인터페이스**: 자연어 기반 시스템 조작

```java
@RestController
@RequestMapping("/api/chat")
public class ChatbotController {
    private final ChatClient chatClient;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        PromptTemplate template = new PromptTemplate(
            "당신은 친절한 고객 지원 담당자입니다. 다음 질문에 답변하세요: {question}"
        );

        Prompt prompt = template.create(Map.of("question", request.message()));
        String response = chatClient.call(prompt).getResult().getOutput().getContent();

        return ResponseEntity.ok(response);
    }
}
```

### 4-2. 문서 검색 및 질의응답(RAG)
- **지식 베이스 구축**: 회사 문서, 매뉴얼 등을 벡터화하여 저장
- **의미 기반 검색**: 키워드가 아닌 의미로 문서 검색
- **컨텍스트 기반 응답**: 검색된 문서를 바탕으로 정확한 답변 생성

```java
@Service
public class KnowledgeBaseService {
    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;
    private final ChatClient chatClient;

    // 문서 추가
    public void addDocument(String content, Map<String, Object> metadata) {
        Document doc = new Document(content, metadata);
        vectorStore.add(List.of(doc));
    }

    // RAG 기반 질의응답
    public String askQuestion(String question) {
        // 유사 문서 검색
        List<Document> docs = vectorStore.similaritySearch(question);

        // 컨텍스트 구성
        String context = docs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        // 프롬프트 생성 및 응답
        String prompt = String.format("""
            다음 문서를 참고하여 질문에 답변하세요.

            문서:
            %s

            질문: %s
            """, context, question);

        return chatClient.call(prompt);
    }
}
```

### 4-3. 콘텐츠 생성
- **블로그 포스트 생성**: 주제에 맞는 글 작성
- **코드 리뷰**: 코드 분석 및 개선 제안
- **요약 생성**: 긴 문서의 핵심 내용 요약

```java
@Service
public class ContentGenerationService {
    private final ChatClient chatClient;

    public String generateBlogPost(String topic, String tone) {
        PromptTemplate template = new PromptTemplate("""
            주제: {topic}
            톤: {tone}

            위 주제로 블로그 포스트를 작성해주세요.
            서론, 본론, 결론 구조로 500자 이내로 작성하세요.
            """);

        Prompt prompt = template.create(Map.of(
            "topic", topic,
            "tone", tone
        ));

        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
```

### 4-4. 데이터 추출 및 구조화
- **비정형 데이터 파싱**: 텍스트에서 구조화된 데이터 추출
- **엔티티 인식**: 인명, 지명, 날짜 등 추출
- **분류 및 태깅**: 자동 카테고리 분류

```java
public record ProductInfo(
    String name,
    String category,
    double price,
    List<String> features
) {}

@Service
public class DataExtractionService {
    private final ChatClient chatClient;

    public ProductInfo extractProductInfo(String description) {
        BeanOutputParser<ProductInfo> parser = new BeanOutputParser<>(ProductInfo.class);

        PromptTemplate template = new PromptTemplate("""
            다음 제품 설명에서 정보를 추출하세요:
            {description}

            {format}
            """);

        Prompt prompt = template.create(Map.of(
            "description", description,
            "format", parser.getFormat()
        ));

        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        return parser.parse(response);
    }
}
```

---

## 5. 백엔드 개발자 관점의 중요성

### 5-1. 비즈니스 로직 통합
- **AI 기능을 기존 서비스에 자연스럽게 통합**
- **Spring 의존성 주입 및 트랜잭션 관리 활용**
- **마이크로서비스 아키텍처에서 AI 서비스 구성**

### 5-2. 성능 및 비용 관리
- **캐싱 전략**: 동일 요청에 대한 AI 호출 최소화
- **배치 처리**: 여러 요청을 묶어서 처리
- **토큰 사용량 모니터링**: API 비용 관리

```java
@Service
public class OptimizedChatService {
    private final ChatClient chatClient;
    private final CacheManager cacheManager;

    @Cacheable(value = "chatResponses", key = "#message")
    public String chat(String message) {
        // 캐시된 응답이 있으면 AI 호출 생략
        return chatClient.call(message);
    }

    // 배치 처리
    public List<String> batchProcess(List<String> messages) {
        return messages.stream()
            .map(chatClient::call)
            .toList();
    }
}
```

### 5-3. 보안 및 컴플라이언스
- **API 키 관리**: 환경 변수 및 시크릿 관리
- **데이터 프라이버시**: 민감 정보 필터링
- **사용자 입력 검증**: 프롬프트 인젝션 방지

```java
@Configuration
public class SecurityConfig {

    @Bean
    public OpenAiChatClient chatClient(
        @Value("${spring.ai.openai.api-key}") String apiKey) {
        // API 키는 환경 변수나 시크릿 매니저에서 관리
        return new OpenAiChatClient(new OpenAiApi(apiKey));
    }
}

@Service
public class SecureChatService {

    public String sanitizeInput(String userInput) {
        // 민감 정보 필터링 (이메일, 전화번호 등)
        return userInput.replaceAll(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b",
            "[EMAIL_REDACTED]"
        );
    }
}
```

### 5-4. 모니터링 및 로깅
- **AI 요청/응답 로깅**: 디버깅 및 분석
- **성능 메트릭**: 응답 시간, 토큰 사용량 추적
- **에러 핸들링**: AI 서비스 장애 대응

```java
@Aspect
@Component
public class ChatClientLoggingAspect {

    @Around("execution(* org.springframework.ai.chat.ChatClient.call(..))")
    public Object logChatCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("AI call completed in {}ms", duration);
            return result;
        } catch (Exception e) {
            log.error("AI call failed", e);
            throw e;
        }
    }
}
```

---

## 6. Spring AI vs 다른 AI 프레임워크

### 6-1. LangChain (Python)
- **LangChain**: Python 기반, 풍부한 생태계, 다양한 통합
- **Spring AI**: Java/Spring 생태계, 엔터프라이즈 환경 최적화

### 6-2. Semantic Kernel (Microsoft)
- **Semantic Kernel**: .NET 기반, Azure 통합 우수
- **Spring AI**: Spring 기반, 멀티 클라우드 지원

### 6-3. 선택 기준
- **기존 스택이 Spring이면** → Spring AI
- **Python 생태계 활용이 중요하면** → LangChain
- **.NET/Azure 환경이면** → Semantic Kernel

---

## 7. 핵심 요약

| 특성 | 설명 |
|------|------|
| **목적** | AI 통합 추상화 및 Spring 생태계 통합 |
| **주요 기능** | 채팅 모델, 임베딩, 벡터 스토어, 프롬프트 템플릿 |
| **지원 모델** | OpenAI, Azure, Anthropic, Hugging Face, Google 등 |
| **핵심 장점** | Spring 개발자 친화적, 이식성, 생산성 |
| **사용 사례** | 챗봇, RAG, 콘텐츠 생성, 데이터 추출 |
| **대상** | Spring 기반 백엔드 개발자 |

### 7-1. 주요 장점
- **추상화 계층**: AI 제공자 변경이 쉬움 (OpenAI → Claude 등)
- **Spring 통합**: 기존 Spring 기술(Security, Data, Cloud)과 자연스럽게 연동
- **생산성**: Boilerplate 코드 최소화, 빠른 프로토타이핑
- **엔터프라이즈 지원**: 트랜잭션, 보안, 모니터링 등 엔터프라이즈 기능

### 7-2. 시작하기

**의존성 추가 (Gradle)**
```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
    implementation 'org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter'
}
```

**기본 설정**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
```

**간단한 사용 예제**
```java
@RestController
public class SimpleAIController {
    private final ChatClient chatClient;

    public SimpleAIController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.call(message);
    }
}
```

### 7-3. 실무 팁
- **프롬프트 템플릿 활용**: 재사용 가능한 프롬프트를 템플릿으로 관리
- **벡터 스토어 선택**: 규모와 성능 요구사항에 맞는 벡터 DB 선택
- **비용 모니터링**: AI API 호출 비용을 추적하고 캐싱으로 최적화
- **점진적 도입**: 작은 기능부터 시작하여 점차 확대
- **테스트 전략**: Mock을 활용한 단위 테스트, AI 응답의 불확실성 고려

---

## 참고 자료
- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI 예제 프로젝트](https://github.com/spring-projects/spring-ai-examples)
