# Contributing

Follow these rules:
- Use feature branches: `feature/<ticket>-short-desc`
- Create PRs targeting `develop`
- PR title: `[FEATURE] Short description`
- Ensure all tests pass, run `mvn -B test`
- Use conventional commits: type(scope): short description
  - Example: `feat(auth): add registration dto`
- Run static analysis (optional): `mvn -DskipTests=false verify`
