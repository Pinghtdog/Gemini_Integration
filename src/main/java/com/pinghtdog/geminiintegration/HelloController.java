package com.pinghtdog.geminiintegration;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.awt.event.ActionEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration; // For timeout
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class HelloController {
    @FXML
    private TextArea promptInput;

    @FXML
    private Button submitButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea resultArea;

    @FXML
    public void handleSubmitButtonAction(ActionEvent event) {
        String prompt = promptInput.getText();
        if (prompt == null || prompt.trim().isEmpty()) {
            statusLabel.setText("Status: Please enter a prompt.");
            return;
        }

        // Clear previous results and update status
        resultArea.clear();
        statusLabel.setText("Status: Sending request...");
        submitButton.setDisable(true); // Disable button during request

        // --- Create and run the background task for the API call ---
        Task<String> geminiTask = createGeminiApiTask(prompt);

        geminiTask.setOnSucceeded(workerStateEvent -> {
            resultArea.setText(geminiTask.getValue());
            statusLabel.setText("Status: Success!");
            submitButton.setDisable(false); // Re-enable button
        });

        geminiTask.setOnFailed(workerStateEvent -> {
            Throwable error = geminiTask.getException();
            resultArea.setText("Error calling API:\n" + error.getMessage());
            statusLabel.setText("Status: Error!");
            submitButton.setDisable(false); // Re-enable button
            error.printStackTrace(); // Print stack trace for debugging
        });

        // Start the task on a new thread
        new Thread(geminiTask).start();
    }

    public void handleSubmitButtonAction(javafx.event.ActionEvent actionEvent) {
        String prompt = promptInput.getText();
        if (prompt == null || prompt.trim().isEmpty()) {
            statusLabel.setText("Status: Please enter a prompt.");
            return;
        }

        // Clear previous results and update status
        resultArea.clear();
        statusLabel.setText("Status: Sending request...");
        submitButton.setDisable(true); // Disable button during request

        // --- Create and run the background task for the API call ---
        Task<String> geminiTask = createGeminiApiTask(prompt);

        geminiTask.setOnSucceeded(workerStateEvent -> {
            resultArea.setText(geminiTask.getValue());
            statusLabel.setText("Status: Success!");
            submitButton.setDisable(false); // Re-enable button
        });

        geminiTask.setOnFailed(workerStateEvent -> {
            Throwable error = geminiTask.getException();
            resultArea.setText("Error calling API:\n" + error.getMessage());
            statusLabel.setText("Status: Error!");
            submitButton.setDisable(false); // Re-enable button
            error.printStackTrace(); // Print stack trace for debugging
        });

        // Start the task on a new thread
        new Thread(geminiTask).start();
    }

    private Task<String> createGeminiApiTask(String prompt) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                // --- Get the NEW, SECURE API Key ---
                // IMPORTANT: Load securely (e.g., environment variable)
                // Do NOT hardcode it here!
                String apiKey = System.getenv("MY_TEST_GEMINI_API_KEY");
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    throw new RuntimeException("API Key not found. Set MY_TEST_GEMINI_API_KEY environment variable.");
                }

                // --- Placeholder for actual API Call Logic ---
                // Replace this section with the HttpClient code from the previous example
                updateMessage("Calling Gemini API..."); // status update
                System.out.println("Simulating API call for prompt: " + prompt);

               String Response = callGeminiApi(prompt, apiKey); // Call API method

                updateMessage("Processing response..."); // status update
                Thread.sleep(500);

                return Response; // Return response

                // Simulate error for testing:
