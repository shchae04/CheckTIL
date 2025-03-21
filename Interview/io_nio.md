# IO 방식과 NIO의 차이점

Java에서 입출력 작업을 수행하는 방식에는 전통적인 IO와 New IO (NIO)가 있습니다. 두 방식은 여러 측면에서 차이가 있습니다.

## 1. 기본 개념
- **IO (Input/Output)**
    - **스트림 기반**: 데이터를 바이트나 문자 단위의 스트림으로 처리합니다.
    - **블로킹 I/O**: 읽기나 쓰기 작업 시, 해당 작업이 완료될 때까지 쓰레드가 대기합니다.

- **NIO (New IO)**
    - **버퍼와 채널 기반**: 데이터를 버퍼에 저장하고, 채널을 통해 입출력을 수행합니다.
    - **논블로킹 I/O 지원**: 데이터를 요청한 후, 완료되지 않아도 다른 작업을 수행할 수 있어 효율적입니다.
    - **선택자(Selector)**: 하나의 쓰레드가 다수의 채널을 감시하여 이벤트 기반 처리를 할 수 있습니다.

## 2. 처리 방식 및 성능
- **블로킹 vs 논블로킹**
    - **IO**: 각 입출력 작업이 완료될 때까지 기다리므로, 동시 접속이나 대규모 시스템에서는 성능 저하가 발생할 수 있습니다.
    - **NIO**: 논블로킹 모드를 사용하여 쓰레드가 다른 작업을 수행할 수 있어, 고성능 및 대규모 네트워크 애플리케이션에 유리합니다.

- **데이터 처리 방식**
    - **IO**: 스트림을 통해 한 번에 한 바이트씩 혹은 정해진 크기의 블록 단위로 처리합니다.
    - **NIO**: 버퍼를 사용해 데이터를 읽고 쓰며, 필요에 따라 데이터를 메모리맵 파일로 처리할 수 있습니다.

## 3. 활용 사례 및 개발 난이도
- **IO 방식**
    - 소규모 애플리케이션이나 단순 파일 입출력 작업에 적합합니다.
    - 구현이 상대적으로 단순하고 이해하기 쉽습니다.

- **NIO 방식**
    - 고성능 서버, 대용량 데이터 처리, 다수의 동시 접속 처리가 필요한 경우에 유리합니다.
    - 선택자와 채널, 버퍼 등 개념이 추가되어 학습 곡선이 높습니다.

## 결론
- **IO**는 단순한 입출력 작업에 적합하며, 사용법이 간단합니다.
- **NIO**는 비동기 I/O 및 멀티플렉싱 기능을 제공하여, 대규모 및 고성능 애플리케이션 개발에 유리합니다.

각 방식은 상황에 맞게 선택하여 사용하며, 애플리케이션의 요구사항에 따라 적절한 방식을 도입하는 것이 중요합니다.
