# 두 개의 스택으로 큐 구현하기 (Interview/TIL)

면접 상황: 3년 차 백엔드 개발자. 질문: "두 개의 스택만 사용해서 큐를 어떻게 구현하시겠어요? 시간 복잡도까지 설명해주세요."

## 0) 면접 답변 스크립트 (3년 차 버전)
- 30초 버전
  - 두 스택(in/out)으로 구현합니다. offer는 in에 push만 하고, poll/peek 시 out이 비었을 때만 in 전체를 out으로 한번 옮겨서 FIFO를 만듭니다. 각 원소는 최대 한 번만 이동하므로 상각 O(1)이고, 단일 poll의 최악은 O(n)일 수 있습니다. Java에선 Stack 대신 ArrayDeque를 Deque로 쓰는 게 더 빠르고 안전합니다.
- 90초 버전
  - 선택: Stack 클래스 대신 ArrayDeque< E > 두 개를 씁니다(스레드-세이프 필요 없고 null 금지라는 제약이 명확함). offer는 in.push로 O(1). poll/peek는 out이 비었을 때만 in→out으로 이동해 순서를 뒤집습니다.
  - 복잡도: n개의 연산 동안 각 요소는 in→out 이동을 최대 1회만 겪으니 전체 O(n) → 연산당 상각 O(1). 최악 단일 연산은 O(n)입니다.
  - 운영 고려: 동시성 필요하면 메서드 단위 synchronized 또는 ReentrantLock으로 임계영역 보호, 혹은 ConcurrentLinkedQueue 같은 라이브러리 대안 검토. 용량 제한이 있으면 size 체크 후 거절/블로킹 정책 정의. 빈 큐 정책은 예외/nullable 중 사전에 합의합니다.
  - 트레이드오프: 원형 버퍼 기반 큐는 예측 가능한 O(1) 최악 시간과 더 나은 캐시 친화성이 있지만, 동적 성장/축소는 두 스택 방식이 간결합니다.

---

## 1) 한 줄 요약 (TL;DR)
- 입력 스택(in)과 출력 스택(out) 두 개를 사용한다.
- enqueue(offer)는 입력 스택에 push.
- dequeue(poll)/peek는 출력 스택이 비었을 때 입력 스택의 모든 원소를 옮긴 뒤, 출력 스택에서 pop/peek.
- 각 연산의 상각 시간 복잡도는 O(1).

코드 참조: CS/Algorithms/TwoStackQueue.java

---

## 2) 아이디어와 핵심 원리
- 스택은 LIFO, 큐는 FIFO. 스택 두 개를 이용하면 순서를 한 번 뒤집고 다시 뒤집어 FIFO를 얻을 수 있다.
- inStack: 새로 들어오는 요소를 쌓아두는 스택.
- outStack: 실제로 꺼내는(앞단) 스택.
- outStack이 비었을 때만 inStack의 모든 원소를 하나씩 pop해서 outStack에 push한다. 그러면 가장 먼저 들어온 원소가 outStack의 top이 된다.

---

## 3) Java 구현 (ArrayDeque 사용)
- ArrayDeque는 null 요소를 허용하지 않음에 유의.

```java
package CS.Algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * A Queue implementation using two stacks with amortized O(1) operations.
 */
public class TwoStackQueue<E> {
    private final Deque<E> inStack = new ArrayDeque<>();
    private final Deque<E> outStack = new ArrayDeque<>();

    // Enqueue: 뒤에 붙이기
    public void offer(E e) {
        inStack.push(e);
    }

    // Peek: 맨 앞 조회 (제거하지 않음)
    public E peek() {
        moveIfNeeded();
        if (outStack.isEmpty()) throw new NoSuchElementException("Queue is empty");
        return outStack.peek();
    }

    // Dequeue: 맨 앞 제거하고 반환
    public E poll() {
        moveIfNeeded();
        if (outStack.isEmpty()) throw new NoSuchElementException("Queue is empty");
        return outStack.pop();
    }

    public boolean isEmpty() {
        return inStack.isEmpty() && outStack.isEmpty();
    }

    public int size() {
        return inStack.size() + outStack.size();
    }

    public void clear() {
        inStack.clear();
        outStack.clear();
    }

    private void moveIfNeeded() {
        if (outStack.isEmpty()) {
            while (!inStack.isEmpty()) {
                outStack.push(inStack.pop());
            }
        }
    }
}
```

