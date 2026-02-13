---
id: 4
title: Audio upload and transcription
status: To Do
priority: high
labels:
  - backend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-3
---

## Description

Add audio recording upload to interviews with automatic transcription via AWS Transcribe. Audio files are stored in S3 with a 7-day lifecycle policy (auto-deleted). Transcription text is stored permanently in the database.

## Plan

1. Implement S3 upload service for audio files
2. Configure S3 bucket lifecycle policy for 7-day expiration
3. Implement AWS Transcribe integration for audio-to-text
4. Add audio upload endpoint to InterviewController
5. Store transcription in Interview entity when complete
6. Track audio expiry dates
7. Write tests with mocked AWS services

## Acceptance Criteria

- [ ] Audio files uploaded to S3 successfully
- [ ] S3 lifecycle auto-deletes audio after 7 days
- [ ] Transcription triggered on upload completion
- [ ] Transcription text stored in interview record
- [ ] Audio expiry date tracked correctly
- [ ] All AWS interactions tested with mocks
