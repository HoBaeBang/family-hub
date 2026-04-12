# Family Hub — 아키텍처 문서 (Architecture Document)

> **버전**: v0.1.0  
> **작성일**: 2026-04-12  
> **기능정의서**: docs/SPEC.md 참고

---

## 1. 아키텍처 철학

### 왜 과도기 아키텍처인가?

> "처음부터 MSA로 시작하면 복잡도가 학습을 압도한다.  
> 처음부터 모놀리스로만 만들면 MSA를 경험할 수 없다.  
> 그래서 핵심 도메인은 Modular Monolith로 단단하게 쌓고,  
> 독립적으로 분리 가능한 영역부터 MSA로 꺼내나간다."

### 핵심 원칙

| 원칙 | 설명 |
|------|------|
| 도메인 경계 우선 | 기술보다 도메인 경계를 먼저 설계한다 |
| 점진적 분리 | 필요성이 증명된 것만 MSA로 분리한다 |
| 이벤트 기반 통신 | 서비스 간 결합을 Kafka 이벤트로 최소화한다 |
| 단일 진입점 | 클라이언트는 Gateway만 바라본다 |
| 관측 가능성 | 분산 시스템은 추적 불가능하면 운영 불가능하다 |

---

## 2. 전체 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         │
│                    Next.js (frontend/)                      │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS
┌───────────────────────────▼─────────────────────────────────┐
│                      API Gateway Layer                      │
│              Spring Cloud Gateway (app-gateway/)            │
│         - JWT 인증 필터                                      │
│         - 경로별 라우팅                                      │
│         - Rate Limiting                                     │
└──────┬──────────────────────────────────────────┬───────────┘
       │                                          │
       ▼                                          ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│   Modular Monolith       │   │     Microservices            │
│   (monolith/)            │   │     (services/)              │
│                          │   │                              │
│  ┌────────────────────┐  │   │  ┌────────────────────────┐  │
│  │     app-api        │  │   │  │  notification-service  │  │
│  │                    │  │   │  │  - SSE 실시간 알림      │  │
│  │  Auth              │  │   │  │  - 이메일 알림          │  │
│  │  Workspace         │  │   │  └────────────────────────┘  │
│  │  Project           │  │   │  ┌────────────────────────┐  │
│  │  Issue             │  │   │  │    search-service      │  │
│  │  Workflow          │  │   │  │  - OpenSearch 색인      │  │
│  │  Sprint            │  │   │  │  - 통합 검색            │  │
│  │  Roadmap           │  │   │  └────────────────────────┘  │
│  └────────────────────┘  │   │  ┌────────────────────────┐  │
│                          │   │  │  investment-service    │  │
│  ┌────────────────────┐  │   │  │  - 시세 polling        │  │
│  │     app-batch      │  │   │  │  - 수익률 계산          │  │
│  │  - 알림 배치       │  │   │  └────────────────────────┘  │
│  └────────────────────┘  │   │  ┌────────────────────────┐  │
│                          │   │  │  calendar-sync-service │  │
└──────────────────────────┘   │  │  - Google Calendar 동기│  │
                               │  └────────────────────────┘  │
                               └──────────────────────────────┘
                                            │
            ┌───────────────────────────────▼────────────────┐
            │               Infrastructure Layer             │
            │                                                │
            │  ┌─────────┐ ┌───────┐ ┌───────┐ ┌─────────┐  │
            │  │  MySQL  │ │ Redis │ │ Kafka │ │  Mongo  │  │
            │  └─────────┘ └───────┘ └───────┘ └─────────┘  │
            │  ┌──────────────────┐ ┌────────────────────┐   │
            │  │   OpenSearch     │ │     AWS S3         │   │
            │  └──────────────────┘ └────────────────────┘   │
            └────────────────────────────────────────────────┘
