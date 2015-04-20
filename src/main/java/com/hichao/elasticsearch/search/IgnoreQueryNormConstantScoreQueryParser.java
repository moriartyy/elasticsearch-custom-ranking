package com.hichao.elasticsearch.search;

import java.io.IOException;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.cache.filter.support.CacheKeyFilter;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;

public class IgnoreQueryNormConstantScoreQueryParser implements QueryParser {
    
    public static final String NAME = "iqn_constant_score";

    @Inject
    public IgnoreQueryNormConstantScoreQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        Filter filter = null;
        boolean filterFound = false;
        Query query = null;
        boolean queryFound = false;
        float boost = 1.0f;
        boolean cache = false;
        CacheKeyFilter.Key cacheKey = null;

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                if ("filter".equals(currentFieldName)) {
                    filter = parseContext.parseInnerFilter();
                    filterFound = true;
                } else if ("query".equals(currentFieldName)) {
                    query = parseContext.parseInnerQuery();
                    queryFound = true;
                } else {
                    throw new QueryParsingException(parseContext.index(), "[iqn_constant_score] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if ("_cache".equals(currentFieldName)) {
                    cache = parser.booleanValue();
                } else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
                    cacheKey = new CacheKeyFilter.Key(parser.text());
                } else {
                    throw new QueryParsingException(parseContext.index(), "[iqn_constant_score] query does not support [" + currentFieldName + "]");
                }
            }
        }
        if (!filterFound && !queryFound) {
            throw new QueryParsingException(parseContext.index(), "[constant_score] requires either 'filter' or 'query' element");
        }

        if (query == null && filter == null) {
            return null;
        }

        if (filter != null) {
            // cache the filter if possible needed
            if (cache) {
                filter = parseContext.cacheFilter(filter, cacheKey);
            }

            Query query1 = new XIgnoreQueryNormConstantScoreQuery(filter);
            query1.setBoost(boost);
            return query1;
        }
        // Query
        query = new IgnoreQueryNormConstantScoreQuery(query);
        query.setBoost(boost);
        return query;
    }
}
