## Agile Methodology: Kanban Implementation

For this project, we're adopting a **Kanban** methodology to manage workflow and enhance team collaboration, using **GitHub Projects** as our primary tool.

Our Kanban board in GitHub Projects will visually represent the workflow, broken down into the following key stages:

* **To Do:** This column will house all identified user stories and tasks that are yet to be started.  Each card will clearly define the task and its acceptance criteria. 
* **In Progress:** When a team member starts working on a task, its corresponding card will be moved to this column. We will aim to limit the number of items in this stage to maintain focus and prevent bottlenecks.
* **In Review:** Once a task is completed, it moves here for peer review or verification. This is crucial for ensuring quality and adherence to the GitHub Flow branching strategy, where changes are reviewed via pull requests before merging into the main branch.
* **Done:** Tasks that have been successfully reviewed, merged, and meet all acceptance criteria will be moved to this column.

To ensure continuous progress and timely feedback, we'll conduct **two 1-week review cycles**. While Kanban is a continuous flow system, these weekly checkpoints will serve as mini-iterations allowing the team to:
1.  Review the work completed in the "Done" column.
2.  Discuss any impediments or challenges encountered.
3.  Refine and prioritize tasks in the "To Do" column for the upcoming week.

The **GitHub Flow** branching strategy will be integral to our Kanban process. Each task or user story taken from "To Do" will involve creating a feature branch from `main`. Once development is complete and passes local checks (including any unit tests), a pull request will be opened. This pull request triggers the move to the "In Review" stage on our Kanban board. After review and approval, the feature branch will be merged into `main`, and the card moved to "Done." This ensures that `main` always reflects a deployable state.

