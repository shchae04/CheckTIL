# Amazon DynamoDB

Amazon DynamoDB는 AWS에서 제공하는 완전 관리형 NoSQL 데이터베이스 서비스입니다. 이 문서에서는 DynamoDB의 주요 개념, 특징, 그리고 사용 방법에 대해 설명합니다.

## 1. DynamoDB란?

DynamoDB는 어떤 규모에서도 일관된 한 자릿수 밀리초 응답 시간을 제공하는 키-값 및 문서 데이터베이스입니다. 완전 관리형 서비스로, 서버 프로비저닝, 하드웨어 또는 소프트웨어 설치 및 구성, 복제, 소프트웨어 패치 또는 클러스터 확장과 같은 데이터베이스 관리 작업이 필요하지 않습니다.

### 1.1 주요 특징

- **완전 관리형**: 인프라 관리 필요 없음
- **서버리스**: 용량 계획 없이 자동 확장
- **고성능**: 밀리초 단위의 응답 시간
- **고가용성**: 여러 AWS 가용 영역에 자동 복제
- **내구성**: 데이터 내구성 보장
- **유연한 데이터 모델**: 스키마 없는 설계
- **보안**: IAM을 통한 세분화된 액세스 제어
- **글로벌 테이블**: 다중 리전 복제
- **백업 및 복원**: 온디맨드 백업 및 특정 시점 복구

## 2. DynamoDB 핵심 개념

### 2.1 데이터 모델

DynamoDB는 테이블, 항목(items) 및 속성(attributes)으로 구성됩니다:

- **테이블**: 데이터 항목의 모음
- **항목(Item)**: 테이블의 각 레코드 (관계형 DB의 행과 유사)
- **속성(Attribute)**: 항목의 기본 데이터 요소 (관계형 DB의 열과 유사)

DynamoDB는 스키마가 없어 각 항목마다 다른 속성을 가질 수 있습니다.

### 2.2 기본 키(Primary Key)

DynamoDB 테이블은 다음 두 가지 유형의 기본 키를 지원합니다:

1. **파티션 키(Partition Key)**: 단일 속성으로 구성된 기본 키
   - 내부적으로 해시 함수를 사용하여 데이터 분산
   - 테이블 내에서 고유해야 함

2. **복합 키(Composite Key)**: 파티션 키와 정렬 키(Sort Key)로 구성
   - 파티션 키가 같은 항목들은 정렬 키 값에 따라 정렬됨
   - 동일한 파티션 키를 가진 여러 항목 허용

### 2.3 보조 인덱스(Secondary Indexes)

기본 키 외에 다른 속성으로 데이터를 쿼리할 수 있게 해주는 인덱스:

1. **글로벌 보조 인덱스(GSI)**: 파티션 키와 정렬 키가 테이블의 기본 키와 다를 수 있음
   - 테이블당 최대 20개까지 생성 가능

2. **로컬 보조 인덱스(LSI)**: 테이블과 동일한 파티션 키를 사용하지만 다른 정렬 키 사용
   - 테이블 생성 시에만 정의 가능
   - 테이블당 최대 5개까지 생성 가능

## 3. 용량 모드

DynamoDB는 두 가지 용량 모드를 제공합니다:

### 3.1 프로비저닝된 용량 모드(Provisioned Capacity)

- 초당 읽기 및 쓰기 용량 단위를 미리 지정
- 예측 가능한 워크로드에 적합
- 자동 확장(Auto Scaling) 설정 가능
- 프로비저닝된 용량을 초과하면 제한(Throttling) 발생

### 3.2 온디맨드 용량 모드(On-Demand)

- 용량 계획 없이 요청당 지불
- 트래픽이 예측 불가능한 워크로드에 적합
- 자동으로 확장 및 축소
- 프로비저닝된 모드보다 비용이 높을 수 있음

## 4. 데이터 일관성 모델

DynamoDB는 두 가지 일관성 모델을 제공합니다:

### 4.1 최종 일관된 읽기(Eventually Consistent Reads)

- 기본 읽기 모드
- 최근 완료된 쓰기 작업의 결과가 즉시 반영되지 않을 수 있음
- 비용이 저렴하고 읽기 처리량이 높음

### 4.2 강력한 일관된 읽기(Strongly Consistent Reads)

