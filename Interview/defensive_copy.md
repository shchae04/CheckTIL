# 방어적 복사(Defensive Copy)

방어적 복사는 외부에 의해 내부 상태가 의도치 않게 변경되는 것을 막기 위해, 객체의 참조를 직접 노출하지 않고 "복사본"을 만들어 주고받는 설계 기법입니다. 특히 불변(immutable) 클래스를 설계할 때, 또는 DTO/Record에 가변 필드가 포함될 때 필수적으로 고려해야 합니다.

## 왜 필요한가?
- 캡슐화 유지: 내부 컬렉션이나 가변 객체(Date, 배열 등)를 그대로 노출하면 외부 코드가 내부 상태를 변경할 수 있습니다.
- 버그 예방: 공유 참조로 인한 사이드 이펙트를 차단하여 디버깅 비용을 줄입니다.
- 스레드 안전성 강화: 멀티스레드 환경에서 객체의 예상치 못한 변경을 방지합니다.

## 언제 적용하나?
- 불변 클래스를 만들 때(특히 필드에 List, Map, 배열, Date, Calendar 등 가변 객체가 있을 때)
- DTO/Record의 구성 요소가 가변일 때
- 라이브러리 경계(퍼블릭 API)에서 입력/출력을 안전하게 만들 때
- 캐시나 공유 객체의 상태를 보호할 때

## 얕은 복사 vs 깊은 복사
- 얕은 복사(shallow copy): 1단계 필드만 복사. 내부에 또 다른 가변 객체가 있으면 여전히 공유될 수 있음.
- 깊은 복사(deep copy): 중첩된 가변 객체까지 재귀적으로 새로운 인스턴스를 만들어 완전히 분리.
- 실무에서는 비용과 필요성을 고려해 선택. 대개 컬렉션 컨테이너만 새로 만들고, 요소는 불변이라면 얕은 복사로 충분.

---

## 자바 코드 예시

### 1) 생성자에서 방어적 복사 (가변 필드 입력값 보관 시)
```java
import java.util.Date;

public final class Event {
    private final String name;
    private final Date when; // java.util.Date는 가변 객체

    public Event(String name, Date when) {
        this.name = name;
        // 입력값을 그대로 보관하지 않고 복사본을 만든다.
        this.when = new Date(when.getTime());
    }

    public String name() { return name; }

    public Date when() {
        // 내부 상태를 그대로 노출하지 않고 복사본을 반환한다.
        return new Date(when.getTime());
    }
}
```

### 2) 컬렉션 필드에 대한 방어적 복사
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Team {
    private final List<String> members;

    public Team(List<String> members) {
        // 새로운 리스트로 감싸 내부 리스트를 보호
        this.members = new ArrayList<>(members); // 얕은 복사
    }

    // 방법 A: 복사본을 반환
    public List<String> getMembersCopy() {
        return new ArrayList<>(members);
    }

    // 방법 B: 수정 불가 뷰를 반환 (외부에서 add/remove 시도 시 예외 발생)
    public List<String> getMembersUnmodifiable() {
        return Collections.unmodifiableList(members);
    }
}
```

주의: `unmodifiableList`는 "읽기 전용 뷰"를 제공하지만, 내부 리스트가 다른 경로로 변경되면 뷰에도 반영됩니다. 외부 변경 경로를 완전히 차단하려면 생성자에서 방어적 복사를 꼭 수행해야 합니다.

### 3) 배열 필드에 대한 방어적 복사
```java
import java.util.Arrays;

public final class Payload {
    private final byte[] data;

    public Payload(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
```

### 4) Record에서의 방어적 복사 (가변 구성요소 포함 시)
Record는 불변적 구조를 권장하지만, 구성 요소가 가변 타입일 수 있습니다. 이 경우 compact constructor에서 복사하세요.

```java
import java.util.ArrayList;
import java.util.List;

public record Order(String id, List<String> items) {
    public Order {
        // null 체크 및 방어적 복사
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (items == null) items = List.of();
        else items = new ArrayList<>(items);
    }

    // 필요하다면 접근자에서 추가 방어
    @Override public List<String> items() {
        return List.copyOf(items); // 불변 복사본 반환 (Java 10+)
    }
}
```

### 5) 잘못된 예와 문제점 데모
```java
import java.util.ArrayList;
import java.util.List;

class BadTeam {
    private final List<String> members;
    BadTeam(List<String> members) { this.members = members; }
    public List<String> members() { return members; } // 내부 참조 그대로 노출 (문제)
}

public class Demo {
    public static void main(String[] args) {
        List<String> base = new ArrayList<>();
        base.add("A");
        BadTeam team = new BadTeam(base);

        // 외부에서 참조를 통해 내부 상태 변경
        team.members().add("HACKED");
        System.out.println(team.members()); // [A, HACKED]
    }
}
```

---

## 대안과 고려사항
- 불변 타입 사용: 가능하다면 `java.time` 패키지(Instant/LocalDateTime 등), 불변 DTO, 불변 요소 사용.
- 읽기 전용 뷰: `Collections.unmodifiableXxx`, `List.copyOf`, `Set.copyOf` 등으로 반환. (생성자 방어와 함께 사용 권장)
- 비용 고려: 대용량 컬렉션/빈번한 호출에서는 복사 비용이 부담될 수 있으므로 경계(API)에서만 복사하거나, 스냅샷 전략/카피-온-라이트(copy-on-write) 구조 고려.

## 베스트 프랙티스 체크리스트
- 생성자/팩토리에서 가변 인자 수신 시 복사한다.
- 게터에서 가변 상태를 그대로 노출하지 않는다(복사본 또는 읽기 전용 뷰).
- Record에서도 가변 컴포넌트는 compact constructor에서 복사한다.
- equals/hashCode에 사용되는 가변 상태는 특히 주의한다.
- 복사가 비싼 경우 문서화하고 호출 경계를 조정한다.

## 참고
- Effective Java 3/E, Item 50: Defensive Copies
- Java SE API: Collections.unmodifiableList, List.copyOf, Arrays.copyOf