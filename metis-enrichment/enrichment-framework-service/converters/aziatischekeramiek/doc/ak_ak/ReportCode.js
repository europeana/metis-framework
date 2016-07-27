
YAHOO.util.Event.addListener(window, "load", function() {
	    
    Environment = new function() {
//        var oPanel = new YAHOO.widget.Panel("myPanel");
//        oPanel.render();
//        oPanel.show();

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
                {caption:"<h2>Environment variables</h2>", sortedBy:{key:"id",dir:"desc"}, width: "200"});
        
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
                {caption:"<h2>Graphs</h2>", sortedBy:{key:"id",dir:"desc"}});
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
                {caption:"<h2>Rules</h2>", sortedBy:{key:"id",dir:"desc"}});
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
                {caption:"<h2>Forgotten XML tags</h2>", sortedBy:{key:"id",dir:"desc"}});
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
                {caption:"<h2>Console</h2>"});
    }

    Counters = new function() {
        var columnDefs = [
            {key:"term", sortable:true, resizeable:true},
            {key:"count", sortable:true, resizeable:true}
        ];
        this.countersDataSource = new YAHOO.util.DataSource(YAHOO.AnnoCultor.Data.counters);
        this.countersDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.countersDataSource.responseSchema = {
             fields: ["term","count"]
        };
        this.countersDataTable = new YAHOO.widget.DataTable(
                "countTable",
                columnDefs, 
                this.countersDataSource, 
                {caption:"<h2>Counters (top 250 of each)</h2>", sortedBy:{key:"count",dir:"desc"}});
    }

//this.tabView = new YAHOO.widget.TabView('demo');
//var tab0 = tabView.getTab(0);
    
//    function handleContentChange(e, b) {  
//        alert(e.prevValue);
//        this.envDataTable.refresh();
//    }
    
    //this.tabView.addListener('activeTabChange', handleContentChange);
    //tab0.set('content', '<p>Updated tab content.</p>');
    
});
