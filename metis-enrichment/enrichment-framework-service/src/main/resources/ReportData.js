YAHOO.namespace ("AnnoCultor"); 
YAHOO.AnnoCultor.Data = 
{
environment: 
[
 {id:"annoCultorDir", value:"/eculture/annocultor/"},
 {id:"collectionDir", value:"/eculture/dummy/"},
 {id:"diffDir", value:"/eculture/dummy/diff"},
 {id:"docDir", value:"/eculture/dummy/doc"},
 {id:"finalDir", value:"/eculture/dummy/rdf"},
 {id:"previousDir", value:"/eculture/dummy/prev"},
 {id:"tmpDir", value:"/eculture/dummy/tmp"},
 {id:"ontoDir", value:"/eculture/dummy/rdfs"},
 {id:"localProfileFile", value:"/eculture/dummy/annocultor.properties"}
],
graphs: 
[
 {id:"rmac.works.rdf", subjects:5570, triples:15000, diff:"../diff/rmac.works.html"},
 {id:"rmac.images.rdf", subjects:5570, triples:30000, diff:"../diff/rmac.works.html"},
 {id:"rmac.works.terms.aat.rdf", subjects:50, triples:150},
 {id:"rmac.works.terms.tgn.rdf", subjects:557, triples:1200, diff:"../diff/rmac.works.html"}
],
rules: 
[
 {id:"rmac.works.rdf", rule:"LiteralRenameRule", tag:"record/id", firings:50},
 {id:"rmac.works.rdf", rule:"LiteralRenameRule", tag:"record/description", firings:3},
 {id:"rmac.works.rdf", rule:"LiteralRenameRule", tag:"record/title", firings:50},
 {id:"rmac.images.rdf", rule:"LiteralRenameRule", tag:"record/id", firings:5},
 {id:"rmac.images.rdf", rule:"ResourceRenameRule", tag:"record/artist", firings:50},
 {id:"rmac.works.rdf", rule:"FacetRule", tag:"record/label", firings:0}
],
unusedtags:
[
 {id:"record/item/alternative", occurrences:40},
 {id:"record/item/another_artist", occurrences:1},
 {id:"record/item/titel", occurrences:5},
 {id:"record/item/ietsanders", occurrences:4675},
],	
console:
[
 {line:"Running AnnoCUltor"},
 {line:"Loading vocabulary TGN"},
 {line:"Converting"},
 {line:"Warning: something may be wrong"},
 {line:"Error: it is WRONG"}
]	

};