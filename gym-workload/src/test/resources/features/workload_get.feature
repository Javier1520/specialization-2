@component @workload
Feature: Workload - Get trainer workload

  @negative @auth
  Scenario: Unauthorized access is rejected
    When I GET Workload service workload for username "any" without token
    Then the Workload response status should be 403

  @negative
  Scenario: Missing trainer returns 404
    Given no trainer exists with username "missing"
    When I GET Workload service workload for username "missing" with a valid token
    Then the Workload response status should be 404
    And the Workload error message should contain "Trainer not found"

