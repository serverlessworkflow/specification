Feature: Raise Task
  As an implementer of the workflow DSL
  I want to ensure that the Raise task behaves correctly
  So that my implementation conforms to the expected behavior

  Scenario: Raise task with inline error
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: raise-custom-error
    do:
      - raiseComplianceError:
          raise:
            error:
              status: 400
              type: https://serverlessworkflow.io/errors/types/compliance
              title: Compliance Error
    """
    When the workflow is executed
    Then the workflow should fault with error:
    """yaml
    status: 400
    type: https://serverlessworkflow.io/errors/types/compliance
    title: Compliance Error
    instance: /do/raiseComplianceError
    """
