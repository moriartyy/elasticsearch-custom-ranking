/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



package com.hichao.elasticsearch.search.function;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.index.query.functionscore.ScoreFunctionParser;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class FeatureScoreFunctionParser implements ScoreFunctionParser {

    public static String[] NAMES = { "feature_score", "featureScore" };

    @Inject
    public FeatureScoreFunctionParser() {
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public ScoreFunction parse(QueryParseContext parseContext, XContentParser parser) throws IOException, QueryParsingException {

        String aggregateMode = "sum";
        Map<String, Object> vars = null;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                if ("params".equals(currentFieldName)) {
                    vars = parser.map();
                } else {
                    throw new QueryParsingException(parseContext.index(), NAMES[0] + " query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("aggregateMode".equals(currentFieldName)) {
                    aggregateMode = parser.text();
                } else {
                    throw new QueryParsingException(parseContext.index(), NAMES[0] + " query does not support [" + currentFieldName + "]");
                }
            }
        }

        if (vars == null) {
            throw new QueryParsingException(parseContext.index(), NAMES[0] + " requires 'params' field");
        }

        try {
            return new FeatureScoreFunction(parseContext.lookup(), vars, aggregateMode);
        } catch (Exception e) {
            throw new QueryParsingException(parseContext.index(), NAMES[0] + " the script could not be loaded", e);
        }
    }
}