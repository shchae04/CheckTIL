# 로컬 캐시와 리모트 캐시

캐싱은 애플리케이션의 성능을 향상시키는 중요한 기술입니다. 캐시의 위치와 구현 방식에 따라 로컬 캐시와 리모트 캐시로 구분할 수 있으며, 각각의 특성과 적합한 사용 사례가 다릅니다. 이 문서에서는 로컬 캐시와 리모트 캐시의 개념, 특징, 장단점, 그리고 실제 구현 방법에 대해 알아보겠습니다.

## 1. 로컬 캐시(Local Cache)

로컬 캐시는 애플리케이션 인스턴스 내부에 위치하여 메모리에 데이터를 저장하는 캐싱 방식입니다.

### 1.1 특징

- **접근 속도**: 메모리 내에 위치하므로 매우 빠른 접근 속도를 제공합니다.
- **격리성**: 각 애플리케이션 인스턴스가 독립적인 캐시를 가집니다.
- **생명주기**: 애플리케이션 인스턴스의 생명주기와 동일합니다(인스턴스가 종료되면 캐시도 소멸).
- **일관성**: 여러 인스턴스가 있을 경우 캐시 간 일관성 유지가 어려울 수 있습니다.

### 1.2 장점

- **매우 낮은 지연 시간**: 네트워크 호출 없이 메모리에서 직접 데이터를 조회합니다.
- **네트워크 오버헤드 없음**: 외부 시스템과의 통신이 필요 없습니다.
- **구현 용이성**: 외부 의존성 없이 애플리케이션 내에서 구현 가능합니다.
- **네트워크 장애에 강함**: 외부 시스템에 의존하지 않아 네트워크 장애의 영향을 받지 않습니다.

### 1.3 단점

- **메모리 제한**: 단일 인스턴스의 메모리 크기로 제한됩니다.
- **캐시 중복**: 여러 인스턴스에서 동일한 데이터가 중복 저장될 수 있습니다.
- **일관성 문제**: 분산 환경에서 데이터 변경 시 모든 인스턴스의 캐시를 동기화하기 어렵습니다.
- **확장성 제한**: 수평적 확장 시 캐시 효율성이 떨어질 수 있습니다.

### 1.4 적합한 사용 사례

- **읽기 빈도가 높고 변경이 적은 데이터**: 자주 조회되지만 거의 변경되지 않는 데이터
- **인스턴스별 독립적인 데이터**: 다른 인스턴스와 공유할 필요가 없는 데이터
- **작은 규모의 데이터**: 메모리에 부담을 주지 않는 크기의 데이터
- **참조 데이터**: 코드 테이블, 설정 정보 등 정적인 참조 데이터

### 1.5 Java에서의 로컬 캐시 구현 예시

#### 1.5.1 간단한 HashMap 기반 캐시

```java
public class SimpleCache<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    
    public V get(K key) {
        return cache.get(key);
    }
    
    public void put(K key, V value) {
        cache.put(key, value);
    }
    
    public void remove(K key) {
        cache.remove(key);
    }
    
    public void clear() {
        cache.clear();
    }
}
```

#### 1.5.2 Guava Cache 사용 예시

```java
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCacheExample {
    public static void main(String[] args) {
        // 최대 1000개 항목, 항목당 10분 유효 시간을 가진 캐시 생성
        Cache<String, Object> cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        
        // 캐시에 데이터 저장
        cache.put("key1", "value1");
        
        // 캐시에서 데이터 조회
        Object value = cache.getIfPresent("key1");
        System.out.println("Cached value: " + value);
        
        // 캐시에 없으면 계산하여 저장
        try {
            String result = (String) cache.get("key2", () -> computeExpensiveValue("key2"));
            System.out.println("Computed value: " + result);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    private static String computeExpensiveValue(String key) {
        // 비용이 많이 드는 연산 시뮬레이션
        System.out.println("Computing value for " + key);
        return "computed_" + key;
    }
}
```

#### 1.5.3 Caffeine Cache 사용 예시

