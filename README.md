# TaskFlow Backend

> 확장 가능한 아키텍처와 운영 효율성을 중심으로 설계된 태스크 관리 시스템

TaskFlow는 Spring Boot 기반의 RESTful API 서버로, JWT 인증, AOP 기반 로깅, 실시간 대시보드 등의 기능을 제공합니다.

---

## 목차

1. [개발환경](#개발환경)
2. [빠른 시작](#빠른-시작)
3. [주요 기능](#주요-기능)
4. [아키텍처](#아키텍처)
5. [기술적 선택 근거](#기술적-선택-근거)
6. [트러블 슈팅](#트러블-슈팅)
7. [팀 협업](#팀-협업)
8. [회고](#회고)

---

## 개발환경

| 분류 | 기술 스택 |
|------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.0, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.0 |
| **Authentication** | JWT (JSON Web Token) |
| **Testing** | JUnit 5, Mockito |
| **Build Tool** | Gradle |

---

## 빠른 시작

### 1. 데이터베이스 설정

```sql
CREATE DATABASE taskFlow;
```

### 2. 환경변수 설정

`src/main/resources/properties/env.properties` 파일을 생성하고 다음과 같이 설정합니다:

```properties
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/taskFlow
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET_KEY=your_jwt_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=1209600000
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 프론트엔드 연동 (옵션)

```bash
# Docker를 이용한 프론트엔드 실행
docker pull dannyseo4284/taskflow-frontend:latest
docker run -d -p 3000:3000 dannyseo4284/taskflow-frontend:latest
```

---

## 주요 기능

### 인증/인가 시스템
- **JWT 기반 인증**: Access Token(15분) + Refresh Token(14일)
- **토큰 블랙리스트**: 로그아웃된 토큰 재사용 방지
- **로그인 시도 제한**: 5회 실패 시 15분간 계정 차단
- **Spring Security**: 커스텀 인증 필터 체인

### 태스크 관리
- **상태 기반 워크플로우**: TODO → IN_PROGRESS → DONE
- **검색 및 필터링**: 제목/내용 기반 검색, 상태별 필터
- **Soft Delete**: 데이터 무결성 보장
- **우선순위 관리**: LOW, MEDIUM, HIGH

### 실시간 대시보드
- **주간 통계**: 작업 현황 및 증가율
- **오늘의 할 일**: 마감 임박 작업 5개
- **작업 추이**: 주간/월간 트렌드 분석
- **진행률 비교**: 개인 vs 팀 완료율

### 활동 로그
- **AOP 기반 자동 로깅**: 주요 사용자 행동 기록
- **비동기 이벤트 처리**: 메인 로직에 영향 없는 로그 저장
- **필터링 조회**: 사용자/타입/날짜별 로그 검색

---

## 아키텍처

### ERD 구조

![ERD](ERD.png)

## API 엔드포인트

**총 28개 엔드포인트**

```
API Count Summary
==========================
  - AuthController.java
    ├── POST APIs:        5
  - CommentController.java
    ├── GET APIs:        1
    ├── POST APIs:        1
    ├── PUT APIs:        1
    └── DELETE APIs:        1
  - UserController.java
    ├── GET APIs:        2
  - DashboardController.java
    ├── GET APIs:        8
  - TaskController.java
    ├── GET APIs:        4
    ├── POST APIs:        1
    ├── PATCH APIs:        1
    ├── PUT APIs:        1
    └── DELETE APIs:        1
  - ActivityLogController.java
    ├── GET APIs:        1
==========================
API Statistics
==========================
- GET APIs: 16
- POST APIs: 7
- PATCH APIs: 1
- PUT APIs: 2
- DELETE APIs: 2
- Total APIs: 28
==========================
```
---

## 기술적 선택 근거

### AOP + 이벤트 기반 로깅
**선택 이유**
- 특정 메서드에만 로깅 적용 가능 (세밀한 제어)
- 비즈니스 로직과 로깅 로직 완전 분리
- 비동기 처리로 성능 영향 최소화

**구현 방식**
```java
@ActivityLog(type = ActivityType.TASK_CREATED, target = TargetType.TASK)
@PostMapping("/tasks")
public ResponseEntity<TaskCreateResponseDto> createTask() {
    // 비즈니스 로직
}
```
---
### JWT 토큰 블랙리스트 (MySQL 사용)

**Redis 대신 MySQL을 선택한 이유**

| 고려사항 | MySQL | Redis |
|----------|-------|-------|
| **인프라 복잡도** | ✅ 기존 DB 활용 | ❌ 별도 인스턴스 필요 |
| **데이터 영속성** | ✅ 재시작 시에도 유지 | ❌ 메모리 기반 |
| **트랜잭션 일관성** | ✅ 사용자 탈퇴와 함께 처리 | ❌ 별도 처리 필요 |
| **팀 규모 적합성** | ✅ 충분한 성능 | ⚠️ 과도한 스펙 |
---
### 이벤트 기반 아키텍처

**Auth ↔ User 도메인 분리**

```java
// 이벤트 발행
@Transactional
public void signup(SignupRequest request) {
    Auth auth = authRepository.save(newAuth);
    eventPublisher.publishEvent(new UserRegisteredEvent(auth.getId(), auth.getName()));
}

// 이벤트 처리
@EventListener
@Async
public void handleUserRegistered(UserRegisteredEvent event) {
    User user = new User(event.getUserId(), event.getName());
    userRepository.save(user);
}
```

**장점**
- 도메인 간 느슨한 결합
- 장애 격리 효과
- 확장성 확보
---
### Native Query + Projection (대시보드)

**선택 이유**
- DB에서 집계 처리 → 네트워크 부하 최소화
- 인터페이스 Projection → 메모리 사용량 절약
- 복잡한 날짜 연산을 DB에 위임

```java
@Query(value = """
    SELECT DATE(t.created_at) AS date,
           COUNT(*) AS totalCount,
           SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END) AS doneCount
    FROM tasks t
    WHERE t.created_at BETWEEN :start AND :end
    GROUP BY DATE(t.created_at)
    """, nativeQuery = true)
List<DailyTaskTrendProjection> fetchDailyTrend(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
```

---

## 트러블 슈팅

### 문제 1: 글로벌 예외처리의 한계

**문제 상황**
```java
// 기존: 모든 도메인 예외가 한 곳에 집중
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class) // Auth 도메인
    @ExceptionHandler(TaskException.class) // Task 도메인
    @ExceptionHandler(UserException.class) // User 도메인
    // ... 계속 증가
}
```

**해결 방법**
```java
// 개선: 도메인별로 예외 처리 분리
@RestControllerAdvice(basePackages = "com.example.domain.auth")
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
        // Auth 도메인 전용 예외 처리
    }
}
```

**결과**
- 코드 응집도 향상
- 도메인별 예외 처리 로직 관리 용이
- DDD 원칙 준수
---
### 문제 2: DDL과 JPA 엔티티 불일치

**문제 상황**
- 팀원별로 다른 DDL 버전 사용
- 컬럼명, 데이터 타입 불일치로 인한 SQLException 빈발

**해결 과정**
1. **즉시 공유 체계 구축**: DDL 변경 시 팀 채팅방 즉시 공지
2. **체크리스트 도입**: 모든 팀원 확인 완료 후 다음 작업 진행
3. **로컬 환경 통일**: 변경사항 적용 후 애플리케이션 재시작

**향후 개선 계획**
- Flyway/Liquibase 도입으로 마이그레이션 자동화
- CI/CD 파이프라인에서 스키마 검증 추가

---

## 팀 협업

### 개발 문화
---
**체계적인 일정 관리**
- 매일 TODO 작성 및 공유
- 진행상황 실시간 체크
---
**지식 공유 문화**
- 학습 내용 블로그 작성 및 공유
- 주요 기술 결정사항 문서화

**팀원 학습 블로그 공유**

- **김나경**
  - [Spring 로그 레벨(Log Level)](https://gajicoding.tistory.com/380)
  - [전략 패턴(Strategy Pattern)](https://gajicoding.tistory.com/381)
  - [템플릿 메서드 패턴(Template Method Pattern)](https://gajicoding.tistory.com/382)

- **차준호**
  - [Docker의 동작원리](https://juno0112.tistory.com/84)
  - [데이터베이스 인덱스의 구조와 특징](https://juno0112.tistory.com/86)
  - [가상화 기술: 하드웨어 가상화 vs OS 가상화 vs 컨테이너화](https://juno0112.tistory.com/87)

- **김신영**
  - [save() vs saveAll() vs bulk insert](https://velog.io/@eggtart21/save-vs-saveAll-vs-bulk-insert)
  - [MySQL 인덱스 성능 비교 실험 (100만 건 기준)](https://velog.io/@eggtart21/%E3%84%B4%E3%85%87%E3%84%B9%E3%85%8E%E3%84%B4%E3%85%87%E3%85%8E)

- **박민철**
  - [GET Method에 RequestBody 요청 적합한지에 대해](https://syuare.tistory.com/67)
  - [HTTP 201 CREATED 의 응답 헤더에 포함되어야 하는 것](https://syuare.tistory.com/68)
 
- **박현우**
  - [지연로딩(Lazy)과 즉시로딩(Eager)](https://thisisyou.tistory.com/23)
---
**일관성 있는 개발 규칙**

```
📝 개발 프로세스
├── Commit Convention: feat, fix, docs, refactor
├── Git Flow: main → develop → feature/*
├── PR/Issue Template: 작업 명확성 확보
└── 코드 리뷰: 최소 2명 승인 후 머지
```

**코드 리뷰 세부 규칙**
- **승인 조건**: Pull Request는 최소 2명 이상의 승인 필요
- **리뷰 기준**: 기능 완성도, 가독성, 일관성, 예외 처리 여부 검토
- **의견 조율**: 리뷰어 간 의견 충돌 시 팀 논의를 통한 합의 후 반영
---
### Git 브랜치 전략

```
main (운영)
├── develop (개발)
    ├── feature/auth-system
    ├── feature/task-crud
    └── feature/dashboard-stats
```

**커밋 컨벤션**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `refactor`: 코드 리팩토링

---

## 회고

### 성과

**기술적 성취**
- AOP 기반 로깅 시스템 구축
- JWT 토큰 보안 체계 완성
- 이벤트 기반 아키텍처 도입
- 실시간 대시보드 통계 시스템

**팀워크**
- 체계적인 코드 리뷰 문화 정착
- 지식 공유를 통한 팀 역량 향상
- 일관된 개발 프로세스 수립

### 개선이 필요한 부분

**기술적 한계**
- 테스트 커버리지 부족 (30% 달성했으나 더 높은 수준 필요)
- API 문서화 미비 (Swagger 미적용)
- 수동 스키마 관리의 비효율성

**아키텍처 한계**
- 단일 서버 구조로 인한 확장성 제약
- 이벤트 기반 처리의 일시적 데이터 불일치
- 애그리거트 패턴 부재

### 향후 계획

**단기 개선**
- [ ] Flyway를 통한 DB 마이그레이션 자동화
- [ ] Swagger/OpenAPI 적용으로 API 문서화
- [ ] 테스트 커버리지 70% 이상 달성

**중장기 개선**
- [ ] 마이크로서비스 아키텍처 전환 검토
- [ ] Redis 캐시 도입으로 성능 최적화
- [ ] 모니터링 시스템 구축 (Prometheus + Grafana)

---
