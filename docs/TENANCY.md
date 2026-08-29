# Organization and Tenant Architecture

The platform uses a shared-database, organization-aware architecture.

```
Platform
  -> Organization
      -> Members
      -> Future Campus / Branch
      -> Future Academic Units
      -> Future Courses and Learning Data
```

A user may belong to multiple organizations.

Organization membership is intentionally separate from global roles. Global roles describe platform authority; future organization-scoped roles will be added without changing the user table.

Current endpoints are protected for platform SUPER_ADMIN management. The /me endpoint returns organizations available to the authenticated user.
