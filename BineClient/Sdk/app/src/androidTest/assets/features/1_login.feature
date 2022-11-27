Feature: Login

  @login-feature
  Scenario Outline: Input valid phone number to proceed
    Given I am on login screen
    When I input phone number <phone>
    And I press submit button
    Then I should see OTP Screen <phone>
    Examples:
      | phone |
      | 9999999999  |

  @login-feature
  Scenario Outline: Select Language
    Given I am on login screen
    When I want to change language
    And I select <language> and <submit>
    Then I should see <language>
    Examples:
      |language  | submit|
      |हिन्दी      | Ok    |
      |ಕನ್ನಡ      | ओके    |
      |English   | ಸರಿ   |

  @login-feature
  Scenario: Resend OTP Timer
    Given I am on OTP Screen
    Then I should see the resend OTP timer
    And Age consent option

  @login-feature
  Scenario: Resend OTP
    Given I am on OTP Screen
    When Two minutes have passed
    And I want to resend OTP
    Then Receive OTP resend confirmation

  @login-feature
  Scenario: Input OTP to proceed
    Given I am on OTP Screen
    When I enter OTP 123457
    And I confirm age consent
    And I press verify button
    Then I should see invalid OTP error

  @login-feature
  Scenario: Successful Phone and OTP validation
    Given I am on OTP Screen
    When I enter OTP 123456
    And I confirm age consent
    And I press verify button
    And I read terms and conditions
    And I accept terms and conditions to proceed
    And I accept permissions and proceed
    Then I should see Home Screen
