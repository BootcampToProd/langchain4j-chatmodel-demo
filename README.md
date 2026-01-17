# 🤖 LangChain4j ChatModel: Deep Dive into AI Interactions

This repository demonstrates the full capabilities of the **LangChain4j ChatModel** interface in LangChain4j. It explores the core mechanics of AI interaction, demonstrating how to construct complex requests, manage conversation context, configure model parameters, and analyze rich response metadata.

📖 **Complete Guide**: For detailed explanations and a full code walkthrough, read our comprehensive tutorial.<br>
👉 [**LangChain4j ChatModel: A Complete Beginner’s Guide**](https://bootcamptoprod.com/langchain4j-chatmodel/)

🎥 **Video Tutorial**: Prefer hands-on learning? Watch our step-by-step implementation guide.<br>
👉 YouTube Tutorial - Coming Soon!!

---

## ✨ What This Project Demonstrates

This application serves as a deep dive into the **ChatModel API**, covering the full lifecycle of an AI request:

- **Message Management** - Understanding the roles of `SystemMessage`, `UserMessage`, and `AiMessage` to create context-aware personas.
- **Request Configuration** - Using `ChatRequest` and `ChatRequestParameters` to configure model behavior (Temperature, Max Tokens, Stop Sequences).
- **Contextual Conversations** - Managing conversation history to enable back-and-forth dialogue logic.
- **Response Analysis** - Extracting critical metadata from `ChatResponse`, including `TokenUsage` and `FinishReason`.

---

## 🛠️ Prerequisites

To run this application, you will need the following:

1. **OpenRouter API Key**: This project uses OpenRouter to access free AI models (DeepSeek, Llama, etc.) via OpenAI-compatible endpoints.
    - Sign up at [OpenRouter.ai](https://openrouter.ai/) to generate your key.
2. **Setup Environment Variables**: Set your API key as an environment variable:
```bash
# Set your OpenRouter API Key
export OPENROUTER_API_KEY=your_api_key_here
```
---

## 🚀 How to Run and Test

**For detailed instructions on how to set up, configure, and test the application, kindly go through our comprehensive article:**  
👉 [**Click here for Setup & Testing Instructions**](https://bootcamptoprod.com/langchain4j-chatmodel/)

---