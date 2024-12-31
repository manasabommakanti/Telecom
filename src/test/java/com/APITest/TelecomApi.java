package com.APITest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class TelecomApi {
    private String token;
    private String contactId;
    private ExtentReports extent;
    private ExtentTest test;

    @BeforeClass
    public void setup() {
        // Set Base URI
        RestAssured.baseURI = "https://thinking-tester-contact-list.herokuapp.com";

        // Set up ExtentReports
        ExtentSparkReporter spark = new ExtentSparkReporter("ExtentReport.html");
        spark.config().setDocumentTitle("Telecom API Testing");
        spark.config().setReportName("Telecom API Test Cases");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @Test(priority = 1)
    public void testAddUser() {
        test = extent.createTest("Add User");
        Response response = given()
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"telecom_user_test@gmail.com\",\"password\":\"myPassword\"}")
                .post("/users");

        // Validate response
        assertEquals(response.statusCode(), 201);
        token = response.jsonPath().getString("token");
        test.pass("User added successfully, token generated: " + token);
    }

    @Test(priority = 2, dependsOnMethods = "testAddUser")
    public void testGetUserProfile() {
        test = extent.createTest("Get User Profile");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/users/me");

        // Validate response
        assertEquals(response.statusCode(), 200);
        test.pass("User profile fetched successfully");
    }
    
    @Test(priority = 3, dependsOnMethods = "testAddUser")
    public void testUpdateUser() {
        test = extent.createTest("Update User");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Updated\",\"lastName\":\"Username\",\"email\":\"telecom_updated_user@gmail.com\",\"password\":\"myNewPassword\"}")
                .patch("/users/me");

        assertEquals(response.statusCode(), 200);
        test.pass("User updated successfully");
    }

    @Test(priority = 4)
    public void testLoginUser() {
        test = extent.createTest("Login User");
        Response response = given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"telecom_updated_user@gmail.com\",\"password\":\"myNewPassword\"}")
                .post("/users/login");

        // Validate response
        assertEquals(response.statusCode(), 200);
        token = response.jsonPath().getString("token");
        test.pass("User logged in successfully, new token: " + token);
    }

    @Test(priority = 5)
    public void testAddContact() {
        test = extent.createTest("Add Contact");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthdate\":\"1970-01-01\",\"email\":\"jdoe@fake.com\",\"phone\":\"8005555555\",\"street1\":\"1 Main St.\",\"street2\":\"Apartment A\",\"city\":\"Anytown\",\"stateProvince\":\"KS\",\"postalCode\":\"12345\",\"country\":\"USA\"}")
                .post("/contacts");

        // Validate response
        assertEquals(response.statusCode(), 201);
        contactId = response.jsonPath().getString("_id");
        test.pass("Contact added successfully, contact ID: " + contactId);
    }

    @Test(priority = 6)
    public void testGetContactList() {
        test = extent.createTest("Get Contact List");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/contacts");

        // Validate response
        assertEquals(response.statusCode(), 200);
        test.pass("Contact list fetched successfully");
    }

    @Test(priority = 7, dependsOnMethods = "testAddContact")
    public void testGetContactById() {
        test = extent.createTest("Get Contact By ID");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/contacts/" + contactId);

        // Validate response
        assertEquals(response.statusCode(), 200);
        test.pass("Contact fetched successfully by ID");
    }

    @Test(priority = 8, dependsOnMethods = "testAddContact")
    public void testUpdateContact() {
        test = extent.createTest("Update Contact");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Amy\",\"lastName\":\"Miller\",\"birthdate\":\"1992-02-02\",\"email\":\"amiller@fake.com\",\"phone\":\"8005554242\",\"street1\":\"13 School St.\",\"street2\":\"Apt. 5\",\"city\":\"Washington\",\"stateProvince\":\"QC\",\"postalCode\":\"A1A1A1\",\"country\":\"Canada\"}")
                .put("/contacts/" + contactId);

        // Validate response
        assertEquals(response.statusCode(), 200);

        String email = response.jsonPath().getString("email");

        assertEquals(email, "amiller@fake.com");

        test.pass("Contact updated successfully");
    }

    @Test(priority = 9, dependsOnMethods = "testAddContact")
    public void testUpdatePartialContact() {
        test = extent.createTest("Update Partial Contact");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\"firstName\":\"Anna\"}")
                .patch("/contacts/" + contactId);

        // Validate response
        assertEquals(response.statusCode(), 200);

        String updatedFirstName = response.jsonPath().getString("firstName");
        assertEquals(updatedFirstName, "Anna");

        test.pass("Contact partially updated successfully");
    }

    @Test(priority = 10)
    public void testLogoutUser() {
        test = extent.createTest("Logout User");
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .post("/users/logout");

        // Validate response
        assertEquals(response.statusCode(), 200, "Failed to log out user");
        test.pass("User logged out successfully");
    }

    @AfterClass
    public void tearDown() {
        extent.flush(); // Generate the report
    }
}
