# QueueLess REST API
Base URL: `http://localhost:8081/api/v1`

## Health
- `GET /health`

## Queues
- `GET /queues` — list queues
- `POST /queues` — create queue
  ```json
  {"name":"General Consultation","businessName":"City Clinic","maxCapacity":30}
  ```
- `GET /queues/{queueId}` — queue details and live counts
- `GET /queues/{queueId}/entries` — entries ordered by token
- `POST /queues/{queueId}/join` — join queue
  ```json
  {"customerName":"Asha Patil","phone":"9876543210"}
  ```
- `POST /queues/{queueId}/call-next` — call the next waiting customer
- `PATCH /queues/{queueId}/entries/{entryId}/status` — change status
  ```json
  {"status":"COMPLETED"}
  ```
  Allowed status values: `COMPLETED`, `SKIPPED`, `CANCELLED`. `WAITING` and `CALLED` are managed by the queue workflow.

## Entry statuses
`WAITING`, `CALLED`, `COMPLETED`, `SKIPPED`, `CANCELLED`.
