# Development Guidelines

## 1. Core Principles

All code generated or modified by the agent must follow these principles:

- **Readability over cleverness:** Prefer straightforward, explicit implementations over clever or overly concise code.

- **Simplicity over abstraction:** Do not introduce unnecessary abstractions, design patterns, or architectural layers.

- **Consistency over personal preference:** Follow the existing project conventions and architecture.

- **Maintainability over short-term convenience:** Write code that another developer can easily understand, review, and modify.

- **Minimal changes:** Only modify files necessary to implement the requested feature. Avoid unrelated refactoring.

## 2. Code Quality & Readability

### 2.1 Code Structure

- Each class, function, and component should have a clear and well-defined responsibility.

- Avoid large classes or functions that handle multiple unrelated concerns.

- Prefer small, cohesive functions with descriptive names.

- As a guideline, keep functions within approximately 40 lines when practical. Do not split functions artificially just to satisfy this limit.

- Avoid deeply nested conditionals. Prefer early returns and guard clauses.

- Keep UI rendering, business logic, and data access separated.

- Avoid unnecessary inheritance and excessive dependency injection.

### 2.2 Naming Conventions

- Use descriptive names for classes, functions, variables, and parameters.

- Avoid ambiguous abbreviations and meaningless names such as `data`, `manager`, `helper`, or `util` unless their purpose is genuinely clear.

- Use consistent naming conventions throughout the project.

- Boolean variables should use meaningful prefixes such as `is`, `has`, or `can`.

### 2.3 Code Formatting

- Use consistent indentation and formatting.

- Break long expressions and function calls into multiple lines when doing so improves readability.

- Avoid excessively compact code and multiple unrelated statements on one line.

- Remove unused imports, variables, and unreachable code.

- Avoid unnecessary comments that merely repeat what the code already expresses.

- Add comments to explain non-obvious decisions, algorithms, and important edge cases.

### 2.4 Complexity Management

- Avoid premature optimization.

- Avoid introducing generic frameworks for features that only require a simple implementation.

- Extract reusable components when there is genuine duplication or a clear reuse opportunity.

- Avoid both excessive code duplication and excessive abstraction.

- Do not create additional files or classes without a clear responsibility.

## 3. Android Architecture Guidelines

- Follow the existing Android project architecture and technology stack.

- Keep Activities and Fragments lightweight.

- Keep business logic out of UI rendering code.

- Use ViewModel or an equivalent state-management approach when screen state and business logic become sufficiently complex.

- Separate data access from presentation logic.

- Use lifecycle-aware mechanisms for asynchronous operations.

- Avoid blocking the main thread.

- Avoid GlobalScope and unmanaged background tasks.

- Handle loading, success, empty, and error states explicitly.

- Handle configuration changes and lifecycle events appropriately.

- Avoid introducing architectural layers that do not provide meaningful benefits.

### Dependency Management

- Prefer existing project dependencies and Android platform APIs.

- Do not introduce a new library or framework without a clear justification.

- Avoid unnecessary dependency upgrades.

- Do not modify Gradle configuration, SDK versions, application permissions, or the Android manifest unless required by the task.

- Explain the reason and impact of dependency or configuration changes.

## 4. UI/UX Design Standards

The application should have a polished, consistent, and intentional visual design.

### 4.1 Visual Consistency

- Establish a consistent design system for colors, typography, spacing, shapes, and component styles.

- Avoid arbitrary colors, dimensions, and typography values scattered throughout the codebase.

- Prefer centralized theme definitions and Android resources.

- Use a consistent spacing scale, preferably based on multiples of 4dp or 8dp.

- Maintain consistent alignment, margins, padding, and corner radii.

- Use whitespace intentionally to establish visual hierarchy.

### 4.2 Visual Quality

- Prioritize clean, modern, and minimal interfaces.

- Establish a clear hierarchy between page titles, section headings, body text, and secondary information.

- Avoid overcrowded layouts and excessive decorative elements.

- Avoid excessive gradients, shadows, borders, and animations.

- Use color contrast to communicate hierarchy and importance.

- Ensure icons are visually consistent in style, size, and alignment.

- Avoid mixing unrelated visual styles or default component appearances.

### 4.3 Component Design

