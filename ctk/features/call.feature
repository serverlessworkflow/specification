Feature: Call Task
  As an implementer of the workflow DSL
  I want to ensure that call tasks can be executed within the workflow
  So that my implementation conforms to the expected behavior

  # Tests HTTP call using `content` output
  # Tests interpolated path parameters
  # Tests auto-deserialization when reading response with 'application/json' content type
  # Tests output filtering
  Scenario: Call HTTP With Content Output
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: http-call-with-content-output
    do:
      - getFirstAvailablePet:
          call: http
          with:
            method: get
            endpoint:
              uri: https://petstore.swagger.io/v2/pet/findByStatus?status={status}
          output:
            from: .[0]
    """
    And given the workflow input is:
    """yaml
    status: available
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have properties 'id', 'name', 'status'
    
  # Tests HTTP call using `response` output
  # Tests interpolated path parameters
  # Tests auto-deserialization when reading response with 'application/json' content type
  Scenario: Call HTTP With Response Output
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: http-call-with-response-output
    do:
      - getPetById:
          call: http
          with:
            method: get
            endpoint:
              uri: https://petstore.swagger.io/v2/pet/{petId}
            output: response
    """
    And given the workflow input is:
    """yaml
    petId: 1
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have properties 'request', 'request.method', 'request.uri', 'request.headers', 'headers', 'statusCode', 'content'
    And the workflow output should have properties 'content.id', 'content.name', 'content.status'

  # Tests HTTP call using `basic` authentication
  # Tests interpolated path parameters
  Scenario: Call HTTP Using Basic Authentication
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: http-call-with-basic-auth
    do:
      - getSecuredEndpoint:
          call: http
          with:
            method: get
            endpoint:
              uri: https://httpbin.org/basic-auth/{username}/{password}
              authentication:
                basic:
                  username: ${ .username }
                  password: ${ .password }
    """
    And given the workflow input is:
    """yaml
    username: serverless-workflow
    password: conformance-test
    """
    When the workflow is executed
    Then the workflow should complete

  # Tests OpenAPI call using `content` output
  # Tests output filtering
  Scenario: Call OpenAPI With Content Output
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: openapi-call-with-content-output
    do:
      - getPetsByStatus:
          call: openapi
          with:
            document:
              uri: https://petstore.swagger.io/v2/swagger.json
            operation: findPetsByStatus
            parameters:
              status: ${ .status }
          output:
            from: . | length
    """
    And given the workflow input is:
    """yaml
    status: available
    """
    When the workflow is executed
    Then the workflow should complete

  # Tests OpenAPI call using `response` output
  # Tests output filtering
  Scenario: Call OpenAPI With Response Output
    Given a workflow with definition:
    """yaml
    document:
      dsl: 1.0.0-alpha1
      namespace: default
      name: openapi-call-with-response-output
    do:
      - getPetById:
          call: openapi
          with:
            document:
              uri: https://petstore.swagger.io/v2/swagger.json
            operation: getPetById
            parameters:
              petId: ${ .petId }
            output: response
    """
    And given the workflow input is:
    """yaml
    petId: 1
    """
    When the workflow is executed
    Then the workflow should complete
    And the workflow output should have properties 'request', 'request.method', 'request.uri', 'request.headers', 'headers', 'statusCode', 'content'
    And the workflow output should have properties 'content.id', 'content.name', 'content.status'