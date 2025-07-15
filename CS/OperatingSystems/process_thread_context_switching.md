# 프로세스와 스레드의 컨텍스트 스위칭 (Process and Thread Context Switching)

프로세스와 스레드는 운영체제에서 실행의 기본 단위로, 컨텍스트 스위칭은 CPU가 하나의 실행 단위에서 다른 실행 단위로 전환하는 과정입니다. 이 문서에서는 프로세스와 스레드의 컨텍스트 스위칭 차이점과 스레드의 컨텍스트 스위칭이 더 빠른 이유에 대해 설명합니다.

## 1. 컨텍스트 스위칭이란? (What is Context Switching?)

### 설명
컨텍스트 스위칭은 CPU가 현재 실행 중인 프로세스나 스레드를 중단하고 다른 프로세스나 스레드로 전환하는 과정입니다. 이 과정에서 현재 실행 중인 프로세스/스레드의 상태(컨텍스트)를 저장하고, 다음에 실행할 프로세스/스레드의 상태를 복원합니다.

### 컨텍스트 스위칭이 발생하는 상황
- **시분할(Time Sharing)**: CPU 시간을 여러 프로세스에 공평하게 분배하기 위해
- **우선순위 기반 스케줄링**: 더 높은 우선순위의 프로세스/스레드가 실행 준비될 때
- **I/O 작업**: 프로세스/스레드가 I/O 작업을 기다리는 동안 CPU를 다른 작업에 할당
- **동기화 이벤트**: 세마포어, 뮤텍스 등의 동기화 객체를 기다릴 때
- **인터럽트 처리**: 하드웨어 인터럽트가 발생했을 때

### 컨텍스트 스위칭 과정
1. **현재 컨텍스트 저장**: 레지스터 값, 프로그램 카운터, 스택 포인터 등을 PCB(Process Control Block) 또는 TCB(Thread Control Block)에 저장
2. **다음 실행 단위 선택**: 스케줄러가 다음에 실행할 프로세스/스레드 결정
3. **새 컨텍스트 복원**: 선택된 프로세스/스레드의 PCB/TCB에서 컨텍스트 정보를 CPU 레지스터로 복원
4. **실행 재개**: 새로운 프로세스/스레드의 실행 시작 또는 재개

## 2. 프로세스 vs 스레드 (Process vs Thread)

### 프로세스 (Process)
프로세스는 실행 중인 프로그램의 인스턴스로, 독립적인 메모리 공간과 시스템 자원을 할당받습니다.

#### 프로세스의 주요 특징
- **독립적인 메모리 공간**: 각 프로세스는 자체 주소 공간을 가짐
- **자원 소유**: 파일 핸들, 소켓 등의 시스템 자원을 직접 소유
- **보호**: 다른 프로세스의 메모리에 직접 접근할 수 없음
- **통신 방법**: IPC(Inter-Process Communication) 메커니즘을 통해 통신

#### 프로세스 컨텍스트 구성 요소
- **코드 세그먼트**: 실행 코드
- **데이터 세그먼트**: 전역 변수와 정적 변수
- **힙**: 동적으로 할당된 메모리
- **스택**: 함수 호출 정보, 지역 변수
- **레지스터 값**: CPU 레지스터 상태
- **프로그램 카운터**: 다음 실행할 명령어 위치
- **파일 디스크립터**: 열린 파일 목록
- **신호 처리기**: 프로세스에 등록된 신호 핸들러
- **사용자 ID, 그룹 ID**: 프로세스 소유자 정보
- **기타 커널 자원**: 세마포어, 공유 메모리 등

### 스레드 (Thread)
스레드는 프로세스 내에서 실행되는 더 작은 실행 단위로, 같은 프로세스 내의 다른 스레드와 메모리 공간을 공유합니다.

