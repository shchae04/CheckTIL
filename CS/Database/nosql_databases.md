# NoSQL 데이터베이스

NoSQL(Not Only SQL) 데이터베이스는 전통적인 관계형 데이터베이스와 다른 접근 방식을 취하는 데이터베이스 시스템입니다. 이 문서에서는 NoSQL의 개념, 종류, 그리고 사용 방법에 대해 설명합니다.

## 1. NoSQL이란?

NoSQL은 "Not Only SQL"의 약자로, 관계형 데이터베이스(RDBMS)의 한계를 극복하기 위해 등장한 데이터베이스 시스템입니다. 주요 특징은 다음과 같습니다:

- **스키마 없음**: 고정된 테이블 스키마가 필요 없음
- **수평적 확장성**: 분산 아키텍처를 통한 쉬운 확장
- **대용량 데이터 처리**: 빅데이터 환경에 적합
- **유연한 데이터 모델**: 다양한 형태의 데이터 저장 가능
- **고가용성**: 분산 시스템을 통한 장애 대응

### 1.1 NoSQL vs RDBMS

| 특성 | NoSQL | RDBMS |
|------|-------|-------|
| 데이터 모델 | 다양함(문서, 키-값, 컬럼, 그래프 등) | 테이블 기반 |
| 스키마 | 유연함(스키마리스) | 고정됨(엄격한 스키마) |
| 확장성 | 수평적 확장(Scale-out) | 수직적 확장(Scale-up) |
| 트랜잭션 | 제한적 ACID 또는 BASE | ACID |
| 쿼리 언어 | 데이터베이스별 API | SQL |
| 관계 | 명시적이지 않음 | 외래 키를 통한 명시적 관계 |

## 2. NoSQL 데이터베이스 유형

NoSQL 데이터베이스는 데이터 저장 방식에 따라 크게 네 가지 유형으로 분류됩니다:

### 2.1 문서형 데이터베이스 (Document Stores)

JSON, BSON, XML과 같은 문서 형태로 데이터를 저장합니다.

**주요 제품**:
- **MongoDB**: 가장 인기 있는 문서형 데이터베이스
- **CouchDB**: HTTP API와 JSON 기반
- **Firebase Firestore**: 구글의 클라우드 기반 문서형 데이터베이스

**사용 예시 (MongoDB)**:
```javascript
// 문서 생성
db.users.insertOne({
  name: "홍길동",
  age: 30,
  email: "hong@example.com",
  interests: ["프로그래밍", "독서", "여행"],
  address: {
    city: "서울",
    zipcode: "12345"
  }
});

// 문서 조회
db.users.find({ age: { $gt: 25 } });

// 문서 업데이트
db.users.updateOne(
  { name: "홍길동" },
  { $set: { age: 31 } }
);
```

### 2.2 키-값 데이터베이스 (Key-Value Stores)

단순한 키-값 쌍으로 데이터를 저장합니다. 매우 빠른 읽기/쓰기 성능을 제공합니다.

**주요 제품**:
- **Redis**: 인메모리 키-값 저장소, 캐싱에 많이 사용
- **DynamoDB**: AWS의 관리형 NoSQL 데이터베이스
- **Riak**: 고가용성 분산 키-값 저장소

**사용 예시 (Redis)**:
```bash
# 키-값 저장
SET user:1000 '{"name":"홍길동", "email":"hong@example.com"}'

# 키-값 조회
GET user:1000

# 만료 시간 설정 (초 단위)
SETEX session:user:1000 3600 "active"

# 리스트 작업
LPUSH notifications:user:1000 "새 메시지가 도착했습니다"
LRANGE notifications:user:1000 0 -1
```

### 2.3 컬럼형 데이터베이스 (Column Stores)

데이터를 컬럼 단위로 저장하며, 대용량 분석 작업에 효율적입니다.

**주요 제품**:
- **Cassandra**: 높은 확장성과 가용성
- **HBase**: Hadoop 생태계의 일부
- **ScyllaDB**: Cassandra 호환 고성능 데이터베이스

