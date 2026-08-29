# Authorization Architecture

## Authority model

The platform uses database-driven authorization.

```
User
  -> Roles
      -> Permissions
          -> Spring GrantedAuthority
              -> @PreAuthorize
```

Each authenticated request resolves the current user's active roles and permissions from the database.

### Role authorities

Roles are exposed as:

```
ROLE_SUPER_ADMIN
ROLE_ADMIN
ROLE_STUDENT
ROLE_TEACHER
```

### Permission authorities

Permissions are exposed directly:

```
SYSTEM:READ
USER:READ
USER:CREATE
USER:UPDATE
USER:DELETE
```

## Recommended protection

Prefer permission checks for business APIs:

```java
@PreAuthorize("hasAuthority('USER:READ')")
```

Use roles only for broad platform-level access.

## Important

Permissions are intentionally resolved dynamically instead of permanently embedded in JWT claims. Role or permission changes therefore take effect on the next authenticated request without waiting for JWT expiration.
