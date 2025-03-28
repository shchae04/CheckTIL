# try-with-resources를 사용해야 하는 이유

### try-catch-finally와 try-with-resources 비교

`try-catch-finally`와 `try-with-resources`는 모두 자바에서 예외 처리와 리소스 관리를 위한 구문입니다. 하지만 `try-with-resources`는 Java 7부터 도입된 기능으로, 리소스 관리에 있어 더 안전하고 간결한 코드를 작성할 수 있게 해줍니다.

### try-catch-finally의 문제점

전통적인 `try-catch-finally` 구문을 사용할 때 발생할 수 있는 문제점들:

1. **리소스 누수 가능성**
   - `finally` 블록에서 리소스를 닫지 않거나, 예외 발생 시 리소스가 제대로 닫히지 않을 수 있습니다.
   ```java
   FileInputStream fis = null;
   try {
       fis = new FileInputStream("file.txt");
       // 파일 처리 로직
   } catch (IOException e) {
       // 예외 처리
   } finally {
       if (fis != null) {
           try {
               fis.close(); // 여기서 또 예외가 발생할 수 있음
           } catch (IOException e) {
               // close() 메서드에서 발생한 예외 처리
           }
       }
   }
   ```

2. **코드 복잡성 증가**
   - 여러 리소스를 관리할 때 코드가 매우 복잡해집니다.
   - 각 리소스마다 null 체크와 close() 호출이 필요합니다.

3. **예외 처리의 어려움**
   - `try` 블록과 `finally` 블록 모두에서 예외가 발생하면, `finally` 블록의 예외가 `try` 블록의 예외를 덮어씁니다.
   - 원래 발생한 예외 정보가 손실될 수 있습니다.

### try-with-resources의 장점

`try-with-resources`를 사용하면 다음과 같은 이점이 있습니다:

1. **자동 리소스 관리**
   - `AutoCloseable` 인터페이스를 구현한 리소스는 try 블록이 종료될 때 자동으로 close() 메서드가 호출됩니다.
   ```java
   try (FileInputStream fis = new FileInputStream("file.txt")) {
       // 파일 처리 로직
   } catch (IOException e) {
       // 예외 처리
   }
   ```

2. **간결한 코드**
   - 리소스 관리 코드가 크게 간소화됩니다.
   - 여러 리소스도 쉽게 관리할 수 있습니다.
   ```java
   try (FileInputStream fis = new FileInputStream("input.txt");
        FileOutputStream fos = new FileOutputStream("output.txt")) {
       // 파일 처리 로직
   } catch (IOException e) {
       // 예외 처리
   }
   ```

3. **예외 처리 개선**
   - 리소스를 닫는 과정에서 발생한 예외는 억제된(suppressed) 예외로 처리됩니다.
   - 원래 발생한 예외 정보가 보존되며, 억제된 예외는 `getSuppressed()` 메서드로 확인할 수 있습니다.

4. **리소스 누수 방지**
   - 예외 발생 여부와 관계없이 리소스가 항상 닫힙니다.
   - 개발자의 실수로 인한 리소스 누수 가능성이 크게 줄어듭니다.

### 실제 사용 예시

**데이터베이스 연결 관리:**

```java
// try-catch-finally 사용
Connection conn = null;
Statement stmt = null;
ResultSet rs = null;
try {
    conn = DriverManager.getConnection(DB_URL, USER, PASS);
    stmt = conn.createStatement();
    rs = stmt.executeQuery("SELECT * FROM users");
    // 결과 처리
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
    try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
    try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}

// try-with-resources 사용
try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
    // 결과 처리
} catch (SQLException e) {
    e.printStackTrace();
}
```

### 결론

`try-with-resources`는 자바에서 리소스 관리를 위한 더 안전하고 간결한 방법을 제공합니다. 이 구문을 사용하면 코드의 가독성이 향상되고, 리소스 누수 가능성이 줄어들며, 예외 처리가 개선됩니다. 따라서 `AutoCloseable` 인터페이스를 구현한 리소스를 다룰 때는 항상 `try-with-resources`를 사용하는 것이 좋습니다.