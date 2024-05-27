Feature: Debug Task
  As an implementer of the workflow DSL
  I want to ensure that debug tasks can be executed within the workflow
  So that my implementation conforms to the expected behaviour

  # Tests Debug with literal constant
  Scenario: Debug with literal constant
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: debug
    do:
      printMessage:
        debug: Hello world!
    """
    And given the workflow input is:
    """yaml
    """
    When the workflow is executed
    Then the workflow should complete

  # Tests Debug with expression
  Scenario: Debug with expression
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: debug
    do:
      printMessage:
        debug: ${Hello \(.name)!} 
    """
    And given the workflow input is:
    """yaml
      name: John
    """
    When the workflow is executed
    Then the workflow should complete

