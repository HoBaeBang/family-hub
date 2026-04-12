# Family Hub — 기능정의서 (Functional Specification)

> **버전**: v0.2.0  
> **작성일**: 2026-04-12  
> **목적**: 개인/가족용 프로젝트 관리 툴 (Jira + 가족 허브 + 투자 대시보드)  
> **아키텍처**: Modular Monolith + Microservices (과도기 혼합 모델)  
> **개발자**: Aslan  
> **상세 아키텍처**: docs/ARCHITECTURE.md 참고

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
**Family Hub** — 가족/개인을 위한 올인원 프로젝트 관리 플랫폼

### 1.2 목적
- 가족 구성원이 함께 프로젝트와 일정을 관리할 수 있는 Jira급 협업 도구 구축
- 개인 투자 현황을 한눈에 파악할 수 있는 대시보드 통합
- 백엔드 기본기(설계, 트랜잭션, 이벤트, 캐싱, 검색 등) 실무 수준 학습
- Modular Monolith → MSA 분리 과정을 직접 경험하며 아키텍처 판단력 습득

### 1.3 대상 사용자
- 개인 개발자 (사이드 프로젝트 관리)
- 가족 구성원 (공동 일정 및 할 일 관리)

### 1.4 핵심 컨셉
```
Workspace (팀/가족 그룹)
└── Project (프로젝트 단위)
    ├── Board (칸반 or 스프린트 보드)
    │   └── Issue (이슈/카드)
    │       ├── Sub-task
    │       ├── 댓글
    │       └── 활동 로그
    ├── Workflow (상태 + 전환 규칙 커스텀)
    ├── Backlog
    ├── Sprint
    └── Roadmap (타임라인/간트)
```

### 1.5 아키텍처 요약
- **Modular Monolith**: 핵심 도메인 (Auth, Workspace, Project, Issue, Workflow, Sprint)
- **Microservices**: 독립 확장/변경이 필요한 영역 (Notification, Search, Investment, Calendar Sync)
- **API Gateway**: 단일 진입점 (Spring Cloud Gateway)
- **이벤트 버스**: Kafka로 서비스 간 비동기 통신

---

## 2. 기술 스택

### 2.1 백엔드 (Modular Monolith)
| 구분 | 기술 | 용도 |
|------|------|------|
| Framework | Spring Boot 3.x | 메인 API 서버 |
| Build | Gradle (멀티모듈) | 모듈 분리 빌드 |
| DB | MySQL 8.x | 핵심 도메인 데이터 |
| NoSQL | MongoDB | 댓글, 활동 로그, 알림 이력 |
| Cache | Redis | 세션, JWT Refresh Token, 시세 캐싱 |
| Message | Kafka | 이벤트 기반 비동기 처리 |
| Monitoring | Pinpoint | APM, 성능 추적 |
| Logging | OpenSearch | 애플리케이션 로그 수집 |

### 2.2 백엔드 (Microservices)
| 서비스 | 기술 | 주요 의존성 |
|--------|------|------------|
| notification-service | Spring Boot 3.x | Kafka, Redis, SMTP |
| search-service | Spring Boot 3.x | OpenSearch, Kafka |
| investment-service | Spring Boot 3.x | Redis, 외부 시세 API, Kafka |
| calendar-sync-service | Spring Boot 3.x | Google Calendar API, Kafka |

### 2.3 API Gateway
| 구분 | 기술 |
|------|------|
| Gateway | Spring Cloud Gateway |
| 인증 검증 | JWT Filter (Gateway 레벨) |
| 라우팅 | 경로별 서비스 라우팅 |

### 2.4 프론트엔드
| 구분 | 기술 | 용도 |
|------|------|------|
| Framework | Next.js 14 (App Router) | SSR/CSR 혼합 |
| Styling | Tailwind CSS | 유틸리티 퍼스트 |
| UI Library | shadcn/ui | 컴포넌트 |
| 차트 | Recharts | 투자 차트, 번다운 차트 |
| 상태관리 | Zustand | 클라이언트 상태 |
| API 통신 | TanStack Query | 서버 상태 관리 |

### 2.5 인프라
| 구분 | 기술 | 용도 |
|------|------|------|
| 컨테이너 | Docker / Docker Compose | 로컬 개발 |
| 클라우드 | AWS ECS (Fargate) | 서비스 배포 |
| DB | AWS RDS (MySQL) | 프로덕션 DB |
| Cache | AWS ElastiCache (Redis) | 프로덕션 캐시 |
| Message | AWS MSK (Kafka) | 프로덕션 메시지 |
| 검색 | AWS OpenSearch Service | 프로덕션 검색 |
| IaC | Terraform | 인프라 코드화 |

