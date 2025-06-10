# Today I Learned (TIL)

이 저장소는 제가 일상에서 학습한 다양한 기술, 개념, 문제 해결 방법 등을 정리한 내용을 담고 있습니다.

## 목차

### 컴퓨터 과학 (Computer Science)

#### 알고리즘 (Algorithms)
- [정렬 알고리즘 (Sorting Algorithms)](CS/Algorithms/sorting_algorithms.md)
- [이진 탐색 (Binary Search)](CS/Algorithms/binary_search.md)
- [트리 데이터 구조 (Tree Data Structures)](CS/Algorithms/tree_data_structures.md)
- [푸리에 변환 (Fourier Transform)](CS/Algorithms/fourier_transform.md)

#### 동시성 (Concurrency)
- [생산자-소비자 문제 (Producer-Consumer Problem)](CS/Concurrency/producer_consumer_problem.md)
- [Java ForkJoin 프레임워크 사용법](CS/Concurrency/java_forkjoin_framework.md)
- [Java Executors와 ThreadPool 사용법](CS/Concurrency/java_executors_threadpool.md)

#### 운영체제 (Operating Systems)
- [페이징 기법 (Paging Techniques)](CS/OperatingSystems/paging_techniques.md)
- [스택과 힙 (Stack and Heap)](CS/OperatingSystems/stack_and_heap.md)

### 웹 개발 (Web Development)

#### JavaScript
- [jQuery AJAX data 함수에서 발생하는 CallStack 오류 해결하기](Web/JavaScript/jquery_ajax_data_function_callstack_error.md)

#### API 설계 및 개발
- [Swagger API 사용법](Web/swagger_api_usage.md)

### 데이터베이스
- [ACID (원자성, 일관성, 고립성, 지속성)](Interview/ACID.md)
- [데이터베이스 인덱스의 동작원리](Interview/database_index_principles.md)
- [조회 트래픽을 고려한 DB 인덱스 설계](Interview/db_index_design_for_query_traffic.md)
- [SQL 쿼리 튜닝의 원리와 기법](Interview/sql_query_tuning.md)
- [CTE 쿼리의 장점 및 단점](Interview/cte_query.md)
- [N+1 문제가 발생하는 이유](Interview/n_plus_1_problem.md)
- [데이터베이스 정규화](Interview/database_normalization.md)
- [데이터베이스 커넥션 풀](Interview/database_connection_pool.md)
- [캐싱 전략](Interview/caching_strategies.md)
- [로컬 캐시와 리모트 캐시](Interview/local_remote_cache.md)
- [ORM, JPA, Hibernate의 장단점](Interview/orm_jpa_hibernate.md)
- [MyBatis에서 JPA로 전환할 때 고려해야 할 점](Interview/mybatis_to_jpa_migration.md)

### 아키텍처 & 설계
- [SOLID 원칙](Interview/SOLID.md)
- [DDD (Domain-Driven Design)](Interview/DDD.md)
- [MSA (Microservice Architecture)](Interview/msa.md)
- [계층형 아키텍처 (Layered Architecture)](Interview/layered_architecture.md)
- [DTO와 VO의 차이](Interview/DTO_VO.md)
- [멱등성 (Idempotent)](Interview/Idempotent.md)
- [RESTful API 구현 방법](Interview/restful_api.md)
- [동기 vs 비동기](Interview/sync_vs_async.md)
- [동기 방식 외부 서비스 호출 시 장애 대응 전략](Interview/handling_external_service_failures.md)
- [시스템 비동기 통합 방식](Interview/system_async_integration_method.md)
- [디자인 패턴](Interview/design_patterns.md)

### 개발 방법론
- [TDD (Test-Driven Development)](Interview/TDD.md)
- [클린 코드 (Clean Code)](Interview/clean_code.md)

