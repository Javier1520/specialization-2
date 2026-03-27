@component @crm @user
Feature: CRM - User registration endpoints

  @component @positive
  Scenario: Register a new trainee successfully
    When I POST register trainee with first name "John" and last name "Doe"
    Then the CRM response status should be 201
    And the registration response should contain a username starting with "John.Doe"
    And the registration response should contain a password with 10 characters

  @component @negative
  Scenario: Register a trainee fails when first name is missing
    When I POST register trainee with first name "" and last name "Doe"
    Then the CRM response status should be 400
    And the CRM validation error for "firstName" should be "First name is required"

  @component @positive
  Scenario: Register a new trainer successfully
    When I POST register trainer with first name "Jane" and last name "Smith" and specialization "YOGA"
    Then the CRM response status should be 201
    And the registration response should contain a username starting with "Jane.Smith"
    And the registration response should contain a password with 10 characters