```java
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;

public class CaffeineCacheExample {
    public static void main(String[] args) {
        // 최대 100개 항목, 항목당 5분 유효 시간을 가진 캐시 생성
        Cache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats() // 통계 기록 활성화
                .build();
        
        // 캐시에 데이터 저장
        cache.put("key1", "value1");
        
        // 캐시에서 데이터 조회
        Object value = cache.getIfPresent("key1");
        System.out.println("Cached value: " + value);
        
        // 캐시에 없으면 계산하여 저장
        String result = (String) cache.get("key2", k -> computeExpensiveValue(k));
        System.out.println("Computed value: " + result);
        
        // 캐시 통계 출력
        System.out.println("Cache stats: " + cache.stats());
    }
    
    private static String computeExpensiveValue(String key) {
        // 비용이 많이 드는 연산 시뮬레이션
        System.out.println("Computing value for " + key);
        return "computed_" + key;
    }
}
```

## 2. 리모트 캐시(Remote Cache)

리모트 캐시는 애플리케이션 외부에 위치한 독립적인 캐시 서버나 서비스를 통해 데이터를 저장하고 관리하는 방식입니다.

### 2.1 특징

- **공유 가능**: 여러 애플리케이션 인스턴스가 동일한 캐시를 공유할 수 있습니다.
- **독립적 생명주기**: 애플리케이션과 별개로 운영되어 애플리케이션 재시작에도 데이터가 유지됩니다.
- **확장성**: 캐시 서버를 독립적으로 확장할 수 있습니다.
- **네트워크 의존성**: 캐시 접근 시 네트워크 통신이 필요합니다.

### 2.2 장점

- **데이터 일관성**: 모든 인스턴스가 동일한 캐시를 참조하므로 일관성 유지가 용이합니다.
- **대용량 데이터 처리**: 전용 캐시 서버를 사용하여 대용량 데이터를 처리할 수 있습니다.
- **영속성**: 애플리케이션 재시작에도 캐시 데이터가 유지됩니다.
- **중앙 관리**: 캐시 정책을 중앙에서 관리할 수 있습니다.

### 2.3 단점

- **네트워크 지연**: 네트워크 통신으로 인한 지연 시간이 발생합니다.
- **네트워크 장애 위험**: 네트워크 문제 발생 시 캐시 접근이 불가능할 수 있습니다.
- **구현 복잡성**: 외부 시스템 연동으로 인한 추가적인 구현 복잡성이 있습니다.
- **운영 비용**: 별도의 캐시 서버 운영에 따른 비용이 발생합니다.

### 2.4 적합한 사용 사례

- **분산 환경**: 여러 서버나 마이크로서비스 간에 데이터를 공유해야 하는 경우
- **세션 데이터**: 사용자 세션 정보와 같이 여러 서버에서 접근해야 하는 데이터
- **대용량 데이터**: 단일 인스턴스의 메모리로 처리하기 어려운 대용량 데이터
- **빈번한 변경 데이터**: 변경 사항을 모든 인스턴스에 즉시 반영해야 하는 데이터

### 2.5 주요 리모트 캐시 기술

#### 2.5.1 Redis

인메모리 데이터 구조 저장소로, 다양한 데이터 타입을 지원하고 높은 성능을 제공합니다.

```java
// Redis 클라이언트 예시 (Jedis 사용)
import redis.clients.jedis.Jedis;

public class RedisExample {
    public static void main(String[] args) {
        // Redis 서버에 연결
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 데이터 저장
        jedis.set("key1", "value1");
        
        // 만료 시간 설정 (초 단위)
        jedis.setex("key2", 60, "value with 1 minute expiry");
        
        // 데이터 조회
        String value = jedis.get("key1");
        System.out.println("Value from Redis: " + value);
        
        // 연결 종료
        jedis.close();
    }
}
```

#### 2.5.2 Memcached

분산 메모리 캐싱 시스템으로, 단순한 키-값 저장소입니다.

