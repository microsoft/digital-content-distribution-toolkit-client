Feature: TnC

  @tnc-feature
  Scenario: I should not be allowed to accept without reading Terms and Conditions
    Given I am on Terms and Conditions screen
    Then I should not able to accept before reading

  @tnc-feature
  Scenario: I should be allowed to reject
    Given I am on Terms and Conditions screen
    When I read conditions until bottom
    And I opt to reject the conditions
    Then I should be shown a prompt to terminate

  @tnc-feature
  Scenario: I should be allowed to accept and proceed
    Given I am on Terms and Conditions screen
    When I read conditions until bottom
    Then I should be able to accept and proceed