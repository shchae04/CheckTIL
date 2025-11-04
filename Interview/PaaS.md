# PaaS란?

## 1. 한 줄 정의
PaaS(Platform as a Service)는 애플리케이션 개발과 배포에 필요한 플랫폼과 인프라를 클라우드 서비스로 제공하여, 개발자가 인프라 관리 없이 코드 작성에만 집중할 수 있게 하는 클라우드 서비스 모델이다.

---

## 2. 클라우드 서비스 모델 비교

### 2-1. IaaS (Infrastructure as a Service)
- **제공 범위**: 가상 서버, 스토리지, 네트워크 등 인프라만 제공
- **관리 대상**: OS, 런타임, 애플리케이션을 사용자가 직접 관리
- **자유도**: 높음 (모든 설정 커스터마이징 가능)
- **예시**: AWS EC2, Google Compute Engine, Azure Virtual Machines

```
[사용자 관리]
- 애플리케이션
- 데이터
- 런타임
- 미들웨어
- OS

[제공자 관리]
- 가상화
- 서버
- 스토리지
- 네트워크
```

### 2-2. PaaS (Platform as a Service)
- **제공 범위**: 인프라 + OS + 런타임 + 미들웨어까지 제공
- **관리 대상**: 애플리케이션과 데이터만 사용자가 관리
- **자유도**: 중간 (플랫폼 제약 내에서 개발)
- **예시**: Heroku, Google App Engine, AWS Elastic Beanstalk, Azure App Service

```
[사용자 관리]
- 애플리케이션
- 데이터

[제공자 관리]
- 런타임
- 미들웨어
- OS
- 가상화
- 서버
- 스토리지
- 네트워크
```

### 2-3. SaaS (Software as a Service)
- **제공 범위**: 완성된 소프트웨어를 서비스로 제공
- **관리 대상**: 사용자는 설정 및 사용만 함
- **자유도**: 낮음 (정해진 기능만 사용)
- **예시**: Gmail, Salesforce, Notion, Slack

```
[사용자 관리]
- 설정 및 사용

[제공자 관리]
- 애플리케이션
- 데이터
- 런타임
- 미들웨어
- OS
- 가상화
- 서버
- 스토리지
- 네트워크
```

### 2-4. FaaS (Function as a Service) / Serverless
- **제공 범위**: PaaS보다 더 추상화, 함수 단위 실행
- **관리 대상**: 함수 코드만 작성
- **자유도**: 매우 낮음 (함수 단위 제약)
- **예시**: AWS Lambda, Google Cloud Functions, Azure Functions

---

## 3. PaaS의 핵심 특성

### 3-1. 자동화된 인프라 관리
- **자동 스케일링**: 트래픽에 따라 자동으로 서버 증설/축소
- **로드 밸런싱**: 자동으로 트래픽 분산
- **고가용성**: 자동 장애 복구 및 다중화

```bash
# 예시: Heroku에서 자동 스케일링
heroku ps:scale web=5  # 웹 서버를 5개로 확장

# AWS Elastic Beanstalk - 설정만으로 자동 스케일링
# CPU 사용률이 70% 초과 시 자동으로 인스턴스 추가
```

### 3-2. 개발 환경 통합
- **빌드 자동화**: Git push만으로 자동 배포
- **CI/CD 통합**: 자동 테스트 및 배포 파이프라인
- **다양한 언어 지원**: Node.js, Java, Python, Go 등

```bash
# Heroku 배포 예시
git add .
git commit -m "Update feature"
git push heroku main  # 자동으로 빌드 + 배포

# 자동으로 수행되는 작업:
# 1. 의존성 설치
# 2. 빌드
# 3. 컨테이너 생성
# 4. 배포
# 5. 롤링 업데이트
```

### 3-3. 데이터베이스 및 부가 서비스 통합
- **관리형 DB**: PostgreSQL, MySQL, MongoDB 등 원클릭 설치
- **애드온**: Redis, Elasticsearch, 모니터링 도구 등 쉽게 추가
- **자동 백업**: 정기적인 데이터 백업 자동화

