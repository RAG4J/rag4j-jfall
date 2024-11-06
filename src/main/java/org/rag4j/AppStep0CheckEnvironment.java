package org.rag4j;

import lombok.extern.slf4j.Slf4j;
import org.rag4j.integrations.ollama.OllamaAccess;
import org.rag4j.integrations.weaviate.WeaviateAccess;
import org.rag4j.util.keyloader.KeyLoader;

@Slf4j
public class AppStep0CheckEnvironment {
    public static void main(String[] args) {
        KeyLoader keyLoader = new KeyLoader();

        String secretKey = keyLoader.getOpenAIKey();

        if (secretKey == null || secretKey.isEmpty()) {
            log.error("OpenAI key not found in environment variables.");
        } else {
            log.info("OpenAI key found in environment variables.");
        }

        String weaviateAPIKey = keyLoader.getWeaviateAPIKey();
        if (weaviateAPIKey == null || weaviateAPIKey.isEmpty()) {
            log.error("Weaviate API key not found in environment variables.");
        } else {
            log.info("Weaviate API key found in environment variables.");
        }
        WeaviateAccess weaviateAccess = new WeaviateAccess(keyLoader);
        weaviateAccess.logClusterMeta();

        OllamaAccess ollamaAccess = new OllamaAccess();
        ollamaAccess.listModels().forEach(log::info);
    }
}