- Reuse UI components when they serve a genuine common purpose.

- Keep reusable components independent of unnecessary screen-specific logic.

- Avoid creating generic components with excessive configuration parameters.

- Ensure interactive components provide clear visual feedback.

- Use appropriate loading indicators, empty states, error messages, and confirmation feedback.

### 4.4 Accessibility & Responsiveness

- Ensure interactive elements have sufficiently large touch targets (preferably at least 48dp).

- Provide meaningful content descriptions for relevant icons and images.

- Avoid text clipping, overlapping elements, and horizontal overflow.

- Consider different screen sizes and aspect ratios.

- Handle the soft keyboard and system status/navigation bars correctly.

- Maintain sufficient text/background contrast.

- Do not rely exclusively on color to communicate information.

## 5. Implementation Workflow

Before implementing a feature:

1. Inspect the existing project structure and relevant source files.

2. Understand the current architecture, naming conventions, and UI design.

3. Identify the minimum set of files that need to be modified.

4. Consider potential side effects and compatibility issues.

During implementation:

1. Keep changes focused on the requested feature.

2. Follow existing conventions unless there is a compelling reason to change them.

3. Avoid unrelated refactoring and formatting changes.

4. Do not replace existing implementations without understanding their purpose.

5. Do not introduce mock implementations, placeholder logic, or fake data into production code unless explicitly requested.

After implementation:

1. Review the modified code for readability and unnecessary complexity.

2. Check for unused imports, duplicated logic, and potential runtime errors.

3. Run relevant tests and build checks when possible.

4. Verify that existing functionality has not been unintentionally affected.

5. If UI changes are involved, inspect the rendered interface when an emulator or screenshot workflow is available.

## 6. Testing & Verification

- Add or update unit tests for non-trivial business logic.

- Test important edge cases and failure scenarios.

- Avoid tests that depend unnecessarily on external services or network availability.

- Run relevant Gradle tests and build tasks when possible.

- Do not claim that code has been tested if the corresponding tests or build commands were not executed.

- Clearly report any remaining issues or unverified behavior.

## 7. Code Review Standards

Before considering a task complete, verify:

- Is the implementation easy to understand without extensive explanation?

- Does each class or function have a clear responsibility?

- Is the implementation more complicated than necessary?

- Are there unnecessary dependencies or abstractions?

- Are there unrelated changes in the diff?

- Are error cases and lifecycle issues handled appropriately?

- Does the UI follow the established design system?

- Are there any obvious performance, memory, or resource-management problems?

The final implementation should be easy for another developer to review.

## 8. Git Workflow & Version Control

Git must be used throughout the development process to maintain a clear and traceable history of each experiment.

### 8.1 Commit Requirements

- Every completed experiment task must result in a Git commit.
- Each experiment task should have its own independent commit.
- The implementation code and its corresponding documentation should normally be included in the same commit.
- Do not accumulate multiple completed experiment tasks into one large commit.
- Do not create commits for incomplete or broken implementations.

### 8.2 Before Committing

Before creating a commit:

1. Inspect `git status` to understand all modified and untracked files.
2. Review the changes using `git diff`.
3. Check for accidental modifications, debugging code, unused files, and sensitive information.
4. Run relevant build checks and tests whenever possible.
5. Ensure the documentation for the current task has been created or updated.
6. Stage only files related to the current experiment task.

Never blindly stage unrelated changes or overwrite existing user modifications.

### 8.3 Commit Message Convention

Use clear and descriptive commit messages.

Follow this format:

`<type>(<task-id>): <short description>`

Common types:

- `feat`: New functionality or experiment implementation.
- `fix`: Bug fixes.
- `refactor`: Code restructuring without changing functionality.
- `docs`: Documentation-only changes.
- `test`: Adding or modifying tests.
- `chore`: Build configuration and maintenance.

Examples:

- `feat(task-01): implement user authentication`
- `feat(task-02): add local database support`
- `fix(task-02): handle database initialization errors`
- `docs(task-01): explain authentication workflow`

### 8.4 Git Safety

- Never use destructive Git commands such as `git reset --hard` or force push without explicit permission.
- Never amend, rewrite, or delete existing commits without explicit permission.
- Do not commit passwords, API keys, private credentials, or local environment configuration.
- Do not push to a remote repository unless explicitly requested.
- Never modify the Git identity configuration without permission.