```

---

## 3. Modular Monolith 내부 구조

### 3.1 모듈 의존성 규칙

```
app-api        → core-domain, core-db, core-mongo, core-redis, core-kafka
app-batch      → core-domain, core-db, core-kafka
core-db        → core-domain
core-mongo     → core-domain
core-redis     → (독립)
core-kafka     → core-domain
core-external  → (독립)
support-logging → (독립)
```

> **규칙**: core-domain은 어디에도 의존하지 않는다 (순수 도메인 로직)

### 3.2 모듈별 책임

#### `core-domain/`
```
- Entity 클래스 (JPA 어노테이션 포함)
- Value Object
- 도메인 서비스
- 도메인 이벤트 정의
- Repository 인터페이스 (Port)
- 커스텀 예외 클래스
```

#### `core-db/`
```
- JPA Repository 구현체
- QueryDSL 쿼리 클래스
- JPA 설정 (DataSource, TransactionManager)
- DB 마이그레이션 (Flyway)
```

#### `core-mongo/`
```
- MongoDB Document 클래스
- MongoRepository 구현체
- MongoDB 설정
```
> **사용처**: 댓글(Comment), 활동 로그(ActivityLog), 알림 이력

#### `core-redis/`
```
- Redis 설정 (RedisTemplate, ConnectionFactory)
- 캐시 유틸 클래스
- Redis Key 상수 관리
```
> **사용처**: JWT Refresh Token, 시세 캐시, 분산 락

#### `core-kafka/`
```
- KafkaProducer 공통 설정
- KafkaConsumer 공통 설정
- 이벤트 DTO 정의 (토픽별)
- 직렬화 설정 (JSON)
```

#### `app-api/`
```
- Controller (REST API)
- Service (애플리케이션 로직)
- Security 설정
- 패키지 구조: 도메인별 패키지 (auth, workspace, project, issue ...)
```

#### `app-batch/`
```
- Spring Batch 또는 @Scheduled 기반
- 알림 배치 발송
- 시세 데이터 정리 배치
```

### 3.3 패키지 구조 (app-api 내부)

```
com.familyhub.api/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── security/
├── workspace/
│   ├── controller/
│   ├── service/
│   └── dto/
├── project/
│   ├── controller/
│   ├── service/
│   └── dto/
├── issue/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── event/          ← Kafka 이벤트 발행
├── workflow/
│   ├── controller/
│   ├── service/
│   └── dto/
├── sprint/
│   ├── controller/
│   ├── service/
│   └── dto/
└── common/
    ├── response/       ← 공통 응답 포맷
    ├── exception/      ← 전역 예외 처리
    └── config/
```

---

## 4. Microservices 구조

### 4.1 각 서비스 독립 원칙

```
✅ 각 서비스는 독립적으로 빌드 / 배포 가능
✅ 자체 DB 또는 저장소를 가짐 (DB per Service)
✅ 다른 서비스의 DB에 직접 접근 금지
✅ 서비스 간 통신은 Kafka(비동기) 또는 REST(동기)만 허용
```

### 4.2 notification-service

```
역할     : Kafka 이벤트 소비 → SSE / 이메일 발송
DB      : MongoDB (알림 이력 저장)
Redis   : 알림 읽음 상태 캐시
의존     : Kafka Consumer, SMTP, Redis

패키지:
com.familyhub.notification/
├── consumer/     ← Kafka Consumer
├── sender/       ← SSE Sender, Email Sender
├── service/
├── repository/   ← MongoDB
└── config/
```

### 4.3 search-service

```
역할     : Kafka 이벤트로 색인 관리, 검색 API 제공
DB      : OpenSearch
의존     : Kafka Consumer, OpenSearch Client

패키지:
com.familyhub.search/
├── consumer/     ← Kafka Consumer (색인 트리거)
├── indexer/      ← OpenSearch 색인 로직
├── controller/   ← 검색 REST API
├── service/
└── config/
```

### 4.4 investment-service

```
역할     : 자산 관리, 시세 polling, 수익률 계산
DB      : MySQL (자산, 매수내역)
Redis   : 시세 캐시
의존     : 외부 시세 API, Kafka Producer, Redis

패키지:
com.familyhub.investment/
├── controller/
├── service/
├── repository/
├── scheduler/    ← 시세 polling @Scheduled
├── client/       ← 외부 API 클라이언트
├── event/        ← Kafka Producer (변동 감지)
└── config/
```

### 4.5 calendar-sync-service

```
역할     : 일정 관리, Google Calendar 양방향 동기화
DB      : MySQL (일정 데이터)
의존     : Google Calendar API, Kafka

