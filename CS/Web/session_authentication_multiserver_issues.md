# 다중 서버 환경에서 세션 기반 인증 방식의 문제점

## 1. 한 줄 정의
다중 서버 환경에서 세션 기반 인증은 세션 데이터가 특정 서버에 저장되어 발생하는 세션 불일치, 확장성 제한, 로드밸런싱 복잡성 등의 문제를 야기한다.

---

## 2. 주요 문제점

### 2-1. 세션 고립 문제 (Session Isolation)
- **문제 상황**: 사용자가 로그인한 서버와 다른 서버로 요청이 라우팅될 경우
- **원인**: 세션 데이터가 각 서버의 메모리에 개별적으로 저장
- **결과**: 인증된 사용자임에도 불구하고 재로그인 요구

```
사용자 → 로드밸런서 → 서버A (세션 생성)
사용자 → 로드밸런서 → 서버B (세션 없음, 인증 실패)
```

### 2-2. 스티키 세션(Sticky Session)의 한계
- **개념**: 특정 사용자를 항상 같은 서버로 라우팅
- **구현 방법**:
  - IP 해시 기반 라우팅
  - 쿠키 기반 서버 지정
  - 세션 ID 기반 서버 매핑

```nginx
# Nginx 스티키 세션 설정 예시
upstream backend {
    ip_hash;  # 클라이언트 IP 기반 라우팅
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}
```

**스티키 세션의 문제점**:
- **불균등 부하 분산**: 특정 서버에 트래픽 집중 가능
- **확장성 제한**: 새 서버 추가 시 세션 재분배 복잡
- **장애 복구 어려움**: 특정 서버 다운 시 해당 사용자들 모든 세션 손실

### 2-3. 메모리 사용량 증가
- **서버별 세션 저장**: 각 서버가 자체 세션 데이터 보관
- **중복 저장**: 같은 사용자 정보가 여러 서버에 중복 저장 가능
- **메모리 누수**: 세션 만료 처리 미흡 시 메모리 계속 증가

```java
// 메모리 기반 세션 저장 문제 예시
@Component
public class InMemorySessionStore {
    private Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    // 문제: 서버별로 독립적인 세션 저장소
    public void storeSession(String sessionId, HttpSession session) {
        sessions.put(sessionId, session);
    }
}
```

---

## 3. 확장성 및 성능 문제

### 3-1. 수평 확장의 어려움
- **신규 서버 추가**: 기존 세션 데이터와 연결 불가
- **서버 제거**: 해당 서버의 모든 세션 손실
- **오토스케일링 제약**: 동적 서버 추가/제거 시 세션 관리 복잡

### 3-2. 장애 복구 및 고가용성 문제
- **단일 서버 장애**: 해당 서버의 모든 활성 세션 손실
- **롤링 업데이트**: 서버 재시작 시 세션 유지 불가
- **백업 및 복구**: 세션 데이터 백업 전략 부재

```
서버A 다운 → 서버A의 1000개 활성 세션 모두 손실
→ 1000명 사용자 강제 로그아웃 → 사용자 경험 악화
```

### 3-3. 동기화 문제
- **세션 상태 변경**: 한 서버에서 세션 수정 시 다른 서버에 반영 안됨
- **동시성 문제**: 같은 사용자가 여러 서버에 동시 접근 시 데이터 불일치
- **캐시 무효화**: 세션 정보 변경 시 다른 서버 캐시 갱신 필요

---

## 4. 보안 관련 문제

### 4-1. 세션 하이재킹 위험 증가
- **네트워크 통신**: 서버 간 세션 동기화를 위한 네트워크 노출
- **세션 ID 탈취**: 여러 서버에 걸친 세션 ID 노출 위험
- **암호화 부족**: 서버 간 세션 데이터 전송 시 암호화 필요

### 4-2. 일관성 없는 보안 정책
- **서버별 상이한 설정**: 각 서버마다 다른 세션 타임아웃 설정
- **권한 검증 불일치**: 세션 기반 권한 확인 시 서버별 차이 발생

---

## 5. 해결 방안

### 5-1. 중앙 세션 저장소 (Centralized Session Store)
```redis
# Redis를 이용한 중앙 세션 저장소
SET session:abc123 "user_id:1001,role:admin,login_time:1640995200"
EXPIRE session:abc123 1800  # 30분 TTL
```

**장점**:
- 모든 서버가 동일한 세션 데이터 접근
- 서버 추가/제거 시에도 세션 유지
- 확장성 및 고가용성 확보

**단점**:
- 추가 인프라 필요 (Redis, Memcached 등)
- 네트워크 레이턴시 발생
- 중앙 저장소 장애 시 전체 시스템 영향

### 5-2. 토큰 기반 인증 (JWT)
```javascript
// JWT 토큰 기반 인증
const jwt = require('jsonwebtoken');

// 토큰 생성
const token = jwt.sign({
    userId: 1001,
    role: 'admin',
    exp: Math.floor(Date.now() / 1000) + (60 * 60) // 1시간
}, 'secret-key');

// 토큰 검증 (서버별 상태 불필요)
const decoded = jwt.verify(token, 'secret-key');
```