#### 스레드의 주요 특징
- **공유 메모리**: 같은 프로세스 내 스레드들은 코드, 데이터, 힙 영역을 공유
- **독립적인 스택**: 각 스레드는 자체 스택을 가짐
- **경량 구조**: 프로세스보다 생성 및 종료 비용이 적음
- **직접 통신**: 공유 메모리를 통해 직접 통신 가능

#### 스레드 컨텍스트 구성 요소
- **스레드 ID**: 스레드 식별자
- **레지스터 값**: CPU 레지스터 상태
- **스택 포인터**: 스레드의 스택 위치
- **프로그램 카운터**: 다음 실행할 명령어 위치
- **스레드 로컬 스토리지**: 스레드별 데이터 저장소

## 3. 컨텍스트 스위칭 비교: 프로세스 vs 스레드

### 프로세스 컨텍스트 스위칭
프로세스 간 컨텍스트 스위칭은 다음과 같은 단계를 포함합니다:

1. 현재 프로세스의 모든 상태 정보(레지스터, 메모리 맵, 파일 디스크립터 등)를 PCB에 저장
2. 메모리 관리 정보 업데이트 (페이지 테이블, TLB 플러시 등)
3. 다음 프로세스의 PCB에서 상태 정보 복원
4. 새 프로세스의 주소 공간으로 전환 (MMU 재구성)

### 스레드 컨텍스트 스위칭
같은 프로세스 내 스레드 간 컨텍스트 스위칭은 다음과 같은 단계를 포함합니다:

1. 현재 스레드의 레지스터 값과 스택 포인터를 TCB에 저장
2. 다음 스레드의 TCB에서 레지스터 값과 스택 포인터 복원
3. 스레드 로컬 스토리지 포인터 업데이트

## 4. 스레드 컨텍스트 스위칭이 더 빠른 이유

### 1. 메모리 관리 오버헤드 감소
- **주소 공간 유지**: 스레드 스위칭에서는 같은 프로세스 내에서 이루어지므로 메모리 주소 공간을 변경할 필요가 없음
- **TLB 플러시 불필요**: 프로세스 스위칭에서는 Translation Lookaside Buffer(TLB)를 플러시해야 하지만, 스레드 스위칭에서는 필요 없음
- **페이지 테이블 전환 불필요**: 같은 프로세스 내 스레드는 동일한 페이지 테이블을 사용하므로 전환 비용이 없음

### 2. 캐시 효율성
- **캐시 무효화 감소**: 프로세스 스위칭은 캐시 내용을 크게 변경시키지만, 스레드는 같은 코드와 데이터를 공유하므로 캐시 적중률이 더 높음
- **캐시 웜업 시간 단축**: 스레드 간에는 공유 데이터가 이미 캐시에 있을 가능성이 높아 캐시 웜업 시간이 단축됨

### 3. 저장/복원할 컨텍스트 양의 차이
- **적은 컨텍스트 정보**: 스레드는 프로세스보다 저장/복원해야 할 컨텍스트 정보가 적음
- **시스템 자원 정보 불필요**: 파일 디스크립터, 신호 핸들러 등의 시스템 자원 정보는 프로세스 내에서 공유되므로 스레드 스위칭 시 처리할 필요 없음

### 4. 커널 모드 전환 감소
- **사용자 수준 스레드**: 일부 스레드 구현(사용자 수준 스레드)은 커널 개입 없이 사용자 공간에서 스위칭이 가능하여 시스템 콜 오버헤드가 없음
- **커널 자원 접근 감소**: 스레드 스위칭은 커널 자원에 대한 접근이 적어 모드 전환 횟수가 감소

## 5. 성능 차이 시각화

### 컨텍스트 스위칭 비용 비교
```
프로세스 컨텍스트 스위칭 (약 1-10μs):
+-------------------+----------------------+-------------------+----------------------+
| 현재 프로세스     | 메모리 관리 정보     | 다음 프로세스     | 주소 공간 전환      |
| 상태 저장         | 업데이트             | 상태 복원         | (MMU 재구성)        |
+-------------------+----------------------+-------------------+----------------------+

스레드 컨텍스트 스위칭 (약 0.1-1μs):
+-------------------+-------------------+
| 현재 스레드       | 다음 스레드       |
| 상태 저장         | 상태 복원         |
+-------------------+-------------------+
```

