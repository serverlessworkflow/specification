Feature: Branch Task
  As an implementer of the workflow DSL
  I want to ensure that branch tasks can be executed within the workflow
  So that my implementation conforms to the expected behavior

  # Tests branch tasks With competing concurrent sub tasks
  Scenario: Branch Task With Competing Concurrent Sub Tasks
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: branch-task-with-compete
    do:
      - branchWithCompete:
          branch:
            compete: true
            on:
              - setRed:
                  set:
                    colors: ${ .colors + ["red"] }
              - setGreen:
                  set:
                    colors: ${ .colors + ["green"] }
              - setBlue:
                  set:
                    colors: ${ .colors + ["blue"] }
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have a 'colors' property containing 1 items