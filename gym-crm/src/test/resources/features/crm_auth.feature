@component @crm @auth
Feature: CRM - Authentication endpoints

  @component @positive
  Scenario: Login successfully with valid credentials
    Given a user "John.Doe" exists with password "password12"
    When I POST login with username "John.Doe" and password "password12"
    Then the CRM response status should be 200
    And the login response should contain a JWT token and a refresh token

  @component @negative
  Scenario: Login fails with invalid credentials
    Given login fails for username "John.Doe" and password "wrong-pass"
    When I POST login with username "John.Doe" and password "wrong-pass"
    Then the CRM response status should be 400
    And the CRM error message should contain "Invalid username or password"

  @component @negative
  Scenario: Login fails when username is missing
    When I POST login with username "" and password "some-pass"
    Then the CRM response status should be 400
    And the CRM validation error for "username" should be "Username is required"

  @component @positive
  Scenario: Refresh token successfully
    Given a valid refresh token "valid-refresh-token" exists
    When I POST refresh token with "valid-refresh-token"
    Then the CRM response status should be 200
    And the refresh response should contain a new JWT token

  @component @negative
  Scenario: Refresh token fails when token is not in database
    Given refresh token "invalid-token" is not in database
    When I POST refresh token with "invalid-token"
    Then the CRM response status should be 400
    And the CRM error message should contain "Refresh token is not in database!"
