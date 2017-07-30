/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.pubmed.retriever;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reciter.model.pubmed.PubMedArticle;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class PubMedArticleRetriever {

    /**
     * Initializes and starts threads that handles the retrieval process. Partition the number of articles
     * into manageable pieces and ask each thread to handle one partition.
     */
    public List<PubMedArticle> retrievePubMed(PubMedQuery pubMedQuery, int numberOfPubmedArticles) {
        String nodeUrl = "http://localhost:5000/pubmed/query-complex/";
        RestTemplate restTemplate = new RestTemplate();
        log.info("Sending web request: " + nodeUrl);
        ResponseEntity<PubMedArticle[]> responseEntity = null;
        PubMedQuery p = PubMedQuery.builder().author("test").end(new Date()).start(new Date()).build();
        try {
            responseEntity = restTemplate.postForEntity(nodeUrl, p, PubMedArticle[].class);
        } catch (Exception e) {
            log.error("Unable to retrieve via external REST api=[" + nodeUrl + "]", e);
        }
        PubMedArticle[] pubMedArticles = responseEntity.getBody();
        return Arrays.asList(pubMedArticles);
    }
}
