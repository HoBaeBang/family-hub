# CLAUDE.md — Family Hub

Claude Code가 이 프로젝트를 이해하기 위한 컨텍스트 파일입니다.
**새 세션 시작 시 이 파일과 함께 docs/SPEC.md, docs/ARCHITECTURE.md를 반드시 읽어주세요.**

---

## 프로젝트 개요

**Family Hub** — 개인/가족을 위한 Jira급 프로젝트 관리 + 투자 대시보드 플랫폼

---

## 아키텍처 요약

```
Monorepo
├── monolith/          ← Modular Monolith (핵심 도메인)
├── services/          ← Microservices (독립 분리된 서비스)
├── gateway/           ← Spring Cloud Gateway
├── frontend/          ← Next.js
├── infra/             ← Docker Compose, Terraform
└── docs/              ← 설계 문서
```

**핵심 원칙**: 핵심 도메인(Issue, Workflow, Sprint)은 Monolith, 독립 확장 가능한 것(Notification, Search, Investment, Calendar)은 MSA로 분리

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Monolith | Spring Boot 3.x, Gradle 멀티모듈 |
| DB | MySQL, MongoDB, Redis |
| 이벤트 | Kafka |
| 검색 | OpenSearch |
| MSA 통신 | Kafka (비동기), REST (동기) |
| Gateway | Spring Cloud Gateway |
| Frontend | Next.js 14, Tailwind, shadcn/ui |
| 인프라 | AWS ECS, RDS, ElastiCache, MSK |
| 모니터링 | Pinpoint, OpenSearch |

---

## 현재 진행 Phase

> **Phase 1** — Monorepo 세팅, Gradle 멀티모듈 구조, API Gateway, Docker Compose 인프라

---

## 개발 원칙

1. **설계는 개발자(Aslan)가 먼저** — Claude Code는 설계 리뷰 및 구현 보조
2. **테스트 코드 항상 함께** — 기능 구현 시 단위/통합 테스트 동반
3. **컨펌 포인트 준수** — 아래 항목은 반드시 리뷰 후 진행
   - ERD 변경
   - 트랜잭션 경계 설계
   - Kafka 이벤트 스키마
   - 서비스 간 통신 방식
   - 보안 관련 코드
4. **ADR 작성** — 주요 아키텍처 결정은 docs/ADR/ 에 기록

---

## 세션 시작 템플릿

```
docs/SPEC.md와 docs/ARCHITECTURE.md를 읽어줘.
현재 Phase [N] 진행 중이야.
오늘은 [작업 내용]을 하려고 해.
내 설계 방향은 [설계 의도]야. 리뷰해줘.
```

---

## 문서 위치

| 문서 | 경로 |
|------|------|
| 기능정의서 | docs/SPEC.md |
| 아키텍처 | docs/ARCHITECTURE.md |
| ERD | docs/ERD.md (예정) |
| API 명세 | docs/API.md (예정) |
| ADR | docs/ADR/ |
