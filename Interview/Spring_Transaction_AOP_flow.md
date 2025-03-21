스프링 트랜잭션 AOP 동작 흐름

## 1. 개요
스프링의 트랜잭션 관리는 AOP(Aspect-Oriented Programming)를 활용하여 **@Transactional** 애노테이션이 붙은 메서드에 트랜잭션 경계를 설정합니다. 이를 통해 비즈니스 로직과 트랜잭션 관리 로직을 분리하여 코드의 가독성과 유지보수성을 높일 수 있습니다.

## 2. 동작 흐름

1. **프록시 생성**
    - 스프링 컨테이너는 애플리케이션 시작 시 **BeanPostProcessor**를 통해 @Transactional이 적용된 빈에 대해 AOP 프록시를 생성합니다.
    - 생성된 프록시는 실제 빈을 감싸며, 메서드 호출 시 트랜잭션 처리를 위한 추가 로직을 수행합니다.

2. **메서드 호출 가로채기**
    - 클라이언트가 프록시 빈의 메서드를 호출하면, 프록시가 해당 호출을 가로채어 트랜잭션 처리 로직을 실행합니다.

3. **트랜잭션 시작**
    - 프록시는 내부의 **TransactionInterceptor**를 통해 트랜잭션 관리자(예: **PlatformTransactionManager**)에게 트랜잭션을 시작하도록 요청합니다.
    - 이때, 트랜잭션 속성(전파, 격리, 타임아웃 등)이 적용되어 적절한 트랜잭션이 생성됩니다.

4. **타깃 메서드 실행**
    - 트랜잭션이 시작된 후, 프록시는 실제 타깃 객체의 메서드를 호출하여 비즈니스 로직을 수행합니다.

5. **예외 처리 및 트랜잭션 종료**
    - 메서드 실행 중 예외가 발생하면, 트랜잭션 관리자에 의해 해당 트랜잭션은 롤백됩니다.
    - 예외 없이 정상 실행되면, 트랜잭션은 커밋되어 변경 사항이 확정됩니다.

6. **결과 반환**
    - 트랜잭션 처리가 완료되면, 프록시는 최종 결과를 클라이언트에게 반환합니다.

## 3. 핵심 컴포넌트
- **AOP 프록시**: 실제 빈을 감싸며, 메서드 호출 시 트랜잭션 경계 설정과 처리를 담당합니다.
- **TransactionInterceptor**: AOP 어드바이스로, 메서드 실행 전 트랜잭션 시작과 후처리(커밋/롤백) 로직을 수행합니다.
- **PlatformTransactionManager**: 트랜잭션의 생성, 커밋, 롤백 등 실제 트랜잭션 관리를 담당하는 핵심 컴포넌트입니다.

## 4. 트랜잭션 전파 및 격리
- **전파(Propagation)**: 메서드 호출 시 기존 트랜잭션의 존재 여부에 따라 새로운 트랜잭션을 생성할지, 기존 트랜잭션에 참여할지를 결정합니다.
- **격리(Isolation)**: 동시에 실행되는 트랜잭션 간 데이터의 독립성을 보장하여, 데이터 정합성을 유지하는 설정입니다.

## 5. 정리
- 스프링 트랜잭션 AOP는 AOP 프록시를 이용하여 트랜잭션 경계를 명확히 하고, 비즈니스 로직과 트랜잭션 관리 로직을 분리합니다.
- 이를 통해 개발자는 복잡한 트랜잭션 관리 코드를 직접 작성하지 않고, 선언적 트랜잭션 관리를 손쉽게 구현할 수 있습니다.
- 예외 발생 시 자동 롤백 및 정상 실행 시 커밋 처리로, 데이터 무결성을 효과적으로 유지할 수 있습니다.