- 가장 최근에 완료된 쓰기 작업의 결과를 반영
- 읽기 요청 시 `ConsistentRead` 파라미터를 `true`로 설정
- 최종 일관된 읽기보다 지연 시간이 길고 비용이 높음

## 5. DynamoDB 작업

### 5.1 기본 CRUD 작업

**항목 생성/업데이트 (PutItem)**:
```javascript
var params = {
  TableName: 'Users',
  Item: {
    'UserId': { S: 'user123' },
    'Name': { S: '홍길동' },
    'Email': { S: 'hong@example.com' },
    'Age': { N: '30' }
  }
};

dynamodb.putItem(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Item added successfully");
});
```

**항목 조회 (GetItem)**:
```javascript
var params = {
  TableName: 'Users',
  Key: {
    'UserId': { S: 'user123' }
  }
};

dynamodb.getItem(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Item retrieved:", data.Item);
});
```

**항목 업데이트 (UpdateItem)**:
```javascript
var params = {
  TableName: 'Users',
  Key: {
    'UserId': { S: 'user123' }
  },
  UpdateExpression: 'SET Age = :newage',
  ExpressionAttributeValues: {
    ':newage': { N: '31' }
  }
};

dynamodb.updateItem(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Item updated successfully");
});
```

**항목 삭제 (DeleteItem)**:
```javascript
var params = {
  TableName: 'Users',
  Key: {
    'UserId': { S: 'user123' }
  }
};

dynamodb.deleteItem(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Item deleted successfully");
});
```

### 5.2 쿼리 및 스캔

**쿼리(Query)**: 파티션 키 값을 기준으로 항목 검색
```javascript
var params = {
  TableName: 'Orders',
  KeyConditionExpression: 'CustomerId = :cid',
  ExpressionAttributeValues: {
    ':cid': { S: 'customer1' }
  }
};

dynamodb.query(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Query results:", data.Items);
});
```

**스캔(Scan)**: 테이블의 모든 항목 검색
```javascript
var params = {
  TableName: 'Products',
  FilterExpression: 'Price > :p',
  ExpressionAttributeValues: {
    ':p': { N: '100' }
  }
};

dynamodb.scan(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Scan results:", data.Items);
});
```

## 6. 고급 기능

### 6.1 트랜잭션

DynamoDB는 여러 테이블에 걸친 ACID 트랜잭션을 지원합니다:

```javascript
var params = {
  TransactItems: [
    {
      Put: {
        TableName: 'Orders',
        Item: {
          'OrderId': { S: 'order1' },
          'CustomerId': { S: 'customer1' },
          'Status': { S: 'PENDING' }
        }
      }
    },
    {
      Update: {
        TableName: 'Customers',
        Key: { 'CustomerId': { S: 'customer1' } },
        UpdateExpression: 'SET OrderCount = OrderCount + :inc',
        ExpressionAttributeValues: { ':inc': { N: '1' } }
      }
    }
  ]
};

dynamodb.transactWriteItems(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Transaction successful");
});
```

### 6.2 TTL(Time to Live)

항목의 만료 시간을 설정하여 자동으로 삭제되게 할 수 있습니다:

```javascript
// TTL 속성 설정
var params = {
  TableName: 'Sessions',
  Item: {
    'SessionId': { S: 'session123' },
    'UserId': { S: 'user123' },
    'Data': { S: '세션 데이터' },
    'ExpirationTime': { N: '1609459200' } // Unix 타임스탬프
  }
};

dynamodb.putItem(params, function(err, data) {
  if (err) console.log(err);
  else console.log("Session with TTL added");
});
```

### 6.3 스트림(Streams)

DynamoDB 스트림은 테이블의 데이터 수정 이벤트를 시간 순서대로 캡처합니다:

- 테이블의 변경 사항을 실시간으로 추적
- Lambda 함수와 통합하여 이벤트 기반 처리 가능
- 데이터 복제, 알림, 감사 등에 활용

## 7. DynamoDB 모범 사례

### 7.1 데이터 모델링 모범 사례

- **액세스 패턴 중심 설계**: 쿼리 패턴을 먼저 정의하고 그에 맞게 설계
- **데이터 비정규화**: 조인 대신 데이터 중복을 허용하여 쿼리 성능 향상
- **복합 키 활용**: 계층적 데이터 구조에 파티션 키와 정렬 키 조합 사용
- **GSI 오버로딩**: 하나의 GSI를 여러 쿼리 패턴에 활용
- **희소 인덱스**: 특정 조건의 항목만 인덱싱하여 효율성 향상

