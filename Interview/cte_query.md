# CTE 쿼리 (Common Table Expressions)

## 1. CTE란?

CTE(Common Table Expression)는 SQL 쿼리 내에서 이름이 부여된 임시 결과 집합으로, 복잡한 쿼리를 더 읽기 쉽고 유지보수하기 쉽게 만들어주는 SQL 기능입니다. CTE는 `WITH` 절을 사용하여 정의되며, 해당 쿼리 실행 중에만 존재합니다.

기본 구문:
```sql
WITH cte_name (column_names) AS (
    -- CTE 정의 쿼리
)
-- CTE를 사용하는 메인 쿼리
SELECT * FROM cte_name;
```

## 2. CTE의 장점

1. **가독성 향상**: 복잡한 쿼리를 논리적인 블록으로 분리하여 코드의 가독성을 높입니다.

2. **유지보수성 개선**: 반복되는 서브쿼리를 한 번만 정의하고 여러 번 참조할 수 있어 유지보수가 용이합니다.

3. **재귀 쿼리 지원**: 재귀 CTE를 사용하면 계층적 데이터(조직도, 파일 시스템 등)를 쉽게 처리할 수 있습니다.

4. **모듈화**: 복잡한 쿼리를 작은 논리적 단위로 나눌 수 있어 쿼리 개발 및 디버깅이 쉬워집니다.

5. **성능 최적화**: 일부 데이터베이스 시스템에서는 CTE를 사용하여 쿼리 최적화를 수행할 수 있습니다.

## 3. CTE의 단점

1. **데이터베이스 호환성**: 모든 데이터베이스 시스템이 CTE를 지원하지는 않으며, 지원하더라도 구문과 기능에 차이가 있을 수 있습니다.

2. **성능 이슈**: 일부 데이터베이스에서는 CTE가 매번 재평가되어 성능 저하가 발생할 수 있습니다.

3. **디버깅 복잡성**: 복잡한 CTE 구조에서는 문제 발생 시 디버깅이 어려울 수 있습니다.

4. **최적화 제한**: 일부 데이터베이스 시스템에서는 CTE를 사용할 때 쿼리 최적화 기능이 제한될 수 있습니다.

## 4. CTE 사용 예시

### 4.1 기본 CTE 예시

```sql
-- 부서별 평균 급여가 전체 평균 급여보다 높은 부서 찾기
WITH DeptAvgSalary AS (
    SELECT department_id, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY department_id
),
CompanyAvgSalary AS (
    SELECT AVG(salary) AS company_avg
    FROM employees
)
SELECT d.department_id, d.avg_salary
FROM DeptAvgSalary d, CompanyAvgSalary c
WHERE d.avg_salary > c.company_avg
ORDER BY d.avg_salary DESC;
```

### 4.2 재귀 CTE 예시

```sql
-- 조직 계층 구조 조회
WITH RECURSIVE EmployeeHierarchy AS (
    -- 기준점(앵커 멤버)
    SELECT employee_id, name, manager_id, 1 AS level
    FROM employees
    WHERE manager_id IS NULL
    
    UNION ALL
    
    -- 재귀 멤버
    SELECT e.employee_id, e.name, e.manager_id, eh.level + 1
    FROM employees e
    JOIN EmployeeHierarchy eh ON e.manager_id = eh.employee_id
)
SELECT employee_id, name, level
FROM EmployeeHierarchy
ORDER BY level, employee_id;
```

### 4.3 복잡한 집계 예시

```sql
-- 월별, 제품별 매출 및 누적 매출 계산
WITH MonthlySales AS (
    SELECT 
        EXTRACT(YEAR FROM order_date) AS year,
        EXTRACT(MONTH FROM order_date) AS month,
        product_id,
        SUM(quantity * price) AS monthly_sales
    FROM orders
    GROUP BY 
        EXTRACT(YEAR FROM order_date),
        EXTRACT(MONTH FROM order_date),
        product_id
)
SELECT 
    year, 
    month, 
    product_id, 
    monthly_sales,
    SUM(monthly_sales) OVER (
        PARTITION BY product_id 
        ORDER BY year, month
        ROWS UNBOUNDED PRECEDING
    ) AS cumulative_sales
FROM MonthlySales
ORDER BY product_id, year, month;
```

## 5. 데이터베이스별 CTE 지원

- **PostgreSQL**: 버전 8.4 이상에서 완전히 지원
- **MySQL**: 버전 8.0 이상에서 지원
- **SQL Server**: 버전 2005 이상에서 지원
- **Oracle**: 버전 11g R2 이상에서 지원 (WITH 절 사용)
- **SQLite**: 버전 3.8.3 이상에서 지원

## 6. 결론

CTE는 복잡한 SQL 쿼리를 더 읽기 쉽고 유지보수하기 쉽게 만들어주는 강력한 도구입니다. 특히 재귀 쿼리나 여러 단계의 데이터 처리가 필요한 경우에 유용합니다. 그러나 데이터베이스 시스템 간의 호환성 문제와 일부 성능 고려사항을 염두에 두어야 합니다. 적절히 사용하면 SQL 코드의 품질과 가독성을 크게 향상시킬 수 있습니다.