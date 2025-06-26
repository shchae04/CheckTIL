# JPA의 ddl-auto 옵션 이해하기

## 목차
1. [ddl-auto 옵션이란?](#1-ddl-auto-옵션이란)
2. [ddl-auto 옵션의 종류](#2-ddl-auto-옵션의-종류)
3. [각 환경별 권장 설정](#3-각-환경별-권장-설정)
4. [주의사항 및 모범 사례](#4-주의사항-및-모범-사례)
5. [실제 사용 예시](#5-실제-사용-예시)

## 1. ddl-auto 옵션이란?

JPA의 `ddl-auto` 옵션은 애플리케이션 실행 시점에 데이터베이스 스키마를 자동으로 생성, 수정, 검증하는 기능을 제공합니다. 이 옵션은 Hibernate의 SchemaManagementTool을 통해 구현되며, 개발 및 테스트 환경에서 특히 유용합니다.

`ddl-auto` 옵션은 다음과 같은 상황에서 유용합니다:
- 개발 초기 단계에서 빠른 프로토타이핑
- 테스트 환경에서 매번 새로운 스키마로 테스트 실행
- 엔티티 클래스와 데이터베이스 스키마의 동기화 유지
- 개발자가 직접 DDL을 작성하는 번거로움 감소

## 2. ddl-auto 옵션의 종류

JPA에서 제공하는 `ddl-auto` 옵션은 다음과 같습니다:

### 1. `create`
- 애플리케이션 시작 시 기존 테이블을 모두 **삭제**하고 엔티티를 기반으로 새로운 테이블을 생성합니다.
- 기존 데이터는 모두 **손실**됩니다.
- 개발 초기 단계나 테스트 환경에서 주로 사용합니다.

### 2. `create-drop`
- `create`와 동일하게 시작 시 테이블을 생성하지만, 애플리케이션 **종료 시점**에 생성한 테이블을 모두 삭제합니다.
- 단위 테스트 등 테스트 케이스 실행 시 주로 사용합니다.

### 3. `update`
- 기존 테이블은 유지하면서 엔티티와 테이블 간의 차이점만 **변경**합니다.
- 새로운 필드나 컬럼이 추가되면 이를 반영하지만, 기존 컬럼 삭제는 반영하지 않습니다.
- 개발 환경에서 스키마 변경 사항을 반영할 때 유용합니다.

### 4. `validate`
- 엔티티와 테이블이 정상적으로 매핑되었는지 **검증**만 수행합니다.
- 테이블 생성이나 수정은 하지 않으며, 매핑이 올바르지 않으면 애플리케이션이 실행되지 않습니다.
- 프로덕션 환경에서 안전하게 사용할 수 있는 옵션입니다.

### 5. `none`
- 스키마 자동 생성 기능을 사용하지 않습니다.
- 명시적으로 설정하지 않아도 기본값은 `none`입니다.
- 프로덕션 환경에서 주로 사용합니다.

## 3. 각 환경별 권장 설정

### 개발 환경 (Development)
- 권장 옵션: `update` 또는 `create`
- 개발 초기에는 `create`를 사용하여 빠르게 스키마를 생성하고, 안정화 단계에서는 `update`로 전환하여 데이터 유지
- 개발자의 로컬 환경에서는 필요에 따라 `create-drop`도 사용 가능

### 테스트 환경 (Testing)
- 권장 옵션: `create-drop` 또는 `create`
- 매 테스트마다 깨끗한 환경에서 시작하기 위해 `create-drop` 사용
- 통합 테스트 환경에서는 `create`를 사용하여 테스트 간 데이터 공유 가능

### 스테이징 환경 (Staging)
- 권장 옵션: `validate` 또는 `none`
- 프로덕션과 유사한 환경을 유지하기 위해 자동 스키마 생성은 지양
- 스키마 변경은 마이그레이션 도구(Flyway, Liquibase 등)를 통해 관리

### 프로덕션 환경 (Production)
- 권장 옵션: `none` 또는 `validate`
- 프로덕션에서는 절대 자동 스키마 생성 사용 금지
- `validate`를 사용하여 배포 전 엔티티와 스키마의 일치 여부 확인 가능
- 스키마 변경은 반드시 마이그레이션 도구를 통해 계획적으로 수행

## 4. 주의사항 및 모범 사례

### 주의사항

1. **데이터 손실 위험**
   - `create`와 `create-drop` 옵션은 기존 테이블을 삭제하므로 데이터 손실이 발생합니다.
   - 프로덕션 환경에서는 절대 사용하지 마세요.

2. **성능 영향**
   - `update` 옵션은 애플리케이션 시작 시 스키마 분석 작업을 수행하므로 시작 시간이 길어질 수 있습니다.
   - 테이블이 많은 대규모 애플리케이션에서는 성능 저하가 발생할 수 있습니다.

3. **제한된 변경 지원**
   - `update` 옵션은 컬럼 추가는 지원하지만, 컬럼 이름 변경이나 타입 변경은 제대로 처리하지 못할 수 있습니다.
   - 복잡한 변경은 마이그레이션 도구를 사용하는 것이 안전합니다.

4. **외래 키 제약 조건**
   - 테이블 생성 순서에 따라 외래 키 제약 조건 생성에 실패할 수 있습니다.
   - 특히 순환 참조가 있는 경우 문제가 발생할 수 있습니다.

### 모범 사례

1. **환경별 설정 분리**
   - 개발, 테스트, 프로덕션 환경별로 다른 설정 사용
   - Spring Boot의 프로필 기능을 활용하여 환경별 설정 관리

2. **마이그레이션 도구 활용**
   - 프로덕션 환경에서는 Flyway나 Liquibase 같은 마이그레이션 도구 사용
   - 스키마 변경 이력 관리 및 롤백 지원

3. **스키마 생성 스크립트 활용**
   - `hibernate.hbm2ddl.auto` 대신 `hibernate.hbm2ddl.scripts` 옵션을 사용하여 스키마 생성 스크립트만 추출
   - 생성된 스크립트를 검토 후 수동으로 적용

4. **개발 초기에만 자동 생성 사용**
   - 프로젝트 초기 단계에서만 자동 스키마 생성 사용
   - 안정화 단계에서는 마이그레이션 도구로 전환

## 5. 실제 사용 예시

### Spring Boot에서의 설정

#### application.properties 파일 설정
```properties
# 개발 환경
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# 또는 환경별 설정
# spring.jpa.hibernate.ddl-auto=create-drop  # 테스트 환경
# spring.jpa.hibernate.ddl-auto=validate     # 스테이징 환경
# spring.jpa.hibernate.ddl-auto=none         # 프로덕션 환경
```

#### application.yml 파일 설정
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

#### 프로필별 설정 예시
```yaml
spring:
  profiles:
    active: dev

---
spring:
  config:
    activate:
      on-profile: dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

---
spring:
  config:
    activate:
      on-profile: test
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

---
spring:
  config:
    activate:
      on-profile: prod
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
```

### 순수 Hibernate에서의 설정

#### hibernate.cfg.xml 파일 설정
```xml
<hibernate-configuration>
  <session-factory>
    <!-- 데이터베이스 연결 설정 -->
    <property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
    <property name="connection.url">jdbc:mysql://localhost:3306/mydb</property>
    <property name="connection.username">root</property>
    <property name="connection.password">password</property>
    
    <!-- 스키마 자동 생성 설정 -->
    <property name="hibernate.hbm2ddl.auto">update</property>
    
    <!-- 기타 설정 -->
    <property name="show_sql">true</property>
    <property name="format_sql">true</property>
    
    <!-- 엔티티 클래스 매핑 -->
    <mapping class="com.example.entity.User"/>
    <mapping class="com.example.entity.Product"/>
  </session-factory>
</hibernate-configuration>
```

#### Java 코드에서 설정
```java
Properties properties = new Properties();
properties.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
properties.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/mydb");
properties.setProperty("hibernate.connection.username", "root");
properties.setProperty("hibernate.connection.password", "password");
properties.setProperty("hibernate.hbm2ddl.auto", "update");
properties.setProperty("hibernate.show_sql", "true");

StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
    .applySettings(properties)
    .build();

SessionFactory sessionFactory = new MetadataSources(registry)
    .addAnnotatedClass(User.class)
    .addAnnotatedClass(Product.class)
    .buildMetadata()
    .buildSessionFactory();
```

### 마이그레이션 도구와 함께 사용하기

#### Flyway와 함께 사용하는 예시 (Spring Boot)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 검증만 수행
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

#### Liquibase와 함께 사용하는 예시 (Spring Boot)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 검증만 수행
  
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

JPA의 `ddl-auto` 옵션은 개발 생산성을 크게 향상시키는 유용한 기능이지만, 환경에 맞게 적절히 사용하는 것이 중요합니다. 특히 프로덕션 환경에서는 데이터 안전성을 위해 자동 스키마 생성 기능을 비활성화하고, 계획된 마이그레이션 전략을 사용하는 것이 바람직합니다.