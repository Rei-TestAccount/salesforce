# Prodly QA Automation Assignment

## Overview
This project implements an automated QA assignment using **Java**, **Rest Assured**, and **Cucumber (BDD)**.

It performs a complete end-to-end automation flow between **Salesforce** and **GitHub**, including:

1. Creating and inserting Account records into Salesforce via REST API
2. Exporting Salesforce data to GitHub as a CSV file
3. Creating a new GitHub branch and adding phone numbers to the CSV
4. Updating Salesforce records with data from GitHub
5. Executing both positive and negative test scenarios with cleanup

---

## Tech Stack
- **Java 11**
- **Maven**
- **Rest Assured** – API testing
- **Cucumber (BDD)** – behavior-driven test design
- **SLF4J + Logback** – structured logging
- **Apache Commons CSV** – CSV parsing and generation

---

## Environment Setup

Create a `.env` file in the project root using the provided `.env.example`:

SF_INSTANCE_URL=https://your-instance.my.salesforce.com
SF_CLIENT_ID=your_salesforce_connected_app_consumer_key_here
SF_CLIENT_SECRET=your_salesforce_connected_app_consumer_secret_here

GITHUB_OWNER=your_github_username_here
GITHUB_REPO=your_repository_name_here
GITHUB_TOKEN=your_github_personal_access_token_here

HTTP_TIMEOUT_MS=15000


Make sure your GitHub repository and token have permissions to:
- Read/write repository contents
- Create and delete branches

---

## Running Tests

1. Ensure you have Java 11+ and Maven installed
2. Open a terminal in the project root directory
3. Run the tests:

   ```bash
   mvn test

## During execution:

- Accounts are created in Salesforce
- Data is exported to GitHub (CSV committed to master)
- A new branch is created and modified
- Salesforce records are updated based on GitHub data
- Temporary data is cleaned up automatically

## Business Rules Implemented

- If Country = "US", then NumberOfEmployees > 100
- Each (Name + Country) combination is unique
- Negative test simulates an update failure with an invalid record ID to demonstrate graceful error handling

## Test Output

Each test run produces:

### CSV file:
data-autotest/accounts-<RUN_ID>-<timestamp>.csv

### Temporary GitHub branch:
phones-<RUN_ID>-<timestamp>

All Salesforce records, CSV files, and temporary branches are automatically deleted after test execution.

# Notes

- ### Do not commit your .env file to GitHub.

- The framework uses unique RUN_ID and timestamps to avoid collisions between runs.
- The automation handles both positive and negative scenarios as required by the assignment.
