
YAHOO.util.Event.addListener(window, "load", function() {

    var tabView = new YAHOO.widget.TabView();

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Environment',
        content: '<div id="envTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Graphs',
        content: '<div id="graphTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Matched',
        content: '<div id="matchedTable"></div>',
        active: true
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Missed',
        content: '<div id="missedTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Ambiguous',
        content: '<div id="ambigousTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Rules',
        content: '<div id="rulesTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Unused',
        content: '<div id="unusedTable"></div>'
    }));

    tabView.addTab( new YAHOO.widget.Tab({
        label: 'Console',
        content: '<div id="conTable"></div>'
    }));

    tabView.appendTo('divReport'); 

    Environment = new function() {
        this.envDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.environment);
        this.envDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.envDataSource.responseSchema = {
             fields: ["id","value"]
        };
        this.envDataTable = new YAHOO.widget.DataTable(
                "envTable",
                [
                 {key:"id", sortable:true, resizeable:true},
                 {key:"value", sortable:true, resizeable:true}
                ], 
                this.envDataSource, 
                {caption:"<p class='subtitle'>Environment variables</p>", sortedBy:{key:"id",dir:"desc"}, width: "200"});
        
    }

    Graphs = new function() {
        var columnDefs = [
            {key:"id", sortable:true, resizeable:true},
            {key:"subjects", sortable:true, resizeable:true},
            {key:"triples", sortable:true, resizeable:true},
            {key:"diff", sortable:true, resizeable:true,formatter: "link"}
        ];
        this.graphDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.graphs);
        this.graphDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.graphDataSource.responseSchema = {
             fields: ["id","subjects","triples","diff"]
        };
        this.graphDataTable = new YAHOO.widget.DataTable(
                "graphTable",
                columnDefs, 
                this.graphDataSource, 
                {caption:"<p class='subtitle'>Named RDF graphs, each stored in a separate file</p>", sortedBy:{key:"id",dir:"desc"}});
    }

    Rules = new function() {
        var columnDefs = [
            {key:"id", sortable:true, resizeable:true},
            {key:"rule", sortable:true, resizeable:true},
            {key:"tag", sortable:true, resizeable:true},
            {key:"firings", sortable:true, resizeable:true}
        ];
        this.rulesDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.rules);
        this.rulesDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.rulesDataSource.responseSchema = {
             fields: ["id","rule","tag","firings"]
        };
        this.envDataTable = new YAHOO.widget.DataTable(
                "rulesTable",
                columnDefs, 
                this.rulesDataSource, 
                {caption:"<p class='subtitle'>Rule invocation counts</p>", sortedBy:{key:"id",dir:"desc"}});
    }

    Unused = new function() {
        var columnDefs = [
            {key:"id", sortable:true, resizeable:true},
            {key:"occurrences", sortable:true, resizeable:true}
        ];
        this.unusedDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.unusedtags);
        this.unusedDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.unusedDataSource.responseSchema = {
             fields: ["id","occurrences"]
        };
        this.unusedDataTable = new YAHOO.widget.DataTable(
                "unusedTable",
                columnDefs, 
                this.unusedDataSource, 
                {caption:"<p class='subtitle'>Forgotten XML tags, present in data but not covered by any rule</p>", sortedBy:{key:"id",dir:"desc"}});
    }

    Console = new function() {
        var columnDefs = [
            {key:"line", sortable:false, resizeable:true}
        ];
        this.conDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.console);
        this.conDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.conDataSource.responseSchema = {
             fields: ["line"]
        };
        this.conDataTable = new YAHOO.widget.DataTable(
                "conTable",
                columnDefs, 
                this.conDataSource, 
                {caption:"<p class='subtitle'>Console messages</p>"});
    }

    Missed = new function() {
        var columnDefs = [
            {key:"term", sortable:true, resizeable:true},
            {key:"count", sortable:true, resizeable:true}
        ];
        this.countersDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.missed);
        this.countersDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.countersDataSource.responseSchema = {
             fields: ["term","count"]
        };
        this.countersDataTable = new YAHOO.widget.DataTable(
                "missedTable",
                columnDefs, 
                this.countersDataSource, 
                {caption:"<p class='subtitle'>Labels not found in vocabularies</p>", sortedBy:{key:"count",dir:"desc"}});
    }

    Ambiguous = new function() {
        var columnDefs = [
            {key:"term", sortable:true, resizeable:true},
            {key:"count", sortable:true, resizeable:true}
        ];
        this.countersDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.ambigous);
        this.countersDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.countersDataSource.responseSchema = {
             fields: ["term","count"]
        };
        this.countersDataTable = new YAHOO.widget.DataTable(
                "ambigousTable",
                columnDefs, 
                this.countersDataSource, 
                {caption:"<p class='subtitle'>Labels with multiple codes found in vocabularies, and disambiguation failed</p>", sortedBy:{key:"count",dir:"desc"}});
    }

    Matched = new function() {
        var columnDefs = [
            {key:"term", sortable:true, resizeable:true},
            {key:"count", sortable:true, resizeable:true}
        ];
        this.countersDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.matched);
        this.countersDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.countersDataSource.responseSchema = {
             fields: ["term","count"]
        };
        this.countersDataTable = new YAHOO.widget.DataTable(
                "matchedTable",
                columnDefs, 
                this.countersDataSource, 
                {caption:"<p class='subtitle'>Labels where codes are found, and successfully disambiguated</p>", sortedBy:{key:"count",dir:"desc"}});
    }

});
