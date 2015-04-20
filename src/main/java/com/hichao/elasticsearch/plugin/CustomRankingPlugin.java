package com.hichao.elasticsearch.plugin;

import org.elasticsearch.index.query.functionscore.FunctionScoreModule;
import org.elasticsearch.index.similarity.SimilarityModule;
import org.elasticsearch.indices.query.IndicesQueriesModule;
import org.elasticsearch.plugins.AbstractPlugin;

import com.hichao.elasticsearch.index.SimpleSimilarityProvider;
import com.hichao.elasticsearch.search.IgnoreQueryNormConstantScoreQueryParser;
import com.hichao.elasticsearch.search.function.FeatureScoreFunctionParser;

public class CustomRankingPlugin extends AbstractPlugin {
    public String name() {  
        return "custom-ranking";  
    }  

    public String description() {
        return "Custom ranking plugin";
    }

    public void onModule(FunctionScoreModule functionScoreModule){
        functionScoreModule.registerParser(FeatureScoreFunctionParser.class);
    }
    
    public void onModule(IndicesQueriesModule indicesQueriesModule){
        indicesQueriesModule.addQuery(new IgnoreQueryNormConstantScoreQueryParser());
    }
    
    public void onModule(SimilarityModule module){
        module.addSimilarity("simple", SimpleSimilarityProvider.class);
    }
}
