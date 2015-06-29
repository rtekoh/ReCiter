package reciter.lucene.docsimilarity;

import reciter.lucene.DocumentVector;
import reciter.lucene.DocumentVectorType;
import reciter.model.article.ReCiterArticle;

public class KeywordCosineSimilarity  extends AbstractCosineSimilarity {

	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		double max = -1;

		DocumentVector docV1 = docA.getDocumentVectors().get(DocumentVectorType.KEYWORD);
		DocumentVector docV2 = docB.getDocumentVectors().get(DocumentVectorType.KEYWORD);
		double sim = cosineSim(docV1, docV2);
		if (sim > max) {
			max = sim;
		}
		return max;
	}

}