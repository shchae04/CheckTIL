# 다대다 관계 해결법 (Many-to-Many Relationship Solutions)

## 1. 한 줄 요약
- 데이터베이스에서 다대다(M:N) 관계를 개념적 이해부터 논리적 설계, 물리적 구현까지 단계별로 해결하는 방법을 다룬다.

---

## 2. 다대다 관계란?
- **정의**: 두 엔티티 간에 양방향으로 여러 개의 관계가 존재하는 상황
- **예시**: 학생-강의, 고객-상품, 저자-도서, 사용자-역할(Role) 등
- **특징**: 직접적으로 관계형 데이터베이스에서 표현할 수 없음 (외래키로는 1:N 관계만 표현 가능)

---

## 3. 개념적 수준 (Conceptual Level)

### 3.1 다대다 관계의 개념적 이해
- **문제 상황**: 학생 한 명이 여러 강의를 수강할 수 있고, 하나의 강의에 여러 학생이 수강할 수 있음
- **관계의 특성**:
  - 학생(Student) ←→ 강의(Course): M:N 관계
  - 양방향 의존성: 학생 정보 변경이 강의에 영향을 주지 않고, 그 역도 성립
  - 관계 자체가 추가 속성을 가질 수 있음 (수강 날짜, 성적 등)

### 3.2 개념적 모델링
```
[학생] ←------ 수강 ------→ [강의]
  |                           |
  - 학번                      - 강의코드
  - 이름                      - 강의명
  - 전공                      - 학점
  - 학년                      - 교수명
```

### 3.3 다른 예시들
- **고객-상품**: 고객은 여러 상품을 주문 가능, 상품은 여러 고객에게 판매 가능
- **저자-도서**: 한 저자가 여러 책을 쓸 수 있고, 한 책에 여러 저자가 참여 가능
- **사용자-권한**: 사용자는 여러 권한을 가질 수 있고, 권한은 여러 사용자에게 부여 가능

---

## 4. 논리적 수준 (Logical Level)

### 4.1 정규화를 통한 해결
- **문제**: 다대다 관계를 직접 표현하면 데이터 중복과 이상 현상 발생
- **해결책**: 연결 엔티티(Junction Entity) 또는 교차 테이블(Bridge Table) 도입

### 4.2 연결 엔티티 설계 원칙
1. **분해**: M:N → 1:N + N:1
2. **연결 엔티티**: 두 엔티티를 연결하는 중간 엔티티 생성
3. **식별자**: 보통 두 엔티티의 기본키 조합으로 복합키 구성

### 4.3 논리적 ERD 설계
```
[학생] ----< [수강] >---- [강의]
  |         |   |          |
 PK: 학번   FK1 FK2      PK: 강의코드
          학번 강의코드
          수강일자
          성적
```

### 4.4 관계 속성 처리
- **순수 연결**: 연결 엔티티가 외래키만 보유 (Student-Course)
- **속성 보유**: 연결 엔티티가 추가 속성 보유 (수강일자, 성적)
- **독립 엔티티화**: 연결 엔티티가 독립적인 의미를 가질 때 (주문-OrderItem)

### 4.5 정규화 체크포인트
- **1NF**: 모든 속성이 원자값 (Atomic Value)
- **2NF**: 부분 함수 종속성 제거
- **3NF**: 이행 함수 종속성 제거
- 연결 엔티티는 일반적으로 자동으로 3NF 만족

---

## 5. 물리적 수준 (Physical Level)

### 5.1 테이블 구조 설계

#### 학생-강의 예시
```sql
-- 학생 테이블
CREATE TABLE student (
    student_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    major VARCHAR(30),
    grade INTEGER CHECK (grade BETWEEN 1 AND 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 강의 테이블  
CREATE TABLE course (
    course_code VARCHAR(10) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credits INTEGER CHECK (credits > 0),
    professor VARCHAR(50),
    semester VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 수강 테이블 (Junction Table)
CREATE TABLE enrollment (
    student_id VARCHAR(10),
    course_code VARCHAR(10),
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade CHAR(1) CHECK (grade IN ('A', 'B', 'C', 'D', 'F')),
    status VARCHAR(20) DEFAULT 'ENROLLED',
    PRIMARY KEY (student_id, course_code),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES course(course_code) ON DELETE CASCADE
);
```

### 5.2 인덱스 전략
```sql
-- Junction Table 인덱스
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_course ON enrollment(course_code);
CREATE INDEX idx_enrollment_date ON enrollment(enrollment_date);

-- 복합 인덱스 (특정 쿼리 최적화용)
CREATE INDEX idx_enrollment_student_date ON enrollment(student_id, enrollment_date DESC);
```

