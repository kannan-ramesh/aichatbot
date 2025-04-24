package com.kannanrameshrk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Controller
public class ChatBotController {

    @Value("${gemini.api.key}")
    private String API_KEY;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @GetMapping("/")
    public String display(HttpSession session, Model model) {
        if (session.getAttribute("chatHistory") == null) {
            session.setAttribute("chatHistory", new ArrayList<Message>());
        }
        model.addAttribute("chatHistory", session.getAttribute("chatHistory"));
        return "index";
    }

    @PostMapping("/chat")
    public String getAnswer(@RequestParam String reqChat, Model model, HttpSession session) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String modelName = "gemini-1.5-pro";
        String url = BASE_URL + modelName + ":generateContent?key=" + API_KEY;

        String requestBody = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        { \"text\": \"" + reqChat.replace("\"", "\\\"") + "\" }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        String json = response.body().string();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        String aiReply;
        try {
            aiReply = rootNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
        } catch (Exception e) {
            JsonNode errorNode = rootNode.get("error");
            String errorMessage = errorNode != null ? errorNode.toString() : "Unknown error from Gemini.";
            aiReply = "Sorry, I couldn't get a response: " + errorMessage;
        }

        // Add messages to session history
        @SuppressWarnings("unchecked")
		List<Message> history = (List<Message>) session.getAttribute("chatHistory");
        history.add(new Message("user", reqChat));
        history.add(new Message("bot", aiReply));
        session.setAttribute("chatHistory", history);

        model.addAttribute("chatHistory", history);
        return "index";
    }
}
