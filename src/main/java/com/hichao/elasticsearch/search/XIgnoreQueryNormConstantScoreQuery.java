package com.hichao.elasticsearch.search;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.elasticsearch.common.lucene.search.ApplyAcceptedDocsFilter;

public class XIgnoreQueryNormConstantScoreQuery extends ConstantScoreQuery {

    private final Filter actualFilter;

    public XIgnoreQueryNormConstantScoreQuery(Filter filter) {
        super(new ApplyAcceptedDocsFilter(filter));
        this.actualFilter = filter;
    }

    // trick so any external systems still think that its the actual filter we use, and not the
    // deleted filter
    @Override
    public Filter getFilter() {
        return this.actualFilter;
    }
}