**사용 예시 (Cassandra CQL)**:
```sql
-- 키스페이스 생성
CREATE KEYSPACE example WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};

-- 테이블 생성
CREATE TABLE example.users (
  user_id UUID PRIMARY KEY,
  name TEXT,
  email TEXT,
  created_at TIMESTAMP
);

-- 데이터 삽입
INSERT INTO example.users (user_id, name, email, created_at)
VALUES (uuid(), '홍길동', 'hong@example.com', toTimestamp(now()));

-- 데이터 조회
SELECT * FROM example.users WHERE user_id = 123e4567-e89b-12d3-a456-426614174000;
```

### 2.4 그래프 데이터베이스 (Graph Databases)

노드와 엣지로 구성된 그래프 구조로 데이터를 저장합니다. 복잡한 관계를 가진 데이터에 적합합니다.

**주요 제품**:
- **Neo4j**: 가장 인기 있는 그래프 데이터베이스
- **JanusGraph**: 분산형 그래프 데이터베이스
- **Amazon Neptune**: AWS의 관리형 그래프 데이터베이스

**사용 예시 (Neo4j Cypher)**:
```cypher
// 노드 생성
CREATE (john:Person {name: '홍길동', age: 30})
CREATE (jane:Person {name: '김영희', age: 28})
CREATE (company:Company {name: '테크 주식회사', founded: 2010})

// 관계 생성
CREATE (john)-[:WORKS_AT {since: 2018}]->(company)
CREATE (jane)-[:WORKS_AT {since: 2019}]->(company)
CREATE (john)-[:KNOWS {since: 2015}]->(jane)

// 관계 쿼리
MATCH (p:Person)-[:WORKS_AT]->(c:Company)
WHERE c.name = '테크 주식회사'
RETURN p.name, p.age
```

## 3. NoSQL 데이터베이스 사용 방법

### 3.1 적합한 NoSQL 데이터베이스 선택

프로젝트 요구사항에 맞는 NoSQL 데이터베이스를 선택하는 것이 중요합니다:

- **문서형**: 복잡하고 중첩된 데이터 구조, 자주 변경되는 스키마
- **키-값**: 단순한 데이터 구조, 빠른 읽기/쓰기, 캐싱
- **컬럼형**: 대용량 데이터 분석, 시계열 데이터
- **그래프**: 복잡한 관계를 가진 데이터, 소셜 네트워크, 추천 시스템

### 3.2 데이터 모델링

NoSQL 데이터베이스에서의 데이터 모델링은 관계형 데이터베이스와 다른 접근 방식이 필요합니다:

1. **비정규화(Denormalization)**: 데이터 중복을 허용하여 쿼리 성능 향상
2. **쿼리 중심 설계**: 자주 사용되는 쿼리 패턴에 최적화
3. **임베딩 vs 참조**: 관련 데이터를 임베딩할지 참조할지 결정

**예시 (MongoDB 문서 설계)**:
```javascript
// 임베딩 방식 (1:N 관계)
var userWithOrders = {
  "_id": ObjectId("5f8a7b2b9d3b6c001f7a1234"),
  "name": "홍길동",
  "email": "hong@example.com",
  "orders": [
    {
      "orderId": "ORD-001",
      "date": ISODate("2023-01-15"),
      "items": ["상품A", "상품B"],
      "total": 50000
    },
    {
      "orderId": "ORD-002",
      "date": ISODate("2023-02-20"),
      "items": ["상품C"],
      "total": 30000
    }
  ]
};

// 참조 방식 (N:M 관계)
// users 컬렉션
var user = {
  "_id": ObjectId("5f8a7b2b9d3b6c001f7a1234"),
  "name": "홍길동",
  "email": "hong@example.com"
};

// orders 컬렉션
var order = {
  "_id": ObjectId("6a1b2c3d4e5f6a001f7b2345"),
  "orderId": "ORD-001",
  "userId": ObjectId("5f8a7b2b9d3b6c001f7a1234"),
  "date": ISODate("2023-01-15"),
  "items": ["상품A", "상품B"],
  "total": 50000
};
```

### 3.3 확장성 고려사항

NoSQL 데이터베이스의 주요 장점 중 하나는 확장성입니다:

- **샤딩(Sharding)**: 데이터를 여러 서버에 분산 저장
- **레플리케이션(Replication)**: 데이터 복제를 통한 가용성 향상
- **일관성 모델**: 강한 일관성 vs 최종 일관성 선택

### 3.4 Java에서 NoSQL 사용 예시

**MongoDB와 Spring Data MongoDB 사용 예시**:

```java
// 엔티티 클래스
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private List<String> interests;
    private Address address;

    // 생성자, getter, setter 생략
}

@Document
public class Address {
    private String city;
    private String zipcode;

    // 생성자, getter, setter 생략
}

// 리포지토리 인터페이스
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByNameContaining(String name);
    List<User> findByInterestsContaining(String interest);
    List<User> findByAddress_City(String city);
}

// 서비스 클래스
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findUsersByInterest(String interest) {
        return userRepository.findByInterestsContaining(interest);
    }

    // 몽고DB 템플릿을 사용한 복잡한 쿼리
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<User> findUsersByAgeRange(int minAge, int maxAge) {
        Query query = new Query();
        query.addCriteria(Criteria.where("age").gte(minAge).lte(maxAge));
        return mongoTemplate.find(query, User.class);
    }
}
```

**Redis와 Spring Data Redis 사용 예시**:

```java
// 설정 클래스
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }
}

// 서비스 클래스
@Service
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    public void cacheData(String key, Object data, long ttlSeconds) {
        valueOps.set(key, data, Duration.ofSeconds(ttlSeconds));
    }

    public Object getCachedData(String key) {
        return valueOps.get(key);
    }

    public void deleteCachedData(String key) {
        redisTemplate.delete(key);
    }
}
```

## 4. NoSQL 사용 시 고려사항

### 4.1 일관성과 가용성 (CAP 이론)

CAP 이론에 따르면 분산 시스템은 다음 세 가지 속성 중 최대 두 가지만 동시에 만족할 수 있습니다:

- **일관성(Consistency)**: 모든 노드가 동일한 시점에 동일한 데이터를 볼 수 있음
- **가용성(Availability)**: 모든 요청이 성공 또는 실패 응답을 받음
- **분할 허용성(Partition Tolerance)**: 네트워크 분할이 발생해도 시스템이 계속 작동

NoSQL 데이터베이스는 일반적으로 일관성을 희생하고 가용성과 분할 허용성을 선택합니다(AP 시스템).

### 4.2 트랜잭션 처리

대부분의 NoSQL 데이터베이스는 ACID 트랜잭션을 완전히 지원하지 않습니다. 대신 BASE 원칙을 따릅니다:

- **Basically Available**: 기본적으로 가용성 보장
- **Soft state**: 시스템 상태가 시간에 따라 변할 수 있음
- **Eventually consistent**: 최종적으로 일관성 보장

최근에는 MongoDB와 같은 일부 NoSQL 데이터베이스가 제한된 범위에서 ACID 트랜잭션을 지원하기 시작했습니다.

### 4.3 보안 고려사항

NoSQL 데이터베이스 사용 시 보안 측면에서 고려해야 할 사항:

- **인증 및 권한 관리**: 강력한 인증 메커니즘 사용
- **데이터 암호화**: 저장 데이터 및 전송 데이터 암호화
- **NoSQL 인젝션 방지**: 사용자 입력 검증
- **감사 로깅**: 데이터베이스 액세스 및 변경 로깅

## 5. 결론

NoSQL 데이터베이스는 대용량 데이터, 높은 확장성, 유연한 스키마가 필요한 현대적인 애플리케이션에 적합한 솔루션입니다. 각 유형의 NoSQL 데이터베이스는 특정 사용 사례에 최적화되어 있으므로, 프로젝트 요구사항을 신중하게 분석하여 적절한 데이터베이스를 선택하는 것이 중요합니다.

관계형 데이터베이스와 NoSQL 데이터베이스는 상호 배타적이지 않으며, 많은 현대적인 시스템에서는 두 가지 유형을 함께 사용하는 다중 데이터베이스 아키텍처(Polyglot Persistence)를 채택하고 있습니다.

## 참고 자료

- [MongoDB 공식 문서](https://docs.mongodb.com/)
- [Redis 공식 문서](https://redis.io/documentation)
- [Apache Cassandra 공식 문서](https://cassandra.apache.org/doc/latest/)
- [Neo4j 공식 문서](https://neo4j.com/docs/)
- [NoSQL Databases: A Survey and Decision Guidance](https://medium.baqend.com/nosql-databases-a-survey-and-decision-guidance-ea7823a822d)
- [CAP 이론 설명](https://www.ibm.com/cloud/learn/cap-theorem)