### 2.6 외부 연동
| 서비스 | 용도 |
|--------|------|
| Google OAuth2 | 소셜 로그인 |
| Google Calendar API | 일정 양방향 동기화 |
| 한국투자증권 Open API | 국내 주식 시세 |
| Yahoo Finance API | 해외 주식 / ETF 시세 |
| Upbit / Binance API | 코인 시세 |

---

## 3. 레포지토리 구조 (Monorepo)

```
family-hub/                          ← Git Monorepo 루트
│
├── monolith/                        ← 모듈러 모놀리스
│   ├── app-api/                     ← Spring Boot 메인 API
│   ├── app-batch/                   ← 스케줄러 (알림 배치 등)
│   ├── core-domain/                 ← 엔티티, 도메인 로직, VO
│   ├── core-db/                     ← JPA Repository, QueryDSL
│   ├── core-mongo/                  ← MongoDB Repository
│   ├── core-redis/                  ← Redis 설정, 캐시 유틸
│   ├── core-kafka/                  ← Producer / Consumer 공통
│   └── support-logging/             ← 공통 로깅, MDC
│
├── services/                        ← 독립 마이크로서비스
│   ├── notification-service/        ← 알림 (SSE, 이메일)
│   ├── search-service/              ← OpenSearch 색인 / 검색
│   ├── investment-service/          ← 투자 시세, 수익률
│   └── calendar-sync-service/       ← Google Calendar 동기화
│
├── gateway/
│   └── app-gateway/                 ← Spring Cloud Gateway
│
├── frontend/                        ← Next.js
│
├── infra/
│   ├── docker-compose.yml           ← 전체 앱 로컬 실행
│   ├── docker-compose.infra.yml     ← Kafka, Redis, MySQL, OpenSearch
│   └── terraform/                   ← AWS IaC
│
└── docs/
    ├── SPEC.md                      ← 기능정의서 (이 파일)
    ├── ARCHITECTURE.md              ← 아키텍처 상세
    ├── ERD.md                       ← 데이터베이스 설계
    ├── API.md                       ← API 명세
    └── ADR/                         ← Architecture Decision Records
        ├── ADR-001-monorepo.md
        ├── ADR-002-modular-monolith.md
        └── ADR-003-kafka-event-driven.md
```

---

## 4. 기능 정의

---

### 4.1 인증 / 사용자 관리 (AUTH) — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| AUTH-01 | 이메일 회원가입 | P0 |
| AUTH-02 | 이메일 로그인 | P0 |
| AUTH-03 | Google OAuth2 소셜 로그인 | P0 |
| AUTH-04 | JWT Access / Refresh Token 발급 | P0 |
| AUTH-05 | Refresh Token 갱신 | P0 |
| AUTH-06 | 로그아웃 (Token 무효화) | P0 |
| AUTH-07 | 회원 프로필 조회 / 수정 | P1 |
| AUTH-08 | 비밀번호 변경 | P1 |

**JWT 구조**
```
Access Token  — 30분, 클라이언트 메모리
Refresh Token — 7일, Redis + HttpOnly Cookie
```

**Google OAuth2 추가 scope**
```
- openid, email, profile           (기본 로그인)
- .../auth/calendar                (Calendar Sync 서비스용)
```

---

### 4.2 Workspace — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| WS-01 | Workspace 생성 | P0 |
| WS-02 | Workspace 목록 조회 | P0 |
| WS-03 | Workspace 설정 수정 | P1 |
| WS-04 | 멤버 초대 (초대 링크 / 이메일) | P0 |
| WS-05 | 멤버 권한 변경 | P1 |
| WS-06 | 멤버 추방 | P1 |
| WS-07 | Workspace 삭제 | P2 |

**권한 정의**
| 권한 | 설명 |
|------|------|
| ADMIN | 전체 관리 (멤버 초대/추방, 프로젝트 삭제) |
| MEMBER | 프로젝트 참여, 이슈 생성/수정 |
| VIEWER | 읽기 전용 |

---

### 4.3 Project — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| PRJ-01 | 프로젝트 생성 (KANBAN / SCRUM) | P0 |
| PRJ-02 | 프로젝트 목록 조회 | P0 |
| PRJ-03 | 프로젝트 설정 수정 | P1 |
| PRJ-04 | 프로젝트 멤버 관리 | P1 |
| PRJ-05 | 프로젝트 아카이브 / 삭제 | P2 |

---

