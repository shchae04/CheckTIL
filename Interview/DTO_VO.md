# DTO와 VO의 차이가 무엇인가요?

DTO(Data Transfer Object)와 VO(Value Object)는 소프트웨어 설계에서 자주 사용되는 객체 유형으로, 각각의 역할과 특징이 다릅니다.

## DTO (Data Transfer Object)
- **목적:**  
  계층 간 혹은 네트워크를 통해 `데이터를 전달하기 위한 객체`입니다.
- **특징:**
    - **데이터 전송:** 서비스 계층, API, 클라이언트-서버 간 데이터 전달 시 사용됩니다.
    - **단순 구조:** 비즈니스 로직을 포함하지 않고, 단순히 데이터를 담고 전달하는 역할만 수행합니다.
    - **가변성:** 일반적으로 객체의 상태가 변경 가능한 mutable 객체입니다.
- **예시:**  
  사용자 요청/응답 데이터, 폼 데이터 객체 등

## VO (Value Object)
- **목적:**  
  `도메인 모델` 내에서 특정 값을 나타내는 객체로, 그 자체의 값이 의미를 가집니다.
- **특징:**
    - **불변성:** 생성 후 내부 상태가 변경되지 않으며, immutable 객체로 설계됩니다.
    - **동등성:** 객체의 동일성이 아닌, 내부 값의 동일성으로 비교합니다. 즉, 두 VO의 모든 속성이 같으면 동일한 객체로 간주합니다.
    - **비즈니스 로직 포함 가능:** 값 자체에 관련된 간단한 검증이나 연산 로직을 포함할 수 있습니다.
- **예시:**  
  날짜, 주소, 화폐 단위 등과 같이 변경될 필요가 없는 값

## 결론
- **DTO**는 데이터를 전송하기 위해 사용되는 단순한 객체이며, 주로 mutable하고 비즈니스 로직은 포함하지 않습니다.
- **VO**는 도메인 내의 값 표현을 위해 사용되며, 불변성을 유지하고 값 자체의 동일성을 중요시합니다.
