# Repository Guidelines

## Project Structure & Module Organization
Notes are grouped by topic to keep learning paths discoverable. Use `CS/` for core computer science concepts (e.g., `CS/Algorithms`, `CS/OperatingSystems`), `Web/` for frontend and backend web topics (such as `Web/JavaScript`), `Automation/` for workflow tooling (`Automation/n8n`), and `Git/` for workflow references. Interview prep content belongs in `Interview/`, while cross-cutting scaffolding or diagrams can live under `docs/`. Keep each entry as a standalone Markdown file; add supporting screenshots or diagrams to a sibling `images/` directory and link with relative paths.

## Build, Test, and Development Commands
The repository is Markdown-first and ships without a build pipeline. After writing, run an optional lint pass with `npx markdownlint "**/*.md"` from the project root to catch heading or spacing issues. Use `rg <keyword>` before creating new notes to avoid duplicate coverage and to reference related material.

## Coding Style & Naming Conventions
Title each document with a single `#` heading, followed by concise `##` sections. Favor bullet lists or tables for checklists and comparisons, and include command snippets in fenced code blocks, for example:
```bash
 docker ps
```

File names should match the primary topic in lowercase snake_case (for example, `database_normalization.md`), and directories should not nest deeper than two levels without discussion. Maintain consistent language per document (many existing notes are Korean); when mixing languages, add inline translations for key terminology.

## Testing Guidelines
Fact-check commands and code blocks by running them in a clean shell or container before publishing. When summarizing external sources, cite them at the end using Markdown links. Use the lint command above and preview the document in your Markdown viewer to confirm heading hierarchy and link integrity.

## Commit & Pull Request Guidelines
Follow the existing history by writing short, descriptive commit messages that summarize the main addition, e.g., `git commit -m "데이터베이스 설계 원칙"`. Combine related edits into a single commit when possible. Pull requests should list the affected directories, describe the motivation for the change, and mention any follow-up items or open questions. Include screenshots for visual updates and link to supporting issues or references so future readers can trace context quickly.