---

## 4) 복잡도 분석 (상각 O(1) 증명 직관)
- offer: 항상 inStack.push만 하므로 O(1).
- poll/peek: outStack이 비어있을 때만 inStack의 모든 요소를 한 번씩 이동. 각 요소는 "in -> out"으로 최대 한 번만 이동한다.
- n번의 offer와 n번의 poll 전체 작업에서, 각 요소는 push/pop이 총 O(1)번씩만 수행된다. 총 비용 O(n) → 연산당 평균 O(1) (상각 O(1)).
- 최악의 단일 poll은 O(n)일 수 있으나, 연속된 연산 관점에서 평균은 O(1).

공간 복잡도: O(n) (두 스택에 담긴 총 요소 수 만큼).

---

## 5) 엣지 케이스와 예외 처리
- 빈 큐에서 peek/poll: NoSuchElementException 던짐 (현재 구현 기준). 필요시 null 반환 형태로 바꿀 수 있음.
- null 삽입: ArrayDeque는 null을 허용하지 않음. null 데이터를 허용하려면 다른 Deque 구현체나 감싸는 래퍼가 필요.
- clear 후 연산: 정상 동작 (두 스택 모두 비우기).

---

## 6) 자주 하는 실수
- outStack이 비어있지 않은데도 매 연산마다 in→out 이동을 수행: 불필요한 O(n) 비용 유발. 반드시 "outStack이 비었을 때만" 옮긴다.
- 예외 처리 누락: 빈 큐에서 poll/peek 호출 시의 정책을 명확히 정하지 않음.
- size 계산 실수: 두 스택의 합을 잊고 한쪽만 세는 경우.

---

## 7) 변형/확장 포인트
- 스레드 세이프: 두 스택(in/out)에 대한 동시 접근을 막기 위해 동기화 필요. 간단히 전체 메서드에 synchronized를 걸거나, ReentrantLock 두 개를 이용해 경합을 줄이는 방식 고려.
- 제한 용량 큐: offer 시 현재 size와 capacity 비교 후 거절/블로킹 정책 정의.
- Iterable 지원: 큐의 순서로 순회하려면 outStack 역순 → inStack 정순을 이어 붙이는 커스텀 이터레이터 구현.
- 최소값/최대값 O(1) 조회: 각 스택에 보조 스택을 두어 min/max를 함께 관리하고, 이동 시 보조 스택도 동기화.

---

## 8) 팔로업 질문 예시
- 상각 시간 복잡도의 개념과, 왜 이 구조가 상각 O(1)인지 설명해주세요.
- 단일 연산의 최악 시간은 왜 O(n)일 수 있나요? 실제 시스템에서의 영향은?
- 동시성 환경에서 race condition은 어디서 발생할 수 있으며, 어떻게 해결하시겠습니까?
- 배열 기반(배열 원형 큐) vs 두 스택 큐의 장단점 비교.
- Java의 ArrayDeque와 Stack/LinkedList 중 어떤 것을 선택했고, 이유는 무엇인가요?

---

## 9) 요약
- inStack에 넣고, 필요할 때만 outStack으로 몰아서 옮겨 FIFO를 만든다.
- 각 원소는 최대 한 번만 이동 → 전체적으로 O(1) 상각 성능.
- 예외/엣지/동시성/변형 포인트까지 준비하면 면접 대응 완성.