```bash
# Heroku Add-on 추가 예시
heroku addons:create heroku-postgresql:hobby-dev
heroku addons:create heroku-redis:hobby-dev
heroku addons:create papertrail:choklad  # 로그 관리

# 자동으로 환境변수 설정 및 연동 완료
```

### 3-4. 비용 구조
- **종량제**: 사용한 만큼만 과금
- **프리 티어**: 소규모 프로젝트는 무료로 시작 가능
- **예측 가능한 비용**: 플랫폼에서 비용 예측 도구 제공

---

## 4. 주요 PaaS 서비스 비교

### 4-1. Heroku
- **특징**: 가장 사용하기 쉬움, Git 기반 배포
- **강점**: 빠른 프로토타이핑, 스타트업에 적합
- **약점**: 상대적으로 비싼 가격, 제한적인 커스터마이징

```bash
# Heroku 배포 흐름
heroku create my-app
git push heroku main
heroku open

# Procfile로 프로세스 정의
echo "web: node server.js" > Procfile
```

### 4-2. AWS Elastic Beanstalk
- **특징**: AWS 서비스와 긴밀한 통합
- **강점**: 세밀한 설정 가능, AWS 생태계 활용
- **약점**: 학습 곡선이 높음

```bash
# EB CLI 배포
eb init
eb create production
eb deploy

# .ebextensions로 상세 설정 가능
```

### 4-3. Google App Engine
- **특징**: 구글 인프라 활용, 자동 스케일링 강력
- **강점**: 트래픽 급증 대응 우수, Firebase 연동
- **약점**: 플랫폼 종속성 높음

```yaml
# app.yaml 설정 예시
runtime: nodejs20
instance_class: F2
automatic_scaling:
  min_instances: 1
  max_instances: 10
```

### 4-4. Azure App Service
- **특징**: Microsoft 생태계와 통합
- **강점**: .NET 애플리케이션에 최적화, 하이브리드 클라우드 지원
- **약점**: 타 플랫폼 대비 생태계 제한적

---

## 5. 사용 사례

### 5-1. PaaS를 사용해야 하는 경우
- **빠른 MVP 개발**: 인프라 걱정 없이 빠르게 제품 출시
- **스타트업**: 초기 인프라 투자 없이 시작
- **마이크로서비스**: 여러 서비스를 빠르게 배포
- **프로토타입**: 아이디어 검증용 빠른 구현

```python
# 예시: Flask 앱을 Heroku에 배포
# 1. requirements.txt 작성
Flask==2.3.0
gunicorn==20.1.0

# 2. Procfile 작성
web: gunicorn app:app

# 3. Git push만으로 배포 완료
git push heroku main
```

### 5-2. IaaS를 사용해야 하는 경우
- **세밀한 제어 필요**: OS 레벨 커스터마이징
- **레거시 시스템**: 특정 OS 버전, 라이브러리 필요
- **비용 최적화**: 대규모 트래픽에서 더 저렴
- **컴플라이언스**: 특정 보안 요구사항

### 5-3. FaaS를 사용해야 하는 경우
- **이벤트 기반 처리**: 파일 업로드, 메시지 큐 처리
- **간헐적 작업**: 크론 잡, 배치 처리
- **API Gateway**: 간단한 REST API
- **극단적 비용 절감**: 요청 없을 때 비용 0원

---

## 6. 백엔드 개발자 관점의 중요성

### 6-1. 생산성
- **인프라 관리 제거**: 서버 설정, 모니터링에 소요되는 시간 절약
- **빠른 배포**: Git push만으로 몇 분 내 배포
- **개발 집중**: 비즈니스 로직 구현에만 집중

### 6-2. 운영 부담 감소
- **자동 스케일링**: 트래픽 급증 시 자동 대응
- **모니터링 통합**: 로그, 메트릭이 기본 제공
- **보안 패치**: 플랫폼 차원에서 자동 적용