패키지:
com.familyhub.calendar/
├── controller/
├── service/
├── repository/
├── sync/         ← Google Calendar 동기화 로직
├── webhook/      ← Google Webhook 수신
└── config/
```

---

## 5. API Gateway 설계

### 5.1 라우팅 규칙

| 경로 패턴 | 라우팅 대상 |
|-----------|------------|
| `/api/v1/auth/**` | monolith (app-api) |
| `/api/v1/workspaces/**` | monolith (app-api) |
| `/api/v1/projects/**` | monolith (app-api) |
| `/api/v1/issues/**` | monolith (app-api) |
| `/api/v1/sprints/**` | monolith (app-api) |
| `/api/v1/notifications/**` | notification-service |
| `/api/v1/search/**` | search-service |
| `/api/v1/investments/**` | investment-service |
| `/api/v1/calendar/**` | calendar-sync-service |

### 5.2 Gateway 공통 필터

```
1. JWT 인증 필터     - Access Token 검증 (공개 API 제외)
2. Rate Limit 필터   - IP / User 기반 요청 제한
3. Logging 필터      - 요청/응답 로깅 (MDC에 traceId 주입)
4. CORS 필터         - 허용 Origin 관리
```

### 5.3 공개 API (인증 불필요)

```
POST /api/v1/auth/signup
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /api/v1/auth/oauth2/**
GET  /actuator/health
```

---

## 6. 이벤트 아키텍처 (Kafka)

### 6.1 이벤트 설계 원칙

```
✅ 이벤트는 과거형으로 명명한다 (issue.created, not issue.create)
✅ 이벤트 페이로드는 소비자가 필요한 최소한의 정보만 포함
✅ 이벤트 스키마 변경 시 버전 관리 (v1, v2)
✅ Consumer는 멱등성을 보장해야 한다
```

### 6.2 이벤트 페이로드 예시

**issue.status.changed**
```json
{
  "eventType": "issue.status.changed",
  "eventId": "uuid",
  "occurredAt": "2026-04-12T00:00:00Z",
  "payload": {
    "issueId": 123,
    "issueTitle": "로그인 버그 수정",
    "workspaceId": 1,
    "projectId": 10,
    "fromStatus": "IN_PROGRESS",
    "toStatus": "IN_REVIEW",
    "changedBy": {
      "userId": 42,
      "name": "Aslan"
    },
    "assigneeId": 55
  }
}
```

**investment.fluctuated**
```json
{
  "eventType": "investment.fluctuated",
  "eventId": "uuid",
  "occurredAt": "2026-04-12T00:00:00Z",
  "payload": {
    "userId": 42,
    "assetSymbol": "005930",
    "assetName": "삼성전자",
    "changeRate": -5.3,
    "currentPrice": 71500
  }
}
```

### 6.3 Kafka 토픽 구성

| Topic | Partition | Replication | 보존 기간 |
|-------|-----------|-------------|----------|
| issue.created | 3 | 2 | 7일 |
| issue.updated | 3 | 2 | 7일 |
| issue.deleted | 3 | 2 | 7일 |
| issue.status.changed | 3 | 2 | 7일 |
| issue.assigned | 3 | 2 | 7일 |
| issue.commented | 3 | 2 | 7일 |
| issue.mentioned | 3 | 2 | 7일 |
| sprint.started | 2 | 2 | 7일 |
| sprint.completed | 2 | 2 | 7일 |
| investment.fluctuated | 2 | 2 | 3일 |
| calendar.synced | 2 | 2 | 3일 |

---

## 7. 데이터베이스 전략

### 7.1 DB per Service 원칙

```
monolith              → MySQL (family_hub_db)
investment-service    → MySQL (investment_db)
notification-service  → MongoDB (알림 이력)
search-service        → OpenSearch
calendar-sync-service → MySQL (calendar_db)
```

### 7.2 MySQL 핵심 테이블 목록 (monolith)

```sql
-- 사용자
users
user_providers           -- OAuth 소셜 연결 정보

-- 워크스페이스
workspaces
workspace_members

-- 프로젝트
projects
project_members

-- 워크플로우
workflow_statuses        -- 상태 정의
workflow_transitions     -- 전환 규칙

-- 이슈
issues
issue_relations          -- 이슈 간 연결 (블로킹/관련)
issue_tags

-- 스프린트
sprints
sprint_issues            -- 스프린트-이슈 매핑

-- 일정 (calendar-sync-service와 분리 고려)
schedules
schedule_recurrences     -- 반복 일정 규칙
```

### 7.3 MongoDB 컬렉션 (공유)

```
comments                 -- 이슈 댓글
activity_logs            -- 활동 히스토리 (이슈 변경 이력)
notifications            -- 알림 이력
```

### 7.4 Redis Key 설계

```
refresh_token:{userId}              ← JWT Refresh Token
stock_price:{symbol}                ← 시세 캐시
session:{sessionId}                 ← 세션
notification:unread:{userId}        ← 읽지 않은 알림 수
distributed_lock:{resource}         ← 분산 락
```

---

## 8. 인증 / 인가 흐름

### 8.1 JWT 인증 흐름

```
Client
  │
  ├─ POST /api/v1/auth/login
  │       ↓
  │   Gateway (인증 불필요 경로)
  │       ↓
  │   Monolith → JWT 발급
  │       ↓
  │   Access Token (30분) + Refresh Token (7일, HttpOnly Cookie)
  │
  ├─ API 요청
  │   Authorization: Bearer {accessToken}
  │       ↓
  │   Gateway JWT Filter → 검증
  │       ↓
  │   각 서비스 (userId, roles 헤더로 전달)
  │
  └─ Access Token 만료 시
      POST /api/v1/auth/refresh
          ↓
      Refresh Token 검증 (Redis) → 새 Access Token 발급
      (Refresh Token Rotation 적용)
```

### 8.2 서비스 간 인증

```
Gateway → 내부 서비스 통신 시
  X-Internal-User-Id: {userId}
  X-Internal-Roles: {roles}
  헤더로 인증 정보 전달 (JWT 재검증 불필요)
```

---

## 9. 관측성 (Observability)

### 9.1 분산 트레이싱 (Pinpoint)

```
모든 서비스에 Pinpoint Agent 적용
→ 서비스 간 호출 추적
→ DB 쿼리 성능 추적
→ Kafka 메시지 흐름 추적
```

### 9.2 로깅 전략

```
각 서비스 → Logback → OpenSearch
MDC 필드:
  - traceId     (Gateway에서 생성, 전파)
  - userId
  - requestPath
  - serviceName
```

### 9.3 헬스체크

```
모든 서비스: GET /actuator/health
Gateway: 각 서비스 헬스체크 라우팅 포함
```

---

## 10. 로컬 개발 환경

### 10.1 docker-compose.infra.yml (인프라만)

```yaml
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]

  redis:
    image: redis:7
    ports: ["6379:6379"]

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports: ["9092:9092"]

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0

  mongodb:
    image: mongo:7
    ports: ["27017:27017"]

  opensearch:
    image: opensearchproject/opensearch:2.11.0
    ports: ["9200:9200"]
```

### 10.2 실행 순서

```bash
# 1. 인프라 먼저 기동
docker-compose -f infra/docker-compose.infra.yml up -d

# 2. Gateway 기동
cd gateway/app-gateway && ./gradlew bootRun

# 3. Monolith 기동
cd monolith && ./gradlew :app-api:bootRun

# 4. 필요한 MSA 기동
cd services/notification-service && ./gradlew bootRun
```

---

## 11. AWS 배포 아키텍처 (목표)

```
                          [Route 53]
                               │
                          [ALB / HTTPS]
                               │
                    [API Gateway - ECS Task]
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
  [Monolith ECS]    [Notification ECS]   [Investment ECS]
          │                    │                    │
  [RDS MySQL]          [DocumentDB]         [ElastiCache Redis]
                               │
                    [MSK - Kafka]
                               │
              [OpenSearch Service]
```

---

## 12. Architecture Decision Records (ADR)

### ADR-001: Monorepo 채택

**결정**: 단일 Git 저장소에서 모든 서비스 관리  
**이유**: 초기 개발 속도, 공통 타입 공유, 통합 CI/CD 용이  
**트레이드오프**: 저장소 크기 증가, 빌드 시간 관리 필요

---

### ADR-002: Modular Monolith 우선 채택

**결정**: 핵심 도메인은 모놀리스로 시작  
**이유**: 도메인 경계 탐색 단계에서 MSA 분리는 오버엔지니어링  
**기준**: 트래픽, 팀 규모, 변경 빈도 기준으로 분리 여부 판단

---

### ADR-003: Kafka 기반 이벤트 통신

**결정**: 서비스 간 비동기 통신은 Kafka 사용  
**이유**: 느슨한 결합, 이벤트 재처리 가능, 실무 기술 스택과 일치  
**트레이드오프**: 결과적 일관성 허용, 멱등성 보장 필요

---

### ADR-004: DB per Service

**결정**: 각 MSA는 자체 DB 소유  
**이유**: 독립 배포, 스키마 결합 방지  
**트레이드오프**: 조인 불가 → 이벤트 기반 데이터 복제로 보완

---

*이 문서는 아키텍처 결정이 변경될 때마다 업데이트됩니다.*
