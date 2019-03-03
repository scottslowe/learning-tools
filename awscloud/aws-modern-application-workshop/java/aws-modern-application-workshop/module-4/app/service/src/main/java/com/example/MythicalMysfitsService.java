package com.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MythicalMysfitsService {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    public Mysfits getAllMysfits() {

        List<Mysfit> mysfits = mapper.scan(Mysfit.class, new DynamoDBScanExpression());
        Mysfits allMysfits = new Mysfits(mysfits);

        return allMysfits;
    }

    public Mysfits queryMysfits(String filter, String value) {

        HashMap<String, AttributeValue> attribValue = new HashMap<String, AttributeValue>();
        attribValue.put(":"+value,  new AttributeValue().withS(value));

        DynamoDBQueryExpression<Mysfit> queryExpression = new DynamoDBQueryExpression<Mysfit>()
                .withIndexName(filter+"Index")
                .withKeyConditionExpression(filter + "= :" + value)
                .withExpressionAttributeValues(attribValue)
                .withConsistentRead(false);

        List<Mysfit> mysfits = mapper.query(Mysfit.class, queryExpression);
        Mysfits allMysfits = new Mysfits(mysfits);

        return allMysfits;
    }

    public Mysfit getMysfit(String mysfitId){

        return mapper.load(Mysfit.class, mysfitId);
    }

    public void likeMysfit(String mysfitId) {

        Mysfit mysfitToUpdate = mapper.load(Mysfit.class, mysfitId);
        Integer likes = mysfitToUpdate.getLikes() + 1;
        mysfitToUpdate.setLikes(likes);
        mapper.save(mysfitToUpdate);

    }

    public void adoptMysfit(String mysfitId) {

        Mysfit mysfitToUpdate = mapper.load(Mysfit.class, mysfitId);
        mysfitToUpdate.setAdopted(true);
        mapper.save(mysfitToUpdate);
    }

}