```java
// Memcached 클라이언트 예시 (Spymemcached 사용)
import net.spy.memcached.MemcachedClient;

public class MemcachedExample {
    public static void main(String[] args) {
        try {
            // Memcached 서버에 연결
            MemcachedClient client = new MemcachedClient(
                new InetSocketAddress("localhost", 11211));
            
            // 데이터 저장 (키, 만료 시간(초), 값)
            client.set("key1", 3600, "value1");
            
            // 데이터 조회
            Object value = client.get("key1");
            System.out.println("Value from Memcached: " + value);
            
            // 연결 종료
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 2.5.3 Hazelcast

분산 인메모리 데이터 그리드로, 데이터 분산 및 클러스터링 기능을 제공합니다.

```java
// Hazelcast 예시
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastExample {
    public static void main(String[] args) {
        // Hazelcast 인스턴스 생성
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        
        // 분산 맵 가져오기
        IMap<String, String> map = hazelcastInstance.getMap("myDistributedMap");
        
        // 데이터 저장
        map.put("key1", "value1");
        
        // 데이터 조회
        String value = map.get("key1");
        System.out.println("Value from Hazelcast: " + value);
        
        // 인스턴스 종료
        hazelcastInstance.shutdown();
    }
}
```

## 3. 하이브리드 캐싱 전략

많은 시스템에서는 로컬 캐시와 리모트 캐시를 함께 사용하는 하이브리드 접근 방식을 채택합니다.

### 3.1 다중 레벨 캐싱(Multi-Level Caching)

```
클라이언트 -> 로컬 캐시 -> 리모트 캐시 -> 데이터베이스
```

1. 먼저 로컬 캐시에서 데이터를 찾습니다.
2. 로컬 캐시에 없으면 리모트 캐시를 확인합니다.
3. 리모트 캐시에도 없으면 데이터베이스에서 조회합니다.
4. 조회한 데이터는 리모트 캐시와 로컬 캐시에 모두 저장합니다.

### 3.2 구현 예시

```java
public class MultiLevelCache {
    private final Map<String, Object> localCache = new HashMap<>();
    private final RedisClient redisClient;
    private final DatabaseClient dbClient;
    
    public MultiLevelCache(RedisClient redisClient, DatabaseClient dbClient) {
        this.redisClient = redisClient;
        this.dbClient = dbClient;
    }
    
    public Object get(String key) {
        // 1. 로컬 캐시 확인
        Object localValue = localCache.get(key);
        if (localValue != null) {
            System.out.println("Local cache hit for key: " + key);
            return localValue;
        }
        
        // 2. 리모트 캐시 확인
        Object remoteValue = redisClient.get(key);
        if (remoteValue != null) {
            System.out.println("Remote cache hit for key: " + key);
            // 로컬 캐시에도 저장
            localCache.put(key, remoteValue);
            return remoteValue;
        }
        
        // 3. 데이터베이스에서 조회
        Object dbValue = dbClient.get(key);
        if (dbValue != null) {
            System.out.println("Database hit for key: " + key);
            // 리모트 캐시와 로컬 캐시에 모두 저장
            redisClient.set(key, dbValue);
            localCache.put(key, dbValue);
        }
        
        return dbValue;
    }
    
    public void invalidate(String key) {
        // 캐시 무효화 시 로컬과 리모트 모두 제거
        localCache.remove(key);
        redisClient.delete(key);
    }
}
```

### 3.3 Spring Boot에서의 하이브리드 캐싱 구현

Spring Boot에서는 `@Cacheable` 어노테이션과 여러 캐시 매니저를 조합하여 하이브리드 캐싱을 구현할 수 있습니다.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        // 복합 캐시 매니저 생성
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        
        // 로컬 캐시 매니저 (Caffeine)
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES));
        
        // 리모트 캐시 매니저 (Redis)
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)))
                .build();
        
        // 복합 캐시 매니저에 로컬과 리모트 캐시 매니저 등록
        compositeCacheManager.setCacheManagers(Arrays.asList(
                caffeineCacheManager,
                redisCacheManager
        ));
        
        // 모든 캐시에서 찾지 못할 경우 예외 발생하지 않도록 설정
        compositeCacheManager.setFallbackToNoOpCache(true);
        
        return compositeCacheManager;
    }
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 연결 설정
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory();
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }
}
```

