Convert from CSV (preferred)

1. Download and unzip http://download.geonames.org/export/dump/allCountries.zip and http://download.geonames.org/export/dump/alternateNames.zip to input_source
2. Run convert-from-csv.bat

Convert from RDF

1. Download http://download.geonames.org/all-geonames-rdf.zip and unzip it to input_source
2. run preprocess.sh, it will rearrange Geonames records per country in input_source
3. run convert.sh, it will create a filtered dump in output_rdf

For questions and comments, please, email support@annocultor.eu