### 저장/복원되는 컨텍스트 정보 비교
```
프로세스 컨텍스트:
+------------------+
| 메모리 맵        |
+------------------+
| 파일 디스크립터  |
+------------------+
| 신호 핸들러      |
+------------------+
| 페이지 테이블    |
+------------------+
| CPU 레지스터     |
+------------------+
| 프로그램 카운터  |
+------------------+
| 기타 커널 자원   |
+------------------+

스레드 컨텍스트:
+------------------+
| CPU 레지스터     |
+------------------+
| 프로그램 카운터  |
+------------------+
| 스택 포인터      |
+------------------+
| 스레드 로컬 저장소|
+------------------+
```

## 6. 실제 성능 차이 예시

### 측정 예시 (Linux 시스템 기준)
일반적인 x86-64 시스템에서의 대략적인 컨텍스트 스위칭 비용:

- **프로세스 컨텍스트 스위칭**: 약 1-10 마이크로초
- **스레드 컨텍스트 스위칭**: 약 0.1-1 마이크로초

### 코드 예시 (C 언어)
```c
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <sys/wait.h>

#define NUM_SWITCHES 10000
#define NUM_PROCESSES 2
#define NUM_THREADS 2

// 프로세스 간 컨텍스트 스위칭 측정
void measure_process_context_switch() {
    int pipes[2][2];
    pid_t pid;
    char buf[1];
    struct timeval start, end;
    
    // 파이프 생성
    pipe(pipes[0]);
    pipe(pipes[1]);
    
    gettimeofday(&start, NULL);
    
    if ((pid = fork()) == 0) {
        // 자식 프로세스
        close(pipes[0][1]);
        close(pipes[1][0]);
        
        for (int i = 0; i < NUM_SWITCHES; i++) {
            read(pipes[0][0], buf, 1);
            write(pipes[1][1], "a", 1);
        }
        
        exit(0);
    } else {
        // 부모 프로세스
        close(pipes[0][0]);
        close(pipes[1][1]);
        
        for (int i = 0; i < NUM_SWITCHES; i++) {
            write(pipes[0][1], "a", 1);
            read(pipes[1][0], buf, 1);
        }
        
        wait(NULL);
    }
    
    gettimeofday(&end, NULL);
    
    long seconds = end.tv_sec - start.tv_sec;
    long microseconds = end.tv_usec - start.tv_usec;
    double elapsed = seconds + microseconds/1000000.0;
    
    printf("프로세스 컨텍스트 스위칭 %d회: %.6f초 (평균: %.9f초/회)\n", 
           NUM_SWITCHES, elapsed, elapsed/NUM_SWITCHES);
}

// 스레드 데이터 구조
typedef struct {
    pthread_mutex_t mutex;
    pthread_cond_t cond[2];
    int turn;
    int count;
} thread_data_t;

// 스레드 함수
void* thread_func(void* arg) {
    thread_data_t* data = (thread_data_t*)arg;
    int id = 1;
    
    pthread_mutex_lock(&data->mutex);
    
    while (data->count < NUM_SWITCHES) {
        while (data->turn != id && data->count < NUM_SWITCHES)
            pthread_cond_wait(&data->cond[id], &data->mutex);
        
        if (data->count >= NUM_SWITCHES)
            break;
        
        data->turn = 1 - id;
        data->count++;
        
        pthread_cond_signal(&data->cond[1-id]);
    }
    
    pthread_mutex_unlock(&data->mutex);
    return NULL;
}

// 스레드 간 컨텍스트 스위칭 측정
void measure_thread_context_switch() {
    pthread_t thread;
    thread_data_t data;
    struct timeval start, end;
    
    // 초기화
    pthread_mutex_init(&data.mutex, NULL);
    pthread_cond_init(&data.cond[0], NULL);
    pthread_cond_init(&data.cond[1], NULL);
    data.turn = 0;
    data.count = 0;
    
    gettimeofday(&start, NULL);
    
    pthread_create(&thread, NULL, thread_func, &data);
    
    pthread_mutex_lock(&data.mutex);
    
    while (data.count < NUM_SWITCHES) {
        while (data.turn != 0 && data.count < NUM_SWITCHES)
            pthread_cond_wait(&data.cond[0], &data.mutex);
        
        if (data.count >= NUM_SWITCHES)
            break;
        
        data.turn = 1;
        data.count++;
        
        pthread_cond_signal(&data.cond[1]);
    }
    
    pthread_mutex_unlock(&data.mutex);
    
    pthread_join(thread, NULL);
    
    gettimeofday(&end, NULL);
    
    long seconds = end.tv_sec - start.tv_sec;
    long microseconds = end.tv_usec - start.tv_usec;
    double elapsed = seconds + microseconds/1000000.0;
    
    printf("스레드 컨텍스트 스위칭 %d회: %.6f초 (평균: %.9f초/회)\n", 
           NUM_SWITCHES, elapsed, elapsed/NUM_SWITCHES);
    
    // 정리
    pthread_mutex_destroy(&data.mutex);
    pthread_cond_destroy(&data.cond[0]);
    pthread_cond_destroy(&data.cond[1]);
}

int main() {
    printf("컨텍스트 스위칭 성능 비교 테스트\n");
    printf("-----------------------------------\n");
    
    measure_process_context_switch();
    measure_thread_context_switch();
    
    return 0;
}
```