//                 throw new RuntimeException("Simulated API Error: Rate limit exceeded");
            }
        };
    }


    // --- Method for the actual API call would go here (using HttpClient) ---
     private String callGeminiApi(String prompt, String apiKey) throws Exception {

         // --- 1. Select Model and Construct URL ---
         String model = "gemini-1.5-flash"; // Or "gemini-1.0-pro", etc. Check available models
         String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

         // --- 2. Build JSON Payload ---
         // Using Gson library to create the JSON structure
         Gson gson = new Gson();
         JsonObject part = new JsonObject();
         part.addProperty("text", prompt); // Add the user's prompt text

         JsonObject content = new JsonObject();
         // Put the 'part' object into a JSON array for 'parts'
         content.add("parts", gson.toJsonTree(new JsonObject[]{part}));

         JsonObject payload = new JsonObject();
         // Put the 'content' object into a JSON array for 'contents'
         payload.add("contents", gson.toJsonTree(new JsonObject[]{content}));

         // Convert the whole payload structure to a JSON string
         String jsonPayload = gson.toJson(payload);
         System.out.println("Request Payload: " + jsonPayload); // Optional: Log the request payload for debugging

         // --- 3. Create HttpClient and HttpRequest ---
         HttpClient client = HttpClient.newBuilder()
                 .connectTimeout(Duration.ofSeconds(30)) // Timeout for establishing connection
                 .build();

         HttpRequest request = HttpRequest.newBuilder()
                 .uri(URI.create(url)) // Set the API endpoint URL
                 .header("Content-Type", "application/json") // Specify the content type
                 .timeout(Duration.ofSeconds(60)) // Timeout for the entire request
                 .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8)) // Set method to POST and provide the JSON body
                 .build();

         // --- 4. Send Request and Get Response ---
         // This runs inside the background Task's call() method, so synchronous send() is fine here.
         System.out.println("Sending request to: " + url); // Optional: Log the request URL (excluding key in production logs ideally)
         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

         // --- 5. Process Response ---
         int statusCode = response.statusCode();
         String responseBody = response.body();
         System.out.println("Response Status Code: " + statusCode); // Optional: Log status code
         // System.out.println("Response Body: " + responseBody); // Optional: Log the full response body for debugging

         if (statusCode == 200) { // HTTP OK
             try {
                 // Parse the successful JSON response using Gson
                 JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                 // Navigate the JSON structure carefully based on Gemini API documentation
                 // Expected structure: candidates -> [0] -> content -> parts -> [0] -> text
                 JsonElement candidatesElement = jsonResponse.get("candidates");
                 if (candidatesElement != null && candidatesElement.isJsonArray() && !candidatesElement.getAsJsonArray().isEmpty()) {
                     JsonObject firstCandidate = candidatesElement.getAsJsonArray().get(0).getAsJsonObject();

                     // Handle potential safety ratings / finish reasons if needed
                     JsonElement finishReasonElement = firstCandidate.get("finishReason");
                     if (finishReasonElement != null && !"STOP".equals(finishReasonElement.getAsString())) {
                         // Handle cases where generation stopped for other reasons (SAFETY, RECITATION, MAX_TOKENS etc.)
                         throw new RuntimeException("Gemini stopped generation. Reason: " + finishReasonElement.getAsString() + ". Check response body for details.");
                     }


                     JsonObject contentObject = firstCandidate.getAsJsonObject("content");
                     if (contentObject != null) {
                         JsonElement partsElement = contentObject.get("parts");
                         if (partsElement != null && partsElement.isJsonArray() && !partsElement.getAsJsonArray().isEmpty()) {
                             JsonObject firstPart = partsElement.getAsJsonArray().get(0).getAsJsonObject();
                             JsonElement textElement = firstPart.get("text");
                             if (textElement != null && textElement.isJsonPrimitive()) {
                                 return textElement.getAsString(); // Success! Return the generated text
                             }
                         }
                     }
                 }
                 // If structure is not as expected or text is missing after checking candidates
                 throw new RuntimeException("Could not parse valid text from Gemini response structure: " + responseBody);

             } catch (JsonSyntaxException | IllegalStateException | IndexOutOfBoundsException | NullPointerException e) {
                 // Handle potential JSON parsing or structure errors
                 throw new RuntimeException("Error processing Gemini response JSON: " + e.getMessage() + "\nResponse body: " + responseBody, e);
             }
         } else {
             // Handle API errors (non-200 status codes like 400, 429, 500 etc.)
             // The responseBody often contains useful error details from the API
             throw new RuntimeException("Gemini API Error: Status Code " + statusCode + "\nResponse: " + responseBody);
         }
     }
}