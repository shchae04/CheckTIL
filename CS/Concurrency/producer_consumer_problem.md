# 생산자-소비자 문제 (Producer-Consumer Problem)

생산자-소비자 문제는 멀티스레드 프로그래밍에서 가장 대표적인 동기화 문제 중 하나입니다. 
## 1. 생산자-소비자 문제란?

### 개념
생산자-소비자 문제는 여러 스레드가 공유 자원에 접근할 때 발생하는 동기화 문제를 설명하는 고전적인 예제입니다. 이 문제에서는 두 종류의 프로세스(또는 스레드)가 존재합니다:

1. **생산자(Producer)**: 데이터를 생성하여 공유 버퍼(또는 큐)에 넣는 역할
2. **소비자(Consumer)**: 공유 버퍼에서 데이터를 꺼내 처리하는 역할

### 문제 상황
생산자-소비자 문제에서 다음과 같은 상황이 발생할 수 있습니다:

1. **버퍼가 가득 찼을 때** 생산자가 더 이상 데이터를 넣을 수 없음
2. **버퍼가 비어있을 때** 소비자가 데이터를 꺼낼 수 없음
3. **여러 생산자나 소비자가 동시에** 버퍼에 접근할 때 데이터 무결성 문제 발생

## 2. 멀티스레드 환경에서의 문제점

멀티스레드 환경에서 생산자-소비자 패턴을 구현할 때 다음과 같은 문제가 발생할 수 있습니다:

### 경쟁 상태(Race Condition)
여러 스레드가 공유 자원(버퍼)에 동시에 접근하여 데이터를 변경할 때 발생합니다. 이로 인해 데이터 불일치나 예상치 못한 결과가 발생할 수 있습니다.

### 교착 상태(Deadlock)
생산자와 소비자가 서로를 기다리는 상황에서 발생합니다. 예를 들어, 생산자는 버퍼가 비워지기를 기다리고, 소비자는 버퍼에 데이터가 채워지기를 기다리는 상황에서 둘 다 진행되지 않는 상태입니다.

### 기아 상태(Starvation)
특정 스레드가 필요한 자원을 계속해서 얻지 못하는 상황입니다. 예를 들어, 생산자가 너무 빠르게 데이터를 생성하여 소비자가 처리할 기회를 얻지 못하는 경우입니다.

## 3. 해결 방법

### 3.1 synchronized와 wait/notify 사용

Java에서는 `synchronized` 키워드와 `wait()`, `notify()`, `notifyAll()` 메소드를 사용하여 생산자-소비자 문제를 해결할 수 있습니다.

```java
/**
 * synchronized와 wait/notify를 사용한 생산자-소비자 문제 해결
 */
public class ProducerConsumerExample {
    private static final int BUFFER_SIZE = 5;
    private final Queue<Integer> buffer = new LinkedList<>();
    private final Object lock = new Object();  // 동기화를 위한 락 객체

    /**
     * 생산자 클래스
     */
    class Producer implements Runnable {
        @Override
        public void run() {
            int value = 0;
            while (true) {
                try {
                    Thread.sleep(1000);  // 생산 시간 시뮬레이션

                    synchronized (lock) {
                        // 버퍼가 가득 찼으면 대기
                        while (buffer.size() == BUFFER_SIZE) {
                            System.out.println("버퍼가 가득 찼습니다. 생산자 대기 중...");
                            lock.wait();
                        }

                        // 데이터 생산 및 버퍼에 추가
                        value++;
                        buffer.add(value);
                        System.out.println("생산: " + value + ", 버퍼 크기: " + buffer.size());

                        // 소비자에게 알림
                        lock.notifyAll();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 소비자 클래스
     */
    class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);  // 소비 시간 시뮬레이션

                    synchronized (lock) {
                        // 버퍼가 비어있으면 대기
                        while (buffer.isEmpty()) {
                            System.out.println("버퍼가 비어있습니다. 소비자 대기 중...");
                            lock.wait();
                        }

                        // 버퍼에서 데이터 꺼내기
                        int value = buffer.poll();
                        System.out.println("소비: " + value + ", 버퍼 크기: " + buffer.size());

                        // 생산자에게 알림
                        lock.notifyAll();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 생산자-소비자 예제 실행 메소드
     */
    public void start() {
        Thread producerThread = new Thread(new Producer());
        Thread consumerThread = new Thread(new Consumer());

        producerThread.start();
        consumerThread.start();
    }

    /**
     * 메인 메소드
     */
    public static void main(String[] args) {
        ProducerConsumerExample example = new ProducerConsumerExample();
        example.start();
    }
}
```

### 3.2 BlockingQueue 사용

Java의 `BlockingQueue` 인터페이스는 생산자-소비자 패턴을 구현하기 위한 스레드 안전한 큐를 제공합니다.

