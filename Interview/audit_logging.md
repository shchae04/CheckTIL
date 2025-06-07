# 감사 로깅(Audit Logging)의 설계 및 구현 방법

## 목차
1. [감사 로깅이란?](#1-감사-로깅이란)
   - [감사 로깅의 개념](#감사-로깅의-개념)
   - [일반 로깅과 감사 로깅의 차이](#일반-로깅과-감사-로깅의-차이)
2. [감사 로깅이 중요한 이유](#2-감사-로깅이-중요한-이유)
   - [보안 및 규정 준수](#보안-및-규정-준수)
   - [문제 해결 및 포렌식](#문제-해결-및-포렌식)
   - [사용자 행동 분석](#사용자-행동-분석)
3. [감사 로깅 설계 원칙](#3-감사-로깅-설계-원칙)
   - [무엇을 로깅할 것인가](#무엇을-로깅할-것인가)
   - [로그 데이터 구조 설계](#로그-데이터-구조-설계)
   - [로그 저장 및 보존 전략](#로그-저장-및-보존-전략)
   - [로그 접근 제어](#로그-접근-제어)
4. [감사 로깅 구현 방법](#4-감사-로깅-구현-방법)
   - [Spring AOP를 이용한 구현](#spring-aop를-이용한-구현)
   - [인터셉터를 이용한 구현](#인터셉터를-이용한-구현)
   - [이벤트 기반 구현](#이벤트-기반-구현)
   - [데이터베이스 감사 로깅](#데이터베이스-감사-로깅)
5. [감사 로깅 활용 사례](#5-감사-로깅-활용-사례)
   - [사용자 인증 및 권한 변경 추적](#사용자-인증-및-권한-변경-추적)
   - [중요 데이터 접근 및 변경 감사](#중요-데이터-접근-및-변경-감사)
   - [규제 준수를 위한 감사 로깅](#규제-준수를-위한-감사-로깅)

## 1. 감사 로깅이란?

### 감사 로깅의 개념

감사 로깅(Audit Logging)은 시스템 내에서 발생하는 중요한 이벤트, 사용자 활동, 데이터 변경 등을 체계적으로 기록하는 프로세스입니다. 이는 단순한 디버깅이나 모니터링을 위한 로깅과는 달리, 보안, 규정 준수, 포렌식 분석 등을 목적으로 하는 특수한 형태의 로깅입니다.

감사 로그는 "누가, 언제, 어디서, 무엇을, 어떻게, 왜"라는 질문에 답할 수 있도록 설계되어야 합니다. 이를 통해 시스템 내에서 발생한 모든 중요 활동을 추적하고 검증할 수 있습니다.

### 일반 로깅과 감사 로깅의 차이

| 특성 | 일반 로깅 | 감사 로깅 |
|------|----------|----------|
| 목적 | 디버깅, 문제 해결, 성능 모니터링 | 보안, 규정 준수, 포렌식 분석 |
| 내용 | 기술적 정보, 오류, 경고 | 사용자 활동, 데이터 변경, 접근 시도 |
| 보존 기간 | 일반적으로 짧음 (일/주 단위) | 일반적으로 길음 (월/년 단위) |
| 접근 제어 | 개발자, 운영자 중심 | 엄격한 접근 제어 (보안 담당자, 감사자) |
| 변경 가능성 | 경우에 따라 수정 가능 | 불변성 보장 (변경 불가) |

## 2. 감사 로깅이 중요한 이유

### 보안 및 규정 준수

많은 산업 분야에서 규제 기관은 데이터 접근 및 변경에 대한 감사 로깅을 의무화하고 있습니다. 예를 들어, 금융 산업의 SOX(Sarbanes-Oxley Act), 의료 산업의 HIPAA(Health Insurance Portability and Accountability Act), 개인정보 보호를 위한 GDPR(General Data Protection Regulation) 등이 있습니다.

감사 로깅은 이러한 규정을 준수하는 데 필수적이며, 규정 준수 여부를 증명하는 데 중요한 증거가 됩니다.

### 문제 해결 및 포렌식

보안 사고가 발생했을 때, 감사 로그는 사고의 원인을 파악하고 영향 범위를 결정하는 데 중요한 역할을 합니다. 또한, 법적 분쟁이나 내부 조사 시 중요한 증거 자료로 활용될 수 있습니다.

### 사용자 행동 분석

감사 로그를 분석하면 사용자의 행동 패턴을 파악할 수 있습니다. 이를 통해 비정상적인 활동을 탐지하고, 잠재적인 보안 위협을 사전에 식별할 수 있습니다.

## 3. 감사 로깅 설계 원칙

### 무엇을 로깅할 것인가

효과적인 감사 로깅을 위해 다음과 같은 이벤트를 기록해야 합니다:

1. **인증 이벤트**: 로그인 성공/실패, 로그아웃, 비밀번호 변경
2. **권한 변경**: 사용자 권한 부여/취소, 역할 변경
3. **데이터 접근**: 중요 데이터 조회, 특히 개인정보나 민감한 정보
4. **데이터 변경**: 생성, 수정, 삭제 작업
5. **시스템 설정 변경**: 구성 변경, 보안 설정 변경
6. **비정상 활동**: 접근 거부, 비정상적인 요청 패턴

### 로그 데이터 구조 설계

효과적인 감사 로그는 다음과 같은 정보를 포함해야 합니다:

1. **시간 정보**: 이벤트 발생 시간 (타임스탬프)
2. **사용자 정보**: 사용자 ID, 이름, 역할, IP 주소
3. **이벤트 정보**: 이벤트 유형, 설명
4. **리소스 정보**: 영향을 받은 리소스/데이터
5. **결과 정보**: 성공/실패, 오류 코드
6. **컨텍스트 정보**: 세션 ID, 요청 ID, 관련 비즈니스 컨텍스트

### 로그 저장 및 보존 전략

감사 로그는 다음과 같은 저장 및 보존 전략을 고려해야 합니다:

1. **분리된 저장소**: 애플리케이션 데이터와 분리된 저장소에 보관
2. **불변성 보장**: 로그 변조 방지를 위한 기술 적용 (해시 체인, 디지털 서명 등)
3. **적절한 보존 기간**: 규제 요구사항에 맞는 보존 기간 설정
4. **백업 및 복구**: 정기적인 백업 및 복구 절차 수립
5. **로그 순환**: 오래된 로그의 아카이빙 및 관리 방안

### 로그 접근 제어

감사 로그 자체도 보호되어야 할 중요한 자산입니다:

1. **최소 권한 원칙**: 필요한 사람에게만 최소한의 접근 권한 부여
2. **접근 로깅**: 감사 로그에 대한 접근도 기록
3. **역할 기반 접근 제어**: 역할에 따른 로그 접근 권한 차등화
4. **암호화**: 민감한 로그 정보의 암호화

## 4. 감사 로깅 구현 방법

### Spring AOP를 이용한 구현

Spring AOP(Aspect-Oriented Programming)를 사용하면 비즈니스 로직과 감사 로깅 로직을 분리할 수 있습니다:

```java
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    
    public AuditLogAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Around("@annotation(auditLogged)")
    public Object logAuditEvent(ProceedingJoinPoint joinPoint, AuditLogged auditLogged) throws Throwable {
        // 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        
        // 메서드 정보 가져오기
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // 감사 로그 생성
        AuditLog auditLog = new AuditLog();
        auditLog.setTimestamp(new Date());
        auditLog.setUsername(username);
        auditLog.setAction(auditLogged.action());
        auditLog.setResourceType(auditLogged.resourceType());
        auditLog.setDescription(String.format("%s.%s 메서드 호출", className, methodName));
        
        try {
            // 메서드 실행
            Object result = joinPoint.proceed();
            
            // 성공 로그
            auditLog.setStatus("SUCCESS");
            auditLogRepository.save(auditLog);
            
            return result;
        } catch (Exception e) {
            // 실패 로그
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(e.getMessage());
            auditLogRepository.save(auditLog);
            
            throw e;
        }
    }
}
```

커스텀 어노테이션 정의:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogged {
    String action();
    String resourceType();
}
```

사용 예:

```java
@Service
public class UserService {

    @AuditLogged(action = "UPDATE", resourceType = "USER")
    public void updateUserRole(Long userId, String newRole) {
        // 사용자 역할 업데이트 로직
    }
}
```

### 인터셉터를 이용한 구현

Spring MVC 인터셉터를 사용하여 HTTP 요청에 대한 감사 로깅을 구현할 수 있습니다:

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class AuditLogInterceptor implements HandlerInterceptor {

    private final AuditLogRepository auditLogRepository;
    
    public AuditLogInterceptor(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 요청 시작 시간을 request 속성에 저장
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        
        // 요청 정보 가져오기
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        // 처리 시간 계산
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        // 감사 로그 생성
        AuditLog auditLog = new AuditLog();
        auditLog.setTimestamp(new Date());
        auditLog.setUsername(username);
        auditLog.setAction(method);
        auditLog.setResourceType("HTTP_REQUEST");
        auditLog.setResourceId(uri);
        auditLog.setStatus(status >= 200 && status < 300 ? "SUCCESS" : "FAILURE");
        auditLog.setDuration(duration);
        auditLog.setIpAddress(request.getRemoteAddr());
        
        if (ex != null) {
            auditLog.setErrorMessage(ex.getMessage());
        }
        
        auditLogRepository.save(auditLog);
    }
}
```

### 이벤트 기반 구현

Spring의 이벤트 시스템을 활용하여 느슨하게 결합된 감사 로깅을 구현할 수 있습니다:

```java
// 감사 이벤트 정의
public class AuditEvent {
    private final String username;
    private final String action;
    private final String resourceType;
    private final String resourceId;
    private final Date timestamp;
    
    // 생성자, getter 등
}

// 이벤트 발행자
@Service
public class UserService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public UserService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void updateUserRole(Long userId, String newRole) {
        // 사용자 역할 업데이트 로직
        
        // 감사 이벤트 발행
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        AuditEvent event = new AuditEvent(
            username,
            "UPDATE_ROLE",
            "USER",
            userId.toString(),
            new Date()
        );
        
        eventPublisher.publishEvent(event);
    }
}

// 이벤트 리스너
@Component
public class AuditEventListener {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        AuditLog log = new AuditLog();
        log.setUsername(event.getUsername());
        log.setAction(event.getAction());
        log.setResourceType(event.getResourceType());
        log.setResourceId(event.getResourceId());
        log.setTimestamp(event.getTimestamp());
        log.setStatus("SUCCESS");
        
        auditLogRepository.save(log);
    }
}
```

### 데이터베이스 감사 로깅

JPA와 Hibernate Envers를 사용하여 엔티티 변경 이력을 자동으로 추적할 수 있습니다:

```java
// build.gradle 또는 pom.xml에 의존성 추가
// implementation 'org.hibernate:hibernate-envers'

import org.hibernate.envers.Audited;
import javax.persistence.*;

@Entity
@Audited
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    private String email;
    
    @Audited(withModifiedFlag = true)
    private String role;
    
    // getter, setter 등
}
```

Spring Data JPA와 함께 사용:

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface UserRepository extends JpaRepository<User, Long>, RevisionRepository<User, Long, Integer> {
    // 기본 CRUD 메서드 + 리비전 조회 메서드 제공
}
```

## 5. 감사 로깅 활용 사례

### 사용자 인증 및 권한 변경 추적

사용자 인증 및 권한 변경은 보안 관점에서 매우 중요한 이벤트입니다. 다음과 같은 이벤트를 추적해야 합니다:

- 로그인 성공/실패
- 비밀번호 변경/재설정
- 계정 잠금/해제
- 사용자 역할 변경
- 권한 부여/취소

```java
@Component
public class AuthenticationAuditListener {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuthenticationAuditListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setUsername(event.getAuthentication().getName());
        log.setAction("LOGIN");
        log.setResourceType("AUTHENTICATION");
        log.setStatus("SUCCESS");
        log.setIpAddress(getCurrentRequest().getRemoteAddr());
        
        auditLogRepository.save(log);
    }
    
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setUsername(event.getAuthentication().getName());
        log.setAction("LOGIN");
        log.setResourceType("AUTHENTICATION");
        log.setStatus("FAILURE");
        log.setErrorMessage("Bad credentials");
        log.setIpAddress(getCurrentRequest().getRemoteAddr());
        
        auditLogRepository.save(log);
    }
    
    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
```

### 중요 데이터 접근 및 변경 감사

금융 정보, 개인정보 등 중요 데이터에 대한 접근 및 변경은 반드시 감사 로깅되어야 합니다:

```java
@Service
public class FinancialRecordService {
    
    private final FinancialRecordRepository repository;
    private final AuditLogService auditLogService;
    
    // 생성자 주입
    
    @AuditLogged(action = "VIEW", resourceType = "FINANCIAL_RECORD")
    public FinancialRecord getFinancialRecord(Long id) {
        return repository.findById(id).orElseThrow();
    }
    
    @AuditLogged(action = "UPDATE", resourceType = "FINANCIAL_RECORD")
    @Transactional
    public FinancialRecord updateFinancialRecord(Long id, FinancialRecordDTO dto) {
        FinancialRecord record = repository.findById(id).orElseThrow();
        
        // 변경 전 데이터 기록
        Map<String, Object> before = new HashMap<>();
        before.put("amount", record.getAmount());
        before.put("description", record.getDescription());
        
        // 데이터 업데이트
        record.setAmount(dto.getAmount());
        record.setDescription(dto.getDescription());
        
        // 변경 후 데이터 기록
        Map<String, Object> after = new HashMap<>();
        after.put("amount", record.getAmount());
        after.put("description", record.getDescription());
        
        // 상세 변경 내역 기록
        auditLogService.logDataChange("FINANCIAL_RECORD", id.toString(), before, after);
        
        return repository.save(record);
    }
}
```

### 규제 준수를 위한 감사 로깅

금융, 의료, 개인정보 보호 등 규제가 엄격한 산업에서는 규제 요구사항에 맞는 감사 로깅이 필요합니다:

```java
@Service
public class ComplianceAuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public ComplianceAuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public List<AuditLog> getComplianceReport(Date startDate, Date endDate, String resourceType) {
        return auditLogRepository.findByTimestampBetweenAndResourceType(startDate, endDate, resourceType);
    }
    
    public void exportComplianceReport(Date startDate, Date endDate, String format, OutputStream outputStream) {
        List<AuditLog> logs = getComplianceReport(startDate, endDate, null);
        
        // 형식에 맞게 보고서 생성 (CSV, PDF 등)
        if ("csv".equalsIgnoreCase(format)) {
            generateCsvReport(logs, outputStream);
        } else if ("pdf".equalsIgnoreCase(format)) {
            generatePdfReport(logs, outputStream);
        }
        
        // 보고서 생성 자체도 감사 로깅
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";
        
        AuditLog reportLog = new AuditLog();
        reportLog.setTimestamp(new Date());
        reportLog.setUsername(username);
        reportLog.setAction("EXPORT_REPORT");
        reportLog.setResourceType("COMPLIANCE_REPORT");
        reportLog.setDescription(String.format("기간: %s ~ %s, 형식: %s", startDate, endDate, format));
        reportLog.setStatus("SUCCESS");
        
        auditLogRepository.save(reportLog);
    }
    
    // 보고서 생성 메서드 구현
}
```

감사 로깅은 시스템의 보안과 규정 준수를 위한 필수적인 요소입니다. 잘 설계된 감사 로깅 시스템은 보안 사고 예방, 탐지, 대응에 큰 도움이 되며, 규제 요구사항을 충족하는 데도 중요한 역할을 합니다. 효과적인 감사 로깅을 위해서는 무엇을 로깅할지, 어떻게 저장하고 보호할지, 어떻게 활용할지에 대한 전략적인 접근이 필요합니다.