# Spring Batch란?: 백엔드 개발자 관점에서의 배치 처리 프레임워크

## 1. 한 줄 정의
- Spring Batch는 대용량 데이터를 효율적으로 처리하기 위한 경량화된 배치 프레임워크로, Job과 Step 구조를 기반으로 읽기-처리-쓰기(Read-Process-Write) 패턴을 통해 트랜잭션 관리, 재시작, 스킵, 재시도 등의 기능을 제공한다. 백엔드 관점에서는 대량의 데이터를 청크(Chunk) 단위로 분할하여 처리하는 배치 아키텍처로 이해할 수 있다.

---

## 2. Spring Batch 핵심 구성 요소

### 2-1. Job
- **개념**: 전체 배치 프로세스를 캡슐화하는 최상위 개념
- **백엔드 관점**: 하나의 비즈니스 로직 단위, 실행 가능한 작업의 컨테이너
- **핵심 포인트**:
  - 하나의 Job은 1개 이상의 Step으로 구성
  - JobInstance: 논리적 실행 단위 (같은 Job이라도 파라미터가 다르면 다른 Instance)
  - JobExecution: 실제 실행 단위 (실패 시 재실행하면 새로운 Execution 생성)

```java
@Configuration
public class SampleJobConfiguration {

    @Bean
    public Job sampleJob(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("sampleJob", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }
}
```

### 2-2. Step
- **개념**: Job을 구성하는 독립적인 작업 단위
- **백엔드 관점**: 트랜잭션 경계를 가진 실행 단위, 독립적으로 성공/실패 판단
- **핵심 포인트**:
  - Tasklet 기반: 단순한 단일 작업 수행
  - Chunk 기반: 대량 데이터를 일정 크기로 나누어 처리
  - Step 간 데이터 공유는 ExecutionContext 활용

```java
@Bean
public Step chunkStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<Input> reader,
                      ItemProcessor<Input, Output> processor,
                      ItemWriter<Output> writer) {
    return new StepBuilder("chunkStep", jobRepository)
            .<Input, Output>chunk(100, transactionManager)  // 청크 크기 100
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
}
```

### 2-3. ItemReader
- **개념**: 데이터를 읽어오는 인터페이스
- **백엔드 관점**: 데이터 소스로부터 데이터를 추출하는 Input Layer
- **핵심 포인트**:
  - JdbcPagingItemReader: DB에서 페이징으로 읽기
  - JpaPagingItemReader: JPA 기반 페이징 읽기
  - FlatFileItemReader: CSV, 텍스트 파일 읽기
  - 커서 기반 vs 페이징 기반 선택이 성능에 영향

```java
@Bean
public JdbcPagingItemReader<User> reader(DataSource dataSource) {
    return new JdbcPagingItemReaderBuilder<User>()
            .name("userReader")
            .dataSource(dataSource)
            .pageSize(100)
            .selectClause("SELECT user_id, name, email")
            .fromClause("FROM users")
            .whereClause("WHERE status = 'ACTIVE'")
            .sortKeys(Map.of("user_id", Order.ASCENDING))
            .rowMapper(new BeanPropertyRowMapper<>(User.class))
            .build();
}
```

### 2-4. ItemProcessor
- **개념**: 읽은 데이터를 가공/변환하는 인터페이스
- **백엔드 관점**: 비즈니스 로직이 실행되는 Processing Layer
- **핵심 포인트**:
  - null 반환 시 해당 아이템 필터링 (Writer로 전달 안 됨)
  - CompositeItemProcessor로 여러 Processor 체이닝
  - 비즈니스 검증, 데이터 변환, 외부 API 호출 등 수행

```java
@Bean
public ItemProcessor<User, UserDto> processor() {
    return user -> {
        // 비즈니스 로직 수행
        if (user.getAge() < 18) {
            return null;  // 18세 미만 필터링
        }

        // 데이터 변환
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName().toUpperCase());
        dto.setEmail(user.getEmail());
        return dto;
    };
}
```