### 8.5 After Committing

After each completed task:

- Verify that the commit was successfully created.
- Report the commit hash and commit message.
- Summarize the files included in the commit.
- Report the working tree status.
- Clearly disclose any tests or build checks that could not be executed.

A task is not considered fully completed until its implementation, documentation, and Git commit have all been addressed.

## 9. Learning-Oriented Documentation

Every experiment task must have a corresponding Markdown documentation file under the `docs/` directory.

The documentation serves two purposes:

1. Record the implementation and technical decisions of each experiment.

2. Help the developer understand the Android concepts, programming techniques, and code involved.

The documentation must be written for a beginner with little Android experience and a weak Java foundation. For each experiment with implementation code, write a walkthrough that teaches the feature through real excerpts from the repository.

Use the existing walkthroughs for [Android migration](docs/tasks/task-02-android-migration.md), [navigation and audio](docs/tasks/task-03-interface-and-audio.md), and [ranking and SQLite](docs/tasks/task-04-rank-and-sqlite.md) as examples of the expected depth.

### 9.1 Documentation Organization

Use the following directory structure:

docs/

├── README.md

└── tasks/

├── task-01-xxx.md

├── task-02-xxx.md

├── task-03-xxx.md

└── ...

Requirements:

- Create one Markdown document for each experiment task.

- Use descriptive filenames containing the task ID and topic.

- Maintain `docs/README.md` as an index of all experiment documents.

- Keep the documentation consistent in structure and terminology. Use repository-relative links to the source files discussed so they work in both a local checkout and on GitHub.

- Update existing documentation when an experiment is modified or corrected.

### 9.2 Documentation Content

Each experiment document should contain the following sections when relevant:

#### 1. Task Overview

- What does this experiment require?

- What functionality are we implementing?

- What is the expected result?

#### 2. Key Concepts

Explain the important concepts needed to understand the implementation.

For Android-specific concepts:

- Explain what the concept is.

- Explain why Android needs it.

- Explain its role in the current experiment.

- Provide a simple example when necessary.

Do not assume prior knowledge of Android components, lifecycle management, UI frameworks, or Android APIs.

For Java or Kotlin concepts:

- Explain unfamiliar syntax and programming constructs.

- When useful, relate unfamiliar Kotlin syntax to equivalent Java concepts.

- Explain the Java or Kotlin constructs the reader needs when they first appear in a code excerpt. This can include inheritance, constructors, interfaces, generics, lambdas, method references, callbacks, and exception handling.
- Tie each explanation to the actual statement and its role in the feature. For example, explain who supplies a button listener's lambda parameter and when the body runs.

Do not explain basic syntax that is already obvious or unrelated to the experiment.

#### 3. Implementation Overview

Explain the overall implementation before discussing individual files.

- Start with a concrete user action or input, then trace it to the observable result.

- Explain how the main components interact and identify the methods involved.

- Introduce the responsibilities of important classes and files before stepping through their code. Identify any handoff between the UI thread, a worker thread, or storage when it affects the flow.

- Use a simple diagram when it genuinely helps explain the architecture or data flow.

#### 4. Key Code Walkthrough

Use short, focused fenced code blocks to teach the main steps of the feature. Every experiment with implementation code must include real source excerpts for its key behavior.

- Select excerpts from the current source and give each block its language, such as `java`, `kotlin`, or `xml`. Link to the source file and name the class, method, or layout the reader should open.
- Keep each excerpt small enough to explain one step. Mark any omitted lines and identify the revision if a snippet comes from a historical commit. Label any supplementary pseudocode or simplified sample clearly.
- Explain who calls the code, when it runs, where the inputs come from, and what it returns or changes. Explain important conditions, error paths, and Android API calls when they affect the result.
- Break the explanation into meaningful groups of statements. Show why the code is placed there and how its result is used by the next step.
- Include a worked example with values or steps that match the implementation. For example, trace one touch point through coordinate conversion, one menu selection through an Intent, or one score through insertion and list refresh.

Keep the surrounding detail proportional to the step. The reader should be able to follow the excerpt and use the link to see the complete method.

