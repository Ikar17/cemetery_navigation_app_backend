# How to run the project
  - Clone the repository: git clone https://github.com/Ikar17/cemetery_navigation_app_backend.git
  - Create a new database named "navigation_app" in PostgreSQL
  - Build and run the application from your IDE (e.g. IntelliJ)

  The application will be available at http://localhost:8080.


 # API Endpoints
   - POST http://localhost:8080/auth/signin
       - Endpoint to authenticate existing user. You have to send a object with email and password fields.
         For example: 
         {
           "email" : "john@gmail.com",
           "password" : "12345"
         }
         
   - POST http://localhost:8080/auth/signup
       - Endpoint to register a new user. You have to send a object with first name, last name, email and password fields.
         For example: 
         {
           "firstName": "John",
           "lastName" : "Smith", 
           "email" : "john@gmail.com",
           "password" : "12345"
         }