### 7.2 성능 최적화

- **파티션 키 선택**: 고유 값 분포가 좋은 속성 선택
- **배치 작업 사용**: BatchGetItem, BatchWriteItem으로 처리량 향상
- **페이지 매김 활용**: 대량 데이터 처리 시 페이지 매김 사용
- **프로젝션 최소화**: 필요한 속성만 요청하여 데이터 전송량 감소
- **적절한 용량 모드 선택**: 워크로드 패턴에 맞는 용량 모드 선택

### 7.3 비용 최적화

- **예약 용량 구매**: 장기 사용 시 예약 용량으로 비용 절감
- **자동 확장 설정**: 트래픽에 따라 자동으로 용량 조정
- **TTL 활용**: 불필요한 데이터 자동 삭제로 스토리지 비용 절감
- **압축 사용**: 큰 속성 값은 압축하여 저장
- **DAX 활용**: DynamoDB Accelerator로 읽기 비용 절감

## 8. DynamoDB와 다른 데이터베이스 비교

### 8.1 DynamoDB vs MongoDB

| 특성 | DynamoDB | MongoDB |
|------|----------|---------|
| 유형 | 관리형 서비스 | 자체 관리 또는 Atlas(관리형) |
| 데이터 모델 | 키-값 및 문서 | 문서 중심 |
| 쿼리 언어 | 제한된 쿼리 기능 | 풍부한 쿼리 언어 |
| 확장성 | 자동 확장 | 수동 또는 자동(Atlas) |
| 트랜잭션 | 지원 | 지원 |
| 인덱싱 | 제한적(GSI, LSI) | 다양한 인덱스 유형 |
| 가격 책정 | 사용량 기반 | 인스턴스 기반 또는 사용량 기반(Atlas) |

### 8.2 DynamoDB vs RDBMS

| 특성 | DynamoDB | RDBMS |
|------|----------|-------|
| 데이터 모델 | 비관계형, 스키마 없음 | 관계형, 고정 스키마 |
| 확장성 | 수평적 확장 | 주로 수직적 확장 |
| 트랜잭션 | 제한적 지원 | 완전한 ACID 지원 |
| 조인 | 지원하지 않음 | 복잡한 조인 지원 |
| 쿼리 유연성 | 제한적 | SQL의 풍부한 표현력 |
| 일관성 | 최종 또는 강력한 일관성 | 강력한 일관성 |

## 9. 사용 사례

DynamoDB는 다음과 같은 사용 사례에 적합합니다:

- **모바일 및 웹 애플리케이션**: 낮은 지연 시간, 높은 확장성 요구
- **게임 애플리케이션**: 사용자 세션, 점수, 게임 상태 관리
- **IoT 데이터 저장**: 센서 데이터, 디바이스 상태 정보
- **마이크로서비스 백엔드**: 확장 가능한 데이터 저장소
- **세션 관리**: 웹 및 모바일 앱의 사용자 세션
- **실시간 분석**: 스트림을 통한 실시간 데이터 처리
- **콘텐츠 관리**: 메타데이터, 사용자 프로필, 설정

## 10. 결론

Amazon DynamoDB는 확장성, 성능, 가용성이 중요한 애플리케이션에 적합한 완전 관리형 NoSQL 데이터베이스 서비스입니다. 서버리스 아키텍처, 자동 확장, 글로벌 테이블과 같은 기능을 통해 개발자는 인프라 관리보다 애플리케이션 개발에 집중할 수 있습니다.

그러나 복잡한 쿼리, 조인, 트랜잭션이 많은 애플리케이션에는 관계형 데이터베이스가 더 적합할 수 있습니다. 데이터베이스 선택 시 애플리케이션의 요구사항, 액세스 패턴, 확장성 요구 사항을 고려하는 것이 중요합니다.

## 참고 자료

- [Amazon DynamoDB 공식 문서](https://docs.aws.amazon.com/dynamodb/)
- [DynamoDB 개발자 안내서](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/)
- [AWS SDK for JavaScript](https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/DynamoDB.html)
- [DynamoDB 모범 사례](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)
- [The DynamoDB Book](https://www.dynamodbbook.com/)
- [AWS re:Invent DynamoDB 세션](https://aws.amazon.com/dynamodb/resources/)