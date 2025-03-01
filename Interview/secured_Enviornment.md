# 환경변수를 안전하게 관리하는 방법 

## 1. 환경변수란?
환경변수(Environment Variables)는 운영체제나 애플리케이션에서 설정값을 저장하는 변수로,  
API 키, 데이터베이스 접속 정보, 비밀키 등 민감한 정보를 저장하는 데 사용됩니다.

## 2. 환경변수를 안전하게 관리하는 방법

### 2.1. `.env` 파일 사용
- 환경변수를 코드에 직접 하드코딩하지 않고 `.env` 파일에 저장합니다.
- `.env` 파일을 **`.gitignore`에 추가**하여 Git에 푸시되지 않도록 합니다.

#### 예시 (`.env` 파일)
```env
DATABASE_URL=postgres://user:password@localhost:5432/mydb
API_KEY=your-secret-api-key
```

#### 예시 (`.env` 파일을 로드하는 방법 - Spring Boot)
Spring Boot에서는 `.env` 파일이 아닌 `application.properties` 또는 `application.yml`에서 환경변수를 사용합니다.

##### `application.properties` 예시:
```properties
database.url=${DATABASE_URL}
api.key=${API_KEY}
```

##### `application.yml` 예시:
```yaml
database:
  url: ${DATABASE_URL}
api:
  key: ${API_KEY}
```

Spring Boot 실행 시 환경변수를 설정하는 방법:
```sh
export DATABASE_URL=postgres://user:password@localhost:5432/mydb
export API_KEY=your-secret-api-key
```

### 2.2. 환경변수는 직접 코드에 하드코딩하지 않는다
```java
// ❌ 위험한 방식 (환경변수 직접 하드코딩)
private static final String API_KEY = "your-secret-api-key";

// ✅ 안전한 방식 (환경변수 사용)
@Value("${api.key}")
private String apiKey;
```

### 2.3. CI/CD 및 서버에서 환경변수 설정
- 로컬 환경에서는 `.env` 파일을 사용하지만, 운영 서버에서는 **OS의 환경변수**에 직접 설정하는 것이 좋습니다.
- Docker 컨테이너에서는 `docker run --env-file .env` 옵션을 사용해 환경변수를 주입할 수 있습니다.
- Kubernetes에서는 **Secrets**를 활용하여 환경변수를 안전하게 관리할 수 있습니다.

### 2.4. Spring Vault를 이용한 환경변수 관리

Spring Vault를 사용하면 **HashiCorp Vault**를 활용하여 **민감한 정보를 안전하게 저장하고 불러올 수 있습니다**.

#### Spring Vault 설정 방법

1. **Vault 설치 및 실행 (Docker)**
   ```sh
   docker run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' -p 8200:8200 vault
   ```

2. **Vault에 보안 값 저장**
   ```sh
   export VAULT_ADDR='http://127.0.0.1:8200'
   vault login myroot
   vault kv put secret/myapp database.url="jdbc:mysql://localhost:3306/mydb" api.key="super-secret-key"
   ```

3. **Spring Boot에서 Vault 연동**
   `pom.xml`에 Vault 의존성 추가:
   ```xml
   <dependency>
       <groupId>org.springframework.vault</groupId>
       <artifactId>spring-vault-core</artifactId>
       <version>2.3.1</version>
   </dependency>
   ```
   `build.gradle`에 의존성 추가
   ``` groovy
   dependencies {
    implementation 'org.springframework.vault:spring-vault-core:2.3.1'
   }
   ```


4. **`bootstrap.yml` 설정**
   ```yaml
   spring:
     cloud:
       vault:
         uri: http://127.0.0.1:8200
         token: myroot
         kv:
           enabled: true
     application:
       name: myapp
   ```

5. **환경변수 로드 (`@Value` 활용)**
   ```java
   @Component
   public class VaultConfig {
       @Value("${database.url}")
       private String databaseUrl;

       @Value("${api.key}")
       private String apiKey;
   }
   ```

### 2.5. `.env` 파일을 안전하게 공유하는 방법
- `.env` 파일을 직접 공유하지 않고, **환경변수 템플릿 파일(`.env.example`)**을 제공한다.

#### `.env.example` 예시
```env
DATABASE_URL= # 여기에 데이터베이스 URL을 입력하세요
API_KEY= # 여기에 API 키를 입력하세요
```

### 2.6. 환경변수 유출 방지
- `.env` 파일을 깃허브에 업로드하지 않도록 `.gitignore`에 반드시 추가한다
- 환경변수가 포함된 로그를 남기지 않도록 주의한다
- `.env` 파일을 암호화하여 저장할 수도 있습니다 (예: `sops`, `age` 사용).
- Spring Vault와 같은 **비밀 관리 시스템(Vault, AWS Secrets Manager, Azure Key Vault 등)**을 활용하는 것이 더욱 안전하다.

---

## 3. 결론
Spring 환경에서 환경변수를 안전하게 관리하려면:

✅ `.env` 또는 `application.yml`을 사용하고 `.gitignore`에 추가  
✅ 코드에 직접 민감한 정보를 하드코딩하지 X  
✅ 운영 환경에서는 OS 환경변수 또는 Vault를 사용  
✅ `.env.example` 파일을 제공하여 팀원들과 안전하게 공유  
✅ Spring Vault를 활용하면 보안성을 더욱 강화할 수 있다.