### 4.4 Issue — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| ISS-01 | 이슈 생성 | P0 |
| ISS-02 | 이슈 목록 조회 (보드 뷰) | P0 |
| ISS-03 | 이슈 상세 조회 | P0 |
| ISS-04 | 이슈 수정 | P0 |
| ISS-05 | 이슈 삭제 | P1 |
| ISS-06 | 이슈 상태 변경 (워크플로우 규칙 적용) | P0 |
| ISS-07 | 이슈 담당자 지정 | P0 |
| ISS-08 | 이슈 우선순위 설정 | P0 |
| ISS-09 | 이슈 간 연결 (블로킹 / 관련 / 복제) | P1 |
| ISS-10 | Sub-task 생성 | P1 |
| ISS-11 | 댓글 작성 / 수정 / 삭제 (MongoDB) | P1 |
| ISS-12 | 멘션 (@사용자) | P1 |
| ISS-13 | 첨부파일 업로드 (S3) | P2 |
| ISS-14 | 활동 히스토리 조회 (MongoDB) | P1 |
| ISS-15 | 이슈 드래그앤드롭 (순서/상태 변경) | P0 |

**이슈 타입**: `EPIC` / `STORY` / `TASK` / `BUG` / `SUB_TASK`
**우선순위**: `HIGHEST` / `HIGH` / `MEDIUM` / `LOW` / `LOWEST`

**이슈 필드**
| 필드 | 타입 | 필수 |
|------|------|------|
| title | String | ✅ |
| description | RichText (HTML) | ❌ |
| type | Enum | ✅ |
| status | WorkflowStatus FK | ✅ |
| assignee | User FK | ❌ |
| reporter | User FK | ✅ (자동) |
| priority | Enum | ✅ (기본 MEDIUM) |
| dueDate | DateTime | ❌ |
| storyPoint | Integer | ❌ |
| tags | List\<String\> | ❌ |
| parentIssue | Issue FK | ❌ |

**Kafka 이벤트 발행**
```
issue.created        → search-service (색인)
issue.updated        → search-service (색인 갱신)
issue.status.changed → notification-service, search-service
issue.assigned       → notification-service
issue.commented      → notification-service
issue.mentioned      → notification-service
```

---

### 4.5 Workflow — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| WF-01 | 상태(Status) 생성 / 수정 / 삭제 | P0 |
| WF-02 | 상태 간 전환 규칙 설정 | P0 |
| WF-03 | 워크플로우 템플릿 제공 | P1 |
| WF-04 | 상태별 담당자 자동 지정 규칙 | P2 |

**기본 템플릿**
```
[개발용]   TO_DO → IN_PROGRESS → IN_REVIEW → DONE
[가사용]   할일  → 진행중       → 완료
[스터디용] 계획  → 학습중       → 정리중    → 완료
```

---

### 4.6 Sprint — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| SPR-01 | 스프린트 생성 | P0 |
| SPR-02 | 백로그 → 스프린트 이슈 이동 | P0 |
| SPR-03 | 스프린트 시작 | P0 |
| SPR-04 | 스프린트 완료 | P0 |
| SPR-05 | 번다운 차트 조회 | P1 |
| SPR-06 | 스프린트 완료율 / 통계 | P1 |
| SPR-07 | 미완료 이슈 다음 스프린트로 이동 | P1 |

**스프린트 상태**: `PLANNED` → `ACTIVE` → `COMPLETED`

---

### 4.7 Roadmap — Monolith

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| ROAD-01 | Epic 기준 타임라인 (간트 차트) | P1 |
| ROAD-02 | 드래그로 기간 조정 | P2 |
| ROAD-03 | Epic 하위 이슈 진행률 표시 | P1 |

---

### 4.8 Notification Service — MSA

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| NOTI-01 | 인앱 실시간 알림 (SSE) | P1 |
| NOTI-02 | 이메일 알림 | P1 |
| NOTI-03 | 알림 읽음 처리 | P1 |
| NOTI-04 | 알림 설정 (타입별 ON/OFF) | P2 |

**Kafka Consumer Topics**
```
issue.status.changed / issue.assigned / issue.commented / issue.mentioned
sprint.started / sprint.completed
investment.fluctuated
```

---

### 4.9 Search Service — MSA

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| SRCH-01 | 이슈 제목 / 설명 검색 | P1 |
| SRCH-02 | 댓글 내용 검색 | P2 |
| SRCH-03 | 필터 (담당자, 상태, 우선순위, 날짜) | P1 |
| SRCH-04 | 검색 결과 하이라이팅 | P2 |

**Kafka Consumer**: `issue.created` / `issue.updated` / `issue.deleted`

---

### 4.10 Investment Service — MSA

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| INV-01 | 자산 등록 (주식 / ETF / 코인 / 예적금) | P0 |
| INV-02 | 매수 내역 등록 (날짜, 수량, 단가) | P0 |
| INV-03 | 실시간 시세 조회 (Redis 캐싱) | P0 |
| INV-04 | 수익률 / 평가금액 / 손익 계산 | P0 |
| INV-05 | 지수 차트 (코스피, 나스닥, S&P500) | P1 |
| INV-06 | 자산 비중 파이차트 | P1 |
| INV-07 | 월별 수익률 히스토리 | P1 |
| INV-08 | 목표 수익률 설정 및 달성률 표시 | P2 |

