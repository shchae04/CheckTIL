# 톰캣(Tomcat)의 역할

톰캣(Apache Tomcat)은 자바 서블릿(Servlet)과 JSP(JavaServer Pages) 기술을 지원하는 오픈 소스 웹 애플리케이션 서버(WAS: Web Application Server)입니다. 이 문서에서는 톰캣의 주요 역할과 기능에 대해 알아보겠습니다.

## 1. 톰캣의 기본 개념

### 1.1. 웹 서버 vs 웹 애플리케이션 서버

- **웹 서버(Web Server)**: 정적 콘텐츠(HTML, CSS, 이미지 등)를 제공하는 서버 (예: Apache HTTP Server, Nginx)
- **웹 애플리케이션 서버(WAS)**: 동적 콘텐츠를 생성하고 제공하는 서버로, 웹 서버의 기능을 포함하면서 추가적인 기능을 제공

톰캣은 기본적으로 WAS이지만, 단독으로 웹 서버 역할도 수행할 수 있습니다.

## 2. 톰캣의 주요 역할

### 2.1. 서블릿 컨테이너(Servlet Container)

톰캣의 가장 핵심적인 역할은 서블릿 컨테이너로서의 기능입니다.

```java
// 간단한 서블릿 예제
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>안녕하세요, 톰캣 서블릿입니다!</h1>");
        out.println("</body></html>");
    }
}
```

서블릿 컨테이너로서 톰캣은:
- 서블릿의 생명주기(초기화, 요청 처리, 소멸) 관리
- HTTP 요청을 받아 적절한 서블릿에 전달
- 서블릿에서 생성된 응답을 클라이언트에게 반환
- 멀티스레딩 지원으로 동시 요청 처리
- 서블릿 인스턴스 풀링 및 관리

### 2.2. JSP 엔진

JSP(JavaServer Pages)는 HTML 내에 Java 코드를 삽입할 수 있는 기술입니다. 톰캣은 JSP 엔진을 내장하고 있어 JSP 파일을 서블릿으로 변환하고 실행합니다.

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>JSP 예제</title>
</head>
<body>
    <h1>현재 시간: <%= new java.util.Date() %></h1>
    <%
        for(int i=1; i<=5; i++) {
            out.println("<p>" + i + "번째 줄입니다.</p>");
        }
    %>
</body>
</html>
```

### 2.3. 웹 서버 기능

톰캣은 기본적인 웹 서버 기능을 제공합니다:
- HTTP/HTTPS 프로토콜 지원
- 정적 콘텐츠 제공
- 가상 호스트 지원
- 보안 기능 (SSL/TLS, 인증 등)

### 2.4. 웹 애플리케이션 관리

- WAR(Web Application Archive) 파일 배포 및 관리
- 웹 애플리케이션의 시작, 중지, 재시작 기능
- 클래스 로딩 관리
- JNDI(Java Naming and Directory Interface) 지원

## 3. 톰캣의 아키텍처

### 3.1. 주요 구성 요소

- **Catalina**: 서블릿 컨테이너 구현체
- **Coyote**: HTTP 커넥터 (HTTP 요청/응답 처리)
- **Jasper**: JSP 엔진
- **Cluster**: 클러스터링 기능
- **High Availability**: 고가용성 기능
- **Tomcat Manager**: 웹 애플리케이션 관리 도구

### 3.2. 설정 구조

톰캣의 주요 설정 파일:
- `server.xml`: 톰캣 서버의 주요 설정
- `web.xml`: 웹 애플리케이션 설정
- `context.xml`: 컨텍스트 설정
- `tomcat-users.xml`: 사용자 인증 정보

```xml
<!-- server.xml 예시 -->
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

## 4. 톰캣의 활용

### 4.1. 스프링 프레임워크와의 통합

스프링 부트는 내장 톰캣을 사용하여 별도의 설치 없이 웹 애플리케이션을 실행할 수 있습니다.

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4.2. 마이크로서비스 아키텍처에서의 활용

경량화된 톰캣은 컨테이너화(Docker)와 마이크로서비스 아키텍처에 적합합니다.

### 4.3. 성능 최적화

```xml
<!-- 커넥션 풀 설정 예시 -->
<Resource name="jdbc/myDB" auth="Container"
          type="javax.sql.DataSource"
          maxTotal="100" maxIdle="30"
          maxWaitMillis="10000"
          username="user" password="password"
          driverClassName="com.mysql.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/mydb"/>
```

## 5. 톰캣 vs 다른 WAS

### 5.1. 톰캣 vs 제티(Jetty)
- 톰캣: 더 많은 기능, 더 넓은 커뮤니티
- 제티: 더 가볍고 임베디드 사용에 적합

### 5.2. 톰캣 vs JBoss/WildFly
- 톰캣: 서블릿 컨테이너에 중점
- JBoss/WildFly: 완전한 Java EE 스택 제공

### 5.3. 톰캣 vs WebLogic/WebSphere
- 톰캣: 오픈 소스, 가벼움
- WebLogic/WebSphere: 상용 제품, 엔터프라이즈 기능 강화

## 6. 결론

톰캣은 자바 웹 애플리케이션을 위한 가장 인기 있는 서블릿 컨테이너로, 경량화된 설계와 풍부한 기능을 제공합니다. 단순한 웹 애플리케이션부터 복잡한 엔터프라이즈 시스템까지 다양한 환경에서 활용되고 있으며, 특히 스프링 부트와의 통합으로 더욱 널리 사용되고 있습니다.

## 참고 자료
- [Apache Tomcat 공식 웹사이트](https://tomcat.apache.org/)
- [Java Servlet 스펙](https://javaee.github.io/servlet-spec/)
- [JSP 스펙](https://javaee.github.io/javaee-spec/javadocs/)