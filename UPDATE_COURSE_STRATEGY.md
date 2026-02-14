# Course Update (PATCH-like) — Sections & Chapters

## Overview

The `PUT /courses/{courseId}` endpoint supports a **PATCH-like update** of a course.

All fields are **optional**.  
Only the fields present in the request are applied.

This includes **structural updates** on:

- Sections
- Chapters
- Videos (indirectly, via chapter deletion)

The update logic supports **create / update / soft delete** in a single request.

---

## General Rules

### 1. PATCH semantics

- **Field omitted (`null`)** → no change
- **Field present** → patch applied

This applies recursively to:

- `sections`
- `chapters`

### 2. Identification rules

- `id = null` → **CREATE**
- `id != null` → **UPDATE**
- existing entity **missing from payload** → **SOFT DELETE**

### 3. Deletions are soft

- Sections, chapters and videos are **soft-deleted** (`deletedAt`)
- Video deletion is handled asynchronously via **Outbox + Rabbit**

---

## UpdateCourseRequest Schema (simplified)

```json
{
  "title": "optional",
  "description": "optional",
  "price": 0,
  "sections": [
    {
      "id": "optional",
      "title": "optional",
      "position": 1,
      "chapters": [
        {
          "id": "optional",
          "title": "optional",
          "position": 1
        }
      ]
    }
  ]
}
```

---

## Sections Patch Behavior

| sections value | Behavior                          |
|----------------|-----------------------------------|
| `null`         | No section changes                |
| `[]`           | Soft delete all existing sections |
| `[ ... ]`      | Patch sections                    |

## Chapters Patch Behavior

| chapters value | Behavior                                |
|----------------|-----------------------------------------|
| `null`         | No chapter changes                      |
| `[]`           | Soft delete all chapters in the section |
| `[ ... ]`      | Patch chapters                          |

---

## Examples

### Example 1 — Update section & chapter titles

```json
{
  "sections": [
    {
      "id": "section-1",
      "title": "Updated section title",
      "chapters": [
        {
          "id": "chapter-1",
          "title": "Updated chapter title"
        }
      ]
    }
  ]
}
```

**Result:**

- Section `section-1` updated
- Chapter `chapter-1` updated
- No creation, no deletion

---

### Example 2 — Create a new chapter

```json
{
  "sections": [
    {
      "id": "section-1",
      "chapters": [
        {
          "id": "chapter-1",
          "title": "Existing chapter"
        },
        {
          "title": "New chapter",
          "position": 2
        }
      ]
    }
  ]
}
```

**Result:**

- New chapter created (id generated)
- Existing chapter preserved
- No deletion

---

### Example 3 — Delete a chapter (soft delete)

**Existing state:**

```
Section section-1
├─ chapter-1
└─ chapter-2
```

**Request:**

```json
{
  "sections": [
    {
      "id": "section-1",
      "chapters": [
        {
          "id": "chapter-1",
          "title": "Kept chapter"
        }
      ]
    }
  ]
}
```

**Result:**

- `chapter-2` soft-deleted
- If `chapter-2` had a video:
    - video marked `deletedAt`
    - `externalDeletionStatus = REQUESTED`
    - outbox event created (`VIDEO_DELETION_REQUESTED`)

---

### Example 4 — Delete all chapters in a section

```json
{
  "sections": [
    {
      "id": "section-1",
      "chapters": []
    }
  ]
}
```

**Result:**

- All chapters in `section-1` soft-deleted
- Videos handled via outbox if present

---

### Example 5 — Delete a full section (cascade)

**Existing state:**

```
Course
├─ section-1
└─ section-2
```

**Request:**

```json
{
  "sections": [
    {
      "id": "section-1"
    }
  ]
}
```

**Result:**

- `section-2` soft-deleted
- All its chapters soft-deleted
- All related videos marked for external deletion

---

### Example 6 — Update course metadata only (no structure impact)

```json
{
  "title": "New course title"
}
```

**Result:**

- Only course title updated
- Sections & chapters untouched

---

## Video Deletion Flow (Important)

When a chapter containing a video is deleted:

1. Video is **soft-deleted**
2. `externalDeletionStatus = REQUESTED`
3. An **outbox event** is created
4. **Async worker** deletes the video from the external provider (e.g. Vimeo)
5. Retries + backoff handled via **RabbitMQ**

**No external API call happens in the HTTP transaction.**

---

## Design Guarantees

- ✅ Non-destructive by default
- ✅ Explicit deletions only
- ✅ Idempotent updates
- ✅ Compatible with drag & drop UIs
- ✅ Safe for partial updates
- ✅ No JPA entities exposed outside persistence layer

---

## Common Pitfalls (Frontend)

| Action                                   | Result                              |
|------------------------------------------|-------------------------------------|
| ❌ Sending `sections: []` unintentionally | → deletes all sections              |
| ❌ Sending `chapters: []` unintentionally | → deletes all chapters in a section |
| ✅ Omit the field entirely (`null`)       | → no change is intended             |

---

## Summary

This **PATCH-like design** allows full structural editing of a course in a single request while keeping:

- ✅ Strong consistency
- ✅ Safe deletions
- ✅ Async external side effects
- ✅ Clear frontend/backend contract