**장점**:
- 서버 상태 불필요 (Stateless)
- 확장성 우수
- 마이크로서비스 아키텍처에 적합

**단점**:
- 토큰 크기가 세션 ID보다 큼
- 토큰 무효화 어려움
- 보안 키 관리 복잡

### 5-3. 세션 복제 (Session Replication)
```java
// Spring Session을 이용한 세션 클러스터링
@EnableRedisHttpSession
@Configuration
public class SessionConfig {
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory(
            new RedisStandaloneConfiguration("localhost", 6379)
        );
    }
}
```

**구현 방식**:
- **Active-Active**: 모든 서버에 세션 복제
- **Active-Passive**: 마스터 서버에서 슬레이브로 복제
- **DB 기반**: 데이터베이스에 세션 정보 저장

---

## 6. 아키텍처별 해결 전략

### 6-1. 마이크로서비스 아키텍처
```yaml
# API Gateway에서 JWT 토큰 검증
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-gateway-config
data:
  nginx.conf: |
    location /api/ {
        auth_jwt "API";
        auth_jwt_key_file /etc/nginx/jwt.key;
        proxy_pass http://backend-service;
    }
```

### 6-2. 컨테이너 환경 (Docker/Kubernetes)
```yaml
# StatefulSet을 이용한 세션 고정
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: web-app
spec:
  serviceName: web-app
  replicas: 3
  template:
    spec:
      containers:
      - name: app
        env:
        - name: SESSION_STORE_TYPE
          value: "redis"
        - name: REDIS_URL
          value: "redis://redis-cluster:6379"
```

### 6-3. 클라우드 환경
```python
# AWS ElastiCache를 이용한 세션 저장
import redis
import pickle

class CloudSessionStore:
    def __init__(self):
        self.redis_client = redis.Redis(
            host='elasticache-cluster.abc123.cache.amazonaws.com',
            port=6379,
            decode_responses=False
        )

    def save_session(self, session_id, session_data):
        serialized_data = pickle.dumps(session_data)
        self.redis_client.setex(session_id, 1800, serialized_data)

    def get_session(self, session_id):
        data = self.redis_client.get(session_id)
        return pickle.loads(data) if data else None
```

---

## 7. 성능 및 비용 고려사항

### 7-1. 레이턴시 분석
```
로컬 메모리 세션: ~0.1ms
Redis 세션 저장소: ~1-5ms (네트워크 환경에 따라)
DB 세션 저장소: ~10-50ms
```

### 7-2. 확장성 비교
| 방식 | 확장성 | 복잡도 | 비용 | 가용성 |
|------|--------|--------|------|--------|
| 로컬 세션 | 낮음 | 낮음 | 낮음 | 낮음 |
| 스티키 세션 | 중간 | 중간 | 낮음 | 중간 |
| 중앙 저장소 | 높음 | 중간 | 중간 | 높음 |
| JWT 토큰 | 매우 높음 | 중간 | 낮음 | 높음 |

---

## 8. 실무 적용 가이드

### 8-1. 선택 기준
- **소규모 서비스**: 스티키 세션 + 로컬 저장
- **중간 규모**: Redis 중앙 세션 저장소
- **대규모/MSA**: JWT 토큰 기반 인증
- **하이브리드**: 중요 데이터는 토큰, 임시 데이터는 중앙 세션

### 8-2. 모니터링 포인트
```
- 세션 생성/소멸 속도
- 세션 저장소 메모리 사용량
- 세션 검증 레이턴시
- 서버별 세션 분포도
- 세션 관련 에러율
```

---

## 9. 예상 면접 질문

### 9-1. 기본 개념
1. 다중 서버 환경에서 세션 기반 인증의 주요 문제점은?
2. 스티키 세션의 장단점과 대안은?
3. JWT와 세션 기반 인증의 차이점과 선택 기준은?

### 9-2. 아키텍처 설계
1. 1만 동시 사용자를 처리하는 웹 서비스의 세션 관리 전략은?
2. 마이크로서비스에서 사용자 인증을 어떻게 처리하시겠나요?
3. 세션 저장소 장애 시 대응 방안은?

### 9-3. 트러블슈팅
1. 사용자가 갑자기 로그아웃되는 문제의 원인과 해결방법은?
2. 세션 관련 성능 이슈를 어떻게 진단하고 해결하나요?

---

## 10. 핵심 요약

### 10-1. 주요 문제점
- **세션 고립**: 서버별 독립적인 세션 저장
- **확장성 제한**: 수평 확장 시 세션 관리 복잡
- **가용성 문제**: 서버 장애 시 세션 손실

### 10-2. 해결 방향
- **중앙 집중화**: Redis, DB 등 중앙 세션 저장소
- **무상태화**: JWT 등 토큰 기반 인증
- **하이브리드**: 상황에 맞는 적절한 조합

### 10-3. 백엔드 개발자 핵심 포인트
- 다중 서버 환경에서는 세션 상태 관리가 가장 중요한 설계 요소다
- 확장성과 성능, 비용을 종합적으로 고려한 인증 전략이 필요하다
- 모니터링과 장애 대응 계획을 미리 수립해야 한다