package com.hichao.elasticsearch.search;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoostableQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

public class IgnoreQueryNormConstantScoreQueryBuilder extends BaseQueryBuilder implements BoostableQueryBuilder<IgnoreQueryNormConstantScoreQueryBuilder> {

    private final FilterBuilder filterBuilder;
    private final QueryBuilder queryBuilder;

    private float boost = -1;
    

    /**
     * A query that wraps a filter and simply returns a constant score equal to the
     * query boost for every document in the filter.
     *
     * @param filterBuilder The filter to wrap in a constant score query
     */
    public IgnoreQueryNormConstantScoreQueryBuilder(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        this.queryBuilder = null;
    }
    /**
     * A query that wraps a query and simply returns a constant score equal to the
     * query boost for every document in the query.
     *
     * @param queryBuilder The query to wrap in a constant score query
     */
    public IgnoreQueryNormConstantScoreQueryBuilder(QueryBuilder queryBuilder) {
        this.filterBuilder = null;
        this.queryBuilder = queryBuilder;
    }    

    /**
     * Sets the boost for this query.  Documents matching this query will (in addition to the normal
     * weightings) have their score multiplied by the boost provided.
     */
    public IgnoreQueryNormConstantScoreQueryBuilder boost(float boost) {
        this.boost = boost;
        return this;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(IgnoreQueryNormConstantScoreQueryParser.NAME);
        if (queryBuilder != null) {
            assert filterBuilder == null;
            builder.field("query");
            queryBuilder.toXContent(builder, params);
        } else {
            builder.field("filter");
            filterBuilder.toXContent(builder, params);  
        }
        
        if (boost != -1) {
            builder.field("boost", boost);
        }
        builder.endObject();
    }
}