### 네트워크 & 보안
- [HTTP 프로토콜](Interview/HTTP.md)
- [인터넷의 작동원리](Interview/internet_working_principles.md)
- [Classful vs Classless Networking](Interview/classful_classless.md)
- [Same-Site와 Same-Origin 정책](Interview/same_site_same_origin.md)
- [환경 변수 보안](Interview/secured_Environment.md)
- [URL 인코딩](Interview/url_encoding_%.md)
- [대칭키와 비대칭키](Interview/symmetric_asymmetric_keys.md)
- [CORS (Cross-Origin Resource Sharing)](Interview/CORS.md)
- [CSRF (Cross-Site Request Forgery)](Interview/CSRF.md)
- [도메인 네임이란?](Interview/domain_name.md)
- [DNS의 작동원리](Interview/dns_working_principles.md)
- [DNS 개발자를 위한 간단 가이드](Interview/dns_for_developers.md)
- [NLB와 ALB의 차이점](Interview/nlb_vs_alb.md)
- [웹사이트 접근 과정](Interview/website_access_process.md)
- [CDN (Content Delivery Network)](Interview/cdn.md)
- [감사 로깅(Audit Logging)의 설계 및 구현 방법](Interview/audit_logging.md)
- [단축 URL 서비스 설계](Interview/short_url.md)

### Java & JVM
- [Java 8의 주요 특징과 기능](Interview/java8_features.md)
- [Java 11의 주요 특징과 기능](Interview/java11_features.md)
- [Java 17의 주요 특징과 기능](Interview/java17_features.md)
- [Java 21의 주요 특징과 기능](Interview/java21_features.md)
- [Java Stream API 사용법과 예시](Interview/java_stream_api.md)
- [Java의 즉시 평가와 지연 평가](Interview/java_immediate_vs_lazy_evaluation.md)
- [Java 가상 스레드 (Virtual Threads)](Interview/java_virtual_threads.md)
- [Java Default 메서드의 이해와 활용](Interview/java_default_methods.md)
- [Java Optional 활용법](Interview/java_optional.md)
- [Java 네트워크 I/O 병목 현상 해결 방법](Interview/java_network_io_bottleneck_solutions.md)
- [동등성과 동일성](Interview/equality_identity.md)
- [I/O와 NIO](Interview/io_nio.md)
- [JVM 가비지 컬렉션](Interview/jvm_gc.md)
- [Null을 반환하는 것의 문제점](Interview/why_null_return.md)
- [동시성 처리 방법](Interview/concurrency_handle.md)
- [Optional vs Null](Interview/optional_vs_null.md)

### Spring Framework
- [Spring Bean의 생명주기](Interview/spring_bean_lifecycle.md)
- [Spring에서 프록시를 사용하는 이유](Interview/spring_proxy.md)
- [Spring Security의 구조](Interview/spring_security.md)
- [Spring Framework의 요청 흐름](Interview/spring_request_flow.md)
- [Spring Boot 로그 레벨 관리](Interview/spring_boot_log_level_management.md)
- [Spring Boot JPA와 QueryDSL 가이드](Interview/spring_boot_jpa_querydsl_guide.md)
- [Spring Data JPA vs QueryDSL 비교](Interview/spring-data-jpa-vs-querydsl-summary.md)
- [Spring Transaction과 AOP 흐름](Interview/Spring_Transaction_AOP_flow.md)
- [로깅에서의 MDC (Mapped Diagnostic Context) 활용](Interview/logging_with_mdc.md)
- [Filter와 Interceptor의 차이](Interview/filter_vs_interceptor.md)
- [try-with-resources를 사용해야 하는 이유](Interview/try_with_resources.md)

### 클라우드 & DevOps
- [클라우드 네이티브](Interview/cloud_native.md)
- [AWS 웹사이트 배포 및 도메인 연결 과정](Interview/aws_website_deployment.md)
- [Spring Boot, Thymeleaf 프로젝트에 CI/CD 적용하는 법](Interview/spring_boot_thymeleaf_cicd.md)

### 자동화
- [n8n 시작하기](Automation/n8n/start.md)

## 이미지 자료
모든 이미지 자료는 [Interview/assets/images](Interview/assets/images) 디렉토리에 저장되어 있습니다.