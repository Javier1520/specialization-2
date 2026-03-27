@component @crm @training
Feature: CRM - Training endpoints component and integration

  @component @negative
  Scenario: addTraining returns 400 when trainer username is missing
    When I POST addTraining without trainer username
    Then the CRM response status should be 400
    And the CRM validation error for "trainerUsername" should be "Trainer username is required"

  @component @negative
  Scenario: deleteTraining returns 400 when trainee username is missing
    When I DELETE training without trainee username
    Then the CRM response status should be 400
    And the CRM validation error for "traineeUsername" should be "Trainee username is required"

  @integration @positive
  Scenario: addTraining returns 201 when request is valid and action type is ADD
    Given a valid training context exists for trainee "john.trainee" and trainer "mary.trainer"
    When I POST addTraining with valid action type ADD
    Then the CRM response status should be 201

  @integration @positive
  Scenario: deleteTraining returns 200 when request is valid and action type is DELETE
    Given an existing training context exists for delete of trainee "john.trainee" and trainer "mary.trainer"
    When I DELETE training with valid action type DELETE
    Then the CRM response status should be 200

  @integration @negative
  Scenario: getTrainerWorkload returns 404 when trainer does not exist
    Given workload lookup fails with trainer not found for "bv.trainer"
    When I GET CRM workload for username "bv.trainer"
    Then the CRM response status should be 404
    And the CRM error message should contain "Trainer not found: bv.trainer"

  @integration @positive
  Scenario: getTrainerWorkload returns 200 when trainer exists
    Given workload lookup succeeds for trainer "mary.trainer"
    When I GET CRM workload for username "mary.trainer"
    Then the CRM response status should be 200