### 실행 결과 예시
```
컨텍스트 스위칭 성능 비교 테스트
-----------------------------------
프로세스 컨텍스트 스위칭 10000회: 0.052631초 (평균: 0.000005263초/회)
스레드 컨텍스트 스위칭 10000회: 0.006842초 (평균: 0.000000684초/회)
```

위 결과에서 볼 수 있듯이, 스레드 컨텍스트 스위칭이 프로세스 컨텍스트 스위칭보다 약 7.7배 빠른 것을 확인할 수 있습니다. 실제 시스템과 워크로드에 따라 이 차이는 더 크거나 작을 수 있습니다.

## 7. 결론

스레드의 컨텍스트 스위칭이 프로세스의 컨텍스트 스위칭보다 빠른 주된 이유는 다음과 같습니다:

1. **메모리 주소 공간 공유**: 스레드는 같은 프로세스 내에서 메모리 주소 공간을 공유하므로, 주소 변환 테이블(MMU)을 재설정할 필요가 없습니다.
2. **캐시 효율성**: 스레드는 같은 코드와 데이터를 사용하므로 캐시 적중률이 높고, 캐시 무효화가 적게 발생합니다.
3. **저장/복원할 컨텍스트 정보 감소**: 스레드는 프로세스보다 저장하고 복원해야 할 상태 정보가 적습니다.
4. **시스템 자원 공유**: 파일 디스크립터, 신호 핸들러 등의 시스템 자원 정보는 프로세스 내에서 공유되므로 스레드 스위칭 시 처리할 필요가 없습니다.

이러한 이유로 멀티스레딩은 높은 동시성이 필요하지만 컨텍스트 스위칭 오버헤드를 최소화해야 하는 애플리케이션에서 자주 사용됩니다. 그러나 스레드는 메모리 공유로 인한 동기화 문제와 데이터 경쟁 조건을 주의해야 한다는 단점도 있습니다.

운영체제와 하드웨어 설계가 발전함에 따라 컨텍스트 스위칭 비용은 계속 감소하고 있지만, 여전히 고성능 시스템 설계에서 중요한 고려 사항입니다. 특히 많은 수의 프로세스나 스레드를 사용하는 서버 애플리케이션에서는 컨텍스트 스위칭 오버헤드를 최소화하는 것이 전체 시스템 성능에 큰 영향을 미칩니다.