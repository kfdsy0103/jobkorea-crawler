package jobkorea.embedding.service;

import java.util.List;
import java.util.Map;
import jobkorea.embedding.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    public void embedRecruitmentPost(String summation, Recruitment recruitment) {
        DocumentReader reader = new TextReader(summation);
        List<Document> documents = reader.read();

        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            metadata.putAll(Map.of(
                    "recruitmentId", recruitment.getId(),
                    "source", "jobkorea",
                    "company", recruitment.getCompanyName(),
                    "endDate", recruitment.getEndDate()
            ));
        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        documents = tokenTextSplitter.apply(documents);

        vectorStore.add(documents);
    }
}