### 2-5. ItemWriter
- **개념**: 처리된 데이터를 저장하는 인터페이스
- **백엔드 관점**: 데이터를 최종 목적지에 기록하는 Output Layer
- **핵심 포인트**:
  - Chunk 단위로 일괄 처리 (배치 insert/update)
  - JdbcBatchItemWriter: JDBC 배치로 DB에 저장
  - JpaItemWriter: JPA로 엔티티 저장
  - 청크 크기만큼 모아서 한 번에 처리하여 성능 최적화

```java
@Bean
public JdbcBatchItemWriter<UserDto> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<UserDto>()
            .dataSource(dataSource)
            .sql("INSERT INTO user_summary (user_id, name, email) VALUES (:userId, :name, :email)")
            .beanMapped()
            .build();
}
```

### 2-6. JobRepository & JobLauncher
- **개념**:
  - JobRepository: 배치 메타데이터를 저장/관리하는 저장소
  - JobLauncher: Job을 실행시키는 인터페이스
- **백엔드 관점**:
  - JobRepository는 배치 실행 이력을 관리하는 감사 시스템
  - JobLauncher는 배치 실행의 진입점
- **핵심 포인트**:
  - BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION 등 메타 테이블 사용
  - 재시작 기능을 위한 실행 컨텍스트 저장
  - 동기/비동기 실행 모드 지원

```java
@Service
public class BatchService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job sampleJob;

    public void runBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", LocalDate.now().toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(sampleJob, jobParameters);
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 청크 기반 처리 (Chunk-Oriented Processing)
- **청크 단위 트랜잭션**: N개씩 묶어서 커밋하여 메모리 효율성 확보
- **처리 흐름**: Read → Process → Write (청크 크기만큼 반복) → Commit
- **성능 최적화**: 청크 크기 조정으로 성능과 메모리 균형 조절

### 3-2. 트랜잭션 관리
- **Step 레벨 트랜잭션**: 각 Step은 독립적인 트랜잭션 경계
- **Chunk 단위 커밋**: 청크 크기만큼 처리 후 자동 커밋
- **롤백 정책**: 청크 내 실패 시 해당 청크만 롤백

### 3-3. 멀티스레드 처리
- **TaskExecutor 활용**: Step 내에서 멀티스레드 처리
- **파티셔닝**: 데이터를 여러 파티션으로 나누어 병렬 처리
- **원격 청킹**: Reader/Writer를 분리하여 분산 처리

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 재시작 및 실패 처리
- **재시작 가능성**: JobInstance는 한 번만 성공, 실패 시 같은 파라미터로 재실행
- **Skip 정책**: 특정 예외 발생 시 해당 아이템만 건너뛰기
- **Retry 정책**: 일시적 오류에 대한 재시도 메커니즘
- **실행 이력 관리**: JobRepository를 통한 모든 실행 이력 추적

```java
@Bean
public Step resilientStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager) {
    return new StepBuilder("resilientStep", jobRepository)
            .<Input, Output>chunk(100, transactionManager)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .faultTolerant()  // 장애 허용 활성화
            .skip(FlatFileParseException.class)
            .skipLimit(10)  // 최대 10개까지 스킵
            .retry(DeadlockLoserDataAccessException.class)
            .retryLimit(3)  // 최대 3번 재시도
            .build();
}
```

### 4-2. 스케줄링 및 실행
- **Spring Scheduler**: @Scheduled 어노테이션으로 주기적 실행
- **Quartz**: 복잡한 스케줄링 요구사항 처리
- **Jenkins/Airflow**: 외부 스케줄러와 연동
- **파라미터 관리**: 실행 시점마다 고유한 JobParameters 필요

```java
@Component
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dailyJob;

    @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
    public void runDailyBatch() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("date", LocalDate.now().toString())
                .addLong("time", System.currentTimeMillis())  // 고유성 보장
                .toJobParameters();

        jobLauncher.run(dailyJob, params);
    }
}
```

### 4-3. 성능 최적화
- **청크 크기 튜닝**: 너무 작으면 오버헤드, 너무 크면 메모리 이슈
- **페이징 vs 커서**: 대용량 데이터는 커서 방식이 유리
- **병렬 처리**: 멀티스레드, 파티셔닝으로 처리 속도 향상
- **인덱스 활용**: Reader의 쿼리 성능 최적화 필수

### 4-4. 모니터링 및 알림
- **실행 상태 추적**: BATCH_JOB_EXECUTION 테이블 모니터링
- **실패 알림**: 배치 실패 시 알림 시스템 연동
- **성능 메트릭**: 처리 건수, 실행 시간, 실패율 등 추적
- **로그 관리**: 각 Step별 상세 로그 기록

---

## 5. 예상 면접 질문

### 5-1. 기본 개념 질문
1. Spring Batch와 일반적인 스케줄러(예: Quartz)의 차이점은 무엇인가요?
2. JobInstance와 JobExecution의 차이를 설명해주세요.
3. Tasklet 방식과 Chunk 방식의 차이와 각각 어떤 상황에 적합한가요?

### 5-2. 아키텍처 및 설계 질문
1. 대용량 데이터(수백만 건)를 처리하는 배치를 설계할 때 고려사항은?
2. 배치 처리 중 실패 시 재시작 전략을 어떻게 구성하시겠나요?
3. Spring Batch에서 멀티스레드 처리를 구현하는 방법들을 설명해주세요.

### 5-3. 트러블슈팅 질문
1. 배치 처리 중 OutOfMemory 에러가 발생한다면 어떻게 해결하시겠나요?
2. 같은 Job을 동시에 여러 번 실행하려고 할 때 발생할 수 있는 문제는?
3. ItemReader에서 데이터를 페이징으로 읽을 때 주의할 점은?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **청크 기반 처리**: 대용량 데이터를 일정 단위로 나누어 처리
- **트랜잭션 관리**: 청크 단위 자동 커밋/롤백
- **재시작 가능**: 실패 지점부터 재실행 가능
- **확장성**: 멀티스레드, 파티셔닝을 통한 성능 향상

### 6-2. 백엔드 개발자의 핵심 이해사항
- Spring Batch는 대용량 데이터를 안정적으로 처리하기 위한 프레임워크
- Read-Process-Write 패턴으로 관심사 분리와 재사용성 확보
- JobRepository를 통한 실행 이력 관리로 추적성과 재시작 기능 제공
- 청크 크기, 트랜잭션 경계, 병렬 처리가 성능의 핵심

### 6-3. 실무 적용 포인트
- **청크 크기 최적화**: 메모리와 성능의 균형점 찾기 (일반적으로 100~1000)
- **장애 허용 설계**: Skip, Retry 정책으로 안정성 확보
- **성능 모니터링**: 실행 시간, 처리 건수, 실패율 지속 추적
- **테스트 전략**: JobLauncherTestUtils를 활용한 통합 테스트

### 6-4. 일반적인 배치 처리 흐름

```
1. JobLauncher가 Job 실행
   ↓
