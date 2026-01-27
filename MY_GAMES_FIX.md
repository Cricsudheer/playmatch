# My Games API - Path Conflict Fix

## Issue
When calling `GET /v2/mvp/matches/my-games`, the application was throwing an error:

```
MethodArgumentTypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: my-games
```

## Root Cause
Spring MVC was matching the request to the wrong handler method:
- Request: `GET /v2/mvp/matches/my-games`
- Matched: `@GetMapping("/{id}")` instead of `@GetMapping("/my-games")`
- Spring tried to convert "my-games" to UUID (for the `{id}` parameter)

## Why This Happened
Spring MVC request mapping resolution follows a specific order:
1. Exact matches first
2. Path patterns with wildcards/variables second
3. **Order of method declaration matters when ambiguity exists**

In the original code:
```java
@GetMapping("/{id}")           // Declared FIRST - matches ANY path segment
public ResponseEntity<MatchResponseDto> getMatch(@PathVariable UUID id) { ... }

@GetMapping("/my-games")       // Declared SECOND - more specific but comes after
public ResponseEntity<MyGamesResponseDto> getMyGames() { ... }
```

## Solution
Reordered the endpoint methods in `MatchController.java`:

```java
@GetMapping("/my-games")       // FIRST - More specific literal path
public ResponseEntity<MyGamesResponseDto> getMyGames() { ... }

@GetMapping("/{id}")           // SECOND - Generic path variable
public ResponseEntity<MatchResponseDto> getMatch(@PathVariable UUID id) { ... }
```

## Rule of Thumb
**Always place more specific path mappings BEFORE generic path variable mappings in your controller.**

### Good Order
```java
@GetMapping("/search")        // Literal path - specific
@GetMapping("/my-games")      // Literal path - specific
@GetMapping("/{id}")          // Path variable - generic
@GetMapping("/{id}/details")  // Path variable with suffix - semi-specific
```

### Bad Order
```java
@GetMapping("/{id}")          // ❌ Will match everything, including "search" and "my-games"
@GetMapping("/search")        // ❌ Never reached!
@GetMapping("/my-games")      // ❌ Never reached!
```

## Testing
After the fix:
- ✅ `GET /v2/mvp/matches/my-games` → Works correctly (routes to getMyGames())
- ✅ `GET /v2/mvp/matches/{uuid}` → Works correctly (routes to getMatch())
- ✅ Build compiles successfully
- ✅ No path conflicts

## Files Modified
- `src/main/java/com/example/playmatch/mvp/matches/controller/MatchController.java`
  - Moved `@GetMapping("/my-games")` method BEFORE `@GetMapping("/{id}")` method

## Status
✅ **FIXED** - Endpoint now accessible at `GET /v2/mvp/matches/my-games`

---

**Date:** January 23, 2026
**Priority:** High (breaking issue)
**Resolution Time:** ~5 minutes