### 5.3 제약조건 설계
```sql
-- 체크 제약조건
ALTER TABLE enrollment ADD CONSTRAINT chk_enrollment_status 
    CHECK (status IN ('ENROLLED', 'COMPLETED', 'DROPPED', 'FAILED'));

-- 유니크 제약조건 (동일 학기 중복 수강 방지)
ALTER TABLE enrollment ADD CONSTRAINT uk_student_course_semester 
    UNIQUE (student_id, course_code, semester);
```

### 5.4 성능 최적화 고려사항

#### 5.4.1 대리키(Surrogate Key) 도입
```sql
-- 대리키를 사용한 Junction Table
CREATE TABLE enrollment (
    enrollment_id BIGSERIAL PRIMARY KEY,  -- 대리키
    student_id VARCHAR(10) NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade CHAR(1),
    UNIQUE(student_id, course_code),  -- 비즈니스 키는 유니크 제약
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (course_code) REFERENCES course(course_code)
);
```

#### 5.4.2 파티셔닝
```sql
-- 날짜 기반 파티셔닝 (PostgreSQL 예시)
CREATE TABLE enrollment (
    enrollment_id BIGSERIAL,
    student_id VARCHAR(10),
    course_code VARCHAR(10),
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade CHAR(1)
) PARTITION BY RANGE (enrollment_date);

CREATE TABLE enrollment_2024 PARTITION OF enrollment
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

---

## 6. 실무 구현 패턴

### 6.1 기본 CRUD 쿼리

#### 6.1.1 등록 (Create)
```sql
-- 수강 신청
INSERT INTO enrollment (student_id, course_code, enrollment_date)
VALUES ('2024001', 'CS101', CURRENT_DATE);
```

#### 6.1.2 조회 (Read)
```sql
-- 특정 학생의 수강 과목 조회
SELECT s.name, c.course_name, c.credits, e.enrollment_date
FROM student s
JOIN enrollment e ON s.student_id = e.student_id
JOIN course c ON e.course_code = c.course_code
WHERE s.student_id = '2024001';

-- 특정 강의의 수강 학생 조회
SELECT c.course_name, s.name, s.major, e.enrollment_date
FROM course c
JOIN enrollment e ON c.course_code = e.course_code
JOIN student s ON e.student_id = s.student_id
WHERE c.course_code = 'CS101';

-- 집계 쿼리 (학생별 수강 과목 수)
SELECT s.name, COUNT(e.course_code) as course_count
FROM student s
LEFT JOIN enrollment e ON s.student_id = e.student_id
GROUP BY s.student_id, s.name
ORDER BY course_count DESC;
```

#### 6.1.3 수정 (Update)
```sql
-- 성적 입력
UPDATE enrollment 
SET grade = 'A', status = 'COMPLETED'
WHERE student_id = '2024001' AND course_code = 'CS101';
```

#### 6.1.4 삭제 (Delete)
```sql
-- 수강 취소
DELETE FROM enrollment 
WHERE student_id = '2024001' AND course_code = 'CS101';
```

### 6.2 복잡한 쿼리 패턴

#### 6.2.1 교집합 조회
```sql
-- 두 강의를 모두 수강한 학생
SELECT s.student_id, s.name
FROM student s
WHERE EXISTS (
    SELECT 1 FROM enrollment e1 
    WHERE e1.student_id = s.student_id AND e1.course_code = 'CS101'
) AND EXISTS (
    SELECT 1 FROM enrollment e2 
    WHERE e2.student_id = s.student_id AND e2.course_code = 'CS102'
);
```

#### 6.2.2 차집합 조회
```sql
-- CS101은 수강했지만 CS102는 수강하지 않은 학생
SELECT s.student_id, s.name
FROM student s
WHERE EXISTS (
    SELECT 1 FROM enrollment e1 
    WHERE e1.student_id = s.student_id AND e1.course_code = 'CS101'
) AND NOT EXISTS (
    SELECT 1 FROM enrollment e2 
    WHERE e2.student_id = s.student_id AND e2.course_code = 'CS102'
);
```

---

## 7. 고급 패턴 및 변형

### 7.1 자기 참조 다대다 관계
```sql
-- 사용자 친구 관계 (대칭적 관계)
CREATE TABLE user_friends (
    user_id1 BIGINT,
    user_id2 BIGINT,
    friendship_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    PRIMARY KEY (user_id1, user_id2),
    FOREIGN KEY (user_id1) REFERENCES users(user_id),
    FOREIGN KEY (user_id2) REFERENCES users(user_id),
    CHECK (user_id1 < user_id2)  -- 중복 방지: 항상 작은 ID가 먼저
);
```

### 7.2 계층형 다대다 관계
```sql
-- 다단계 권한 시스템
CREATE TABLE user_role_assignments (
    assignment_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_by BIGINT,  -- 권한을 부여한 사용자
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,  -- 권한 만료일
    scope VARCHAR(100),    -- 권한 적용 범위
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (granted_by) REFERENCES users(user_id)
);
```

### 7.3 조건부 다대다 관계
```sql
-- 시간 기반 유효성을 가진 관계
CREATE TABLE product_category_mappings (
    mapping_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CHECK (valid_to IS NULL OR valid_to > valid_from)
);

