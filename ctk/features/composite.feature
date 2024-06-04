Feature: Composite Task
  As an implementer of the workflow DSL
  I want to ensure that composite tasks can be executed within the workflow
  So that my implementation conforms to the expected behavior

  # Tests composite tasks with sequential sub tasks
  Scenario: Composite Task With Sequential Sub Tasks
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: composite-sequential
    do:
      - setRGB:
          execute:
            sequentially:
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
    Then the workflow should complete with output:
    """yaml
    colors: [ red, green, blue ]
    """

  # Tests composite tasks With competing concurrent sub tasks
  Scenario: Composite Task With Competing Concurrent Sub Tasks
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: composite-sequential
    do:
      - setRGB:
          execute:
            concurrently:
              - setRed:
                  set:
                    colors: ${ .colors + ["red"] }
              - setGreen:
                  set:
                    colors: ${ .colors + ["green"] }
              - setBlue:
                  set:
                    colors: ${ .colors + ["blue"] }
            compete: true
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have a 'colors' property containing 1 items