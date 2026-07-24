---
name: thymeleaf-bootstrap
description: Server-rendered frontend standards using Spring MVC, Thymeleaf and Bootstrap 5.3.8 with reusable fragments, server-side search/filtering/pagination, optional HTMX and minimal JavaScript.
compatibility: opencode
metadata:
  frontend: server-rendered
  css: bootstrap-5.3.8
---

# Thymeleaf + Bootstrap 5.3.8

## Mandatory Architecture

Frontend is rendered by the Spring Boot application.

Use:
- Spring MVC
- Thymeleaf
- Bootstrap 5.3.8
- Bootstrap via CDN by default
- server-side rendering
- minimal JavaScript

Do not introduce React, Vue, Angular, SPA routing, webpack, Vite or npm pipeline unless architecture is intentionally changed.

## Templates

Use reusable template structure and fragments.

Keep business logic out of HTML.

Prepare model data in Java.

## Forms

Use standard Spring MVC forms.

Display field-level validation errors, form-level errors and success messages.

Preserve user-entered input after validation failure.

## Search

Search happens on server.

Prefer GET forms and query parameters such as `?search=abc`.

Search state should be represented in the URL.

## Filters

Represent filters using query parameters.

Preserve existing filters when changing page/sort.

## Sorting

Perform sorting server-side.

Use a controlled whitelist of sortable properties.

Represent state in URL.

## Pagination

Do not load the entire database table and paginate in JavaScript.

Use server-side pagination.

Preserve search, filter and sort while paging.

## Infinite Scroll

When explicitly requested:
1. browser requests next page
2. server queries only next page
3. server returns HTML fragment
4. browser appends fragment
5. no request after last page exists

Ordering must be deterministic.

Avoid duplicates between pages.

## HTMX

HTMX is allowed as progressive enhancement when it clearly reduces custom JavaScript.

Good uses:
- partial form submit
- live server-side search
- lazy-loaded fragments
- infinite scroll
- row refresh

Return HTML fragments, not unnecessary JSON.

Do not use HTMX when a normal page navigation/form is simpler.


## JavaScript

Use JavaScript sparingly.

Do not put untrusted server values directly into executable inline JavaScript.

Prefer HTML attributes, `data-*`, normal forms and Bootstrap components.

## Accessibility

Use semantic elements, labels, accessible buttons, descriptive links and keyboard-accessible controls.

## Empty States

Every list should handle zero results, filtered zero results and errors when relevant.

## Performance

Do not render thousands of rows.

Paginate.

Avoid huge fragments and expensive template expressions inside loops.

## Security

Thymeleaf escaping should remain enabled.

Avoid `th:utext` for untrusted content.

Use Spring Security CSRF protection for state-changing form requests.

## Testing

Verify template rendering, model attributes, validation, search/filter URLs, pagination, empty state and critical flows.

### Thymeleaf — Quick Reference (Spring Boot 3+)

```xml
<!-- Layout via fragments -->
<div th:replace="~{fragments/header :: header}"></div>

<!-- Iteration -->
<tr th:each="user : ${users}" th:class="${user.active} ? 'active' : ''">
  <td th:text="${user.name}">Name</td>
  <td th:text="${#dates.format(user.createdAt, 'dd/MM/yyyy')}">Date</td>
</tr>

<!-- Conditionals -->
<div th:if="${#lists.isEmpty(users)}">
  <p>No users found.</p>
</div>

<!-- URL building -->
<a th:href="@{/users/{id}(id=${user.id})}">View</a>

<!-- Security (Spring Security) -->
<div sec:authorize="hasRole('ADMIN')">
  <a th:href="@{/admin}">Admin Panel</a>
</div>
<div sec:isAuthenticated()>
  <p th:text="${#authentication.name}">User</p>
</div>
```

**Don't use** `th:inline="javascript"` — it's a script injection risk.

### HTMX — Core Patterns

```xml
<!-- Click to load content -->
<button hx-get="/users/42/details" hx-target="#details">
  Load Details
</button>
<div id="details"></div>

<!-- Form submit returns HTML fragment -->
<form hx-post="/users" hx-target="#user-list" hx-swap="afterbegin">
  <input type="text" name="name" required>
  <button type="submit">Create</button>
</form>

<!-- Inline delete with confirmation -->
<button hx-delete="/users/42" hx-confirm="Delete this user?"
        hx-target="closest tr" hx-swap="outerHTML">
  Delete
</button>

<!-- Search with debounce -->
<input type="text" name="q" hx-get="/users/search"
       hx-trigger="keyup changed delay:300ms" hx-target="#results">

<!-- Polling every 30s -->
<div hx-get="/notifications" hx-trigger="every 30s" hx-swap="innerHTML">
</div>
```

**HTMX + Spring Boot**: controller returns fragments — no `@ResponseBody`, just `Model` + template name returning HTML.

```java
@PostMapping("/users")
public String createUser(@Valid User user, Model model) {
    userService.save(user);
    model.addAttribute("user", user);
    return "fragments/user-row :: user-row";
}
```

### Alpine.js — When HTMX isn't Enough

```xml
<!-- Toggle (use Alpine, not a full HTMX roundtrip) -->
<div x-data="{ open: false }">
  <button @click="open = !open">Toggle</button>
  <div x-show="open" x-transition>Hidden content</div>
</div>

<!-- Dropdown -->
<div x-data="{ selected: '' }">
  <select x-model="selected">
    <option value="">All</option>
    <option value="active">Active</option>
  </select>
  <p x-text="selected ? 'Filtering: ' + selected : 'No filter'"></p>
</div>

<!-- HTMX + Alpine: trigger after HTMX swap -->
<button hx-get="/users/42" hx-target="#detail"
        @htmx:after-request="$refs.detail.classList.remove('hidden')">
  Load
</button>
<div id="detail" x-ref="detail" class="hidden"></div>
```

### Project Setup

```xml
<!-- Thymeleaf (comes with spring-boot-starter-web) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<!-- Spring Security integration -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

```html
<!-- layout.html — base template -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
  <meta charset="UTF-8">
  <title th:text="${title}">App</title>
  <script src="https://unpkg.com/htmx.org@2"></script>
  <script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3/dist/cdn.min.js"></script>
  <link rel="stylesheet" href="/css/app.css">
</head>
<body>
  <div th:replace="~{fragments/header :: header}"></div>
  <main th:replace="~{:: main}">
  <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```