-- 현재 유효한 매핑 조회용 뷰
CREATE VIEW active_product_categories AS
SELECT product_id, category_id, is_primary, sort_order
FROM product_category_mappings
WHERE valid_from <= CURRENT_DATE 
  AND (valid_to IS NULL OR valid_to > CURRENT_DATE);
```

---

## 8. 성능 및 확장성 고려사항

### 8.1 대용량 데이터 처리
```sql
-- 배치 삽입 최적화
INSERT INTO enrollment (student_id, course_code, enrollment_date)
SELECT 
    s.student_id,
    'CS101' as course_code,
    CURRENT_DATE as enrollment_date
FROM student s
WHERE s.major = 'Computer Science'
  AND NOT EXISTS (
    SELECT 1 FROM enrollment e 
    WHERE e.student_id = s.student_id AND e.course_code = 'CS101'
  );
```

### 8.2 읽기 성능 최적화
```sql
-- 비정규화를 통한 읽기 최적화 (필요시)
CREATE TABLE student_course_summary (
    student_id VARCHAR(10) PRIMARY KEY,
    total_courses INTEGER DEFAULT 0,
    completed_courses INTEGER DEFAULT 0,
    current_gpa DECIMAL(3,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- 트리거로 요약 테이블 자동 업데이트
CREATE OR REPLACE FUNCTION update_student_summary()
RETURNS TRIGGER AS $$
BEGIN
    -- 학생 요약 정보 업데이트 로직
    INSERT INTO student_course_summary (student_id, total_courses, completed_courses)
    SELECT 
        NEW.student_id,
        COUNT(*),
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)
    FROM enrollment 
    WHERE student_id = NEW.student_id
    ON CONFLICT (student_id) DO UPDATE SET
        total_courses = EXCLUDED.total_courses,
        completed_courses = EXCLUDED.completed_courses,
        last_updated = CURRENT_TIMESTAMP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enrollment_summary
    AFTER INSERT OR UPDATE OR DELETE ON enrollment
    FOR EACH ROW EXECUTE FUNCTION update_student_summary();
```

---

## 9. 실무 체크리스트

### 9.1 설계 단계
- [ ] 다대다 관계의 비즈니스 의미 명확히 정의
- [ ] 연결 엔티티의 추가 속성 필요성 검토
- [ ] 관계의 제약조건 (필수/선택, 카디널리티 제한) 정의
- [ ] 데이터 생명주기 및 삭제 정책 수립

### 9.2 구현 단계
- [ ] 적절한 데이터 타입 및 제약조건 설정
- [ ] 외래키 제약조건의 ON DELETE/UPDATE 정책 정의
- [ ] 필요한 인덱스 생성 (특히 외래키 컬럼)
- [ ] 대용량 데이터 대비 파티셔닝 전략 수립

### 9.3 운영 단계
- [ ] 주요 쿼리 패턴의 성능 모니터링
- [ ] Junction Table의 크기 증가 추이 관찰
- [ ] 데이터 아카이빙 정책 수립 및 실행
- [ ] 백업 및 복구 전략에 다대다 관계 고려

---

## 10. 관련 자료
- [데이터베이스 모델링 예시](./database_modeling_example.md)
- [데이터베이스 동시성 제어](./database_concurrency_control.md)
- [좋은 데이터베이스 설계](./good_database_design.md)

---

## 11. 마무리
다대다 관계는 관계형 데이터베이스 설계의 핵심 개념 중 하나로, 개념적 이해부터 물리적 구현까지 단계적 접근이 중요합니다. 특히 Junction Table의 설계와 성능 최적화, 그리고 비즈니스 로직에 맞는 제약조건 설정이 성공적인 구현의 핵심입니다.

**핵심 기억사항**:
- M:N → 1:N + N:1 분해가 기본 원리
- Junction Table은 단순한 연결뿐만 아니라 비즈니스 의미를 가질 수 있음
- 성능과 확장성을 고려한 인덱싱 및 파티셔닝 전략 필수
- 실무에서는 단순한 패턴부터 시작해서 필요에 따라 복잡한 패턴으로 확장