### 6-3. 비용 효율성
- **초기 비용 절감**: 서버 구매, 데이터센터 불필요
- **탄력적 과금**: 트래픽에 따라 비용 조절
- **인력 비용 절감**: DevOps 엔지니어 불필요

### 6-4. 기술 부채 관리
- **플랫폼 종속성**: 특정 PaaS에 종속될 위험
- **마이그레이션 비용**: 다른 플랫폼으로 이전 시 비용
- **제한된 커스터마이징**: 플랫폼 제약 내에서만 작업

---

## 7. 핵심 요약

| 특성 | IaaS | PaaS | SaaS |
|------|------|------|------|
| **제어 수준** | 높음 | 중간 | 낮음 |
| **관리 범위** | OS부터 전체 | 앱, 데이터만 | 사용만 |
| **개발 속도** | 느림 | 빠름 | 해당 없음 |
| **유연성** | 매우 높음 | 중간 | 낮음 |
| **비용** | 가변적 | 중간~높음 | 구독제 |
| **학습 곡선** | 가파름 | 완만 | 매우 쉬움 |
| **사용 사례** | 레거시, 대규모 | MVP, 스타트업 | 완성 솔루션 |

### 7-1. PaaS 선택 기준
- **빠른 개발이 중요하면** → PaaS
- **인프라 관리 인력이 부족하면** → PaaS
- **프로토타입이나 MVP 개발이면** → PaaS
- **세밀한 제어가 필요하면** → IaaS
- **대규모 트래픽에 비용 최적화가 중요하면** → IaaS
- **이벤트 기반 단순 처리면** → FaaS

### 7-2. 실무 팁
- **하이브리드 전략**: 핵심은 IaaS, 부가 기능은 PaaS/FaaS 활용
- **벤더 종속성 최소화**: 컨테이너(Docker) 사용으로 이식성 확보
- **비용 모니터링**: PaaS는 생각보다 비용이 빠르게 증가 가능
- **단계적 마이그레이션**: 초기는 PaaS로 빠르게 시작, 성장 후 IaaS로 전환 검토
- **스케일 전략**: 트래픽이 일정 규모 이상이면 IaaS가 비용 효율적
- **학습 투자**: 초기에는 PaaS로 빠르게 배우고, 점진적으로 IaaS 지식 확장

### 7-3. 실제 사례
- **스타트업 초기**: Heroku로 빠르게 출시 → 성장 후 AWS EC2로 전환
- **마이크로서비스**: API Gateway (FaaS) + 핵심 서비스 (PaaS) + 데이터베이스 (IaaS)
- **이벤트 처리**: Lambda (FaaS) + S3 (IaaS) 조합
- **엔터프라이즈**: 온프레미스 (자체 인프라) + 클라우드 (하이브리드 PaaS) 혼용

---

## 8. 대표적인 PaaS 제공 업체

### 8-1. 범용 PaaS
- **Heroku**: 개발자 친화적, Ruby on Rails 커뮤니티에서 시작
- **Google App Engine**: 구글 인프라, 강력한 자동 스케일링
- **AWS Elastic Beanstalk**: AWS 생태계 통합
- **Azure App Service**: Microsoft 기술 스택 최적화
- **Render**: 최근 부상, Heroku 대안으로 주목

### 8-2. 특화된 PaaS
- **Vercel**: Next.js, 프론트엔드 배포에 최적화
- **Netlify**: JAMstack, 정적 사이트 호스팅
- **Railway**: 간단한 설정, 빠른 배포
- **Fly.io**: 엣지 컴퓨팅, 전 세계 분산 배포
- **DigitalOcean App Platform**: 간결한 인터페이스, 합리적 가격

### 8-3. 컨테이너 기반 PaaS
- **Google Cloud Run**: 컨테이너를 서버리스로 실행
- **AWS Fargate**: 서버리스 컨테이너
- **Azure Container Apps**: 마이크로서비스 중심
