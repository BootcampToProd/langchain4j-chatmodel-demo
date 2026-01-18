package com.bootcamptoprod.controller;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChatModel Demo Controller
 * <p>
 * This controller demonstrates all 5 ChatModel methods:
 * 1. chat(String) - Simple string input
 * 2. chat(ChatMessage...) - Varargs messages
 * 3. chat(List<ChatMessage>) - List of messages
 * 4. chat(ChatRequest) - Full request with parameters
 * 5. doChat(ChatRequest) - Low-level direct call
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatModelDemoController {

    @Autowired
    private ChatModel chatModel;

    /**
     * ENDPOINT 1: Simple String Chat
     * Method: chat(String userMessage)
     * <p>
     * This is the simplest method - just send a string, get a string back.
     * No metadata, no token info, just pure Q&A.
     * <p>
     * POST /api/chat/simple
     * Body: { "message": "What is Spring Boot?" }
     */
    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleChat(@RequestBody Map<String, String> request) {

        // Extract user message from request
        String userMessage = request.get("message");

        // Call the simplest ChatModel method - returns only text
        String response = chatModel.chat(userMessage);

        // Build response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aiResponse", response);

        return ResponseEntity.ok(result);
    }

    /**
     * ENDPOINT 2: Chat with Messages (Varargs)
     * Method: chat(ChatMessage... messages)
     * <p>
     * Send multiple messages (System + User) and get full ChatResponse with metadata.
     * <p>
     * POST /api/chat/with-chat-messages
     * Body: {
     * "systemMessage": "You are a Java expert",
     * "userMessage": "Explain Spring Boot"
     * }
     */
    @PostMapping("/with-chat-messages")
    public ResponseEntity<Map<String, Object>> chatWithMessages(@RequestBody Map<String, String> request) {

        // Extract messages
        String systemText = request.getOrDefault("systemMessage", "You are a helpful assistant");
        String userText = request.get("userMessage");

        // Create ChatMessage objects
        SystemMessage systemMessage = SystemMessage.from(systemText);
        UserMessage userMessage = UserMessage.from(userText);

        // Call ChatModel with varargs - returns full ChatResponse
        ChatResponse response = chatModel.chat(systemMessage, userMessage);

        // Build detailed response with all metadata
        Map<String, Object> result = buildDetailedResponse(response);

        return ResponseEntity.ok(result);
    }

    /**
     * ENDPOINT 3: Conversational Chat with History
     * Method: chat(List<ChatMessage> messages)
     * <p>
     * Send a conversation (multiple back-and-forth messages) for context-aware responses.
     * <p>
     * POST /api/chat/conversation
     * Body: {
     * "messages": [
     * { "type": "system", "content": "You are a coding tutor" },
     * { "type": "user", "content": "What is JPA?" },
     * { "type": "ai", "content": "JPA is Java Persistence API..." },
     * { "type": "user", "content": "Show me an example" }
     * ]
     * }
     */
    @PostMapping("/conversation")
    public ResponseEntity<Map<String, Object>> conversationalChat(
            @RequestBody Map<String, Object> request) {

        // Extract messages list
        List<Map<String, String>> messagesData = (List<Map<String, String>>) request.get("messages");

        // Convert to ChatMessage objects
        List<ChatMessage> messages = messagesData.stream()
                .map(this::convertToChatMessage)
                .collect(Collectors.toList());

        // Call ChatModel with list of messages
        ChatResponse response = chatModel.chat(messages);

        // Build detailed response with all metadata
        Map<String, Object> result = buildDetailedResponse(response);

        return ResponseEntity.ok(result);
    }

    /**
     * ENDPOINT 4: Advanced Chat with Full Parameters
     * Method: chat(ChatRequest chatRequest)
     * <p>
     * Most powerful method - full control over all parameters.
     * <p>
     * POST /api/chat/advanced
     * Body: {
     * "systemMessage": "You are a creative writer",
     * "userMessage": "Write a haiku about coding",
     * "temperature": 0.9,
     * "maxTokens": 100,
     * "topP": 0.95,
     * "frequencyPenalty": 0.5,
     * "presencePenalty": 0.5,
     * "stopSequences": ["END"]
     * }
     */
    @PostMapping("/advanced")
    public ResponseEntity<Map<String, Object>> advancedChat(@RequestBody Map<String, Object> request) {

        // Extract messages
        String systemText = (String) request.getOrDefault("systemMessage", "You are a helpful assistant");
        String userText = (String) request.get("userMessage");

        // Extract parameters (with defaults)
        Double temperature = getDoubleParam(request, "temperature", 0.7);
        Integer maxTokens = getIntegerParam(request, "maxTokens", 500);
        Double topP = getDoubleParam(request, "topP", 0.9);
        Double frequencyPenalty = getDoubleParam(request, "frequencyPenalty", 1.5);
        Double presencePenalty = getDoubleParam(request, "presencePenalty", 1.5);
        @SuppressWarnings("unchecked")
        List<String> stopSequences = (List<String>) request.get("stopSequences");

        // Build ChatRequestParameters
        ChatRequestParameters requestParameters = ChatRequestParameters.builder()
                .modelName("tngtech/deepseek-r1t2-chimera:free")
                .temperature(temperature)
                .maxOutputTokens(maxTokens)
                .topP(topP)
                .frequencyPenalty(frequencyPenalty)
                .presencePenalty(presencePenalty)
                .stopSequences(stopSequences)
                .build();


        // Build ChatRequest
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(
                        SystemMessage.from(systemText),
                        UserMessage.from(userText)
                )
                .parameters(requestParameters)
                .build();

        // Call ChatModel with full ChatRequest
        ChatResponse response = chatModel.chat(chatRequest);


        // Build response
        Map<String, Object> result = buildDetailedResponse(response);

        return ResponseEntity.ok(result);
    }

    /**
     * ENDPOINT 5: Direct Chat (Low-level)
     * Method: doChat(ChatRequest chatRequest)
     * <p>
     * Low-level method that bypasses listeners - use with caution.
     * <p>
     * POST /api/chat/direct
     * Body: { "message": "Hello AI" }
     */
    @PostMapping("/direct")
    public ResponseEntity<Map<String, Object>> directChat(@RequestBody Map<String, String> request) {

        String userMessage = request.get("message");

        // We need to provide OpenAiChatRequestParameters specifically here
        OpenAiChatRequestParameters openAiChatRequestParameters = OpenAiChatRequestParameters.builder()
                .modelName("tngtech/deepseek-r1t2-chimera:free")
                .temperature(0.8)
                .build();

        // Build simple ChatRequest
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(userMessage))
                .parameters(openAiChatRequestParameters)
                .build();

        // Call doChat - bypasses listeners
        ChatResponse response = chatModel.doChat(chatRequest);

        // Build response
        Map<String, Object> result = buildDetailedResponse(response);

        return ResponseEntity.ok(result);
    }

    /**
     * Get Model Information
     * <p>
     * Shows all model capabilities, provider info, default parameters, and listeners.
     * <p>
     * GET /api/chat/model-info
     */
    @GetMapping("/model-info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {


        Map<String, Object> modelInfo = new LinkedHashMap<>();

        // 1. Provider Information
        ModelProvider provider = chatModel.provider();
        modelInfo.put("provider", provider.name());

        // 2. Supported Capabilities
        Set<Capability> capabilities = chatModel.supportedCapabilities();
        List<String> capabilityNames = capabilities.stream()
                .map(Capability::name)
                .sorted()
                .collect(Collectors.toList());

        modelInfo.put("supportedCapabilities", capabilityNames);
        modelInfo.put("capabilitiesCount", capabilities.size());

        // 3. Default Request Parameters
        ChatRequestParameters defaultParams = chatModel.defaultRequestParameters();
        Map<String, Object> paramsMap = new LinkedHashMap<>();

        if (defaultParams != null) {
            paramsMap.put("modelName", defaultParams.modelName());
            paramsMap.put("temperature", defaultParams.temperature());
            paramsMap.put("maxOutputTokens", defaultParams.maxOutputTokens());
            paramsMap.put("topP", defaultParams.topP());
            paramsMap.put("topK", defaultParams.topK());
            paramsMap.put("frequencyPenalty", defaultParams.frequencyPenalty());
            paramsMap.put("presencePenalty", defaultParams.presencePenalty());
            paramsMap.put("stopSequences", defaultParams.stopSequences());

            ResponseFormat responseFormat = defaultParams.responseFormat();
            if (responseFormat != null) {
                paramsMap.put("responseFormat", responseFormat.type().name());
            }
        }

        modelInfo.put("defaultParameters", paramsMap);

        // 4. Registered Listeners
        List<ChatModelListener> listeners = chatModel.listeners();
        List<String> listenerNames = listeners.stream()
                .map(listener -> listener.getClass().getSimpleName())
                .collect(Collectors.toList());

        modelInfo.put("registeredListeners", listenerNames);
        modelInfo.put("listenersCount", listeners.size());

        return ResponseEntity.ok(modelInfo);
    }


    // ==================== Helper Methods ====================

    /**
     * Build detailed response map with all metadata
     */
    private Map<String, Object> buildDetailedResponse(ChatResponse response) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aiResponse", response.aiMessage().text());

        // Add all metadata
        addTokenUsageInfo(result, response);
        addFinishReasonInfo(result, response);
        addMetadataInfo(result, response);

        return result;
    }

    /**
     * Add token usage information to result
     */
    private void addTokenUsageInfo(Map<String, Object> result, ChatResponse response) {
        TokenUsage tokenUsage = response.metadata().tokenUsage();

        if (tokenUsage != null) {
            Map<String, Object> tokenInfo = new LinkedHashMap<>();
            tokenInfo.put("inputTokens", tokenUsage.inputTokenCount());
            tokenInfo.put("outputTokens", tokenUsage.outputTokenCount());
            tokenInfo.put("totalTokens", tokenUsage.totalTokenCount());

            result.put("tokenUsage", tokenInfo);
        }
    }

    /**
     * Add finish reason information to result
     */
    private void addFinishReasonInfo(Map<String, Object> result, ChatResponse response) {
        FinishReason finishReason = response.metadata().finishReason();

        if (finishReason != null) {
            Map<String, String> finishInfo = new LinkedHashMap<>();
            finishInfo.put("reason", finishReason.name());
            result.put("finishReason", finishInfo);
        }
    }

    /**
     * Add response metadata to result
     */
    private void addMetadataInfo(Map<String, Object> result, ChatResponse response) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("modelName", response.metadata().modelName());
        metadata.put("responseId", response.id());

        result.put("metadata", metadata);
    }

    /**
     * Convert DTO map to ChatMessage
     */
    private ChatMessage convertToChatMessage(Map<String, String> messageData) {
        String type = messageData.get("type").toLowerCase();
        String content = messageData.get("content");

        return switch (type) {
            case "system" -> SystemMessage.from(content);
            case "user" -> UserMessage.from(content);
            case "ai", "assistant" -> AiMessage.from(content);
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };
    }

    /**
     * Safely extract Double parameter from request
     */
    private Double getDoubleParam(Map<String, Object> request, String key, Double defaultValue) {
        Object value = request.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return defaultValue;
    }

    /**
     * Safely extract Integer parameter from request
     */
    private Integer getIntegerParam(Map<String, Object> request, String key, Integer defaultValue) {
        Object value = request.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        return defaultValue;
    }
}