2. Job이 Step 순차 실행
   ↓
3. Step에서 청크 단위로 처리:
   - ItemReader: 청크 크기만큼 읽기
   - ItemProcessor: 각 아이템 처리
   - ItemWriter: 청크 단위로 일괄 쓰기
   - Commit
   ↓
4. 모든 데이터 처리 완료
   ↓
5. JobRepository에 실행 결과 저장
```

### 6-5. 실무에서 자주 사용하는 패턴

```java
// 1. 간단한 배치 Job 구성
@Configuration
public class SimpleBatchConfig {

    @Bean
    public Job userMigrationJob(JobRepository jobRepository, Step migrationStep) {
        return new JobBuilder("userMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())  // 자동으로 고유 파라미터 생성
                .start(migrationStep)
                .build();
    }

    @Bean
    public Step migrationStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrationStep", jobRepository)
                .<OldUser, NewUser>chunk(1000, transactionManager)
                .reader(oldUserReader())
                .processor(userTransformProcessor())
                .writer(newUserWriter())
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skipLimit(100)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("Step 시작: {}", stepExecution.getStartTime());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("처리 건수: {}", stepExecution.getWriteCount());
                        return ExitStatus.COMPLETED;
                    }
                })
                .build();
    }
}
```

---

## 7. 참고 자료

- [Spring Batch 공식 문서](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- Spring Batch 메타데이터 테이블: BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION 등
- 일반적인 청크 크기 권장: 100~1000 (데이터 특성에 따라 조정)