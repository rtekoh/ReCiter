package main.reciter.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class DocumentVectorGenerator {

	/**
	 * Creates a DocumentVector for a specific field of a ReCiterArticle.
	 * 
	 * @param termsVector
	 * @param documentID
	 * @param documentVectorType
	 * @return
	 * @throws IOException
	 */
	public DocumentVector createDocumentVector(Terms termsVector, int documentID, 
			DocumentVectorType documentVectorType, DocumentTerm documentTerms, int maxDocs) throws IOException {
		
		DocumentVector documentVector = new DocumentVector(documentVectorType, documentTerms);
		documentVector.setDocumentVectorType(documentVectorType);
		
		if (termsVector != null) {
			TermsEnum termsEnum = termsVector.iterator(null);
			BytesRef text = null;
			while ((text = termsEnum.next()) != null) {
				String term = text.utf8ToString();
				int freq = (int) termsEnum.totalTermFreq();
				
				documentVector.getTermToFreqMap().put(term, freq);
				
				if (documentVectorType == DocumentVectorType.ARTICLE_TITLE) {
					documentVector.setEntry(documentTerms.getAllTitleTerms().get(term), freq);
				} else if (documentVectorType == DocumentVectorType.JOURNAL_TITLE) {
					documentVector.setEntry(documentTerms.getAllJournalTitleTerms().get(term), freq);
				} else if (documentVectorType == DocumentVectorType.AFFILIATION) {
//					double idfWeight = (double) maxDocs / documentTerms.getAffiliationIDFMap().get(term).size();
//					idfWeight = Math.log10(idfWeight);
//					documentVector.setEntry(documentTerms.getAllAffiliationTerms().get(term), freq * idfWeight);
//					documentVector.setEntry(documentTerms.getAllAffiliationTerms().get(term), freq); (uncomment this)
//					documentVector.setEntry(documentTerms.getAllAffiliationTerms().get(term), 1); // (comment this)
					documentVector.setEntry(documentTerms.getAllAffiliationTerms().get(term), freq);
				} else if (documentVectorType == DocumentVectorType.KEYWORD) {
					documentVector.setEntry(documentTerms.getAllKeywordTerms().get(term), freq);
				}
			}
			documentVector.setVector(documentVector.getDocumentVectorSimilarity().normalize(documentVector.getVector()));
		}
//		System.out.println(documentID + ": " + documentVector.getTermToFreqMap());
		return documentVector;
	}
	
	/**
	 * Create a document array of a specific ReCiterArticle.
	 * @param indexReader
	 * @param documentID
	 * @return
	 * @throws IOException
	 */
	public DocumentVector[] getDocumentVectors(IndexReader indexReader, int documentID, DocumentTerm documentTerms) 
			throws IOException {
		
		DocumentVector[] docVectorArray = new DocumentVector[4];
		
		int maxDocs = indexReader.maxDoc();
		
		docVectorArray[0] = createDocumentVector(
				indexReader.getTermVector(documentID, DocumentVectorType.AFFILIATION.name()), 
				documentID, DocumentVectorType.AFFILIATION, documentTerms, maxDocs);
		
		docVectorArray[1] = createDocumentVector(
				indexReader.getTermVector(documentID, DocumentVectorType.ARTICLE_TITLE.name()), 
				documentID, DocumentVectorType.ARTICLE_TITLE, documentTerms, maxDocs);
		
		docVectorArray[2] = createDocumentVector(
				indexReader.getTermVector(documentID, DocumentVectorType.JOURNAL_TITLE.name()),
				documentID, DocumentVectorType.JOURNAL_TITLE, documentTerms, maxDocs);
		
		docVectorArray[3] = createDocumentVector(
				indexReader.getTermVector(documentID, DocumentVectorType.KEYWORD.name()), 
				documentID, DocumentVectorType.KEYWORD, documentTerms, maxDocs);
		
		return docVectorArray;
	}
}
