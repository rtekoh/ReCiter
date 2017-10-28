package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;

@DynamoDBTable(tableName = "ScopusArticle")
@AllArgsConstructor
public class ScopusArticle {

    private String id;
    private reciter.model.scopus.ScopusArticle scopusArticle;

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    @DynamoDBAttribute(attributeName = "scopusarticle")
    public reciter.model.scopus.ScopusArticle getScopusArticle() {
        return scopusArticle;
    }

    public void setPmid(String id) {
        this.id = id;
    }

    public void setScopusArticle(reciter.model.scopus.ScopusArticle scopusArticle) {
        this.scopusArticle = scopusArticle;
    }
}