**시세 캐싱 정책**
```
국내 주식 / 해외 주식 : Redis TTL 1분 (장중), 1시간 (장외)
코인                  : Redis TTL 30초
예적금                : Redis TTL 1일
```

---

### 4.11 Calendar Sync Service — MSA

| ID | 기능명 | 우선순위 |
|----|--------|----------|
| CAL-01 | 개인 일정 등록 / 수정 / 삭제 | P0 |
| CAL-02 | 공유 일정 (Workspace 전체 공개) | P0 |
| CAL-03 | 이슈 마감일 캘린더 뷰 연동 | P1 |
| CAL-04 | 반복 일정 | P1 |
| CAL-05 | Google Calendar 양방향 동기화 | P1 |
| CAL-06 | 일정 알림 | P1 |
| CAL-07 | 월간 / 주간 / 일간 뷰 | P1 |

---

## 5. 서비스 간 통신 설계

### Kafka 이벤트 토픽 전체 목록
| Topic | Producer | Consumer |
|-------|----------|----------|
| issue.created | monolith | search-service |
| issue.updated | monolith | search-service |
| issue.deleted | monolith | search-service |
| issue.status.changed | monolith | notification-service |
| issue.assigned | monolith | notification-service |
| issue.commented | monolith | notification-service |
| issue.mentioned | monolith | notification-service |
| sprint.started | monolith | notification-service |
| sprint.completed | monolith | notification-service |
| investment.fluctuated | investment-service | notification-service |
| calendar.synced | calendar-sync-service | monolith |

### 동기 REST 호출
| Caller | Callee | 이유 |
|--------|--------|------|
| monolith | search-service | 검색 결과 즉시 조회 |
| monolith | investment-service | 현재 시세 즉시 조회 (캐시 hit) |

---

## 6. 데이터 저장소 역할 분담

| 저장소 | 데이터 |
|--------|--------|
| MySQL (monolith) | 유저, 워크스페이스, 프로젝트, 이슈, 워크플로우, 스프린트 |
| MySQL (investment-service) | 자산, 매수내역, 수익률 히스토리 |
| MongoDB | 댓글, 활동 로그, 알림 이력 |
| Redis | Refresh Token, 시세 캐시, 세션 |
| OpenSearch | 이슈/댓글 검색 색인, 애플리케이션 로그 |
| AWS S3 | 첨부파일 |

---

## 7. 비기능 요구사항

| 항목 | 목표 |
|------|------|
| 이슈 목록 조회 | 200ms 이하 |
| 검색 응답 | 300ms 이하 |
| 시세 캐시 갱신 | 1분 (장중) |
| 보안 | HTTPS, JWT Rotation, VPC 내부망 격리 |
| 관측성 | Pinpoint 분산 추적, OpenSearch 중앙 로그 |

---

## 8. API 설계 원칙

**공통 응답 포맷**
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-12T00:00:00Z"
}
```

---

## 9. 개발 Phase 로드맵

| Phase | 내용 | 핵심 학습 |
|-------|------|-----------|
| Phase 1 | Monorepo, Gradle 멀티모듈, API Gateway, Docker Compose | 프로젝트 구조 |
| Phase 2 | JWT 인증, OAuth2, Spring Security | 보안, 인증/인가 |
| Phase 3 | Workspace → Project → Issue CRUD, 칸반 보드 | JPA 설계, 트랜잭션 |
| Phase 4 | Workflow 엔진 (상태/전환 규칙) | 상태 패턴, 도메인 로직 |
| Phase 5 | Sprint + Backlog + 번다운 차트 | 집계 쿼리, 통계 |
| Phase 6 | Notification Service 분리 (첫 MSA) | Kafka, SSE, 이벤트 |
| Phase 7 | Investment Service + 시세 연동 | Redis 캐싱, 외부 API |
| Phase 8 | Search Service + OpenSearch | 검색 엔진, 색인 |
| Phase 9 | Calendar Sync Service + Google API | OAuth2 심화, Webhook |
| Phase 10 | AWS 배포, Pinpoint, 성능 최적화 | 인프라, APM |

---

## 10. Claude Code 협업 가이드

**세션 시작 템플릿**
```
docs/SPEC.md와 docs/ARCHITECTURE.md를 읽어줘.
현재 Phase [N] 진행 중이야.
오늘은 [작업 내용]을 하려고 해.
내 설계 방향은 [설계 의도]야. 리뷰해줘.
```

**반드시 리뷰 후 진행할 컨펌 포인트**
- ERD 변경
- 트랜잭션 경계 설계
- Kafka 이벤트 스키마
- 서비스 간 통신 방식 결정
- 보안 관련 코드

---

*이 문서는 개발 진행에 따라 지속적으로 업데이트됩니다.*