## 4. 캐시 일관성 유지 전략

분산 환경에서 로컬 캐시와 리모트 캐시를 함께 사용할 때 일관성 유지는 중요한 과제입니다.

### 4.1 TTL(Time To Live) 설정

모든 캐시 항목에 적절한 만료 시간을 설정하여 오래된 데이터가 자동으로 제거되도록 합니다.

### 4.2 이벤트 기반 캐시 무효화

데이터 변경 시 이벤트를 발행하여 모든 인스턴스의 로컬 캐시를 무효화합니다.

```java
// Redis Pub/Sub을 활용한 캐시 무효화 예시
@Service
public class CacheInvalidationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Object> localCache;
    
    public CacheInvalidationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCache = new ConcurrentHashMap<>();
        
        // 캐시 무효화 이벤트 구독
        redisTemplate.getConnectionFactory().getConnection().subscribe(
            (message, pattern) -> {
                String key = new String(message.getBody());
                System.out.println("Invalidating local cache for key: " + key);
                localCache.remove(key);
            },
            "cache:invalidate".getBytes()
        );
    }
    
    // 데이터 변경 시 캐시 무효화 이벤트 발행
    public void invalidateCache(String key) {
        System.out.println("Publishing cache invalidation for key: " + key);
        redisTemplate.convertAndSend("cache:invalidate", key);
    }
}
```

### 4.3 Write-Through 패턴

데이터 변경 시 데이터베이스와 캐시를 동시에 업데이트하여 일관성을 유지합니다.

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final CacheService cacheService;
    
    public UserService(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }
    
    @Transactional
    public User updateUser(User user) {
        // 1. 데이터베이스 업데이트
        User updatedUser = userRepository.save(user);
        
        // 2. 캐시 업데이트 (Write-Through)
        String cacheKey = "user:" + user.getId();
        cacheService.put(cacheKey, updatedUser);
        
        return updatedUser;
    }
}
```

## 5. 성능 모니터링 및 최적화

캐시 시스템의 효율성을 유지하기 위해서는 지속적인 모니터링과 최적화가 필요합니다.

### 5.1 주요 모니터링 지표

- **히트율(Hit Ratio)**: 전체 요청 중 캐시에서 처리된 비율
- **지연 시간(Latency)**: 캐시 조회에 소요되는 시간
- **메모리 사용량**: 캐시가 사용하는 메모리 양
- **만료/제거율**: 캐시에서 만료되거나 제거되는 항목의 비율

### 5.2 Spring Boot Actuator를 활용한 캐시 모니터링

```java
@Configuration
public class CacheMonitoringConfig {
    @Bean
    public CacheMetricsRegistrar cacheMetricsRegistrar(MeterRegistry registry, CacheManager cacheManager) {
        return new CacheMetricsRegistrar(registry, cacheManager);
    }
}
```

## 결론

로컬 캐시와 리모트 캐시는 각각 고유한 장단점을 가지고 있으며, 애플리케이션의 요구사항에 따라 적절히 선택하거나 조합하여 사용해야 합니다. 로컬 캐시는 매우 빠른 접근 속도를 제공하지만 분산 환경에서 일관성 유지가 어렵고, 리모트 캐시는 데이터 공유와 일관성 유지에 유리하지만 네트워크 지연이 발생합니다.

많은 경우 로컬 캐시와 리모트 캐시를 계층적으로 조합한 하이브리드 접근 방식이 최적의 성능과 일관성을 제공할 수 있습니다. 캐시 전략을 선택할 때는 데이터의 특성, 접근 패턴, 일관성 요구사항, 그리고 시스템의 아키텍처를 종합적으로 고려해야 합니다.

효과적인 캐싱 전략은 애플리케이션의 성능을 크게 향상시키고 데이터베이스 부하를 줄이며, 사용자 경험을 개선하는 데 중요한 역할을 합니다.