package com.hichao.elasticsearch.search.function;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.index.fielddata.ScriptDocValues.Doubles;
import org.elasticsearch.search.lookup.SearchLookup;

import java.util.Map;

public class FeatureScoreFunction extends ScoreFunction {

    private final String aggregateMode;
    
    private final Map<String, Object> params;

    private final SearchLookup lookup;

    private final float[] weights;

    private final String[] featureNames;

    public FeatureScoreFunction(SearchLookup lookup, Map<String, Object> params, String aggregateMode) {
        super(CombineFunction.REPLACE);
        this.lookup = lookup;
        this.params = params;
        this.aggregateMode = aggregateMode;
        int featureCount = params.size();
        this.weights = new float[featureCount];
        this.featureNames = new String[featureCount];
        int index = 0;
        for (String featureName : params.keySet()) {
            this.weights[index] = Float.parseFloat(params.get(featureName).toString());
            this.featureNames[index] = featureName;
            index++;
        }
    }

    @Override
    public void setNextReader(AtomicReaderContext context) {
        if (lookup != null) {
            lookup.setNextReader(context);
        }
    }

    @Override
    public double score(int docId, float subQueryScore) {

        this.lookup.setNextDocId(docId);
        double finalScore = subQueryScore;
        
        if ("sum".equals(this.aggregateMode)){
            for (int i = 0; i < this.weights.length; i++) {
                Doubles docValues = (Doubles)this.lookup.doc().get(this.featureNames[i]);
                finalScore += ((Doubles)docValues).getValue() * this.weights[i];
            }
        }else{
            for (int i = 0; i < this.weights.length; i++) {
                Doubles docValues = (Doubles)this.lookup.doc().get(this.featureNames[i]);
                finalScore *= docValues.getValue() * this.weights[i];
            }
        }
        return finalScore;
    }

    @Override
    public Explanation explainScore(int docId, Explanation subQueryExpl) {
        Explanation exp;
//        if (script instanceof ExplainableSearchScript) {
//            script.setNextDocId(docId);
//            script.setNextScore(subQueryExpl.getValue());
//            exp = ((ExplainableSearchScript) script).explain(subQueryExpl);
//        } else {
//            double score = score(docId, subQueryExpl.getValue());
//            exp = new Explanation(CombineFunction.toFloat(score), "script score function: composed of:");
//            exp.addDetail(subQueryExpl);
//        }
        
        this.lookup.setNextDocId(docId);
        float subQueryValue = subQueryExpl.getValue();
        
        StringBuilder formula = new StringBuilder();
        formula.append(subQueryValue).append(" + ");
        for (int i = 0; i < this.weights.length; i++) {
            Doubles docValues = (Doubles)this.lookup.doc().get(this.featureNames[i]);
            formula.append(this.weights[i]).append(" * ").append(((Doubles)docValues).getValue()).append(" + ");
        }
        
        double score = score(docId, subQueryValue);
        exp = new Explanation(CombineFunction.toFloat(score), 
                String.format("feature score function: composed of: %s", formula.subSequence(0, formula.length()-3).toString()));
        exp.addDetail(subQueryExpl);
        return exp;
    }

    @Override
    public String toString() {
        return "params [" + params + "]";
    }

}