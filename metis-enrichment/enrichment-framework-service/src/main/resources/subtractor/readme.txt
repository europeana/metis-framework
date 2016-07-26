
OntologySubtractor tool

Purpose
 
  After a conversion AnnoCultor (http://annocultor.eu) creates RDF files, produced from some kind of external database.
  These files can be large in size, and for practical reasons, they are split in multiple volumes.
  For example, file Dataset_Subset.rdf would be saved as several volumes Dataset_Subset.1.rdf, Dataset_Subset.2.rdf, etc.
  When the dataset is updated we may re-run the conversion automatically creating new versions of these files.
  
  But sometimes we may want to delete a few triples from these RDF files. 
  If we do that on the files themselves then our deletions would be overwritten with the next conversion.
  
  A solution would be not to touch the generated files Dataset_Subset.1.rdf, Dataset_Subset.2.rdf, etc.
  but, instead, create separate files with the RDF statements that need to be deleted. Thus,
  create file Dataset_Subset.all.deleted.rdf, with the same name as the files that you want to delete data from,
  with any affix (e.g. '.all.') but with volume number replaced with 'deleted'. 
  It should be placed in the same directory as the generated files.
  
  The tool will copy all RDF files, skipping the statements from the files named *.*.*.deleted.rdf
  
  Note: file Dataset_Subset.all.deleted.rdf would only be applied to files Dataset_Subset.1.rdf, Dataset_Subset.2.rdf, etc.
  It would not be applied to Dataset_AnotherSubset.1.rdf or any other files. To delete from the latter you need to 
  create file Dataset_AnotherSubset.all.deleted.rdf.
  
How to run

  Run with two parameters: source-dir destination-dir. You may start with an empty destination-dir and see what come there
  
  By default, the tool copies all RDF files and then updates those that have deletions. To copy only the files that have
  associated *.*.*.deleted.rdf file) you need to add optional third parameter -nocopy
  
And now to add statements?

  That easy: in RDF when several files are loaded all their statements are merged. So, create file Dataset_Subset.added.rdf
  and load it with the others. 