```java
/**
 * BlockingQueue를 사용한 생산자-소비자 문제 해결
 */
public class BlockingQueueExample {
    private static final int BUFFER_SIZE = 5;
    private final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(BUFFER_SIZE);

    /**
     * 생산자 클래스
     */
    class Producer implements Runnable {
        @Override
        public void run() {
            int value = 0;
            while (true) {
                try {
                    Thread.sleep(1000);  // 생산 시간 시뮬레이션

                    // 데이터 생산 및 큐에 추가 (큐가 가득 차면 자동으로 대기)
                    value++;
                    queue.put(value);
                    System.out.println("생산: " + value + ", 큐 크기: " + queue.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 소비자 클래스
     */
    class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);  // 소비 시간 시뮬레이션

                    // 큐에서 데이터 꺼내기 (큐가 비어있으면 자동으로 대기)
                    int value = queue.take();
                    System.out.println("소비: " + value + ", 큐 크기: " + queue.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 생산자-소비자 예제 실행 메소드
     */
    public void start() {
        Thread producerThread = new Thread(new Producer());
        Thread consumerThread = new Thread(new Consumer());

        producerThread.start();
        consumerThread.start();
    }

    /**
     * 메인 메소드
     */
    public static void main(String[] args) {
        BlockingQueueExample example = new BlockingQueueExample();
        example.start();
    }
}
```

### 3.3 세마포어(Semaphore) 사용

세마포어는 공유 자원에 대한 접근을 제어하는 데 사용되는 동기화 도구입니다.

```java
/**
 * 세마포어를 사용한 생산자-소비자 문제 해결
 */
public class SemaphoreExample {
    private static final int BUFFER_SIZE = 5;
    private final Queue<Integer> buffer = new LinkedList<>();

    // 세마포어 정의
    private final Semaphore mutex = new Semaphore(1);  // 상호 배제를 위한 세마포어
    private final Semaphore empty = new Semaphore(BUFFER_SIZE);  // 빈 공간 수를 나타내는 세마포어
    private final Semaphore full = new Semaphore(0);  // 채워진 공간 수를 나타내는 세마포어

    /**
     * 생산자 클래스
     */
    class Producer implements Runnable {
        @Override
        public void run() {
            int value = 0;
            while (true) {
                try {
                    Thread.sleep(1000);  // 생산 시간 시뮬레이션

                    // 빈 공간이 있을 때까지 대기
                    empty.acquire();
                    // 버퍼에 대한 독점 접근 획득
                    mutex.acquire();

                    // 데이터 생산 및 버퍼에 추가
                    value++;
                    buffer.add(value);
                    System.out.println("생산: " + value + ", 버퍼 크기: " + buffer.size());

                    // 버퍼에 대한 독점 접근 해제
                    mutex.release();
                    // 채워진 공간 세마포어 증가
                    full.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 소비자 클래스
     */
    class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);  // 소비 시간 시뮬레이션

                    // 채워진 공간이 있을 때까지 대기
                    full.acquire();
                    // 버퍼에 대한 독점 접근 획득
                    mutex.acquire();

                    // 버퍼에서 데이터 꺼내기
                    int value = buffer.poll();
                    System.out.println("소비: " + value + ", 버퍼 크기: " + buffer.size());

                    // 버퍼에 대한 독점 접근 해제
                    mutex.release();
                    // 빈 공간 세마포어 증가
                    empty.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 생산자-소비자 예제 실행 메소드
     */
    public void start() {
        Thread producerThread = new Thread(new Producer());
        Thread consumerThread = new Thread(new Consumer());

        producerThread.start();
        consumerThread.start();
    }

    /**
     * 메인 메소드
     */
    public static void main(String[] args) {
        SemaphoreExample example = new SemaphoreExample();
        example.start();
    }
}
```

## 4. 각 해결 방법의 비교

| 해결 방법 | 장점 | 단점 |
|---------|------|------|
| synchronized와 wait/notify | - 기본 Java 기능만 사용<br>- 세밀한 제어 가능 | - 구현이 복잡할 수 있음<br>- 실수하기 쉬움 |
| BlockingQueue | - 간결한 코드<br>- 스레드 안전성 보장<br>- 다양한 구현체 제공 | - 세밀한 제어가 어려울 수 있음 |
| 세마포어(Semaphore) | - 정교한 동기화 제어 가능<br>- 고전적인 해결책으로 널리 알려짐 | - 구현이 복잡함<br>- 세마포어 사용 실수 시 교착상태 발생 가능 |

## 5. 결론

생산자-소비자 문제는 멀티스레드 프로그래밍에서 발생하는 동기화 문제를 이해할 수 있습니다. 이 문제를 해결하기 위한 여러 방법이 있으며, 각각의 방법은 고유한 장단점을 가지고 있습니다.

실제 애플리케이션에서는 상황에 따라 적절한 방법을 선택해야 합니다:
- 간단한 구현이 필요하면 `BlockingQueue`를 사용
- 세밀한 제어가 필요하면 `synchronized`와 `wait/notify` 또는 세마포어를 사용
- 성능이 중요하면 적절한 크기의 버퍼와 효율적인 동기화 메커니즘을 선택

멀티스레드 환경에서의 동기화 문제는 항상 신중하게 접근해야 하며, 교착 상태나 기아 상태와 같은 문제를 방지하기 위한 설계가 중요합니다.
