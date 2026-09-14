package com.messatto.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> openApi() {
        return ResponseEntity.ok(OPEN_API);
    }

    @GetMapping(value = "/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> swaggerUi() {
        return ResponseEntity.ok(SWAGGER_UI);
    }

    private static final String SWAGGER_UI = """
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Messatto API Docs</title>
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css">
              </head>
              <body>
                <div id="swagger-ui"></div>
                <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>
                  window.onload = function() {
                    SwaggerUIBundle({ url: '/v3/api-docs', dom_id: '#swagger-ui' });
                  };
                </script>
              </body>
            </html>
            """;

    private static final String OPEN_API = """
            {
              "openapi": "3.0.1",
              "info": {
                "title": "Messatto API",
                "version": "v1",
                "description": "QR-based mess management backend for hostel meal check-ins, attendance, demand analytics, and verified feedback."
              },
              "servers": [
                { "url": "http://localhost:8080" }
              ],
              "components": {
                "securitySchemes": {
                  "bearerAuth": {
                    "type": "http",
                    "scheme": "bearer",
                    "bearerFormat": "JWT"
                  }
                }
              },
              "paths": {
                "/auth/login": {
                  "post": {
                    "tags": ["Authentication"],
                    "summary": "Login and receive a JWT access token",
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": { "email": "student@college.edu", "password": "Student@123" }
                        }
                      }
                    },
                    "responses": { "200": { "description": "Authenticated" }, "401": { "description": "Invalid credentials" } }
                  }
                },
                "/super-admin/users": {
                  "post": {
                    "tags": ["Super Admin"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Create a student, mess admin, or super admin user",
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": {
                            "name": "Student One",
                            "email": "student1@college.edu",
                            "rollNumber": "CS-001",
                            "password": "Student@123",
                            "role": "STUDENT",
                            "hostel": "Hostel A"
                          }
                        }
                      }
                    },
                    "responses": { "201": { "description": "User created" }, "409": { "description": "Duplicate email or roll number" } }
                  }
                },
                "/admin/meals": {
                  "post": {
                    "tags": ["Meals"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Create a meal",
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": {
                            "mealDate": "2026-06-19",
                            "mealType": "LUNCH",
                            "vegMenu": "Rice, dal, paneer, salad",
                            "nonVegMenu": "Rice, dal, chicken curry, salad",
                            "startTime": "12:30:00",
                            "endTime": "14:30:00"
                          }
                        }
                      }
                    },
                    "responses": { "201": { "description": "Meal created" } }
                  }
                },
                "/meals/today": {
                  "get": {
                    "tags": ["Meals"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "View today's meals",
                    "responses": { "200": { "description": "Meals for the current date" } }
                  }
                },
                "/admin/meals/{mealId}/qr": {
                  "post": {
                    "tags": ["QR"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Generate a short-lived QR token for VEG or NON_VEG",
                    "parameters": [
                      { "name": "mealId", "in": "path", "required": true, "schema": { "type": "string", "format": "uuid" } }
                    ],
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": { "mealChoice": "VEG", "ttlSeconds": 900 }
                        }
                      }
                    },
                    "responses": { "200": { "description": "QR token generated" } }
                  }
                },
                "/attendance/scan": {
                  "post": {
                    "tags": ["Attendance"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Scan QR and record attendance",
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": { "token": "qr-token-or-messatto-payload" }
                        }
                      }
                    },
                    "responses": { "200": { "description": "Attendance recorded or existing attendance returned" }, "409": { "description": "Different choice already recorded" } }
                  }
                },
                "/student/attendance": {
                  "get": {
                    "tags": ["Attendance"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "View student attendance history",
                    "responses": { "200": { "description": "Attendance history" } }
                  }
                },
                "/feedback": {
                  "post": {
                    "tags": ["Feedback"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Submit verified feedback after attendance",
                    "requestBody": {
                      "required": true,
                      "content": {
                        "application/json": {
                          "example": {
                            "mealId": "00000000-0000-0000-0000-000000000000",
                            "tasteRating": 4,
                            "hygieneRating": 5,
                            "quantityRating": 4,
                            "serviceRating": 5,
                            "comment": "Fresh food and fast service",
                            "anonymous": true
                          }
                        }
                      }
                    },
                    "responses": { "200": { "description": "Feedback submitted" }, "403": { "description": "Attendance required" }, "409": { "description": "Duplicate feedback" } }
                  }
                },
                "/admin/dashboard/live": {
                  "get": {
                    "tags": ["Admin"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "View live attendance counts",
                    "parameters": [
                      { "name": "date", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } },
                      { "name": "mealType", "in": "query", "required": false, "schema": { "type": "string", "enum": ["BREAKFAST", "LUNCH", "DINNER"] } }
                    ],
                    "responses": { "200": { "description": "Live dashboard counts" } }
                  }
                },
                "/admin/reports/daily": {
                  "get": {
                    "tags": ["Reports"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Export daily report as JSON or CSV",
                    "parameters": [
                      { "name": "date", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } },
                      { "name": "format", "in": "query", "required": false, "schema": { "type": "string", "enum": ["json", "csv"] } }
                    ],
                    "responses": { "200": { "description": "Daily report" } }
                  }
                },
                "/admin/reports/weekly": {
                  "get": {
                    "tags": ["Reports"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "Export weekly report as JSON",
                    "parameters": [
                      { "name": "startDate", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
                    ],
                    "responses": { "200": { "description": "Weekly report" } }
                  }
                },
                "/admin/feedback/summary": {
                  "get": {
                    "tags": ["Feedback"],
                    "security": [{ "bearerAuth": [] }],
                    "summary": "View aggregated feedback analytics",
                    "parameters": [
                      { "name": "mealId", "in": "query", "required": false, "schema": { "type": "string", "format": "uuid" } },
                      { "name": "date", "in": "query", "required": false, "schema": { "type": "string", "format": "date" } }
                    ],
                    "responses": { "200": { "description": "Feedback summary" } }
                  }
                }
              }
            }
            """;
}
