Feature: Try Task
  As an implementer of the workflow DSL
  I want to ensure that try tasks can be executed within the workflow
  So that my implementation conforms to the expected behavior

  # Tests that try tasks complete when catching errors, and execute the defined handler task, if any
  # Tests simple uri interpolation
  # Tests custom error variable name
  # Tests error instance path
  Scenario: Try Handle Caught Error
    Given a workflow with definition:
    """yaml
    document:
      dsl: '1.0.0'
      namespace: default
      name: try-catch-404
      version: '1.0.0'
    do:
      - tryGetPet:
          try:
            - getPet:
                call: http
                with:
                  method: get
                  endpoint:
                    uri: https://petstore.swagger.io/v2/pet/getPetByName/{petName}
          catch:
            errors:
              with:
                type: https://serverlessworkflow.io/dsl/errors/types/communication
                status: 404
            as: err
            do:
              - setError:
                  set:
                    error: ${ $err }
    """
    And given the workflow input is:
    """yaml
    petName: Milou
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have properties 'error', 'error.type', 'error.status', 'error.title'
    And the workflow output should have a 'error.instance' property with value:
    """yaml
    /do/0/tryGetPet/try/0/getPet
    """

  # Tests that try tasks fault when an uncaught error is raised
  # Tests simple uri interpolation
  # Tests custom error variable name
  # Tests error instance path
  Scenario: Try Raise Uncaught Error
    Given a workflow with definition:
    """yaml
    document:
      dsl: '1.0.0'
      namespace: default
      name: try-catch-503
      version: '1.0.0'
    do:
      - tryGetPet:
          try:
            - getPet:
                call: http
                with:
                  method: get
                  endpoint:
                    uri: https://petstore.swagger.io/v2/pet/getPetByName/{petName}
          catch:
            errors:
              with:
                type: https://serverlessworkflow.io/dsl/errors/types/communication
                status: 503
            as: err
            do:
              - setError:
                  set:
                    error: ${ $err }
    """
    And given the workflow input is:
    """yaml
    petName: Milou
    """
    When the workflow is executed
    Then the workflow should fault

  # Tests task-level catch functionality
  # Tests error handling without try block
  # Tests custom error variable name
  # Tests error instance path
  Scenario: Task Level Catch Handle Error
    Given a workflow with definition:
    """yaml
    document:
      dsl: '1.0.0'
      namespace: default
      name: task-level-catch
      version: '1.0.0'
    do:
      - getPet:
          call: http
          with:
            method: get
            endpoint:
              uri: https://petstore.swagger.io/v2/pet/getPetByName/{petName}
          catch:
            errors:
              with:
                type: https://serverlessworkflow.io/dsl/errors/types/communication
                status: 404
            as: taskError
            do:
              - handleError:
                  set:
                    error: ${ $taskError }
    """
    And given the workflow input is:
    """yaml
    petName: Milou
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have properties 'error', 'error.type', 'error.status', 'error.title'
    And the workflow output should have a 'error.instance' property with value:
    """yaml
    /do/0/getPet
    """

  # Tests task-level catch with retry
  # Tests retry policy configuration
  # Tests error handling
  Scenario: Task Level Catch With Retry
    Given a workflow with definition:
    """yaml
    document:
      dsl: '1.0.0'
      namespace: default
      name: task-level-catch-retry
      version: '1.0.0'
    do:
      - getPet:
          call: http
          with:
            method: get
            endpoint:
              uri: https://petstore.swagger.io/v2/pet/getPetByName/{petName}
          catch:
            errors:
              with:
                type: https://serverlessworkflow.io/dsl/errors/types/communication
                status: 503
            retry:
              delay:
                seconds: 2
              backoff:
                exponential: {}
              limit:
                attempt:
                  count: 3
            do:
              - handleError:
                  set:
                    status: "service_unavailable"
    """
    And given the workflow input is:
    """yaml
    petName: Milou
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have a 'status' property with value 'service_unavailable'

  # Tests task-level catch with conditional handling
  # Tests when condition in catch
  # Tests error variable access
  Scenario: Task Level Catch With Condition
    Given a workflow with definition:
    """yaml
    document:
      dsl: '1.0.0'
      namespace: default
      name: task-level-catch-condition
      version: '1.0.0'
    do:
      - getPet:
          call: http
          with:
            method: get
            endpoint:
              uri: https://petstore.swagger.io/v2/pet/getPetByName/{petName}
          catch:
            errors:
              with:
                type: https://serverlessworkflow.io/dsl/errors/types/communication
            when: $error.status == 404
            as: notFoundError
            do:
              - handleNotFound:
                  set:
                    message: "Pet not found"
    """
    And given the workflow input is:
    """yaml
    petName: Milou
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have a 'message' property with value 'Pet not found'