#### 5. Running and Verification

Provide practical instructions for verifying the implementation.

- Explain how to launch and run the application.

- Describe the expected UI behavior or output.

- Explain how to verify that the functionality works correctly.

- Include relevant Gradle commands when appropriate.

- Include relevant edge cases with their expected results. Distinguish the checks already performed from steps the reader can run and state any remaining verification gaps.

#### 6. Common Issues and Troubleshooting

Record important problems encountered during implementation.

For each issue, explain:

- What the problem looks like.

- Why it happens.

- How it was resolved.

Only include relevant issues rather than inventing hypothetical problems.

#### 7. Key Takeaways

Summarize the most important knowledge gained from the experiment.

The reader should be able to review this section later and recall:

- What was implemented.

- Which Android concepts were involved.

- What programming techniques were learned.

- What knowledge can be reused in future experiments.

### 9.3 Writing Style

The documentation must balance conciseness and clarity.

- Write in clear, natural Chinese.

- Keep explanations compact but sufficiently detailed.

- Assume the reader is unfamiliar with Android development.

- Introduce technical terms before relying on them.

- Explain the reasoning behind the implementation and show how each code excerpt moves the feature forward.

- Prefer concrete examples over abstract descriptions.

- Use Markdown headings, lists, tables, and code blocks to organize information.

- Use diagrams only when they improve understanding.

- Avoid unnecessary background theory and lengthy textbook-style explanations.

- Avoid overly brief statements that leave the reader unable to understand the underlying mechanism.

The goal is not to maximize documentation length, but to maximize the amount of useful knowledge a beginner can acquire from reading it.

### 9.4 Documentation Quality Requirements

Before completing a task, review its documentation:

- Can a beginner understand the purpose of the experiment?

- Can the reader explain the overall execution flow?

- Can the reader understand the important code without repeatedly asking the agent?

- Are unfamiliar Android and Java/Kotlin concepts explained?

- Do the code excerpts match the linked source, and do all source links resolve?
- Can the reader follow one concrete example from input through the key code to its visible or stored result?
- Are implementation details, expected results, and verification claims consistent with the actual project?
- Can the reader independently reproduce and verify the experiment?

When changing an experiment, update affected excerpts, explanations, and verification steps in the same task.

Never generate documentation that describes code behavior inaccurately or claims functionality that has not been implemented.

## 10. Team Collaboration & Git Branch Management

The project has two long-lived branches:

- `main`: The primary development and integration branch, maintained by the repository owner and their AI agent.

- `cui`: A dedicated development branch for the teammate.

Do not create additional development branches unless explicitly requested.

### 10.1 Main Branch

- The repository owner and their AI agent work directly on `main`.

- All regular development tasks are implemented and committed on `main`.

- The repository owner is responsible for maintaining the overall project.

- The agent must not switch away from `main` unless explicitly instructed.

### 10.2 Teammate Branch

- `cui` is reserved exclusively for the teammate's development tasks.

- Create `cui` from the latest `main` if it does not already exist.

- When the repository owner delegates a new task to the teammate, synchronize `cui` with the latest `main` before development begins.

- The teammate works independently on `cui`.

- The teammate must not directly modify `main`.

### 10.3 Synchronization Before Development

Before assigning a new task to the teammate:

1. Ensure `main` contains the latest accepted changes.

2. Fetch the latest remote repository state.

3. Switch to `cui`.

4. Merge the latest `main` into `cui`.

5. Resolve any existing conflicts carefully.

6. Push the synchronized `cui` branch to the remote repository.

7. Confirm that the teammate can start development from the updated branch.

Example:

`git fetch origin`

`git switch cui`

`git merge main`

`git push origin cui`

Do not discard existing commits or uncommitted changes on `cui` during synchronization.

### 10.4 Teammate Development Requirements

The teammate must:

- Implement the assigned experiment on `cui`.

- Follow all existing code quality and Android architecture guidelines.

- Create a separate Git commit for each completed experiment task.

- Create or update the corresponding Markdown documentation under `docs/tasks/`.

- Include implementation code and documentation in the corresponding task commit.

- Use descriptive commit messages.

- Push completed changes to the remote `cui` branch.
